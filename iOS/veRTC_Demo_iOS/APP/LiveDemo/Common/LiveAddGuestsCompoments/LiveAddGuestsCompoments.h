//
//  LiveAddGuestsCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveAddGuestsListsView.h"
#import "LiveUserModel.h"
#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, LiveAddGuestsDismissState) {
    LiveAddGuestsDismissStateNone,
    LiveAddGuestsDismissStateInvite,
    LiveAddGuestsDismissStateCloseConnect,
};

FOUNDATION_EXTERN NSTimeInterval const LiveApplyOvertimeInterval;

NS_ASSUME_NONNULL_BEGIN

@interface LiveAddGuestsCompoments : NSObject

@property (nonatomic, assign, readonly) BOOL isConnect;

@property (nonatomic, strong, readonly) NSArray *guestList;

- (instancetype)initWithRoomID:(LiveRoomInfoModel *)roomInfoModel;

// List

- (void)showList:(void (^)(LiveAddGuestsDismissState state))dismissBlock;

- (void)updateList;

// Live room render

- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID;

- (void)leaveRTCRoom;

- (void)showAddGuests:(UIView *)superView
        streamPushUrl:(NSString *)streamPushUrl
              hostUid:(NSString *)hostUid
             userList:(NSArray<LiveUserModel *> *)userList;

- (nullable NSString *)removeAddGuestsUid:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList;

- (void)closeAddGuests;

- (void)updateGuests:(NSArray<LiveUserModel *> *)userList;

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid;

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid;

- (void)closeSheet:(NSString *)uid;

// Audience apply

- (void)showApply:(LiveUserModel *)loginUserModel hostID:(NSString *)hostID;

- (void)closeApply;

@end

NS_ASSUME_NONNULL_END
