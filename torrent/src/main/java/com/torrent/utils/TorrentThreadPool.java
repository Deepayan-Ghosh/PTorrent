package com.torrent.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TorrentThreadPool {
    private ExecutorService threadPool;
    private static volatile TorrentThreadPool pool;

    private TorrentThreadPool() {
        this.threadPool = Executors.newFixedThreadPool(30);
    }

    public static synchronized TorrentThreadPool getThreadPool() {
        if(pool == null) {
            pool = new TorrentThreadPool();
        }
        return pool;
    }

    public synchronized void execute(Runnable task) {
        threadPool.execute(task);
    }
}
