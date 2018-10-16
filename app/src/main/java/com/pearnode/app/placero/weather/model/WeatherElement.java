package com.pearnode.app.placero.weather.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 11/19/2017.
 */
public class WeatherElement {

    private String uniqueId = "";
    private String positionId = "";
    private String temperature = "";
    private String conditionText = "";
    private String conditionCode = "";
    private String address = "";
    private String windChill = "";
    private String windDirection = "";
    private String windSpeed = "";
    private String humidity = "";
    private String visibility = "";
    private String createdOn = "";
    private List<ForecastElement> forecast = new ArrayList<>();

    public String getPositionId() {
        return this.positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getTemperature() {
        return this.temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWindChill() {
        return this.windChill;
    }

    public void setWindChill(String windChill) {
        this.windChill = windChill;
    }

    public String getWindDirection() {
        return this.windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindSpeed() {
        return this.windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getVisibility() {
        return this.visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<ForecastElement> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastElement> forecast) {
        this.forecast = forecast;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
