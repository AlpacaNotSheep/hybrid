//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ReplaceEntity : NSObject<NSCoding>
@property (nonatomic, copy) NSString *old;
@property (nonatomic, copy) NSString *placeNew;
@property (nonatomic, copy) NSString *type;
@end
