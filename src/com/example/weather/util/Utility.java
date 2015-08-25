package com.example.weather.util;

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
}
