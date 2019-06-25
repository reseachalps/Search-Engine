package eu.researchalps.api;

import com.datapublica.companies.workflow.service.QueueService;
import com.datapublica.companies.workflow.service.QueueSubscriber;
import com.datapublica.companies.workflow.service.impl.AmqpQueueService;
import eu.researchalps.api.util.ApiConstants;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Controller
@RequestMapping("/services/queue")
public class QueueServiceApi {
    @Autowired
    private QueueService service;

    private Map<String, QueueSubscriber> subscriberMap = Maps.newHashMap();

    @PostConstruct
    public void init() {
        for (QueueSubscriber subscriber : service.getSubscribers()) {
            subscriberMap.put(subscriber.getClass().getName(), subscriber);
        }
    }

    @RequestMapping(value = "/subscribers", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation("Get the list of all internal subscribers")
    public Set<String> getListeners() {
        return subscriberMap.keySet();
    }

    @RequestMapping(value = "/subscribers/stop", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @ApiOperation("Stop an internal subscriber based on a name")
    public ApiConstants.OK stopListener(@RequestParam String subscriber) {
        service.stop(subscriberMap.get(subscriber));
        return ApiConstants.OK_MESSAGE;
    }

    @RequestMapping(value = "/subscribers/resume", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @ApiOperation("Resume an internal subscriber based on a name")
    public ApiConstants.OK resumeListener(@RequestParam String subscriber) {
        service.resume(subscriberMap.get(subscriber));
        return ApiConstants.OK_MESSAGE;
    }

    @RequestMapping(value = "/throttle", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    @ApiOperation("Update the throttle limit")
    public ApiConstants.OK setThrottle(@RequestParam @ApiParam(required = true, defaultValue = "0") int messagesPerSecond) {
        ((AmqpQueueService) service).setThrottleLimit(messagesPerSecond);
        return ApiConstants.OK_MESSAGE;
    }
}
