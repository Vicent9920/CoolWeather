package cn.com.vicent.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vicent on 2016/12/25.
 */

public class Suggestion {
    public class Comf{
        @SerializedName("txt")
        public String text;
    }
    public class Cw{
        @SerializedName("txt")
        public String text;
    }
    public class Sport{
        @SerializedName("txt")
        public String text;
    }
    @SerializedName("comf")
    public Comf comf;
    @SerializedName("cw")
    public Cw cw;
//    @SerializedName("sport")
    public Sport sport;
}
