package cn.com.vicent.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Forecast {
//    @SerializedName("date")
    public String date;
    @SerializedName("cond")
    public Cond cond;
    @SerializedName("tmp")
    public Tmp tmp;
    public class Cond{
        @SerializedName("txt_d")
        public String text_d;
    }
    public class Tmp{
//        @SerializedName("max")
        public String max;
//        @SerializedName("min")
        public String min;
    }
}
