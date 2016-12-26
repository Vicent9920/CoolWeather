package cn.com.vicent.coolweather.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.vicent.coolweather.MainActivity;
import cn.com.vicent.coolweather.R;
import cn.com.vicent.coolweather.WeatherActivity;
import cn.com.vicent.coolweather.db.City;
import cn.com.vicent.coolweather.db.County;
import cn.com.vicent.coolweather.db.Province;
import cn.com.vicent.coolweather.util.HttpUtil;
import cn.com.vicent.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 选择城市的Fragment
 * Created by Vicent on 2016/12/24.
 */

public class Choose_Area extends Fragment{
    private static final String TAG = "Choose_Area";
    public static int LEVEL_PROVINCE = 0;
    public static int LEVEL_CITY = 1;
    public static int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) layout.findViewById(R.id.title_text);
        backButton = (Button) layout.findViewById(R.id.back_button);
        listView = (ListView) layout.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(i).getCountyId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra(WeatherActivity.WEATHER_ID,weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swLayout.setRefreshing(true);
                        activity.titleUpdateTime.setVisibility(View.VISIBLE);
                        activity.weatherId = weatherId;
                        activity.requestWeather();

                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for (Province province:
                 provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String adress = "http://guolin.tech/api/china";
            queryFromSever(adress,LEVEL_PROVINCE);
        }
    }
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city: cityList
                 ) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String adress = "http://guolin.tech/api/china/"+provinceCode;
            queryFromSever(adress,LEVEL_CITY);
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
                String tag = county.getCountyName();
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String adress = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromSever(adress,LEVEL_COUNTY);
        }
    }

    private void queryFromSever(String adress, final int level) {
        Log.d(TAG, "queryFromSever: "+adress);
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败！",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                Log.d(TAG, "onResponse: "+responseText);
                if(LEVEL_PROVINCE==level){

                    result = Utility.handleProvinceResponse(responseText);
                }else if(LEVEL_CITY==level){

                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if(LEVEL_COUNTY==level){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(LEVEL_PROVINCE==level){
                                queryProvinces();
                            }else if(LEVEL_CITY==level){
                                queryCities();
                            }else if (LEVEL_COUNTY==level){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }




    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("正在拼命加载，客官请稍候。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if(progressDialog!=null)
        {
            progressDialog.dismiss();
        }
    }
}
