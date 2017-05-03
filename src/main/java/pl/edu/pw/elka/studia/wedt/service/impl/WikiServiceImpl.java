package pl.edu.pw.elka.studia.wedt.service.impl;

import org.javatuples.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class WikiServiceImpl implements WikiService {
    @Override
    public List<Pair<String,String>> getLanguages() {
        List<Pair<String,String>> result = new ArrayList<>();
        result.add(new Pair<String, String>("pl", "Polski"));
        result.add(new Pair<String, String>("en", "English"));
        return result;
    }

    @Override
    public List<String> getEntries(String language, String search) {
        List<String> result = new ArrayList<>();
        result.add("Ala");
        result.add("kot");
        result.add("pies");
        return result;
    }
}
