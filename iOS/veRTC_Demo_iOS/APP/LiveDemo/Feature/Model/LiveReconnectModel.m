//
//  LiveReconnectModel.m
//  veRTC_Demo
//
//  Created by on 2021/10/21.
//  
//

#import "LiveReconnectModel.h"

@implementation LiveReconnectModel

+ (NSDictionary *)modelContainerPropertyGenericClass {
    return @{@"rtcUserList" : [LiveUserModel class]};
}

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{
        @"roomModel" : @"live_room_info",
        @"streamPushUrl" : @"stream_push_url",
        @"rtcRoomID" : @"rtc_room_id",
        @"rtcToken" : @"rtc_token",
        @"rtcUserList" : @"linkmic_user_list",
    };
}

@end
