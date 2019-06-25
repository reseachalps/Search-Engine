package eu.researchalps.api;

import com.datapublica.companies.api.selector.ErrorSelector;
import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.repository.mongo.ErrorRepository;
import com.datapublica.companies.workflow.service.ErrorHandler;
import eu.researchalps.api.exception.NotFoundException;
import eu.researchalps.api.util.ApiConstants;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
@RequestMapping("/services/error")
public class ErrorServiceApi {
    @Autowired
    private ErrorHandler errorHandler;
    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private ErrorRepository errors;

    /**
     * Find errors using an error selector.
     *
     * @param query The error selector
     * @param page The page number
     * @param size The page size
     * @return The matched errors
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    @ApiOperation("Get the list of errors sorted by date desc")
    public Page<ErrorMessage> getErrors(@RequestParam("select") ErrorSelector query, @RequestParam(value = "page", required = false, defaultValue = "1") int page, @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        return this.errors.select(query, new PageRequest(page - 1, size));
    }

    /**
     * Find an error by id.
     *
     * @param id The error id
     * @return The corresponding error
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    @ApiOperation("Get a specific error")
    public ErrorMessage getError(@PathVariable("id") String id) throws NotFoundException {
        final ErrorMessage error = this.errors.findOne(new ObjectId(id));
        if(error != null) {
            return error;
        }
        throw new NotFoundException("error", id);
    }

    @ResponseBody
    @RequestMapping(value = "/recover", method = RequestMethod.POST)
    @ApiOperation("Recover a selection of errors")
    public long recover(@RequestBody ErrorSelector select) {
        return errorRepository.select(select).filter(errorHandler::recover).count();
    }

    @ResponseBody
    @RequestMapping(value = "/ignore", method = RequestMethod.POST)
    @ApiOperation(value = "Ignoring a selection of errors", notes = "Warning! This could be harmful for your workflow")
    public long dismiss(@RequestBody ErrorSelector select) {
        return  errorRepository.select(select).peek(errorRepository::delete).count();
    }
}
