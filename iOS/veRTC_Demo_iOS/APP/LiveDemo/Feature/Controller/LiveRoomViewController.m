//
//  LiveRoomViewController.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRoomViewController.h"
#import "LiveAddGuestsCompoments.h"
#import "LiveCoHostCompoments.h"
#import "LiveHostAvatarView.h"
#import "LiveIMCompoments.h"
#import "LivePeopleNumView.h"
#import "LivePullStreamCompoments.h"
#import "LivePushStreamCompoments.h"
#import "LiveRTCManager.h"
#import "LiveRoomBottomView.h"
#import "LiveRoomGuestSettingView.h"
#import "LiveRoomHostSettingView.h"
#import "LiveRoomViewController+SocketControl.h"
#import "NetworkingTool.h"

@interface LiveRoomViewController () <LiveRoomBottomViewDelegate>

@property (nonatomic, strong) LivePeopleNumView *peopleNumView;
@property (nonatomic, strong) LiveHostAvatarView *hostAvatarView;
@property (nonatomic, strong) LiveRoomBottomView *bottomView;
@property (nonatomic, strong) LiveIMCompoments *imCompoments;
@property (nonatomic, strong) LiveCoHostCompoments *coHostCompoments;
@property (nonatomic, strong) LiveAddGuestsCompoments *addGuestsCompoments;
@property (nonatomic, strong) LivePullStreamCompoments *livePullStreamCompoments;
@property (nonatomic, strong) LivePushStreamCompoments *livePushStreamCompoments;
@property (nonatomic, strong) UIView *liveView;

@property (nonatomic, strong) LiveRoomInfoModel *liveRoomModel;
@property (nonatomic, strong) LiveReconnectModel *reconnectModel;
@property (nonatomic, strong) LiveUserModel *currentUserModel;
@property (nonatomic, strong) NSString *streamPushUrl;
@property (nonatomic, strong) NSString *linkerID;

@property (nonatomic, strong) void (^reconnectCallback)(NSInteger errorCode);

@end

@implementation LiveRoomViewController

- (instancetype)initWithRoomModel:(LiveRoomInfoModel *)liveRoomModel
                   reconnectModel:(LiveReconnectModel *)reconnectModel
                    streamPushUrl:(NSString *)streamPushUrl {
    self = [super init];
    if (self) {
        [UIApplication sharedApplication].idleTimerDisabled = YES;
        _liveRoomModel = liveRoomModel;
        _reconnectModel = reconnectModel;
        _streamPushUrl = streamPushUrl;
        
        [[LiveRTCManager shareRtc] configEngineWithPushUrl:streamPushUrl];
        __weak __typeof(self) wself = self;
        [LiveRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString * _Nonnull roomId, NSInteger errorCode, NSInteger joinType) {
            [wself receivedJoinRoom:roomId errorCode:errorCode joinType:joinType];
        };
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self addSocketListener];
    
    __weak __typeof(self) wself = self;
    [self reconnectWithRTCJoinRoom:^(NSInteger errorCode) {
        [wself restoreRoom];
    }];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
}

#pragma mark - LiveRoomBottomViewDelegate

- (void)liveRoomBottomView:(LiveRoomBottomView *_Nonnull)liveRoomBottomView
                itemButton:(LiveRoomItemButton *_Nullable)itemButton
                roleStatus:(BottomRoleStatus)roleStatus {
    if (itemButton.currentState == LiveRoomItemButtonStatePK) {
        if (self.addGuestsCompoments.isConnect) {
            [[ToastComponents shareToastComponents] showWithMessage:@"与观众连线中，无法发起主播连线"];
        } else {
            [self clickBottomCoHost:liveRoomBottomView
                         itemButton:itemButton
                         roleStatus:roleStatus];
        }
    } else if (itemButton.currentState == LiveRoomItemButtonStateChat) {
        if (self.coHostCompoments.isConnect) {
            [[ToastComponents shareToastComponents] showWithMessage:@"主播连线中，无法发起观众连线"];
        } else {
            [self clickBottomAddGuests:liveRoomBottomView
                            itemButton:itemButton
                            roleStatus:roleStatus];
        }
    } else if (itemButton.currentState == LiveRoomItemButtonStateBeauty) {
        if (!self.beautyCompoments) {
            [[ToastComponents shareToastComponents] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
            return;
        }
        if (roleStatus == BottomRoleStatusHost) {
            [self.beautyCompoments showWithType:EffectBeautyRoleTypeHost
                                  fromSuperView:self.view dismissBlock:^(BOOL result) {
                
            }];
        } else if (roleStatus == BottomRoleStatusGuests) {
            [self.beautyCompoments showWithType:EffectBeautyRoleTypeGuest
                                  fromSuperView:self.view dismissBlock:^(BOOL result) {
                
            }];
        }
        
    } else if (itemButton.currentState == LiveRoomItemButtonStateSet) {
        [self clickBottomSettingWithRoleStatus:roleStatus];
    } else if (itemButton.currentState == LiveRoomItemButtonStateEnd) {
        [self clickBottomEndLive];
    }
}

#pragma mark - Reconnect
- (void)receivedJoinRoom:(NSString *)roomId
               errorCode:(NSInteger)errorCode
                joinType:(NSInteger)joinType {
    if ([roomId isEqualToString:self.liveRoomModel.roomID]) {
        if (errorCode == 0 && joinType == 0) {
            [self restoreRoomAfterJoinRTMRoom];
        }
        if (joinType != 0 && errorCode == 0) {
            [self reconnectLiveRoom];
        }
        return;
    }
    if (self.reconnectCallback) {
        self.reconnectCallback(errorCode);
        self.reconnectCallback = nil;
    }
}

- (void)reconnectLiveRoom {
    __weak __typeof(self) wself = self;
    [LiveRTMManager reconnect:self.liveRoomModel.roomID
                               block:^(LiveReconnectModel *reconnectModel, RTMACKModel *model) {
        if (model.result) {
            wself.reconnectModel = reconnectModel;
            [wself restoreRoom];
        } else if (model.code == RTMStatusCodeUserIsInactive ||
                   model.code == RTMStatusCodeRoomDisbanded ||
                   model.code == RTMStatusCodeUserNotFound) {
            [wself hangUp];
        } else {
            
        }
    }];
}

#pragma mark - Network request

- (void)loadDataWithJoinLiveRoom {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveJoinLiveRoom:self.liveRoomModel.roomID
                                      block:^(LiveRoomInfoModel *roomModel, LiveUserModel *userModel, RTMACKModel *model) {
        if (model.result) {
            [wself restoreRoomWithRoomInfoModel:roomModel
                                      userModel:userModel
                                    rtcUserList:@[]];
        } else {
            AlertActionModel *alertModel = [[AlertActionModel alloc] init];
            alertModel.title = @"确定";
            alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
                if ([action.title isEqualToString:@"确定"]) {
                    [wself hangUp];
                }
            };
            [[AlertActionManager shareAlertActionManager] showWithMessage:@"加入房间失败，回到房间列表页" actions:@[ alertModel ]];
        }
    }];
}

