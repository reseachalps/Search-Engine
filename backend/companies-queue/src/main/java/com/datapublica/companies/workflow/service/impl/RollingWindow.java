package com.datapublica.companies.workflow.service.impl;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Create a rolling time window count (e.g. how much since n seconds)
 */
public class RollingWindow {
    private long rollingTime;
    
    // FIFO
    private Queue<Long> rolling = new LinkedList<>();

    public RollingWindow(long rollingTime) {
        this.rollingTime = rollingTime;
    }

    public synchronized void put() {
        long l = clean();
        rolling.add(l);
    }

    public synchronized int count() {
        clean();
        return rolling.size();
    }

    private synchronized long clean() {
        long l = System.currentTimeMillis();
        while(!rolling.isEmpty() && rolling.peek().compareTo(l - rollingTime) < 0)
            rolling.poll();
        return l;
    }
}
