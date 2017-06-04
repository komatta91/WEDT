package pl.edu.pw.elka.studia.wedt.controller.response;

/**
 * Created by Komatta on 2017-05-08.
 */
public class CalculateResponse {
    private String googleDistance;
    private String angle;
    private String finalScore;
    private String googleTime;
    private String angleTime;
    private String totalTime;

    public String getGoogleDistance() {
        return googleDistance;
    }

    public void setGoogleDistance(String googleDistance) {
        this.googleDistance = googleDistance;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }

    public String getGoogleTime() {
        return googleTime;
    }

    public void setGoogleTime(String googleTime) {
        this.googleTime = googleTime;
    }

    public String getAngleTime() {
        return angleTime;
    }

    public void setAngleTime(String angleTime) {
        this.angleTime = angleTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }
}
