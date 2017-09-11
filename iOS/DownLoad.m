//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "DownLoad.h"
#import "DesEncrypt.h"
#import "UZCacheManager.h"
#import "UZWebEntity.h"

#import <objc/runtime.h>

@import UZNetworkModule;
@import MJExtension;
@import UZUtilModule;
@import UZCategoryModule;

#define BLOCK_SAFE(block, ...) if (block) { block(__VA_ARGS__); }
static char UZCacheResultBlock;


@interface UZDownLoadQueue :NSOperationQueue
+ (UZDownLoadQueue *)getQueue;
@end

static UZDownLoadQueue * __queue;

@implementation UZDownLoadQueue

+ (UZDownLoadQueue *)getQueue
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        __queue = [[UZDownLoadQueue alloc] init];
        __queue.maxConcurrentOperationCount = 3;
    });
    return __queue;
}

@end

@interface DownLoad()


@end

@implementation DownLoad
+ (void)requestURL:(NSString *)requestURL
            params:(NSString *)version
        completion:(void (^)(id result))completion
{
    
    if (version) {
        requestURL = [requestURL stringByAppendingFormat:@"?ver=%@",version];
    }
    // 编码
    NSString *encode = [requestURL stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    // 构造URL
    NSURL *url = [NSURL URLWithString:encode];
    
    //使用operation queue
    NSBlockOperation *operation = [NSBlockOperation blockOperationWithBlock:^{
    
        NSError *downloadError = nil;
        NSData *imageData = [NSData dataWithContentsOfURL:url options:nil error:&downloadError];
        if (downloadError == nil) {
            if (imageData != nil) {
                id result = imageData;
                BLOCK_SAFE(completion,result);
            }
        } else {
            BLOCK_SAFE(completion,nil);
        }
    }];
    [[UZDownLoadQueue getQueue] addOperation:operation];

}

+ (void)loadHybridData:(NSString *)urlStr param:(NSDictionary *)paramDict finish:(void (^)(NSString * result))finishBlock{
    objc_setAssociatedObject(self, &UZCacheResultBlock, finishBlock, OBJC_ASSOCIATION_COPY);
    
    __weak __typeof(self) weakSelf = self;
    // NO 当前没网
    
    BOOL isReach = [UZCommonUtils getNetworkRechability];
    if (!isReach) {
        [self queryDataWithModelWithKey:[paramDict mj_JSONString] isCheckValid:NO resultStr:^(NSString *resultJsonStr) {
            BLOCK_SAFE(finishBlock,resultJsonStr);
            NSLog(@"html页面获得缓存数据");
        }];
        return;
    }
    BOOL isCache = NO;
    if ([paramDict[@"usecache"] integerValue] != 0) {
        isCache = YES;
        // 先查询数据库 检查是否有有效的数据
        [self queryDataWithModelWithKey:[paramDict mj_JSONString] isCheckValid:YES resultStr:^(NSString *resultJsonStr) {
            if (resultJsonStr) {
                BLOCK_SAFE(finishBlock,resultJsonStr);
                NSLog(@"html页面获得缓存数据");

                return;
                
            }else{
                [weakSelf requestData:urlStr param:paramDict isCache:isCache];
            }
        }];
        // 如果没有缓存 去请求
    }else{
        //不需要缓存
        [self requestData:urlStr param:paramDict isCache:isCache];
    }
    
}

+ (void)requestData:(NSString *)urlStr param:(NSDictionary *)paramDict isCache:(BOOL)isCache{
    
    void (^finishBlock)(NSString * result, BOOL isCache) = objc_getAssociatedObject(self, &UZCacheResultBlock);
    
    UZWebEntity * model = [[UZWebEntity alloc] init];
    model.param = [paramDict mj_JSONString];
    NSDate * expiredDate = [NSDate dateWithTimeIntervalSinceNow:[paramDict[@"usecache"] doubleValue]*60];
    model.timeInterval = [expiredDate timeIntervalSince1970];
    
    UZBusinessNetwork * network = [[UZBusinessNetwork alloc] init];
    NSMutableDictionary *params=[[NSMutableDictionary alloc]initWithDictionary:[UZCommonUtils getParamsList]];
    [params setValuesForKeysWithDictionary:paramDict[@"param"]];
    @weakify(self);
    [network postHybridWithAction:urlStr params:params finished:^(UZHttpResponse *response) {
        if ([response.MC integerValue] != -3) {
            
            NSString * jsonStr = response.Content;
            model.result = jsonStr;
            
            if (isCache && [response.MC integerValue] == 200) {
                // 保存数据
                //NSLog(@"回调 current %@",[NSThread currentThread]);
                [UZCacheManager async:^(id<UZCacheHandleProto> handler) {
                    //NSLog(@"插入 current %@",[NSThread currentThread]);
                    [handler insertModel:model];
                }];
            }
            
            BLOCK_SAFE(finishBlock,jsonStr,NO);
            //            NSLog(@"-----------------------实时");
            
        }else {
            // 如果因为网络请求失败 直接拼接json 返回给web
            [self_weak_ queryDataWithModelWithKey:[paramDict mj_JSONString] isCheckValid:NO resultStr:^(NSString *resultJsonStr) {
                BLOCK_SAFE(finishBlock,resultJsonStr,YES);
                NSLog(@"网络请求失败状态下html页面获得缓存数据");
                
            }];
            
            
        }
    }];
    
}




+ (void)queryDataWithModelWithKey:(NSString *)key isCheckValid:(BOOL)isCheck resultStr:(void(^)(NSString *resultJsonStr))resultJsonBlock{
    
    UZWebEntity * queryModel = [[UZWebEntity alloc] init];
    queryModel.param = key;
    
    [UZCacheManager async:^(id<UZCacheHandleProto> handler) {
        [handler queryModel:queryModel resultBlock:^(UZWebEntity *model) {
            if (isCheck) {
                
                if (!model.param) {
                    
                    BLOCK_SAFE(resultJsonBlock,nil);
                    return ;
                }
                // 拿到过期时间
                NSDate * expireDate = [NSDate dateWithTimeIntervalSince1970:model.timeInterval];
                // 如果负数 还没过期  正数 过期
                NSTimeInterval seconds = [[NSDate date] timeIntervalSinceDate:expireDate];
                if (seconds > 0) {
                    BLOCK_SAFE(resultJsonBlock,nil);
                }else{
                    BLOCK_SAFE(resultJsonBlock,model.result);
                    
                }
            }else{
                // 没网状态
                if (model.param) {
                    NSMutableDictionary * resultDict = [[model.result mj_JSONObject] mutableCopy];
                    [resultDict setValue:@(-3) forKey:@"ErrorCode"];
                    [resultDict setValue:@"网络连接失败，请重试" forKey:@"ErrorMsg"];

                    NSString * str = [resultDict mj_JSONString];
                    NSLog(@"%@",str);
                    BLOCK_SAFE(resultJsonBlock,[resultDict mj_JSONString]);
                    
                }else{
                    NSString * json = [@{@"ErrorCode":@(-3),@"ErrorMsg":@"网络连接失败，请重试",@"JsonResult":@{}} mj_JSONString];
                    BLOCK_SAFE(resultJsonBlock,json);
                }
            }
        }];
    }];
    
    
}

//获取资源更新数据
+ (void)testDownloadingFromCompletion:(void (^)(id result))completion {
    
    UZBusinessNetwork *network = [[UZBusinessNetwork alloc] init];
    NSMutableDictionary *params=[[NSMutableDictionary alloc]initWithDictionary:[UZCommonUtils getParamsList]];
    params[@"Path"] = @"http://msitelogic.uzai.com/api/";
    params[@"ControllerName"] = @"HybridVersion";
    params[@"ActionName"] = @"GetVersionFile";
    
    NSMutableDictionary *uploadDic = [[NSMutableDictionary alloc] init];
    if (isPreRelease) {
        uploadDic[@"CompiledVersion"] = @"release";
    } else {
        uploadDic[@"CompiledVersion"] = @"store";
    }
    [params setObject:uploadDic forKey:@"PostData"];
    
    @weakify(self);
    [network postHybridWithAction:@"/api/MobileCommon/RequestWebApi/544" params:params finished:^(UZHttpResponse * _Nonnull response) {
        if (response.MC.integerValue == 200) {
            if ([response.dataList isKindOfClass:[NSDictionary class]]) {
                NSDictionary *responseDic = response.dataList;
                NSString *errorCode = [responseDic[@"ErrorCode"] toString];
                if ([errorCode isEqualToString:@"200"]) {
                    NSDictionary *resultDic = [responseDic[@"JsonResult"] mj_JSONObject];
                    dispatch_async(dispatch_get_main_queue(), ^{
                        
                        [[NSUserDefaults standardUserDefaults] setObject:[resultDic[@"CRC32"] toString] forKey:[UZUserDefaultsKeys UZResourceLastCRCKey]];
                        [[NSUserDefaults standardUserDefaults] synchronize];
                        BLOCK_SAFE(completion,resultDic[@"Detail"]);
                    });
                } else {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        BLOCK_SAFE(completion,nil);
                    });
                }
            } else {
                BLOCK_SAFE(completion,nil);
            }
        }
    }];
}

