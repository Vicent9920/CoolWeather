package cn.com.vicent.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
//    @SerializedName("update")
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
