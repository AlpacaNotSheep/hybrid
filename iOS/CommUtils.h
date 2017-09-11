//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>
#define KROOTFILENAME @"uzai"
#define KROOTDIRECTORY @"WebViewCache"

@interface CommUtils : NSObject

/**
 获取document路径
 
 @return document路径
 */
+ (NSString *)documentPath;
/*
 *网页资源文件根文件路径
 */
+(NSString*)cachesPath;
/*
 *文件路径不存在的目录则去创建
 */
+(NSString*)writeToLibraryCachePath: (NSString*) fileName;
/**
 *  获取文件路径
 *
 */
+ (NSString *)getLibraryCachePath:(NSString *)fileName;
/*
 *工程文件路径
 */
+(NSString*)sourcePath: (NSString*) fileName;
/*
 *从网上获取文件路径
 */
+(NSString*)convertLocalPathWithUrl: (NSString*) fileName;
/*
 *文件是否存在
 */
+(BOOL)fileExistsAtPath: (NSString*) fileName;
/*
 *文件是否存在  并且是否是目录
 */
+(BOOL)fileExistsAtPath: (NSString*) fileName isDirectory: (BOOL *)isDirectory;
/*
 *文件拷贝
 */
+ (BOOL)copyMissingFile:(NSString *)sourcePath toPath:(NSString *)toPath;
/*
 *删除Document目录下文件
 */
+ (BOOL)deleteDocumentFile:(NSString *)fileName;
/*
 *获取当前时间
 */
+ (NSString *)getCurrentTimeString;

/*
 *返回网页代码
 */
+ (NSString *)stringWithContentsOfFile:(NSString *)fileName;

/*
 * @brief 把格式化的JSON格式的字符串转换成字典
 * @param jsonString JSON格式的字符串
 * @return 返回字典
 */
//+ (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString;

/**
 *  判断本地是否存在下载的指定文件 如果存在则返回绝对路径
 *
 *  @param BOOL isExist description
 *
 *  @return isExist
 */
+ (void)getLocalFile:(NSString *)localPath absolutePath:(void (^)(BOOL isExist , NSString * localPath))pathBlock;

/**
 *  判断本地是否存在下载文件
 *
 *  @param loadPath     loadPath description
 *  @param requestBlock requestBlock description
 *
 *  @return return value description
 */
+ (BOOL)componentHybridPath:(NSString *)loadPath request:(void (^)(NSURLRequest * request))requestBlock;


/**
 *  截取链接
 *
 *  @param urlString urlString description
 *
 *  @return 链接中的参数
 */
+(NSArray *)subStringWithUrl:(NSString *)urlString;

/**
 判断网址是否需要拼接参数

 @param request request description
 @return yes需要
 */
+ (BOOL)isNeedAppendBaseParam:(NSURLRequest *)request;

@end
