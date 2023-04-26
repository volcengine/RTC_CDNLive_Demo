// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveReconnectModel.h"
#import "LiveRoomInfoModel.h"
#import "LiveRoomSettingComponent.h"
#import "LiveUserModel.h"
#import "RoomStatusModel.h"
#import <UIKit/UIKit.h>
#import "BytedEffectProtocol.h"

@interface LiveRoomViewController : UIViewController

@property (nonatomic, copy) void (^hangUpBlock)(BOOL result);
@property (nonatomic, strong) LiveRoomSettingComponent *settingComponent;

- (instancetype)initWithRoomModel:(LiveRoomInfoModel *)liveRoomModel
                    streamPushUrl:(NSString *)streamPushUrl;

#pragma mark - Listener

/**
 * @brief 收到用户加入房间
 * @param userModel 用户模型
 * @param audienceCount 当前房间用户数量
 */
- (void)addUser:(LiveUserModel *)userModel
  audienceCount:(NSInteger)audienceCount;

/**
 * @brief 收到用户离开房间
 * @param userModel 用户模型
 * @param audienceCount 当前房间用户数量
 */
- (void)removeUser:(LiveUserModel *)userModel
     audienceCount:(NSInteger)audienceCount;

/**
 * @brief 收到 IM 消息
 * @param message 消息内容
 * @param sendUserModel 发送者用户模型
 */
- (void)receivedIMMessage:(NSString *)message
            sendUserModel:(LiveUserModel *)sendUserModel;

/**
 * @brief 收到连麦状态变化
 * @param status 连麦状态
 */
- (void)receivedRoomStatus:(LiveInteractStatus)status;

/**
 * @brief 收到主播或嘉宾相机和麦克风状态变化
 * @param uid 设备变化的用户ID
 * @param operatorUid 操作设备变化的用户ID
 * @param camera 相机状态
 * @param mic 麦克风状态
 */
- (void)receivedAddGuestsMediaChangeWithUser:(NSString *)uid
                                 operatorUid:(NSString *)operatorUid
                                      camera:(BOOL)camera
                                         mic:(BOOL)mic;

/**
 * @brief 收到主播或嘉宾临时切到后台的消息
 * @param uid 用户ID
 * @param userName 用户昵称
 * @param userRole 用户角色
 */
- (void)receivedLeaveTemporary:(NSString *)uid
                      userName:(NSString *)userName
                      userRole:(NSString *)userRole;

/**
 * @brief 收到直播结束消息
 * @param type 直播结束类型。2：因为超时关闭，3：因为违规关闭。
 */
- (void)receivedLiveEnd:(NSString *)type;

#pragma mark - Listener Cohost

/**
 * @brief 收到多主播连麦邀请
 * @param inviter 邀请的用户模型
 * @param linkerID 邀请ID
 * @param extra 扩展透传数据
 */
- (void)receivedCoHostInviteWithUser:(LiveUserModel *)inviter
                            linkerID:(NSString *)linkerID
                               extra:(NSString *)extra;

/**
 * @brief 收到多主播连麦拒绝
 * @param invitee 被邀请的用户模型
 */
- (void)receivedCoHostRefuseWithUser:(LiveUserModel *)invitee;

/**
 * @brief 收到多主播连麦成功
 * @param invitee 被邀请的用户模型
 * @param linkerID 邀请ID
 */
- (void)receivedCoHostSucceedWithUser:(LiveUserModel *)invitee
                             linkerID:(NSString *)linkerID;

/**
 * @brief 收到多主播连麦消息,远端主播加入连麦
 * @param userlList 连麦用户列表
 * @param otherRoomId 对方房间ID
 * @param otherToken 对方房间的RTC Token
 */
- (void)receivedCoHostJoin:(NSArray<LiveUserModel *> *)userlList
         otherAnchorRoomId:(NSString *)otherRoomId
          otherAnchorToken:(NSString *)otherToken;

/**
 * @brief 收到多主播连麦结束
 */
- (void)receivedCoHostEnd;

#pragma mark - Listener Guests

/**
 * @brief 收到主播嘉宾连麦申请
 * @param applicant 申请者用户模型
 * @param linkerID 连麦ID
 * @param extra 扩展透传数据
 */
- (void)receivedAddGuestsApplyWithUser:(LiveUserModel *)applicant
                              linkerID:(NSString *)linkerID
                                 extra:(NSString *)extra;

/**
 * @brief 收到主播嘉宾连麦邀请
 * @param inviter 邀请者用户模型
 * @param linkerID 连麦ID
 * @param extra 扩展透传数据
 */
- (void)receivedAddGuestsInviteWithUser:(LiveUserModel *)inviter
                               linkerID:(NSString *)linkerID
                                  extra:(NSString *)extra;

/**
 * @brief 收到主播嘉宾连麦拒绝
 * @param invitee 被邀请嘉宾用户模型
 */
- (void)receivedAddGuestsRefuseWithUser:(LiveUserModel *)invitee;

/**
 * @brief 收到主播嘉宾连麦相机、麦克风开关变化
 * @param uid 嘉宾用户ID
 * @param camera 嘉宾相机开关状态
 * @param mic 嘉宾麦克风开关状态
 */
- (void)receivedAddGuestsManageGuestMedia:(NSString *)uid
                                   camera:(NSInteger)camera
                                      mic:(NSInteger)mic;

/**
 * @brief 收到主播嘉宾连麦开始
 * @param invitee 被邀请嘉宾用户模型
 * @param linkerID 连麦ID
 * @param rtcRoomID RTC 房间ID
 * @param rtcToken RTC Token
 */
- (void)receivedAddGuestsSucceedWithUser:(LiveUserModel *)invitee
                                linkerID:(NSString *)linkerID
                               rtcRoomID:(NSString *)rtcRoomID
                                rtcToken:(NSString *)rtcToken;

/**
 * @brief 收到主播嘉宾连麦，嘉宾加入消息
 * @param userlList 当前连麦用户列表
 */
- (void)receivedAddGuestsJoin:(NSArray<LiveUserModel *> *)userlList;

/**
 * @brief 收到主播嘉宾连麦，嘉宾离开消息
 * @param uid 嘉宾用户ID
 * @param userList 当前连麦用户列表
 */
- (void)receivedAddGuestsRemoveWithUser:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList;

/**
 * @brief 收到主播嘉宾连麦结束
 */
- (void)receivedAddGuestsEnd;


@end
