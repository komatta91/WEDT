package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContinueToken {
    private String parameterName;
    private String continueToken;

    @JsonAnySetter
    public void setContinueTokenPl(String parameterName, String continueToken) {
        this.parameterName = parameterName;
        this.continueToken = continueToken;
    }

    @JsonProperty("continue")
    public void setContinueTokenBl(String continueToken) { }

    public String getParameterName() {
        return parameterName;
    }

    public String getContinueToken() {
        return continueToken;
    }
}
