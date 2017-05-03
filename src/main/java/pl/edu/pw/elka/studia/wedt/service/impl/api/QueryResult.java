package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResult {
    private String title;
    private List<ArticleTitle> articleTitleList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ArticleTitle> getArticleTitleList() {
        return articleTitleList;
    }

    @JsonProperty("links")
    public void setArticleTitleList(List<ArticleTitle> articleTitleList) {
        this.articleTitleList = articleTitleList;
    }
}
