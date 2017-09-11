//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "CacheManager.h"
#import "FMDatabase.h"
#import "CommUtils.h"
#import "WebEntity.h"

#define HybridCachePath @"HybridCache"
#define HybridCacheDBPath @"HybridCache/hybrid.db"

static dispatch_queue_t __serialQueue() {
    static dispatch_queue_t ____serialQueue;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ____serialQueue = dispatch_queue_create("com.hybrid.UZCacheManager.queue", DISPATCH_QUEUE_SERIAL);
    });
    return ____serialQueue;
}

@interface UZCacheHandler : NSObject<CacheHandleProto>

@property (nonatomic, strong) FMDatabase *db;

@end

@implementation UZCacheHandler

+ (UZCacheHandler *)shareHander {
    static UZCacheHandler *____shareHander;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ____shareHander = [[UZCacheHandler alloc] initUZCacheHandler];
    });
    return ____shareHander;
}

- (instancetype)init
{
    NSLog(@"UZCacheHandler.init 不支持的方法");
    return nil;
}


- (instancetype)initUZCacheHandler
{
    self = [super init];
    if (self) {
        [UZCacheHandler isExistDataBasePath];
        self.db = [self createDb];
    }
    return self;
}

- (FMDatabase *)createDb {
    FMDatabase *db = [FMDatabase databaseWithPath:[UZCommUtils getLibraryCachePath:HybridCacheDBPath]];
    BOOL isOpen = [db open];
    if (!isOpen) {
        NSLog(@"数据库打开失败");
        return nil;
    }else{
        return db;
    }
}

