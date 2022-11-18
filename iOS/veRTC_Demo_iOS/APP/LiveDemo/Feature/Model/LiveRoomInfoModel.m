//
//  LiveRoomInfoModel.m
//  veRTC_Demo
//
//  Created by on 2021/10/19.
//  
//

#import "LiveRoomInfoModel.h"

@implementation LiveRoomInfoModel

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{@"liveAppID": @"live_app_id",
             @"rtcAppID" : @"rtc_app_id",
             @"roomID" : @"room_id",
             @"roomName" : @"room_name",
             @"anchorUserID" : @"host_user_id",
             @"anchorUserName" : @"host_user_name",
             @"status" : @"status",
             @"audienceCount" : @"audience_count",
             @"streamPullStreamList" : @"stream_pull_url_list",
    };
}

@end
