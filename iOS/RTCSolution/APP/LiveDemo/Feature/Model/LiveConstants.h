// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#ifndef LiveConstants_h
#define LiveConstants_h

typedef NS_ENUM(NSInteger, LiveRoomStatus) {
    // 直播
    LiveRoomStatusLive          = 1,
    // 观众连麦
    LiveRoomStatusAudienceLink  = 2,
    // 主播连麦
    LiveRoomStatusCoHost        = 3,
};

typedef NS_ENUM(NSInteger, LiveUserRole) {
    // 观众
    LiveUserRoleAudience    = 1,
    // 主播
    LiveUserRoleHost        = 2,
};

typedef NS_ENUM(NSInteger, LiveInteractStatus) {
    // 空闲中
    LiveInteractStatusOther                = 0,
    // 邀请中
    LiveInteractStatusInviting             = 1,
    // 申请中
    LiveInteractStatusApplying             = 2,
    // 观众连麦互动中
    LiveInteractStatusAudienceLink         = 3,
    // 主播连麦互动中
    LiveInteractStatusHostLink             = 4,
};

#endif /* LiveConstants_h */
