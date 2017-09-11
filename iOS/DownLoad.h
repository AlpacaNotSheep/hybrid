//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>
#if DEBUG
#define isPreRelease 1
#else
#define isPreRelease 0
#endif

//0-App Store，1-上线前最后一个包 
@interface DownLoad : NSObject
/* 处理方式 */
// 1 NSURLConnection 异步
// 2 回调方法，刷新UI

/* 参数 */
// 1 判断请求方式
// 2 URL
// 3 请求参数
// 4 completion

//+ (void)requestURL:(NSString *)requestURL
//            method:(NSString *)method
//            params:(NSDictionary *)params
//        completion:(void (^)(id result))completion;

+ (void)requestURL:(NSString *)requestURL
            params:(NSString *)version
        completion:(void (^)(id result))completion;

+ (void)loadHybridData:(NSString *)urlStr param:(NSDictionary *)paramDict finish:(void (^)(NSString * result))finishBlock;
// 解析H5页面的参数
+ (NSDictionary *)getParamDictionary:(NSString *)requestString;
// 获取H5链接中的json参数
+ (NSString *)getJsonParam:(NSString *)requestString;

+ (void)testDownloadingFromCompletion:(void (^)(id result))completion;

+ (void)uploadDownloadResourceStatus:(NSDictionary *)updateInfo Completion:(void (^)(id result))completion;

@end
