package com.demo.app.hybrid.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.CRC32;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.demo.app.BuildConfig;
import com.demo.app.domain.CommonRequestField;
import com.demo.app.mvp.app.BaseApplication;
import com.demo.app.mvp.greendao.GreenDaoManager;
import com.demo.app.mvp.model.UpDateVersionModel;
import com.demo.app.mvp.model.bean.ReceiveDTO;
import com.demo.app.mvp.model.bean.RequestDTO;
import com.demo.app.mvp.model.greendaobean.UpdateInfo;
import com.demo.app.mvp.model.network.NetWorks;
import com.demo.app.mvp.model.network.https2HttpUtils;
import com.demo.app.util.CommReqFieldValuePackag;
import com.demo.app.util.DESUtil;
import com.demo.app.util.FileUtils;
import com.demo.app.util.IKeySourceUtil;
import com.demo.app.util.LogUtil;
import com.demo.app.util.NetworkManageUtil;
import com.demo.app.util.SharedPreferencesUtils;
import com.demo.app.wxapi.MD5Util;

import retrofit2.http.HEAD;
import rx.Subscriber;

public class SyncVersion {
    private Cookie cookie = null;
//    public static boolean isDebug = Config.isDebug;

    public static int hybridVersion = 1;
    /*检查更新的文件内容是否和 校验码一致。一致不更新 反之  更新*/
    private CRC32  mCrc32;
    private JSONObject _json ;
    public SyncVersion(Context context) {
        cookie = new Cookie(context);
    }

    public String storePath = "";

