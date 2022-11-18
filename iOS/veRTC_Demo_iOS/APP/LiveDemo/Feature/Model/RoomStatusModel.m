//
//  RoomStatusModel.m
//  veRTC_Demo
//
//  Created by on 2021/11/4.
//  
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
