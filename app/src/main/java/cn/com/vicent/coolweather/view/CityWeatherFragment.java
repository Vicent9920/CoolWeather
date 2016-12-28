package cn.com.vicent.coolweather.view;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import cn.com.vicent.coolweather.R;
import cn.com.vicent.coolweather.WeatherActivity2;
import cn.com.vicent.coolweather.gson.Forecast;
import cn.com.vicent.coolweather.gson.Weather;
import cn.com.vicent.coolweather.util.HttpUtil;
import cn.com.vicent.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.R.attr.id;

/**
 * 城市数据
 */
public class CityWeatherFragment extends Fragment {

    private static final String TAG = "CityWeatherFragment";
    /**
     * 风向
     */
    private TextView windDirText;
    /**
     * 风力
     */
    private TextView windScText;
    /**
     * 相对湿度
     */
    private TextView humText;
    /**
     * 能见度
     */
    private TextView visText;
    /**
     * 温度
     */
    private TextView degreeText;
    /**
     * 天气信息
     */
    private TextView weatherInfoText;
    /**
     * 天气信息图片
     */
    private ImageView weatherCodeImg;
    /**
     * 未来天气预报
     */
    private LinearLayout forecastLayout;
    /**
     * 空气质量等级
     */
    private TextView qltyText;
    /**
     * 空气指数
     */
    private TextView apiText;
    /**
     * PM2.5指数
     */
    private TextView pm25Text;
    /**
     * 舒适度
     */
    private TextView comfortText;
    /**
     * 洗车指数
     */
    private TextView carWashText;
    /**
     * 运动建议
     */
    private TextView sportText;

    private Activity context;
    private WeatherActivity2.OnFragmentToActivityListener listener;
    private String weatherId;
    public CityWeatherFragment(WeatherActivity2.OnFragmentToActivityListener listener){
        Log.d(TAG, "CityWeatherFragment: ");
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        View layout = inflater.inflate(R.layout.fragment_city_weather,container,false);
        initView(layout);
        Bundle arguments = getArguments();
        weatherId = (String) arguments.get(WeatherActivity2.WEATHER_ID);
        Log.d(TAG, "onAttach: "+id);
        if(weatherId!=null){
            requestWeather();
        }
        return layout;
    }

    /**
     * 初始化控件
     * @param layout
     */
    private void initView(View layout) {
        windDirText = (TextView) layout.findViewById(R.id.wind_dir);

        windScText = (TextView) layout.findViewById(R.id.wind_sc);
        humText = (TextView) layout.findViewById(R.id.hum_text);
        visText = (TextView) layout.findViewById(R.id.vis_text);

        degreeText = (TextView) layout.findViewById(R.id.degree_text);
        weatherInfoText = (TextView) layout.findViewById(R.id.info_text);
        weatherCodeImg = (ImageView) layout.findViewById(R.id.wearher_code);

        forecastLayout = (LinearLayout) layout.findViewById(R.id.forecast_layout);

        qltyText = (TextView) layout.findViewById(R.id.air_qlty);
        apiText = (TextView) layout.findViewById(R.id.api_text);
        pm25Text = (TextView) layout.findViewById(R.id.pm25_text);

        comfortText = (TextView) layout.findViewById(R.id.comfort_text);
        carWashText = (TextView) layout.findViewById(R.id.car_wash_text);
        sportText = (TextView) layout.findViewById(R.id.sport_text);

    }


    /**
     * 加载数据
     */
    private void showWeather(Weather weather) {
        Log.d(TAG, "showWeather: ");
//        String cityName = weather.basic.cityName;
//        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmp+"℃";
        Log.d(TAG, "showWeather: "+degree);
        String weatherInfo = weather.now.cond.text;
//        titleCity.setText(cityName);
//        titleUpdateTime.setText("更新时间: "+updateTime);
        windDirText.setText(weather.now.wind.dir);
        windScText.setText(weather.now.wind.sc+"级");
        humText.setText(weather.now.hum+"%");
        visText.setText(weather.now.vis+"km");
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        Glide.with(this).load("http://files.heweather.com/cond_icon/"+weather.now.cond.code+".png").into(weatherCodeImg);

        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.daily_foreast){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item,forecastLayout,false);
            ImageView codeImg = (ImageView) view.findViewById(R.id.code_img);
            codeImg.setColorFilter(Color.WHITE);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView tmpText = (TextView) view.findViewById(R.id.tmp_text);
            Glide.with(this).load("http://files.heweather.com/cond_icon/"+forecast.cond.code_d+".png").into(codeImg);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.text_d);
            tmpText.setText(forecast.tmp.max+"° / "+forecast.tmp.min+"°");
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            qltyText.setText("空气质量 "+weather.aqi.city.qlty);
            apiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        comfortText.setText("舒适度："+weather.suggestion.comf.text);
        carWashText.setText("洗车指数："+weather.suggestion.cw.text);
        sportText.setText("运动建议："+weather.suggestion.sport.text);


    }
    /**
     * 请求天气数据
     */
    public void requestWeather() {
//        http://guolin.tech/api/weather?cityid=CN101011300&key=bc0418b57b2d4918819d3974ac1285d9
        String url = WeatherActivity2.BASE_URL+weatherId+WeatherActivity2.KEY;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //TODO 待处理异常
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"数据刷新失败",Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, "requestWeather onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather argum = Utility.handleWeatherResponse(responseText);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(argum!=null & "ok".equals(argum.status)){
                            // TODO 待返回Activity保存responseText
                                    showWeather(argum);
                                    listener.upDateWeather(argum);
                            Log.d(TAG, "requestWeather onResponse: ");
                        }else{
                            Toast.makeText(context,"数据刷新失败",Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            }
        });
    }
    public void refresh(){
        requestWeather();
    }
}
