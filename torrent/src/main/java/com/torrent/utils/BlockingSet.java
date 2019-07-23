package com.torrent.utils;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingSet extends LinkedBlockingQueue<InetSocketAddress> {
    private Set<InetSocketAddress> set = new ConcurrentHashMap().newKeySet();
    @Override
    public boolean add(InetSocketAddress address) {
        if(!set.contains(address)) {
            try {
                super.put(address);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            set.add(address);
            return true;
        }
        return false;
    }

    @Override
    public InetSocketAddress take() {
        InetSocketAddress frontAddr = null;
        try {
            frontAddr = super.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        set.remove(frontAddr);
        return frontAddr;
    }

    @Override
    public boolean addAll(Collection<? extends InetSocketAddress> collection) {
        for(InetSocketAddress addr: collection) {
            this.add(addr);
        }
        return true;
    }

    @Override
    public int size() {
        return super.size();
    }
}
