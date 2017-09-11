
package com.demo.app.hybrid.logic;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.mobile.core.http.event.IDataEvent;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.demo.app.R;
import com.demo.app.activity.OrderDetailNewActivity;
import com.demo.app.activity.webOrPay.SubOrderPayActivity;
import com.demo.app.apshare.ShareEntryActivity;
import com.demo.app.data.load.AddCallRecordLoader;
import com.demo.app.domain.CommonReceiveDTO;
import com.demo.app.domain.CommonRequestField;
import com.demo.app.domain.receive.CommonReceiver;
import com.demo.app.domain.receive.DetailTypeReceive;
import com.demo.app.http.Plugin;
import com.demo.app.hybrid.CommonExecuteUtil;
import com.demo.app.hybrid.core.Config;
import com.demo.app.hybrid.core.Cookie;
import com.demo.app.mvp.greendao.GreenDaoManager;
import com.demo.app.mvp.greendao.gen.HybridCacheDao;
import com.demo.app.mvp.model.greendaobean.HybridCache;
import com.demo.app.mvp.model.network.https2HttpUtils;
import com.demo.app.mvp.module.hybrid.activity.BaseWebActivity;
import com.demo.app.mvp.module.hybrid.activity.ProductDetail548Activity;
import com.demo.app.mvp.module.login.LoginActivity601;
import com.demo.app.mvp.module.order.activity.OrderListNewActivity;
import com.demo.app.mvp.module.product.activity.ProductDetailUi540;
import com.demo.app.util.CommReqFieldValuePackag;
import com.demo.app.util.Const;
import com.demo.app.util.CookieUtil;
import com.demo.app.util.DESUtil;
import com.demo.app.util.DialogUtil;
import com.demo.app.util.DownloadUtils;
import com.demo.app.util.FileUtils;
import com.demo.app.util.IKeySourceUtil;
import com.demo.app.util.LogUtil;
import com.demo.app.util.NetworkManageUtil;
import com.demo.app.util.SharedPreferencesUtils;
import com.demo.app.util.UserInfoCheckUtil;
import com.demo.app.util.demoProUtil;
import com.demo.app.util.VoidRepeatClickUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;



public class IndexWebActivity extends BaseWebActivity {
    private int what;
    private int isUseCache;
    private String param;
    private String url;

    private HybridCacheDao hybridCacheDao;
    private String phoneNumber;
    private static final String TABLE_NAME = "hybrid_cache";
    private static String HOST = "https://mdingzhi.demo.com/";
    private String loginDataJson;
    private Dialog dialog;
    private String subjectType;
    private Context context;
    private String topicId;
    private int operateCode;//区分收藏操作码 1：添加收藏 2：取消收藏
    private static final int COLLECT_RESULT = 1;//收藏按钮操作成功·
    private static final int COLLECT_ERROR = 2;//收藏按钮操作失败
    private String currentGAPath;
    private String classID;
    private boolean netAvailable = true;
    /**
     * 标示是否从单项产品支付跳转过来
     */
    private boolean isSingleProductOrder=false;

    @Override
    public int getResLayoutId() {
        return -1;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState,
                getIntent().getStringExtra(IKeySourceUtil.GA_FROM_FLAG), null);
        currentGAPath = gaPtahString;

