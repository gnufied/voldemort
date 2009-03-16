/*
 * Copyright 2008-2009 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.server;

import static voldemort.utils.Utils.croak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import voldemort.VoldemortException;
import voldemort.cluster.Cluster;
import voldemort.cluster.Node;
import voldemort.server.admin.AdminService;
import voldemort.server.http.HttpService;
import voldemort.server.jmx.JmxService;
import voldemort.server.scheduler.SchedulerService;
import voldemort.server.socket.SocketService;
import voldemort.server.storage.StorageService;
import voldemort.store.StorageEngine;
import voldemort.store.Store;
import voldemort.store.filesystem.FilesystemStorageEngine;
import voldemort.store.metadata.MetadataStore;
import voldemort.utils.ByteArray;
import voldemort.utils.ByteUtils;
import voldemort.utils.Props;
import voldemort.utils.SystemTime;
import voldemort.utils.Utils;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;
import voldemort.xml.ClusterMapper;

/**
 * This is the main server, it bootstraps all the services.
 * 
 * It can be embedded or run directly via it's main method.
 * 
 * @author jay
 * 
 */
public class VoldemortServer extends AbstractService {

    protected static final Logger logger = Logger.getLogger(VoldemortServer.class.getName());
    public static final long DEFAULT_PUSHER_POLL_MS = 60 * 1000;

    private final Node identityNode;
    private final List<VoldemortService> services;
    private final ConcurrentMap<String, Store<ByteArray, byte[]>> storeMap;
    private final ConcurrentHashMap<String, StorageEngine<ByteArray, byte[]>> storeEngineMap;
    private final VoldemortConfig voldemortConfig;
    private final MetadataStore metadataStore;
    private final AdminService adminService;

    public static enum SERVER_STATE {
        NORMAL_STATE,
        REBALANCING_STATE,
    }

    public VoldemortServer(VoldemortConfig config) {
        super("voldemort-server");
        this.voldemortConfig = config;

        this.storeMap = new ConcurrentHashMap<String, Store<ByteArray, byte[]>>();
        this.storeEngineMap = new ConcurrentHashMap<String, StorageEngine<ByteArray, byte[]>>();
        this.metadataStore = new MetadataStore(new FilesystemStorageEngine(MetadataStore.METADATA_STORE_NAME,
                                                                           voldemortConfig.getMetadataDirectory()),
                                               storeMap);
        this.identityNode = this.metadataStore.getCluster()
                                              .getNodeById(voldemortConfig.getNodeId());
        this.services = createServices(metadataStore);
        this.adminService = createAdminService(metadataStore, services);
    }

    public VoldemortServer(Props props, Cluster cluster) {
        super("voldemort-server");
        this.voldemortConfig = new VoldemortConfig(props);
        this.identityNode = cluster.getNodeById(voldemortConfig.getNodeId());
        this.storeMap = new ConcurrentHashMap<String, Store<ByteArray, byte[]>>();
        this.storeEngineMap = new ConcurrentHashMap<String, StorageEngine<ByteArray, byte[]>>();
        this.metadataStore = new MetadataStore(new FilesystemStorageEngine(MetadataStore.METADATA_STORE_NAME,
                                                                           voldemortConfig.getMetadataDirectory()),
                                               storeMap);
        // update cluster details in metaDataStore
        metadataStore.put(new ByteArray(ByteUtils.getBytes(MetadataStore.CLUSTER_KEY, "UTF-8")),
                          new Versioned<byte[]>(ByteUtils.getBytes(new ClusterMapper().writeCluster(cluster),
                                                                   "UTF-8"),
                                                new VectorClock()));
        this.services = createServices(metadataStore);
        this.adminService = createAdminService(metadataStore, services);
    }

    public VoldemortServer(VoldemortConfig config, Cluster cluster) {
        super("voldemort-server");
        this.voldemortConfig = config;
        this.identityNode = cluster.getNodeById(voldemortConfig.getNodeId());
        this.storeMap = new ConcurrentHashMap<String, Store<ByteArray, byte[]>>();
        this.storeEngineMap = new ConcurrentHashMap<String, StorageEngine<ByteArray, byte[]>>();
        this.metadataStore = new MetadataStore(new FilesystemStorageEngine(MetadataStore.METADATA_STORE_NAME,
                                                                           voldemortConfig.getMetadataDirectory()),
                                               storeMap);
        // update cluster details in metaDataStore
        metadataStore.put(new ByteArray(ByteUtils.getBytes(MetadataStore.CLUSTER_KEY, "UTF-8")),
                          new Versioned<byte[]>(ByteUtils.getBytes(new ClusterMapper().writeCluster(cluster),
                                                                   "UTF-8"),
                                                new VectorClock()));
        this.services = createServices(metadataStore);
        this.adminService = createAdminService(metadataStore, services);
    }

