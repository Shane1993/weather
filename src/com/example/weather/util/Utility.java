package com.example.weather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.weather.db.WeatherDB;
import com.example.weather.model.City;
import com.example.weather.model.County;
import com.example.weather.model.Province;

public class Utility {

	/**
	 * 解析和处理服务器返回的升级数据
	 * 	服务器返回的省市县数据都是“代号|城市,代号|城市”这种格式的
	 * 	如：01|北京,02|上海,03|天津。。。
	 */
	public synchronized static boolean handleProvinceResponse(WeatherDB weatherDB, String response)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0)
			{
				for(String p : allProvinces)
				{
					//将数据分解成代号和城市
					String array[] = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					
					//将解析出来的数据存储到数据库
					weatherDB.saveProvince(province);
				}
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的市级数据
	 * 	1901|南京,1902|无锡,1903|镇江,1904|苏州。。。
	 */
	public synchronized static boolean handleCitiesResponse(WeatherDB weatherDB, String response, int provinceId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0)
			{
				for(String c : allCities)
				{
					//将数据分解成代号和城市
					String array[] = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					
					//将解析出来的数据存储到数据库
					weatherDB.saveCity(city);
				}
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * 处理和解析服务器返回的县级数据
	 * 	190401|苏州,190402|常熟,190403|张家港。。。
	 */
	public synchronized static boolean handleCountiesResponse(WeatherDB weatherDB, String response, int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0)
			{
				for(String c : allCounties)
				{
					//将数据分解成代号和城市
					String array[] = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					
					//将解析出来的数据存储到数据库
					weatherDB.saveCounty(county);
				}
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
	 * 	数据类型如下
	 * 	{"weatherinfo":
	 *		{"city":"昆山","cityid":"101190404","temp1":"21℃","temp2":"9℃",
	 *		"weather":"多云转小雨","img1":"d1.gif","img2":"n7.gif","ptime":"11:00"}
	 *	}
	 */
	public static void handleWeatherResponse(Context context, String response)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,weatherDesp, publishTime);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 将服务器返回的所有天气信息存储到SharePreferences文件中。
	 */
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
