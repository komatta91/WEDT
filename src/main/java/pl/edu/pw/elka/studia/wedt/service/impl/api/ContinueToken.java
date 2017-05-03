package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContinueToken {
    private String continueToken;

    @JsonProperty("plcontinue")
    public void setContinueTokenPl(String continueToken) {
        this.continueToken = continueToken;
    }

    @JsonProperty("blcontinue")
    public void setContinueTokenBl(String continueToken) {
        this.continueToken = continueToken;
    }

    @JsonProperty("cmcontinue")
    public void setContinueTokenCm(String continueToken) {
        this.continueToken = continueToken;
    }

    public String getContinueToken() {
        return continueToken;
    }
}
