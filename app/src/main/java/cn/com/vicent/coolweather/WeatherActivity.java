package cn.com.vicent.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Calendar;

import cn.com.vicent.coolweather.gson.Forecast;
import cn.com.vicent.coolweather.gson.Weather;
import cn.com.vicent.coolweather.util.HttpUtil;
import cn.com.vicent.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String WEATHER_INFO = "weather";
    public static final String WEATHER_ID = "weather_id";
    public static final String KEY = "&key=bc0418b57b2d4918819d3974ac1285d9";
    public static final String BASE_URL = "http://guolin.tech/api/weather?cityid=";

    private static final String TAG = "WeatherActivity";
    /**
     * 天气预报界面
     */
    private ScrollView weatherLayout;
    /**
     * 背景图
     */
    private ImageView windView;
    /**
     * 标题
     */
    private TextView titleCity;
    /**
     * 更新时间
     */
    private TextView titleUpdateTime;
    /**
     * 温度
     */
    private TextView degreeText;
    /**
     * 天气信息
     */
    private TextView weatherInfoText;
    /**
     * 未来天气预报
     */
    private LinearLayout forecastLayout;
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
    /**
     * 数据存储工具
     */
    private SharedPreferences prefs;
    /**
     * 刷新控件
     */
    public SwipeRefreshLayout swLayout;
    public DrawerLayout drawerLayout;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weathers = prefs.getString(WEATHER_INFO,null);
        final String weatherId;
        if(weathers==null){
            weatherId = getIntent().getStringExtra(WEATHER_ID);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }else{
            Weather weather = Utility.handleWeatherResponse(weathers);
            weatherId = weather.basic.weatherId;
            showWeather(weather);
        }
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    /**
     * 请求天气数据
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
//        http://guolin.tech/api/weather?cityid=CN101011300&key=bc0418b57b2d4918819d3974ac1285d9
        String url = BASE_URL+weatherId+KEY;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onFailure: ");
                        Toast.makeText(WeatherActivity.this,"信息加载失败，请稍候再试",Toast.LENGTH_SHORT).show();
                        swLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "onResponse: "+responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null & "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(WEATHER_INFO,responseText);
                            editor.apply();
                            Intent intent = new Intent(WeatherActivity.this,AutoUpdateServices.class);
                            startService(intent);
                            showWeather(weather);
                        }else{
                            Log.d(TAG, "onResponse: ");
                            Toast.makeText(WeatherActivity.this,"信息加载失败，请稍候再试",Toast.LENGTH_SHORT).show();

                        }
                        swLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 加载数据
     * @param weather
     */
    private void showWeather(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmp+"℃";
        String weatherInfo = weather.now.cond.text;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.daily_foreast){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.text_d);
            maxText.setText(forecast.tmp.max);
            minText.setText(forecast.tmp.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            String api = weather.aqi.city.aqi;
            apiText.setText(weather.aqi.city.aqi);
            String pm25 = weather.aqi.city.pm25;
            pm25Text.setText(weather.aqi.city.pm25);
        }
        comfortText.setText("舒适度："+weather.suggestion.comf.text);
        carWashText.setText("洗车指数："+weather.suggestion.cw.text);
        sportText.setText("运动建议："+weather.suggestion.sport.text);
        String imageUrl = prefs.getString(getKey(),null);
        if(imageUrl!=null){
            Glide.with(this).load(imageUrl).into(windView);
        }else{
            loadWindView();
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void loadWindView() {
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String path = response.body().string();
                if(path!=null){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getKey(),path);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(path).into(windView);
                        }
                    });
                }
            }
        });
    }

    /**
     * 获取日期
     * @return
     */
    private String getKey() {

        int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int key = m*10+d;
        return key+"";

    }

    /**
     * 初始化控件
     */
    private void initView() {
        windView = (ImageView) findViewById(R.id.wind_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        swLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_referesh_layout);
        swLayout.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        apiText = (TextView) findViewById(R.id.api_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        homeButton = (Button) findViewById(R.id.nav_button);
        homeButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.nav_button){
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