// CoHost
- (void)loadDataWithAnchorLinkmicReply:(LiveUserModel *)inviter
                              linkerID:(NSString *)linkerID
                             replyType:(LiveInviteReply)replyType {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAnchorLinkmicReply:self.liveRoomModel.roomID
                                    inviterRoomID:inviter.roomID
                                    inviterUserID:inviter.uid
                                         linkerID:linkerID
                                        replyType:replyType
                                            block:^(NSString * _Nullable rtcRoomID, NSString * _Nullable rtcToken, NSArray<LiveUserModel *> * _Nullable userList, RTMACKModel * _Nonnull model) {
        if (model.result) {
            if (replyType == LiveInviteReplyPermitted) {
                wself.linkerID = linkerID;
                [wself.coHostCompoments readyJoinRTCRoomByToken:rtcToken
                                                         roomID:rtcRoomID
                                                         userID:[LocalUserComponents userModel].uid];
                [wself receivedCoHostJoin:userList];
            }
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAnchorLinkmicFinish {
    [LiveRTMManager liveAnchorLinkmicFinish:self.liveRoomModel.roomID
                                          linkerID:self.linkerID
                                             block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

// Add Guests - host
- (void)loadDataWithPermitAudienceLinkmic:(NSString *)audienceRoomID
                           audienceUserID:(NSString *)audienceUserID
                                 linkerID:(NSString *)linkerID
                                    reply:(LiveInviteReply)reply {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAudienceLinkmicPermit:self.liveRoomModel.roomID
                                      audienceRoomID:audienceRoomID
                                      audienceUserID:audienceUserID
                                            linkerID:linkerID
                                          permitType:reply
                                               block:^(NSString *rtcRoomID,
                                                       NSString *rtcToken,
                                                       NSArray<LiveUserModel *> *userList,
                                                       RTMACKModel *model) {
        if (model.result) {
            if (reply == LiveInviteReplyPermitted) {
                wself.linkerID = linkerID;
                [wself.addGuestsCompoments readyJoinRTCRoomByToken:rtcToken
                                                            roomID:rtcRoomID
                                                            userID:[LocalUserComponents userModel].uid];
                [wself receivedAddGuestsJoin:userList];
            }
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAudienceLinkmicFinish {
    [LiveRTMManager liveAudienceLinkmicFinish:self.liveRoomModel.roomID
                                               block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

// Add Guests - Guests
- (void)loadDataWithReplyAudienceLinkmic:(NSString *)linkerID
                                   reply:(LiveInviteReply)reply {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAudienceLinkmicReply:self.liveRoomModel.roomID
                                           linkerID:linkerID
                                          replyType:reply
                                              block:^(NSString *rtcRoomID,
                                                      NSString *rtcToken,
                                                      NSArray<LiveUserModel *> *userList,
                                                      RTMACKModel *model) {
        if (model.result) {
            if (reply == LiveInviteReplyPermitted) {
                wself.linkerID = linkerID;
                [wself.addGuestsCompoments readyJoinRTCRoomByToken:rtcToken
                                                            roomID:rtcRoomID
                                                            userID:[LocalUserComponents userModel].uid];
                [wself receivedAddGuestsJoin:userList];
                // Guests update bottom ui
                [wself.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
            }
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAudienceLinkmicLeave {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAudienceLinkmicLeave:self.liveRoomModel.roomID
                                           linkerID:self.linkerID
                                              block:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            // Audience update bottom ui
            [wself.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithupdateRes:(LiveUserModel *)loginUserModel {
    CGSize videoSize = (loginUserModel.role == 2) ? [LiveSettingVideoConfig defultVideoConfig].videoSize : [LiveSettingVideoConfig defultVideoConfig].guestVideoSize;
    [LiveRTMManager liveUpdateResWithSize:videoSize
                                      roomID:self.liveRoomModel.roomID
                                       block:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            [[LiveRTCManager shareRtc] updateRes:videoSize];
        }
    }];
}

#pragma mark - SocketControl

- (void)addUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount {
    LiveIMModel *imModel = [[LiveIMModel alloc] init];
    imModel.isJoin = YES;
    imModel.userModel = userModel;
    [self.imCompoments addIM:imModel];
    
    [self.addGuestsCompoments updateList];
    [self.peopleNumView updateTitleLabel:audienceCount];
}

- (void)removeUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount {
    LiveIMModel *imModel = [[LiveIMModel alloc] init];
    imModel.isJoin = NO;
    imModel.userModel = userModel;
    [self.imCompoments addIM:imModel];
    
    [self.addGuestsCompoments updateList];
    [self.peopleNumView updateTitleLabel:audienceCount];
}

// CoHost
- (void)receivedCoHostInviteWithUser:(LiveUserModel *)inviter
                            linkerID:(NSString *)linkerID
                               extra:(NSString *)extra {
    __weak __typeof(self) wself = self;
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"接受";
    alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
        if ([action.title isEqualToString:@"接受"]) {
            [wself loadDataWithAnchorLinkmicReply:inviter
                                         linkerID:linkerID
                                        replyType:LiveInviteReplyPermitted];
        }
    };
    
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = @"拒绝";
    alertCancelModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
        if ([action.title isEqualToString:@"拒绝"]) {
            [wself loadDataWithAnchorLinkmicReply:inviter
                                         linkerID:linkerID
                                        replyType:LiveInviteReplyForbade];
        }
    };
    
    NSString *message = [NSString stringWithFormat:@"%@邀请你进行主播连线，是否接受?", inviter.name];
    [[AlertActionManager shareAlertActionManager] showWithMessage:message
                                                          actions:@[alertCancelModel, alertModel]
                                                        hideDelay:LiveApplyOvertimeInterval];
}

- (void)receivedCoHostRefuseWithUser:(LiveUserModel *)invitee {
    NSString *message = @"主播暂时有点事，拒绝了你的邀请";
    [[ToastComponents shareToastComponents] showWithMessage:message];
    [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusNone];
}

- (void)receivedCoHostSucceedWithUser:(LiveUserModel *)invitee
                             linkerID:(NSString *)linkerID
                            rtcRoomID:(NSString *)rtcRoomID
                             rtcToken:(NSString *)rtcToken {
    self.linkerID = linkerID;
    [self.coHostCompoments readyJoinRTCRoomByToken:rtcToken
                                            roomID:rtcRoomID
                                            userID:[LocalUserComponents userModel].uid];
}

- (void)receivedCoHostJoin:(NSArray<LiveUserModel *> *)userlList {
    [self.livePushStreamCompoments close];
    [self.coHostCompoments showCoHost:self.liveView
                        streamPushUrl:self.streamPushUrl
                        userModelList:userlList
                       loginUserModel:self.currentUserModel];
    [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusClose];
}

- (void)receivedCoHostEnd {
    NSString *message = @"主播已断开连线";
    [[ToastComponents shareToastComponents] showWithMessage:message];
    [self.coHostCompoments closeCoHost];
    [self.coHostCompoments leaveRTCRoom];
    [self.livePushStreamCompoments openWithUserModel:self.currentUserModel];
    [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusNone];
}

// Add Guests - host

- (void)receivedAddGuestsApplyWithUser:(LiveUserModel *)applicant
                              linkerID:(NSString *)linkerID
                                 extra:(NSString *)extra {
    if ([self isHost]) {
        __weak __typeof(self) wself = self;
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"同意";
        alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
            if ([action.title isEqualToString:@"同意"]) {
                [wself loadDataWithPermitAudienceLinkmic:applicant.roomID
                                          audienceUserID:applicant.uid
                                                linkerID:linkerID
                                                   reply:LiveInviteReplyPermitted];
            }
        };
        
        AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
        alertCancelModel.title = @"拒绝";
        alertCancelModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
            if ([action.title isEqualToString:@"拒绝"]) {
                [wself loadDataWithPermitAudienceLinkmic:applicant.roomID
                                          audienceUserID:applicant.uid
                                                linkerID:linkerID
                                                   reply:LiveInviteReplyForbade];
            }
        };
        
        NSString *message = [NSString stringWithFormat:@"观众%@向你发来连线申请", applicant.name];
        [[AlertActionManager shareAlertActionManager] showWithMessage:message
                                                              actions:@[alertCancelModel, alertModel]
                                                            hideDelay:LiveApplyOvertimeInterval];
    }
}

// Add Guests - Guests

- (void)receivedAddGuestsInviteWithUser:(LiveUserModel *)inviter
                               linkerID:(NSString *)linkerID
                                  extra:(NSString *)extra {
    __weak __typeof(self) wself = self;
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"同意";
    alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
        if ([action.title isEqualToString:@"同意"]) {
            [wself loadDataWithReplyAudienceLinkmic:linkerID
                                              reply:LiveInviteReplyPermitted];
        }
    };
    
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = @"拒绝";
    alertCancelModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
        if ([action.title isEqualToString:@"拒绝"]) {
            [wself loadDataWithReplyAudienceLinkmic:linkerID
                                              reply:LiveInviteReplyForbade];
        }
    };
    
    [[AlertActionManager shareAlertActionManager] showWithMessage:@"是否接受主播的连麦邀请?"
                                                          actions:@[alertCancelModel, alertModel]
                                                        hideDelay:LiveApplyOvertimeInterval];
}

- (void)receivedAddGuestsManageGuestMedia:(NSString *)uid
                                   camera:(NSInteger)camera
                                      mic:(NSInteger)mic {
    if (![uid isEqualToString:[LocalUserComponents userModel].uid]) {
        return;
    }
    BOOL cameraBool = self.currentUserModel.camera;
    BOOL micBool = self.currentUserModel.mic;
    if (camera != -1) {
        cameraBool = camera == 1 ? YES : NO;
    }
    if (mic != -1) {
        micBool = mic == 1 ? YES : NO;
    }
    if (self.addGuestsCompoments.isConnect) {
        [self.addGuestsCompoments updateGuestsCamera:cameraBool uid:uid];
        [self.addGuestsCompoments updateGuestsMic:micBool uid:uid];
        if (camera != -1 && !cameraBool) {
            [[ToastComponents shareToastComponents] showWithMessage:@"你的摄像头已被主播关闭"];
        }
        if (mic != -1 && !micBool) {
            [[ToastComponents shareToastComponents] showWithMessage:@"你已被主播禁麦"];
        }
    }
    self.currentUserModel.camera = cameraBool;
    self.currentUserModel.mic = micBool;
    [self.settingCompoments refreshGuestSettingView];
    [LiveRTMManager liveUpdateMediaStatus:self.liveRoomModel.roomID
                                             mic:micBool
                                          camera:cameraBool
                                           block:nil];
}

// Add Guests - All
- (void)receivedAddGuestsSucceedWithUser:(LiveUserModel *)invitee
                                linkerID:(NSString *)linkerID
                               rtcRoomID:(NSString *)rtcRoomID
                                rtcToken:(NSString *)rtcToken {
    self.linkerID = linkerID;
    [self.addGuestsCompoments readyJoinRTCRoomByToken:rtcToken
                                               roomID:rtcRoomID
                                               userID:[LocalUserComponents userModel].uid];
    if (![self isHost]) {
        // Guests update bottom ui
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
    }
}

- (void)receivedAddGuestsRefuseWithUser:(LiveUserModel *)invitee {
    NSString *message = @"";
    if ([self isHost]) {
        message = [NSString stringWithFormat:@"观众%@拒绝连线", invitee.name];
        [self.addGuestsCompoments updateList];
    } else {
        message = @"主播拒绝和你连线";
        self.currentUserModel.status = LiveInteractStatusOther;
        [self.addGuestsCompoments closeApply];
    }
    [[ToastComponents shareToastComponents] showWithMessage:message];
}

- (void)receivedAddGuestsJoin:(NSArray<LiveUserModel *> *)userList {
    if ([self isHost]) {
        [self.livePushStreamCompoments close];
        [self.addGuestsCompoments showAddGuests:self.liveView
                                  streamPushUrl:_streamPushUrl
                                        hostUid:self.liveRoomModel.anchorUserID
                                       userList:userList];
        [self.addGuestsCompoments updateGuests:userList];
        [self.addGuestsCompoments updateList];
        
        if (userList.count == 2) {
            [[ToastComponents shareToastComponents] showWithMessage:@"点击观众画面可进行麦位管理"];
        }
    } else {
        [self.livePullStreamCompoments close];
        [self.addGuestsCompoments showAddGuests:self.liveView
                                  streamPushUrl:_streamPushUrl
                                        hostUid:self.liveRoomModel.anchorUserID
                                       userList:userList];
        [self.addGuestsCompoments updateGuests:userList];
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
        self.currentUserModel.status = LiveInteractStatusAudienceLink;
        [self.addGuestsCompoments closeApply];
    }
}

- (void)receivedAddGuestsRemoveWithUser:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList {
    if ([self isHost]) {
        NSString *userName = [self.addGuestsCompoments removeAddGuestsUid:uid userList:userList];
        if (userName != nil) {
            NSString *message = [NSString stringWithFormat:@"%@断开了与您的连麦", userName];
            [[ToastComponents shareToastComponents] showWithMessage:message];
        }
    } else {
        if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
            [[ToastComponents shareToastComponents] showWithMessage:@"主播已和你断开连线"];
            [self.addGuestsCompoments closeAddGuests];
            [self.addGuestsCompoments closeApply];
            [self.addGuestsCompoments leaveRTCRoom];
            [self.livePullStreamCompoments open:self.liveRoomModel];
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
            self.currentUserModel.status = LiveInteractStatusOther;
        } else {
            NSString *userName = [self.addGuestsCompoments removeAddGuestsUid:uid userList:userList];
            if (userName != nil) {
                NSString *message = [NSString stringWithFormat:@"%@断开了与您的连麦", userName];
                [[ToastComponents shareToastComponents] showWithMessage:message];
            }
        }
    }
}

- (void)receivedAddGuestsEnd {
    if ([self isHost]) {
        [self.addGuestsCompoments closeAddGuests];
        [self.addGuestsCompoments updateList];
        [self.addGuestsCompoments leaveRTCRoom];
        [self.livePushStreamCompoments openWithUserModel:self.currentUserModel];
    } else {
        [[ToastComponents shareToastComponents] showWithMessage:@"主播已和你断开连线"];
        [self.addGuestsCompoments closeAddGuests];
        [self.addGuestsCompoments closeApply];
        [self.addGuestsCompoments leaveRTCRoom];
        [self.livePullStreamCompoments open:self.liveRoomModel];
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
        self.currentUserModel.status = LiveInteractStatusOther;
    }
}

- (void)receivedLiveEnd:(BOOL)isKick type:(NSString *)type {
    if (!isKick) {
        if ([type integerValue] == 2) {
            [[ToastComponents shareToastComponents] showWithMessage:@"本次体验时间已超过20mins" delay:0.8];
        } else if ([type integerValue] == 3) {
            [[ToastComponents shareToastComponents] showWithMessage:@"直播间内容违规，直播间已被关闭" delay:0.8];
        } else {
            if (![self isHost]) {
                [[ToastComponents shareToastComponents] showWithMessage:@"主播已关闭直播" delay:0.8];
            }
        }
    } else {
        [[ToastComponents shareToastComponents] showWithMessage:@"相同ID用户已登录，您已被强制下线！" delay:0.8];
    }
    [self navigationControllerPop:isKick];
}

- (void)receivedRoomStatus:(LiveInteractStatus)status {
    self.liveRoomModel.hostUserModel.status = status;
    if (![self isHost]) {
        /// 画面与消息会有延迟，目前此处理方式为兼容处理，等之后替换回能够发送SEI消息的推拉流库需改回SEI发送通知
        SEL selector = @selector(delayUpdatePullStatus:);
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:selector object:nil];
        [self performSelector:selector withObject:@(status) afterDelay:3];
    }
}

- (void)delayUpdatePullStatus:(NSNumber *)statusNum {
    [self updatePullStatus:statusNum.integerValue];
}

- (void)receivedAddGuestsMediaChangeWithUser:(NSString *)uid
                                 operatorUid:(NSString *)operatorUid
                                      camera:(BOOL)camera
                                         mic:(BOOL)mic {
    [self changeMediaWithUser:uid camera:camera mic:mic];
}

- (void)receivedLeaveTemporary:(NSString *)uid
                      userName:(NSString *)userName
                      userRole:(NSString *)userRole {
    NSString *message = @"";
    if ([userRole integerValue] == 2) {
        message = @"主播暂时离开";
    } else {
        message = @"嘉宾暂时离开";
    }
    [[ToastComponents shareToastComponents] showWithMessage:message];
}

#pragma mark - Private Action
- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.settingCompoments close];
    [self.beautyCompoments close];
}

- (void)addSubviewAndConstraints {
    [self.view addSubview:self.liveView];
    [self.liveView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];
    
    [self.view addSubview:self.hostAvatarView];
    [self.hostAvatarView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(36);
        make.left.equalTo(self.view).offset(16);
        make.top.equalTo(self.view).offset([DeviceInforTool getStatusBarHight] + 2);
    }];
    
    [self.view addSubview:self.peopleNumView];
    [self.peopleNumView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(32);
        make.right.equalTo(self.view).offset(-16);
        make.top.equalTo(self.view).offset([DeviceInforTool getStatusBarHight] + 5);
    }];
    
    [self.view addSubview:self.bottomView];
    [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-16);
        make.bottom.mas_equalTo(-32 - [DeviceInforTool getVirtualHomeHeight]);
        make.height.mas_equalTo(36);
        make.width.mas_equalTo(0);
    }];
    
    [self imCompoments];
}

