//
//  LiveConstants.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/4/19.
//  Copyright © 2022 bytedance. All rights reserved.
//

#ifndef LiveConstants_h
#define LiveConstants_h

typedef NS_ENUM(NSInteger, LiveRoomStatus) {
    LiveRoomStatusLive          = 1, // 直播
    LiveRoomStatusAudienceLink  = 2, // 观众连麦
    LiveRoomStatusCoHost        = 3, // 主播连麦
};

typedef NS_ENUM(NSInteger, LiveUserRole) {
    LiveUserRoleAudience    = 1, // 观众
    LiveUserRoleHost        = 2, // 主播
};

typedef NS_ENUM(NSInteger, LiveInteractStatus) {
    LiveInteractStatusOther                = 0, // 空闲中
    LiveInteractStatusInviting             = 1, // 邀请中
    LiveInteractStatusApplying             = 2, // 申请中
    LiveInteractStatusAudienceLink         = 3, // 观众连麦互动中
    LiveInteractStatusHostLink             = 4, // 主播连麦互动中
};

#endif /* LiveConstants_h */
