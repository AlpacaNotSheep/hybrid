//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "DataBase.h"
#import "NSString+Expand.h"
#import "UZCommUtils.h"
#import <objc/runtime.h>
@import UZCategoryModule;
@import UZUtilModule;
@import MJExtension;

@interface DataBase ()<UIAlertViewDelegate>
{
    dispatch_queue_t _serialQueue;
}
@property (nonatomic, strong) NSMutableArray * replaceTypeArr;
@property (nonatomic, strong) NSMutableDictionary * replaceTextDict;

@end

@implementation DataBase
static bool logging = false;

+ (void)enableLogging { logging = true; }
static DataBase *_parser;

+ (instancetype)sharedInstance{
    @synchronized(self){
        __weak __typeof(self) weakSelf = self;
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            _parser = [[weakSelf alloc]init];
            [weakSelf create];
//             [self enableLogging];
        });
    }
    return _parser;
    
    
}

//FIXED: 根据版本号 每个版本启动第一次进行拷贝文件
//1.如果不存在cache目录 那么创建
+ (void)create
{
    NSString *firstLaunch = [NSString stringWithFormat:@"firstLaunch%@",[UZCommonVariables kAppVersion]];
    if ([[NSUserDefaults standardUserDefaults] boolForKey:firstLaunch] || ![UZCommUtils fileExistsAtPath:KROOTFILENAME]) {
        //当前版本第一次安装 或者 document不存在拷贝source文件
        [UZCommUtils deleteDocumentFile:KROOTDIRECTORY];
        NSString *resourcePath = [[NSBundle bundleForClass:self] resourcePath];
        NSString *path = [resourcePath stringByAppendingPathComponent: KROOTDIRECTORY];
        [UZCommUtils copyMissingFile:path toPath:[UZCommUtils cachesPath]];
    }
}
//2.发送请求获取版本号txt
static char UZObserverLoadKey;

- (void)requestUZStateBlock:(LoadVersionCompleteBlock)_block{
    objc_setAssociatedObject(self, &UZObserverLoadKey, _block, OBJC_ASSOCIATION_COPY);
    _serialQueue = dispatch_queue_create("com.uzai.hybrid", DISPATCH_QUEUE_SERIAL);
    _flag = 0;
    _failFlag = 0;
    __weak __typeof(self) weakSelf = self;
    
#if isDebug
    [UZDownLoad requestURL:[NSString stringWithFormat:@"%@/version-ssl.txt?ver=%@",KWEBNURL,[UZCommUtils getCurrentTimeString]] params:nil completion:^(id result) {
        if (result) {
            result = [result mj_JSONObject];
            [weakSelf checkBridVersion:result];
            
        }else{
            [weakSelf requestAYState:NO];
        }
    }];
#else
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        [UZDownLoad testDownloadingFromCompletion:^(id result) {
            if (result) {
                result = [result mj_JSONObject];
                [weakSelf checkBridVersion:result];
                
            }else{
                [weakSelf requestAYState:NO];
            }
        }];
    });
#endif

    
}
//3.验证是否需要app端更新
- (void)checkBridVersion:(id)result{
    NSDictionary *hybridVersion = [result objectForKey:@"hybridVersion"];
    if ([hybridVersion isKindOfClass:[NSDictionary class]]) {
        NSString *ver =  [hybridVersion objectForKey:@"ios"];
        // APP版本不一致需要先更新客户端。
        if (KBridVersion.intValue == ver.intValue) {
            
            [self placeLink:result];
            [self loadData:result];
        }else{
            [self log:@"需要客户端更新，不能下载资源" json:@""];
        }
    }else{
        [self log:@"需要客户端更新，不能下载资源" json:@""];
    }
    
}

//4.下载需要替换的数据
- (void)placeLink:(id)data{
    //优先保存，以防用户强退没有保存成功
    //保存所有HTML文件列表
    NSArray * htmlDict = [[data objectForKey:@"update"] objectForKey:@"html"];
    NSData *encodedhtmlDict = [NSKeyedArchiver archivedDataWithRootObject:htmlDict];
    // 保存所有HTML文件名 用于判断文件名是否缺少hybrid目录
    [[NSUserDefaults standardUserDefaults] setObject:encodedhtmlDict forKey:KBridHTMLList];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    NSMutableDictionary * replacesDataDict = [[NSMutableDictionary alloc] init];
    NSMutableArray *replaceTypeArr = [NSMutableArray array];
    NSDictionary *replaceDict = [data objectForKey:@"replace"];
    NSEnumerator *enumeratorKey = [replaceDict keyEnumerator];
    for (NSString *object in enumeratorKey) {
        [replaceTypeArr addObject:object];
    }
    
    for (NSInteger i = 0; i < [replaceTypeArr count]; i++) {
        NSMutableArray *replaces = [NSMutableArray array];
        for (NSDictionary *dic in [replaceDict objectForKey:replaceTypeArr[i]]) {
            UZReplaceEntity *entity = [UZReplaceEntity mj_objectWithKeyValues:dic];
            entity.placeNew = dic[@"new"];
            entity.type = replaceTypeArr[i];
            [replaces addObject:entity];
        }
        [replacesDataDict setValue:replaces forKey:replaceTypeArr[i]];
    }
    //替换文段
    self.replaceTextDict = replacesDataDict;
    //替换类型
    self.replaceTypeArr = replaceTypeArr;
   
}

