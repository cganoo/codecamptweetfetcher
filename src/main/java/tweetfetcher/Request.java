package tweetfetcher;

import java.util.List;

/**
 * Created by cganoo on 13/11/2015.
 */
public class Request {
    private String account;
    private String region;
    private Object detail;
    private String detailType;
    private String source;
    private String time;
    private String id;
    private List<String> resources;

    public String getAccount() {
        return account;
    }

    public Request setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public Request setRegion(String region) {
        this.region = region;
        return this;
    }

    public Object getDetail() {
        return detail;
    }

    public Request setDetail(Object detail) {
        this.detail = detail;
        return this;
    }

    public String getDetailType() {
        return detailType;
    }

    public Request setDetailType(String detailType) {
        this.detailType = detailType;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Request setSource(String source) {
        this.source = source;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Request setTime(String time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public Request setId(String id) {
        this.id = id;
        return this;
    }

    public List<String> getResources() {
        return resources;
    }

    public Request setResources(List<String> resources) {
        this.resources = resources;
        return this;
    }
}