- (void)showEndCoHostAlert {
    __weak __typeof(self) wself = self;
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"断开连线";
    alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
        if ([action.title isEqualToString:@"断开连线"]) {
            [wself loadDataWithAnchorLinkmicFinish];
        }
    };
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = @"取消";
    NSString *message = @"确定断开连线？";
    [[AlertActionManager shareAlertActionManager] showWithMessage:message actions:@[ alertCancelModel, alertModel ]];
}

- (void)updateLayoutToRole:(LiveUserModel *)userModel
               rtcUserList:(NSArray<LiveUserModel *> *)rtcUserList {
    if (userModel.role == LiveUserRoleAudience) {
        // Audience
        if (userModel.status == LiveInteractStatusAudienceLink) {
            // Add Guests
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
            [self.addGuestsCompoments showAddGuests:self.liveView
                                      streamPushUrl:_streamPushUrl
                                            hostUid:self.liveRoomModel.anchorUserID
                                           userList:rtcUserList];
        } else {
            // None
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
            [self.livePullStreamCompoments open:self.liveRoomModel];
            if (self.addGuestsCompoments.isConnect) {
                [self.addGuestsCompoments closeAddGuests];
                [self.addGuestsCompoments leaveRTCRoom];
            }
        }
    } else if (userModel.role == LiveUserRoleHost) {
        // Host
        if (userModel.status == LiveInteractStatusHostLink) {
            // Co Hsot
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusClose];
            [self.coHostCompoments showCoHost:self.liveView
                                streamPushUrl:_streamPushUrl
                                userModelList:rtcUserList
                               loginUserModel:userModel];
        } else if (userModel.status == LiveInteractStatusAudienceLink) {
            // Add Guests
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.addGuestsCompoments showAddGuests:self.liveView
                                      streamPushUrl:_streamPushUrl
                                            hostUid:self.liveRoomModel.anchorUserID
                                           userList:rtcUserList];
        } else {
            // None
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.livePushStreamCompoments openWithUserModel:userModel];
            [self.livePushStreamCompoments updateHostMic:userModel.mic
                                                  camera:userModel.camera];
            if (self.addGuestsCompoments.isConnect) {
                [self.addGuestsCompoments closeAddGuests];
                [self.addGuestsCompoments leaveRTCRoom];
            }
            if (self.coHostCompoments.isConnect) {
                [self.coHostCompoments closeCoHost];
                [self.coHostCompoments leaveRTCRoom];
            }
        }
    } else {
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
        [self.livePullStreamCompoments open:self.liveRoomModel];
        if (self.addGuestsCompoments.isConnect) {
            [self.addGuestsCompoments closeAddGuests];
            [self.addGuestsCompoments leaveRTCRoom];
        }
    }
    [self addBeautyEffect:userModel];
}

