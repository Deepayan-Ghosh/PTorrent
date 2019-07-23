package com.bencode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public class BencodeEncoder {
    private Logger LOGGER;
    private OutputStream out;

    public BencodeEncoder(OutputStream out) {
        this.LOGGER = Logger.getLogger(BencodeEncoder.class.getName());
        this.out = out;
    }

    public void bencode(Object o) throws IOException {
        if(o instanceof BencodeValue)
            o = ((BencodeValue) o).getValue();

        if(o instanceof byte[])
            bencode((byte[])o);
        if(o instanceof String)
            bencode(((String) o).getBytes());
        if(o instanceof Number)
            bencode((Number)o);
        if(o instanceof List)
            bencode((List<BencodeValue>) o);
        if(o instanceof Map)
            bencode((Map<String, BencodeValue>)o);
    }

    private void bencode(Map<String,BencodeValue> m) throws IOException {
        out.write('d');
        TreeSet<String> keys = new TreeSet<>(m.keySet());
        for(String key : keys) {
                bencode(key);
                bencode(m.get(key));
        }
        out.write('e');
    }

    private void bencode(List<BencodeValue> l) throws IOException {
        out.write('l');
        for(BencodeValue val: l)
            bencode(val);
        out.write('e');
    }

    private void bencode(Number num) throws IOException {
        out.write('i');
        out.write(num.toString().getBytes());
        out.write('e');
    }

    private void bencode(byte[] s) throws IOException {
        out.write(Integer.valueOf(s.length).toString().getBytes());
        out.write(':');
        for(byte b: s)
            out.write(b);
    }
}
