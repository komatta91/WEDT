package pl.edu.pw.elka.studia.wedt.service;

import pl.edu.pw.elka.studia.wedt.controller.response.CalculateResponse;

import java.math.BigDecimal;

/**
 * Created by Komatta on 2017-05-03.
 */
public interface CalculatorService {
    CalculateResponse calculate(String language, String firstEntry, String secondEntry);
}

