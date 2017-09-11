//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>

@class UZWebEntity;



@protocol CacheHandleProto <NSObject>

- (void)insertModel:(UZWebEntity *)model;
- (void)deleteModel:(UZWebEntity *)model;
- (void)queryModel:(UZWebEntity *)model resultBlock:(void(^)(UZWebEntity *model))resultBlock;

@end




typedef void(^HandleCache)(id<CacheHandleProto> handler);


@interface CacheManager : NSObject
//+ (UZCacheManager *)shareManager;
//- (void)insertModel:(UZWebEntity *)model;
//- (void)deleteModel:(UZWebEntity *)model;
//- (void)queryModel:(UZWebEntity *)model resultBlock:(void(^)(UZWebEntity *model))resultBlock;

+ (void)sync:(HandleCache)handlerCache;
+ (void)async:(HandleCache)handlerCache;




@end
