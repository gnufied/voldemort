/**
 * Autogenerated by Thrift
 * 
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 * 
 * Here is the thrift file that defines the MockMessage struct.
 * 
 * #!/usr/local/bin/thrift --gen java
 * 
 * namespace java voldemort.serialization.thrift
 * 
 * struct MockMessage { 1: string name, 2: map<i64, map<string, i32>>
 * mappings, 3: list<i16> intList, 4: set<string> strSet, }
 */
package voldemort.serialization.thrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.facebook.thrift.TBase;
import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TField;
import com.facebook.thrift.protocol.TList;
import com.facebook.thrift.protocol.TMap;
import com.facebook.thrift.protocol.TProtocol;
import com.facebook.thrift.protocol.TProtocolUtil;
import com.facebook.thrift.protocol.TSet;
import com.facebook.thrift.protocol.TStruct;
import com.facebook.thrift.protocol.TType;

@SuppressWarnings("serial")
public class MockMessage implements TBase, java.io.Serializable {

    public String name;
    public Map<Long, Map<String, Integer>> mappings;
    public List<Short> intList;
    public Set<String> strSet;

    public final Isset __isset = new Isset();

    public static final class Isset implements java.io.Serializable {

        public boolean name = false;
        public boolean mappings = false;
        public boolean intList = false;
        public boolean strSet = false;
    }

    public MockMessage() {}

    public MockMessage(String name,
                       Map<Long, Map<String, Integer>> mappings,
                       List<Short> intList,
                       Set<String> strSet) {
        this();
        this.name = name;
        this.__isset.name = true;
        this.mappings = mappings;
        this.__isset.mappings = true;
        this.intList = intList;
        this.__isset.intList = true;
        this.strSet = strSet;
        this.__isset.strSet = true;
    }

    @Override
    public boolean equals(Object that) {
        if(that == null)
            return false;
        if(that instanceof MockMessage)
            return this.equals((MockMessage) that);
        return false;
    }

