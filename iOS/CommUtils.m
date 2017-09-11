//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "CommUtils.h"
#import "UZHybridModule/UZHybridModule-Swift.h"
@import UZUtilModule;
@import UZCategoryModule;
@import UZNetworkModule;

// 上一版HTML版本文件列表
#define KBridHTMLList @"hybridhtmlList"
#define BLOCK_SAFE(block, ...) if (block) { block(__VA_ARGS__); }
@implementation CommUtils

#pragma mark -文件操作

/**
 获取document路径

 @return document路径
 */
+ (NSString *)documentPath {
    NSArray * paths  = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString * documentsDirectory = ([paths count] > 0) ? [paths objectAtIndex:0] : nil;
    return documentsDirectory;
}

//根文件路径
+ (NSString *)cachesPath {
    // 缓存写到library/Caches 目录下
//    NSArray   *paths  = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSArray * paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString * documentsDirectory = ([paths count] > 0) ? [paths objectAtIndex:0] : nil;
    return [documentsDirectory stringByAppendingFormat:@"/%@",KROOTDIRECTORY];
}
//文件路径 如果没有目录 则创建
+ (NSString *)writeToLibraryCachePath:(NSString*) fileName {
    
    NSString *documentsPath = [self getLibraryCachePath:fileName];
    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSArray * subPathArr = [documentsPath componentsSeparatedByString:@"/"];
    NSMutableArray * tempArr = [subPathArr mutableCopy];
    [tempArr removeLastObject];
    NSString * dir = [tempArr componentsJoinedByString:@"/"];
    NSError * error;
    BOOL isCreate = [fileManager createDirectoryAtPath:dir withIntermediateDirectories:YES attributes:nil error:&error];
    if (!isCreate) {
//        NSAssert(isCreate, @"create directory fail");
    }
    
    return documentsPath;
}	
+ (NSString *)getLibraryCachePath:(NSString *)fileName {
    
    if(fileName == nil)
        return nil;
    NSString * documentsDirectory = [CommUtils cachesPath];
    NSString * documentsPath = [documentsDirectory stringByAppendingPathComponent: fileName];
    return documentsPath;
}
//将网上路径截取得到为本地需要路径
+ (NSString*)convertLocalPathWithUrl:(NSString*)fileName {
    NSArray * perArr = [fileName componentsSeparatedByString:@"ttps:/"];
    NSString * localPath = perArr.lastObject;
    NSMutableString * finalPath = [[NSString stringWithFormat:@"%@/%@",KROOTFILENAME,[localPath lowercaseString]] mutableCopy];
    NSString * finalStr = [finalPath stringByReplacingOccurrencesOfString:@"//" withString:@"/"];
    return finalStr;
}
//source文件路径
+ (NSString*)sourcePath: (NSString*)fileName {
    if(fileName == nil)
        return nil;
    NSString * resourcePath= [[NSBundle mainBundle]resourcePath];
    NSString * path  = [resourcePath stringByAppendingPathComponent: [NSString stringWithFormat:@"%@",fileName]];
    return path;
}


