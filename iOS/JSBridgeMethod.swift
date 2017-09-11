//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

import UIKit
import SDWebImage
import UZCategoryModule
import MBProgressHUD
import AFNetworking
import UZShareModule
import UZMeidator
import UZUtilModule
import UZSQLModule
import UZNetworkModule
import EventKit
import UZAppDelegateModule
public typealias BridgeHandler = (action:String, param:String) -> Void

public class JSBridgeMethod: NSObject {
    weak var curWebView: UIWebView?
    
    weak var curVC:UIViewController? {
        
        get{
            return UZCommonUtils.getCurrentVC()
        }
    }
    var actionHandler: BridgeHandler?
    
    
    public func actionManager(action: String, param: String) {
        print("---------\(action)---------")
        print("---------\(param)---------")

        // 所有标识符都用小写！！！！！
        let actionLower = action.lowercaseString
        switch actionLower {
        // common response
        case "go.back":
            self.goBack(param)
            
        case "action.invoke":// 这个方法一定会调用，如果页面无法显示数据，看下请求是否正常
            self.loadData(param)
            
        case "open.page":
            self.openViewController(param)
            
        case "action.copy":
            self.copyToPasteBoard(param)
            
        case "action.share":
            self.share(param)
            
        case "action.getfile":
            self.getSubFile(param)

        case "action.login":
            self.login(actionLower, jsonParam: param)
            
        // 以下回调到当前类中执行
        
        case "go.payback":
            self.payGoBack(actionLower, jsonParam: param)
            
        case "action.pay":
            self.callPay(actionLower, jsonParam: param)
     
            
        default:
            return
        }
        
    }
    
    
    // 返回
    func goBack(jsonStr : String) {
        QLURLNavigator.defaultNavigator().backHandlerUrl(jsonStr, lastPage: { (islastPage, isRootPage) in
            
            dispatch_async(dispatch_get_main_queue(), {
                self.curWebView?.removeFromSuperview()
                self.curWebView = nil
                if islastPage{
                    self.curVC?.navigationController?.popViewControllerAnimated(true)
                }else{
                    self.curVC?.navigationController?.popToRootViewControllerAnimated(true)
                }
            })
        }) { (controller) in
            dispatch_async(dispatch_get_main_queue(), {
                self.curWebView?.removeFromSuperview()
                self.curWebView = nil
                self.curVC?.navigationController?.popToViewController(controller, animated: true)
            })
        }
    }
    
    // 复制到剪切板
    func copyToPasteBoard(jsonStr : String){
        guard let paramDict = toJSONObject(jsonStr) else{
            return
        }
        let content = paramDict["Content"] as? String
        if content != nil {
            dispatch_async(dispatch_get_main_queue(), {
                UIPasteboard.generalPasteboard().string = content
                MBProgressHUD.showSuccess("复制成功", toView: self.curVC?.view)
            })
            
        }
    }
    
    //保存到相册
    func saveAblum(jsonStr : String) {
        guard let paramDict = toJSONObject(jsonStr) else{
            return
        }
        let imageUrl = paramDict["Content"] as? String
        let urlStr = NSURL(string: imageUrl!)
        
        SDWebImageManager.sharedManager().downloadImageWithURL(urlStr!, options: SDWebImageOptions.RetryFailed, progress: { (a, b) in
            
            }, completed: { (image, error, cacheTyPE, isSuccess, url) in
                dispatch_async(dispatch_get_main_queue(), {
                    if error == nil{
                        UIImageWriteToSavedPhotosAlbum(image, self, Selector(self.saveAlbumSuccess()),nil)
                    }else{
                        MBProgressHUD.showSuccess("保存图片失败", toView: self.curVC?.view)
                    }
                })
                
        })
    }
    
    
    //保存相册成功
    func saveAlbumSuccess() {
        MBProgressHUD.showSuccess("成功保存图片到相册", toView: self.curVC?.view)
    }
    
    //加载数据
    func loadData(jsonStr : String) {
        if let paramDict = toJSONObject(jsonStr) {
            let curIndex = paramDict["index"] as! NSNumber
            UZDownLoad.loadHybridData(paramDict["url"] as! String, param: paramDict, finish: { (result) in
                dispatch_async(dispatch_get_main_queue(), {
                    //                    guard let context = self.curWebView?.javaScriptContext else{
                    //                        return
                    //                    }
                    //                    //积分商城 不能正常展示数据 疑似多个iFrame中的JSContext问题
                    //                    let jsValue =  context.evaluateScript(jsMessage)
                    self.insertCalendarHandler(result, param: paramDict)
                    let jsMessage = "api.callback('\(curIndex)',\(result))"
//                    print("===========\(jsMessage)===========")
//                    print("===========\(jsMessage.stringByRemovingPercentEncoding)================")

                    self.curWebView?.stringByEvaluatingJavaScriptFromString(jsMessage)
                    
                })
            })
        }
        
    }
    
