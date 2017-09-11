package com.demo.app.mvp.module.hybrid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.demo.app.R;
import com.demo.app.hybrid.core.Config;
import com.demo.app.hybrid.core.Cookie;
import com.demo.app.hybrid.core.ExceptionJson;
import com.demo.app.hybrid.core.SyncVersion;
import com.demo.app.mvp.app.MvpBaseActivity;
import com.demo.app.mvp.model.network.https2HttpUtils;
import com.demo.app.util.CatchExceptionUtils;
import com.demo.app.util.DialogUtil;
import com.demo.app.util.FileUtils;
import com.demo.app.util.IKeySourceUtil;
import com.demo.app.util.LogUtil;
import com.demo.app.util.PhoneInfoUtil;
import com.demo.app.util.demoProUtil;
import com.demo.app.util.WebUtils;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.demo.app.util.IKeySourceUtil.CLOSE_WEB_LOADING;


public abstract class BaseWebActivity<Activity> extends MvpBaseActivity implements Serializable {
    protected ExceptionJson exceptionJson = new ExceptionJson();
    private Cookie cookie;

    protected String locationProvince;
    protected String locationCity;
    protected String startCity;
    protected String appVersion;
    protected String userid;

    protected WebView webView,targetWebView;
    protected RelativeLayout layout_no_data;
    protected LinearLayout top_bar;
    protected ImageView left_btn;
    /**
     * 原生导航栏标题
     */
    protected TextView middleTitle;
    /**
     * 分享的按钮
     */
    protected ImageView rightShareButton;

    /**
     * 顶部返回主页面的按钮
     */
    public ImageView rightBtn;

    //加载dialog
    protected Dialog dialog;
    /**
     * 如果活动的url链接里包含/subject则不显示原生的加载动画，加载动画h5页面自己写
     */
    protected boolean isShowLoading = true;
    /**
     * 访问接口失败
     */
    private AlertDialog builder;
    /**
     * 标题集合
     */
    protected List<String> titles = new ArrayList<String>();

    private String host = Config.SERVICEHOST;
    private String content = "";

    //重载init方法
    public void init(String path, String directory, String versionFile, String versionHost) {

        SyncVersion syncVersion = new SyncVersion(BaseWebActivity.this);
        CountDownLatch latch = new CountDownLatch(1);

        File file = new File(path +File.separator+ directory);
        //如果文件不存在
        if (!file.exists()) {
            //得到assets目录下的版本文件内容（是一个Json串，json串内是版本和一系列需要下载的文件等）
            String versionTxt = syncVersion.getDefaultVersionFile(directory, BaseWebActivity.this);
            //保存到sp中一段cookievalue实体的json字符串
            new Cookie(BaseWebActivity.this).setCookie(versionHost + versionFile, versionTxt, 0);
            //asset目录下创建目录
            FileUtils.copyAssetsPath(directory, path + File.separator + directory, BaseWebActivity.this);
        }
        //下载版本文件和版本文件中对应的file、修改file中需要替换的字符串、保存到assets指定目录下
        download(path, directory, syncVersion, versionFile, versionHost, latch);

    }

    private void download(String path, String directory,
                          SyncVersion syncVersion, String versionFile, String versionHost,
                          CountDownLatch latch) {

        syncVersion.setStorePath(path + File.separator + directory);//下载的东西的存储路径 是在sd卡中
        syncVersion.getVersionList(latch, versionFile, versionHost);//实际在这里下载
    }

    @Override
    protected void onCreate(Bundle savedInstanceState, String from, String parameter) {
        super.onCreate(savedInstanceState, getIntent().getStringExtra(IKeySourceUtil.GA_FROM_FLAG), parameter);

        //加载资源文件
        int resLayoutId = getResLayoutId();
        if (resLayoutId == -1) {//用默认的layout
            setContentView(R.layout.activity_hybrid_web);
            initView();
        } else {
            setContentView(resLayoutId);
        }
    }

