package pl.edu.pw.elka.studia.wedt.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.edu.pw.elka.studia.wedt.controller.request.CalculateRequest;
import pl.edu.pw.elka.studia.wedt.controller.request.EntryRequest;
import pl.edu.pw.elka.studia.wedt.controller.response.CalculateResponse;
import pl.edu.pw.elka.studia.wedt.service.CalculatorService;
import pl.edu.pw.elka.studia.wedt.service.WikiService;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by User on 2016-08-05.
 */
@Controller
public class MainController {

    @Autowired
    private WikiService wikiService;

    @Autowired
    private CalculatorService calcService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(ModelMap model) {
        model.put("languages", wikiService.getLanguages());
        return "index";
    }

    @PostConstruct
    public void init() {
    }


    @RequestMapping(value = "/EntryList", method = RequestMethod.POST)
    public @ResponseBody
    List<String> getEntryList(@RequestBody EntryRequest request) {
        return wikiService.getEntries(request.getLanguage(), request.getSearch());

    }

    @RequestMapping(value = "/Calculate", method = RequestMethod.POST)
    public @ResponseBody
    CalculateResponse calculate(@RequestBody CalculateRequest request) {
        return calcService.calculate(request.getLanguage(), request.getFirstEntry(), request.getSecondEntry());

    }

}
