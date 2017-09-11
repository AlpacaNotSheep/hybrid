//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//
#import "QLURLNavigator.h"
#import "NSObject+Common.h"

@import MJExtension;
@import UZUtilModule;
@import UZCategoryModule;
#define BLOCK_SAFE(block, ...) if (block) { block(__VA_ARGS__); }

@interface QLURLNavigator ()

@end

@implementation QLURLNavigator
+ (QLURLNavigator *)defaultNavigator
{
    static QLURLNavigator  * __navigator;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        __navigator = [[QLURLNavigator alloc] init];
    });
    return __navigator;
}

- (void)handlerUrl:(NSString *)jsonStr controller:(URLControllerHandler)controllerHandler tabBar:(URLTabBarHandler)barHandler webView:(URLWebViewHandler)webViewHandler
{
    NSDictionary * param = [jsonStr mj_JSONObject];
    NSDictionary * classDict = param[@"ClassInfo"];
    NSString * className = classDict[@"ClassName"];
    NSString *cocoaPodName = classDict[@"cocoaPodName"];
    BOOL isStoryBoard = [classDict[@"isStoryBoard"] boolValue];
    BOOL isXib = [classDict[@"isXib"] boolValue];//添加是否是xib
    NSString * storyBoardIdentifier = classDict[@"storyBoardIdentifier"];
    
    NSString * url = param[@"Url"];
    BOOL isTabBar = [param[@"IsTabBar"] boolValue];
    NSDictionary * content = param[@"Content"];

    if (url.length) {
        dispatch_async(dispatch_get_main_queue(), ^{
            // 将链接 参数 和ga传递出去
//            NSMutableDictionary * tempDict = [[NSMutableDictionary alloc] initWithDictionary:content];
//            [tempDict setObject:param[@"GA"] forKey:@"GA"];
//            [tempDict setObject:param[@"IsNav"] forKey:@"IsNav"];
//            [tempDict setObject:url.stringByRemovingPercentEncoding forKey:@"Url"];
            BLOCK_SAFE(webViewHandler,param);
        });
    }else if (isTabBar) {
        //如果是TabBar切换
        NSInteger tabBarIndex = [param[@"TabBarIndex"] integerValue];
       UINavigationController * currentNav = [self getCurrentNavigatorController:tabBarIndex];
        if (barHandler) {
            barHandler(currentNav, tabBarIndex);
        }
    }else{
        id baseVC;

        if (isStoryBoard) {
#warning warning
            UIStoryboard *storyBoard;
            if ([UIStoryboard storyboardWithName:className bundle:[NSBundle bundleWithIdentifier:[NSString stringWithFormat:@"org.cocoapods.%@",cocoaPodName]]]) {
                storyBoard = [UIStoryboard storyboardWithName:className bundle:[NSBundle bundleWithIdentifier:[NSString stringWithFormat:@"org.cocoapods.%@",cocoaPodName]]];
                baseVC = [storyBoard instantiateViewControllerWithIdentifier:storyBoardIdentifier];
            }else
            {
                return;
            }
        }else if(isXib){
            Class class = [self aClassFromString:className cocoapodsName:cocoaPodName];
            if (!class) {
                return;
            }
            baseVC = [[class alloc] initWithNibName:className bundle:[NSBundle bundleWithIdentifier:[NSString stringWithFormat:@"org.cocoapods.%@",cocoaPodName]]];
        }else{
            Class class = [self aClassFromString:className cocoapodsName:cocoaPodName];
            if (!class) {
                return;
            }
            baseVC = [[class alloc]init];
        }
        if ([baseVC isKindOfClass:[UIViewController class]]) {
            [baseVC fillWithParams:content];
            if (controllerHandler) {
                controllerHandler(baseVC, param);
            }
        }
    }
}

-(Class)aClassFromString:(NSString *)className cocoapodsName:(NSString *)cocoapodsName
{
    if (className&&cocoapodsName) {
        NSString *newClassName= [NSString stringWithFormat:@"%@.%@",cocoapodsName,className];
        if (NSClassFromString(newClassName)) {
          return NSClassFromString(newClassName);
        }
        return  NSClassFromString(className);
    }
    return nil;
}





- (void)openSearch:(NSString *)jsonStr param:(URLSearchHandler)searchHandler
{
    NSDictionary * param = [jsonStr mj_JSONObject];
    if (searchHandler) {
        searchHandler(param);
    }
}

- (UINavigationController *)getCurrentNavigatorController:(NSInteger)index
{
    UITabBarController * tabBar = [[(NSObject *)[UIApplication sharedApplication].delegate valueForKey:@"window"] valueForKey:@"rootViewController"];
//    tabBar.selectedIndex = index;
    UINavigationController * currentNav = (UINavigationController *)tabBar.selectedViewController;
//    [currentNav popToRootViewControllerAnimated:YES];
    return currentNav;
}

//- (void)setServiceWithTarget:(id)target param:(NSDictionary *)content
//{
//
//    NSArray * keys = [content allKeys];
//    for (NSString * key in keys) {
//        
//        if ([content[key] isKindOfClass:[NSNumber class]]) {
//            NSString * obj = [content[key] stringValue];
//            [target setValue:obj forKey:key];
//            continue;
//        }
//        [target setValue:content[key] forKey:key];
//    }
//}




//go.back://{\"ClassInfo\":{\"ClassName\":\"\",\"isLastPage\":true,\"isRootPage\":false},\"GA\":\"启动页->上海站首页->发现首页->发现频道产品详情页\"}

- (void)backHandlerUrl:(NSString *)jsonStr
              lastPage:(void(^)(BOOL islastPage,BOOL isRootPage))lastPageHandler
            controller:(void(^)(UIViewController * controller))controllerHandler
{
    NSString * json = jsonStr.stringByRemovingPercentEncoding;
    NSDictionary * param = [json mj_JSONObject];
    param = param[@"ClassInfo"];
    BOOL isLast = [param[@"isLastPage"] boolValue];
    BOOL isRoot = [param[@"isRootPage"] boolValue];
    BLOCK_SAFE(lastPageHandler,isLast,isRoot);
    if (!isLast && !isRoot) {
        NSString * className = param[@"ClassName"];
        Class class = NSClassFromString(className);
        id  baseVC = [[class alloc] init];
        if ([baseVC isKindOfClass:[UIViewController class]]) {
            BLOCK_SAFE(controllerHandler,baseVC);
        }
    }
    
}
//- (objc_property_t)getProperty:(id)target key:(NSString *)key
//{
//    unsigned int propertyCount = 0;
//    objc_property_t *properties = class_copyPropertyList([target class], &propertyCount);
//    
//    for (unsigned int i = 0; i < propertyCount; ++i) {
//        objc_property_t property = properties[i];
//        const char * name = property_getName(property);//获取属性名字
//        
//        const char * attributes = property_getAttributes(property);//获取属性类型
//    }
//    return nil;
//}
@end