#warning 正式环境需要把isDebug改为0
//5.下载数据解析
- (void)loadData:(id)data{
    [self log:@"start loading data" json:@""];
    NSArray *httpLists =  [self queryStringFromDictionary:[data objectForKey:@"update"]];
    if (!self.httpSources) {
        _httpSources = httpLists;
    }
    dispatch_group_t group = dispatch_group_create();
    dispatch_queue_t globalQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0);
    dispatch_async(globalQueue, ^{
        for (UZDataEntity *entity in httpLists) {
            // 与之前存储的版本信息对比
            dispatch_group_enter(group);
            if([self compareLocalListWith:entity]) {
                //版本信息一致，证明已经下载过
                // 转换成本地路径
                NSString *localpath = [UZCommUtils convertLocalPathWithUrl:entity.file];
                // 判断本地是否存在文件，不存在则下载
                if (![UZCommUtils fileExistsAtPath:localpath]) {
                    [self downHtmlWithFile:entity.file ver:entity.ver dispatchGourp:group];
                }else{
                    if (isDebug) {
                        [self downHtmlWithFile:entity.file ver:entity.ver dispatchGourp:group];
                    }else{
                        _flag++;
                        dispatch_group_leave(group);
                    }
                }
            }else{
                //版本信息不一致，重新下载
                [self downHtmlWithFile:entity.file ver:entity.ver dispatchGourp:group];
            }
        }
        
        //下载任务队列结束时调用
        dispatch_notify(group, globalQueue,  ^{
            NSLog(@"all round => %d success flag -> %d fail FLAG -> %d \n",httpLists.count, self.flag, self.failFlag);
            NSDictionary * statuDic = [self getStatusDict];
            NSLog(@"fail list:%@",statuDic.mj_JSONString);
            
            __block NSMutableArray *updateInfos = [NSMutableArray new];
            [statuDic enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
                NSMutableDictionary *updateInfoDic = [[NSMutableDictionary alloc] initWithDictionary:obj];
                [updateInfoDic setObject:key forKey:@"file"];
                [updateInfos addObject:updateInfoDic];
            }];
            
            NSMutableDictionary *uploadStatusDic = [NSMutableDictionary new];
            [uploadStatusDic setObject:updateInfos forKey:@"update"];
            [uploadStatusDic setObject:[UZCommonUtils deviceWANIPAddress] forKey:@"IP"];
            [uploadStatusDic setObject:@"" forKey:@"remark"];
            [uploadStatusDic setObject:@"" forKey:@"netInfo"];
           
            //下载完成之后 比对一下plist文件的版本与线上版本
//            [self compareVersionPlistWithUpdataList:httpLists];
            
            __weak __typeof(self) weakSelf = self;
            [UZDownLoad uploadDownloadResourceStatus:uploadStatusDic Completion:^(id result) {
                [weakSelf setStatusDict:[NSDictionary new]];
            }];
        });
        
        if (self.flag == httpLists.count) {
            [self requestAYState:YES];
            [self log:@"request finish" json:@""];
        }
    });
}

//6.遍历网络数据
- (NSArray *)queryStringFromDictionary:(NSDictionary *)dictionary
{
    NSMutableArray *pairs = [NSMutableArray array];
    for (NSString *key in [dictionary keyEnumerator])
    {
        id value = [dictionary objectForKey:key];
        if ([value isKindOfClass:[NSArray class]]) {//遍历最外层字典（文件名）
            for(NSDictionary *dic in value) {
                UZDataEntity *entity = [UZDataEntity mj_objectWithKeyValues:dic];
                [pairs addObject:entity];
            }
        }
    }
    return pairs;
}
//7.version列表和本地数据version.plist比较
- (BOOL)compareLocalListWith:(UZDataEntity *)entity{
    
    NSDictionary *localListsDict = [self getVersionDict];
    NSString * ver = localListsDict[entity.file];
    if (ver != nil) {
        if ([entity.ver isEqualToString:localListsDict[entity.file]]) {
            return YES;
        }else{
            return NO;
        }
    }
    // 之前没有存储过
    return NO;
}

