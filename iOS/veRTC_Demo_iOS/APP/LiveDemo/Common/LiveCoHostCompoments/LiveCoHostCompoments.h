//
//  LiveCoHostCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveCoHostAudienceListsView.h"
#import "LiveCoHostRaiseHandListsView.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveCoHostDismissState) {
    LiveCoHostDismissStateNone,
    LiveCoHostDismissStateInviteIng,
};

@interface LiveCoHostCompoments : NSObject

@property (nonatomic, assign, readonly) BOOL isConnect;

- (instancetype)initWithRoomID:(NSString *)roomID;

// list

- (void)showInviteList:(void (^ __nullable)(LiveCoHostDismissState state))dismissBlock;

- (void)updateInviteList;

// live room

- (void)readyJoinRTCRoomByToken:(NSString *)token
                         roomID:(NSString *)roomID
                         userID:(NSString *)userID;

- (void)leaveRTCRoom;

- (void)showCoHost:(UIView *)superView
     streamPushUrl:(NSString *)streamPushUrl
     userModelList:(NSArray<LiveUserModel *> *)userModelList
    loginUserModel:(LiveUserModel *)loginUserModel;

- (void)closeCoHost;

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid;

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END
