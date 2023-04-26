// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveRoomInfoModel.h"
#import "LiveUserModel.h"
#import "RoomStatusModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveReconnectModel : NSObject

@property (nonatomic, strong) LiveRoomInfoModel *roomModel;
@property (nonatomic, copy) NSString *streamPushUrl;
@property (nonatomic, copy) NSString *rtcRoomID;
@property (nonatomic, copy) NSString *rtcToken;
@property (nonatomic, copy) NSArray<LiveUserModel *> *rtcUserList;
@property (nonatomic, strong) LiveUserModel *loginUserModel;

// 1: 直播模式 2: 主播嘉宾连麦 3: 多主播连麦
@property (nonatomic, assign) NSInteger interactStatus;

@end

NS_ASSUME_NONNULL_END
