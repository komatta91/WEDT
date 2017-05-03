package pl.edu.pw.elka.studia.wedt.service.impl;

import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import pl.edu.pw.elka.studia.wedt.service.WikiService;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ArticleTitle;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ContinueToken;
import pl.edu.pw.elka.studia.wedt.service.impl.api.WikiResponse;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class WikiServiceImpl implements WikiService {
    private static final Logger LOGGER = Logger.getLogger(WikiServiceImpl.class);
    private static final String API_URL_ARTICLE_LINKS = "https://{0}.wikipedia.org/w/api.php?action=query&prop=links&pllimit=max&plnamespace=0&format=json&titles={1}&plcontinue={2}";
    private static final String API_URL_ARTICLE_BACKLINKS = "https://{0}.wikipedia.org/w/api.php?action=query&list=backlinks&bllimit=max&blnamespace=0&format=json&bltitle={1}&blcontinue={2}";
    private static final String API_URL_AMBIGUOUS_PAGES = "https://{0}.wikipedia.org/w/api.php?action=query&list=categorymembers&cmlimit=max&format=json&cmtitle=Category:All_disambiguation_pages&cmcontinue={2}";
    private static final String API_URL_STATISTICS = "https://{0}.wikipedia.org/w/api.php?action=query&meta=siteinfo&siprop=statistics&format=json";

    @Autowired
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
                for (ArticleTitle articleTitle : wikiResponse.getQuery().getPages().getQueryResult().getArticleTitleList()) {
                    result.add(articleTitle.getTitle());
                }
            }
        }catch (Exception e){
            LOGGER.error("Exception occurred for url "+urlTemplate, e);
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
        try {
            String url = MessageFormat.format(API_URL_STATISTICS, language);
            WikiResponse wikiResponse = restOperations.getForObject(url, WikiResponse.class);
            LOGGER.debug(MessageFormat.format("Response for URL {0} is {1}", url, wikiResponse));
            String articleNumber = wikiResponse.getQuery().getStatistics().getArticles();
            return new BigInteger(articleNumber);
        }catch (Exception e){
            LOGGER.error("Exception occurred for statistics", e);
        }
        return null;
    }

    @Override
    public List<String> getForbiddenArticleTitles(String language) {
        return collectAll(API_URL_AMBIGUOUS_PAGES, language, "");
    }

    @Override
    public List<String> getReferencesOfArticle(String language, String search) {
        return collectAll(API_URL_ARTICLE_LINKS, language, search);
    }

    @Override
    public List<String> getReferencesToArticle(String language, String search) {
        return collectAll(API_URL_ARTICLE_BACKLINKS, language, search);
    }

}
