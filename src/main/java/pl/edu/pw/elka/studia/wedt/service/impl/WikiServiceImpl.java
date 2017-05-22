package pl.edu.pw.elka.studia.wedt.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import pl.edu.pw.elka.studia.wedt.service.WikiService;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ArticleTitle;
import pl.edu.pw.elka.studia.wedt.service.impl.api.ContinueToken;
import pl.edu.pw.elka.studia.wedt.service.impl.api.WikiResponse;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
public class WikiServiceImpl implements WikiService {
    private static final Logger LOGGER = Logger.getLogger(WikiServiceImpl.class);
    private enum ApiTemplate {
        API_URL_ARTICLE_LINKS("https://{0}.wikipedia.org/w/api.php?action=query&prop=links&pllimit=max&plnamespace=0&format=json","titles"),
        API_URL_ARTICLE_BACKLINKS("https://{0}.wikipedia.org/w/api.php?action=query&list=backlinks&bllimit=max&blnamespace=0&format=json", "bltitle"),
        API_URL_AMBIGUOUS_PAGES("https://{0}.wikipedia.org/w/api.php?action=query&list=categorymembers&cmlimit=max&cmprop=title&cmnamespace=0&cmtype=page&format=json&cmtitle=Category:All_disambiguation_pages", null),
        API_URL_SEARCH("https://{0}.wikipedia.org/w/api.php?action=query&list=search&srprop=titlesnippet&srlimit=100&format=json", "srsearch"),
        API_URL_STATISTICS("https://{0}.wikipedia.org/w/api.php?action=query&meta=siteinfo&siprop=statistics&format=json",null);

        private String template;
        private String titleParamName;

        ApiTemplate(String template, String titleParamName) {
            this.template = template;
            this.titleParamName = titleParamName;
        }

        public String getTemplate() {
            return template;
        }

        public String getTitleParamName() {
            return titleParamName;
        }
    }

    private static Cache<Pair<String, String>, Integer> BACKLINK_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000000000L)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    private static Cache<String, Set<String>> AMBIGUOUS_PAGES_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000000000L)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    private void saveBigBacklink(String key, String val){
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(assumeBacklinksFile, true));
            writer.writeNext(new String[]{key, val});
            writer.close();
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
    private void loadBigBacklinks(){
        if(assumeBacklinks) {
            try {
                CSVReader reader = new CSVReader(new FileReader(assumeBacklinksFile));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    BACKLINK_CACHE.put(new Pair<>("en", nextLine[0]), Integer.parseInt(nextLine[1]));
                    LOGGER.info("Assuming backlinks amount for en, " + nextLine[0] + ": " + nextLine[1]);
                }
            } catch (IOException | NumberFormatException e) {
                LOGGER.error(e);
            }
        }
    }

    @Autowired
    private RestOperations restOperations;

    @Autowired
    private Environment environment;

    @Value("${wiki.api.use.disambiguation:true}")
    private Boolean disambiguationEnabled;

    @Value("${wiki.api.assume.backlinks:true}")
    private Boolean assumeBacklinks;

    @Value("${wiki.api.assume.backlinks.file:backlinks_en.cache}")
    private String assumeBacklinksFile;

    @Value("${wiki.api.assume.backlinks.threshold:50000}")
    private Integer assumeBacklinksThreshold;

    @PostConstruct
    public void init() {
        for(Pair<String,String> langs : getLanguages()) {
            if(AMBIGUOUS_PAGES_CACHE.getIfPresent(langs.getValue0()) == null) {
                if(disambiguationEnabled) {
                    LOGGER.info("Initializing ambiguous pages list for lang:" + langs.getValue0());
                    AMBIGUOUS_PAGES_CACHE.put(langs.getValue0(), new HashSet<>(collectAll(ApiTemplate.API_URL_AMBIGUOUS_PAGES, langs.getValue0(), "", false)));
                    LOGGER.info("Ambiguous pages list initialized for lang:" + langs.getValue0());
                }else{
                    LOGGER.info("Ambiguous pages list for lang:" + langs.getValue0()+" disabled");
                    AMBIGUOUS_PAGES_CACHE.put(langs.getValue0(), new HashSet<String>());
                }
            }
        }
        loadBigBacklinks();
    }


    private List<String> collectAll(ApiTemplate apiTemplate, String language, String search, boolean fireOnce){
        List<String> result = new ArrayList<>();
        StopWatch requestStopWatch = new StopWatch(WikiServiceImpl.class.getSimpleName());
        requestStopWatch.start(apiTemplate.name()+": "+language+", "+search);
        try {
            String url = MessageFormat.format(apiTemplate.getTemplate(), language);
            ContinueToken continueToken = null;
            do {
                URIBuilder builder = new URIBuilder(url);
                if(apiTemplate.getTitleParamName() != null) {
                    builder.addParameter(apiTemplate.getTitleParamName(), search);
                }
                if(continueToken != null){
                    builder.addParameter(continueToken.getParameterName(), continueToken.getContinueToken());
                }
                WikiResponse wikiResponse = restOperations.getForObject(builder.build(), WikiResponse.class);
                if(wikiResponse.getQuery() == null){//wiki api slack
                    throw new Exception(restOperations.getForObject(builder.build(), String.class));
                }
                continueToken = wikiResponse.getContinueToken();
                List<ArticleTitle> articleTitleList;
                if (wikiResponse.getQuery().getPages() != null) {
                    articleTitleList = wikiResponse.getQuery().getPages().getQueryResult().getArticleTitleList();
                } else {
                    articleTitleList = wikiResponse.getQuery().getSearch();
                }
                for (ArticleTitle articleTitle : articleTitleList) {
                    result.add(articleTitle.getTitle());
                }
            }while (continueToken != null);
            Set<String> ambiguousArticlesList = getForbiddenArticleTitles(language);
            if(ambiguousArticlesList != null) {
                result.removeAll(ambiguousArticlesList);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred for url " + apiTemplate + " and search: " + search, e);
        }
        requestStopWatch.stop();
        LOGGER.info(requestStopWatch.getLastTaskInfo().getTaskName() + ": running time (millis) = " +  requestStopWatch.getLastTaskInfo().getTimeMillis());
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
            String url = MessageFormat.format(ApiTemplate.API_URL_STATISTICS.getTemplate(), language);
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
        List<String> entries = collectAll(ApiTemplate.API_URL_SEARCH, language, search, true);
        Set<String> forbidden = getForbiddenArticleTitles(language);
        entries.removeAll(forbidden);
        return entries;
    }

    @Override
    public Set<String> getForbiddenArticleTitles(String language) {
        return AMBIGUOUS_PAGES_CACHE.getIfPresent(language);
    }

    @Override
    public List<String> getReferencesOfArticle(String language, String search) {
        if(StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        return collectAll(ApiTemplate.API_URL_ARTICLE_LINKS, language, search, false);
    }

    @Override
    public List<String> getReferencesToArticle(String language, String search) {
        if(StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        return collectAll(ApiTemplate.API_URL_ARTICLE_BACKLINKS, language, search, false);
    }

    @Override
    public Integer getReferencesToArticleAmount(String language, String search) {
        Pair<String, String> key = new Pair<>(language, search);
        Integer result = BACKLINK_CACHE.getIfPresent(key);
        if(result == null){
            result = getReferencesToArticle(language, search).size();
            BACKLINK_CACHE.put(key, result);
            LOGGER.info("Cache put: "+language+", "+search+": "+result);
            if(result > 50000){
                saveBigBacklink(search, result.toString());
            }
        }else{
            LOGGER.info("Cache hit: "+language+", "+search+": "+result);
        }
        return result;
    }

}
