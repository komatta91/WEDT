package pl.edu.pw.elka.studia.wedt.service.impl;

import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Tuple;
import org.la4j.vector.dense.BasicVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import pl.edu.pw.elka.studia.wedt.controller.response.CalculateResponse;
import pl.edu.pw.elka.studia.wedt.service.CalculatorService;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class CalculatorServiceImpl implements CalculatorService {
    private Logger LOGGER = Logger.getLogger(CalculatorServiceImpl.class);

    private int SCALE = 20;

    private int N_THREADS = 25;

    @Autowired
    private WikiService wikiService;

    private BigDecimal googleNormalizedDistance(StopWatch stopWatch, String language, String firstEntry, String secondEntry){
        //Wiki does not provide backlink count in it's API: https://phabricator.wikimedia.org/T49173
        //therefore counting must be done here
        stopWatch.start("GoogleNormalizedDistance");
        BigInteger wikiArticlesAmount = wikiService.getTotalArticlesNumber(language);
        List<String> firstList = wikiService.getReferencesToArticle(language, firstEntry);
        List<String> secondList = wikiService.getReferencesToArticle(language, secondEntry);
        List<String> commonList = new ArrayList<>();
        commonList.addAll(firstList);
        commonList.retainAll(secondList);//This list might be worth presenting to the end-user
        double numerator = Math.log(Math.max(1, Math.max(firstList.size(), secondList.size()))) - Math.log(Math.max(1, commonList.size()));
        double denominator = Math.log(wikiArticlesAmount.doubleValue()) - Math.log(Math.max(1, Math.min(firstList.size(), secondList.size())));
        BigDecimal result = new BigDecimal(numerator).divide(new BigDecimal(denominator),SCALE, BigDecimal.ROUND_HALF_EVEN);
        stopWatch.stop();
        LOGGER.info(stopWatch.getLastTaskInfo().getTaskName() + ": running time (millis) = " +  stopWatch.getLastTaskInfo().getTimeMillis());
        return result;
    }

    private BigDecimal angleMeasure(StopWatch stopWatch, String language, String firstEntry, String secondEntry) {
        stopWatch.start("AngleMeasure");


        BigInteger wikiArticlesAmount = wikiService.getTotalArticlesNumber(language);
        List<String> firstList = wikiService.getReferencesOfArticle(language, firstEntry);
        List<String> secondList = wikiService.getReferencesOfArticle(language, secondEntry);
        Set<String> distinctLinks = new LinkedHashSet<>(firstList);
        distinctLinks.addAll(secondList);
        Map<String, BigDecimal> firstVector = calculateWeights(language, wikiArticlesAmount, distinctLinks, firstList);
        Map<String, BigDecimal> secondVector = calculateWeights(language, wikiArticlesAmount, distinctLinks, secondList);

        StringBuilder message = new StringBuilder();
        for (String key : distinctLinks) {
            message.append("Key: " + key);
            if (firstVector.containsKey(key)) {
                message.append(" - firstVector: " + firstVector.get(key).toPlainString());
            }
            else {
                message.append(" - firstVector: null");
            }
            if (secondVector.containsKey(key)) {
                message.append(", secondVector: " + secondVector.get(key).toPlainString());
            }
            else {
                message.append(", secondVector: null");
            }
            message.append("\n");
        }
        LOGGER.info(message.toString());

        BigDecimal result = calculateAngle(firstVector, secondVector);
        stopWatch.stop();
        LOGGER.info(stopWatch.getLastTaskInfo().getTaskName() + ": running time (millis) = " +  stopWatch.getLastTaskInfo().getTimeMillis());
        return result;
    }

    private Map<String, BigDecimal> calculateWeights(final String language, final BigInteger wikiArticlesAmount, Set<String> distinctLinks, List<String> existingList) {
        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        List<Future<Pair<String, BigDecimal>>> results = new ArrayList<>();

        for (final String link: distinctLinks) {
            if (!existingList.contains(link)) {
                results.add(executor.submit(new Callable<Pair<String, BigDecimal>>() {
                    @Override
                    public Pair<String, BigDecimal> call() throws Exception {
                        return new Pair<>(link, new BigDecimal(0));
                    }
                }));
            } else {
                results.add(executor.submit(new Callable<Pair<String, BigDecimal>>() {
                    @Override
                    public Pair<String, BigDecimal> call() throws Exception {
                        try {
                            BigDecimal wikiAmmount = new BigDecimal(wikiArticlesAmount.toString());
                            BigDecimal refAmmount = new BigDecimal(wikiService.getReferencesToArticleAmount(language, link));
                            BigDecimal result = new BigDecimal(Math.log(wikiAmmount.divide(refAmmount, SCALE, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
                            return new Pair<>(link, result);
                        } catch (Exception e){
                            LOGGER.error("Calc failed for " + link, e);
                            return new Pair<>(link, new BigDecimal(Math.log(wikiArticlesAmount.doubleValue())));
                        }
                    }
                }));
            }
        }

        Map<String, BigDecimal> result = new HashMap<>(distinctLinks.size());
        for (Future<Pair<String, BigDecimal>> future: results) {
            try {
                Pair<String, BigDecimal> pair = future.get();
                result.put(pair.getValue0(), pair.getValue1());
            } catch (Exception e) {
                LOGGER.error(e);
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
        BigDecimal radAngle = new BigDecimal(Math.acos(innerProduct.divide(v1Norm.multiply(v2Norm), SCALE, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
        LOGGER.info("radAngle: " + radAngle);
        return radAngle.divide(new BigDecimal(Math.PI), SCALE, BigDecimal.ROUND_HALF_EVEN);
    }


    @Override
    public CalculateResponse calculate(String language, String firstEntry, String secondEntry) {
        StopWatch stopWatch = new StopWatch(this.getClass().getSimpleName());
        BigDecimal googleDistance = googleNormalizedDistance(stopWatch, language, firstEntry, secondEntry);
        BigDecimal angle = angleMeasure(stopWatch, language,firstEntry,secondEntry);
        CalculateResponse response = new CalculateResponse();
        response.setGoogleDistance(googleDistance.toPlainString());
        response.setAngle(angle.toPlainString());
        response.setFinalScore(googleDistance.add(angle).divide(new BigDecimal(2), SCALE, BigDecimal.ROUND_HALF_EVEN).toPlainString());
        LOGGER.info(response.toString());
        LOGGER.info(stopWatch.prettyPrint());
        return response;
    }
}
