package pl.edu.pw.elka.studia.wedt.service;

import org.javatuples.Pair;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Komatta on 2017-05-03.
 */
public interface WikiService {
    List<Pair<String,String>> getLanguages();

    BigInteger getTotalArticlesNumber(String language);
    List<String> getForbiddenArticleTitles(String language);
    List<String> getReferencesOfArticle(String language, String search);
    List<String> getReferencesToArticle(String language, String search);
}
