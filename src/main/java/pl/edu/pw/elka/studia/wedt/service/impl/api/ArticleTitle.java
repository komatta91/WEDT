package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleTitle {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
