package com.datapublica.companies.workflow.service;

/**
 * Queue statistics
 */
public class QueueStats {
    /**
     * Number of items in the queue
     */
    private long count;
    /**
     * In throughput (in message/minutes)
     */
    private int throughputIn;
    /**
     * Out throughput (in message/minutes)
     */
    private int throughputOut;
    /**
     * Number of consumers bound to this queue
     */
    private int consumerCount;

    public QueueStats(int count, int throughputIn, int throughputOut, int consumerCount) {
        this.count = count;
        this.throughputIn = throughputIn;
        this.throughputOut = throughputOut;
        this.consumerCount = consumerCount;
    }

    public long getCount() {
        return count;
    }

    public int getThroughputIn() {
        return throughputIn;
    }

    public int getThroughputOut() {
        return throughputOut;
    }

    public int getConsumerCount() {
        return consumerCount;
    }
}