- (void)addBeautyEffect:(LiveUserModel *)userModel {
    BOOL beautyEnable = NO;
    if (userModel.role == LiveUserRoleAudience) {
        if (userModel.status == LiveInteractStatusAudienceLink) {
            beautyEnable = YES;
        }
    } else if (userModel.role == LiveUserRoleHost) {
        beautyEnable = YES;
    } else {
        beautyEnable = NO;
    }
    if (beautyEnable) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            //CV initialization and setting special effects together will cause deadlock, so you need to delay setting special effects
            [self.beautyCompoments resumeLocalEffect];
        });
    }
}

- (void)clickBottomAddGuests:(LiveRoomBottomView *)bottomView
                  itemButton:(LiveRoomItemButton *_Nullable)itemButton
                  roleStatus:(BottomRoleStatus)roleStatus {
    __weak __typeof(self) wself = self;
    if (roleStatus == BottomRoleStatusAudience) {
        if (self.liveRoomModel.hostUserModel.status == LiveInteractStatusHostLink) {
            [[ToastComponents shareToastComponents] showWithMessage:@"主播正在发起双主播连线"];
        } else {
            [self.addGuestsCompoments showApply:self.currentUserModel
                                         hostID:self.liveRoomModel.anchorUserID];
        }
    } else if (roleStatus == BottomRoleStatusHost) {
        if ([bottomView getButtonTouchStatus:LiveRoomItemButtonStatePK]) {
            [[ToastComponents shareToastComponents] showWithMessage:@"正在等待被邀主播的应答"];
        } else {
            [self.addGuestsCompoments showList:^(LiveAddGuestsDismissState state) {
                if (state == LiveAddGuestsDismissStateCloseConnect) {
                    [wself showCloseAddGuests];
                }
            }];
        }
    } else if (roleStatus == BottomRoleStatusGuests) {
        [self showCloseAddGuests];
    } else {
        
    }
}

