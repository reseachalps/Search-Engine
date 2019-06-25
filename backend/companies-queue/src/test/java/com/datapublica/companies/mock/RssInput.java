package com.datapublica.companies.mock;

import com.datapublica.companies.workflow.service.scheduler.ScheduledJobInput;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

/**
 * Greetings traveler!
 * <p>
 * My name is: samuel
 * We are the 12/09/14, 19:50
 */
public class RssInput extends ScheduledJobInput<RssInput.Input<JsonNode>, String> {
    public static class Input<Extra> {
        public String url;
        public Extra extra;

        public Input() {
        }

        public Input(String url, Extra extra) {
            this.url = url;
            this.extra = extra;
        }
    }

    public RssInput(String id, Date timestamp, Date lastExecution, Input<JsonNode> body, String status) {
        super(id, timestamp, lastExecution, body, status);
    }

    public RssInput() {
    }
}