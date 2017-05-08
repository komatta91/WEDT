package pl.edu.pw.elka.studia.wedt.service.impl;

import org.apache.log4j.Logger;
import org.la4j.vector.dense.BasicVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import pl.edu.pw.elka.studia.wedt.service.CalculatorService;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CalculatorServiceImpl implements CalculatorService {

    @Autowired
    private WikiService wikiService;

    private StopWatch stopWatch = new StopWatch("CalculatorServiceImpl");

    Logger LOGGER = Logger.getLogger(CalculatorServiceImpl.class);

    private BigDecimal googleNormalizedDistance(String language, String firstEntry, String secondEntry){
        //Wiki does not provide backlink count in it's API: https://phabricator.wikimedia.org/T49173
        //therefore counting must be done here
        stopWatch.start("GoogleNormalizedDistance");
        BigInteger wikiArticlesAmount = wikiService.getTotalArticlesNumber(language);
        List<String> firstList = wikiService.getReferencesToArticle(language, firstEntry);
        List<String> secondList = wikiService.getReferencesToArticle(language, secondEntry);
        List<String> commonList = new ArrayList<>();
        commonList.addAll(firstList);
        commonList.retainAll(secondList);//This list might be worth presenting to the end-user
        double numerator = Math.log(Math.max(firstList.size(), secondList.size())) - Math.log(commonList.size());
        double denominator = Math.log(wikiArticlesAmount.doubleValue()) - Math.log(Math.min(firstList.size(), secondList.size()));
        BigDecimal result = new BigDecimal(numerator).divide(new BigDecimal(denominator), BigDecimal.ROUND_HALF_EVEN);
        stopWatch.stop();
        LOGGER.info(stopWatch.getLastTaskInfo().getTaskName() + ": " +  stopWatch.getLastTaskInfo().getTimeMillis());
        return result;
    }

    private BigDecimal angleMeasure(String language, String firstEntry, String secondEntry) {
        stopWatch.start("AngleMeasure");
        BigInteger wikiArticlesAmount = wikiService.getTotalArticlesNumber(language);
        List<String> firstList = wikiService.getReferencesOfArticle(language, firstEntry);
        List<String> secondList = wikiService.getReferencesOfArticle(language, secondEntry);
        Set<String> distinctLinks = new LinkedHashSet<>(firstList);
        distinctLinks.addAll(secondList);
        Map<String, BigDecimal> firstVector = calculateWeights(language, wikiArticlesAmount, distinctLinks, firstList);
        Map<String, BigDecimal> secondVector = calculateWeights(language, wikiArticlesAmount,distinctLinks, secondList);
        BigDecimal result = calculateAngle(firstVector, secondVector);
        stopWatch.stop();
        LOGGER.info(stopWatch.getLastTaskInfo().getTaskName() + ": " +  stopWatch.getLastTaskInfo().getTimeMillis());
        return result;
    }

    private Map<String, BigDecimal> calculateWeights(String language,  BigInteger wikiArticlesAmount, Set<String> distinctLinks, List<String> existingList) {
        Map<String, BigDecimal> result = new HashMap<>(distinctLinks.size());

        for (String link: distinctLinks) {
            if (!existingList.contains(link)) {
                result.put(link, new BigDecimal(0));
            } else {
                int backLinkCount = wikiService.getReferencesToArticle(language, link).size();
                result.put(link, new BigDecimal(Math.log(new BigDecimal(wikiArticlesAmount.toString()).divide(new BigDecimal(backLinkCount), BigDecimal.ROUND_HALF_EVEN).doubleValue())));
            }
        }
        return result;
    }

    private BigDecimal calculateAngle( Map<String, BigDecimal> firstVector, Map<String, BigDecimal> secondVector) {
        BasicVector v1 = new BasicVector(firstVector.size());
        BasicVector v2 = new BasicVector(firstVector.size());
        int i = 0;
        for (String vec: firstVector.keySet()) {
            v1.set(i, firstVector.get(vec).doubleValue());
            v2.set(i, secondVector.get(vec).doubleValue());
            i++;
        }
        BigDecimal innerProduct = new BigDecimal(v1.innerProduct( v2 ));
        BigDecimal v1Norm = new BigDecimal(v1.norm());
        BigDecimal v2Norm = new BigDecimal(v2.norm());
        BigDecimal radAngle = innerProduct.divide(v1Norm.multiply(v2Norm), BigDecimal.ROUND_HALF_EVEN);
        return new BigDecimal(Math.acos(radAngle.divide(new BigDecimal(Math.PI), BigDecimal.ROUND_HALF_EVEN).doubleValue()));
    }


    @Override
    public BigDecimal calculate(String language, String firstEntry, String secondEntry) {
        BigDecimal result = googleNormalizedDistance(language, firstEntry, secondEntry);
        googleNormalizedDistance(language, firstEntry, secondEntry);
        LOGGER.info(stopWatch.prettyPrint());
        return result;
        //return googleNormalizedDistance(language, firstEntry, secondEntry).add(angleMeasure(language,firstEntry,secondEntry)).divide(new BigDecimal(2), 5, BigDecimal.ROUND_HALF_EVEN);
    }
}
