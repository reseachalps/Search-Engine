package eu.researchalps.api;

import com.datapublica.companies.model.scheduler.ScheduledMessage;
import com.datapublica.companies.model.scheduler.TriggerInfo;
import com.datapublica.companies.repository.mongo.ErrorRepository;
import com.datapublica.companies.repository.mongo.ScheduledMessageRepository;
import com.datapublica.companies.workflow.service.QueueService;
import com.datapublica.companies.workflow.service.QueueStats;
import net.redhogs.cronparser.CronExpressionDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 *
 */
@Controller
@RequestMapping("/services/status")
public class CoreStatusApi {
    @Autowired
    private QueueService queueService;

    @Autowired
    private ScheduledMessageRepository scheduledMessageRepository;

    @Autowired
    private ErrorRepository errorRepository;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public CoreStatus status() {
        return new CoreStatus(queueService, errorRepository, scheduledMessageRepository);
    }

    public static class CoreStatus {
        public Map<String, QueueStats> queues;
        public Map<String, Integer> exchanges;
        public Map<String, Long> errors;
        public Map<String, ScheduledMessageDTO> scheduledMessages;
        public long totalErrors;

        public CoreStatus(QueueService service, ErrorRepository repository, ScheduledMessageRepository scheduledMessageRepository) {
            this.totalErrors = repository.count();
            errors = new HashMap<>();

            queues = service.queueStats();
            queues.keySet().forEach(queue -> errors.put(queue, repository.countByQueue(queue)));
            exchanges = service.exchangeStats();

            scheduledMessages = new HashMap<>();
            StreamSupport.stream(scheduledMessageRepository.findAll().spliterator(), false)
                    .forEach(message -> scheduledMessages.put(message.getId(), new ScheduledMessageDTO(message)));
        }
    }

    public static class ScheduledMessageDTO {
        public ScheduledMessage scheduledMessage;

        public ScheduledMessageDTO(ScheduledMessage scheduledMessage) {
            this.scheduledMessage = scheduledMessage;
        }

        public String getTriggerHuman() {
            TriggerInfo triggerInfo = scheduledMessage.getTriggerInfo();
            switch (triggerInfo.type) {
                case CRON:
                    try {
                        return CronExpressionDescriptor.getDescription(triggerInfo.expression, Locale.ENGLISH);
                    } catch (ParseException e) {
                        return triggerInfo.type.name() + " : " + triggerInfo.expression;
                    }
                case RATE:
                case FIXED_RATE:
                    return triggerInfo.type.name() + " : period " + triggerInfo.period + " s";
                default:
                    return triggerInfo.type.name();
            }
        }
    }
}
