package eu.researchalps.workflow.search;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.WordStemMapping;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.repository.WordStemMappingRepository;
import eu.researchalps.search.model.FullStructureIndex;
import eu.researchalps.search.repository.FullStructureIndexRepository;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.full.FullStructureTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IndexStructureProcess extends QueueComponent implements QueueListener<String> {
    public static final MessageQueue<String> QUEUE = MessageQueue.get("INDEX_STRUCTURE", String.class);
    private static final Logger log = LoggerFactory.getLogger(IndexStructureProcess.class);

    @Autowired
    private FullStructureService fsService;

    @Autowired
    private FullStructureMapper mapper;

    @Autowired
    private FullStructureIndexRepository index;

    @Autowired
    private WordStemMappingRepository wordStemMappingRepository;

    @Autowired
    private StemmingService stemmingService;


    @Override
    public void receive(String id) {
        FullStructure fs;
        try(FullStructureTransaction tx = fsService.tx(id, false)) {
            if (tx == null || tx.getData() == null) {
                // Delete the structure in the index
                index.delete(id);
                // Also delete its wordStemMapping from the DB
                wordStemMappingRepository.delete(id);
                return;
            }
            fs = tx.getData();
            fs.getFieldsToRefresh().forEach(tx::refresh);
            fs.getFieldsToRefresh().clear();
            // This will save
            tx.save(false, true);
        }

        // Index the company
        FullStructureIndex fullStructureIndex = mapper.toFullStructure(fs);
        index.save(fullStructureIndex);

        // also save the stem mapping for this company in mongo
        Map<String, String> stems = new HashMap<>();
        final byte[] vector = stemmingService.buildStemMapping(fullStructureIndex.getRaw(), stems);

        WordStemMapping wordStemMapping = new WordStemMapping(fs.getId(), stems, vector);
        wordStemMappingRepository.save(wordStemMapping);
    }

    @Override
    public MessageQueue<String> getQueue() {
        return QUEUE;
    }
}
