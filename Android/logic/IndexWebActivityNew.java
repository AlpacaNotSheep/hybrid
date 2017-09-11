
package com.demo.app.hybrid.logic;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.mobile.core.http.event.IDataEvent;
import com.ptmind.sdk.PtAgent;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;
import com.demo.app.AppUpgradeService;
import com.demo.app.R;
import com.demo.app.activity.OrderDetailNewActivity;
import com.demo.app.activity.PayDialogActivity;
import com.demo.app.activity.webOrPay.SubOrderPayActivity;
import com.demo.app.alipay.AlixPay;
import com.demo.app.alipay.BaseHelper;
import com.demo.app.alipay.PayResult;
import com.demo.app.apshare.ShareEntryActivity;
import com.demo.app.data.load.AddCallRecordLoader;
import com.demo.app.domain.CommonReceiveDTO;
import com.demo.app.domain.CommonRequestField;
import com.demo.app.domain.demand.MarkMessageRequest;
import com.demo.app.domain.demand.PayConfigYinlianRequest;
import com.demo.app.domain.receive.CommonReceiver;
import com.demo.app.domain.receive.OrderPayConfigYinlianReceive;
import com.demo.app.domain.receive.OrderPayConfigZhifubaoReceive;
import com.demo.app.domain.receive.OrderWeiXinConfigInfoNewDTO;
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
import com.demo.app.mvp.module.product.activity.ProductShowList553Activity;
import com.demo.app.mvp.utils.AntiEmulator;
import com.demo.app.util.ApplicationValue;
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
import com.demo.app.util.PayPublicUtil;
import com.demo.app.util.PhoneInfoUtil;
import com.demo.app.util.SharedPreferencesUtils;
import com.demo.app.util.UserInfoCheckUtil;
import com.demo.app.util.demoProUtil;
import com.demo.app.util.VoidRepeatClickUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
public class IndexWebActivityNew extends BaseWebActivity {
    private WebView orderWebView;
    private TextView titleTextView;//// TODO: 2017/4/26

    /**
     * 访问接口失败
     */
    private AlertDialog builder;
    /**
     * 顶部返回主页面的按钮
     */
    private ImageView rightHomeBtn;
    /**
     * 如果活动的url链接里包含/subject则不显示原生的加载动画，加载动画h5页面自己写
     */
    private boolean isShowLoading = true;
    /**
     * 标题集合
     */
    private List<String> titles = new ArrayList<String>();

    private int what;
    private int isUseCache;
    private String param;
    private String url;
    private String shareUrl;//用于分享

    private HybridCacheDao hybridCacheDao;
    private String phoneNumber;
    private static String HOST = "https://mdingzhi.demo.com/";
    private String loginDataJson;
//    private Dialog dialog;
    private String subjectType;
    private Context context;
    private String topicId;
    private int operateCode;//区分收藏操作码 1：添加收藏 2：取消收藏
    private static final int COLLECT_RESULT = 1;//收藏按钮操作成功·
    private static final int COLLECT_ERROR = 2;//收藏按钮操作失败
    private String currentGAPath;
    private String classID;
    private boolean netAvailable = true;

    private String sid = "";
    private final int REQUEST_CONTACT = 3;
    private boolean hasThirdLoginUrl = false;
    private String planeTicketUrl;
    private String productName, prepayMent, orderID, orderCode, number;
    private String comeFrom ;
    /**
     * 是否显示home按钮的标识
     */
    private boolean isShowHome = false;
    /**
     * 是否需要认证的标识，当值为1时，请求接口地址 http://pay.demo.com/ GetPayConfig
     * 改为https://pay.demo.com/ GetPayConfig
     */
    private int isSSL = 0;

    /**
     * 是否为众信账号，true：众信账号，false：悠哉账号
     */
    private int isUtour;
    /**
     * 银联支付信息
     */
    private OrderPayConfigYinlianReceive orderPayConfigYinlianEncrypt;
    /**
     * 支付宝 支付信息
     */
    private OrderPayConfigZhifubaoReceive orderPayConfigZhifubaoEncrypt;
    /**
     * 微信的支付信息
     */
    private OrderWeiXinConfigInfoNewDTO orderWeiXinConfigEncrypt;
    /**
     * 支付方式：0为支付宝，1为微信
     */
    private AlixPay alixPay;
    private int payType = 0;
    private boolean isPlaneTicket = false;
    /**
     * 基带控制对话框
     */
    private AlertDialog baseBandDialog;
    /**
     * loading持续时间
     */
    private long loadingDuringTime=3000;

    @Override
    public int getResLayoutId() {
        return -1;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, getIntent().getStringExtra(IKeySourceUtil.GA_FROM_FLAG), null);
        currentGAPath = gaPtahString;
        initData();
        loadPageFormat(url);
    }

    private void initData() {
        int message_id = getIntent().getIntExtra("message_id", 0);
        if (message_id > 0) {
            markCollectInfo(message_id);
        }

        context = this;
        hybridCacheDao = GreenDaoManager.getInstance().getDaoSession().getHybridCacheDao();
        dialog = DialogUtil.buildDialogRecover(IndexWebActivityNew.this);
        loadingDelay();
        preferencesUtils = new SharedPreferencesUtils(context, "productdetail");

        //初始化localStorage
        locationProvince = getSharedPreferences("Location", Context.MODE_PRIVATE).getString("LocationProvince", IKeySourceUtil.DEFAULT_CITY);
        locationCity = new SharedPreferencesUtils(IndexWebActivityNew.this, "StartCity").getString("name", IKeySourceUtil.CITY);
        startCity = new SharedPreferencesUtils(IndexWebActivityNew.this, "StartCity").getString("name", IKeySourceUtil.CITY);
        appVersion = CommReqFieldValuePackag.getCommReqField(IndexWebActivityNew.this).getPhoneVersion();
        userid = getSharedPreferences(IKeySourceUtil.LOGIN_STATUS, Context.MODE_PRIVATE).getString("demoId", "0");

        comeFrom = getIntent().getStringExtra("ComeFrom");
        if (TextUtils.isEmpty(comeFrom)) {
            comeFrom = "normal";
        }

        topicId = String.valueOf(getIntent().getIntExtra("topicId", 0));
        if (!TextUtils.isEmpty(getIntent().getStringExtra("ActivityUrl"))){
            url=getIntent().getStringExtra("ActivityUrl");
            shareUrl = getIntent().getStringExtra("ActivityUrl");
        }

        if (!TextUtils.isEmpty(getIntent().getStringExtra("url"))){
            url = getIntent().getStringExtra("url");
            shareUrl = getIntent().getStringExtra("url");
        }
        //需要对url进行处理，如果是不完整路径的链接，需要处理为完整路径的链接,这样才能找到sd卡中本地文件
        if (url.equals("https://mdingzhi.demo.com/")) {
            url = "https://mdingzhi.demo.com/hybrid/home/index.html";
        } else if (url.equals("http://mdingzhi.demo.com/")) {
            url = "http://mdingzhi.demo.com/hybrid/home/index.html";
        }
        if (getIntent() != null) {
            Intent intent = getIntent();
            String mLinkActivityUrl = intent.getStringExtra("activityUrl"); //mLink activityUrl的参数

            if (!TextUtils.isEmpty(mLinkActivityUrl)) {
                if (mLinkActivityUrl.contains("@")) {
                    mLinkActivityUrl = mLinkActivityUrl.replace("@", "=");
                    if (mLinkActivityUrl.contains("!")) {
                        mLinkActivityUrl = mLinkActivityUrl.replace("!", "&");
                    }
                }
                url = mLinkActivityUrl;
            }
        }

        subjectType = getIntent().getStringExtra("subjectType");
        String subjectName = getIntent().getStringExtra("subjectName");
        boolean isShowNav = getIntent().getBooleanExtra("isNav", false);

        //判断是否可以分享
        int isShare = -1;
        isShare = getIntent().getIntExtra("isShare", 0);
        if (isShare == 1) {// 如果传过来的值为1 可以分享
            rightShareButton.setVisibility(View.VISIBLE);
        } else {
            rightShareButton.setVisibility(View.GONE);
        }

        if ("3".equals(subjectType) || isShowNav) {//如果是3，表示需要展示顶部导航条，处理返回键
            setTopbarVisibility(View.VISIBLE);
        } else {
            setTopbarVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(subjectName)) {
            middleTitle.setText(subjectName);
        }

        if (!TextUtils.isEmpty(url)) {
            url = url.trim();
            if (url.contains("/subject")) {
                isShowLoading = false;
            }
        }

        url = CookieUtil.getWebviewUrl(url, context, comeFrom);
        LogUtil.i("hww", "hww:url=" + url);

        if (url.contains("/ticketIndex") || url.contains("/SpecialTicketOrders"))
            isPlaneTicket = true;

        setListner();
        addUserAgent();

    }

    private void loadingDelay() {
        if (handler!=null){
            handler.sendEmptyMessageDelayed(IKeySourceUtil.CLOSE_WEB_LOADING,loadingDuringTime);
        }
    }

    private void setListner() {
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowHome = false;
                if (orderWebView.canGoBack()) {
                    orderWebView.goBack();
                    if (titles.size() > 0) {
                        titles.remove(titles.size() - 1);
                        setWebTitle();
                    }
                } else {
                    demoProUtil.firstMlinkGotoHome(context);
                    finish();
                }
            }
        });

        // 分享的按钮  //todo 重复
        int isShare = -1;
        String mLinkIsShare = "";
        if (Const.mLinkGoToWeb) {
            mLinkIsShare = getIntent().getStringExtra("isShare");
            Const.mLinkGoToWeb = false;
        } else {
            isShare = getIntent().getIntExtra("isShare", 0);
        }
        if (!TextUtils.isEmpty(mLinkIsShare)) {
            isShare = Integer.parseInt(mLinkIsShare);
        }
        if (isShare == 1) {// 如果传过来的值为1 可以分享
            rightShareButton.setVisibility(View.VISIBLE);
//            rightShareButton.setBackgroundResource(R.drawable.share_btn_selector);
            rightShareButton.setOnClickListener(this);
        } else {
            rightShareButton.setVisibility(View.GONE);
        }

        //分享按钮点击事件
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
                            if (TextUtils.isEmpty(topicsName)) {
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
                                shareUrl = intent1.getStringExtra("shareUrl");
                            }
                            if (TextUtils.isEmpty(shareContent) || shareContent == null) {
                                shareContent = "";
                                shareContent = callJsDescription("document.querySelector('meta[name=description]').content");
                                if (shareUrl.contains("?")) {
                                    HashMap<String, String> map = CookieUtil
                                            .ParseTokenString(shareUrl);
                                    if (map != null && map.get("description") != null) {
                                        shareContent = map.get("description");
                                    }
                                }
                            }
                            //设置分享的数据
                            CookieUtil.setShareData(IndexWebActivityNew.this, 2, shareUrl,
                                    imgUrl, topicsName,
                                    shareUrl, shareContent + " "
                                            + shareUrl + " 快来看看");
                            Intent intent = new Intent();//跳转到分享的界面
                            intent.setClass(IndexWebActivityNew.this,
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


        //设置webview
        orderWebView = webView;
        orderWebView.clearCache(true);
        orderWebView.getSettings().setDefaultTextEncodingName("utf-8");
        orderWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);// 允许js弹出窗口
        orderWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
        // 设置可以支持缩放
        orderWebView.getSettings().setSupportZoom(true);
        // 设置不出现缩放工具
        orderWebView.getSettings().setBuiltInZoomControls(false);
        //自适应屏幕
        orderWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        orderWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                String appName = getString(R.string.demo_travel);
                try {
                    if (url.contains(".apk")) {
                        appName = url.substring(0, url.indexOf("?"));
                        appName = appName.substring(appName.lastIndexOf("/") + 1, appName.length());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Intent updateIntent = new Intent(context, AppUpgradeService.class);
                updateIntent.putExtra("downloadUrl", url);
                updateIntent.putExtra("isNeedInstall", true);// 第三方应用是否允许安装
                updateIntent.putExtra("appName", appName);
                startService(updateIntent);

            }
        });
