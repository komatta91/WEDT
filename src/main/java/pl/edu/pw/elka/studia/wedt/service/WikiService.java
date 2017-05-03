package pl.edu.pw.elka.studia.wedt.service;

import org.javatuples.Pair;

import java.util.List;

/**
 * Created by Komatta on 2017-05-03.
 */
public interface WikiService {
    List<Pair<String,String>> getLanguages();

    List<String> getEntries(String language, String search);
}