        initData();
        loadPageFormat(url);
    }

    private void initData() {
        context=this;
//        hybridCacheDao = new HybridCacheDao(IndexWebActivity.this);
        hybridCacheDao= GreenDaoManager.getInstance().getDaoSession().getHybridCacheDao();
        dialog = DialogUtil.buildDialogRecover(IndexWebActivity.this);
        preferencesUtils = new SharedPreferencesUtils(context,"productdetail");

        //初始化localStorage
        locationProvince = getSharedPreferences("Location", Context.MODE_PRIVATE).getString("LocationProvince",IKeySourceUtil.DEFAULT_CITY);
        locationCity = new SharedPreferencesUtils(IndexWebActivity.this, "StartCity").getString("name", IKeySourceUtil.CITY);
        startCity = new SharedPreferencesUtils(IndexWebActivity.this, "StartCity").getString("name", IKeySourceUtil.CITY);
        appVersion = CommReqFieldValuePackag.getCommReqField(IndexWebActivity.this).getPhoneVersion();
        userid = getSharedPreferences(IKeySourceUtil.LOGIN_STATUS, Context.MODE_PRIVATE).getString("demoId", "0");

        topicId =String.valueOf(getIntent().getIntExtra("topicId", 0));
        url = getIntent().getStringExtra("url");
        //需要对url进行处理，如果是不完整路径的链接，需要处理为完整路径的链接,这样才能找到sd卡中本地文件
        if (url.equals("https://mdingzhi.demo.com/")){
            url="https://mdingzhi.demo.com/hybrid/home/index.html";
        }else if (url.equals("http://mdingzhi.demo.com/")){
            url="http://mdingzhi.demo.com/hybrid/home/index.html";
        }
        subjectType = getIntent().getStringExtra("subjectType");
        String subjectName=getIntent().getStringExtra("subjectName");
        boolean isShowNav=getIntent().getBooleanExtra("isNav", false);

        //判断是否可以分享
        int isShare = -1;
        isShare = getIntent().getIntExtra("isShare", 0);
        if (isShare == 1) {// 如果传过来的值为1 可以分享
            rightShareButton.setVisibility(View.VISIBLE);
        } else {
            rightShareButton.setVisibility(View.GONE);
        }

        isSingleProductOrder=getIntent().getBooleanExtra("isSingleProductOrder",false);

        LogUtil.i("hww", "hww:url=" + url);
//        LogUtil.i("hww", "hww:subjectType=" + subjectType);
        if ("3".equals(subjectType)||isShowNav) {//如果是3，表示需要展示顶部导航条，处理返回键
            setTopbarVisibility(View.VISIBLE);
        }else{
            setTopbarVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(subjectName)){
            middleTitle.setText(subjectName);
        }

        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demoProUtil.firstMlinkGotoHome(context);
                finish();
            }
        });
        //分享按钮
        rightShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VoidRepeatClickUtil.isFastDoubleClick()) {
                    try {
                        String imgUrl = "";
                        String shareContent = "";
                        String topicsName = "";
                        if (getIntent() != null) {
                            Intent intent1 = getIntent();
                            imgUrl = intent1.getStringExtra("shareImage");//需要分享图片的地址
                            if (TextUtils.isEmpty(imgUrl)) {
                                imgUrl = Const.SHARE_DEFAULT_IMAGE_URL;
                            }
                            shareContent = intent1.getStringExtra("shareContent");
                            topicsName = intent1.getStringExtra("TopicsName");
                            //mLink参数接收
                            String mLinkTopicsName = intent1.getStringExtra("topicsName");
                            if (TextUtils.isEmpty(topicsName)){
                                topicsName = middleTitle.getText().toString();
                            }
                            if (!TextUtils.isEmpty(mLinkTopicsName)) {
                                topicsName = mLinkTopicsName;
                            }
                            String mLinkTopicsImgUrl = intent1.getStringExtra("topicsImgUrl");
                            if (!TextUtils.isEmpty(mLinkTopicsImgUrl)) {
                                imgUrl = mLinkTopicsImgUrl;
                            }
                            if (!TextUtils.isEmpty(intent1.getStringExtra("shareUrl"))) {
                                url = intent1.getStringExtra("shareUrl");
                            }
                            if (TextUtils.isEmpty(shareContent) || shareContent == null){
                                shareContent = "";
                                if (url.contains("?")) {
                                    HashMap<String, String> map = CookieUtil
                                            .ParseTokenString(url);
                                    if (map != null && map.get("description") != null) {
                                        shareContent = map.get("description");
                                    }
                                }
                            }
                            //设置分享的数据
                            CookieUtil.setShareData(IndexWebActivity.this, 2, url,
                                    imgUrl, topicsName,
                                    url, shareContent + " "
                                            + url + " 快来看看");
                            Intent intent = new Intent();//跳转到分享的界面
                            intent.setClass(IndexWebActivity.this,
                                    ShareEntryActivity.class);
                            intent.putExtra("activityType", 2);
                            intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, "分享界面");
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    
    //对传递过来的url进行判断，判断是加载网络还是加载本地文件
    private void loadPageFormat(String url){
        if (url.contains("http")||url.contains("https")){
            judgeHttpUrl(url);
        }else if (url.contains("file:")){
            judgeFileUrl(url);
        }
    }


/**
     * 如果传递过来的url是http格式的，进行判断
     * @param url
     */

    private void judgeHttpUrl(String url) {
        String noParamsHttpUrl = "";//不带参数的http格式的url
        String params="";//参数
        if (url.contains("?")){
            noParamsHttpUrl=url.split("\\?")[0];
            params="?"+url.split("\\?")[1];
        }else{
            noParamsHttpUrl=url;
        }
        noParamsHttpUrl=noParamsHttpUrl.toLowerCase();
        if (noParamsHttpUrl.contains("/hybrid")){
            noParamsHttpUrl=noParamsHttpUrl.replace("/hybrid","");
        }

        Cookie cookie=new Cookie(context);
        //得到sp中保存的cookievalue实体中的版本文件内容，参数其实就是version.txt文件的下载地址
        String versionTxt = cookie.getCookie(Config.getVersionCooikeKey());
        if (versionTxt.equals("")){
            refreshPage(url);
            //如果版本文件中为空那么下载版本文件
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
        }else{
            try {
                boolean haveLoadPage=false;//是否加载了网页
                JSONObject json = new JSONObject(versionTxt);
                json = json.getJSONObject("update");

                //从版本文件的“html”中循环取出每个html地址，和传过来的url进行判断，如果相等表示版本文件中存在此url，那么从本地找此url路径是否存在，如果存在则打开本地文件
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    if (!str.equals("html")){//如果不是html就退出while循环
                        continue;
                    }
                    JSONArray array = json.getJSONArray(str);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String p = obj.getString("file");//得到html文件的下载地址

                        String noHybridPath;
                        if (p.toLowerCase().contains("/hybrid")){
                            noHybridPath=p.toLowerCase().replace("/hybrid","");
                        }else{
                            noHybridPath=p;
                        }
                        noHybridPath=noHybridPath.toLowerCase();

                        if (noHybridPath.equals(noParamsHttpUrl)){//都去掉hybrid后，判断是否相等，即判断此url是否在下载列表中
                            String local_path=null;
                            if (p.contains(https2HttpUtils.HTTPS)){
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("https://", "");
                            }else {
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "");
                            }
                            if (FileUtils.isFileExists(local_path)){//加载本地文件
                                LogUtil.i("hww", "本地");
                                haveLoadPage=true;
                                refreshPage("file://" + local_path+params);
                                break;
                            }
                            else{//加载网络文件
                                LogUtil.i("hww", "网络");
                                haveLoadPage=true;
                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                refreshPage(url);
                                init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
                            }
                        }
                    }
                }

                //循环结束如果没有加载页面，那么直接加载网络
                if (!haveLoadPage){
                    refreshPage(url);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                refreshPage(url);
            }
        }

    }


