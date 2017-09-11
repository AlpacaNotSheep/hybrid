package com.demo.app.hybrid;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.demo.app.BuildConfig;
import com.demo.app.hybrid.core.Config;
import com.demo.app.hybrid.core.Cookie;
import com.demo.app.hybrid.core.SyncVersion;
import com.demo.app.mvp.app.BaseApplication;
import com.demo.app.mvp.app.InitializeService;
import com.demo.app.util.FileUtils;
import com.demo.app.util.LogUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static android.R.attr.path;

public class InitDataService extends IntentService {
    //    private StartControllerImp startController;
    private static final String START_VERSION_DOWNLOAD = "com.demo.app.versoin.download";

    public InitDataService() {
        super("InitDataService");
    }


    public static void start(Context  context) {
        Intent intent = new Intent(context, InitDataService.class);
        intent.setAction(START_VERSION_DOWNLOAD);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent!=null){
            String action= intent.getAction();
            if (START_VERSION_DOWNLOAD.equals(action)){
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                init(path, Config.DIRECTORY, File.separator + Config.VERSION_FILE_WEB, Config.getVersionLoadUrl());
            }
        }
    }


    /**
     * 下载融合
     *
     * @param path
     * @param directory
     * @param versionFile  版本文件名称: /version-ssl.txt
     * @param versionHost
     */
    private Cookie mCookie;

    public void init(String path, String directory, String versionFile, String versionHost) {
        mCookie = new Cookie(this);
        SyncVersion syncVersion = new SyncVersion(this);
        CountDownLatch latch = new CountDownLatch(1);

       File file = new File(path + File.separator + directory+File.separator+Config.VERSION_FILE_WEB);//判断version.txt文件是否存在，不能判断demo文件夹是否存在，以为demo文件夹更早会被创建
        if (!file.exists()) {//用户第一次下载文件会保存version版本文件，第二次直接download
            String mLocalVerionTxt = syncVersion.getDefaultVersionFile(directory, this.getApplicationContext());
            mCookie.setCookie(versionHost + versionFile, mLocalVerionTxt, 0);
            FileUtils.copyAssetsPath(directory, path + File.separator + directory, this);
        }
        /*
        * 如果用户清空缓存但是没删除数据的话。要重新在cookie注入versiontxt的数据
        * 优先读取文件下的version-ssl  如果没有则到assets读
        * */

       String cookie_versionTxt= mCookie.getCookie(versionHost + versionFile);
        if (cookie_versionTxt==null||cookie_versionTxt.equals("")){
                String version_txt="";
                if (file.exists()){
                    FileInputStream inputStream=null;
                    try {
                        inputStream = new FileInputStream(file);
                        byte[] b = new byte[inputStream.available()];
                        inputStream.read(b);
                        version_txt = new String(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            /*当version不存在时。走上面的方法*/
//                else {
//                    version_txt= syncVersion.getDefaultVersionFile(directory, this.getApplicationContext());
//                }

            mCookie.setCookie(versionHost + versionFile,version_txt,0);
        }
        // 创建.nomedia文件夹，优化文件中图片出现在相册中
        String sdPathHide = Environment.getExternalStorageDirectory() + "/demo/.nomedia";
        FileUtils.createDirFile(sdPathHide);

        download(path, directory, syncVersion, versionFile, versionHost, latch,cookie_versionTxt);

    }
    /*
    * @params localVersionInFo 读取cookie里面的version信息 用于下载文件时候的更新
    *
    * */
    private void download(String path, String directory, SyncVersion syncVersion, String versionFile, String versionHost, CountDownLatch latch,String localVersionInFo) {
        syncVersion.setStorePath(path + File.separator + directory);
        syncVersion.setLocalVersionInFo(localVersionInFo);
        syncVersion.getVersionList(latch, versionFile, versionHost);

    }

}
