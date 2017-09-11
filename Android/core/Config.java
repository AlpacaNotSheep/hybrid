package com.demo.app.hybrid.core;


import com.demo.app.BuildConfig;
import com.demo.app.util.IKeySourceUtil;

import java.io.File;

public class Config {
    public final static String SERVICEHOST = IKeySourceUtil.HOST_URL_STR;
    public final static String DEVICETYPE = "devicetype=android";//js区分设备标记
    public final static String DEVICESOURCE = "source=android";//js区分设备标记
    public final static String API_CALLBACK = "api.callback";//js框架回调方法
    public final static String API_SUBPAGE_CALLBACK = "api.routerCallback";//子页面中js框架回调方法

    public final static String ACTION_STR = "action"; //调用android方法的别名
    public final static String COOKIE_STR = "cookie"; //操作android cookie的别名

    public final static String[] WHITELIMIT = new String[]{"://demo.com/", "://democdn.com/"};

    public final static String VERSION_KEY = "version";
    public final static String WAP_HOST = "http://msitelogic.demo.com/api/HybridVersion/GetVersionFile";
    public final static String WAP_HOST_DEBUG = "https://r03.democdn.com/content/hybrid/version";
    public final static String VERSION_FILE = "version-ssl.txt";
    public final static String VERSION_FILE_WEB = "version-ssl.txt";
//    public final static String DIRECTORY = "demo/www";
    public final static String DIRECTORY = "demo";

    public final static int LOGIN_CANCEL = -3;//登陆失败
    public final static String FROM_HYBRID = "fromHybrid";
    //true：每次打开应用版本文件都会重新下载一次，测试的时候用；false：正常逻辑
    public final static boolean isDebug = false;

    public static String OPEN_PAGE="open.page";//如果是打开普通的native页面
    public static String OPEN_SEARCHLIST="open.search";//打开搜索列表页面

    public static String FORMAT_USER_AGENT="Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";

    public final static  String TABLE_NAME = "hybrid_cache";//hybrid模块数据库缓存的表名

   

    /*
    * 不同环境下资源数据在cookie中的key值
    *
    * @result  WAP_HOST正式环境    WAP_HOST_DEBUG测试环境
    * */
    public final static String getVersionCooikeKey(){

        if (BuildConfig.versionUpdateEnvironment==1||BuildConfig.versionUpdateEnvironment==0){
            return WAP_HOST + File.separator + Config.VERSION_FILE_WEB;
        }else {
            return WAP_HOST_DEBUG + File.separator + Config.VERSION_FILE_WEB;
        }
    }
    /*
      * 不同环境下资源数据请求的
      *
      * @result  WAP_HOST正式环境    WAP_HOST_DEBUG测试环境
      * */
    public final static String getVersionLoadUrl(){
        if (BuildConfig.versionUpdateEnvironment==1||BuildConfig.versionUpdateEnvironment==0){
            return WAP_HOST;
        }else {
            return WAP_HOST_DEBUG;
        }
    }

    /**
     * //注意此处参数是写死的，只是为了服务器端使用
     */
    public final static String BOOKING_CITY_ID="bookingcityid=0";

}
