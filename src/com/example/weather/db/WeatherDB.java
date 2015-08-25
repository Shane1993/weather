package com.example.weather.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.weather.model.City;
import com.example.weather.model.County;
import com.example.weather.model.Province;

public class WeatherDB {

	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "weather";
	
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	
	private static WeatherDB weatherDB;
	
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化，单例模式
	 */
	private WeatherDB(Context context)
	{
		WeatherOpenHelper helper = new WeatherOpenHelper(context, DB_NAME, null, VERSION);
		
		db = helper.getWritableDatabase();
	}
	
	/**
	 * 获取WeatherDB的实例
	 */
	public synchronized static WeatherDB getInstance(Context context)
	{
		if (weatherDB == null)
		{
			weatherDB = new WeatherDB(context);
		}
		return weatherDB;
	}
	
	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province)
	{
		if (province != null) 
		{
			db.execSQL("insert into Province "
					+ "(id,province_name,province_code) "
					+ "values (?,?,?)",
					new Object[]{province.getId(),province.getProvinceName(),province.getProvinceCode()});
		}
			
	}
	
	/**
	 * 从数据库读取全国所有的省份信息
	 */
	public List<Province> loadProvinces()
	{
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.rawQuery(
				"select * from Province", null);
			
		while(cursor.moveToNext())
		{
			Province province = new Province();
			province.setId(cursor.getInt(cursor.getColumnIndex("id")));
			province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
			province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
			
			list.add(province);
		}
		
		return list;
	}
	
	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city)
	{
		if (city != null) 
		{
			db.execSQL("insert into City "
					+ "(id,city_name,city_code,province_id) "
					+ "values (?,?,?,?)",
					new Object[]{city.getId(),city.getCityName(),city.getCityCode(),city.getProvinceId()});
		}
		
	}
	
	/**
	 * 从数据库读取某省下所有的城市信息
	 */
	public List<City> loadCities(int provinceId)
	{
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.rawQuery(
				"select * from City where province_id = ? ", 
				new String[]{String.valueOf(provinceId)});
		
		while(cursor.moveToNext())
		{
			City city = new City();
			city.setId(cursor.getInt(cursor.getColumnIndex("id")));
			city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
			city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
			city.setProvinceId(provinceId);
			
			list.add(city);
		}
		
		return list;
	}
	
	/**
	 * 将County实例存储到数据库
	 */
	public void saveCounty(County county)
	{
		if (county != null) 
		{
			db.execSQL("insert into County "
					+ "(id,county_name,county_code,city_id) "
					+ "values (?,?,?,?)",
					new Object[]{county.getId(),county.getCountyName(),county.getCountyCode(),county.getCityId()});
		}
		
	}
	
	/**
	 * 从数据库读取某城市下所有的县信息
	 */
	public List<County> loadCounties(int cityId)
	{
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.rawQuery(
				"select * from County where city_id = ? ", 
				new String[]{String.valueOf(cityId)});
		
		while(cursor.moveToNext())
		{
			County county = new County();
			county.setId(cursor.getInt(cursor.getColumnIndex("id")));
			county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
			county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
			county.setCityId(cityId);
			
			list.add(county);
		}
		
		return list;
	}
	
}
