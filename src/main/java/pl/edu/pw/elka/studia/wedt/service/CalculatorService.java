package pl.edu.pw.elka.studia.wedt.service;

import java.math.BigDecimal;

/**
 * Created by Komatta on 2017-05-03.
 */
public interface CalculatorService {
    BigDecimal calculate(String language, String firstEntry, String secondEntry);
}

