//
//  QLURLNavigator.h
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>
typedef BOOL(^URLControllerHandler)(UIViewController * controller, NSDictionary * params);
typedef BOOL(^URLTabBarHandler)(UINavigationController * currentNav, NSInteger tabBarIndex);
typedef BOOL(^URLWebViewHandler)(NSDictionary * params);
typedef BOOL(^URLSearchHandler)(NSDictionary * param);

@interface QLURLNavigator : NSObject

+ (QLURLNavigator *)defaultNavigator;

/**
 *  三种类型的跳转
 *
 *  @param jsonStr           original json
 *  @param controllerHandler  controller
 *  @param barHandler         Tabbar[index]
 *  @param webViewHandler     webView
 */
- (void)handlerUrl:(NSString *)jsonStr
        controller:(URLControllerHandler)controllerHandler
            tabBar:(URLTabBarHandler)barHandler
           webView:(URLWebViewHandler)webViewHandler;

// 跳转到搜索页
- (void)openSearch:(NSString *)jsonStr param:(URLSearchHandler)searchHandler;


/**
 *  返回上一级 上级某一页面 栈底
 *
 *  @param jsonStr           跳转参数
 *  @param lastPageHandler   lastPageHandler description
 *  @param controllerHandler 上级中间页面
 */
- (void)backHandlerUrl:(NSString *)jsonStr
              lastPage:(void(^)(BOOL islastPage,BOOL isRootPage))lastPageHandler
            controller:(void(^)(UIViewController * controller))controllerHandler;

@end
