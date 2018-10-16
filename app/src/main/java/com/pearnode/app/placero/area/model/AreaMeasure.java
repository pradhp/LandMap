package com.pearnode.app.placero.area.model;


import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

/**
 * Created by USER on 12/15/2017.
 */
public class AreaMeasure {

    private double sqFeet = 0;
    private double sqMeters = 0;
    private double acre = 0;
    private double decimals = 0;
    private double hectare = 0;
    private double perimiter = 0;

    public AreaMeasure(Double measureSqFt){
        sqFeet = measureSqFt;
        sqMeters = sqFeet * 0.092903;
        acre = sqFeet / 43560;
        hectare = acre * 0.404686;
        decimals = measureSqFt / 436;
    }

    public double getSqFeet() {
        return this.sqFeet;
    }

    public double getSqMeters() {
        return this.sqMeters;
    }

    public double getAcre() {
        return this.acre;
    }

    public double getDecimals() {
        return this.decimals;
    }

    public double getHectare() {
        return this.hectare;
    }

    public double getPerimiter() {
        return this.perimiter;
    }

    public void setPerimiter(double perimiter) {
        this.perimiter = perimiter;
    }

    public double getValueByField(String fieldName){
        try {
            Method method = getClass().getMethod("get" + StringUtils.capitalize(fieldName));
            return (double) method.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("###.##");
        String content = df.format(sqFeet) + " Sqft, " + df.format(acre)
                + " Acre, " + df.format(decimals) + " Decimals.";
        return content;
    }
}
