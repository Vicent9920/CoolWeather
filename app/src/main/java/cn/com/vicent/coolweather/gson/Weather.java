package cn.com.vicent.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Weather {
    @SerializedName("status")
    public String status;
    public Basic basic;
    public Aqi aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> daily_foreast;
}
