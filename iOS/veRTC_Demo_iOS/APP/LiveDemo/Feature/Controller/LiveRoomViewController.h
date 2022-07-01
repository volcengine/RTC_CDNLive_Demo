//
//  LiveRoomViewController.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveReconnectModel.h"
#import "LiveRoomInfoModel.h"
#import "LiveRoomSettingCompoments.h"
#import "LiveUserModel.h"
#import "RoomStatusModel.h"
#import <UIKit/UIKit.h>
#import "BytedEffectProtocol.h"

@interface LiveRoomViewController : UIViewController

@property (nonatomic, copy) void (^hangUpBlock)(BOOL result);
@property (nonatomic, strong) LiveRoomSettingCompoments *settingCompoments;
@property (nonatomic, strong) BytedEffectProtocol *beautyCompoments;

- (instancetype)initWithRoomModel:(LiveRoomInfoModel *)liveRoomModel
                   reconnectModel:(LiveReconnectModel *)reconnectModel
                    streamPushUrl:(NSString *)streamPushUrl;

- (void)addUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount;

- (void)removeUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount;

// CoHost
- (void)receivedCoHostInviteWithUser:(LiveUserModel *)inviter
                            linkerID:(NSString *)linkerID
                               extra:(NSString *)extra;

- (void)receivedCoHostRefuseWithUser:(LiveUserModel *)invitee;

- (void)receivedCoHostSucceedWithUser:(LiveUserModel *)invitee
                             linkerID:(NSString *)linkerID
                            rtcRoomID:(NSString *)rtcRoomID
                             rtcToken:(NSString *)rtcToken;

- (void)receivedCoHostJoin:(NSArray<LiveUserModel *> *)userlList;

- (void)receivedCoHostEnd;

// Add Guests - host

- (void)receivedAddGuestsApplyWithUser:(LiveUserModel *)applicant
                              linkerID:(NSString *)linkerID
                                 extra:(NSString *)extra;

// Add Guests - Guests

- (void)receivedAddGuestsInviteWithUser:(LiveUserModel *)inviter
                               linkerID:(NSString *)linkerID
                                  extra:(NSString *)extra;

- (void)receivedAddGuestsManageGuestMedia:(NSString *)uid
                                   camera:(NSInteger)camera
                                      mic:(NSInteger)mic;

// Add Guests - All
- (void)receivedAddGuestsSucceedWithUser:(LiveUserModel *)invitee
                                linkerID:(NSString *)linkerID
                               rtcRoomID:(NSString *)rtcRoomID
                                rtcToken:(NSString *)rtcToken;

- (void)receivedAddGuestsRefuseWithUser:(LiveUserModel *)invitee;

- (void)receivedAddGuestsJoin:(NSArray<LiveUserModel *> *)userlList;

- (void)receivedAddGuestsRemoveWithUser:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList;

- (void)receivedAddGuestsEnd;

- (void)receivedRoomStatus:(LiveInteractStatus)status;

- (void)receivedAddGuestsMediaChangeWithUser:(NSString *)uid
                                 operatorUid:(NSString *)operatorUid
                                      camera:(BOOL)camera
                                         mic:(BOOL)mic;

- (void)receivedLeaveTemporary:(NSString *)uid
                      userName:(NSString *)userName
                      userRole:(NSString *)userRole;

- (void)receivedLiveEnd:(BOOL)isKick type:(NSString *)type;

@end
