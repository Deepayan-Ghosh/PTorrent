package com.torrent.utils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;

public class TorrentContext {
    private HashMap<String, Object> contextMap;
    private static ThreadLocal<TorrentContext> context;

    private TorrentContext() {
        this.contextMap = new HashMap<>();
    }

    public static synchronized TorrentContext getContext() {
        if(context == null) {
            context = ThreadLocal.withInitial(() -> new TorrentContext());
        }
        context.get().addProperty(TorrentConstants.SEEDERS, 0);
        context.get().addProperty(TorrentConstants.LEECHERS, 0);
        context.get().addProperty(TorrentConstants.PEER_ADDRESSES, new HashSet<InetSocketAddress>());
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