//        orderWebView.setWebChromeClient(webChromeClient);//// TODO: 2017/4/26

        rightHomeBtn= (ImageView) findViewById(R.id.right_home_btn);
        rightBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ApplicationValue.getInstance().removeActivitys();
                Const.ifRutrnMainPage = true;
                Const.mainTabSite = 0;
            }
        });

    }

    //对传递过来的url进行判断，判断是加载网络还是加载本地文件
    private void loadPageFormat(String url) {
        if (url.contains("http") || url.contains("https")) {
            judgeHttpUrl(url);

        } else if (url.contains("file:")) {
            judgeFileUrl(url);
        }
    }


    /**
     * 如果传递过来的url是http格式的，进行判断
     *
     * @param url
     */

    private void judgeHttpUrl(String url) {
        String noParamsHttpUrl = "";//不带参数的http格式的url
        String params = "";//参数
        if (url.contains("?")&&url.split("\\?").length>1) {
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
            refreshPage(url);
            //如果版本文件中为空那么下载版本文件
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
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
                            String local_path = null;
                            if (p.contains(https2HttpUtils.HTTPS)) {
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("https://", "");
                            } else {
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "");
                            }
                            if (FileUtils.isFileExists(local_path)) {//加载本地文件
                                LogUtil.i("hww", "本地");
                                haveLoadPage = true;
                                refreshPage("file://" + local_path + params);
                                break;
                            } else {//加载网络文件
                                LogUtil.i("hww", "网络");
                                haveLoadPage = true;
                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                refreshPage(url);
                                init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
                            }
                        }
                    }
                }

                //循环结束如果没有加载页面，那么直接加载网络
                if (!haveLoadPage) {
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
     *
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

//            //判断本地有无缓存
            HybridCache hybridCache = hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
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
            HybridCache hybridCache = hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
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
            LogUtil.i("hww", "hww:" + jsonObject.toString());
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
                        } else if (!TextUtils.isEmpty(jsonObject.getString("JsonResult"))) {
                            callCallback(String.valueOf(what), data);
                        } else {
                            callBackErrorData(errorCode, jsonObject.getString("ErrorMsg"));
                        }
                        //保存缓存数据
                        if (isUseCache > 0 && errorCode == 200) {
                            HybridCache hybrid = hybridCacheDao.queryBuilder().where(HybridCacheDao.Properties.Param.eq(param)).unique();
                            //需要判空,没有查询到hybrid发返回null。否则会抛出异常
                            if (null != hybrid) {
                                hybridCacheDao.deleteByKey(hybrid.getId());
                            }

                            HybridCache hybridCache = new HybridCache();
                            hybridCache.setCurrentTime(System.currentTimeMillis());
                            hybridCache.setIsUseCache(isUseCache);
                            hybridCache.setParam(param);
                            hybridCache.setData(data);
                            hybridCacheDao.insert(hybridCache);
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
     *
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
            Intent intent = new Intent(IndexWebActivityNew.this, LoginActivity601.class);
//            Intent intent = new Intent(IndexWebActivityNew.this, LoginThirdActivity540.class);
            intent.putExtra("fromHybrid", "fromHybrid");
            intent.putExtra("PageName", currentGAPath);
            startActivityForResult(intent, 1);

        }
        else if (keyLowerCase.contains("opendetail")) {//打开定价产品详情页
            try {
                JSONObject detailData = new JSONObject(jvalue);
                String classId = detailData.getString("ClassId");
                String productId = detailData.getString("ProductId");
                productID = Long.parseLong(productId);
                classID = classId;
                detailTypeUtil.jumpProductDetail(productID,0,0,currentGAPath + "->跟团产品页");
//                if (isIntent) {
//                    isIntent = false;
//                    dialog = DialogUtil.buildDialogRecover(IndexWebActivityNew.this);
//                    Intent intent = null;
//                    if (preferencesUtils.contains(String.valueOf(productID))) {
//                        cancelDialog();
//                        if (preferencesUtils.getString(String.valueOf(productID), "").contains("10")) {
//                            intent = new Intent(IndexWebActivityNew.this, ProductDetail548Activity.class);
//                            intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
//                            startActivity(intent);
//                        } else {
//                            intent = new Intent(IndexWebActivityNew.this, ProductDetailUi540.class);
//                            intent.putExtra("demoTravelClass", classID);
//                            intent.putExtra("ProductID", productID);
//                            startActivity(intent);
//                        }
//                        isIntent = true;
//                        return "";
//                    }
//                    detailTypeUtil.getDetailType(0, (int) productID, 0, detailType);
//                }
////                Intent intent = new Intent(IndexWebActivityNew.this, ProductDetailUi540.class);
////                intent.putExtra("demoTravelClass", classId);
////                intent.putExtra("ProductID", Long.parseLong(productId));
////                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (keyLowerCase.contains("action.collect")) {//进行收藏 用户未登录状态下可以截取到收藏字段
            if (!VoidRepeatClickUtil.isFastDoubleClick()) {
                operateCode = 1;
                // 判断是否登录
                mBaseApplicate.ifRutrnMydemoPage = false;
                if (!UserInfoCheckUtil.checkLogin(IndexWebActivityNew.this, null, 0, null, "test" + "->登录页")) {
                    return "";
                }
                dealWithCollectButton(operateCode, String.valueOf(userid), topicId, null);
            }
        } else if (keyLowerCase.contains("action.cancelfavorite")) {//取消收藏,用户只有登录状态才会截取到取消收藏字段
            if (!VoidRepeatClickUtil.isFastDoubleClick()) {
                operateCode = 2;
                // 判断是否登录
                mBaseApplicate.ifRutrnMydemoPage = false;
                if (!UserInfoCheckUtil.checkLogin(IndexWebActivityNew.this, null, 0, null, "test" + "->登录页")) {
                    return "";
                }

                try {
                    JSONObject cancelObject = new JSONObject(jvalue);
                    String favoriteid = cancelObject.getString("favoriteid");
                    dealWithCollectButton(operateCode, String.valueOf(userid), topicId, favoriteid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (keyLowerCase.contains("action.getfile")) {
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
        else if (keyLowerCase.equals("go.payback")) {//支付页面点击后退
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
     *
     * @param jvalue
     */
    private void payback(String jvalue) {
        String orderID = null;
        String orderType = null;
        boolean isSonOrder = false;
        try {
            JSONObject jsonObject = new JSONObject(jvalue);
            if (jvalue.contains("orderID")) {
                orderID = jsonObject.getString("orderID");
            }
            if (jvalue.contains("orderType")) {
                orderType = jsonObject.getString("orderType");
            }
            if (jvalue.contains("isSonOrder")) {
                isSonOrder = jsonObject.getBoolean("isSonOrder");
            }

            if (TextUtils.isEmpty(orderID) || orderID.equals("0") ) {//如果为空的话默认跳转到订单列表
                Intent intent = new Intent(this, OrderListNewActivity.class);
                startActivity(intent);
            } else if (isSonOrder) {
                Intent intent = new Intent(this, SubOrderPayActivity.class);//不需要刷新
                intent.putExtra("orderID", orderID);
                intent.putExtra("orderType", orderType);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, OrderDetailNewActivity.class);
                intent.putExtra("orderID", orderID);
                intent.putExtra("orderType", orderType);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected boolean shouldOverrideUrlLoadingOverride(WebView view, String overrideUrl) {
        //回到app首页
        if (overrideUrl.equals("https://m.demo.com/")||overrideUrl.equals("https://m.demo.com")||overrideUrl.equals("http://m.demo.com/")||overrideUrl.equals("http://m.demo.com")){
            ApplicationValue.getInstance().removeActivitys();
            Const.ifRutrnMainPage = true;
            Const.mainTabSite = 0;
            return true;
        }

        String urlOrigin = overrideUrl;
        String urlLowCase = null;
        //参数区分大小写
        if (overrideUrl.contains("?")) {
            urlLowCase = overrideUrl.substring(0, overrideUrl.split("\\?")[0].length());
//            urlLowCase = urlLowCase.toLowerCase();
            urlLowCase = urlLowCase + "?" + overrideUrl.split("\\?")[1];
        } else {
//            urlLowCase = overrideUrl.toLowerCase();//// TODO: 2017/4/26
        }
        LogUtil.i("hww", "hww:overrideUrl" + urlLowCase);
        //处理首页back
        String urlLowCaseBack = urlLowCase.split("\\?")[0];//用来处理私人定制页返回首页事件
        if (urlLowCaseBack.equals("http://m.demo.com") || urlLowCaseBack.equals("http://m.demo.com/") || urlLowCaseBack.equals("https://m.demo.com") || urlLowCaseBack.equals("https://m.demo.com/")) {
            demoProUtil.firstMlinkGotoHome(context);
            finish();

        } else if (urlLowCase.contains("discovery") && urlLowCase.contains("index.html")) {//如果在详情页，处理返回键，返回上一页
            finish();

        } else if (urlLowCase.contains("/PhoneList?sid")) {
            sid = urlLowCase.substring(urlLowCase.lastIndexOf("=") + 1);
            new RxPermissions(IndexWebActivityNew.this)
                    .request(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)//通讯录
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_PICK);
                            intent.setData(ContactsContract.Contacts.CONTENT_URI);
                            startActivityForResult(intent, REQUEST_CONTACT);
                        } else {
                            DialogUtil.toastForShort(context, "请同意我们的权限，才能提供服务");
                        }
                    });
        } else if (urlLowCase.contains("tel:")) {
            if (urlLowCase.split("\\?").length > 1) {//tel:01010109898?devicetype=android
                urlLowCase = urlLowCase.substring(0, urlLowCase.indexOf("?"));
            }
            phoneNumber = urlLowCase.substring("tel:".length(), urlLowCase.length());
            //统一显示从后台获取的客服电话
            if (phoneNumber.equals("") || phoneNumber == null) {
                phoneNumber = Const.kefuPhone_tel;
            }
            DialogUtil.showBuilders(null, IndexWebActivityNew.this,
                    getString(R.string.prompt),
                    "客服电话：" + phoneNumber,
                    getString(R.string.call), getString(R.string.cancel),
                    callOnClickListener);

        } else if (urlLowCase.contains("wd/") && urlLowCase.contains("word=")) {
            String searchContent = URLDecoder.decode(urlLowCase.split("word=")[1].split("&")[0]);
            Intent intent = new Intent();
//                intent.setClass(context, ProductShowList520Activity.class);
//                intent.putExtra("travelClassID", 0);// 1
//                // 为“出境游”，2为“国内游”，3为“周边游”，4为
//                // “当地游”，238为 “邮轮游”，0为搜索
//                intent.putExtra("searchContent", searchContent);
//                intent.putExtra("noSelect", true);
//                intent.putExtra("ga_to_flag", searchContent + "线路列表");
            intent.setClass(context, ProductShowList553Activity.class);
            intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, currentGAPath);
            intent.putExtra("searchContent", searchContent);
            startActivity(intent);
            return true;
        } else if (urlLowCase.contains("m.demo.com/search/list")) {//跳转新的货架列表页
            urlLowCase = urlLowCase.substring(urlLowCase.indexOf("?") + 1, urlLowCase.length());
            HashMap<String, String> map = CookieUtil.ParseTokenString(urlLowCase);
            Intent intent = new Intent();
            intent.setClass(context, ProductShowList553Activity.class);
            if (map.get("city") != null) {
                intent.putExtra("city", map.get("city"));
            }
            if (map.get("keyword") != null) {
                intent.putExtra("keyword", URLDecoder.decode(map.get("keyword")));
            }
            if (map.get("traveclass") != null) {
                intent.putExtra("traveclass", URLDecoder.decode(map.get("traveclass")));
            }
            if (map.get("preferential") != null) {
                intent.putExtra("preferential", URLDecoder.decode(map.get("preferential")));
            }
            if (map.get("play") != null) {
                intent.putExtra("play", URLDecoder.decode(map.get("play")));
            }
            if (map.get("price") != null) {
                intent.putExtra("price", URLDecoder.decode(map.get("price")));
            }
            if (map.get("scenic") != null) {
                intent.putExtra("scenic", URLDecoder.decode(map.get("scenic")));
            }
            if (map.get("day") != null) {
                intent.putExtra("day", URLDecoder.decode(map.get("day")));
            }
            if (map.get("date") != null) {
                intent.putExtra("date", URLDecoder.decode(map.get("date")));
            }
            if (map.get("pageindex") != null) {
                intent.putExtra("pageindex", URLDecoder.decode(map.get("pageindex")));
            }
            if (map.get("sort") != null) {
                intent.putExtra("sort", URLDecoder.decode(map.get("sort")));
            }
            if (map.get("destination") != null) {
                intent.putExtra("destination", URLDecoder.decode(map.get("destination")));
            }
            if (map.get("company") != null) {
                intent.putExtra("company", URLDecoder.decode(map.get("company")));
            }
            if (map.get("cruises") != null) {
                intent.putExtra("cruises", URLDecoder.decode(map.get("cruises")));
            }
            startActivity(intent);
            return true;
        }
        else if (urlLowCase.contains("m.demo.com/goAppHome")) {//如果包含这个字符串 直接返回到App首页
            ApplicationValue.getInstance().removeActivitys();
            Const.ifRutrnMainPage = true;
            Const.mainTabSite = 0;
        }
        else if (urlLowCase.contains("touch_three")
                && urlLowCase.contains("source=android")) {
            if (view.canGoBack()) {
                view.goBack();
            }
        }
        else if (urlLowCase.contains("loginflag=true") && urlLowCase.contains("?loginSucceedUrl=")) {
            hasThirdLoginUrl = true;
            int start = urlLowCase.indexOf("?loginSucceedUrl=") + "?loginSucceedUrl=".length();
            planeTicketUrl = urlLowCase.substring(start);
            toThirdLogin();
            return true;
        } else if (urlLowCase.contains("/AppLogin") && urlLowCase.contains("?loginSucceedUrl=")) {
            hasThirdLoginUrl = true;
            // 指定登录成功后跳转地址
            if (urlLowCase.contains("?loginSucceedUrl=")) {
                int start = urlLowCase.indexOf("?loginSucceedUrl=") + "?loginSucceedUrl=".length();
                planeTicketUrl = urlLowCase.substring(start);
            }
            toThirdLogin();
            return true;
        } else if (urlLowCase.contains("type=share")
                && (urlLowCase.contains("&refUrl=") || urlLowCase.contains("&shareLink="))) {//分享
            int start = urlLowCase.indexOf("&refUrl=") + "&refUrl=".length();

            String shareSuccessUrl = urlLowCase.substring(start);
//                String[] temp=overrideUrl.split("&shareLink=");
//                overrideUrl=temp[0]+"&shareLink="+URLDecoder.decode(temp[1]);
            HashMap<String, String> map = CookieUtil.ParseTokenString(urlLowCase);
            LogUtil.e(context, map.get("keywords"));
            LogUtil.e(context, map.get("description"));
            LogUtil.e(context, map.get("pic"));
            String keywords = map.get("keywords");
            String description = map.get("description");
//				// TODO
//				CookieUtil.setShareData(ActivityWebActivity.this, 2, url,
//						map.get("picture"), titleTextView.getText().toString()
//								.trim(), map.get("URL"),
//						content + " " + map.get("URL") + " 快来看看");

            if (!VoidRepeatClickUtil.isFastDoubleClick()) {
                try {
                    LogUtil.e(context, "解码后:" + URLDecoder.decode(keywords));
                    keywords = URLDecoder.decode(keywords).trim();//分享标题

                    LogUtil.e(context, "解码后:" + URLDecoder.decode(description));
                    description = URLDecoder.decode(description).trim();//分享内容

                    String imgUrl = map.get("pic");//需要分享图片的地址
                    if (imgUrl == null || imgUrl.length() == 0) {
                        imgUrl = Const.SHARE_DEFAULT_IMAGE_URL;
                    }
//                        送流量活动
                    if ((map.get("shareLink") != null)) {
                        overrideUrl = map.get("shareLink");
                        overrideUrl = URLDecoder.decode(overrideUrl, "UTF-8");
                        if (overrideUrl.contains("!")) {
                            overrideUrl = overrideUrl.replace("!", "&");
                        }
                    }
                    if (TextUtils.isEmpty(description) || description == null){
                        description = callJsDescription("document.querySelector('meta[name=description]').content");
                    }
                    //设置分享的数据
                    CookieUtil.setShareData(IndexWebActivityNew.this, 2, overrideUrl,
                            imgUrl, keywords,
                            overrideUrl, description + overrideUrl);
                    Const.ifShare = true;
                    Intent intent = new Intent();//跳转到分享的界面
                    intent.setClass(IndexWebActivityNew.this,
                            ShareEntryActivity.class);
                    intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, "分享界面");
                    intent.putExtra("activityType", 8);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (urlLowCase.contains("prevent_brush=true")) {
            if (AntiEmulator.checkPipes() && AntiEmulator.CheckEmulatorFiles()) {
                showBaseBandDialog();
                return true;
            }
        }
        addUserAgent();
        String prodetailUrl = urlLowCase;
        urlLowCase = CookieUtil.getWebviewUrl(urlLowCase, context, comeFrom);
        LogUtil.i(this, urlLowCase);

        if (urlLowCase.contains("new_mobile_four?type=yi")) {
            orderCompleteJump();
        } else if (urlLowCase.contains("touch_contract=true")) {//签订合同后跳转到订单列表页面
            setResult(RESULT_OK);
            finish();
            return true;
        } else if (urlLowCase.contains("new_mobile_four?type=zheng")) {
            interceptedStrPro(overrideUrl);
            PhoneInfoUtil.getInstance().trackEventForGA("order-4", "payType", "payType");
            PtAgent.advancedEvent(context, "订单/支付", null);
            paySelectDialog(isUtour);
        } else if (urlLowCase.contains("mobile_four/close")) {
            finish();
        } else if (urlLowCase.contains("m.demo.com/product/detail.html")||urlLowCase.contains(IKeySourceUtil.WEB_SINGLE_PRODUCT_DETAIL_STR)) {
            Intent intent = new Intent();
            intent.setClass(context, ProductDetail548Activity.class);
            intent.putExtra("url", prodetailUrl);
            context.startActivity(intent);
        }
        else if (urlLowCase.contains("AppDetail")) {//Url包含AppDetail  跳转原生详情页
//            m.demo.com/waptour-130026.html?/AppDetail/130026/1&devicetype=android&hybridversion=1&source=android&hybridversion=1&source=android
            String appDetailID = urlLowCase.split("\\?")[1].split("&")[0].split("/")[2]; //产品ID
            Intent intent = new Intent(context, ProductDetailUi540.class);
            intent.putExtra("ProductID", Long.parseLong(appDetailID));
            startActivity(intent);
        }else if(urlLowCase.contains("m.demo.com/waptour")){
            try {
//                String[] detailIds = urlLowCase.split("\\?");
//                detailIds = detailIds[0].split("-");
//                detailIds = detailIds[1].split("\\.");
//                String appDetailID = detailIds[0]; //产品ID
                String appDetailID = urlLowCase.split("\\?")[0].split("-")[1].split("\\.")[0]; //产品ID
                Intent intent = new Intent(context, ProductDetailUi540.class);
                intent.putExtra("ProductID", Long.parseLong(appDetailID));
                startActivity(intent);
            }catch (Exception e){
                LogUtil.e("ldq----","waptour-erro");
            }
        }else if (urlLowCase.contains("loginflag=true")) {
            toThirdLogin();
        } else if (urlLowCase.contains("/AppLogin")) {
            toThirdLogin();
        } else if (urlLowCase.contains("loginflag=false")) {
            loadWebUrl(webView,urlLowCase);// 载入网页
            isShowHome = true;
        }
//        else if (urlLowCase.contains("tel:")) {//// TODO: 2017/4/26 overideUrl中有两个，取哪个？
//            DialogUtil.showBuilders(null, context,
//                    getString(R.string.prompt),
//                    getString(R.string.call_text_tip) + Const.kefuPhone,
//                    getString(R.string.call), getString(R.string.cancel),
//                    callOnClickListener);
//        }
        else if (urlLowCase.contains("tpye=share")) {
            HashMap<String, String> map = CookieUtil
                    .ParseTokenString(urlLowCase);
            LogUtil.e(context, map.get("content"));
            LogUtil.e(context, map.get("picture"));
            LogUtil.e(context, map.get("URL"));
            String content = map.get("content");
            try {
                LogUtil.e(context, "解码后:" + URLDecoder.decode(content));// (content,
                // "UTF-8"));
                content = URLDecoder.decode(content).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // TODO
            String imgUrl = map.get("picture");//需要分享图片的地址
            if (imgUrl == null || imgUrl.length() == 0) {
                imgUrl = Const.SHARE_DEFAULT_IMAGE_URL;
            }
            CookieUtil.setShareData(IndexWebActivityNew.this, 2, overrideUrl,
                    imgUrl, middleTitle.getText().toString()
                            .trim(), map.get("URL"),
                    content + " " + map.get("URL") + " 快来看看");

            Intent intent = new Intent();
            intent.setClass(context, ShareEntryActivity.class);
            intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, "分享界面");
            startActivity(intent);
        }  else if (urlLowCase.contains("specialticket/specialtickets")) {
            gaForProject(currentGAPath, "特价机票_产品页");
            loadWebUrl(webView,urlLowCase);// 载入网页
            isShowHome = true;
        } else if (urlLowCase.contains("file:")) {//肯定是完整文件路径，加载本地文件即可
            judgeFileUrl(urlLowCase);
            isShowHome = true;
        }
        /////storage/emulated/0/demo/www/mdingzhi.demo.com/hybrid/Product/List.html?dingzhitype=1&dingzhivalue=1&keyworld=%E6%AC%A7%E6%B4%B2&devicetype=android
        else if (urlLowCase.contains("http:") || urlLowCase.contains("https:")) {//判断网络路径
            String newUrl = urlLowCase;

            //定制模块专属：拼接url
            if (urlLowCase.equals("http://mdingzhi.demo.com/?devicetype=android") || urlLowCase.equals("https://mdingzhi.demo.com/?devicetype=android")) {
                newUrl = HOST + "hybrid/home/index.html?devicetype=android";
            } else if (urlLowCase.contains(HOST)) {
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
            if (newUrl.contains("mdingzhi")) {
                String filePath = newUrl.substring(newUrl.indexOf("mdingzhi"), newUrl.indexOf("?"));
                filePath = Environment.getExternalStorageDirectory() + File.separator + "demo/" + filePath;
                Log.i("hww", "http:filePath" + filePath);
                if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                    refreshPage("file://" + filePath + "?" + newUrl.split("\\?")[1]);
                    return true;
                }
            }

            judgeHttpUrl(urlLowCase);
            isShowHome = true;
        } else {
            refreshPage(urlOrigin);
            isShowHome = true;
        }
        return true;
    }

    /**
     * 跳转登录界面
     */
    private void toThirdLogin() {
        mBaseApplicate.ifRutrnMydemoPage = false;
        UserInfoCheckUtil.checkLogin(IndexWebActivityNew.this, null, 0, null,
                currentGAPath + "->登录页");
    }

    /**
     * 弹出对话框通知用户该活动仅限于手机设备参与
     */
    private void showBaseBandDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setIcon(android.R.drawable.ic_menu_info_details);
        builer.setTitle(getString(R.string.prompt));
        builer.setCancelable(false);
        builer.setMessage("该活动仅限于手机设备参与！");

        // 当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton(
                getString(R.string.confirm),
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IndexWebActivityNew.this.finish();
                    }
                });
        baseBandDialog = builer.create();
        baseBandDialog.show();
    }

    /**
     * 弹出支付方式
     */
    protected void paySelectDialog(int flag) {
        Intent intent = new Intent(IndexWebActivityNew.this,
                PayDialogActivity.class);
        intent.putExtra("PayType", flag);
        startActivityForResult(intent, 2);
    }

    /**
     * 添加user_agent
     */
    public void addUserAgent() {
        // 原来是通过cookie还设置服务器所需的参数，但服务器在接收时会非常麻烦
        // 现通过修改user_agent还设置服务器所需的参数，服务器可直接获取
        WebSettings websettings = webView.getSettings();
        String user_agent = websettings.getUserAgentString();
        //// TODO: 16/11/1
        user_agent = Config.FORMAT_USER_AGENT;
        // demo/3.6.2 (android; extension txt; extension txt)
        String addAgent = " demo/"
                + PhoneInfoUtil.getInstance().getVersion(context)
                + "(android; extension txt; extension txt)";
        String newUserAgent;
        //如果不包含 添加
        if (!user_agent.contains(addAgent)) {
            newUserAgent = user_agent + addAgent;
        } else {
            newUserAgent = user_agent;
        }
        websettings.setUserAgentString(newUserAgent);
    }

    /**
     * 创建意向订单成功跳转
     */
    private void orderCompleteJump() {
        DialogUtil.toastForLong(context,
                getString(R.string.tip_create_order_success));
        Intent intent = new Intent(context, OrderListNewActivity.class);
        intent.putExtra("orderListType", 1);
        intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, currentGAPath + "->"
                + getResources().getString(R.string.ga_order_dfk_list_page));
        intent.putExtra("TravelType", 100);
        startActivity(intent);
        // startActivity(new Intent(context, BookListActivityNew.class));
        finish();
    }

    /**
     * 截取字符串
     *
     * @param url
     */
    private void interceptedStrPro(String url) {
        String decodeUrl = java.net.URLDecoder.decode(url);
        String strArr = decodeUrl.substring(decodeUrl.indexOf("?") + 1);
        String[] keyValue = strArr.split("&");

        for (String aKeyValue : keyValue) {
            if (aKeyValue.contains("pname")) {
                productName = aKeyValue.replace("pname=", "");
            }

            if (aKeyValue.contains("nums")) {
                number = aKeyValue.replace("nums=", "");
            }

            if (aKeyValue.contains("prepayment")) {
                prepayMent = aKeyValue.replace("prepayment=", "");
            }

            if (aKeyValue.contains("orderid")) {
                orderID = aKeyValue.replace("orderid=", "");
            }

            if (aKeyValue.contains("ordercode")) {
                orderCode = aKeyValue.replace("ordercode=", "");
            }

            if (aKeyValue.contains("ordercode")) {
                orderCode = aKeyValue.replace("ordercode=", "");
            }

            if (aKeyValue.contains("IsUtour")) {
                isUtour = Integer.valueOf(aKeyValue.replace("IsUtour=", ""));
            }

            if (aKeyValue.contains("IsSSL")) {
                isSSL = Integer.valueOf(aKeyValue.replace("IsSSL=", ""));
            }
        }
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
            if (url.contains("file")) {
                //如果传过来的是file文件,那么判断本地有没有那个文件, 如果有,那么直接读取本地,如果没有,那么读取线上
                String filePath = url;
                Log.i("hww", "file:filepath" + filePath);
                if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                    try {//读取本地的子页面
                        byte[] temp = FileUtils.getBytes(filePath);
                        String result = new String(temp, "utf-8");
                        try {
                            dealWithReturnData(netAvailable, viewName, result);
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
                            dealWithReturnData(netAvailable, viewName, data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                        //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                    }
                }
            } else if (url.contains("http")) {
                //读取本地。判断本地有没有子页面，如果有就读取本地并返回，如果没有就读取线上并返回
                String temp = loadHttpSubPageUrl(url);
                try {
                    dealWithReturnData(netAvailable, viewName, temp);
                } catch (Exception e) {
                    e.printStackTrace();
                    DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
                    //callSubPageBackErrorData(what,-4, "请求服务器失败，请重试");
                }
            }
        } else {//无网
            String temp = loadHttpSubPageUrl2(url);
            try {
                dealWithReturnData(netAvailable, viewName, temp);
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
                            String local_path = null;
                            if (p.contains("https://")) {
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("https://", "");
                            } else {
                                local_path = Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "");
                            }
                            if (FileUtils.isFileExists(local_path)) {//加载本地文件
                                LogUtil.i("hww", "本地");
                                haveLoadPage = true;
//                                refreshPage("file://" + Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://", "") + params);
                                byte[] temp = FileUtils.getBytes(local_path + params);// /storage/emulated/0/demo/m.demo.com/product/hybrid/
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
    private void dealWithReturnData(boolean netAvailable, String viewName, String data) throws Exception {
        if (!TextUtils.isEmpty(data)) {
            JSONObject jsonData = new JSONObject();
            jsonData.put("ErrorCode", 200);
            jsonData.put("ErrorMsg", "");
            // 需要对特殊字符进行转义
            data = data.replace("\r\n", "");//替换掉换行符
            String formatHtml = Uri.encode(data, "UTF-8").replace("\'", "&acute;").replace("\"", "&quot;");//替换单引号和双引号
            jsonData.put("JsonResult", formatHtml);
//            Log.i("hww", "hww:" + data);
//            callSubPageCallback(viewName, jsonData.toString());//将数据结果等回调给js
            callSubPageCallback(viewName, formatHtml);//将数据结果等回调给js
        } else {
            /*
            *
            * @time 2017-03-06
            * @author jiangyingjun
            * @descrition js 报错后  自己负责提示toast
            *
            * */
            callSubPageCallback(viewName, "");
            if (netAvailable) {
                DialogUtil.toastForShort(this, getResources().getString(R.string.no_http_response));
            } else {
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
    private void callSubPageBackErrorData(int what, int errorCode, String errorMsg) {
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
            new RxPermissions(IndexWebActivityNew.this)
                    .request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)//打电话的权限
                    .subscribe(granted -> {
                        if (granted) {
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
                            AddCallRecordLoader addCallRecordLoader = new AddCallRecordLoader();

                            try {
                                addCallRecordLoader.addCallRecord(IndexWebActivityNew.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            DialogUtil.toastForShort(context, "请同意我们的权限，才能提供服务");
                        }
                    });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == IKeySourceUtil.INTENT_REQUEST) {
                    if (hasThirdLoginUrl) {
                        hasThirdLoginUrl = false;
                        addUserAgent();
                        url = CookieUtil.getWebviewUrl(planeTicketUrl, context,
                                comeFrom);
                    }
                    loadWebUrl(webView, url);
                    isShowHome = true;
                }else if (requestCode == REQUEST_CONTACT) {
                    if (data == null) {
                        return;
                    }
                    Uri result = data.getData();
                    Cursor c=null;
                    String moblie = "";
                    String username = "";
                    try {
                        c= getContentResolver().query(result, null, null, null, null);
                        if (c!=null){
                            c.moveToFirst();
                            String phoneInfo = PayPublicUtil.getInstance().getContactPhone(c, this);
                            if (!TextUtils.isEmpty(phoneInfo)) {
                                username = phoneInfo.split("&")[0];
                                moblie = phoneInfo.split("&")[1];
                            }
                        }
                    }catch (Exception e){

                    }finally {
                        if (c!=null){
                            c.close();
                        }
                    }

                    webView.loadUrl("javascript:setPhoneList(\"" + sid + "\",\"" + moblie + "\",\"" + username + "\")");
                    isShowHome = true;
                } else if (requestCode == 2) {
                    String payTypeStr = data.getStringExtra("PayTypeStr");
                    if ("bt_upay".equals(payTypeStr)) { //U币支付
                        PayPublicUtil.getInstance().payForUCurrency(this, orderID, prepayMent, false);
                    } else if ("bt_union_pay".equals(payTypeStr)) { // 银联支付
                        if (orderPayConfigYinlianEncrypt != null && orderPayConfigYinlianEncrypt.getData() != null && orderPayConfigYinlianEncrypt.getData().length() > 0) {
                            doStartUnionPayPlugin(this, orderPayConfigYinlianEncrypt.getData(), IKeySourceUtil.mMode);
                        } else {
                            payType = 3;
                            /*************************************************
                             * 步骤1：从网络开始,获取交易流水号即TN
                             ************************************************/
                            asynLoadYinlianPayData(isUtour, isSSL, false, Long.valueOf(orderID));
                        }
                    } else if ("bt_alipay".equals(payTypeStr)) { //支付宝支付
                        if (orderPayConfigZhifubaoEncrypt != null && !TextUtils.isEmpty(orderPayConfigZhifubaoEncrypt.getData())) {
                            alixPay = PayPublicUtil.getInstance().payForAlipay(context, orderPayConfigZhifubaoEncrypt.getData().replace("\\", ""), mHandler);
                        } else {
                            payType = 2;
                            /*************************************************
                             * 步骤1：从网络开始,获取交易流水号即TN
                             ************************************************/
                            asynLoadYinlianPayData(isUtour, isSSL, false, Long.valueOf(orderID));
                        }
                    } else if ("bt_credit_card_pay".equals(payTypeStr)) { //信用卡快捷支付
                        PayPublicUtil.getInstance().orderPayForWeb(this, orderID, false);
                    } else if ("bt_ccb_pay".equals(payTypeStr)) { //建行卡支付
                        PayPublicUtil.getInstance().orderCCBPayForWeb(this, orderID, false);
                    } else if ("bt_cashpay".equals(payTypeStr)) { //现金账户支付
                        PayPublicUtil.getInstance().orderCashPayForWeb(this, orderID, false);
                    } else if ("bt_weixinpay".equals(payTypeStr)) {//微信支付
                        if (orderWeiXinConfigEncrypt != null) {
                            PayPublicUtil.getInstance().payForWeiXinPay(this,
                                    productName, prepayMent, orderID, "", orderCode,
                                    mHandler, false, orderWeiXinConfigEncrypt);
                        } else {
                            payType = 1;
                            /*************************************************
                             * 步骤1：从网络开始,获取交易流水号即TN
                             ************************************************/
                            asynLoadYinlianPayData(isUtour, isSSL, false, Long.valueOf(orderID));
                        }
                    } else if ("bt_zhaohangpay".equals(payTypeStr)) {// 招商银行支付
                        PayPublicUtil.getInstance().payForZhaoshangPay(this, isUtour, false, Long.valueOf(orderID), 5);
                    } else if ("bt_zhonghangpay".equals(payTypeStr)) {// 中行快捷支付
                        PayPublicUtil.getInstance().payForZhaoshangPay(this, isUtour, false, Long.valueOf(orderID), 6);
                    }
                } else if (requestCode == 4 || requestCode == 1 || requestCode == 5) {
                    mBaseApplicate.payFlag = true;
                    OrderListNewActivity.orderListRefresh = true;
                    startActivity(new Intent(context, OrderDetailNewActivity.class));
                    this.finish();
                } else if (requestCode == 10) {
                    mBaseApplicate.payFlag = false;
                    /*************************************************
                     * 步骤3：处理银联手机支付控件返回的支付结果
                     ************************************************/
                    String msg = "";
                    if (data == null) {
                        msg = "支付失败！";
                    } else {
                    /*
                     * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
			         */
                        String str = data.getExtras().getString("pay_result");
                        if (!TextUtils.isEmpty(str)) {
                            if (str.equalsIgnoreCase("success")) {
                                msg = "支付成功！";
                                mBaseApplicate.payFlag = true;
                            } else if (str.equalsIgnoreCase("fail")) {
                                msg = "支付失败！";
                            } else if (str.equalsIgnoreCase("cancel")) {
                                msg = "用户取消了支付";
                            }
                        }
                    }
                    BaseHelper.showDialog(IndexWebActivityNew.this,
                            "提示", msg, R.drawable.infoicon,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    if (isPlaneTicket) {
                                        addUserAgent();
                                        url = CookieUtil.getWebviewUrl(IKeySourceUtil.MY_PLANE_TICKET_URL, context, comeFrom);
                                        loadWebUrl(webView, url);
                                        isShowHome = true;
                                    } else {
                                        Intent intent = new Intent(context, OrderListNewActivity.class);
                                        intent.putExtra("orderListType", 0);
                                        intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, currentGAPath + "->" + getResources().getString(R.string.ga_my_demo_qbdd));
                                        intent.putExtra("TravelType", 100);
                                        startActivity(intent);
                                        IndexWebActivityNew.this.finish();
                                    }
                                }
                            });
                }
                break;
            case 20://招行、中行支付失败或支付取消的页面跳转
                mBaseApplicate.payFlag = false;
                Intent intent = new Intent(
                        context,
                        OrderListNewActivity.class);
                intent.putExtra("orderListType", 0);
                intent.putExtra(
                        "TravelType", 100);
                startActivity(intent);
                this.finish();
                break;
        }
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

    /*************************************************
     * 步骤2：通过银联工具类启动支付插件
     ************************************************/
    public void doStartUnionPayPlugin(Activity activity, String tn, String mode) {
        //activity  ——用于启动支付控件的活动对象
        //spId  ——保留使用，这里输入null
        //sysProvider ——保留使用，这里输入null
        //orderInfo   ——订单信息为交易流水号，即TN。
        //mode   —— 银联后台环境标识，“00”将在银联正式环境发起交易,“01”将在银联测试环境发起交易
        UPPayAssistEx.startPayByJAR(activity, PayActivity.class, null, null,
                tn, mode);
    }

    /**
     * 根据支付方式返回银联支付请求参数
     *
     * @param isUtour   ：1 启用众信账户，0 悠哉账户
     * @param isSSL     ：是否需要认证的标识，当值为1时，请求接口地址 http://pay.demo.com/ GetPayConfig
     *                  改为https://pay.demo.com/ GetPayConfig
     * @param hostOrder ：true主订单，false子订单
     * @param orderId   ：订单ID
     */
    private void asynLoadYinlianPayData(int isUtour, int isSSL, boolean hostOrder, long orderId) {
        // 判断有无网络，有网，发送请求；否则，从数据库读取
        if (NetworkManageUtil.isWiFiActive(this)
                || NetworkManageUtil.isNetworkAvailable(this)) {
            handler.sendEmptyMessage(IKeySourceUtil.LOADING);
            PayConfigYinlianRequest request = new PayConfigYinlianRequest();
            CommonRequestField commonRequestField = CommReqFieldValuePackag
                    .getCommReqField(context);
            request.setClientSource(commonRequestField.getClientSource());
            request.setPhoneID(commonRequestField.getPhoneID());
            request.setPhoneType(commonRequestField.getPhoneType());
            request.setPhoneVersion(commonRequestField.getPhoneVersion());
            request.setStartCity(commonRequestField.getStartCity());
            request.setPayType(payType);//3为银联,1为微信
            request.setIsUtour((isUtour != 0));

            //获取userid
            SharedPreferences settings = context.getSharedPreferences(IKeySourceUtil.LOGIN_STATUS, Context.MODE_PRIVATE);
            String userid = settings.getString("demoId", "0");
            request.setUserId(userid + "");

            // 时间格式: yyyyMMddHHmmss,从年精确到秒
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String timestamp = formatter.format(curDate);
            String outOrderCode;
            if (hostOrder) {
                outOrderCode = "02" + orderId;

            } else {
                outOrderCode = "01" + orderId;
            }
            request.setOutOrderCode(outOrderCode);
            // 加密（Timestamp+ ClientSource+ PhoneID+ PhoneIdfa+ PhoneType+
            // PhoneVersion+ StartCity+ PayType + IsUtour）
            String token = timestamp + commonRequestField.getClientSource()
                    + commonRequestField.getPhoneID() + ""
                    + commonRequestField.getPhoneType()
                    + commonRequestField.getPhoneVersion()
                    + commonRequestField.getStartCity() + payType
                    + ((isUtour != 0)) + userid + outOrderCode;
            request.setTimestamp(timestamp);

            Message msg = new Message();
            try {
                request.setToken(DESUtil.des3EncodeCBC(token.getBytes("UTF-8"),
                        IKeySourceUtil.TOKEN_PAYCONFIG_KEY));
                String jsonStr = new Gson().toJson(request);// 序列化

                Plugin.getPayConfigIsNoSSLHttp(context, (isSSL == 1))
                        .getAppPayData(eventYinlian,
                                DESUtil.des3EncodeCBC(jsonStr.getBytes("UTF-8"),
                                        IKeySourceUtil.TOKEN_PAYCONFIG_KEY));
            } catch (Exception e) {
                handler.sendEmptyMessage(1);
                msg.obj = e;
                msg.what = IKeySourceUtil.EXCEPTION;
                handler.sendMessage(msg);
            }
        } else {
            DialogUtil.toastForShort(this, getResources().getString(R.string.check_net));
        }

    }

    IDataEvent<String> eventYinlian = new IDataEvent<String>() {

        @Override
        public void onProcessFinish(int arg0, String result) {
            try {
                handler.sendEmptyMessage(1);
                if (!TextUtils.isEmpty(result)) {
                    String data = DESUtil.desPayDecodeCBC(result);
                    LogUtil.i(this, "RECEIVE JSONSting =>>" + data);
                    switch (payType) {//1为微信，2为支付宝，3为银联
                        case 2://支付宝
                            orderPayConfigZhifubaoEncrypt = JSON
                                    .parseObject(data, OrderPayConfigZhifubaoReceive.class);
                            if (orderPayConfigZhifubaoEncrypt != null && orderPayConfigZhifubaoEncrypt.getRespCode().equals("0000")) {
                                if (!TextUtils.isEmpty(orderPayConfigZhifubaoEncrypt.getData())) {
                                    alixPay = PayPublicUtil.getInstance().payForAlipay(context, orderPayConfigZhifubaoEncrypt.getData().replace("\\", ""), mHandler);
                                } else {
                                    orderPayConfigZhifubaoEncrypt = null;
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                                }
                            } else {
                                if (orderPayConfigZhifubaoEncrypt != null) {
                                    String message = "" + orderPayConfigZhifubaoEncrypt.getMessage();
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, message);
                                } else {
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                                }
                                orderPayConfigZhifubaoEncrypt = null;
                            }
                            break;
                        case 1://微信
                        case 3://银联
                            orderPayConfigYinlianEncrypt = JSON
                                    .parseObject(data, OrderPayConfigYinlianReceive.class);
                            if (orderPayConfigYinlianEncrypt != null
                                    && orderPayConfigYinlianEncrypt.getRespCode().equals(
                                    "0000")) {
                                if (!TextUtils.isEmpty(orderPayConfigYinlianEncrypt.getData())) {
                                    switch (payType) {//1为微信，2为支付宝，3为银联
                                        case 1://微信
                                            orderWeiXinConfigEncrypt = JSON
                                                    .parseObject(orderPayConfigYinlianEncrypt.getData().replace("\\", ""), OrderWeiXinConfigInfoNewDTO.class);
                                            if (orderWeiXinConfigEncrypt != null) {
                                                PayPublicUtil.getInstance().payForWeiXinPay(context,
                                                        productName, prepayMent, orderID, "", orderCode,
                                                        mHandler, false, orderWeiXinConfigEncrypt);
                                            } else {
                                                orderWeiXinConfigEncrypt = null;
                                                DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                                            }
                                            break;
                                        case 3://银联
                                            doStartUnionPayPlugin(IndexWebActivityNew.this, orderPayConfigYinlianEncrypt.getData(), IKeySourceUtil.mMode);
                                            break;
                                    }
                                } else {
                                    orderPayConfigYinlianEncrypt = null;
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                                }
                            } else {
                                if (orderPayConfigYinlianEncrypt != null) {
                                    String message = "" + orderPayConfigYinlianEncrypt.getMessage();
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, message);
                                } else {
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                                }
                                orderPayConfigYinlianEncrypt = null;
                            }
                            break;
                    }
                } else {
                    switch (payType) {//1为微信，2为支付宝，3为银联
                        case 2://支付宝
                            orderPayConfigZhifubaoEncrypt = null;
                            DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                            break;
                        case 1://微信
                        case 3://银联
                            orderPayConfigYinlianEncrypt = null;
                            DialogUtil.toastForShort(IndexWebActivityNew.this, "获取支付信息失败，请重试！");
                            break;
                    }
                }
            } catch (Exception ex) {
                handler.sendEmptyMessage(1);
                Message msg = new Message();
                msg.obj = ex;
                msg.what = IKeySourceUtil.EXCEPTION;
                handler.sendMessage(msg);
                LogUtil.i(context, ex.toString());
            }
        }
    };
    // 这里接收支付结果，支付宝手机端同步通知
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                String strRet = (String) msg.obj;

                LogUtil.i(this, strRet); // strRet范例：resultStatus={9000};memo={};result={partner="2088201564809153"&seller="2088201564809153"&out_trade_no="050917083121576"&subject="123456"&body="2010新款NIKE 耐克902第三代板鞋 耐克男女鞋 386201 白红"&total_fee="0.01"&notify_url="http://notify.java.jpxx.org/index.jsp"&success="true"&sign_type="RSA"&sign="d9pdkfy75G997NiPS1yZoYNCmtRbdOP0usZIMmKCCMVqbSG1P44ohvqMYRztrB6ErgEecIiPj9UldV5nSy9CrBVjV54rBGoT6VSUF/ufjJeCSuL510JwaRpHtRPeURS1LXnSrbwtdkDOktXubQKnIMg2W0PreT1mRXDSaeEECzc="}
                switch (msg.what) {
                    case AlixPay.SDK_PAY_FLAG: {
                        // 处理交易结果
                        try {
                            // 获取交易状态码，具体状态代码请参看文档
                            PayResult payResult = new PayResult((String) msg.obj);
                            String tradeStatus = payResult.getResultStatus();

//                            // 先验签通知
//                            ResultChecker resultChecker = new ResultChecker(strRet);
//                            int retVal = resultChecker.checkSign(orderPayConfigEncrypt.getConfigs().getZhiFuBaoConfigInfo());
//                            // 验签失败
//                            if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) {
//                                BaseHelper.showDialog(
//                                        ActivityWebActivity.this,
//                                        "提示",
//                                        getResources().getString(
//                                                R.string.check_sign_failed),
//                                        android.R.drawable.ic_dialog_alert, null);
//                            } else {// 验签成功。验签成功后再判断交易状态码
                            LogUtil.i(context, "交易状态码：" + tradeStatus);
                            if (tradeStatus.equals("9000")) {// 判断交易状态码，只有9000表示交易成功
                                BaseHelper.showDialog(IndexWebActivityNew.this,
                                        "提示", "支付成功。", R.drawable.infoicon,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                mBaseApplicate.payFlag = true;
                                                if (isPlaneTicket) {
                                                    addUserAgent();
                                                    url = CookieUtil
                                                            .getWebviewUrl(
                                                                    IKeySourceUtil.MY_PLANE_TICKET_URL,
                                                                    context,
                                                                    comeFrom);
                                                    loadWebUrl(webView, url);
                                                    isShowHome = true;
                                                } else {
                                                    Intent intent = new Intent(
                                                            context,
                                                            OrderListNewActivity.class);
                                                    intent.putExtra("orderListType", 0);
                                                    intent.putExtra(
                                                            IKeySourceUtil.GA_FROM_FLAG,
                                                            currentGAPath
                                                                    + "->"
                                                                    + getResources()
                                                                    .getString(
                                                                            R.string.ga_my_demo_qbdd));
                                                    intent.putExtra(
                                                            "TravelType", 100);
                                                    startActivity(intent);
                                                    IndexWebActivityNew.this
                                                            .finish();
                                                }
                                            }
                                        });

                            } else {
                                // 判断resultStatus 为非“9000”则代表可能支付失败
                                // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                                if (TextUtils.equals(tradeStatus, "8000")) {
                                    DialogUtil.toastForShort(IndexWebActivityNew.this, "支付结果确认中");
                                } else {
                                    // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                    BaseHelper.showDialog(IndexWebActivityNew.this,
                                            "提示", "支付失败。", R.drawable.infoicon,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    mBaseApplicate.payFlag = false;
                                                    if (isPlaneTicket) {
                                                        addUserAgent();
                                                        url = CookieUtil
                                                                .getWebviewUrl(
                                                                        IKeySourceUtil.MY_PLANE_TICKET_URL,
                                                                        context,
                                                                        comeFrom);
                                                        loadWebUrl(webView, url);
                                                        isShowHome = true;
                                                    } else {
                                                        Intent intent = new Intent(
                                                                context,
                                                                OrderListNewActivity.class);
                                                        intent.putExtra("orderListType", 0);
                                                        intent.putExtra(
                                                                IKeySourceUtil.GA_FROM_FLAG,
                                                                currentGAPath
                                                                        + "->"
                                                                        + getResources()
                                                                        .getString(
                                                                                R.string.ga_my_demo_qbdd));
                                                        intent.putExtra(
                                                                "TravelType", 100);
                                                        startActivity(intent);
                                                        IndexWebActivityNew.this
                                                                .finish();
                                                    }

                                                }
                                            });
                                }
                            }
