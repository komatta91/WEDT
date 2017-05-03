package pl.edu.pw.elka.studia.wedt.service.impl.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by mmajewski on 2017-05-03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Query {
    private Pages pages;
    private Statistics statistics;
    private List<ArticleTitle> search;

    public Pages getPages() {
        return pages;
    }

    public void setPages(Pages pages) {
        this.pages = pages;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public List<ArticleTitle> getSearch() {
        return search;
    }

    @JsonProperty("search")
    public void setSearch(List<ArticleTitle> search) {
        this.search = search;
    }

    @JsonProperty("backlinks")
    public void setBacklinks(List<ArticleTitle> search) {
        this.search = search;
    }

    @JsonProperty("categorymembers")
    public void setCategoryMembers(List<ArticleTitle> search) {
        this.search = search;
    }
}
