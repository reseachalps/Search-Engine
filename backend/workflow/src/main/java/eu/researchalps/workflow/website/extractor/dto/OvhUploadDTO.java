package eu.researchalps.workflow.website.extractor.dto;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vyncent on 06/01/16.
 */
public class OvhUploadDTO {
    private String id;
    private String targetName;

    private List<byte[]> datas;

    public OvhUploadDTO() {
        datas = new ArrayList<>();
    }

    public List<byte[]> getDatas() {
        return datas;
    }

    public void setDatas(List<byte[]> datas) {
        this.datas = datas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public static class Out {
        private String id;
        List<String> urls;

        public Out() {
        }

        public Out(OvhUploadDTO message) {
            this.id = message.getId();
            if (!CollectionUtils.isEmpty(message.getDatas())) {
                this.urls = new ArrayList<>(message.getDatas().size());
            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getUrls() {
            return urls;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }
    }
}