//上传更新资源情况
+ (void)uploadDownloadResourceStatus:(NSDictionary *)updateInfo Completion:(void (^)(id result))completion {
    
    UZBusinessNetwork *network = [[UZBusinessNetwork alloc] init];
    NSMutableDictionary *params=[[NSMutableDictionary alloc]initWithDictionary:[UZCommonUtils getParamsList]];
    params[@"Path"] = @"http://msitelogic.uzai.com/api/";
    params[@"ControllerName"] = @"HybridVersion";
    params[@"ActionName"] = @"UpdateFeedback";
    
    NSMutableDictionary *uploadDic = [[NSMutableDictionary alloc] initWithDictionary:updateInfo];
    [uploadDic setObject:[[NSUserDefaults standardUserDefaults] objectForKey:[UZUserDefaultsKeys UZResourceLastCRCKey]] forKey:@"CRC32"];
    [uploadDic addEntriesFromDictionary:[UZCommonUtils getParamsList]];
    [params setObject:uploadDic forKey:@"PostData"];
    
    @weakify(self);
    [network postHybridWithAction:@"/api/MobileCommon/RequestWebApi/544" params:params finished:^(UZHttpResponse * _Nonnull response) {
        if (response.MC.integerValue == 200) {
            BLOCK_SAFE(completion,@"finish");
        }
    }];
}

// 解析H5页面的参数 内容中可能有链接 不能直接使用://分割
+ (NSDictionary *)getParamDictionary:(NSString *)requestString
{
    NSArray * arr = [requestString componentsSeparatedByString:@"//uzai?"];
    NSString * tempStr = arr.lastObject;
    NSDictionary * paramDict = [tempStr mj_JSONObject];
    return paramDict;
}

// 解析H5页面的参数 内容中可能有链接 不能直接使用://分割
+ (NSString *)getJsonParam:(NSString *)requestString
{
    NSArray * arr = [requestString componentsSeparatedByString:@"//uzai?"];
    if (arr.count == 1) {
        //        requestString	__NSCFString *	@"action.openhistory://"	0x1773f230
        //如果不带参数 那么参数返回@“”
        return @"";
    }
    NSString * tempStr = arr.lastObject;
    return tempStr;
}

- (void)dealloc
{
    NSLog(@"%@-----dealloc",NSStringFromClass([DownLoad class]));
}

@end