//                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            BaseHelper.showDialog(IndexWebActivityNew.this, "提示",
                                    strRet, R.drawable.infoicon, null);
                        }
                    }
                    break;
                }

                super.handleMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void loadWebUrl(final WebView view, final String url) {
        if (NetworkManageUtil.checkNetworkAvailable(context)) {
            new Thread() {
                public void run() {
                    LogUtil.i(this, "Load Url==>>>" + url);
                    targetWebView = view;

                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = url;
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            DialogUtil.toastForLong(context,
                    getString(R.string.tip_network_invalidate));
        }

    }
    protected void judgeAndLoadUrl(String url) {
//        https://magic.demo.com/orderlist.html?devicetype=android&hybridversion=3&appversion=5.5.1&source=android
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

        } else if (urlLowCase.contains("http:") || urlLowCase.contains("https:")) {//判断网络路径
            String newUrl = urlLowCase;
            if (urlLowCase.contains("http://mdingzhi.demo.com/?") || urlLowCase.contains("https://mdingzhi.demo.com?") || urlLowCase.contains("http://mdingzhi.demo.com?") || urlLowCase.contains("https://mdingzhi.demo.com/?")) {
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
            String filePath = "";
            if (newUrl.contains("mdingzhi")) {
                filePath = newUrl.substring(newUrl.indexOf("mdingzhi"), newUrl.indexOf("?"));
                filePath = Environment.getExternalStorageDirectory() + File.separator + "demo/" + filePath;
            }

            if (FileUtils.isFileExists(filePath)) {//判断本地有无所请求的文件
                refreshPage("file://" + filePath + "?" + newUrl.split("\\?")[1]);
            } else {
                refreshPage(urlLowCase);
            }
        } else if (urlLowCase.contains("http:") || urlLowCase.contains("https:")) {
            refreshPage(urlLowCase);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        hybridCacheDao.close();
        if (baseBandDialog != null) {
            baseBandDialog.dismiss();
            baseBandDialog = null;
        }
    }


    /**
     * 重写物理返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isShowHome = false;
            if (orderWebView.canGoBack()) {
                orderWebView.goBack();
                if (titles.size() > 0) {
                    titles.remove(titles.size() - 1);
                    setWebTitle();
                }
            } else {
                demoProUtil.firstMlinkGotoHome(context);
                finish();
            }
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
        if (isShowHome) {
            rightHomeBtn.setVisibility(View.VISIBLE);
        } else {
            rightHomeBtn.setVisibility(View.GONE);
        }
        isShowHome = false;
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
            Activity activity = dialog.getOwnerActivity();
            if (activity==null||activity.isDestroyed()||activity.isFinishing()){
                return;
            }

            dialog.dismiss();
        }
    }


    /**
     * 处理收藏按钮操作
     *
     * @param operate    操作：1；收藏 2；取消收藏
     * @param userId
     * @param topicId    活动id
     * @param favoriteId 收藏id
     */

    private void dealWithCollectButton(int operate, String userId, String topicId, String favoriteId) {
        CommonRequestField commonRequestField = CommReqFieldValuePackag.getCommReqField(context);

        com.alibaba.fastjson.JSONObject request = new com.alibaba.fastjson.JSONObject();
        request.put("clientSource", commonRequestField.getClientSource());
        request.put("phoneID", commonRequestField.getPhoneID());
        request.put("phoneType", commonRequestField.getPhoneType());
        request.put("phoneVersion", commonRequestField.getPhoneVersion());
        request.put("startCity", commonRequestField.getStartCity());
        request.put("Path", "http://msitelogic.demo.com/api/");
        request.put("ControllerName", "MyFavorite");
        if (operate == 1) {//收藏
            request.put("ActionName", "Create");
            com.alibaba.fastjson.JSONObject postData = new com.alibaba.fastjson.JSONObject();
            postData.put("UserId", userId);
            postData.put("ProductId", topicId);
            request.put("PostData", postData);
        } else {//取消收藏
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
                LogUtil.i("hww", "error=" + ex.getMessage().toString());
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
                    LogUtil.i("hww", "error" + e.getMessage().toString());
                }
            } else {
                DialogUtil.toastForShort(context, getResources().getString(R.string.network_exception));
            }
        }
    };

//    //跳转新详情页判断
//    IDataEvent<String> detailType = new IDataEvent<String>() {
//        @Override
//        public void onProcessFinish(int i, String result) {
//            Intent intent = new Intent();
//            cancelDialog();
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    CommonReceiveDTO commonReceiveDTO = JSON.parseObject(result, CommonReceiveDTO.class);
//                    if (commonReceiveDTO.getMC() == 1000 && commonReceiveDTO.getContent().length() > 0) {
//                        String data = DESUtil.des3DecodeCBC(commonReceiveDTO.getContent());
//                        LogUtil.i("ldq", "IndexWebActivityNew--产品详情：" + data.toString());
//                        CommonReceiver detailTypeResult = JSON.parseObject(data, CommonReceiver.class);
//                        if (detailTypeResult.getErrorCode() == 200) {
//                            DetailTypeReceive jsonResult = JSON.parseObject(detailTypeResult.getJsonResult(), DetailTypeReceive.class);
//                            LogUtil.i("ldq", "IndexWebActivityNew--type详情：" + jsonResult.toString());
//                            preferencesUtils.putString(String.valueOf(productID), jsonResult.getJumpType());
//                            if (jsonResult.getJumpType().contains("10")) {
//                                intent = new Intent(IndexWebActivityNew.this, ProductDetail548Activity.class);
//                                intent.putExtra("url", IKeySourceUtil.WEB_GENTUAN_PRODUCT_DETAIL + productID);
//                                startActivity(intent);
//                            } else if (jsonResult.getJumpType().contains("0")) {
//                                intent = new Intent(IndexWebActivityNew.this, ProductDetailUi540.class);
//                                intent.putExtra("demoTravelClass", classID);
//                                intent.putExtra("ProductID", productID);
//                                startActivity(intent);
//                            }
//                            isIntent = true;
//                            return;
//                        }
//                    }
//                } catch (Exception e) {
////                    DialogUtil.toastForShort(IndexWebActivityNew.this, "暂无数据！");
//                }
//            } else {
////                DialogUtil.toastForShort(IndexWebActivityNew.this, getResources().getString(R.string.network_exception));
//            }
//            intent = new Intent(IndexWebActivityNew.this, ProductDetailUi540.class);
//            intent.putExtra("demoTravelClass", classID);
//            intent.putExtra("ProductID", productID);
//            startActivity(intent);
//            isIntent = true;
//        }
//    };

    /*--------合并代码---------*/
    /**
     * 标识我的消息,标识已经阅读
     */
    private void markCollectInfo(int message_id) {
        if (NetworkManageUtil.isNetworkAvailable(context)
                || NetworkManageUtil.isWiFiActive(context)) {
            SharedPreferences settings = context.getSharedPreferences(
                    IKeySourceUtil.LOGIN_STATUS, Context.MODE_PRIVATE);
            CommonRequestField commonRequestField = CommReqFieldValuePackag
                    .getCommReqField(context);
            MarkMessageRequest request = new MarkMessageRequest();
            request.setClientSource(commonRequestField.getClientSource());
            request.setPhoneID(commonRequestField.getPhoneID());
            request.setPhoneType(commonRequestField.getPhoneType());
            request.setPhoneVersion(commonRequestField.getPhoneVersion());
            request.setStartCity(commonRequestField.getStartCity());
            request.setUserID(settings.getString("demoId", "0"));
            request.setPhoneToken(Const.phoneToken);
            request.setMessageId(message_id);
            request.setMessageState(1);
            request.setType(0);
            try {
                String jsonStr = new Gson().toJson(request);
                Plugin.getHttp(context).markMessage(
                        eventMark,
                        DESUtil.des3EncodeCBC(jsonStr.getBytes("UTF-8"),
                                IKeySourceUtil.PASSWORD_CRYPT_KEY));
            } catch (Exception e) {
            }
        }
    }

    IDataEvent<String> eventMark = new IDataEvent<String>() {

        @Override
        public void onProcessFinish(int arg0, String result) {

            if (!TextUtils.isEmpty(result)) {
                try {
                    CommonReceiveDTO startCityEncrypt = JSON.parseObject(
                            result, CommonReceiveDTO.class);
                    if (startCityEncrypt != null
                            && startCityEncrypt.getMC() == 1000
                            && startCityEncrypt.getContent().length() > 0) {
                    } else {
                        DialogUtil.toastForShort(context, context.getResources().getString(R.string.network_exception));
                    }
                } catch (Exception ex) {
                    LogUtil.i(context, ex.toString());
                }
            } else {
                DialogUtil.toastForShort(context, context.getResources().getString(R.string.network_exception));
            }
        }

    };
}

