//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//
/*
 *逻辑整理
 *1.判断沙盒是否存在文件，不存在从工程中拷贝
 *2.通过http://mdingzhi.uzai.com/version.txt判断是否有更新
 *3.下载需要更新数据，有更新的数据进行覆盖
 */
#import <Foundation/Foundation.h>
#import "CommUtils.h"
#import "DataEntity.h"
#import "DownLoad.h"
#import "ReplaceEntity.h"
// 上一版版本文件列表
#define KBridtextLists @"hybridTextList"
// 上一版HTML版本文件列表
#define KBridHTMLList @"hybridhtmlList"
#define KBridVersion @"1"// version.txt文档中的字段
#define kHybridVersion @"3" //与ybridConstant 的 HybridVersion 保持一致 用于区别js的交互方式

#if DEBUG
#define isDebug 1
#else
#define isDebug 0
#endif

//打包工具使用debug模式归档，1-(资源的)debug包 0-release包

#define KWEBNURL @"https://r03.uzaicdn.com/content/hybrid/version"
typedef void (^LoadVersionCompleteBlock) (BOOL isSuccess,NSError *error);
@interface DataBase : NSObject
+ (instancetype)sharedInstance;//返回单利
+ (void)enableLogging;//是否打印
/*
 *数据请求
 */
- (void)requestUZStateBlock:(LoadVersionCompleteBlock)_block;

@property (nonatomic,assign,readonly)int flag;//网络标记判断是否文件更新完成
@property (nonatomic,assign,readonly)int failFlag;//网络标记判断是否文件更新失败
@property (nonatomic,strong,readonly)NSArray *httpSources;//所有请求到的网络资源
@end