    private String version_crc32,version_updateTime;
    private JSONObject temp_JsonToLocal;

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public void setLocalVersionInFo(String data){
        try {
            this.temp_JsonToLocal=new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//    public  String inputStream2String (InputStream in) throws IOException {
//
//        StringBuffer out = new StringBuffer();
//        byte[]  b = new byte[4096];
//        int n;
//        while ((n = in.read(b))!= -1){
//            out.append(new String(b,0,n));
//        }
//        Log.i("String的长度",new Integer(out.length()).toString());
//        return  out.toString();
//    }

    public class RunCaseDebug implements Runnable {
        private final CountDownLatch countDownLatch;

        public String key = "";
        public String host = "";

        RunCaseDebug(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {

            String url = key + "?ver=" + new Date().getTime();
            byte[] ver = null;
            try {
                if (url.toLowerCase().indexOf("https://") > -1) {
                    try {
                        ver = downloadSSLAllWithHttpClient(url);//download byte 下载版本txt文件
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ver = download(url);// 下载版本txt文件的byte流
                }


                if (ver == null) return;
                String version = new String(ver);//得到下载来的版本txt文件，内容是一个json串
//                saveMarkIntoFile(ver);

                JSONObject json = new JSONObject(version);//json字符串转化为JsonObject对象
                JSONObject jobjHybridVersion = json.getJSONObject("hybridVersion");

                int androidHybridVer = jobjHybridVersion.getInt("android");
                if (androidHybridVer > hybridVersion) {//如果版本文件中的版本号比本地的版本号大，那么需要升级app // TODO: 16/4/19
                    //	baseWebActivity.handler.sendMessage(new Message());
                } else {
                    JSONObject jsonUpdate = json.getJSONObject("update");

                    JSONObject jsonReplace = null;
                    if (!json.isNull("replace")) {//replace 的value存在且不为空
                        jsonReplace = json.getJSONObject("replace");
                    }

                    if (jsonReplace.length() > 0) {//replace数组内是.html .js .css 如果有需要替换的东西

                        Iterator<String> it = jsonUpdate.keys();
                        while (it.hasNext()) {
                            String str = it.next();
                            JSONArray array = jsonUpdate.getJSONArray(str);

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String v = obj.getString("ver");//得到ver string 版本号
                                String p = obj.getString("file");//得到file String 文件url地址

                                String fileName = getFileName(p).toLowerCase();//得到文件名

                                String localVersion = String.valueOf(getOldFileVersionStr(p, key));

                                /*检测文件是否存在*/
                               boolean  result_file_exit= checkVersionFileIsExist(p);

                                if (BuildConfig.ISUpdateAllOrNot){
                                   
                                        Log.e("hww","downloadpath:"+getDownloadPath(p, host, v));
                                        byte[] buf = new byte[0];//从网络下载新版本号的file文件
                                        try {
                                            buf = downloadSSLAllWithHttpClient(getDownloadPath(p, host, v));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (buf == null){

                                            UpdateInfo up = new UpdateInfo();
                                            up.setFile(p);
                                            up.setVer("404");
                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                            
                                           continue;
                                        }

                                        if (mCrc32==null){
                                            mCrc32 = new CRC32();
                                        }
                                        mCrc32.reset();
                                        mCrc32.update(buf);
                                        Long crcResult=mCrc32.getValue();
                                        String result_crc32 = crcResult.toString();
                                    /*文件验证和ver版本号一样*/
                                        if (v.equals(result_crc32)||!v.equals(result_crc32)){
                                            String h = new String(buf);
                                            Iterator<String> itReplace = jsonReplace.keys();
                                            while (itReplace.hasNext()) {//有需要替换的东西

                                                String s = ( itReplace.next()).toLowerCase();

                                                if (isReplaceFile(fileName, s)) {//如果是需要替换文件内字符串的文件
                                                    if (jsonReplace != null && jsonReplace.length() > 0) {
                                                        if (fileName.indexOf(s) > -1) {
                                                            buf = replaceSign(h, jsonReplace.getJSONArray(s));//遍历文件，将文件内指定字符串替换为新的字符串

                                                        }
                                                    }
                                                }
                                            }
                                            //得到当前的这个file理论上的储存路径
                                            String filePath = getCurrentPath(p, storePath, fileName);
                                                                        /*如果该路径不存在。则创建该路径*/
                                            File file=new File(filePath);
                                            if (!file.exists()){
                                                file.mkdirs();
                                            }

                                            if (filePath!=null&&!TextUtils.isEmpty(filePath)){
                                                saveFile(buf, filePath, fileName);//保存到储存路径中
                                            }else {
                                                saveFile(buf,storePath+p,fileName);
                                            }
                                            if (temp_JsonToLocal!=null&&temp_JsonToLocal.length()>0){
                                                upDateLocalVersionInFo(key,temp_JsonToLocal,str,localVersion,v,p);

                                            }
                                        }else {

                                            UpdateInfo up = new UpdateInfo();
                                            up.setFile(p);
                                            up.setVer(result_crc32);
                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                            
                                            
                                        }
                                    
                                }else {
                                    if (!v.equals(localVersion)||localVersion.equals("")||localVersion.equals("0")||!result_file_exit) {
                                        Log.e("hww","downloadpath:"+getDownloadPath(p, host, v));
                                        byte[] buf = new byte[0];//从网络下载新版本号的file文件
                                        try {
                                            buf = downloadSSLAllWithHttpClient(getDownloadPath(p, host, v));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (buf == null){
                                            UpdateInfo up = new UpdateInfo();
                                            up.setFile(p);
                                            up.setVer("404");
                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                            continue;
                                        }

                                        if (mCrc32==null){
                                            mCrc32 = new CRC32();
                                        }
                                        mCrc32.reset();
                                        mCrc32.update(buf);
                                        Long crcResult=mCrc32.getValue();
                                        String result_crc32 = crcResult.toString();
                                    /*文件验证和ver版本号一样*/
                                        if (v.equals(result_crc32)){
                                            String h = new String(buf);
                                            Iterator<String> itReplace = jsonReplace.keys();
                                            while (itReplace.hasNext()) {//有需要替换的东西

                                                String s = ( itReplace.next()).toLowerCase();

                                                if (isReplaceFile(fileName, s)) {//如果是需要替换文件内字符串的文件
                                                    if (jsonReplace != null && jsonReplace.length() > 0) {
                                                        if (fileName.indexOf(s) > -1) {
                                                            buf = replaceSign(h, jsonReplace.getJSONArray(s));//遍历文件，将文件内指定字符串替换为新的字符串

                                                        }
                                                    }
                                                }
                                            }
                                            //得到当前的这个file理论上的储存路径
                                            String filePath = getCurrentPath(p, storePath, fileName);
                                                                        /*如果该路径不存在。则创建该路径*/
                                            File file=new File(filePath);
                                            if (!file.exists()){
                                                file.mkdirs();
                                            }

                                            if (filePath!=null&&!TextUtils.isEmpty(filePath)){
                                                saveFile(buf, filePath, fileName);//保存到储存路径中
                                            }else {
                                                saveFile(buf,storePath+p,fileName);
                                            }
                                            if (temp_JsonToLocal!=null&&temp_JsonToLocal.length()>0){
                                                upDateLocalVersionInFo(key,temp_JsonToLocal,str,localVersion,v,p);

                                            }
                                        }else {

                                            UpdateInfo up = new UpdateInfo();
                                            up.setFile(p);
                                            up.setVer(result_crc32);
                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                        }
                                    }
                                }

                            }
                        }
                    }

//                    cookie.setCookie(key, version, 0);
                }

                /*在此上传更新的结果*/
                uploadVersionLoadInfo();

                countDownLatch.countDown();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }




    /*更新本地记录的version信息，每更新成功一个文件就更新一次
    *
    * @param key 资源数据保存到cookie中的key
    * @param json 保存到cookie中的value 并保存到本地version-ssl.txt中
    * @param json_key  要更新存储资源数据中的那个节点下的数据  比如 html ／style或者image
    * @param  localVerion 本地存储的文件版本号
    * @param   newVersion  新的文件版本号
    * @param  newFilePatch  新的文件路径
    * */

    private void  upDateLocalVersionInFo(String key,JSONObject json,String json_key,String localVerion,String newVersion,String newFilePatch){
        try {
        JSONObject json_update=json.getJSONObject("update");
        JSONArray jsonArray=json_update.optJSONArray(json_key);
        if (jsonArray!=null&&jsonArray.length()>0){
            for (int i=0;i<jsonArray.length();i++){

                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                   String ver= jsonObject.optString("ver");
                    /*有符合条件的就更新内容*/
                    if (ver.equals(localVerion)){
                        jsonObject.put("ver",newVersion);
                        jsonArray.put(i,jsonObject);
                        break;
                    }
                    /*如果没有符合条件的就新增*/
                    if (i==jsonArray.length()-1){
                        JSONObject add = new JSONObject();
                        add.put("ver",newVersion);
                        add.put("file",newFilePatch);
                        jsonArray.put(add);
                        break;
                    }
            }
        }
        json_update.put(json_key, jsonArray);
            json.put("update",json_update);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*更新临时json用于下个文件 更新 保持temp_JsonToLocal信息 最新*/
        temp_JsonToLocal=json;
        /*保存更新到cookie*/
        cookie.setCookie(key, json.toString(), 0);
         /*将请求到的资源数据保存到本地文件*/
         saveMarkIntoFile(json.toString().getBytes());

    }

    /*
    * 检测本地资源文件是否存在
    *
    * */
    private boolean  checkVersionFileIsExist(String path){
        boolean result=false;
        path=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+Config.DIRECTORY+File.separator+path;
        if (path.contains("https://")){
            path = path.replace("https://", "");
            result=FileUtils.isFileExists(path);
            return result;
        }else if (path.contains("http://")){
            path = path.replace("http://", "");
            result=FileUtils.isFileExists(path);
            return result;
        }else {

        }
       return result;


    }




    private void saveMarkIntoFile(byte[] version) {
        if (version!=null){
            if (FileUtils.isSdcardExist()) {
                String sdPath = Environment.getExternalStorageDirectory() + "/";
                String mSavePath = sdPath + Config.DIRECTORY;
                FileUtils.createDirFile(mSavePath);
                File txtFile = FileUtils.createNewFile(mSavePath + "/" + Config.VERSION_FILE);//创建文件
                if (txtFile!=null){
                    try {
                        FileOutputStream fos = new FileOutputStream(txtFile);
                        fos.write(version);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     *
     * @param filePath file的url
     * @param storePath 文件保存路径
     * @param fileName 文件名
     * @return 当前的文件储存路径
     */
    private String getCurrentPath(String filePath, String storePath, String fileName) {
        if (filePath.toLowerCase().indexOf("http://") > -1) {
            filePath = removeHost(filePath);
        }else if (filePath.toLowerCase().indexOf("https://") > -1){
            filePath = removeHost2(filePath);
        }


        filePath = filePath.toLowerCase().replace(File.separator + fileName.toLowerCase(), File.separator);
        if (filePath.indexOf("storePath") == -1) {
            if (storePath.lastIndexOf("/") != storePath.length() - 1) {
                storePath = storePath + "/";
            }
            filePath = storePath + filePath;
        }
        return filePath;
    }

    //移除http:// 后的字符串
    private String removeHost(String url) {
        //String patternString = "http://[^/]+";
        String patternString = "http://";
        Pattern pattern = Pattern.compile(patternString);
        Matcher m = pattern.matcher(url);
        return m.replaceAll("");

    }
    //移除http:// 后的字符串
    private String removeHost2(String url) {
        //String patternString = "https://[^/]+";
        String patternString = "https://";
        Pattern pattern = Pattern.compile(patternString);
        Matcher m = pattern.matcher(url);
        return m.replaceAll("");

    }

    //判断文件是否是需要替换内容的文件
//    private boolean isReplaceFile(String fileName, JSONArray jsonReplaceFileType) {
//        for (int i = 0; i < jsonReplaceFileType.length(); i++) {
//            try {
//                if (fileName.toLowerCase().indexOf(jsonReplaceFileType.getString(i).toLowerCase()) > -1) {
//                    return true;
//                }
//            } catch (JSONException e) {
//
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }

    /**
     * 是否是需要替换文件内字符串的文件
     * @param fileName 文件名，例如Index.html
     * @param jsonReplaceFileType 例如.html,.js,.css
     * @return 是否需要替换字符串
     */
    private boolean isReplaceFile(String fileName, String jsonReplaceFileType) {
        if (fileName.toLowerCase().indexOf(jsonReplaceFileType.toLowerCase()) > -1) {
            return true;
        }

        return false;
    }

    /**
     *将文件内指定字符串替换的为新的字符串
     * @param htmlStr 需要替换的文件字符串
     * @param replace
     * @return
     */
    private byte[] replaceSign(String htmlStr, JSONArray replace) {
        for (int i = 0; i < replace.length(); i++) {
            try {
                JSONObject obj = replace.getJSONObject(i);
                htmlStr = htmlStr.replace(obj.getString("old"), obj.getString("new"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return htmlStr.getBytes();
    }


    //下载任务
    public void getVersionList(CountDownLatch latch, String versionFile, String versionHost) {
        if(BuildConfig.versionUpdateEnvironment==1||BuildConfig.versionUpdateEnvironment==0){

                 String key = versionHost + versionFile;
//                 String host = versionHost;
                    CommonRequestField commonRequestField = CommReqFieldValuePackag
                            .getCommReqField(BaseApplication.getContext());
                    JSONObject jsonObject = new JSONObject();
                    JSONObject postdata = new JSONObject();

                    String jsonStr;
                    RequestDTO requestDTO = new RequestDTO();
                    try {
                        if (BuildConfig.versionUpdateEnvironment==1){
                            postdata.put("CompiledVersion", "store");
                        }else if (BuildConfig.versionUpdateEnvironment==0){
                            postdata.put("CompiledVersion", "release");
                        }
                        postdata.put("CRC32",new SharedPreferencesUtils(BaseApplication.getContext(), IKeySourceUtil.LOGIN_STATUS).getString("CRC32","0"));
                        jsonObject.put("clientSource", commonRequestField.getClientSource());
                        jsonObject.put("phoneID", commonRequestField.getPhoneID());
                        jsonObject.put("phoneType", commonRequestField.getPhoneType());
                        jsonObject.put("phoneVersion", commonRequestField.getPhoneVersion());
                        jsonObject.put("startCity", commonRequestField.getStartCity());
                        jsonObject.put("Path", "http://msitelogic.demo.com/api/");
                        jsonObject.put("ControllerName", "HybridVersion");
                        jsonObject.put("ActionName", "GetVersionFile");
                        jsonObject.put("PostData", postdata.toString());

                        jsonStr = DESUtil.des3EncodeCBC(jsonObject.toString().getBytes("UTF-8"),
                                IKeySourceUtil.PASSWORD_CRYPT_KEY);

                        requestDTO.setContent(jsonStr);
                    }catch (Exception e){

                    }

                    NetWorks.getVersionInFo(requestDTO, new Subscriber<ReceiveDTO>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
//                            Log.e("jyj-->syscverion", e.toString());
                                }

                                @Override
                                public void onNext(ReceiveDTO receiveDTO) {
                                    if (receiveDTO != null && receiveDTO.getMC() == 1000 && receiveDTO.getContent().length() > 0){
                                        try {
                                            String data = DESUtil.des3DecodeCBC(receiveDTO.getContent());

                                            JSONObject json_ = new JSONObject(data);
                                            int ErrorCode=  json_.optInt("ErrorCode");
                                            if (ErrorCode==200){
                                                JSONObject json=new JSONObject(json_.optString("JsonResult"));

                                                if (json!=null&&json.length()>0){

                                                    _json= json.getJSONObject("Detail");
                                                    version_crc32 = json.optString("CRC32");
                                                    new SharedPreferencesUtils(BaseApplication.getContext(), IKeySourceUtil.LOGIN_STATUS).putString("CRC32", version_crc32);
                                                    version_updateTime = json.optString("UpdateTime");

                                                    JSONObject jobjHybridVersion = _json.getJSONObject("hybridVersion");
                                                    int androidHybridVer = jobjHybridVersion.getInt("android");
                                                    if (androidHybridVer > hybridVersion) {//如果版本文件中的版本号比本地的版本号大，那么需要升级app
//                    	baseWebActivity.handler.sendMessage(new Message());
                                                    } else {
                                                        JSONObject jsonUpdate = _json.getJSONObject("update");

                                                        JSONObject jsonReplace = null;
                                                        if (!_json.isNull("replace")) {//replace 的value存在且不为空
                                                            jsonReplace = _json.getJSONObject("replace");
                                                        }
                                                        /**
                                                         * 对比ver 与更新资源文件的逻辑
                                                         * */
                                                        Iterator<String> it = jsonUpdate.keys();
                                                        while (it.hasNext()) {

                                                            String str = it.next();
                                                            JSONArray array = jsonUpdate.getJSONArray(str);
                                                            if (array==null||array.length()<=0){
                                                                continue;
                                                            }
                                                            for (int i = 0; i < array.length(); i++) {

                                                                JSONObject obj = array.getJSONObject(i);
                                                                String v = obj.getString("ver");//得到ver string 版本号
                                                                String p = obj.getString("file");//得到file String 文件url地址

//                                                                Log.e("jyj-->", v+"     "+p);
                                                                //得到本地文件名
                                                                String fileName = getFileName(p).toLowerCase();
                                                                /*获取本地文件的版本号*/
                                                                String localVersion = String.valueOf(getOldFileVersionStr(p, key));
//                                                                Log.e("jyj-->", "version count-->" + index);

                                                                boolean result_file_exit = checkVersionFileIsExist(p);

                                                                if (BuildConfig.ISUpdateAllOrNot){
                                                                    byte[] buf = downloadSSLAllWithHttpClient(getDownloadPath(p, "", v));//从网络下载新版本号的file文件

                                                                    if (buf == null){
//                                                                            Log.e("jyj-->", p+"   文件下载失败-->  ");
                                                                        UpdateInfo up = new UpdateInfo();
                                                                        up.setFile(p);
                                                                        up.setVer("404");
//                                                                            up.setFail("download fail");
                                                                        GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                                                        continue;//如果没有下载下来，绕过这个文件继续下载
                                                                    }else{
//                                                                            Log.e("jyj-->", p+"   文件下载成功-->  "+" 地址：  "+p);
                                                                    }
//                                                                  //判断下载下来的文件和version.txt中的文件的crc32是否相等
                                                                    if (mCrc32==null){
                                                                        mCrc32 = new CRC32();
                                                                    }
                                                                    mCrc32.reset();
                                                                    mCrc32.update(buf);
                                                                    Long crcResult=mCrc32.getValue();
                                                                    String result_crc32 = crcResult.toString();
                                                                    //如果下载下来的文件的crc32和版本文件中文件的crc32一致，说明文件正确 进行下载
//                                                                        Log.e("jyj-->", " 地址：  "+p+"version    "+v +" local version "+crcResult);
                                                                    if (v.equals(result_crc32)){
//                                                                        LogUtil.i("hww","hww:real:download:"+getDownloadPath(p, host, v));
//                                                                            Log.e("jyj-->", "文件比对成功");
                                                                        String h = new String(buf);
                                                                        Iterator<String> itReplace = jsonReplace.keys();
                                                                        while (itReplace.hasNext()) {//有需要替换的东西

                                                                            String s = ( itReplace.next()).toLowerCase();

                                                                            if (isReplaceFile(fileName, s)) {//如果是需要替换文件内字符串的文件
                                                                                if (jsonReplace != null && jsonReplace.length() > 0) {
                                                                                    if (fileName.indexOf(s) > -1) {
                                                                                        buf = replaceSign(h, jsonReplace.getJSONArray(s));//遍历文件，将文件内指定字符串替换为新的字符串

                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        //得到当前的这个file理论上的储存路径
                                                                        String filePath = getCurrentPath(p, storePath, fileName);
                                                                        /*如果该路径不存在。则创建该路径*/
                                                                        File file=new File(filePath);
                                                                        if (!file.exists()){
                                                                            file.mkdirs();
                                                                        }

                                                                        if (filePath!=null&&!TextUtils.isEmpty(filePath)){
                                                                            saveFile(buf, filePath, fileName);//保存到储存路径中
                                                                        }else {
                                                                            saveFile(buf,storePath+p,fileName);
                                                                        }
                                                                        if (temp_JsonToLocal!=null&&temp_JsonToLocal.length()>0){
                                                                            upDateLocalVersionInFo(key,temp_JsonToLocal,str,localVersion,v,p);

                                                                        }
                                                                    }else {
//                                                                            Log.e("jyj-->", "文件比对失败---"+crcResult);
                                                                        /*不一致 则失败*/
                                                                        UpdateInfo up = new UpdateInfo();
                                                                        up.setFile(p);
                                                                        up.setVer(result_crc32);
//                                                                            up.setFail("version comparison fail"+result_crc32);

                                                                        GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                                                    }

                                                                }else {
                                                                          /*本地文件的版本号与接口中的版本号 值做比较。如果不等则下载改文件资源*/
                                                                    if (!v.equals(localVersion)||localVersion.equals("")||localVersion.equals("0")||!result_file_exit) {

                                                                        byte[] buf = downloadSSLAllWithHttpClient(getDownloadPath(p, "", v));//从网络下载新版本号的file文件

                                                                        if (buf == null){
//                                                                            Log.e("jyj-->", p+"   文件下载失败-->  ");
                                                                            UpdateInfo up = new UpdateInfo();
                                                                            up.setFile(p);
                                                                            up.setVer("404");
//                                                                            up.setFail("download fail");
                                                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                                                            continue;//如果没有下载下来，绕过这个文件继续下载
                                                                        }else{
//                                                                            Log.e("jyj-->", p+"   文件下载成功-->  "+buf.toString());
                                                                        }
//                                                                  //判断下载下来的文件和version.txt中的文件的crc32是否相等
                                                                        if (mCrc32==null){
                                                                            mCrc32 = new CRC32();
                                                                        }
                                                                        mCrc32.reset();
                                                                        mCrc32.update(buf);
                                                                        Long crcResult=mCrc32.getValue();
                                                                        String result_crc32 = crcResult.toString();
                                                                        //Log.e("jyj-->", "crcresult==>"+crcResult + "   "+result.length());
                                                                        //如果下载下来的文件的crc32和版本文件中文件的crc32一致，说明文件正确 进行下载

                                                                        if (v.equals(result_crc32)){
//                                                                        LogUtil.i("hww","hww:real:download:"+getDownloadPath(p, host, v));
//                                                                            Log.e("jyj-->", "文件比对成功");
                                                                            String h = new String(buf);
                                                                            Iterator<String> itReplace = jsonReplace.keys();
                                                                            while (itReplace.hasNext()) {//有需要替换的东西

                                                                                String s = ( itReplace.next()).toLowerCase();

                                                                                if (isReplaceFile(fileName, s)) {//如果是需要替换文件内字符串的文件
                                                                                    if (jsonReplace != null && jsonReplace.length() > 0) {
                                                                                        if (fileName.indexOf(s) > -1) {
                                                                                            buf = replaceSign(h, jsonReplace.getJSONArray(s));//遍历文件，将文件内指定字符串替换为新的字符串

                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            //得到当前的这个file理论上的储存路径
                                                                            String filePath = getCurrentPath(p, storePath, fileName);
                                                                        /*如果该路径不存在。则创建该路径*/
                                                                            File file=new File(filePath);
                                                                            if (!file.exists()){
                                                                                file.mkdirs();
                                                                            }

                                                                            if (filePath!=null&&!TextUtils.isEmpty(filePath)){
                                                                                saveFile(buf, filePath, fileName);//保存到储存路径中
                                                                            }else {
                                                                                saveFile(buf,storePath+p,fileName);
                                                                            }
                                                                            if (temp_JsonToLocal!=null&&temp_JsonToLocal.length()>0){
                                                                                upDateLocalVersionInFo(key,temp_JsonToLocal,str,localVersion,v,p);

                                                                            }
                                                                        }else {
//                                                                            Log.e("jyj-->", "文件比对失败---"+crcResult);
                                                                        /*不一致 则失败*/
                                                                            UpdateInfo up = new UpdateInfo();
                                                                            up.setFile(p);
                                                                            up.setVer(result_crc32);
//                                                                            up.setFail("version comparison fail "+result_crc32);
                                                                            GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().insert(up);
                                                                        }
                                                                    }else {

//                                                                        Log.e("jyj-->", "版本号一样");

                                                                    }
                                                                }

                                                            }
                                                        }

                                                    /*在此上传更新的结果*/
                                                        uploadVersionLoadInfo();
                                                    }

                                                }

                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }


                                }
                            }

                    );


        }else {
            RunCaseDebug runCaseDebug = new RunCaseDebug(latch);
            runCaseDebug.key=versionHost + versionFile;//key 拼接起来其实就是url 类似
            runCaseDebug.host=versionHost;//host 赋值
            new Thread(runCaseDebug).start();
        }

    }
    /*上传资源更新结果*/

    private  void uploadVersionLoadInfo(){


        List<UpdateInfo> list= GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().loadAll();
        if (list!=null){

            Log.e("jyj-->", "更新失败的数量"+list.size());
            JSONObject json_result = new JSONObject();
            JSONArray arrayInFo = new JSONArray();

            try {

                for (int i=0;i<list.size();i++){
                    JSONObject json_info = new JSONObject();
                    json_info.put("file", list.get(i).getFile());
                    json_info.put("ver", list.get(i).getVer());
//                                                                json_info.put("fail", list.get(i).getFail());
                    arrayInFo.put(json_info);
                }

                json_result.put("update",arrayInFo);
                json_result.put("netInfo", NetworkManageUtil.checkNetworkName(BaseApplication.getContext()));
                json_result.put("remark", "");
                json_result.put("CRC32", version_crc32);
                json_result.put("IP",NetworkManageUtil.getIpAddressString());
                json_result.put("UpdateTime", version_updateTime);

            } catch (JSONException e) {
                e.printStackTrace();
            }



            UpDateVersionModel model = new UpDateVersionModel();
            model.updateVersionInfo(json_result, new UpDateVersionModel.onUpdateInfoListener() {
                @Override
                public void onUpdateInfoCompleted() {
                }

                @Override
                public void onUpdateInfoError(Throwable throwable) {
//                                                                    Log.e("jyj-->updateResult-->", throwable.toString());
                }

                @Override
                public void onUpdateInfoNext(ReceiveDTO receiveDTO) {
                    try {
                                                                        /*上传更新失败结果后。清除记录*/
                        GreenDaoManager.getInstance().getDaoSession().getUpdateInfoDao().deleteAll();
//                                                                        Log.e("jyj-->updateResult-->", DESUtil.des3DecodeCBC(receiveDTO.getContent()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }else {
//                                                            Log.e("jyj-->", "没有失败的");
        }




    }


    /**
     *
     * @param file 文件路径
     * @param versionKey 下载版本文件的url
     * @return 旧的版本文件中file的版本号 是文件的md5值
     * @return 新版本文件中file的版本号 是文件的crc32值
     * @throws JSONException
     */
    private String getOldFileVersionStr(String file, String versionKey) throws JSONException {
        String v ="";

        String versionTxt = cookie.getCookie(versionKey);//得到sp中保存的cookievalue实体中的版本文件 json字符串，和上次版本的版本文件比较，文件的版本号
        //如果本地本来就没有下载version.txt文件，那么return ""，即下载本次的版本文件
        if (versionTxt.equals("")){
            return "";
        }

        JSONObject json = new JSONObject(versionTxt);
        json = json.getJSONObject("update");
        Iterator<String> it = json.keys();

        boolean lock = true;
        while (it.hasNext()) {
            String str = (String) it.next();
            JSONArray array = json.getJSONArray(str);
            lock = true;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                v = obj.getString("ver");//是文件的crc32值，是String类型
                String p = obj.getString("file");

                if (p.equals(file)) {//如果旧的版本文件中file文件名称和新的版本文件中file文件名称相同
                    lock = false;
                    break;
                }
            }

            if (!lock) {
                break;
            }
        }
        if (lock) {
            v = "0";
        }
        return v;
    }

    /**
     *
     * @param file 文件路径
     * @param versionKey 下载版本文件的url
     * @return 旧的版本文件中file的版本号，是本地文件的md5值
     * @throws JSONException
     */
//    private String getOldFileVersionStr2(String file, String versionKey) throws JSONException {
//        String v ="";
//        String versionTxt = cookie.getCookie(versionKey);//得到sp中保存的cookievalue实体中的版本文件 json字符串，和上次版本的版本文件比较，文件的版本号
//        //如果本地本来就没有下载version.txt文件，那么return 0，即下载本次的版本文件
//        if (versionTxt.equals(""))
//            return "";
//        JSONObject json = new JSONObject(versionTxt);
//        json = json.getJSONObject("update");
//        Iterator<String> it = json.keys();
//
//        boolean lock = true;
//        while (it.hasNext()) {
//            String str = (String) it.next();
//            JSONArray array = json.getJSONArray(str);
//            lock = true;
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = array.getJSONObject(i);
//                v = obj.getString("ver");//是文件的md5值，是String类型
//                String p = obj.getString("file");
//
//                if (p.equals(file)) {//如果旧的版本文件中file文件名称和新的版本文件中file文件名称相同
//                    String newFilePath;
//                    if (p.indexOf("http://")>-1){
//                        newFilePath=Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://","");
//                    }else{
//                        newFilePath=Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("https://","");
//                    }
////                    String newFilePath=Environment.getExternalStorageDirectory() + File.separator + Config.DIRECTORY + "/" + p.replace("http://","");
//                    if (FileUtils.isFileExists(newFilePath)){
//                        //如果文件存在，获取到本地文件的md5值
//                        String haha=MD5Util.md5sum(newFilePath);
//                        LogUtil.i("hww","hww:getOldFileVersiob:haha:"+haha);
//                        LogUtil.i("hww","hww:getOldFileVersiob:"+v);
//                        v=MD5Util.md5sum(newFilePath).substring(0,10);//默认是32位md5，截取前10位，以为服务器的文件md5是10位
//                        lock = false;//false表示本地有这个文件
//                        break;
//                    }else{
//                        lock=true;
//                        break;
//                    }
//                }
//            }
//
//            if (!lock) {
//                break;
//            }
//        }
//        if (lock) {//true表示本地没有这个文件
//            v = "";
//        }
//        return v;
//    }

    /**
     *
     * @param file 文件路径
     * @param versionKey 下载版本文件的url
     * @return 旧的版本文件中file的版本号 如果旧的版本文件中有这个file文件名，那么返回它的ver版本号，如果旧的版本文件中没有这个file文件名，那么返回版本号0
     * @throws JSONException
     */
//    private int getOldFileVersion(String file, String versionKey) throws JSONException {
//        int v = 0;
//        if (isDebug) return v;//debug模式下每次都会下载，返回的版本号永远是0
//        String versionTxt = cookie.getCookie(versionKey);//得到sp中保存的cookievalue实体中的版本文件 json字符串，和上次版本的版本文件比较，文件的版本号
//        //如果本地本来就没有下载version.txt文件，那么return 0，即下载本次的版本文件
//        if (versionTxt.equals(""))
//            return 0;
//        JSONObject json = new JSONObject(versionTxt);
//        json = json.getJSONObject("update");
//        Iterator<String> it = json.keys();
//
//        boolean lock = true;
//        while (it.hasNext()) {
//            String str = (String) it.next();
//            JSONArray array = json.getJSONArray(str);
//            lock = true;
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = array.getJSONObject(i);
//                v = Integer.parseInt(obj.getString("ver"));//不是int值，是一个String类型的md5值
//                String p = obj.getString("file");
//
//                if (p.equals(file)) {//如果旧的版本文件中file文件名称和新的版本文件中file文件名称相同
//                    lock = false;
//                    break;
//                }
//            }
//
//            if (!lock) {
//                break;
//            }
//        }
//        if (lock) {
//            v = 0;
//        }
//        return v;
//    }


    /**
     * 得到file文件的下载url
     * @param path 版本文件中file对应的value
     * @param host
     * @param ver
     * @return
     */
    private String getDownloadPath(String path, String host, String ver) {
        String url = path;

        if (path.indexOf("http://") == -1&&path.indexOf("https://")==-1) {
            if (path.indexOf(File.separator) == 0 && host.lastIndexOf(File.separator) == host.length() - 1) {
                url = host + path.substring(File.separator.length(), path.length());
            } else {
                url = host + path;
            }
        }
        if (ver != "") {
            if (url.indexOf("?") == -1) {
                url = url + "?";
            } else {
                url = url + "&";
            }
            url = url + "ver=" + +new Date().getTime();
//            if (!isDebug) {//// TODO: 16/4/26
//
//            } else {
//                url = url + "ver=" + +new Date().getTime();
//            }
        }

        return url;
    }

    /**
     * 根据file路径得到文件名
     * @param path
     * @return
     */
    private String getFileName(String path) {
        if (path.indexOf(File.separator) > -1) {
            String[] p = path.split(File.separator);
            return p[p.length - 1];//路径中最后一个名称，也就是文件名
        }
        return path;
    }

    /**
     * 下载
     * @param spec
     * @return
     */
    public byte[] download(String spec) {
        if (!isWhitePage(spec))
            return null;
        try {
            URL url = new URL(spec);
            URLConnection conn = url.openConnection();
            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                conn.setRequestProperty("Connection", "close");
            }
            conn.connect();

            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            System.setProperty("http.keepAlive", "false");
            InputStream input = conn.getInputStream();

            int rc = 0;
            while ((rc = input.read(buff)) != -1) {
                byteOutput.write(buff, 0, rc);
            }
            input.close();
            byteOutput.close();

            return byteOutput.toByteArray();
        } catch (Exception e) {
            String err = e.getMessage();
        }

        return null;
    }

    //验证下载地址是否是白名单地址
    public boolean isWhitePage(String spec) {
        boolean isWhite = false;
        String[] white = Config.WHITELIMIT;

        for (int i = 0; i < white.length; i++) {
            isWhite = spec.indexOf(white[i]) > -1;
            if (isWhite)
                break;
        }
        return isWhite;
    }

    public void createDirectory(String filePath) {
        if (filePath == null || filePath.equals("")) return;
        String tmpPath = File.separator;
        String[] paths = filePath.split(File.separator);
        for (int i = 0; i < paths.length; i++) {
            if (paths[i].equals("")) continue;
            tmpPath = tmpPath + File.separator + paths[i];
            File dir = new File(tmpPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    //
    public void saveFile(byte[] buf, String filePath, String fileName) {
        FileOutputStream fos = null;

        try {
            createDirectory(filePath);

            File file = new File(filePath + fileName);
            if (file.exists()) {
                file.delete();
            }


            fos = new FileOutputStream(file);
            fos.write(buf);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public boolean execShell(String command) throws IOException, InterruptedException {
//        Runtime runtime = Runtime.getRuntime();
//        Process proc = runtime.exec(command);
//
//        if (proc.waitFor() != 0) {
//            return true;
//        }
//        return false;
//    }


    //得到assets目录下的版本文件内容（是一个Json串）
    public String getDefaultVersionFile(String AssetsPath, Context context) {

        String result = "";
        try {
            AssetManager am = context.getAssets();

            String[] FileOrDirName = am.list(AssetsPath);// Return a String array of all the assets at the given path.所有文件和子目录名称的数组
            String versionPath = "";

            for (int i = 0; i < FileOrDirName.length; i++) {
                if (FileOrDirName[i].toLowerCase().contains(Config.VERSION_FILE.toLowerCase())) {//如果assets目录下包含版本文件
                    versionPath = FileOrDirName[i];//版本文件的路径
                    break;
                }
            }

            InputStream inputStream = null;
            inputStream = am.open(AssetsPath + "/" + versionPath);//Open an asset using ACCESS_STREAMING mode.  打开版本文件，获取到版本文件内容
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }

            outputStream.close();
            inputStream.close();

            return outputStream.toString();//得到版本文件内容，是一个json串

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    /**
     *  从assets目录中复制整个文件夹内容
     *
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     *  @param  context  Context 使用CopyFiles类的Activity
     */

//    public void copyFilesFassets(String oldPath,String newPath,Context context) {
//        try {
//            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
//            if (fileNames.length > 0) {//如果是目录
//                File file = new File(newPath);
//                file.mkdirs();//如果文件夹不存在，则递归
//                for (String fileName : fileNames) {
//                    copyFilesFassets(oldPath + "/" + fileName,newPath+"/"+fileName,context);
//                }
//            } else {//如果是文件
//                InputStream is = context.getAssets().open(oldPath);
//                FileOutputStream fos = new FileOutputStream(new File(newPath));
//                byte[] buffer = new byte[1024];
//                int byteCount=0;
//                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
//                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
//                }
//                fos.flush();//刷新缓冲区
//                is.close();
//                fos.close();
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            //如果捕捉到错误则通知UI线程
////            MainActivity.handler.sendEmptyMessage(COPY_FALSE);
//        }
//    }


//    public boolean copyAssetsPath(String AssetsPath, String ObjectPath, Context context) {
//        File ObjPath = new File(ObjectPath);//创建一个文件
//        if (!ObjPath.exists() || !ObjPath.isDirectory()) {
//            ObjPath.mkdirs();//创建目录ObjPath
//        }
//
//        AssetManager am = context.getAssets();
//
//        try {
//            String[] FileOrDirName = am.list(AssetsPath);//Return a String array of all the assets at the given path.
//            for (int i = 0; i < FileOrDirName.length; i++) {
//                String path = "";
//                if (AssetsPath != "") {
//                    path = AssetsPath + File.separator;
//                }
//                //如果是assets目录
//                if (isAssetsDirs(AssetsPath + File.separator + FileOrDirName[i], context)) {
//                    File N_DIR = new File(ObjectPath + File.separator + FileOrDirName[i]);//创建file
//                    if (!N_DIR.exists()) {
//                        N_DIR.mkdir();//创建文件目录
//                    }
//
//                    copyAssetsPath(path + FileOrDirName[i], ObjPath + File.separator + FileOrDirName[i], context);//递归创建目录
//                } else {
//                    copyFile(context, path + FileOrDirName[i], ObjPath + File.separator + FileOrDirName[i]);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return true;
//    }

    private boolean isAssetsDirs(String fileOrDirName, Context context) {
        AssetManager am = context.getAssets();

        try {
            am.open(fileOrDirName);//打开某个具体的asset文件，打不开的话应该是目录
            return false;
        } catch (FileNotFoundException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    //拷贝文件，从from拷贝到to中
    public void copyFile(Context context, String from, String to) {
        try {
            InputStream myInput = context.getResources().getAssets().open(from);
            OutputStream myOutput = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //下载
    public byte[] downloadSSLAllWithHttpClient(String url) throws IOException {
//         Log.e("jyj normal-->", url);

        int timeOut = 50 * 1000;
        HttpParams param = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(param, timeOut);
        HttpConnectionParams.setSoTimeout(param, timeOut);
        HttpConnectionParams.setTcpNoDelay(param, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        try {
            registry.register(new Scheme("https", new TrustAllSSLSocketFactory().getDefault(), 443));
        } catch (KeyManagementException e) {

            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {

            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        } catch (KeyStoreException e) {

            e.printStackTrace();
        }

        try {
            ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
            DefaultHttpClient client = new DefaultHttpClient(manager, param);
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                byte[] buffer = new byte[4096];
                int r = 0;
                while ((r = is.read(buffer)) > 0) {
                    output.write(buffer, 0, r);
                }
                output.flush();
                output.close();
                return output.toByteArray();
            }else {
                if (url.contains(https2HttpUtils.HTTPS)){
                    Https2HttpRequset(https2HttpUtils.Https2Http(url));
                }{
                    return null;
                }

            }
        }catch (Exception e){
            return null;
        }
    }



    /**
     * @author jiangyingjun
     * @date 2016-11-15
     * @description  https请求不成功转http请求
     * @version 551
     * **/
    private byte[] Https2HttpRequset(String url) throws IOException {
        int timeOut = 50 * 1000;
        HttpParams param = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(param, timeOut);
        HttpConnectionParams.setSoTimeout(param, timeOut);
        HttpConnectionParams.setTcpNoDelay(param, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        try {
            registry.register(new Scheme("https", new TrustAllSSLSocketFactory().getDefault(), 443));
        } catch (KeyManagementException e) {

            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {

            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        } catch (KeyStoreException e) {

            e.printStackTrace();
        }
        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
        DefaultHttpClient client = new DefaultHttpClient(manager, param);
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        ByteArrayOutputStream output= new ByteArrayOutputStream();
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        byte[] buffer = new byte[4096];
        int r = 0;
        while ((r = is.read(buffer)) > 0) {
            output.write(buffer, 0, r);
        }
        output.flush();
        output.close();
        return output.toByteArray();

//        if (response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
//            ByteArrayOutputStream output= new ByteArrayOutputStream();
//            HttpEntity entity = response.getEntity();
//            InputStream is = entity.getContent();
//            byte[] buffer = new byte[4096];
//            int r = 0;
//            while ((r = is.read(buffer)) > 0) {
//                output.write(buffer, 0, r);
//            }
//            output.flush();
//            output.close();
//            return output.toByteArray();
//        }
//        return null;
    }


    public class TrustAllSSLSocketFactory extends SSLSocketFactory {
        private javax.net.ssl.SSLSocketFactory factory;
        private TrustAllSSLSocketFactory instance;

        private TrustAllSSLSocketFactory() throws KeyManagementException, UnrecoverableKeyException,
                NoSuchAlgorithmException, KeyStoreException {
            super(null);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new TrustAllManager()}, null);
            factory = context.getSocketFactory();
            setHostnameVerifier(new X509HostnameVerifier() {

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                    // TODO Auto-generated method stub

                }

                @Override
                public boolean verify(String host, SSLSession session) {
                    // TODO Auto-generated method stub
                    return true;
                }
            });
        }

        public SocketFactory getDefault() {
            if (instance == null) {
                try {
                    instance = new TrustAllSSLSocketFactory();
                } catch (KeyManagementException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return instance;
        }

        @Override
        public Socket createSocket() throws IOException {
            return factory.createSocket();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            if (Build.VERSION.SDK_INT < 11) { // 3.0
                injectHostname(socket, host);
            }

            return factory.createSocket(socket, host, port, autoClose);
        }

        private void injectHostname(Socket socket, String host) {
            try {
                Field field = InetAddress.class.getDeclaredField("hostName");
                field.setAccessible(true);
                field.set(socket.getInetAddress(), host);
            } catch (Exception ignored) {
            }
        }

        public class TrustAllManager implements X509TrustManager {

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // TODO Auto-generated method stub
                return null;
            }
        }

    }


}
