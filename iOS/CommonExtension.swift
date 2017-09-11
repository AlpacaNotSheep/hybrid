//
//  QLURLNavigator.h
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

import UIKit
import JavaScriptCore

@objc public protocol JSContextWebViewDelegate : NSObjectProtocol{
    
    func webView(webView: UIWebView, didCreateJavaScriptContext context: JSContext)
}


@objc public  protocol QLFrame : NSObjectProtocol {
    
    func parentFrame() ->  NSObject?
}


public class GlobalWebViews: NSObject {

//    static let defaultInstance = GlobalWebViews()
    
    public var g_webViews: NSHashTable? = nil
    static let shareInstance:GlobalWebViews = {
        
        let instance = GlobalWebViews()
        instance.g_webViews = NSHashTable.weakObjectsHashTable()
        return instance
    }()
    
}



/**NSObject*/
public extension NSObject {
    
    public func webView(unuse:AnyObject , didCreateJavaScriptContext ctx: JSContext , forFrame frame:QLFrame) {
        
        
        if (frame.respondsToSelector(#selector(QLFrame.parentFrame)) && frame.parentFrame() != nil) {
            return
        }
        
        let aclosure: () -> Void = {() -> Void in
            guard let _webViews = GlobalWebViews.shareInstance.g_webViews else {
                return
            }
            
            let webViews = _webViews.allObjects
            for obj in  webViews{
                if let webView = obj as? UIWebView {
                    let cookie = String(format: "ts_jscWebView_%lud", UInt(webView.hash))
                    let script = String(format: "var \(cookie) = '\(cookie)'")
                    
                    webView.stringByEvaluatingJavaScriptFromString(script)
                    if let jsValue = ctx.objectForKeyedSubscript(cookie) {
                        if jsValue.toString() == cookie {
                            webView.didCreateJSContext(ctx)
                            return
                        }
                    }
                }
            }
        }
        
        if NSThread.isMainThread() {
            aclosure()
        }else{
            dispatch_async(dispatch_get_main_queue(), aclosure)
        }
    }
}



// objc_setAssociatedObject 绑定不成功
//objc_setAssociatedObject(self, kJavaScriptContext ,context , objc_AssociationPolicy.OBJC_ASSOCIATION_RETAIN_NONATOMIC)
let kJavaScriptContext = "javaScriptContext"

/**UIWebView*/
public extension UIWebView {
    
    private struct cs_associatedKeys {
        
        static var accpetJSContextKey  = "cs_accpetEventKey"
        static var acceptEventTime  = "cs_acceptEventTime"
        
    }
    
    public var javaScriptContext : JSContext? {
        get{
            let ctx = objc_getAssociatedObject( self, &cs_associatedKeys.accpetJSContextKey) as? JSContext
            
            return ctx
        }
        set {
            
        }
    }
    
    public func didCreateJSContext(context: JSContext){
        
        self.willChangeValueForKey(kJavaScriptContext)
        objc_setAssociatedObject(self, &cs_associatedKeys.accpetJSContextKey ,context , objc_AssociationPolicy.OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        self.didChangeValueForKey(kJavaScriptContext)
        guard let delegate = self.delegate as? JSContextWebViewDelegate else{
            return
        }
        delegate.webView(self, didCreateJavaScriptContext: context)
    }
    
}



