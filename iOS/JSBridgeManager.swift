//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

import UIKit
import UZUtilModule
import UZMeidator
import UZAnalyticsModule

/**截取链接的方式与js进行交互*/
public class JSBridgeManager: NSObject {
    public weak var curWebView:UIWebView!

    var curVC:UIViewController? {
        
        get{
            return UZCommonUtils.getCurrentVC()
        }
    }
    var actionHandler: UZBridgeHandler?

    var actionManager = UZJSBridgeMethod()

    public override init() {
        super.init()
    }
    //处理链接的方法
    public func actionManager(request: NSURLRequest, actionCallBack: UZBridgeHandler) -> Bool{
        
        actionHandler = actionCallBack
        actionManager.curWebView = curWebView
        actionManager.actionHandler = actionHandler
        // 往localStorage 中写入必要参数
        self.setLocalStorageParam()
        
        var requestUrl = request.URL?.absoluteString?.stringByRemovingPercentEncoding
        let scheme = request.URL?.scheme?.lowercaseString
        guard let requestString = requestUrl else{
            return false
        }
        
        if scheme == "http" || scheme == "https" || scheme == "file" {
            return true
        }
        
        if scheme == "tel"{
            return true
        }
       
        // 分割参数
        let jsonParam = UZDownLoad.getJsonParam(requestString)
        
        if let _ = scheme {
            self.exec(scheme!, param: jsonParam)

        }
        return false
    }
    
    // 处理不同事件的入口
    func exec(action: String, param: String) {
        actionManager.actionManager(action, param: param)
    
    
    }
    
    public func setLocalStorageParam() {
        self.actionManager.setCookie("userid")
        self.actionManager.setCookie("phonetype")
        self.actionManager.setCookie("phoneversion")
        self.actionManager.setCookie("appversion")
        self.actionManager.setCookie("phoneid")
        self.actionManager.setCookie("cityid")

            }
    
    
    public func noNeedLoad(urlStr: String) -> Bool {
        if self.goBack(urlStr) {
            return true
        }else if (urlStr.containsString("https://buy.uzai.com/") && !urlStr.hasPrefix("action")){
            //判断跳转新旧预定页
            //infostr = "http://buy.uzai.com/touch_one/2016-10-22/1/0/209/168/145020/1781882.html?phoneid="
            let vc = UZMediatorManager.sharedInstance().UZMediator_webPlaceanOrderVC(urlStr as String, isHiddenButton: true)
            vc.title = "创建订单";
            self.curVC?.navigationController?.pushViewController(vc, animated: true)
            return true
        }
        return false
    }
    
    /// 判断是不是m站首页地址  如果是m站首页地址  那么返回上一页
    /// - Parameter urlStr: 加载地址
    /// - Returns: yes 是m站首页地址
    func goBack(urlStr: String) -> Bool {
        if let homeUrl = urlStr.componentsSeparatedByString("://").last {
            if homeUrl == "m.uzai.com" || homeUrl == "m.uzai.com/"{
                curVC?.navigationController?.popViewControllerAnimated(true)
                return true
            }
        }
        return false
        
    }
    
    ///是不是直接加载的地址  有些三方的地址 拼接参数之后会无法加载
    public func loadDirectly(urlStr: String) -> Bool {
        if urlStr.containsString("visa.uzai.com/appvisa") {
            return true
        }
        return false
    }
    
}