    private AdminService createAdminService(MetadataStore metaStore,
                                            List<VoldemortService> serviceList) {
        // check if Admin port is valid or not
        try {
            identityNode.getAdminPort();
        } catch(VoldemortException e) {
            logger.warn("Admin Port not set for server Id:" + identityNode.getId()
                        + " starting w/o Admin Service (REBALANCING WILL NOT WORK)");
        }

        return new AdminService("admin-service",
                                storeEngineMap,
                                identityNode.getAdminPort(),
                                voldemortConfig.getAdminCoreThreads(),
                                voldemortConfig.getAdminMaxThreads(),
                                metaStore,
                                serviceList,
                                identityNode.getId());
    }

    private List<VoldemortService> createServices(MetadataStore metaStore) {
        List<VoldemortService> services = Collections.synchronizedList(new ArrayList<VoldemortService>());
        SchedulerService scheduler = new SchedulerService("scheduler-service",
                                                          voldemortConfig.getSchedulerThreads(),
                                                          SystemTime.INSTANCE);
        services.add(scheduler);
        services.add(new StorageService("storage-service",
                                        this.storeMap,
                                        this.storeEngineMap,
                                        scheduler,
                                        voldemortConfig,
                                        metaStore));
        if(voldemortConfig.isHttpServerEnabled())
            services.add(new HttpService("http-service",
                                         this,
                                         voldemortConfig.getMaxThreads(),
                                         identityNode.getHttpPort()));
        if(voldemortConfig.isSocketServerEnabled())
            services.add(new SocketService("socket-service",
                                           storeMap,
                                           identityNode.getSocketPort(),
                                           voldemortConfig.getCoreThreads(),
                                           voldemortConfig.getMaxThreads(),
                                           voldemortConfig.getSocketBufferSize(),
                                           metaStore,
                                           identityNode.getId()));
        if(voldemortConfig.isJmxEnabled())
            services.add(new JmxService("jmx-service",
                                        this,
                                        this.metadataStore.getCluster(),
                                        storeMap,
                                        services));

        // we want services to stop in the opposite order they started
        Collections.reverse(services);

        return services;
    }

    @Override
    protected void startInner() throws VoldemortException {
        logger.info("Starting all services: ");
        long start = System.currentTimeMillis();
        for(VoldemortService service: services)
            service.start();
        long end = System.currentTimeMillis();

        logger.info("Starting Admin Service");
        adminService.start();

        // add a shutdown hook to stop the server
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if(VoldemortServer.this.isStarted())
                    VoldemortServer.this.stop();
            }
        });

        logger.info("Startup completed in " + (end - start) + " ms.");
    }

    /**
     * Attempt to shutdown the server. As much shutdown as possible will be
     * completed, even if intermediate errors are encountered.
     * 
     * @throws VoldemortException
     */
    @Override
    protected void stopInner() throws VoldemortException {
        List<VoldemortException> exceptions = new ArrayList<VoldemortException>();

        // stop adminService
        adminService.stop();

        logger.info("Stoping services:");
        for(VoldemortService service: services) {
            try {
                service.stop();
            } catch(VoldemortException e) {
                exceptions.add(e);
                logger.error(e);
            }
        }
        logger.info("All services stopped.");

        logger.info("Closing Metadata Store");
        try {
            if(metadataStore != null)
                metadataStore.close();
        } catch(VoldemortException e) {
            logger.error("Error while closing metadata store:", e);
        }
        logger.info("MetadataStore closed.");

        if(exceptions.size() > 0)
            throw exceptions.get(0);
    }

    public static void main(String[] args) throws Exception {
        VoldemortConfig config = null;
        try {
            if(args.length == 0)
                config = VoldemortConfig.loadFromEnvironmentVariable();
            else if(args.length == 1)
                config = VoldemortConfig.loadFromVoldemortHome(args[0]);
            else
                croak("USAGE: java " + VoldemortServer.class.getName() + " [voldemort_home_dir]");
        } catch(Exception e) {
            logger.error(e);
            Utils.croak("Error while loading configuration: " + e.getMessage());
        }

        VoldemortServer server = new VoldemortServer(config);
        if(!server.isStarted())
            server.start();
    }

    public Node getIdentityNode() {
        return this.identityNode;
    }

    public Cluster getCluster() {
        return this.metadataStore.getCluster();
    }

    public List<VoldemortService> getServices() {
        return services;
    }

    public VoldemortService getAdminService() {
        return adminService;
    }

    public VoldemortService getService(String name) {
        for(VoldemortService service: services)
            if(service.getName().equals(name))
                return service;
        return null;
    }

    public ConcurrentMap<String, Store<ByteArray, byte[]>> getStoreMap() {
        return storeMap;
    }

    public VoldemortConfig getVoldemortConfig() {
        return this.voldemortConfig;
    }

    public MetadataStore getMetaDataStore() {
        return metadataStore;
    }
}
