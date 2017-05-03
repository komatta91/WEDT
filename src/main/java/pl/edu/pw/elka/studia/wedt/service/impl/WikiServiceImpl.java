package pl.edu.pw.elka.studia.wedt.service.impl;

import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import pl.edu.pw.elka.studia.wedt.service.WikiService;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ArticleTitle;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ContinueToken;
import pl.edu.pw.elka.studia.wedt.service.impl.api.WikiResponse;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class WikiServiceImpl implements WikiService {
    private static final Logger LOGGER = Logger.getLogger(WikiServiceImpl.class);
    private static final String API_URL_ARTICLE_LINKS = "https://{0}.wikipedia.org/w/api.php?action=query&prop=links&pllimit=max&plnamespace=0&format=json&titles={1}&plcontinue={2}";
    private static final String API_URL_ARTICLE_BACKLINKS = "https://{0}.wikipedia.org/w/api.php?action=query&list=backlinks&bllimit=max&blnamespace=0&format=json&bltitle={1}&blcontinue={2}";
    private static final String API_URL_AMBIGUOUS_PAGES = "https://{0}.wikipedia.org/w/api.php?action=query&list=categorymembers&cmlimit=max&cmprop=title&cmnamespace=0&cmtype=page&format=json&cmtitle=Category:All_disambiguation_pages&cmcontinue={2}";
    private static final String API_URL_SEARCH = "https://{0}.wikipedia.org/w/api.php?action=query&list=search&srprop=titlesnippet&srlimit=100&format=json&srsearch={1}";
    private static final String API_URL_STATISTICS = "https://{0}.wikipedia.org/w/api.php?action=query&meta=siteinfo&siprop=statistics&format=json";

    private static Map<String, List<String>> AMBIGUOUS_PAGES_MAP = new HashMap<>(2);

    @Autowired
    public WikiServiceImpl(RestOperations restOperations) {
        this.restOperations = restOperations;

        for(Pair<String,String> langs : getLanguages()) {
            if(AMBIGUOUS_PAGES_MAP.get(langs.getValue0()) == null) {
                LOGGER.info("Initializing ambiguous pages list for lang:"+langs.getValue0());
                AMBIGUOUS_PAGES_MAP.put(langs.getValue0(), collectAll(API_URL_AMBIGUOUS_PAGES, langs.getValue0(), ""));
                LOGGER.info("Ambiguous pages list initialized for lang:"+langs.getValue0());
            }
        }
    }

    private RestOperations restOperations;

    private List<String> collectAll(String urlTemplate, String language, String search){
        List<String> result = new ArrayList<>();
        String continueToken = "||";
        try {
            while (continueToken != null) {
                    String url = MessageFormat.format(urlTemplate, language, search, continueToken);
                WikiResponse wikiResponse = restOperations.getForObject(url, WikiResponse.class);
                LOGGER.debug(MessageFormat.format("Response for URL {0} is {1}", url, wikiResponse));
                ContinueToken token = wikiResponse.getContinueToken();
                if (token != null) {
                    continueToken = token.getContinueToken();
                } else {
                    continueToken = null;
                }
                List<ArticleTitle> articleTitleList;
                if (wikiResponse.getQuery().getPages() != null) {
                    articleTitleList = wikiResponse.getQuery().getPages().getQueryResult().getArticleTitleList();
                } else {
                    articleTitleList = wikiResponse.getQuery().getSearch();
                }
                for (ArticleTitle articleTitle : articleTitleList) {
                    result.add(articleTitle.getTitle());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred for url " + urlTemplate + " and search: " + search, e);
        }
        return result;
    }

    @Override
    public List<Pair<String,String>> getLanguages() {
        List<Pair<String,String>> result = new ArrayList<>();
        result.add(new Pair<>("pl", "Polski"));
        result.add(new Pair<>("en", "English"));
        return result;
    }

    @Override
    public BigInteger getTotalArticlesNumber(String language) {
        BigInteger result = null;
        try {
            String url = MessageFormat.format(API_URL_STATISTICS, language);
            WikiResponse wikiResponse = restOperations.getForObject(url, WikiResponse.class);
            LOGGER.debug(MessageFormat.format("Response for URL {0} is {1}", url, wikiResponse));
            String articleNumber = wikiResponse.getQuery().getStatistics().getArticles();
            result = new BigInteger(articleNumber);
        }catch (Exception e){
            LOGGER.error("Exception occurred for statistics", e);
        }
        return result;
    }

    @Override
    public List<String> getEntries(String language, String search) {
        if(StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        List<String> entries = collectAll(API_URL_SEARCH, language, search);
        List<String> forbidden = getForbiddenArticleTitles(language);
        LOGGER.info(forbidden);
        entries.removeAll(forbidden);
        return entries;
    }

    @Override
    public List<String> getForbiddenArticleTitles(String language) {
        return AMBIGUOUS_PAGES_MAP.get(language);
    }

    @Override
    public List<String> getReferencesOfArticle(String language, String search) {
        if(StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        return collectAll(API_URL_ARTICLE_LINKS, language, search);
    }

    @Override
    public List<String> getReferencesToArticle(String language, String search) {
        if(StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        return collectAll(API_URL_ARTICLE_BACKLINKS, language, search);
    }

}
