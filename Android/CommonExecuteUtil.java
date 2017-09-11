package com.uzai.app.hybrid;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.demo.app.apshare.ShareEntryActivity;
import com.demo.app.domain.ShareInfo;
import com.demo.app.hybrid.core.Config;
import com.demo.app.hybrid.logic.IndexWebActivity;
import com.demo.app.mvp.module.order.activity.OrderListNewActivity;
import com.demo.app.util.CookieUtil;
import com.demo.app.util.DialogUtil;
import com.demo.app.util.DownloadImageAsyncTask;
import com.demo.app.util.IKeySourceUtil;
import com.demo.app.util.OpenPageUtil;
import com.demo.app.util.VoidRepeatClickUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.CLIPBOARD_SERVICE;



public class CommonExecuteUtil {

    public static void commonExecute(Activity context, String key, String jsonValue, String currentGAPath) {
        String keyLowerCase = key.toLowerCase();
        if (keyLowerCase.contains("share")) {//分享
            if (!VoidRepeatClickUtil.isFastDoubleClick()) {
                ShareInfo shareInfo = JSON.parseObject(jsonValue, ShareInfo.class);
                //设置分享的数据
                CookieUtil.setShareData(context, 2,
                        shareInfo.getUrl(),
                        shareInfo.getImageUrl(),
                        shareInfo.getTitle(),
                        shareInfo.getUrl(),
                        shareInfo.getContent() + " " + shareInfo.getUrl() + " 快来看看");
                Intent intent = new Intent();//跳转到分享的界面
                intent.setClass(context, ShareEntryActivity.class);
                intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, "分享界面");
                context.startActivity(intent);
            }

        } else if (keyLowerCase.equalsIgnoreCase("openorderlist")) {//打开原生订单列表页面
            Intent intent = new Intent(context, OrderListNewActivity.class);
            intent.putExtra("orderListType", 0);
            if (!TextUtils.isEmpty(currentGAPath))
                intent.putExtra(IKeySourceUtil.GA_FROM_FLAG, currentGAPath + "->全部订单列表页");
            context.startActivity(intent);

        } else if (keyLowerCase.contains(Config.OPEN_PAGE) || keyLowerCase.contains(Config.OPEN_SEARCHLIST)) {//打开native页面 或者打开搜索页面
            OpenPageUtil.openCommonPage((Activity) context, key, jsonValue);
        } else if (keyLowerCase.contains("go.back")) {
            try {
                JSONObject jsonObject = new JSONObject(jsonValue);
                JSONObject jsonClassInfo = jsonObject.getJSONObject("ClassInfo");
                String className = jsonClassInfo.getString("ClassName");
                if (!TextUtils.isEmpty(className)) {
                    Class concreteClass = Class.forName(className);
                    Intent intent = new Intent(context, concreteClass);
                    context.startActivity(intent);
                }
                if ("true".equals(jsonClassInfo.getString("isLastPage"))||"1".equals(jsonClassInfo.getString("isLastPage"))) {
                    context.finish();
                } else if ("true".equals(jsonClassInfo.getString("isRootPage"))||"1".equals(jsonClassInfo.getString("isRootPage"))) {
                    context.finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //快递单号copy操作
        else if (keyLowerCase.contains("action.copy")) {
            try {
                JSONObject jsonObject = new JSONObject(jsonValue);
                String copyContent = jsonObject.getString("Content");
                if (!TextUtils.isEmpty(copyContent)) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData textCd = ClipData.newPlainText("kkk", copyContent);
                    clipboard.setPrimaryClip(textCd);
                    DialogUtil.toastForShort(context, "复制成功！");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }//保存图片到相册
        else if (keyLowerCase.contains("action.savealbum")) {
            try {
                JSONObject jsonObject = new JSONObject(jsonValue);
                String imgUrl = jsonObject.getString("Content");
                if (!TextUtils.isEmpty(imgUrl)) {
                    new DownloadImageAsyncTask(context).execute(imgUrl);
                }
            } catch (Exception e) {
                DialogUtil.toastForShort(context, "保存失败");
                e.printStackTrace();
            }
        }
    }
}
