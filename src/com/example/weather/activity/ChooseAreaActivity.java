package com.example.weather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.R;
import com.example.weather.db.WeatherDB;
import com.example.weather.model.City;
import com.example.weather.model.County;
import com.example.weather.model.Province;
import com.example.weather.util.HttpCallbackListener;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherDB weatherDB;
	private List<String> dataList = new ArrayList<String>();

	/**
	 * 省、市、县列表
	 */
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	/**
	 * 选中的省份、市、县和级别
	 */
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	private int currentLevel;
	
	/**
	 * 是否从WeatherActivity中跳转过来。
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity)
		{
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		setContentView(R.layout.choose_area);

		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(ChooseAreaActivity.this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		weatherDB = WeatherDB.getInstance(ChooseAreaActivity.this);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});

		//加载省级数据
		queryProvinces();
	}

	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryProvinces()
	{
		provinceList = weatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for(Province province : provinceList)
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
			
		}else
		{
			queryFromServer(null,"province");
		}
		
	}
	
	/**
	 * 查询某省下的所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryCities()
	{
		cityList = weatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0)
		{
			dataList.clear();
			for(City city : cityList)
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else
		{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 查询某市下的所有县，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryCounties()
	{
		countyList = weatherDB.loadCounties(selectedCity.getId());
		if(countyList.size() > 0)
		{
			dataList.clear();
			for(County county : countyList)
			{
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else
		{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type)
	{
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		}else
		{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		
		showProgressDialog();
		
		//注意该方法是运行在新线程中的，所以如果有UI的操作一定要回到主线程
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type))
				{
					result = Utility.handleProvinceResponse(weatherDB, response);
				}else if("city".equals(type))
				{
					result = Utility.handleCitiesResponse(weatherDB, response, selectedProvince.getId());
				}else if("county".equals(type))
				{
					result = Utility.handleCountiesResponse(weatherDB, response, selectedCity.getId());
				}
				
				if(result)
				{
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type))
							{
								queryProvinces();
							}else if("city".equals(type))
							{
								queryCities();
							}else if("county".equals(type))
							{
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog()
	{
		if(progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog()
	{
		if(progressDialog != null)
		{
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 获取Back按键，根据当前的级别来判断，此时应该返回市列表、省列表还是直接退出
	 */
	@Override
	public void onBackPressed() {
		switch (currentLevel) {
		case LEVEL_COUNTY:
			queryCities();
			
			break;
		case LEVEL_CITY:
			queryProvinces();
			break;
		default:
			
			if(isFromWeatherActivity)
			{
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			
			finish();
		}
	}
}
