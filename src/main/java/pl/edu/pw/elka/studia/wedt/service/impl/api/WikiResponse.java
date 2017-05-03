package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiResponse {

    @JsonProperty("continue")
    private ContinueToken continueToken;
    private Query query;

    public ContinueToken getContinueToken() {
        return continueToken;
    }

    public void setContinueToken(ContinueToken continueToken) {
        this.continueToken = continueToken;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
