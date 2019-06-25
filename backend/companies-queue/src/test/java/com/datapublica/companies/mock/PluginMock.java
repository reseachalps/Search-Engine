package com.datapublica.companies.mock;

import com.datapublica.companies.workflow.MessageQueue;

import java.util.function.Function;

/**
 *
 */
public interface PluginMock<E> extends Function<E, Object> {

    default void fire(E dto, MessageQueue replyTo, MockQueueService mql) {
        Object o = this.apply(dto);
        if(o != null)
            mql.fire(replyTo, o);
    }
}
