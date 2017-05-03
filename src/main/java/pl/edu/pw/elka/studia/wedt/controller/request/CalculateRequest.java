package pl.edu.pw.elka.studia.wedt.controller.request;

/**
 * Created by Komatta on 2017-05-03.
 */

public class CalculateRequest {
    private String language;
    private String firstEntry;
    private String secondEntry;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFirstEntry() {
        return firstEntry;
    }

    public void setFirstEntry(String firstEntry) {
        this.firstEntry = firstEntry;
    }

    public String getSecondEntry() {
        return secondEntry;
    }

    public void setSecondEntry(String secondEntry) {
        this.secondEntry = secondEntry;
    }
}