    //打开某个页面
    func openViewController(jsonStr:String){
        guard let json = jsonStr.stringByRemovingPercentEncoding else{
            return
        }
        
        QLURLNavigator.defaultNavigator().handlerUrl(json, controller: { (vc, param) -> Bool in
            
            //            let rootNav = self.curVC?.navigationController
            //            let topVC = rootNav?.viewControllers.last
            //            guard topVC != nil else{
            //                return false
            //            }
            //            if ((topVC?.isKindOfClass(vc.classForCoder)) == true){
            //                return false
            //            }else{
            vc.hidesBottomBarWhenPushed = true
            self.curVC?.navigationController?.pushViewController(vc, animated: true)
            //            }
            
            return true
            }, tabBar: { (curNav, tabBarIndex) -> Bool in
                //curNav?.popToRootViewControllerAnimated(true) 放第一行会无tabbar
                
                let tabBarController = UZCommonUtils.getTabbar()
                tabBarController?.selectedIndex  = tabBarIndex
                curNav?.popToRootViewControllerAnimated(false)
                
                return true
        }) { (params) -> Bool in
            
            let GAStr = params["GA"]
            let isNav = params["IsNav"] as? NSInteger ?? 1
            var isShare = 1
            if let contentDict =  params["Content"] as? NSDictionary{
                if let shareFlag = contentDict["isShare"] as? String {
                    isShare = shareFlag.integerValue
                }
            }
            guard let url = params["Url"]  as? String else{
                return false
            }
            let controller = UZMediatorManager.sharedInstance().UZMediator_webHomeActiveVC(url, isShare: isShare, isShowNav: isNav, shareContent: "", shareTitle: "", shareImage: "", shareUrl: "")
            controller.hidesBottomBarWhenPushed = true
            self.curVC?.navigationController?.pushViewController(controller, animated: true)
            return true
            
        }
    }
    //分享
    func share(jsonParam: String) {
        let paramDict = toJSONObject(jsonParam)
        if paramDict != nil {
            
            dispatch_async(dispatch_get_main_queue(), {
                UZShareManager.shareWithtitle(paramDict!["Title"] as? String, text: paramDict!["Content"] as? String, andImage: paramDict!["ImageUrl"] as?String, andURL: paramDict!["Url"] as? String, andGACategory: "", withSuccessBlock: nil)
            })
        }
    }
    