- (void)clickBottomCoHost:(LiveRoomBottomView *)bottomView
               itemButton:(LiveRoomItemButton *_Nullable)itemButton
               roleStatus:(BottomRoleStatus)roleStatus {
    if (itemButton.touchStatus == LiveRoomItemTouchStatusClose) {
        [self showEndCoHostAlert];
    } else if (itemButton.touchStatus == LiveRoomItemTouchStatusIng) {
        // Waiting for the response from the invited host
    } else {
        // show list view
        [self.coHostCompoments showInviteList:nil];
    }
}

- (void)clickBottomSettingWithRoleStatus:(BottomRoleStatus)roleStatus {
    if (roleStatus == BottomRoleStatusHost) {
        if (self.addGuestsCompoments.isConnect || self.coHostCompoments.isConnect) {
            [self.settingCompoments showWithType:LiveRoomSettingTypeHostChat
                                   fromSuperView:self.view
                                          roomID:self.liveRoomModel
                                       userModel:self.currentUserModel];
        } else {
            [self.settingCompoments showWithType:LiveRoomSettingTypeHostLiving
                                   fromSuperView:self.view
                                          roomID:self.liveRoomModel
                                       userModel:self.currentUserModel];
        }
    } else if (roleStatus == BottomRoleStatusGuests) {
        [self.settingCompoments showWithType:LiveRoomSettingTypeGuest
                               fromSuperView:self.view
                                      roomID:self.liveRoomModel
                                   userModel:self.currentUserModel];
    } else if (roleStatus == BottomRoleStatusAudience) {
        [self.settingCompoments showWithType:LiveRoomSettingTypeAudience
                               fromSuperView:self.view
                                      roomID:self.liveRoomModel
                                   userModel:nil];
    }
}