//8.文件数据更新
- (void)downHtmlWithFile:(NSString *)file ver:(NSString *)ver dispatchGourp:(dispatch_group_t)group{
    // 解决cdn缓存问题 每次下载都使用时间戳
    NSString * version = [UZCommUtils getCurrentTimeString];
    //非uzai.com or uzaicdn.com域名下的不下载
    if (![self  checkWhiteListWithUrl:file]) {
        _failFlag++;
        [self insertStatusDict:file ver:@"非uzai域名"];
        dispatch_group_leave(group);
        return;
    }
    
    __weak __typeof(self) weakSelf = self;
    [UZDownLoad requestURL:file  params:version completion:^(id result) {
        //写入文件
        if (result) {
            __block NSData* jsonData = result;
            
            //所有文件都需要比对crc32
            // 判断MD5值 判断现在的内容是否正确
            NSString *crcString = [jsonData CRCString];
            [weakSelf log:[NSString stringWithFormat:@"\nfile -> %@ \nver str -> %@ \ncrc32 str -> %@ \n",file,ver,crcString] json:@""];
            //只有在release环境下才进行crc对比判断
            if (![crcString isEqualToString:ver] && !isDebug) {
                NSString * wrongStr = [NSString stringWithFormat:@"\n%@---%@--%@\n",file,ver,crcString];
                [weakSelf log:@"fail:【compare】 ....." json:wrongStr];
                
                //比较失败 ver字段记录自己算的crc32
                [weakSelf insertStatusDict:file ver:crcString];
                //crc比较失败failFlag+1
                _failFlag++;
                dispatch_group_leave(group);
                return ;
            }
            
            NSMutableString * suffix = [[file pathExtension] mutableCopy];
            [suffix insertString:@"." atIndex:0];
            if ([weakSelf existReplaceTypeWith:file]) {//判断是否是需要替换的类型
                NSString *htmlStr = [[NSString alloc] initWithData:result  encoding:NSUTF8StringEncoding];
                
                //替换绝对路径为相对路径
                htmlStr = [weakSelf htmlReplaceWith:htmlStr replaceType:suffix];
                if (htmlStr) {
                    jsonData = [htmlStr dataUsingEncoding:NSUTF8StringEncoding];
                    [weakSelf  log:@"替换文件类型" json:file];
                }
            }else{
                [weakSelf  log:@"不用替换文件类型" json:file];
            }
            NSString *localpath = [UZCommUtils convertLocalPathWithUrl:file];
            //            NSLog(@"%@",localpath);
            
            BOOL resultSuccess = [jsonData writeToFile:[UZCommUtils writeToLibraryCachePath:localpath] atomically:YES];
            if (!resultSuccess) {
                //写入文件失败failFlag+1
                _failFlag++;
                [weakSelf insertStatusDict:file ver:@"存储失败"];
                [weakSelf  log:@"fail:【write local】 ......" json:[UZCommUtils getLibraryCachePath:localpath]];
            }else{
                //记录成功文件数
                _flag++;
                
                dispatch_async(_serialQueue, ^{
                    NSMutableDictionary * versionDic = [NSMutableDictionary dictionaryWithDictionary:[self getVersionDict]];
                    [versionDic setObject:ver forKey:file];
                    [self setVersionDict:versionDic];
                });
              
                [weakSelf  log:@"success......" json:[UZCommUtils getLibraryCachePath:localpath]];
            }
        }else{
            [weakSelf  log:@"fail:【request】......" json:file];
            //下载失败failFlag+1
            [weakSelf insertStatusDict:file ver:@"404"];
            _failFlag++;
        }
        
        dispatch_group_leave(group);
        //如果没有失败文件 则认为全部下载成功
        if (weakSelf.flag == weakSelf.httpSources.count) {
            [weakSelf requestAYState:YES];
        }
    }];
    
}

//version.plist  记录下载成功的文件名以及版本号
- (NSDictionary *)getVersionDict
{
    NSString * path = [UZCommUtils cachesPath];
    NSString *plistPath = [path stringByAppendingPathComponent:@"version.plist"];
    NSDictionary * dict = [NSDictionary dictionaryWithContentsOfFile:plistPath];
    if (dict == nil) {
        return @{};
    }
    return dict;
    
}

- (BOOL)setVersionDict:(NSDictionary *)versionDict
{
    NSString * path = [UZCommUtils cachesPath];
//    NSString *plistPath = [[NSBundle bundleForClass:self.classForCoder] pathForResource:@"version" ofType:@"plist"];
    NSString *plistPath = [path stringByAppendingPathComponent:@"version.plist"];
    BOOL isSuccess = [versionDict writeToFile:plistPath atomically:YES];
    return isSuccess;
}