///文件夹的创建
+ (BOOL)isExistDataBasePath {
    NSString * dbPath = [UZCommUtils getLibraryCachePath:HybridCachePath];
    NSFileManager *fileManger = [NSFileManager defaultManager];
    if (![fileManger isExecutableFileAtPath:dbPath]) {
        return [fileManger createDirectoryAtPath:dbPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    else{
        return YES;
    }
}

///插入数据
- (void)insertModel:(UZWebEntity *)model {
    
    // 删除原数据
    [self deleteModel:model];
    
    BOOL opened =  [self.db open];
    if (!opened) {
        NSLog(@"%s:   数据库打开失败:%@", __func__, [self.db lastErrorMessage]);
        return;
    }
    NSString * createTable = [NSString stringWithFormat:@"create table if not exists %@ (id integer PRIMARY KEY AUTOINCREMENT,param TEXT, timeInterval REAL,result TEXT)",NSStringFromClass([model class])];
    BOOL __unused bo = [self.db executeUpdate:createTable];
    
    //        NSLog(@"createTable:%@",createTable);
    //        NSLog(@"error:%@",[db lastErrorMessage]);
    
    //        NSString * sql = @"insert into %@(%@) values(%@)";
    //        //获得model对象的属性名和属性值组成的字典
    //        NSDictionary * dict=[self propertyList:model isWrite:YES];
    //        //获得属性名组成的字符串，逗号分隔
    //        NSString * namelist=[[dict allKeys] componentsJoinedByString:@","];
    //        //获得属性值名组成的字符串，逗号分隔
    //        NSString * valuelist = [[dict allValues] componentsJoinedByString:@","];
    //格式化最终的插入语句
    //        sql=[NSString stringWithFormat:sql,NSStringFromClass([model class]),namelist,valuelist];
    
    NSString * sql = [NSString stringWithFormat:@"insert into %@(param,timeInterval,result) values('%@',%f,'%@')",NSStringFromClass([model class]),model.param,model.timeInterval,model.result];
    
    bo = [self.db executeUpdate:sql];
    if (!bo) {
        NSLog(@"插入失败:%@",[self.db lastErrorMessage]);
    }else{
        NSLog(@"插入成功 currentThred:%@",[NSThread currentThread]);
    }
    [self.db close];
}


- (void)deleteModel:(UZWebEntity *)model {
    BOOL opened =  [self.db open];
    if (!opened) {
        NSLog(@"%s:   数据库打开失败:%@", __func__, [self.db lastErrorMessage]);
        return;
    }
    NSString * delteSql = [NSString stringWithFormat:@"delete from %@ where param = '%@'",NSStringFromClass([model class]),model.param];
    BOOL isDelete = [self.db executeUpdate:delteSql];
    if (!isDelete) {
        NSLog(@"删除失败:%@",[self.db lastErrorMessage]);
    }else{
        NSLog(@"删除成功 currentThred:%@",[NSThread currentThread]);
    }
    [self.db close];
}

//+ (void)dropTable

- (void)queryModel:(UZWebEntity *)model resultBlock:(void (^)(UZWebEntity *))resultBlock {
    NSLog(@"查询 current:%@",[NSThread currentThread]);
    NSLog(@"查询 key:%@",model.param);

    BOOL opened =  [self.db open];
    if (!opened) {
        NSLog(@"%s:   数据库打开失败:%@", __func__, [self.db lastErrorMessage]);
        dispatch_async(dispatch_get_main_queue(), ^{
            if (resultBlock) {
                resultBlock(nil);
            }
        });
        return;
    }
    NSString * selectSql = [NSString stringWithFormat:@"select * from %@ where param = '%@'",[model class],model.param];
    UZWebEntity * newModel = [[UZWebEntity alloc] init];
    
    FMResultSet *result = [self.db executeQuery:selectSql];
    NSLog(@"fmdb error %@",[self.db lastErrorMessage]);
    
    //获得当前记录数据字典
    while([result next]) {
        newModel.param = [result stringForColumn:@"param"];
        newModel.timeInterval = [[result stringForColumn:@"timeInterval"] doubleValue];
        newModel.result = [result stringForColumn:@"result"];
    }
    [self.db close];
    dispatch_async(dispatch_get_main_queue(), ^{
        if (resultBlock) {
            resultBlock(newModel);
        }
    });
}


@end

//----------------------------------------

@interface CacheManager ()

@end

@implementation CacheManager

////获得当前对象的所有属性列表，如果isWrite为YES，返回的字典中同时包含了属性值，否则属性值为空
//+ (NSDictionary *)propertyList:(id)model   isWrite:(BOOL)isWrite {
//    NSMutableDictionary *props = [NSMutableDictionary dictionary];
//    unsigned int outCount, i;
//    //获得某个类的所有属性的拷贝
//    objc_property_t *properties = class_copyPropertyList([model class], &outCount);
//    for (i = 0; i<outCount; i++) {
//        //获得某一个属性
//        objc_property_t property = properties[i];
//
//        //获得属性名的字符串
//        NSString *propertyName = [[NSString alloc] initWithCString:property_getName(property) encoding:NSUTF8StringEncoding] ;
//
//        //获得指定的属性的值
//        id propertyValue = [model valueForKey:(NSString *)propertyName];
//        if (isWrite) {
//            if (propertyValue)
//            {
//                //保存属性名和属值值到字典中
//                [props setObject:propertyValue forKey:propertyName];
//            }
//        }
//        else{
//            //保存空对象到字典中,为了获得所有属性名的列表
//            [props setObject:[NSNull null] forKey:propertyName];
//        }
//    }
//    //释放拷贝的属性列表
//    free(properties);
//    //返回所需要的当前实例的属性字典（如果对象被赋值了，同时返回对象的值）
//    return props;
//}


//--------------------
+ (void)sync:(HandleCache)handlerCache
{
    dispatch_sync(__serialQueue(), ^{
        if (handlerCache) {
            handlerCache([UZCacheHandler shareHander]);
        }
    });
}
+ (void)async:(HandleCache)handlerCache
{
    dispatch_async(__serialQueue(), ^{
        if (handlerCache) {
            handlerCache([UZCacheHandler shareHander]);
        }
    });
}

@end
