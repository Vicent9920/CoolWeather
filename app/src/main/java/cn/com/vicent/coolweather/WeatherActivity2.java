package cn.com.vicent.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.com.vicent.coolweather.gson.Weather;
import cn.com.vicent.coolweather.util.HttpUtil;
import cn.com.vicent.coolweather.view.CityWeatherFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity2 extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ViewPager.OnPageChangeListener, View.OnLongClickListener {

    public static final String WEATHER_ID = "weather_id";
    public static final String KEY = "&key=bc0418b57b2d4918819d3974ac1285d9";
    public static final String BASE_URL = "http://guolin.tech/api/weather?cityid=";

    private static final String TAG = "WeatherActivity";

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
     * 数据存储工具
     */
    private SharedPreferences prefs;
    /**
     * 刷新控件
     */
    private SwipeRefreshLayout swLayout;
    /**
     *
     */
    public DrawerLayout drawerLayout;
    /**
     * 城市选择
     */
    private Button homeButton;
    private ViewPager weatherViewPager;


    private MyAdapter adapter;

    private List<String> weatherIdList = new ArrayList<>();
    private List<CityWeatherFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_weather2);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        initWindView();
        getWeatherId();
        initAdapter();


    }

    /**
     * 设置背景图
     */
    private void initWindView() {
        String imageUrl = prefs.getString(getKey(),null);
        if(imageUrl!=null){
            Glide.with(this).load(imageUrl).error(R.mipmap.bg).into(windView);
            Log.d(TAG, "initWindView: "+imageUrl);
        }else{
            loadWindView();
        }
    }


    /**
     * 获取WeatherId集合
     */
    private void getWeatherId() {
        int size = prefs.getInt("id_size",0);
        if(size>0){
            for (int i = 0; i <size ; i++) {
                String id = prefs.getString(i+"",null);
                Log.d(TAG, "getWeatherId: "+id);
                if(id!=null)
                weatherIdList.add(id);
            }
        }
    }

    private void initAdapter() {
        for (int i = 0; i < weatherIdList.size(); i++) {
            String id = weatherIdList.get(i);
            if(!TextUtils.isEmpty(id)){
                CityWeatherFragment fragment = new CityWeatherFragment(new Listener());
                Bundle b=new Bundle();
                b.putString(WEATHER_ID,id);
                fragment.setArguments(b);
                fragments.add(fragment);
            }

        }
        adapter = new MyAdapter(getSupportFragmentManager());
        weatherViewPager.setAdapter(adapter);
    }





    /**
     * 加载数据
     * @param weather
     */
    private void showWeather(Weather weather) {
        initWindView();
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmp+"℃";
        String weatherInfo = weather.now.cond.text;
        titleCity.setText(cityName);
        titleUpdateTime.setText("更新时间: "+updateTime);
        swLayout.setRefreshing(false);
//        saveCityValues(weather);
    }

    /**
     * 保存数据
     */
    private void saveCityValues(Weather weather) {
        int index = weatherViewPager.getCurrentItem();
        prefs.edit().putInt("index",index).apply();
        prefs.edit().putString(index+"id",weather.basic.weatherId).apply();
        prefs.edit().putString(index+"city",weather.basic.cityName).apply();
    }

    private void loadWindView() {
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: "+e.toString()+call.toString());
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

                            Glide.with(WeatherActivity2.this).load(path).error(R.mipmap.bg).into(windView);
                            Log.d(TAG, "run: "+path);
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
        int key = d-10==0?70:(d-1)*10;
        String oldValues = prefs.getString("20",null);
        if(oldValues!=null){
            int index = (d-1)*10;
            prefs.edit().putString(index+"",null).apply();
        }
        return d+"";

    }

    /**
     * 初始化控件
     */
    private void initView() {
        weatherViewPager = (ViewPager) findViewById(R.id.weather_content);
        weatherViewPager.addOnPageChangeListener(this);
        windView = (ImageView) findViewById(R.id.wind_view);
        loadWindView();
        titleCity = (TextView) findViewById(R.id.title_city);
        titleCity.setOnLongClickListener(this);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        //过滤颜色
//        windView.setColorFilter(Color.argb(64,256,256,256));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        swLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_referesh_layout);
        //设置刷新条颜色
        swLayout.setColorSchemeResources(R.color.colorPrimary);
        swLayout.setOnRefreshListener(this);
        homeButton = (Button) findViewById(R.id.nav_button);
        homeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.nav_button){
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onRefresh() {
        CityWeatherFragment fragment = fragments.get(weatherViewPager.getCurrentItem());
        fragment.requestWeather();
    }

    public void openActivity(String id,boolean isAdd){
        if(isAdd){//动态添加城市
            weatherIdList.add(id);
            CityWeatherFragment fragment = new CityWeatherFragment(new Listener());
            Bundle bundle = new Bundle();
            bundle.putString(WEATHER_ID,id);
            fragment.setArguments(bundle);
            fragments.add(fragment);
            adapter.notifyDataSetChanged();
            weatherViewPager.setCurrentItem(fragments.size()-1);

        }else {//直接打开选择城市
            int index = weatherIdList.indexOf(id);
            weatherViewPager.setCurrentItem(index);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        CityWeatherFragment fragment = fragments.get(position);
        fragment.requestWeather();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onLongClick(View view) {

        if(view instanceof TextView){
            Snackbar.make(view,"确定要删除当前城市?",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int deleteIndex = weatherViewPager.getCurrentItem();
                    weatherIdList.remove(deleteIndex);
                    fragments.remove(deleteIndex);
                    adapter.notifyDataSetChanged();
                    int currentIndex = deleteIndex==0?0:deleteIndex-1;
                    if(fragments.size()>0){
                        weatherViewPager.setCurrentItem(currentIndex);
                    }else {
                        weatherViewPager.postInvalidate();
                    }
                    doSaveValueThings();
                }
            });

        }
        return false;
    }

    /**
     * 当删除城市后的数据
     */
    private void doSaveValueThings() {
        SharedPreferences.Editor editor = prefs.edit();
        int size = weatherIdList.size();
        editor.putString(size+"",null);
        if(size>0){
            editor.putInt("id_size",size+1);
            for (int i = 0; i < size; i++) {
                editor.putString(i+"",weatherIdList.get(i));
            }
        }else if(size==0){
            editor.putInt("id_size",size+1);
        }
        editor.commit();

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
    public interface OnFragmentToActivityListener{
        void upDateWeather(Weather weather);
    }
    class Listener implements OnFragmentToActivityListener{

        @Override
        public void upDateWeather(Weather weather) {
            showUpdateTime();
            showWeather(weather);

        }
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
}