/**
 status.plist
 //fail:失败次数，ver:比对失败使用程序算的crc32 下载失败等设置失败原因
 "https://m.uzai.com/product/hybrid/boradlocation.html":{"fail":1,"ver":""}
//比较失败ver字段记录自己算的crc32
 @return status.plist
 */
- (NSArray *)getStatusDict
{
    NSString * path = [UZCommUtils cachesPath];
    NSString *plistPath = [path stringByAppendingPathComponent:@"status.plist"];
    NSDictionary * dict = [NSDictionary dictionaryWithContentsOfFile:plistPath];
    if (dict == nil) {
        return @{};
    }
    return dict;
}

- (BOOL)setStatusDict:(NSDictionary *)statusDict
{
    NSString * path = [UZCommUtils cachesPath];
    NSString *plistPath = [path stringByAppendingPathComponent:@"status.plist"];
    BOOL isSuccess = [statusDict writeToFile:plistPath atomically:YES];
    return isSuccess;
}

//将失败的文件写入status.plist
- (void)insertStatusDict:(NSString *)file ver:(NSString *)ver{
    __weak __typeof(self) weakSelf = self;

    dispatch_async(_serialQueue, ^{
        //下载失败记录
        NSMutableDictionary * statuDic = [NSMutableDictionary dictionaryWithDictionary:[self getStatusDict]];
        NSMutableDictionary *updateInfo = [NSMutableDictionary new];
        [updateInfo setObject:ver forKey:@"ver"];
        [statuDic setObject:updateInfo forKey:file];
        [self setStatusDict:statuDic];
        
        [weakSelf log:[NSString stringWithFormat:@"updateInfo Dic -> %@",updateInfo] json:@""];
    });

}

#pragma mark - 逻辑处理  暂时没有回调处理
- (void)requestAYState:(BOOL)isSuccess{
    LoadVersionCompleteBlock _block = objc_getAssociatedObject(self, &UZObserverLoadKey);
    if (_block) {
        _block(isSuccess,nil);
    }
}

//将htmlStr内容的替换成新内容 用来做兼容
- (NSString *)htmlReplaceWith:(NSString *)htmlStr replaceType:(NSString *)type{
    
    NSDictionary *replaceDict = self.replaceTextDict;
    NSArray *typeArr = self.replaceTypeArr;
    NSString * returnStr = nil;
    if ([typeArr containsObject:type]) {
        for (UZReplaceEntity *entity in [replaceDict objectForKey:type]) {
            returnStr = [htmlStr stringByReplacingOccurrencesOfString:entity.old withString:entity.placeNew];
        }
    }
    return returnStr;
}

//替换类型判断
- (BOOL)existReplaceTypeWith:(NSString *)path{
    
    NSArray *localTypes = self.replaceTypeArr;
    NSMutableString * suffix = [[path pathExtension] mutableCopy];
    [suffix insertString:@"." atIndex:0];
    return [localTypes containsObject:suffix];
}
//加入白名单
- (BOOL)checkWhiteListWithUrl:(NSString *)file{
    NSArray *urlComps = [file componentsSeparatedByString:@"://"];
    NSString * urlString=[urlComps lastObject];
    NSArray *httpComps = [urlString componentsSeparatedByString:@"/"];
    NSString * httpHeader=[httpComps firstObject];
    NSString *fileStr = @"uzai.com";
    NSString * fileStr2 = @"uzaicdn.com";
    if ([httpHeader containSubString:fileStr] || [httpHeader containSubString:fileStr2]) {
        return YES;
    }else
        return NO;
    
}

- (void)compareVersionPlistWithUpdataList:(NSArray *)updataList{

    NSMutableDictionary * compareResultDict = [[NSMutableDictionary alloc] init];
    NSDictionary * plistDict = [self getVersionDict];
    for (UZDataEntity *entity in updataList){
        if (![entity.ver isEqualToString:plistDict[entity.file]]) {
            [compareResultDict setObject:plistDict[entity.file] forKey:entity.file];
        }
    }
    NSLog(@"【CompareResule】%@",compareResultDict);
}
//打印数据
- (void) log:(NSString *)action json:(NSString *)json {
    if (!logging) { return; }
    if ([json length] > 0) {
        NSLog(@"UZDataBase %@: %@", action, json);
    } else {
        NSLog(@"UZDataBase %@", action);
    }
}

- (void)dealloc
{
    NSLog(@"%@-----dealloc",NSStringFromClass([DataBase class]));
}
@end
