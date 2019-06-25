package eu.researchalps.api;

import com.datapublica.companies.model.scheduler.TriggerInfo;
import com.datapublica.companies.workflow.service.scheduler.QueueScheduler;
import eu.researchalps.api.exception.NotFoundException;
import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.workflow.errors.ErrorRecoverProcess;
import eu.researchalps.workflow.menesr.MenesrFetchProcess;
import eu.researchalps.workflow.menesr.RecrawlProcess;
import eu.researchalps.workflow.oai.OAIEntryHarvest;
import eu.researchalps.workflow.search.IndexUpdatedProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 *
 */
@Controller
@RequestMapping("/services/scheduling")
public class SchedulingApi {
    @Autowired
    private QueueScheduler queueScheduler;

    @ResponseBody
    @RequestMapping(value = "/{id}/schedule", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK schedule(@PathVariable("id") String id) {
        switch (id) {
            case RecrawlProcess.PROVIDER + ":" + RecrawlProcess.ID:
                queueScheduler.scheduleJob(RecrawlProcess.PROVIDER, RecrawlProcess.ID, RecrawlProcess.ID, new TriggerInfo("0 0 8 * * SUN"), RecrawlProcess.QUEUE, null, 0);
                break;
            case RecrawlProcess.PROVIDER + ":" + RecrawlProcess.ID_PUBLICATION:
                queueScheduler.scheduleJob(RecrawlProcess.PROVIDER, RecrawlProcess.ID_PUBLICATION, RecrawlProcess.ID_PUBLICATION, new TriggerInfo("0 0 8 15 * ?"), RecrawlProcess.QUEUE, null, 0);
                break;
            case MenesrFetchProcess.PROVIDER + ":" + MenesrFetchProcess.ID_SPP:
                queueScheduler.scheduleJob(MenesrFetchProcess.PROVIDER, MenesrFetchProcess.ID_SPP, MenesrFetchProcess.FetchType.STRUCTURE_PUBLICATION_PROJECT, new TriggerInfo("0 0 23 * * *"), MenesrFetchProcess.QUEUE, null, 0);
                break;
            case MenesrFetchProcess.PROVIDER + ":" + MenesrFetchProcess.ID_CV:
                // Every day at 9 pm, CV
                queueScheduler.scheduleJob(MenesrFetchProcess.PROVIDER, MenesrFetchProcess.ID_CV, MenesrFetchProcess.FetchType.CV_DOI, new TriggerInfo("0 0 8 2 * ?"), MenesrFetchProcess.QUEUE, null, 0);
                break;
            case "oai:INRA":
                // Every day at 9 pm, CV
                queueScheduler.scheduleJob("oai", "INRA", new OAIEntryHarvest.Request("http://oai.prodinra.inra.fr/records", "oai_inra"), new TriggerInfo("0 0 0 * * *"), OAIEntryHarvest.QUEUE_FETCH, null, 0);
                break;
            case "oai:HAL":
                queueScheduler.scheduleJob("oai", "HAL", new OAIEntryHarvest.Request("https://api.archives-ouvertes.fr/oai/hal/", "xml-tei"), new TriggerInfo("0 0 0 * * *"), OAIEntryHarvest.QUEUE_FETCH, null, 0);
                break;
            case IndexUpdatedProcess.PROVIDER + ":" + IndexUpdatedProcess.ID:
                queueScheduler.scheduleJob(IndexUpdatedProcess.PROVIDER, IndexUpdatedProcess.ID, IndexUpdatedProcess.ID, new TriggerInfo("0 30 2 * * *"), IndexUpdatedProcess.QUEUE, null, 0);
                break;
            case ErrorRecoverProcess.PROVIDER + ":" + ErrorRecoverProcess.ID:
                queueScheduler.scheduleJob(ErrorRecoverProcess.PROVIDER, ErrorRecoverProcess.ID, ErrorRecoverProcess.ID, new TriggerInfo("0 45 * * * *"), ErrorRecoverProcess.QUEUE, null, 0);
                break;
            default:
                throw new NotFoundException("scheduling", id);

        }
        return ApiConstants.OK_MESSAGE;
    }


    @ResponseBody
    @RequestMapping(value = "/{id}/now", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK scheduleNow(@PathVariable("id") String id) {
        queueScheduler.adjustNextExecution(id, new Date());
        return ApiConstants.OK_MESSAGE;
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/unschedule", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK unschedule(@PathVariable("id") String id) {
        queueScheduler.cancelJob(id);
        return ApiConstants.OK_MESSAGE;
    }

}
