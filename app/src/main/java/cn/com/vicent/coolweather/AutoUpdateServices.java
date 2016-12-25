package cn.com.vicent.coolweather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import cn.com.vicent.coolweather.gson.Weather;
import cn.com.vicent.coolweather.util.HttpUtil;
import cn.com.vicent.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateServices extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int anHour = 2*60*60*1000;
        long triggeratTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpdateServices.class);
        PendingIntent p = PendingIntent.getService(this,1,intent,0);
        manager.cancel(p);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggeratTime,p);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        final SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherInfo = pres.getString(WeatherActivity.WEATHER_INFO,null);
        if(weatherInfo!=null){
            Weather weather = Utility.handleWeatherResponse(weatherInfo);
            String url = WeatherActivity.BASE_URL+weather.basic.weatherId+WeatherActivity.KEY;
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherInfo = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(weatherInfo);
                    if(weather!=null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = pres.edit();
                        editor.putString(WeatherActivity.WEATHER_INFO,weatherInfo);
                        editor.apply();
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