    public boolean equals(MockMessage that) {
        if(that == null)
            return false;

        boolean this_present_name = true && (this.name != null);
        boolean that_present_name = true && (that.name != null);
        if(this_present_name || that_present_name) {
            if(!(this_present_name && that_present_name))
                return false;
            if(!this.name.equals(that.name))
                return false;
        }

        boolean this_present_mappings = true && (this.mappings != null);
        boolean that_present_mappings = true && (that.mappings != null);
        if(this_present_mappings || that_present_mappings) {
            if(!(this_present_mappings && that_present_mappings))
                return false;
            if(!this.mappings.equals(that.mappings))
                return false;
        }

        boolean this_present_intList = true && (this.intList != null);
        boolean that_present_intList = true && (that.intList != null);
        if(this_present_intList || that_present_intList) {
            if(!(this_present_intList && that_present_intList))
                return false;
            if(!this.intList.equals(that.intList))
                return false;
        }

        boolean this_present_strSet = true && (this.strSet != null);
        boolean that_present_strSet = true && (that.strSet != null);
        if(this_present_strSet || that_present_strSet) {
            if(!(this_present_strSet && that_present_strSet))
                return false;
            if(!this.strSet.equals(that.strSet))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public void read(TProtocol iprot) throws TException {
        TField field;
        iprot.readStructBegin();
        while(true) {
            field = iprot.readFieldBegin();
            if(field.type == TType.STOP) {
                break;
            }
            switch(field.id) {
                case 1:
                    if(field.type == TType.STRING) {
                        this.name = iprot.readString();
                        this.__isset.name = true;
                    } else {
                        TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 2:
                    if(field.type == TType.MAP) {
                        {
                            TMap _map0 = iprot.readMapBegin();
                            this.mappings = new HashMap<Long, Map<String, Integer>>(2 * _map0.size);
                            for(int _i1 = 0; _i1 < _map0.size; ++_i1) {
                                long _key2;
                                Map<String, Integer> _val3;
                                _key2 = iprot.readI64();
                                {
                                    TMap _map4 = iprot.readMapBegin();
                                    _val3 = new HashMap<String, Integer>(2 * _map4.size);
                                    for(int _i5 = 0; _i5 < _map4.size; ++_i5) {
                                        String _key6;
                                        int _val7;
                                        _key6 = iprot.readString();
                                        _val7 = iprot.readI32();
                                        _val3.put(_key6, _val7);
                                    }
                                    iprot.readMapEnd();
                                }
                                this.mappings.put(_key2, _val3);
                            }
                            iprot.readMapEnd();
                        }
                        this.__isset.mappings = true;
                    } else {
                        TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 3:
                    if(field.type == TType.LIST) {
                        {
                            TList _list8 = iprot.readListBegin();
                            this.intList = new ArrayList<Short>(_list8.size);
                            for(int _i9 = 0; _i9 < _list8.size; ++_i9) {
                                short _elem10 = 0;
                                _elem10 = iprot.readI16();
                                this.intList.add(_elem10);
                            }
                            iprot.readListEnd();
                        }
                        this.__isset.intList = true;
                    } else {
                        TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 4:
                    if(field.type == TType.SET) {
                        {
                            TSet _set11 = iprot.readSetBegin();
                            this.strSet = new HashSet<String>(2 * _set11.size);
                            for(int _i12 = 0; _i12 < _set11.size; ++_i12) {
                                String _elem13;
                                _elem13 = iprot.readString();
                                this.strSet.add(_elem13);
                            }
                            iprot.readSetEnd();
                        }
                        this.__isset.strSet = true;
                    } else {
                        TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                default:
                    TProtocolUtil.skip(iprot, field.type);
                    break;
            }
            iprot.readFieldEnd();
        }
        iprot.readStructEnd();
    }

    public void write(TProtocol oprot) throws TException {
        TStruct struct = new TStruct("MockMessage");
        oprot.writeStructBegin(struct);
        TField field = new TField();
        if(this.name != null) {
            field.name = "name";
            field.type = TType.STRING;
            field.id = 1;
            oprot.writeFieldBegin(field);
            oprot.writeString(this.name);
            oprot.writeFieldEnd();
        }
        if(this.mappings != null) {
            field.name = "mappings";
            field.type = TType.MAP;
            field.id = 2;
            oprot.writeFieldBegin(field);
            {
                oprot.writeMapBegin(new TMap(TType.I64, TType.MAP, this.mappings.size()));
                for(long _iter14: this.mappings.keySet()) {
                    oprot.writeI64(_iter14);
                    {
                        oprot.writeMapBegin(new TMap(TType.STRING,
                                                     TType.I32,
                                                     this.mappings.get(_iter14).size()));
                        for(String _iter15: this.mappings.get(_iter14).keySet()) {
                            oprot.writeString(_iter15);
                            oprot.writeI32(this.mappings.get(_iter14).get(_iter15));
                        }
                        oprot.writeMapEnd();
                    }
                }
                oprot.writeMapEnd();
            }
            oprot.writeFieldEnd();
        }
        if(this.intList != null) {
            field.name = "intList";
            field.type = TType.LIST;
            field.id = 3;
            oprot.writeFieldBegin(field);
            {
                oprot.writeListBegin(new TList(TType.I16, this.intList.size()));
                for(short _iter16: this.intList) {
                    oprot.writeI16(_iter16);
                }
                oprot.writeListEnd();
            }
            oprot.writeFieldEnd();
        }
        if(this.strSet != null) {
            field.name = "strSet";
            field.type = TType.SET;
            field.id = 4;
            oprot.writeFieldBegin(field);
            {
                oprot.writeSetBegin(new TSet(TType.STRING, this.strSet.size()));
                for(String _iter17: this.strSet) {
                    oprot.writeString(_iter17);
                }
                oprot.writeSetEnd();
            }
            oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MockMessage(");
        sb.append("name:");
        sb.append(this.name);
        sb.append(",mappings:");
        sb.append(this.mappings);
        sb.append(",intList:");
        sb.append(this.intList);
        sb.append(",strSet:");
        sb.append(this.strSet);
        sb.append(")");
        return sb.toString();
    }

}
