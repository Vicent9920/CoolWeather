package cn.com.vicent.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Now {
    @SerializedName("tmp")
    public String tmp;
    @SerializedName("cond")
    public Cond cond;
    public String hum;
    public String vis;
    public Wind wind;
    public class Wind{
        public String dir;
        public String sc;
    }
    public class Cond{
        @SerializedName("txt")
        public String text;
        public String code;
    }
}
