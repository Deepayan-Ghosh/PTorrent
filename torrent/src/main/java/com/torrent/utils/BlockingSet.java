package com.torrent.utils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingSet<T> extends LinkedBlockingQueue<T> {
    private Set<T> set = new ConcurrentHashMap().newKeySet();

    @Override
    public boolean add(T address) {
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
    public T take() {
        T frontAddr = null;
        try {
            frontAddr = super.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        set.remove(frontAddr);
        return frontAddr;
    }

    @Override
    public T poll(int timeout) {
        T frontAddr = super.poll();
        if(frontAddr != null) {
            set.remove(frontAddr);
        }
        return frontAddr;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for(T addr: collection) {
            this.add(addr);
        }
        return true;
    }

    @Override
    public int size() {
        return super.size();
    }
}
