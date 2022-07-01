//
//  RoomStatusModel.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/11/4.
//  Copyright © 2021 . All rights reserved.
//

#import "RoomStatusModel.h"

@implementation RoomStatusModel

+ (NSDictionary *)modelContainerPropertyGenericClass {
    return @{@"interactUserList" : [LiveUserModel class]};
}

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{@"interactStatus" : @"interact_status",
             @"interactUserList" : @"interact_user_list"};
}

@end
