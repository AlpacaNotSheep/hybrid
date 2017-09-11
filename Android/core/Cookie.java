package com.demo.app.hybrid.core;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.demo.app.mvp.app.BaseApplication;
import com.demo.app.tinker.util.SampleApplicationContext;
import com.demo.app.util.IKeySourceUtil;
import com.demo.app.util.LogUtil;


public class Cookie {

	protected ExceptionJson exceptionJson = new ExceptionJson();
	public String memberIDKey = "userid";
	private Context context = null;
	protected BaseApplication application;
//	private MyAoyouControllerImp myAoyouControllerImp;

	public Cookie(Context context) {
		this.context = context;

		if (context instanceof Activity) {
			this.application = SampleApplicationContext.application;
//			 initMemberControl();
		} else if (context instanceof Service) {
			application = SampleApplicationContext.application;
		} else {
//			application = (BaseApplication) BaseApplication.getMContext();
		}


	}

	private SharedPreferences getApplication() {
		return context.getSharedPreferences("data", Context.MODE_PRIVATE);
	}

	@JavascriptInterface
	public String getUserId() {
		//取出userId，返回给js
		SharedPreferences settings = context.getSharedPreferences(
				IKeySourceUtil.LOGIN_STATUS,
				Context.MODE_PRIVATE);

		String userid=settings.getString("demoId", "0");
		LogUtil.i("hww:demoIdNew",userid+"");
		return String.valueOf(userid);
	}


	/**
	 * 功能：设置dataKey的value，将dataKey属性的json串保存到sp中
	 * @param dataKey 某些key值
	 * @param value key对应的value值
	 * @param timeout 超时时间
	 * @return
	 */
	@JavascriptInterface
	public String setCookie(String dataKey, String value, int timeout) {
		LogUtil.i("hww", "setCookie  " + "dataKey:" + dataKey + "   value:" + value);
		try {
//			if (dataKey.toLowerCase().equals(memberIDKey.toLowerCase())) {
//				return exceptionJson.CustomExceptionJson("cookie key不能使用" + memberIDKey.toLowerCase());
//			}
			SharedPreferences sharedPreferences = getApplication();
			SharedPreferences.Editor editor = sharedPreferences.edit();

			CookieValue cv = new CookieValue();
			cv.dataKey = dataKey;
			cv.value = value;//版本文件json串
			cv.timeout = timeout;//超时时间
			cv.date = new Date().getTime();//日期

			editor.putString(dataKey, cv.ConvertJson());//将dataKey和它的value以及超时时间等等保存到sp中 保存String类型json串
			editor.commit();

			return exceptionJson.NoExceptionJson();
		} catch (Exception e) {
			return exceptionJson.SystemExceptionJson(e.getMessage());
		}
	}

	public String setMemberID(String dataKey, String value, int timeout) {

		try {

			SharedPreferences sharedPreferences = getApplication();
			SharedPreferences.Editor editor = sharedPreferences.edit();

			/*
			 * CookieValue cv = new CookieValue(); cv.dataKey = dataKey;
			 * cv.value = value; cv.timeout = timeout; cv.date = new
			 * Date().getTime();
			 */

			editor.putString(dataKey, value);
			editor.commit();

			return exceptionJson.NoExceptionJson();
		} catch (Exception e) {
			return exceptionJson.SystemExceptionJson(e.getMessage());
		}
	}

	/**
	 * js调用getCookie方法获取某个dataKey的value值
	 * @param dataKey 下载版本文件的url
	 * @return sp中保存的cookievalue 实体中的版本文件json字符串 获取dataKey对应的value值
	 */
	@JavascriptInterface
	public String getCookie(String dataKey) {
			SharedPreferences sharedPreferences = getApplication();
			String jsonStr = sharedPreferences.getString(dataKey, "");//从sp中取出dataKey对应的value值 注意是String类型json串

			if (!TextUtils.isEmpty(jsonStr)) {
				CookieValue cookieValue = new CookieValue();
				cookieValue.ConvertObj(jsonStr);//转化为Cookievalue实体

				long longDate = cookieValue.date;
				Date date = new Date(longDate);

				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(date);
				cal.add(Calendar.SECOND, cookieValue.timeout);

				if (cookieValue.timeout > 0) {
					Date now = new Date();
					if (cal.getTime().getTime() < now.getTime()) {//如果cal的时间（也就是sp中保存的时间）<当前的时间,移除Cookie
						removeCookie(dataKey);
						return "";
					}
				}
				return cookieValue.value;//value就是版本文件的内容 json串

			}else{
				return "";
			}
	}

	/**
	 * 从sp中删除这个dataKey
	 * @param dataKey
	 * @return
	 */
	@JavascriptInterface
	public String removeCookie(String dataKey) {
		try {
			SharedPreferences sharedPreferences = getApplication();
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(dataKey);
			editor.commit();

			return exceptionJson.NoExceptionJson();
		} catch (Exception e) {
			return exceptionJson.SystemExceptionJson(e.getMessage());
		}
	}


	public class CookieValue {
		public String dataKey;
		public String value;
		public int timeout;
		public long date;

		//拼接json
		public String ConvertJson() {
			JSONObject json = new JSONObject();
			try {
				json.put("dataKey", dataKey);
				json.put("value", value);
				json.put("timeout", timeout);
				json.put("date", date);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return json.toString();
		}

		//解析json
		public void ConvertObj(String jsonStr) {
			try {
				JSONObject result = new JSONObject(jsonStr);
				dataKey = result.getString("dataKey");
				value = result.getString("value");
				timeout = result.getInt("timeout");
				date = result.getLong("date");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