    // 获取指定子页面的内容
    func getSubFile(jsonParam: String) {
        let paramDict = toJSONObject(jsonParam)
        guard let param = paramDict else{
            return
        }
        
        let urlStr = param["url"] as! String
        let ver = UZCommUtils.getCurrentTimeString()
        let viewName = param["viewName"] as? String
        
        // 是否直接读取线上
        let onLine = param["online"] as! String
        if onLine  == "1"{
            self.getSubFileContent(urlStr, ver: ver, viewName: viewName!)
            
        }else{
            UZCommUtils.getLocalFile(urlStr, absolutePath: { (isExist, absolutePath) in
                if isExist{
                    guard let htmlStr = try? String(contentsOfFile: absolutePath, encoding: NSUTF8StringEncoding)else{
                        // 如果拿不到本地文件 或者本地路径错误 则获取线上文件
                        self.getSubFileContent(urlStr, ver: ver, viewName: viewName!)
//                        assert(false, "拿不到本地文件内容")
                        print("拿不到本地文件内容")
                        return
                    }
                    
                    var code:String
                    var msg: String
                    if UZCommonUtils.getNetworkRechability(){
                        
                        code = "200"
                        msg = "文件获取成功"
                    }else{
                        
                        code = "-3"
                        msg = "网络连接失败，请重试"
                        
                    }
                    guard let content = htmlStr.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding) else{
                        print("获取本地文件内容编码失败")
                        return
                    }
                    
                    let resultDict = ["ErrorCode":code,"ErrorMsg":msg,"JsonResult":content]
                    let jsonResult = toJSONString(resultDict)
                    dispatch_async(dispatch_get_main_queue(), {
                        print("------->api.routerCallback('\(viewName!)',\(jsonResult))<-------")
                        // 编码回调给js
                        self.curWebView?.stringByEvaluatingJavaScriptFromString("api.routerCallback('\(viewName!)',\(jsonResult))")
                    })
                }else{
                    
                    self.getSubFileContent(urlStr, ver: ver, viewName: viewName!)
                    
                }
            })
            
        }
        
    }
    
    func getSubFileContent(urlStr: String,ver: String , viewName: String) {
        UZDownLoad.requestURL(urlStr, params: ver, completion: { (results) in
            guard let aData = results as? NSData else{
                let resultDict = ["ErrorCode":"-4","ErrorMsg":"请求服务器失败，请重试","JsonResult":""]
                let jsonResult = toJSONString(resultDict)
                dispatch_async(dispatch_get_main_queue(), {
                    
                    // 编码回调给js
                    self.curWebView?.stringByEvaluatingJavaScriptFromString("api.routerCallback('\(viewName)',\(jsonResult))")
                })
                return
            }
            guard let htmlStr = String(data: aData, encoding: NSUTF8StringEncoding) else{
//                assert(false, "内容没有正常转化成字符串")
                print("内容没有正常转化成字符串")
                return
            }
            // 编码回调给js
            guard let content = htmlStr.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding) else{
//                assert(false, "获取本地文件内容编码失败")
                print("获取本地文件内容编码失败")
                return
            }
            let resultDict = ["ErrorCode":"200","ErrorMsg":"文件获取成功","JsonResult":content]
            let jsonResult = toJSONString(resultDict)
            dispatch_async(dispatch_get_main_queue(), {
                // 编码回调给js
                let js = "api.routerCallback('\(viewName)',\(jsonResult))"
                print("---------------api.routerCallback\(js)=================")
                self.curWebView?.stringByEvaluatingJavaScriptFromString(js)
            })
            
        })
    }

    //登录
    func login(action: String , jsonParam: String){

        //        self.action(action, jsonParam: jsonParam)
        //判断是否登录
        let paramDict = toJSONObject(jsonParam)
        let forwordUrl = paramDict?["forword"] as? String
        let fromUrl = paramDict?["from"] as? String
        
        if UZClient.sharedInstance().userID == nil {
            UZMediatorManager.sharedInstance().UZMediator_PresentToLoginNavVCWithParams(nil, withSuccessBlock: {
                // 重新请求，刷新页面
                guard  forwordUrl?.length != 0 else{
                    return
                }
                self.componentHybridPath(forwordUrl!)
                }, andCancleBlock: {
                    guard  fromUrl?.length != 0 else{
                        return
                    }
                    self.componentHybridPath(fromUrl!)
            })
        }
        
    }
    func componentHybridPath(loadPath: String){
        let isExist = UZCommUtils.componentHybridPath(loadPath) { (request) in
            dispatch_async(dispatch_get_main_queue(), {
                self.curWebView?.loadRequest(request)
            })
        }
        
        if isExist == false {
            //  如果不存在本地页面 并且断网 显示错误页面
            if !UZCommonUtils.getNetworkRechability() {
                dispatch_async(dispatch_get_main_queue(), {[weak self] in
                    self?.curVC?.showLoadFailedWithFrame(CGRectMake(0, 0, UZCommonVariables.kMainScreenWidth, UZCommonVariables.kMainScreenHeight), andBackBlock: {
                        self?.curVC?.navigationController?.popViewControllerAnimated(true)
                    })
                })
                
            }
            
        }
    }
    //支付
    func callPay(action: String , jsonParam: String) {
        self.action(action, jsonParam: jsonParam)
    }
    
    // 打开通讯录获取联系人
    func getAddress(action: String , jsonParam: String) {
        self.action(action, jsonParam: jsonParam)
        self.curVC?.presentAddressBookVC()
    }

    // 支付页面的返回
    func payGoBack(action:String ,jsonParam: String) {
        self.action(action, jsonParam: jsonParam)
        
    }
    
    
    //具体执行的代码
    func action(action:String ,jsonParam: String) {
        guard let json = jsonParam.stringByRemovingPercentEncoding else{
            return
        }
        dispatch_async(dispatch_get_main_queue()) {
            self.actionHandler?(action: action, param: json)
            
        }
    }
    
    func setCookie(key:String) {
            var value  = ""
            let uzClient = UZClient.sharedInstance()
            switch key {
            case "phonetype":
//                value = UIDevice.currentDevice().model
                value = UZCommonUtils.getCurrentDeviceModel().lowercaseString
            case "phoneversion" , "appversion":
                let infoDict = NSBundle.mainBundle().infoDictionary
                if let infoDic = infoDict {
                    if let version = infoDic["CFBundleShortVersionString"] as? String {
                        value = version
                    }
                }
            case "phoneid":
                value = UZCommonUtils.getDevicephoneId()
            case "cityid":
                
                if let cityID =  UZCommonUtils.getStartCityID(){
                    value = "\(cityID)"
                }else{
                    value = "1"
                }
                
            case "cityname":
                if let cityName = uzClient.cityName   {
                    //城市名
                    value = cityName
                }
            case "devicetype":
                value = "ios"
            case "systemversion":
                value = UIDevice.currentDevice().systemVersion
            case "hybridversion":
                value = HybridConstant.HybridVersion
            case "userid":
                if let userid = uzClient.userID {
                    value = userid
                }
            default:
                value = ""
            }
            
        self.setLocalStorageJS(key, valueT: value)

        
    }
    func setLocalStorageJS(key: String, valueT: String)  {
        var value = valueT
        print("\(key):\(value)")
        var tempDict = [String:AnyObject]()
        tempDict["expireTime"] = 0
        if value == "" {
            if let valueT =  NSUserDefaults.standardUserDefaults().objectForKey(key) as? String {
                value = valueT
            }
        }else{
            tempDict["value"] = value
            value = toJSONString(tempDict)
        }
        dispatch_async(dispatch_get_main_queue(), {
            
            let js = "localStorage.setItem('\(key)','\(value)')"
            self.curWebView?.stringByEvaluatingJavaScriptFromString(js)
        })
    }
    
    ///插入日历事件所需的参数以及回调
    func insertCalendarHandler(result: String, param: [String:AnyObject]) {
        if let paramDic = param["param"] as? [String:AnyObject] {
            let actionName = UZAnyToString(paramDic["ActionName"])
            let controllerName = UZAnyToString(paramDic["ControllerName"])
            if let postDataDic = paramDic["PostData"] as? [String:AnyObject] {
                let goDateString = UZAnyToString(postDataDic["GoDate"])
                if let resultDict = toJSONObject(result) {
                    if UZAnyToString(resultDict["ErrorCode"]) == "200" {
                        let orderCode = UZAnyToString(resultDict["JsonResult"])
                        //判断actionname controller 出发日期 返回的订单号
                        if actionName == "InsertOrder" &&
                            controllerName == "UzaiOrder" &&
                            goDateString.length != 0 &&
                            orderCode.hasPrefix("C") {
                            if let modifiedOrderCode = orderCode.componentsSeparatedByString("_").first {
                                
                                //插入日历操作
                                let eventStore = EKEventStore()
                                eventStore.requestAccessToEntityType( EKEntityType.Event, completion:{(granted, error) in
                                    if (granted) && (error == nil) {
                                        
                                        let dateFormatter = NSDateFormatter()
                                        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss";
                                        var startTime = dateFormatter.dateFromString("\(goDateString) 10:00:00")
                                        var endTime = dateFormatter.dateFromString("\(goDateString) 16:00:00")
                                        startTime = startTime?.dateByAddingTimeInterval(-60 * 60 * 24)
                                        endTime = endTime?.dateByAddingTimeInterval(-60 * 60 * 24)
                                        
                                        let alarm = EKAlarm()
                                        alarm.absoluteDate = startTime
                                        let event = EKEvent(eventStore: eventStore)
                                        event.title = "明日出游，请提前做好出行准备"
                                        event.startDate = startTime!
                                        event.endDate = endTime!
                                        event.addAlarm(alarm)
                                        event.calendar = eventStore.defaultCalendarForNewEvents
                                        
                                        var event_id = ""
                                        do{
                                            try eventStore.saveEvent(event, span: .ThisEvent)
                                            event_id = event.eventIdentifier
                                            
                                            var newDic = NSMutableDictionary()
                                            newDic[modifiedOrderCode] = event_id
                                            if let olderDic = UZCommonUtils.readJsonFileWithName("CalendarReminderList") as? [String : AnyObject] {
                                                newDic.addEntriesFromDictionary(olderDic)
                                            }
                                            UZCommonUtils.saveDictionary(newDic, andName: "CalendarReminderList")
                                        }
                                        catch let error as NSError {
                                            print("json error: \(error.localizedDescription)")
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}
