package com.torrent.utils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;

public class TorrentContext {
    private HashMap<String, Object> contextMap;
    private static ThreadLocal<TorrentContext> context;

    private TorrentContext() {
        this.contextMap = new HashMap<>();
        this.contextMap.put(TorrentConstants.SEEDERS, 0);
        this.contextMap.put(TorrentConstants.LEECHERS, 0);
        this.contextMap.put(TorrentConstants.PEER_ADDRESSES, new BlockingSet<InetSocketAddress>());
    }

    public static synchronized TorrentContext getContext() {
        if(context == null) {
            context = ThreadLocal.withInitial(() -> new TorrentContext());
        }
        return context.get();
    }

    public synchronized void addProperty(String key, Object value) throws NullPointerException{
        this.contextMap.put(key, value);
    }

    public synchronized Object getProperty(String key) throws NullPointerException{
        return this.contextMap.getOrDefault(key, null);
    }

    public int getSize() {
        return this.contextMap.size();
    }
}