//沙盒路径是否存在
+ (BOOL)fileExistsAtPath:(NSString *)fileName {
    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSString  * zipDir = [self getLibraryCachePath:[NSString stringWithFormat:@"%@",fileName]];
    BOOL fileExist = [fileManager fileExistsAtPath:zipDir];
    return fileExist;
    
}
//沙盒路径是否存在 是否是目录
+(BOOL)fileExistsAtPath: (NSString*) fileName isDirectory: (BOOL *)isDirectory
{
    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSString  * zipDir = [self getLibraryCachePath:[NSString stringWithFormat:@"%@",fileName]];
    BOOL fileExist = [fileManager fileExistsAtPath:zipDir isDirectory:isDirectory];
    return fileExist;
}
//文件拷贝
+ (BOOL)copyMissingFile:(NSString *)sourcePath toPath:(NSString *)toPath {
    BOOL retVal = YES; // If the file already exists, we'll return success…
    NSString * finalLocation = [self cachesPath];
    if (![[NSFileManager defaultManager] fileExistsAtPath:[self getLibraryCachePath:KROOTFILENAME]])
    {
        NSError * error;
        retVal = [[NSFileManager defaultManager] copyItemAtPath:sourcePath toPath:finalLocation error:&error];
        NSLog(@"拷贝成功 error：%@",error.description);
    }
    return retVal;
}
//删除Document目录下文件
+ (BOOL)deleteDocumentFile:(NSString *)fileName {
    BOOL retVal = YES;
    if(fileName == nil) {
        return false;
    }
    
    NSArray * paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString * documentsDirectory = ([paths count] > 0) ? [paths objectAtIndex:0] : nil;
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *filePath = [NSString stringWithFormat:@"%@/%@", documentsDirectory, fileName];
    if ([fileManager fileExistsAtPath:filePath]) {
        NSError * error;
        retVal =  [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
        NSLog(@"删除成功 error：%@",error.description);
    }
    return retVal;
}
#pragma mark -获取数据

// 获取当前时间 毫秒
+ (NSString *)getCurrentTimeString {
    NSDate* dat = [NSDate dateWithTimeIntervalSinceNow:0];
    NSTimeInterval a = [dat timeIntervalSince1970]*1000;
    NSString *timeString = [NSString stringWithFormat:@"%f", a];//转为字符型
    
    return timeString;
}

//返回网页代码
+ (NSString *)stringWithContentsOfFile:(NSString *)fileName{
    
    return [NSString stringWithContentsOfFile:fileName encoding:NSUTF8StringEncoding error:nil];
}


/*!
 * @brief 把格式化的JSON格式的字符串转换成字典
 * @param jsonString JSON格式的字符串
 * @return 返回字典
 */
+ (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err) {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}

// 判断本地是否存在下载的指定文件
+ (void)getLocalFile:(NSString *)localPath absolutePath:(void (^)(BOOL isExist , NSString * localPath))pathBlock
{
    // 首先用#分割链接
    NSArray * midArr = [localPath componentsSeparatedByString:@"#"];
    NSString * loadPathMid = midArr.firstObject;
    NSString * separateStr = [NSString stringWithFormat:@"%@/%@",KROOTDIRECTORY,KROOTFILENAME];
    // 用本地存储路径分割
    NSString * urlStr = [loadPathMid componentsSeparatedByString:separateStr].lastObject;
    NSArray * urlArr = [urlStr componentsSeparatedByString:@"?"];
    NSString * searchPath = urlArr.firstObject;
    // 先判断是否有.html文件
    searchPath = [self addHtmlSuffixForSourcePath:searchPath];
    // 从version.txt文件中查找路径
    searchPath = [self searchPathInFileListWithSearchContent:searchPath];
    BOOL isDir;
    NSString * localSearch = [CommUtils convertLocalPathWithUrl:searchPath];
    BOOL isExist = [CommUtils fileExistsAtPath:localSearch isDirectory:&isDir];
    //判断是文件夹还是文件
    if (isDir) {
        // 文件夹不是目标文件 所以not exist
        isExist = NO;
    }
    
    // 如果本地存在
    if (isExist) {
        //获取绝对路径
        NSString  *absolutePath = [self getLibraryCachePath:[NSString stringWithFormat:@"%@",localSearch]];
        BLOCK_SAFE(pathBlock,isExist,absolutePath)
        
    }else{
        
        BLOCK_SAFE(pathBlock,isExist,@"")
    }
    
}


//TODO:#分割逻辑可以去掉  采用 UrlString =  [UrlString stringByAddingPercentEncodingWithAllowedCharacters:([NSCharacterSet characterSetWithCharactersInString:@" `%^{}[]|\"<>"].invertedSet)];  不对#进行编码

+ (BOOL)componentHybridPath:(NSString *)loadPath request:(void (^)(NSURLRequest * request))requestBlock
{
    
    //file:///var/mobile/Containers/Data/Application/F87F294D-EDA1-4AD8-B59F-D9F13E6CE65A/Library/Caches/WebViewCache/uzai/m.uzai.com/product/hybrid/detail.html?productid=135939
    //http://mbuy.uzai.com/booking/#/editpeople
    //https://mbuy.uzai.com/booking/#/editpeople
    // 考虑 链接中包含参数值是链接的情况
    //如果http://m.uzai.com/sign/SignShare/?type=share&keywords=sign&description=haha&pic=http://r.uzaicdn.com/content/m/v2/images/index/logo-app.png&refUrl=http://m.uzai.com/sign/index/?ShareRetrun=0 这种的话
    //http://m.uzai.com NSMakeRange(0, 15)
    NSRange separateRange = [loadPath rangeOfString:@"://" options:(NSCaseInsensitiveSearch) range:NSMakeRange(0, 15)];
    NSString * schemeHeader = [loadPath substringToIndex:separateRange.location+separateRange.length];
    NSString * path = [loadPath substringFromIndex:separateRange.location+separateRange.length];
    // 首先用#分割链接  
    NSArray * midArr = [path componentsSeparatedByString:@"#"];
    NSString * loadPathMid = midArr.firstObject;
    NSString * separateStr = [NSString stringWithFormat:@"%@/%@/",KROOTDIRECTORY,KROOTFILENAME];
    // 用本地存储路径分割
    NSString * urlStr = [loadPathMid componentsSeparatedByString:separateStr].lastObject;
    NSArray * urlArr = [urlStr componentsSeparatedByString:@"?"];
    NSString * searchPath = urlArr.firstObject;
    // 先判断是否有.html文件
    searchPath = [self addHtmlSuffixForSourcePath:searchPath];
    // 从version.txt文件中查找路径
    searchPath = [self searchPathInFileListWithSearchContent:searchPath];
    BOOL isDir;
    NSString * localSearch = [CommUtils convertLocalPathWithUrl:searchPath];
    BOOL isExist = [CommUtils fileExistsAtPath:localSearch isDirectory:&isDir];
    //判断是文件夹还是文件
    if (isDir) {
        // 文件夹不是目标文件 所以not exist
        isExist = NO;
    }
    NSString * joinComponent;
    if ([urlStr rangeOfString:@"?"].location != NSNotFound) {
        joinComponent = @"&";
    }else{
        joinComponent = @"?";
    }
    
    // 如果是//http://mbuy.uzai.com/booking/#/editpeople 这种带有子页面标示的  还需要把子页面标识拼接回去
    NSString * midStr = @"";
    if (midArr.count > 1) {
        midStr = [NSString stringWithFormat:@"#%@",midArr.lastObject];
    }
    
    //TODO:验证
    if (!isExist) {
        // 如果不存在本地页面 并且断网 显示错误页面
        BOOL isReach =  [UZCommonUtils getNetworkRechability];
     
        if (isReach) {
            NSString * httpUrl = [NSString stringWithFormat:@"%@%@%@",schemeHeader,urlStr,joinComponent];
//            httpUrl = [httpUrl stringByReplacingOccurrencesOfString:@":///" withString:@"://"];
            [self htmlStr:httpUrl subPagePath:midStr isLocal:NO request:^(NSURLRequest *request) {
                if (requestBlock) {
                    requestBlock(request);
                }
            }];
        }
        return NO;
        
    }else{
        NSString * localPath = [CommUtils getLibraryCachePath:localSearch];
        if (urlArr.count>1) {
            // 拼接原本链接上带有的参数
            localPath = [localPath stringByAppendingFormat:@"?%@",urlArr.lastObject];
        }
        [self htmlStr:[localPath stringByAppendingString:joinComponent] subPagePath:midStr isLocal:YES request:^(NSURLRequest *request) {
            BLOCK_SAFE(requestBlock,request);
        }];
        return YES;
    }
}

// 请求数据
+ (void)htmlStr:(NSString *)path subPagePath:(NSString *)subPath isLocal:(BOOL)islocal request:(void (^)(NSURLRequest * request))requestBlock
{
    NSString *localPath;
    NSString * userid = @"";
    NSString *baseUrl = path;
    NSString *paramUrl;
    
    NSRange range = [path rangeOfString:@"?"];
    baseUrl = [[path substringToIndex:range.location] stringByAppendingString:@"?"];
    paramUrl = [path substringFromIndex:range.location+1];
    
    NSMutableDictionary *paramDic = [[NSMutableDictionary alloc] initWithDictionary:[UZCommonUtils convertURLToDictionary:paramUrl]];
    if ([UZClient sharedInstance].userID.length) {
        [paramDic setObject:[UZClient sharedInstance].userID forKey:@"userid"];
    }
    [paramDic setObject:@"ios" forKey:@"devicetype"];
    [paramDic setObject:[HybridConstant HybridVersion] forKey:@"hybridversion"];
    [paramDic setObject:@"iphone" forKey:@"source"];
    [paramDic setObject:[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"] forKey:@"appversion"];
    localPath = [baseUrl stringByAppendingString:[UZCommonUtils convertDictionaryToURL:paramDic]];
    localPath = [localPath stringByAppendingString:subPath];
    NSURL *url  = [NSURL URLWithString:localPath];
    // 添加不备份标志
    [url setResourceValue: [NSNumber numberWithBool: YES] forKey: NSURLIsExcludedFromBackupKey error:nil];
    // 添加请求 NSURLRequestUseProtocolCachePolicy
    NSURLRequest *request = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:60.0];
    BLOCK_SAFE(requestBlock,request);
}

/**
 *  判断目录是不是缺少.html  如果没有则加上index.html
 *
 *  @param sourcePath sourcePath description
 *
 *  @return 返回含有。html的文件
 */
+ (NSString *)addHtmlSuffixForSourcePath:(NSString *)sourcePath
{
    if (![sourcePath hasSuffix:@"html"]) {
        if ([sourcePath hasSuffix:@"/"]) {
            // /m.uzai.com/  这种目录不能正确处理
            sourcePath = [sourcePath stringByDeletingPathExtension];
        }
        sourcePath =  [sourcePath stringByAppendingString:@"/index.html"];
        return sourcePath;
    }
    return sourcePath;
    
}

/**
 *  在version.txt文件中查找文件目录
 *
 *  @param searchPath 查找对象
 *
 *  @return 如果匹配则返回version.txt文件中的文件目录 否则返回查找对象
 */
+ (NSString *)searchPathInFileListWithSearchContent:(NSString *)searchPath
{
    NSData * data = [[NSUserDefaults standardUserDefaults] objectForKey:KBridHTMLList];
    NSArray  * htmlList = [NSKeyedUnarchiver unarchiveObjectWithData:data];
    searchPath = searchPath.lowercaseString;
    BOOL isHav = YES;
    // 判断是否有hybrid目录
    // if havent
    if ([searchPath rangeOfString:@"hybrid" options:(NSCaseInsensitiveSearch)].location == NSNotFound) {
        isHav = NO;
    }
    for (NSDictionary * obj in htmlList) {
        NSString * fileName = obj[@"file"];
        fileName = [fileName componentsSeparatedByString:@"://"].lastObject.lowercaseString ;
        if (!isHav) {
            NSMutableArray * fileComponent = [[fileName componentsSeparatedByString:@"/"] mutableCopy];
            [fileComponent removeObject:@"hybrid"];
            NSString * newFileName = [fileComponent componentsJoinedByString:@"/"];
            if ([newFileName isEqualToString:searchPath]) {
                // 如果去掉hybrid之后 与搜索字符串相同 那么返回version.txt文件中的文件名
                return fileName;
            }
        }
    }
    return searchPath;

}

//截取链接
+ (NSArray *)subStringWithUrl:(NSString *)urlString
{
    NSRange headerRange=[urlString rangeOfString:@"?"];
    NSRange appDetailRange=[urlString rangeOfString:@"/AppDetail"];
    NSString *headerString;
    NSString *subString;
    if ([urlString rangeOfString:@"&"].location!=NSNotFound) {
        urlString=[urlString substringToIndex:[urlString rangeOfString:@"&"].location];
    }
    if (appDetailRange.location>headerRange.location) {
        headerString=[urlString substringFromIndex:headerRange.location+1];
        subString= [headerString substringFromIndex:appDetailRange.length+1];
    }
    else
    {
        if (headerRange.location!=NSNotFound) {
            headerString=[urlString substringToIndex:headerRange.location];
        }
        else
        {
            headerString=urlString;
        }
        subString= [headerString substringFromIndex:appDetailRange.location+appDetailRange.length+1];
    }
    return [subString componentsSeparatedByString:@"/"];
}


+ (BOOL)isNeedAppendBaseParam:(NSURLRequest *)request
{
    NSString * scheme = request.URL.scheme;
    if ([scheme isEqualToString:@"http"]||[scheme isEqualToString:@"https"]) {
        NSString * host = request.URL.host;
        if ([host containsString:@"uzai.com"] == NO && [host containsString:@"uzaicdn.com"] == NO) {
            NSLog(@"---request.URL---%@---",request.URL.absoluteString);
            return NO;
        }
    }
    return YES;
}
////将url转换至dictionary
//
//+ (NSDictionary *)convertURLToDictionary:(NSString *)urlString {
//    NSMutableDictionary *queryStringDictionary = [[NSMutableDictionary alloc] init];
//    
//    NSArray *urlComponents = [urlString componentsSeparatedByString:@"&"];
//    for (NSString *keyValuePair in urlComponents)
//    {
//        if (keyValuePair.length) {
//            NSRange range = [keyValuePair rangeOfString:@"="];
//            //如果通过&分割出的字符串里没有=则不进行字典转换
//            if (range.location != NSNotFound) {
//                NSString *key = [keyValuePair substringToIndex:range.location];
//                NSString *value = [keyValuePair substringFromIndex:range.location+1];
//                
//                [queryStringDictionary safeSetObject:value forKey:key];
//            } else {
//                NSLog(@"url中存在多余分隔符& -> %@",keyValuePair);
//            }
//        }
//    }
//    
//    return queryStringDictionary;
//}
//
////将dictionary转换至url
//+ (NSString*) convertDictionaryToURL:(NSDictionary *)dic {
//    NSMutableArray *parts = [NSMutableArray array];
//    for (id key in dic) {
//        id value = [dic objectForKey: key];
//        NSString *part = [NSString stringWithFormat: @"%@=%@", [self encodeURL:key], [self encodeURL:value]];
//        [parts addObject: part];
//    }
//    return [parts componentsJoinedByString: @"&"];
//}
//
//+ (NSString*)encodeURL:(NSString *)string
//{
//    NSString *newString = (__bridge_transfer NSString *)CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, (__bridge CFStringRef)string, NULL, CFSTR(":/?#[]@!$ &'()*+,;=\"<>%{}|\\^~`"), CFStringConvertNSStringEncodingToEncoding(NSUTF8StringEncoding));
//    
//    if (newString)
//    {
//        return newString;
//    }
//    
//    return @"";
//}

@end
