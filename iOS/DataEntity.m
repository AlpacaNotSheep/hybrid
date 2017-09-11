//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

#import "DataEntity.h"

@implementation DataEntity
- (id) initWithCoder: (NSCoder *)coder
{
    if (self = [super init])
    {
        self.file = [coder decodeObjectForKey:@"file"];
        self.ver = [coder decodeObjectForKey:@"ver"];
    }
    return self;
}
- (void) encodeWithCoder: (NSCoder *)coder
{
    [coder encodeObject:self.file forKey:@"file"];
    [coder encodeObject:self.ver forKey:@"ver"];
}
@end
