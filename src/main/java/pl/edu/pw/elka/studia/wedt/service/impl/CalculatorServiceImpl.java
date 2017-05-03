package pl.edu.pw.elka.studia.wedt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.edu.pw.elka.studia.wedt.service.CalculatorService;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import java.math.BigDecimal;

/**
 * Created by Komatta on 2017-05-03.
 */
@Service
@Lazy
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public BigDecimal calculate(String language, String firstEntry, String secondEntry) {
        return new BigDecimal(0);
    }
}
