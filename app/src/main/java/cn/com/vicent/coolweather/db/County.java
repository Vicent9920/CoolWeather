package cn.com.vicent.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * 国家县区级行政区实体类
 * Created by Vicent on 2016/12/24.
 */

public class County extends DataSupport{
    private int id;
    private String countyName;
    private String countyId;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyId() {
        return countyId;
    }

    public void setCountyId(String countyId) {
        this.countyId = countyId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