- (void)clickBottomEndLive {
    if ([self isHost]) {
        __weak __typeof(self) wself = self;
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"结束直播";
        alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
            
            if ([action.title isEqualToString:@"结束直播"]) {
                [wself hangUp];
            }
        };
        AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
        alertCancelModel.title = @"取消";
        NSString *message = @"是否结束直播？";
        [[AlertActionManager shareAlertActionManager] showWithMessage:message actions:@[ alertCancelModel, alertModel ]];
    } else {
        [self hangUp];
    }
}

- (void)showCloseAddGuests {
    if ([self isHost]) {
        __weak __typeof(self) wself = self;
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"关闭";
        alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
            if ([action.title isEqualToString:@"关闭"]) {
                [wself loadDataWithAudienceLinkmicFinish];
            }
        };
        
        AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
        alertCancelModel.title = @"取消";
        
        NSString *message = [NSString stringWithFormat:@"正在与%ld位观众连线，是否确认关闭观众连线？", (long)self.addGuestsCompoments.guestList.count];
        [[AlertActionManager shareAlertActionManager] showWithMessage:message actions:@[ alertCancelModel, alertModel ]];
    } else {
        __weak __typeof(self) wself = self;
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"断开连线";
        alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
            if ([action.title isEqualToString:@"断开连线"]) {
                [wself loadDataWithAudienceLinkmicLeave];
            }
        };
        
        AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
        alertCancelModel.title = @"取消";
        
        NSString *message = @"是否与主播断开连线";
        [[AlertActionManager shareAlertActionManager] showWithMessage:message actions:@[ alertCancelModel, alertModel ]];
    }
}

