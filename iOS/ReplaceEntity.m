//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "ReplaceEntity.h"

@implementation ReplaceEntity
- (id) initWithCoder: (NSCoder *)coder
{
    if (self = [super init])
    {
        self.old = [coder decodeObjectForKey:@"old"];
        self.placeNew = [coder decodeObjectForKey:@"placeNew"];
        self.type = [coder decodeObjectForKey:@"type"];
    }
    return self;
}
- (void) encodeWithCoder: (NSCoder *)coder
{
    [coder encodeObject:self.old forKey:@"old"];
    [coder encodeObject:self.placeNew forKey:@"placeNew"];
    [coder encodeObject:self.type forKey:@"type"];
}
@end
