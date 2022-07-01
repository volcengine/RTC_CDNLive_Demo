//
//  LiveRoomInfoModel.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/19.
//  Copyright © 2021 . All rights reserved.
//

#import <Foundation/Foundation.h>
#import "LiveUserModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveRoomInfoModel : NSObject

@property (nonatomic, copy) NSString *liveAppID;
@property (nonatomic, copy) NSString *rtcAppID;
@property (nonatomic, copy) NSString *roomID;
@property (nonatomic, copy) NSString *roomName;
@property (nonatomic, copy) NSString *anchorUserID;
@property (nonatomic, copy) NSString *anchorUserName;
@property (nonatomic, assign) LiveRoomStatus status;
@property (nonatomic, assign) NSInteger audienceCount;
@property (nonatomic, copy) NSDictionary<NSString *, NSString *> *streamPullStreamList;

// 仅在 liveCreateLive/liveJoinLiveRoom 接口赋值
@property (nonatomic, strong) NSString *rtmToken;
// 只在liveStartLive 与 liveJoinLiveRoom 接口赋值
@property (nonatomic, strong) LiveUserModel *hostUserModel;

@end

NS_ASSUME_NONNULL_END