- (void)restoreRoom {
    if (NOEmptyStr(self.reconnectModel.roomModel.roomID)) {
        // reconnect
        LiveRoomInfoModel *roomInfoModel = self.reconnectModel.roomModel;
        roomInfoModel.hostUserModel = self.liveRoomModel.hostUserModel;
        [self restoreRoomWithRoomInfoModel:self.reconnectModel.roomModel
                                 userModel:self.reconnectModel.loginUserModel
                               rtcUserList:self.reconnectModel.rtcUserList];
        self.reconnectModel = nil;
        return;
    }
    
    if ([self isHost]) {
        [self restoreRoomWithRoomInfoModel:self.liveRoomModel
                                 userModel:self.liveRoomModel.hostUserModel
                               rtcUserList:@[]];
    } else {
        [self loadDataWithJoinLiveRoom];
        [self.livePullStreamCompoments open:self.liveRoomModel];
    }
}

- (void)restoreRoomWithRoomInfoModel:(LiveRoomInfoModel *)roomModel
                           userModel:(LiveUserModel *)userModel
                         rtcUserList:(NSArray<LiveUserModel *> *)rtcUserList {
    self.liveRoomModel = roomModel;
    self.currentUserModel = userModel;
    
    [self addSubviewAndConstraints];
    self.hostAvatarView.hostName = roomModel.anchorUserName;
    [self.peopleNumView updateTitleLabel:roomModel.audienceCount];
    [self updateLayoutToRole:userModel rtcUserList:rtcUserList];
    [self updatePullStatus:roomModel.hostUserModel.status];
    [self joinRTMRoomByToken:roomModel.rtmToken roomID:roomModel.roomID];
    [self changeMediaWithUser:roomModel.hostUserModel.uid
                       camera:roomModel.hostUserModel.camera
                          mic:roomModel.hostUserModel.mic];
}

- (void)restoreRoomAfterJoinRTMRoom {
    [self loadDataWithupdateRes:self.currentUserModel];
}

- (BOOL)isHost {
    return [self.liveRoomModel.anchorUserID isEqualToString:[LocalUserComponents userModel].uid];
}

- (void)hangUp {
    __weak __typeof(self) wself = self;
    if ([self isHost]) {
        // Host
        [LiveRTMManager liveFinishLive:self.liveRoomModel.roomID
                                        block:^(RTMACKModel *_Nonnull model) {
            __strong __typeof(wself) strongSelf = wself;
            if (strongSelf.hangUpBlock) {
                strongSelf.hangUpBlock(model.result);
            }
        }];
    } else {
        // Audience
        [LiveRTMManager liveLeaveLiveRoom:self.liveRoomModel.roomID block:^(RTMACKModel * _Nonnull model) {
            __strong __typeof(wself) strongSelf = wself;
            if (strongSelf.hangUpBlock) {
                strongSelf.hangUpBlock(model.result);
            }
        }];
    }
    
    [self navigationControllerPop:NO];
}

