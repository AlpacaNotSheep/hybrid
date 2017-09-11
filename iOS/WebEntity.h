//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WebEntity : NSObject
@property (nonatomic, copy) NSString * param; //请求参数
@property (nonatomic, assign) double  timeInterval;  //保存数据的时间
@property (nonatomic, copy) NSString * result;//返回的json数据

@end
