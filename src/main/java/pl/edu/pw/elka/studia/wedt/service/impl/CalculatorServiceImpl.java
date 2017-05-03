package pl.edu.pw.elka.studia.wedt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.edu.pw.elka.studia.wedt.service.CalculatorService;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class CalculatorServiceImpl implements CalculatorService {

    @Autowired
    private WikiService wikiService;

    private BigDecimal googleNormalizedDistance(String language, String firstEntry, String secondEntry){
        //Wiki does not provide backlink count in it's API: https://phabricator.wikimedia.org/T49173
        //therefore counting must be done here
        BigInteger wikiArticlesAmount = wikiService.getTotalArticlesNumber(language);
        List<String> firstList = wikiService.getReferencesToArticle(language, firstEntry);
        List<String> secondList = wikiService.getReferencesToArticle(language, secondEntry);
        List<String> commonList = new ArrayList<>();
        commonList.addAll(firstList);
        commonList.retainAll(secondList);//This list might be worth presenting to the end-user

        double numerator = Math.log(Math.max(firstList.size(), secondList.size())) - Math.log(commonList.size());
        double denominator = Math.log(wikiArticlesAmount.doubleValue()) - Math.log(Math.min(firstList.size(), secondList.size()));

        return new BigDecimal(numerator).divide(new BigDecimal(denominator), 5, BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public BigDecimal calculate(String language, String firstEntry, String secondEntry) {
        return googleNormalizedDistance(language, firstEntry, secondEntry);
    }
}