/**
     * 如果传递过来的url是file格式的，进行判断
     * @param url
     */

    private void judgeFileUrl(String url) {
        String filePath = url.substring(url.indexOf("demo"), url.lastIndexOf("html") + 4);
        filePath = Environment.getExternalStorageDirectory() + File.separator + filePath;
        Log.i("hww", "file:filepath" + filePath);
        if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
            refreshPage(url);
        } else {
            refreshPage("https://" + filePath.split("demo/")[1]);
        }
    }


    Handler dataHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //将网络获取到的返回值传给js，给js来处理
                case COLLECT_RESULT:
                    webView.loadUrl("javascript:window.addSubjectToFavoriteResponse('" + commonReceiver.getErrorCode() + "','" + operateCode + "','" + commonReceiver.getJsonResult() + "')");
                    break;

                case COLLECT_ERROR:
                    webView.loadUrl("javascript:window.addSubjectToFavoriteResponse('-200','" + operateCode + "','null')");

                default:
                    break;
            }
        }
    };


    @Override
    protected void dataProvider(int what, String url, String param, int isUseCache) {
        super.dataProvider(what, url, param, isUseCache);

        // 判断有无网络，有网，发送请求；否则，从数据库读取
        if (NetworkManageUtil.isWiFiActive(this) || NetworkManageUtil.isNetworkAvailable(this)) {
            this.what = what;
            this.isUseCache = isUseCache;
            this.param = param;
            this.url = url;

//            DBHelper dbHelper = new DBHelper(IndexWebActivity.this, Configuration.DatabaseFile, null, Configuration.DatabaseVersion);
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            boolean tableIsExist = dbHelper.tabIsExist(TABLE_NAME, db);

//            //判断本地有无缓存
//            HybridCache hybridCache = hybridCacheDao.queryObject(HybridCache.class, TABLE_NAME, new String[]{"_id", "isUseCache", "currentTime", "data"}, "param=?", new String[]{param});
//            hybridCacheDao.close();
            HybridCache hybridCache=hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
            if (hybridCache != null && !TextUtils.isEmpty(hybridCache.getData())) {
                Long savedTime = hybridCache.getCurrentTime();
                int savedIsUseCache = hybridCache.getIsUseCache();
                Long currentTime = System.currentTimeMillis();
                if ((currentTime - savedTime) > savedIsUseCache * 60 * 1000) {
                    postWebData();
                } else {
                    callCallback(String.valueOf(what), hybridCache.getData());
                }
            } else {
                postWebData();
            }
        } else {//无网络判断本地有无缓存数据
//            HybridCache hybridCache = hybridCacheDao.queryObject(HybridCache.class, TABLE_NAME, new String[]{"_id", "isUseCache", "currentTime", "data"}, "param=?", new String[]{param});
//            hybridCacheDao.close();
            HybridCache hybridCache=hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
            if (hybridCache != null && !TextUtils.isEmpty(hybridCache.getData())) {
                callCallback(String.valueOf(what), hybridCache.getData());
            } else {
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("ErrorCode", -3);
                    jsonData.put("ErrorMsg", getResources().getString(R.string.check_net));
                    jsonData.put("JsonResult", null);
                    callCallback(String.valueOf(what), jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void postWebData() {
        //解析param
//        LogUtil.i("hww", "hww:param:" + param.toString());
        CommonRequestField commonRequestField = CommReqFieldValuePackag.getCommReqField(context);

        JSONObject jsonObject = new JSONObject();
        String jsonStr = null;
        try {
            JSONObject jsonObject1 = new JSONObject(param);
            jsonObject.put("clientSource", commonRequestField.getClientSource());
            jsonObject.put("phoneID", commonRequestField.getPhoneID());
            jsonObject.put("phoneType", commonRequestField.getPhoneType());
            jsonObject.put("phoneVersion", commonRequestField.getPhoneVersion());
            jsonObject.put("startCity", commonRequestField.getStartCity());
            jsonObject.put("Path", jsonObject1.getString("Path"));
            jsonObject.put("ActionName", jsonObject1.getString("ActionName"));
            jsonObject.put("ControllerName", jsonObject1.getString("ControllerName"));
            jsonObject.put("PostData", jsonObject1.getString("PostData"));

            jsonStr = jsonObject.toString();
            LogUtil.i("hww", "hww:"+jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Plugin.getDongtaiHttp(context, url, true).postWebData(
                    postData,
                    DESUtil.des3EncodeCBC(jsonStr.getBytes("UTF-8"), IKeySourceUtil.PASSWORD_CRYPT_KEY));
        } catch (Exception e) {
            LogUtil.e("hww", e.getMessage().toString());
            callBackErrorData(-3, getResources().getString(R.string.check_net));
        }
    }


    private IDataEvent<String> postData = new IDataEvent<String>() {

        @Override
        public void onProcessFinish(int arg0, String result) {
//            LogUtil.i("hww", "responseData" + result);
            if (TextUtils.isEmpty(result)) {
                callBackErrorData(-3, "请求服务器失败，请重试");
                return;
            }
            try {
                CommonReceiveDTO commonReceiveDTO = JSON.parseObject(result, CommonReceiveDTO.class);
                if (commonReceiveDTO != null) {
                    if (commonReceiveDTO.getMC() == 1000 && commonReceiveDTO.getContent().length() > 0) {
                        String data = DESUtil.des3DecodeCBC(commonReceiveDTO.getContent());
                        LogUtil.i("hww", "hww:RECEIVE JSONSting =>>" + data);
                        if (TextUtils.isEmpty(data)) {
                            callBackErrorData(-4, "请求服务器失败，请重试");
                        }
                        //解析data数据并且存储
                        JSONObject jsonObject = new JSONObject(data);
                        int errorCode = jsonObject.getInt("ErrorCode");
                        if (errorCode == 200) {
                            callCallback(String.valueOf(what), data);
                        }else if (!TextUtils.isEmpty(jsonObject.getString("JsonResult"))){
                            callCallback(String.valueOf(what), data);
                        } else {
                            callBackErrorData(errorCode, jsonObject.getString("ErrorMsg"));
                        }
                        //保存缓存数据
                        if (isUseCache > 0 && errorCode == 200) {
//                            hybridCacheDao.delete(TABLE_NAME, "param=?", new String[]{param});
                            HybridCache hybrid=hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
                            //需要判空,没有查询到hybrid发返回null。否则会抛出异常
                            if (null!=hybrid){
                                hybridCacheDao.deleteByKey(hybrid.getId());
                            }

                            HybridCache hybridCache = new HybridCache();
                            hybridCache.setCurrentTime(System.currentTimeMillis());
                            hybridCache.setIsUseCache(isUseCache);
                            hybridCache.setParam(param);
                            hybridCache.setData(data);
                            hybridCacheDao.insert(hybridCache);
//                            hybridCacheDao.insertData(hybridCache);
                        }
                    } else {
                        callBackErrorData(-3, getResources().getString(R.string.check_net));
                    }
                } else {
                    callBackErrorData(-3, getResources().getString(R.string.check_net));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                callBackErrorData(-3, getResources().getString(R.string.check_net));
            }
        }
    };


/**
     * 返给js错误数据
     * @param errorCode
     * @param errorMsg
     */

    private void callBackErrorData(int errorCode, String errorMsg) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("ErrorCode", errorCode);
            jsonData.put("ErrorMsg", errorMsg);
            jsonData.put("JsonResult", null);
            callCallback(String.valueOf(what), jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected String execute(String key, String jvalue) {
        LogUtil.i("hww", "hww::execute:key:" + key + ",jvalue" + jvalue);
        String keyLowerCase = key.toLowerCase();

        if (keyLowerCase.contains("login")) {//登陆
            loginDataJson = jvalue;
            Intent intent = new Intent(IndexWebActivity.this, LoginActivity601.class);
//            Intent intent = new Intent(IndexWebActivity.this, LoginThirdActivity540.class);
            intent.putExtra("fromHybrid", "fromHybrid");
            intent.putExtra("PageName",currentGAPath);
            startActivityForResult(intent, 1);

        }  else if (keyLowerCase.contains("opendetail")) {//打开定价产品详情页
            try {
                JSONObject detailData = new JSONObject(jvalue);
                String classId = detailData.getString("ClassId");
                String productId = detailData.getString("ProductId");
                productID = Long.parseLong(productId);
                classID = classId;
                if(isIntent) {
                    isIntent = false;
                    dialog = DialogUtil.buildDialogRecover(IndexWebActivity.this);
                    Intent intent = null;
                    if (preferencesUtils.contains(String.valueOf(productID))){
                        cancelDialog();
                        if (preferencesUtils.getString(String.valueOf(productID),"").equals("10")){
                            intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
                            intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
                            startActivity(intent);
                        }else if (preferencesUtils.getString(String.valueOf(productID),"").equals("30")){
                            intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
                            intent.putExtra("url", IKeySourceUtil.WEB_SINGLE_PRODUCT_DETAIL + productID);
                            startActivity(intent);
                        }
                        else {
                            intent = new Intent(IndexWebActivity.this, ProductDetailUi540.class);
                            intent.putExtra("demoTravelClass", classID);
                            intent.putExtra("ProductID",productID);
                            startActivity(intent);
                        }
                        isIntent = true;
                        return "";
                    }
                    detailTypeUtil.getDetailType(0, (int) productID, 0, detailType);
                }
//                Intent intent = new Intent(IndexWebActivity.this, ProductDetailUi540.class);
//                intent.putExtra("demoTravelClass", classId);
//                intent.putExtra("ProductID", Long.parseLong(productId));
//                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }  else if (keyLowerCase.contains("action.collect")){//进行收藏 用户未登录状态下可以截取到收藏字段
            if (!VoidRepeatClickUtil.isFastDoubleClick()){
                operateCode =1;
                // 判断是否登录
                mBaseApplicate.ifRutrnMydemoPage = false;
                if (!UserInfoCheckUtil.checkLogin(IndexWebActivity.this, null, 0, null, "test" + "->登录页")) {
                    return "";
                }
                dealWithCollectButton(operateCode,String.valueOf(userid), topicId,null);
            }
        }
        else if (keyLowerCase.contains("action.cancelfavorite")){//取消收藏,用户只有登录状态才会截取到取消收藏字段
            if (!VoidRepeatClickUtil.isFastDoubleClick()){
                operateCode =2;
                // 判断是否登录
                mBaseApplicate.ifRutrnMydemoPage = false;
                if (!UserInfoCheckUtil.checkLogin(IndexWebActivity.this, null, 0, null, "test" + "->登录页")) {
                    return "";
                }

                try {
                    JSONObject cancelObject=new JSONObject(jvalue);
                    String favoriteid=cancelObject.getString("favoriteid");
                    dealWithCollectButton(operateCode,String.valueOf(userid), topicId,favoriteid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (keyLowerCase.contains("action.getfile")){
            //判断是否有网络
            if (NetworkManageUtil.isWiFiActive(this) || NetworkManageUtil.isNetworkAvailable(this)) {
                netAvailable = true;
                loadSubPage(netAvailable, jvalue);
            } else {
                //取本地文件，如果娶不到，赋值空
                netAvailable = false;
                loadSubPage(netAvailable, jvalue);
            }
        }
        else if (keyLowerCase.equals("go.payback")){//支付页面点击后退
            payback(jvalue);
        }else{
            CommonExecuteUtil.commonExecute((Activity) context,key,jvalue,currentGAPath);
        }
        /**
         * @author jiangyingjun
         *@time2017-02-16
         * @description交行快捷支付
         *
         **/
//        else if (keyLowerCase.contains("go.bcmpayback")) {
//            try {
//                JSONObject jsonObject=new JSONObject(jvalue);
//                int payReturnCode =jsonObject.optInt("payReturnCode");
//                String payReturnMessage=jsonObject.optString("payReturnMessage");
//
//                DialogUtil.toastForShort(context, payReturnMessage);
//
//
//                if (payReturnCode==200){
//                    Intent intent = new Intent(context, OrderListNewActivity.class);
//                    /* 刷新列表*/
//                    OrderListNewActivity.orderListRefresh = true;
//                    intent.putExtra("orderListType", 0);
//                    intent.putExtra("TravelType", 100);
//                    startActivity(intent);
//                    finish();
//                }
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
       /*
        * @author jiangyingjun
        * @time 2017-02-21
        * @description 添加loading
        * */
//        else if (keyLowerCase.contains("action.excloding")){
//
//           callJs("api.loading");
//        }
        return "";
    }


    /**
     * 支付页面后退按钮
     * @param jvalue
     */
    private void payback(String jvalue) {
        String orderID = null;
        String orderType = null;
        boolean isSonOrder=false;
        try {
            JSONObject jsonObject = new JSONObject(jvalue);
            if (jvalue.contains("orderID")){
                orderID=jsonObject.getString("orderID");
            }
            if (jvalue.contains("orderType")){
                orderType=jsonObject.getString("orderType");
            }
            if (jvalue.contains("isSonOrder")){
                isSonOrder=jsonObject.getBoolean("isSonOrder");
            }

            if (TextUtils.isEmpty(orderID)||orderID.equals("0")){//如果为空的话默认跳转到订单列表
                Intent intent=new Intent(this,OrderListNewActivity.class);
                startActivity(intent);
            } else if (isSonOrder){
                Intent intent=new Intent(this,SubOrderPayActivity.class);//不需要刷新
                intent.putExtra("orderID",orderID);
                intent.putExtra("orderType",orderType);
                startActivity(intent);
            }else{
                Intent intent=new Intent(this,OrderDetailNewActivity.class);
                intent.putExtra("orderID",orderID);
                intent.putExtra("orderType",orderType);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected boolean shouldOverrideUrlLoadingOverride(WebView view, String url) {
        String urlOrigin=url;
        String urlLowCase = null;
        //参数区分大小写
        if (url.contains("?")) {
            urlLowCase = url.substring(0, url.split("\\?")[0].length());
            urlLowCase = urlLowCase.toLowerCase();
            urlLowCase = urlLowCase + "?" + url.split("\\?")[1];
        } else {
            urlLowCase = url.toLowerCase();
        }
        LogUtil.i("hww", "hww:overrideUrl" + urlLowCase);
        //处理首页back
        String urlLowCaseBack = urlLowCase.split("\\?")[0];//用来处理私人定制页返回首页事件

        if (urlLowCaseBack.equals("http://m.demo.com") || urlLowCaseBack.equals("http://m.demo.com/")||urlLowCaseBack.equals("https://m.demo.com")||urlLowCaseBack.equals("https://m.demo.com/")) {
            demoProUtil.firstMlinkGotoHome(context);
            finish();

        }else if (urlLowCase.contains("discovery")&&urlLowCase.contains("index.html")){//如果在详情页，处理返回键，返回上一页
            finish();

        } else if (urlLowCase.contains("tel:")) {
            if (urlLowCase.split("\\?").length > 1) {//tel:01010109898?devicetype=android
                urlLowCase = urlLowCase.substring(0, urlLowCase.indexOf("?"));
            }
            phoneNumber = urlLowCase.substring("tel:".length(), urlLowCase.length());
            //统一显示从后台获取的客服电话
            if(phoneNumber.equals("")||phoneNumber==null){
                        phoneNumber= Const.kefuPhone_tel;
            }
            DialogUtil.showBuilders(null, IndexWebActivity.this,
                    getString(R.string.prompt),
                    "客服电话：" + phoneNumber,
                    getString(R.string.call), getString(R.string.cancel),
                    callOnClickListener);

        }
        else if(urlLowCase.contains("AppDetail")){//Url包含AppDetail  跳转原生详情页
//            m.demo.com/waptour-130026.html?/AppDetail/130026/1&devicetype=android&hybridversion=1&source=android&hybridversion=1&source=android
                String appDetailID =  urlLowCase.split("\\?")[1].split("&")[0].split("/")[2]; //产品ID
//            productID = Long.parseLong(appDetailID);
//            if (isIntent) {
//                isIntent = false;
//                dialog = DialogUtil.buildDialogRecover(IndexWebActivity.this);
//                Intent intent = null;
//                if(preferencesUtils.contains(String.valueOf(productID))){
//                    cancelDialog();
//                    if (preferencesUtils.getString(String.valueOf(productID),"").contains("10")){
//                        intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
//                        intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
//                        startActivity(intent);
//                    }else {
//                        intent = new Intent(context,ProductDetailUi540.class);
//                        intent.putExtra("ProductID", Long.parseLong(appDetailID));
//                        startActivity(intent);
//                    }
//                    isIntent = true;
//                    return true;
//                }
//                detailTypeUtil.getDetailType(0, (int) productID, 0, detailType2);
//            }
            Intent intent = new Intent(context,ProductDetailUi540.class);
            intent.putExtra("ProductID", Long.parseLong(appDetailID));
            startActivity(intent);
        }

        else if (urlLowCase.contains("file:")) {//肯定是完整文件路径，加载本地文件即可
            judgeFileUrl(urlLowCase);
        } else if (urlLowCase.contains("m.demo.com/product/detail.html")||urlLowCase.contains(IKeySourceUtil.WEB_SINGLE_PRODUCT_DETAIL_STR)) {
            Intent intent = new Intent();
            intent.setClass(context, ProductDetail548Activity.class);
            intent.putExtra("url",urlLowCase);
            context.startActivity(intent);
        }

        else if (urlLowCase.contains("http:")||urlLowCase.contains("https:")) {//判断网络路径
            String newUrl = urlLowCase;

            //定制模块专属：拼接url
            if (urlLowCase.equals("http://mdingzhi.demo.com/?devicetype=android")||urlLowCase.equals("https://mdingzhi.demo.com/?devicetype=android")) {
                newUrl = HOST + "hybrid/home/index.html?devicetype=android";
            }
            else if (urlLowCase.contains(HOST)) {
                newUrl = urlLowCase.split("\\?")[1];
                if (!newUrl.contains("hybrid")) {
                    newUrl = HOST + "hybrid/" + newUrl.split(HOST)[1];
                } else if (!urlLowCase.contains(".html")) {
                    newUrl = newUrl + "index.html";
                }
                newUrl = newUrl + "?" + urlLowCase.split("\\?")[1];
            }

            Log.i("hww", "newUrl" + newUrl);
            //判断本地有没有文件
            if (newUrl.contains("mdingzhi")){
                String filePath = newUrl.substring(newUrl.indexOf("mdingzhi"), newUrl.indexOf("?"));
                filePath = Environment.getExternalStorageDirectory() + File.separator + "demo/" + filePath;
                Log.i("hww", "http:filePath" + filePath);
                if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                    refreshPage("file://" + filePath + "?" + newUrl.split("\\?")[1]);
                    return true;
                }
            }

          judgeHttpUrl(urlLowCase);
        }else{
            refreshPage(urlOrigin);
        }
        return true;
    }
    /**
     * 加载子页面
     *
     * @param jvalue
     */
    public void loadSubPage(boolean netAvailable, String jvalue) {
        String url = null;//
        int onLine = 1;
        String viewName = null;
        String data;
        //解析返回的jvalue数据
        try {
            JSONObject jsonObject = new JSONObject(jvalue);
            url = jsonObject.getString("url");
            onLine = jsonObject.getInt("online");
            viewName = jsonObject.getString("viewName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //有网
        if (netAvailable) {
            if (url.contains("file")){
                //如果传过来的是file文件,那么判断本地有没有那个文件, 如果有,那么直接读取本地,如果没有,那么读取线上
                String filePath = url;
                Log.i("hww", "file:filepath" + filePath);
                if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                    try {//读取本地的子页面
                        byte[] temp = FileUtils.getBytes(filePath);
                        String result = new String(temp, "utf-8");
                        try {
                            dealWithReturnData(netAvailable,viewName, result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                            //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                        //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                    }
                    return;
                } else {//读取线上子文页面文件
                    url = "https://" + url.split("demo/")[1];
                    try {
                        byte[] ver = DownloadUtils.downloadSSLAllWithHttpClient(url);
                        if (ver != null) {
                            data = new String(ver, "utf-8");//获取到下载得到的数据
                            dealWithReturnData(netAvailable,viewName, data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                        //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                    }
                }
            }else if(url.contains("http")){
                //读取本地。判断本地有没有子页面，如果有就读取本地并返回，如果没有就读取线上并返回
                String temp = loadHttpSubPageUrl(url);
                try {
                    dealWithReturnData(netAvailable,viewName, temp);
                } catch (Exception e) {
                    e.printStackTrace();
                    DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                    //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                }
            }
        } else {//无网
            String temp = loadHttpSubPageUrl2(url);
            try {
                dealWithReturnData(netAvailable,viewName, temp);
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.toastForShort(this, getResources().getString(R.string.check_net));
                //callSubPageBackErrorData(what,-3, "网络连接失败，请重试");
            }
        }
    }

    /**
     * 加载子页面的时候，如果传递过来的url是http格式的，进行判断。如果本地有此html，取出本地数据，如果本地没有，取出线上数据并且返回
     *
     * @param url
     */
    public String loadHttpSubPageUrl(String url) {
        String result = null;
        String noParamsHttpUrl = "";//不带参数的http格式的url
        String params = "";//参数
        if (url.contains("?")) {
            noParamsHttpUrl = url.split("\\?")[0];
            params = "?" + url.split("\\?")[1];
        } else {
            noParamsHttpUrl = url;
        }
        noParamsHttpUrl = noParamsHttpUrl.toLowerCase();
        if (noParamsHttpUrl.contains("/hybrid")) {
            noParamsHttpUrl = noParamsHttpUrl.replace("/hybrid", "");
        }

        Cookie cookie = new Cookie(context);
        //得到sp中保存的cookievalue实体中的版本文件内容，参数其实就是version.txt文件的下载地址
        String versionTxt = cookie.getCookie(Config.getVersionCooikeKey());
        if (versionTxt.equals("")) {
//            refreshPage(url);
            byte[] tempt;
            try {
                tempt = DownloadUtils.downloadSSLAllWithHttpClient(url);
                result = new String(tempt, "utf-8");//获取到下载得到的数据
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                boolean haveLoadPage = false;//是否加载了网页
                JSONObject json = new JSONObject(versionTxt);
                json = json.getJSONObject("update");

                //从版本文件的“html”中循环取出每个html地址，和传过来的url进行判断，如果相等表示版本文件中存在此url，那么从本地找此url路径是否存在，如果存在则打开本地文件
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    if (!str.equals("html")) {//如果不是html就退出while循环
                        continue;
                    }
                    JSONArray array = json.getJSONArray(str);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String p = obj.getString("file");//得到html文件的下载地址

                        String noHybridPath;
                        if (p.toLowerCase().contains("/hybrid")) {
                            noHybridPath = p.toLowerCase().replace("/hybrid", "");
                        } else {
                            noHybridPath = p;
                        }
                        noHybridPath = noHybridPath.toLowerCase();

                        if (noHybridPath.equals(noParamsHttpUrl)) {//都去掉hybrid后，判断是否相等，即判断此url是否在下载列表中
                           String local_path=null;
                            if (p.contains("https://")){
                                local_path=  Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("https://", "");
                            }else {
                                local_path=  Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "");
                            }
                            if (FileUtils.isFileExists(local_path)) {//加载本地文件
                                LogUtil.i("hww", "本地");
                                haveLoadPage = true;
//                                refreshPage("file://" + Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "") + params);
                                byte[] temp = FileUtils.getBytes(local_path+ params);// /storage/emulated/0/demo/m.demo.com/product/hybrid/
                                result = new String(temp, "utf-8");
                                break;
                            } else {//加载网络文件
                                LogUtil.i("hww", "网络");
                                haveLoadPage = true;
                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
//                                refreshPage(url);
                                byte[] tempt = DownloadUtils.downloadSSLAllWithHttpClient(url);
                                result = new String(tempt, "utf-8");//获取到下载得到的数据
                            }
                        }
                    }
                }

                //循环结束如果没有加载页面，那么直接加载网络数据
                if (!haveLoadPage) {
//                    refreshPage(url);
                    byte[] ver = DownloadUtils.downloadSSLAllWithHttpClient(url);
                    result = new String(ver, "utf-8");//获取到下载得到的数据
                }

            } catch (Exception e) {
                e.printStackTrace();
//                refreshPage(url);
                result = null;

            }
        }

        if (!TextUtils.isEmpty(result)) {
            return result;
        } else {
            return null;
        }

    }

    /**
     * 加载子页面的时候，如果传递过来的url是http格式的，进行判断。如果本地有此html，取出本地数据，如果本地没有，取出线上数据并且返回
     *
     * @param url
     */
    private String loadHttpSubPageUrl2(String url) {
        String result = null;
        String noParamsHttpUrl = "";//不带参数的http格式的url
        String params = "";//参数
        if (url.contains("?")) {
            noParamsHttpUrl = url.split("\\?")[0];
            params = "?" + url.split("\\?")[1];
        } else {
            noParamsHttpUrl = url;
        }
        noParamsHttpUrl = noParamsHttpUrl.toLowerCase();
        if (noParamsHttpUrl.contains("/hybrid")) {
            noParamsHttpUrl = noParamsHttpUrl.replace("/hybrid", "");
        }

        Cookie cookie = new Cookie(context);
        //得到sp中保存的cookievalue实体中的版本文件内容，参数其实就是version.txt文件的下载地址
        String versionTxt = cookie.getCookie(Config.getVersionCooikeKey());
        if (versionTxt.equals("")) {
//            refreshPage(url);
            result = null;
        } else {
            try {
                boolean haveLoadPage = false;//是否加载了网页
                JSONObject json = new JSONObject(versionTxt);
                json = json.getJSONObject("update");

                //从版本文件的“html”中循环取出每个html地址，和传过来的url进行判断，如果相等表示版本文件中存在此url，那么从本地找此url路径是否存在，如果存在则打开本地文件
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    /**
                     * @time 2017-03-07
                     * @author jiangyingjun
                     * @dscription 如果第一个str不是html直接break  会导致无法遍历后面的    所以改成如果equals html 就进入下层逻辑
                     * old version 如果不是html就退出while循环
                     * **/
                    if (str.equals("html")) {
                        JSONArray array = json.getJSONArray(str);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String p = obj.getString("file");//得到html文件的下载地址

                            String noHybridPath;
                            if (p.toLowerCase().contains("/hybrid")) {
                                noHybridPath = p.toLowerCase().replace("/hybrid", "");
                            } else {
                                noHybridPath = p;
                            }
                            noHybridPath = noHybridPath.toLowerCase();

                            if (noHybridPath.equals(noParamsHttpUrl)) {//都去掉hybrid后，判断是否相等，即判断此url是否在下载列表中
                                if (FileUtils.isFileExists(Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", ""))) {//加载本地文件
                                    LogUtil.i("hww", "本地");
                                    haveLoadPage = true;
//                                refreshPage("file://" + Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "") + params);
                                    byte[] temp = FileUtils.getBytes(Environment.getExternalStorageState() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "") + params);
                                    result = new String(temp, "utf-8");
                                    break;
                                } else {//加载网络文件
                                    LogUtil.i("hww", "网络");
                                    haveLoadPage = true;
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
//                                refreshPage(url);
                                    result = null;
                                }
                            }
                        }
                    }

                }

                //循环结束如果没有加载页面，那么直接加载网络
                if (!haveLoadPage) {
//                    refreshPage(url);
                    result = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
//                refreshPage(url);
                result = null;

            }
        }

        if (!TextUtils.isEmpty(result)) {
            return result;
        } else {
            return null;
        }

    }
    /**
     * 处理从线上或者本地返回的数据
     *
     * @param viewName
     * @param data
     * @throws Exception
     */
    private void dealWithReturnData(boolean netAvailable,String viewName, String data) throws Exception {
        if (!TextUtils.isEmpty(data)){
            JSONObject jsonData = new JSONObject();
            jsonData.put("ErrorCode", 200);
            jsonData.put("ErrorMsg", "");
            // 需要对特殊字符进行转义
            data=data.replace("\r\n","");//替换掉换行符
            String formatHtml= Uri.encode(data, "UTF-8").replace("\'", "&acute;").replace("\"","&quot;");//替换单引号和双引号
            jsonData.put("JsonResult", formatHtml);
//            Log.i("hww", "hww:" + data);
//            callSubPageCallback(viewName, jsonData.toString());//将数据结果等回调给js
            callSubPageCallback(viewName, formatHtml);//将数据结果等回调给js
        }else{
            /*
            *
            * @time 2017-03-06
            * @author jiangyingjun
            * @descrition js 报错后  自己负责提示toast
            *
            * */
            callSubPageCallback(viewName, "");
            if (netAvailable){
                DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
            }else {
                DialogUtil.toastForShort(this, getResources().getString(R.string.check_net));
            }

//            if (netAvailable){
//                callSubPageBackErrorData(what,-4,"请求服务器失败，请重试");
//            }else{
//                callSubPageBackErrorData(what,-3,"网络连接失败，请重试");
//            }
        }
    }

    /**
     * 返给js错误数据
     *
     * @param errorCode -4:服务器异常
     * @param errorMsg
     */
    private void callSubPageBackErrorData(int what,int errorCode, String errorMsg) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("ErrorCode", errorCode);
            jsonData.put("ErrorMsg", errorMsg);
            jsonData.put("JsonResult", null);
            callSubPageCallback(String.valueOf(what), jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

/**
     * 拨打电话点击事件
     */

    DialogInterface.OnClickListener callOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            new RxPermissions(IndexWebActivity.this)
                    .request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)//打电话的权限
                    .subscribe(granted -> {
                        if (granted) {
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
                            AddCallRecordLoader addCallRecordLoader = new AddCallRecordLoader();

                            try {
                                addCallRecordLoader.addCallRecord(IndexWebActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            DialogUtil.toastForShort(context, "请同意我们的权限，才能提供服务");
                        }
                    });
//            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
//            AddCallRecordLoader addCallRecordLoader = new AddCallRecordLoader();
//
//            try {
//                addCallRecordLoader.addCallRecord(IndexWebActivity.this);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String from = null;
        String forword = null;
        if (TextUtils.isEmpty(loginDataJson)) {
            return;
        }
        try {
            JSONObject loginData = new JSONObject(loginDataJson);
            from = loginData.getString("from");//登陆失败的页面地址
            forword = loginData.getString("forword");//登陆成功的页面地址
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (requestCode == 1) {//登陆请求
            if (resultCode == Config.LOGIN_CANCEL) {
                judgeAndLoadUrl(from);
            } else {
                judgeAndLoadUrl(forword);
            }
        }
    }


    protected void judgeAndLoadUrl(String url) {
        String urlLowCase = null;
        url = formatUrl(url);
        //参数区分大小写
        if (url.contains("?")) {
            urlLowCase = url.substring(0, url.split("\\?")[0].length());
            urlLowCase = urlLowCase.toLowerCase();
            urlLowCase = urlLowCase + "?" + url.split("\\?")[1];
        } else {
            urlLowCase = url.toLowerCase();
        }
        if (urlLowCase.contains("file:")) {//肯定是完整文件路径，加载本地文件即可
            String filePath = urlLowCase.substring(urlLowCase.indexOf("demo"), url.lastIndexOf("html") + 4);
            filePath = Environment.getExternalStorageDirectory() + File.separator + filePath;
            if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                refreshPage(url);
            } else {
                refreshPage("https://" + filePath.split("demo/")[1]);
            }

        } else if (urlLowCase.contains("http:")||urlLowCase.contains("https:")) {//判断网络路径
            String newUrl = urlLowCase;
            if (urlLowCase.contains("http://mdingzhi.demo.com/?")||urlLowCase.contains("https://mdingzhi.demo.com?")|| urlLowCase.contains("http://mdingzhi.demo.com?")||urlLowCase.contains("https://mdingzhi.demo.com/?")) {
                newUrl = HOST + "hybrid/home/index.html?devicetype=android";
            } else if (urlLowCase.contains(HOST)) {
                newUrl = urlLowCase.split("\\?")[0];
                if (!newUrl.contains("hybrid")) {
                    newUrl = HOST + "hybrid/" + newUrl.split(HOST)[1];
                } else if (!urlLowCase.contains(".html")) {
                    newUrl = newUrl + "index.html";
                }
                newUrl = newUrl + "?" + urlLowCase.split("\\?")[1];
            }

            //判断本地有没有文件
            String filePath="";
            if (newUrl.contains("mdingzhi")){
                filePath = newUrl.substring(newUrl.indexOf("mdingzhi"), newUrl.indexOf("?"));
                filePath = Environment.getExternalStorageDirectory() + File.separator + "demo/" + filePath;
            }

            if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                refreshPage("file://" + filePath + "?" + newUrl.split("\\?")[1]);
            } else {
                refreshPage(urlLowCase);
            }
        }
        else if (urlLowCase.contains("http:")||urlLowCase.contains("https:")){
            refreshPage(urlLowCase);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        hybridCacheDao.close();
    }


/**
     * 重写物理返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (isSingleProductOrder){//如果是单项订单，返回到订单列表
//            返回订单列表页面
                Intent intent = new Intent(this, OrderListNewActivity.class);
                startActivity(intent);
                finish();
            }else{
                callJs("andriodGoBack");
                callJs("androidGoBack");
                if (top_bar.getVisibility() == View.VISIBLE) {//如果加载了错误页面，那么点击返回回到应用首页
                    finish();
                }
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected String pageStartedOverride(WebView view, String url, Bitmap favicon) {
        return super.pageStartedOverride(view, url, favicon);
    }

    @Override
    protected void pageFinishedOverride(WebView view, String url) {
        cancelDialog();
        super.pageFinishedOverride(view, url);
    }

    @Override
    protected void receivedErrorOverride(WebView view, int errorCode, String description, String failingUrl) {
        cancelDialog();
        setTopbarVisibility(View.VISIBLE);
        middleTitle.setText("访问失败，请重试");
        layout_no_data.setVisibility(View.VISIBLE);

        super.receivedErrorOverride(view, errorCode, description, failingUrl);
    }

/**
     * 退出加载提示
     */

    private void cancelDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


/**
     * 处理收藏按钮操作
     * @param operate 操作：1；收藏 2；取消收藏
     * @param userId
     * @param topicId 活动id
     * @param favoriteId 收藏id
     */

    private void dealWithCollectButton(int operate,String userId, String topicId,String favoriteId){
        CommonRequestField commonRequestField = CommReqFieldValuePackag.getCommReqField(context);

        com.alibaba.fastjson.JSONObject request = new com.alibaba.fastjson.JSONObject();
        request.put("clientSource", commonRequestField.getClientSource());
        request.put("phoneID", commonRequestField.getPhoneID());
        request.put("phoneType", commonRequestField.getPhoneType());
        request.put("phoneVersion", commonRequestField.getPhoneVersion());
        request.put("startCity", commonRequestField.getStartCity());
        request.put("Path", "http://msitelogic.demo.com/api/");
        request.put("ControllerName", "MyFavorite");
        if (operate==1){//收藏
            request.put("ActionName", "Create");
            com.alibaba.fastjson.JSONObject postData = new com.alibaba.fastjson.JSONObject();
            postData.put("UserId", userId);
            postData.put("ProductId", topicId);
            request.put("PostData", postData);
        }else{//取消收藏
            request.put("ActionName", "Cancel");
            com.alibaba.fastjson.JSONObject postData = new com.alibaba.fastjson.JSONObject();
            postData.put("Id", favoriteId);
            request.put("PostData", postData);
        }

        if (NetworkManageUtil.isWiFiActive(context) || NetworkManageUtil.isNetworkAvailable(context)) {
            try {
                Plugin.getHttp(context).getFindHome(
                        eventCollect,
                        DESUtil.des3EncodeCBC(request.toString().getBytes("UTF-8"),
                                IKeySourceUtil.PASSWORD_CRYPT_KEY));
                LogUtil.i("hww", "requestCollect:" + request.toString());
            } catch (Exception ex) {
                Message msg = Message.obtain();
                msg.what = COLLECT_ERROR;
                if (dataHandler != null) {
                    dataHandler.sendMessage(msg);
                }
                LogUtil.i("hww","error="+ex.getMessage().toString());
            }
        } else {
            DialogUtil.toastForShort(context, getResources().getString(R.string.network_exception));
        }
    }

    private CommonReceiver commonReceiver;
    IDataEvent<String> eventCollect = new IDataEvent<String>() {

        @Override
        public void onProcessFinish(int what, String result) {
            if (!TextUtils.isEmpty(result)) {
                try {
                    CommonReceiveDTO commonReceiveDTO = JSON.parseObject(result, CommonReceiveDTO.class);
                    if (commonReceiveDTO != null
                            && commonReceiveDTO.getMC() == 1000
                            && commonReceiveDTO.getContent().length() > 0) {
                        String data = DESUtil.des3DecodeCBC(commonReceiveDTO.getContent());
                        LogUtil.i("hww", "hww:collect" + data);
                        commonReceiver = JSON.parseObject(data, CommonReceiver.class);

                        Message msg = Message.obtain();
                        msg.what = COLLECT_RESULT;
                        if (dataHandler != null) {
                            dataHandler.sendMessage(msg);
                        }

                    }
                } catch (Exception e) {
                    Message msg = Message.obtain();
                    msg.what = COLLECT_ERROR;
                    if (dataHandler != null) {
                        dataHandler.sendMessage(msg);
                    }
                    LogUtil.i("hww","error"+e.getMessage().toString());
                }
            } else {
                DialogUtil.toastForShort(context, getResources().getString(R.string.network_exception));
            }
        }
    };

    //跳转新详情页判断
    IDataEvent<String> detailType = new IDataEvent<String>() {
        @Override
        public void onProcessFinish(int i, String result) {
            Intent intent = new Intent();
            cancelDialog();
            if (!TextUtils.isEmpty(result)) {
                try {
                    CommonReceiveDTO commonReceiveDTO = JSON.parseObject(result, CommonReceiveDTO.class);
                    if (commonReceiveDTO.getMC() == 1000 && commonReceiveDTO.getContent().length() > 0) {
                        String data = DESUtil.des3DecodeCBC(commonReceiveDTO.getContent());
                        LogUtil.i("ldq", "IndexWebActivity--产品详情：" + data.toString());
                        CommonReceiver detailTypeResult = JSON.parseObject(data, CommonReceiver.class);
                        if (detailTypeResult.getErrorCode() == 200) {
                            DetailTypeReceive jsonResult = JSON.parseObject(detailTypeResult.getJsonResult(), DetailTypeReceive.class);
                            LogUtil.i("ldq", "IndexWebActivity--type详情：" + jsonResult.toString());
                            preferencesUtils.putString(String.valueOf(productID),jsonResult.getJumpType());
                            if (jsonResult.getJumpType().equals("10")) {
                                intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
                                intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
                                startActivity(intent);
                            }else if (jsonResult.getJumpType().equals("30")){
                                intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
                                intent.putExtra("url", IKeySourceUtil.WEB_SINGLE_PRODUCT_DETAIL + productID);
                                startActivity(intent);
                            }
                            else{
                                intent = new Intent(IndexWebActivity.this, ProductDetailUi540.class);
                                intent.putExtra("demoTravelClass", classID);
                                intent.putExtra("ProductID",productID);
                                startActivity(intent);
                            }
                            isIntent = true;
                            return;
                        }
                    }
                } catch (Exception e) {
//                    DialogUtil.toastForShort(IndexWebActivity.this, "暂无数据！");
                }
            } else {
//                DialogUtil.toastForShort(IndexWebActivity.this, getResources().getString(R.string.network_exception));
            }
            intent = new Intent(IndexWebActivity.this, ProductDetailUi540.class);
            intent.putExtra("demoTravelClass", classID);
            intent.putExtra("ProductID",productID);
            startActivity(intent);
            isIntent = true;
        }
    };
    //跳转新详情页判断
    IDataEvent<String> detailType2 = new IDataEvent<String>() {
        @Override
        public void onProcessFinish(int i, String result) {
            Intent intent = new Intent();
            cancelDialog();
            if (!TextUtils.isEmpty(result)) {
                try {
                    CommonReceiveDTO commonReceiveDTO = JSON.parseObject(result, CommonReceiveDTO.class);
                    if (commonReceiveDTO.getMC() == 1000 && commonReceiveDTO.getContent().length() > 0) {
                        String data = DESUtil.des3DecodeCBC(commonReceiveDTO.getContent());
                        LogUtil.i("ldq", "IndexWebActivity--产品详情：" + data.toString());
                        CommonReceiver detailTypeResult = JSON.parseObject(data, CommonReceiver.class);
                        if (detailTypeResult.getErrorCode() == 200) {
                            DetailTypeReceive jsonResult = JSON.parseObject(detailTypeResult.getJsonResult(), DetailTypeReceive.class);
                            LogUtil.i("ldq", "IndexWebActivity--type详情：" + jsonResult.toString());
                            preferencesUtils.putString(String.valueOf(productID),jsonResult.getJumpType());
                            if (jsonResult.getJumpType().contains("10")) {
                                intent = new Intent(IndexWebActivity.this, ProductDetail548Activity.class);
                                intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
                                startActivity(intent);
                            } else if (jsonResult.getJumpType().contains("0")) {
                                intent = new Intent(context,ProductDetailUi540.class);
                                intent.putExtra("ProductID",productID);
                                startActivity(intent);
                            }
                            isIntent = true;
                            return;
                        }
                    }
                } catch (Exception e) {
//                    DialogUtil.toastForShort(IndexWebActivity.this, "暂无数据！");
                }
            } else {
//                DialogUtil.toastForShort(IndexWebActivity.this, getResources().getString(R.string.network_exception));
            }
            intent = new Intent(IndexWebActivity.this, ProductDetailUi540.class);
            intent = new Intent(context,ProductDetailUi540.class);
            intent.putExtra("ProductID",productID);
            startActivity(intent);
            isIntent = true;
        }
    };
}

