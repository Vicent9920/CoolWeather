package cn.com.vicent.coolweather.gson;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Aqi {
    public City city;
    public class City{
//        @SerializedName("aqi")
        public String aqi;
//        @SerializedName("pm25")
        public String pm25;
        public String qlty;
    }
}
