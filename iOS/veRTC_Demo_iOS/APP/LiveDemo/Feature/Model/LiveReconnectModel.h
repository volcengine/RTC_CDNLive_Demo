//
//  LiveReconnectModel.h
//  veRTC_Demo
//
//  Created by on 2021/10/21.
//  
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
@property (nonatomic, assign) NSInteger interactStatus; // 1: 不互动中 2: 观众互动中 3: 主播互动中

@property (nonatomic, strong) LiveUserModel *loginUserModel;

@end

NS_ASSUME_NONNULL_END