    private void initView() {
//        //动态添加webview，防止webview内存泄露
        RelativeLayout layout_content= (RelativeLayout) findViewById(R.id.layout_content);
        webView=new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout_content.addView(webView);
//        webView = (WebView) findViewById(R.id.webView);

        layout_no_data = (RelativeLayout) findViewById(R.id.layout_no_data);
        top_bar = (LinearLayout) findViewById(R.id.top_bar);
        left_btn = (ImageView) findViewById(R.id.left_btn);
        middleTitle = (TextView) findViewById(R.id.middleTitle);
        rightBtn = (ImageView) findViewById(R.id.right_home_btn);
        rightShareButton = (ImageView) findViewById(R.id.right_share_btn);

        //设置webview相关设置
        initWebView(webView);
        //设置webview相关事件
        initWebEvent(webView);
    }

    public void initWebView(WebView webView) {
        webView.setVerticalScrollBarEnabled(false);
        webView.setBackgroundColor(getResources().getColor(R.color.white));
        //设置webview的配置
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        WebSettings webseting = webView.getSettings();
        // 支持JavaScript
        webseting.setJavaScriptEnabled(true);
        // 支持保存数据
        webseting.setSaveFormData(false);
        webseting.setDomStorageEnabled(true);
//		webseting.setAppCacheMaxSize(1024 * 1024 * 8);// 设置缓冲大小，我设的是8M
        String appCacheDir = getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webseting.setAppCachePath(appCacheDir);
        webseting.setUserAgentString(Config.FORMAT_USER_AGENT);
        webseting.setLoadWithOverviewMode(true);
        webseting.setUseWideViewPort(true);
        webseting.setDatabaseEnabled(true);
        webseting.setAllowFileAccess(true);
        webseting.setCacheMode(WebSettings.LOAD_DEFAULT);
        /*强制webview使用标准字号大小*/
        webseting.setTextZoom(100);

        // webseting.setBlockNetworkImage(true);
        webseting.setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);// Enables debugging of web contents (HTML / CSS / JavaScript) loaded into any WebViews of this application.
        }

        //移除webkit内部存在安全漏洞的javascript接口
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");

    }

    protected void initWebEvent(WebView webView) {
        //帮助webview处理各种通知、请求事件
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                url = formatUrl(url);
                super.onPageStarted(view, url, favicon);
                pageStartedOverride(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                url = formatUrl(url);
                pageFinishedOverride(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                /**
                 * @author jiangyinigjun
                 * @date 2016-10-10
                 * @description https请求无资源时转http请求
                 *
                 * **/
                if (failingUrl.contains(https2HttpUtils.HTTPS)){
                    view.loadUrl(https2HttpUtils.Https2Http(failingUrl));
                    return;
                }
                receivedErrorOverride(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                view.getSettings().setJavaScriptEnabled(true);
                shouldOverrideKeyEventOverride(view, event);
                return super.shouldOverrideKeyEvent(view, event);
            }

            //处理用户点击链接触发的操作
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                url = formatUrl(url);
                view.getSettings().setJavaScriptEnabled(true);
                boolean blnGo = shouldOverrideUrlLoadingOverride(view, url);
                if (blnGo)
                    return true;
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                handler.proceed();

            }
        });

        //辅助webview处理javascript的对话框、网站图标、网站title、加载进度等
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                JsAlert(view, url, message, result);
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                JsComfirm(view, url, message, result);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    handler.sendEmptyMessage(1);// 如果全部载入,隐藏进度对话框
                }
                super.onProgressChanged(view, newProgress);
            }


            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (middleTitle!=null){
                    if (TextUtils.isEmpty(middleTitle.getText().toString())) {

                        String titleMessage = title.trim().split("_")[0];
                        if (titleMessage.contains("合同模板展示页")) {
                            titleMessage = "旅游合同";
                        }
                        if (!TextUtils.isEmpty(titleMessage)) {
                            if (titleMessage.length() > 13) {
                                titleMessage = titleMessage.substring(0, 13) + "...";
                            }
                        } else {
                            titleMessage = "空标题";
                        }
                        titles.add(titleMessage);
                        setWebTitle();
                        setPageGA(titles,getIntent().getStringExtra(IKeySourceUtil.GA_FROM_FLAG));
                    }
                }
            }

        });
    }

    /**
     * 设置web标题
     */
    protected void setWebTitle() {
        if (titles.size() > 0) {
            String tit = titles.get(titles.size() - 1);
            if (tit.equals("空标题")) {
                tit = "";
            }
            middleTitle.setText(tit);
        } else {
            middleTitle.setText("");
        }
    }

    /**
     * 添加页面GA
     * @param titles
     * @param strGAFrom
     */
    private void setPageGA(List<String> titles,String strGAFrom) {
        StringBuilder gaStr = new StringBuilder();
        for (int i = 0; i < titles.size(); i++) {
            if (i > 0) {
                if (i != 1) {
                    gaStr.append("->");
                }
                gaStr.append(titles.get(i));
            }
        }
        if (!TextUtils.isEmpty(gaStr.toString())) {
            gaForProject(getIntent().getStringExtra(IKeySourceUtil.GA_FROM_FLAG), gaStr.toString());
        }
    }

    protected String pageStartedOverride(WebView view, String url, Bitmap favicon) {
        pageStartInit(url);
        return url;
    }

    protected  void pageStartInit(String url){

    }

    protected void pageFinishedOverride(WebView view, String url) {

    }

    protected void receivedErrorOverride(WebView view, int errorCode, String description, String failingUrl) {

    }

    protected void shouldOverrideKeyEventOverride(WebView view, KeyEvent event) {

    }

    protected boolean shouldOverrideUrlLoadingOverride(WebView view, String url) {
        return false;
    }

    protected void receivedSslErrorOverride(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
        handler.proceed();
    }

    protected void JsAlert(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(BaseWebActivity.this)
                .setTitle("")
                .setMessage(message)
                .setPositiveButton("ok", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });

        builderAlert.setCancelable(false);
        builderAlert.create();
        builderAlert.show();
    }

    protected void JsComfirm(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(message)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                        dialog.dismiss();
                    }

                });

        builderAlert.setCancelable(false);
        builderAlert.create();
        builderAlert.show();
    }

    /**
     * 页面中加载url
     * @param url 本地html文件的路径
     */
    public void refreshPage(final String url) {
//        if (NetworkManageUtil.checkNetworkAvailable(this)) {//// TODO: 16/8/29 这里不需要判断？
            new Thread() {
                public void run() {
                    LogUtil.i(this, "hww==>>>" + url);

                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = url;
                    handler.sendMessage(msg);
                }
            }.start();
//        } else {
//            DialogUtil.toastForLong(BaseWebActivity.this, getString(R.string.tip_network_invalidate));
//        }
        // registDataSourceEvent();
        if (!TextUtils.isEmpty(appVersion)){
            registerVersion(appVersion);
        }
        if (!TextUtils.isEmpty(locationProvince)){
            registerLocationProvince(locationProvince);
        }
        if (!TextUtils.isEmpty(locationCity)){
            registerLocationCity(locationCity);
        }
        if (!TextUtils.isEmpty(startCity)){
            registerStartCity(startCity);
        }
        registerUserId(userid);
        registerCookieEvent();
        registerAction();
    }

    protected Handler handler = new Handler() {
        public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0:
//                        if (dialog != null && dialog.isShowing()) {
//                            dialog.dismiss();
//                        }
//                        if (isShowLoading) {
//                            dialog = DialogUtil.buildDialogRecover(BaseWebActivity.this);// 显示进度对话框
//                        }  //TODO  详情页面子页面间交互时 取消显示loadingDilog

                        if (msg.obj != null && webView != null) {
                            /**
                             * @date 2016-12-01
                             * @author jiangyingjun
                             * @description
                             * 解决新详情页在未登录状态下点击下一步登陆返回时进入的界面错误问题
                             *
                             * **/
                            if (ProductDetail548Activity.mKeyLowerCase.equals("login")&&userid==null){
                                ProductDetail548Activity.mKeyLowerCase = "";
                                return;
                            }
                            loadUrl(msg.obj.toString());
                        }
                        if (msg.obj != null && targetWebView != null) {
                            targetWebView.loadUrl(PhoneInfoUtil.getInstance().toUtf8String(msg.obj.toString()));
                        }
                        break;
                    case 1:
                        if (dialog != null && dialog.isShowing() && this != null && (!BaseWebActivity.this.isFinishing())) {
                            dialog.dismiss();// 隐藏进度对话框
                        }
                        break;
                    case IKeySourceUtil.LOADING:
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        dialog = DialogUtil.buildDialogRecover(BaseWebActivity.this);// 显示进度对话框
                        break;
                    case IKeySourceUtil.EXCEPTION:
                        if (msg.obj != null) {
                            builder = CatchExceptionUtils.catchExceptions((Exception) msg.obj, BaseWebActivity.this, dialog);
                        }
                        break;

                    //3秒之后关闭loading
                    case CLOSE_WEB_LOADING:
                        if (dialog != null && dialog.isShowing()) {
                            /*
                            * @data 2017-07-05
                            * @jiangyingjun
                            * @description  进入页面后loading一直显示
                            * 注释掉就好了
                            *
                            * */
//                            android.app.Activity activity = dialog.getOwnerActivity();
//                            if (activity==null||activity.isDestroyed()||activity.isFinishing()){
//                                return;
//                            }
                            dialog.dismiss();
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    private void registerVersion(String appVersion) {
        try {
            webView.loadUrl("javascript:api.setLocalStorage('appversion','"+ URLEncoder.encode(appVersion, "UTF-8")+"')");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void registerLocationProvince(String locationProvince) {
        try {
            webView.loadUrl("javascript:api.setLocalStorage('province','"+ URLEncoder.encode(locationProvince, "UTF-8")+"')");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private void registerLocationCity(String locationCity) {
        try {
            webView.loadUrl("javascript:api.setLocalStorage('departureCityName','"+ URLEncoder.encode(locationCity, "UTF-8")+"')");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private void registerStartCity(String startCity) {
        try {
            webView.loadUrl("javascript:api.setLocalStorage('startcity','"+ URLEncoder.encode(startCity, "UTF-8")+"')");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void registerUserId(String userId) {
        try {
            LogUtil.i("hww","hww:userId:"+userId);
            if (userId!=null){
                webView.loadUrl("javascript:api.setLocalStorage('discoveruserid','"+ URLEncoder.encode(String.valueOf(userId), "UTF-8")+"')");
                webView.loadUrl("javascript:api.setLocalStorage('userid','"+ URLEncoder.encode(String.valueOf(userId), "UTF-8")+"')");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setTopbarVisibility(int visibility){
        top_bar.setVisibility(visibility);
    }

    /**
     * webview加载url
     * @param url
     */
    protected void loadUrl(String url) {
        if (url != null && url != "") {
            url = formatUrl(url);
            webView.loadUrl(url);
        }
    }


    /**
     * 注入原生接口到js中
     * @param object
     */
    protected void addJavascriptInterface(Object object) {
        webView.addJavascriptInterface(object, getJavascriptName(object.getClass().getName()));
    }

    /**
     * 注入原生接口到js中
     * @param object
     * @param objName
     */
    protected void addJavascriptInterface(Object object, String objName) {
        webView.addJavascriptInterface(object, objName);
    }

    private String getJavascriptName(String name) {
        String[] arr = name.split("[.]");
        int length = arr.length;
        return arr[length - 1];
    }

    private void registerCookieEvent() {
        cookie = new Cookie(this);
        addJavascriptInterface(cookie, Config.COOKIE_STR);
    }

    private void registerAction() {
        addJavascriptInterface(this, Config.ACTION_STR);
    }

    @JavascriptInterface
    public void invoke(int what, String url, String param, int isUseCache) {//是被js调用的
        LogUtil.i("hww:","what:"+what+",url:"+url+",param:"+param+",isUseCache"+isUseCache);
        dataProvider(what, url, param, isUseCache);
    }

    /**
     * 请求网络数据然后调用callCallback将请求的数据传递给js
     * @param what
     * @param datakey json字符串
     * @param param
     * @param isUseCache
     */
    protected void dataProvider(int what, String datakey, String param, int isUseCache) {

    }

    @JavascriptInterface
    public String exec(String key, String jvalue) {
        LogUtil.i("hww","执行了exec");
        try {
            jvalue = URLDecoder.decode(jvalue, "UTF-8");
            return execute(key, jvalue);
        } catch (UnsupportedEncodingException e) {
            LogUtil.i("hww","exec:error"+e.getMessage().toString());
            return exceptionJson.SystemExceptionJson(e.getMessage());
        }
    }

    protected String execute(String key, String jvalue) {
        return "";
    }

    /**
     *
     * @param what
     * @param jresult 要传递给js的json串
     */
    public void callCallback(String what, String jresult) {
        callJs(Config.API_CALLBACK, what, jresult);
    }

    /**
     * @param what
     * @param jresult 要传递给js的json串
     */
    public void callSubPageCallback(final String what, final String jresult) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                webView.loadUrl("javascript:" + Config.API_SUBPAGE_CALLBACK + "('" + what + "','" + jresult + "');");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("javascript:" + Config.API_SUBPAGE_CALLBACK + "('" + what + "','" + jresult + "');", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }else{
                    webView.loadUrl("javascript:" + Config.API_SUBPAGE_CALLBACK + "('" + what + "','" + jresult + "');");
                }
            }

        });
    }

    protected void callJs(final String funtionName, final String what, final String jsonParam) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("javascript:" + funtionName + "('" + what + "'," + jsonParam + ");", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }else{
                    webView.loadUrl("javascript:" + funtionName + "('" + what + "'," + jsonParam + ");");
                }

            }

        });

    }

    protected void callJs(final String funtionName) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("javascript:" + funtionName + "();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                } else {
                    webView.loadUrl("javascript:" + funtionName + "();");
                }
            }

        });
    }

    /**
     * 调用js串的时候 引用此方法  例：document.querySelectorAll('*[name=description]')[0].getAttribute('content')
     *
     * @param funtionName
     *                  需要传递的JS串
     */
    protected String callJsDescription(final String funtionName) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(funtionName, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.e("value", value);
                            content = value;
                        }
                    });
                } else {
                    webView.loadUrl(funtionName);
                }
            }
        });
        return content;
    }

    @Override
    protected void onDestroy() {
        WebUtils.clearWebViewResource(webView);
        demoProUtil.firstMlinkGotoHome(BaseWebActivity.this);
        super.onDestroy();

    }


    /**
     * 如果是XML文件的话在这里返回ID如果是View对象的话返回-1  从写getResLayoutView方法
     * @return
     */
    public abstract int getResLayoutId();

    /**
     * 对web页面的url进行拼接等处理
     * @param url
     * @return
     */
    public String formatUrl(String url) {
//        if (url.contains("demo.com") || url.contains("democdn.com")) {
            //过滤url中的#（#是子页面的标示）
            if (url.indexOf("#") > -1) {
                url = url.split("#")[0];
            }

            if (url.toLowerCase().indexOf("devicetype=") == -1) {
                if (url.indexOf("?") > -1) {
                    url = url + "&" + Config.DEVICETYPE;
                } else {
                    url = url + "?" + Config.DEVICETYPE;
                }
            }

            //hybridversion  用来区分app版本(1代表5.4.8版本    3代表5.5.0版本)
            String appversion = new PhoneInfoUtil().getVersion(mActivity);
            if (url.toLowerCase().indexOf("hybridversion") == -1) {
                if (url.indexOf("?") > -1) {
                    url = url + "&" + "hybridversion=3" + "&appversion=" + appversion;//550版本开始hybridversion等于3
                } else {
                    url = url + "?" + "hybridversion=3" + "&appversion=" + appversion;
                }
            }

            if (url.toLowerCase().indexOf("source") == -1) {
                if (url.indexOf("?") > -1) {
                    url = url + "&" + Config.DEVICESOURCE;
                } else {
                    url = url + "?" + Config.DEVICESOURCE;
                }
            }
//        }
        return url;
    }
}
