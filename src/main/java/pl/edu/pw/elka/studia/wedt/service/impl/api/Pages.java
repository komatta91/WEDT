package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pages {
    private QueryResult queryResult;

    public QueryResult getQueryResult() {
        return queryResult;
    }

    @JsonAnySetter
    public void setQueryResult(String pageId,QueryResult queryResult) {
        this.queryResult = queryResult;
    }
}
