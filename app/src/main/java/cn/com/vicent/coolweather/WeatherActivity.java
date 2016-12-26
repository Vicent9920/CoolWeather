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
    public TextView titleUpdateTime;
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
    /**
     * 数据存储工具
     */
    private SharedPreferences prefs;
    /**
     * 刷新控件
     */
    public SwipeRefreshLayout swLayout;
    /**
     *
     */
    public DrawerLayout drawerLayout;
    /**
     * 城市选择
     */
    private Button homeButton;
    /**
     * 城市代码
     */
    public String weatherId;


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

        if(weathers==null){
            weatherId = getIntent().getStringExtra(WEATHER_ID);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather();
        }else{
            Weather weather = Utility.handleWeatherResponse(weathers);
            weatherId = weather.basic.weatherId;
            showWeather(weather);
        }
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showUpdateTime();
                requestWeather();
            }
        });
    }

    /**
     * 显示更新时间
     */
    public void showUpdateTime() {
        titleUpdateTime.setVisibility(View.VISIBLE);
        titleUpdateTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleUpdateTime.setVisibility(View.GONE);
                    }
                });
            }
        },2000);
    }

    /**
     * 请求天气数据
     */
    public void requestWeather() {
//        http://guolin.tech/api/weather?cityid=CN101011300&key=bc0418b57b2d4918819d3974ac1285d9
        String url = BASE_URL+weatherId+KEY;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"信息加载失败，请稍候再试",Toast.LENGTH_SHORT).show();
                        swLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
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
        titleUpdateTime.setText("更新时间: "+updateTime);
        windDirText.setText(weather.now.wind.dir);
        windScText.setText(weather.now.wind.sc+"级");
        humText.setText(weather.now.hum+"%");
        visText.setText(weather.now.vis+"km");
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        Glide.with(this).load("http://files.heweather.com/cond_icon/"+weather.now.cond.code+".png").into(weatherCodeImg);

        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.daily_foreast){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
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
        String imageUrl = prefs.getString(getKey(),null);
        if(imageUrl!=null){
            Glide.with(this).load(imageUrl).error(R.mipmap.bg).into(windView);

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
                            Glide.with(WeatherActivity.this).load(path).error(R.mipmap.bg).into(windView);
                        }
                    });
                }
            }
        });
    }

    /**
     * 获取key
     * @return
     */
    private String getKey() {

        int d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int key = d-1==0?7:d-1;
        String oldValues = prefs.getString(key+"",null);
        if(oldValues!=null){
            prefs.edit().putString(d-1+"",null).apply();
        }
        return d+"";

    }

    /**
     * 初始化控件
     */
    private void initView() {
        weatherCodeImg = (ImageView) findViewById(R.id.wearher_code);
        weatherCodeImg.setColorFilter(Color.WHITE);
        windView = (ImageView) findViewById(R.id.wind_view);
        //过滤颜色
//        windView.setColorFilter(Color.argb(64,256,256,256));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        swLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_referesh_layout);
        //设置刷新条颜色
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
        windDirText = (TextView) findViewById(R.id.wind_dir);
        windScText = (TextView) findViewById(R.id.wind_sc);
        humText = (TextView) findViewById(R.id.hum_text);
        visText = (TextView) findViewById(R.id.vis_text);
        qltyText = (TextView) findViewById(R.id.air_qlty);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.nav_button){
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