- (void)navigationControllerPop:(BOOL)isKick {
    [LiveRTCManager shareRtc].rtcJoinRoomBlock = nil;
    UIViewController *jumpVC = nil;
    for (UIViewController *vc in self.navigationController.viewControllers) {
        NSString *vcName = @"LiveRoomListsViewController";
        if (isKick) {
            vcName = @"MenuViewController";
        }
        if ([NSStringFromClass([vc class]) isEqualToString:vcName]) {
            jumpVC = vc;
            break;
        }
    }
    if (jumpVC) {
        [self.navigationController popToViewController:jumpVC animated:YES];
    } else {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (void)updatePullStatus:(LiveInteractStatus)status {
    if (![self isHost]) {
        PullRenderStatus pullStatus = PullRenderStatusNone;
        if (status == LiveInteractStatusHostLink) {
            // co-host
            pullStatus = PullRenderStatusCoHst;
        }
        [self.livePullStreamCompoments updateWithStatus:pullStatus];
    }
}

- (void)reconnectWithRTCJoinRoom:(void (^)(NSInteger errorCode))block {
    LiveUserModel *joinRTCUserModel = nil;
    if (_reconnectModel.rtcUserList.count > 0) {
        for (int i = 0; i < _reconnectModel.rtcUserList.count; i++) {
            LiveUserModel *userModel = _reconnectModel.rtcUserList[i];
            if ([userModel.uid isEqualToString:[LocalUserComponents userModel].uid]) {
                joinRTCUserModel = userModel;
                break;
            }
        }
        
    }
    if (joinRTCUserModel) {
        _reconnectCallback = block;
        [[LiveRTCManager shareRtc] joinRTCRoomByToken:_reconnectModel.rtcToken
                                               roomID:_reconnectModel.rtcRoomID
                                               userID:joinRTCUserModel.uid];
    } else {
        if (block) {
            block(0);
        }
    }
}

- (void)joinRTMRoomByToken:(NSString *)token
                    roomID:(NSString *)roomID {
    [[LiveRTCManager shareRtc] joinMultiRoomByToken:token
                                             roomID:roomID
                                             userID:[LocalUserComponents userModel].uid];
}

- (void)changeMediaWithUser:(NSString *)uid
                     camera:(BOOL)camera
                        mic:(BOOL)mic {
    if ([uid isEqualToString:self.liveRoomModel.anchorUserID]) {
        self.liveRoomModel.hostUserModel.camera = camera;
        self.liveRoomModel.hostUserModel.mic = mic;
        if (self.livePushStreamCompoments.isConnect) {
            [self.livePushStreamCompoments updateHostMic:mic camera:camera];
        }
        if (self.livePullStreamCompoments.isConnect) {
            [self.livePullStreamCompoments updateHostMic:mic camera:camera];
        }
    }
    if (self.addGuestsCompoments.isConnect) {
        [self.addGuestsCompoments updateGuestsCamera:camera uid:uid];
        [self.addGuestsCompoments updateGuestsMic:mic uid:uid];
        if ([self isHost]) {
            [self.addGuestsCompoments closeSheet:uid];
        }
    }
    if (self.coHostCompoments.isConnect) {
        [self.coHostCompoments updateGuestsMic:mic uid:uid];
        [self.coHostCompoments updateGuestsCamera:camera uid:uid];
    }
    if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
        self.currentUserModel.mic = mic;
        self.currentUserModel.camera = camera;
        [self.settingCompoments refreshGuestSettingView];
    }
}

#pragma mark - getter

- (LiveHostAvatarView *)hostAvatarView {
    if (!_hostAvatarView) {
        _hostAvatarView = [[LiveHostAvatarView alloc] init];
        _hostAvatarView.backgroundColor = [UIColor colorFromRGBHexString:@"#000000" andAlpha:0.2 * 255];
        _hostAvatarView.layer.cornerRadius = 18;
        _hostAvatarView.layer.masksToBounds = YES;
    }
    return _hostAvatarView;
}

- (LivePeopleNumView *)peopleNumView {
    if (!_peopleNumView) {
        _peopleNumView = [[LivePeopleNumView alloc] init];
        _peopleNumView.backgroundColor = [UIColor colorFromRGBHexString:@"#000000" andAlpha:0.2 * 255];
        _peopleNumView.layer.cornerRadius = 16;
        _peopleNumView.layer.masksToBounds = YES;
    }
    return _peopleNumView;
}

- (LiveRoomBottomView *)bottomView {
    if (!_bottomView) {
        _bottomView = [[LiveRoomBottomView alloc] init];
        _bottomView.delegate = self;
    }
    return _bottomView;
}

- (LiveIMCompoments *)imCompoments {
    if (!_imCompoments) {
        _imCompoments = [[LiveIMCompoments alloc] initWithSuperView:self.view];
    }
    return _imCompoments;
}

- (UIView *)liveView {
    if (!_liveView) {
        _liveView = [[UIView alloc] init];
    }
    return _liveView;
}

- (LiveCoHostCompoments *)coHostCompoments {
    if (!_coHostCompoments) {
        _coHostCompoments = [[LiveCoHostCompoments alloc] initWithRoomID:self.liveRoomModel.roomID];
    }
    return _coHostCompoments;
}

- (LiveAddGuestsCompoments *)addGuestsCompoments {
    if (!_addGuestsCompoments) {
        _addGuestsCompoments = [[LiveAddGuestsCompoments alloc]
                                initWithRoomID:self.liveRoomModel.roomID];
    }
    return _addGuestsCompoments;
}

- (LivePushStreamCompoments *)livePushStreamCompoments {
    if (!_livePushStreamCompoments) {
        _livePushStreamCompoments = [[LivePushStreamCompoments alloc]
                                     initWithSuperView:self.liveView
                                     roomModel:self.liveRoomModel
                                     streamPushUrl:self.streamPushUrl];
    }
    return _livePushStreamCompoments;
}

- (LivePullStreamCompoments *)livePullStreamCompoments {
    if (!_livePullStreamCompoments) {
        _livePullStreamCompoments = [[LivePullStreamCompoments alloc] initWithSuperView:self.liveView];
    }
    return _livePullStreamCompoments;
}

- (LiveRoomSettingCompoments *)settingCompoments {
    if (!_settingCompoments) {
        _settingCompoments = [[LiveRoomSettingCompoments alloc] init];
    }
    return _settingCompoments;
}

- (BytedEffectProtocol *)beautyCompoments {
    if (!_beautyCompoments) {
        _beautyCompoments = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[LiveRTCManager shareRtc].rtcEngineKit];
    }
    return _beautyCompoments;
}

- (void)dealloc {
    [UIApplication sharedApplication].idleTimerDisabled = NO;
    [[LiveRTCManager shareRtc] leaveLiveRoom];
    [self.livePullStreamCompoments close];
}

@end
