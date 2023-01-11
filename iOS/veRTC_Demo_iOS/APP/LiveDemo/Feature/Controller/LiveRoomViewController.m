//
//  LiveRoomViewController.m
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveRoomViewController.h"
#import "LiveAddGuestsComponent.h"
#import "LiveCoHostComponent.h"
#import "LiveHostAvatarView.h"
#import "LiveIMComponent.h"
#import "LivePeopleNumView.h"
#import "LivePullStreamComponent.h"
#import "LivePushStreamComponent.h"
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
@property (nonatomic, strong) LiveIMComponent *imComponent;
@property (nonatomic, strong) LiveCoHostComponent *coHostComponent;
@property (nonatomic, strong) LiveAddGuestsComponent *addGuestsComponent;
@property (nonatomic, strong) LivePullStreamComponent *livePullStreamComponent;
@property (nonatomic, strong) LivePushStreamComponent *livePushStreamComponent;
@property (nonatomic, strong) UIView *liveView;
@property (nonatomic, strong) UIView *giftView;

@property (nonatomic, strong) LiveRoomInfoModel *liveRoomModel;

@property (nonatomic, strong) LiveUserModel *currentUserModel;
@property (nonatomic, strong) NSString *streamPushUrl;
@property (nonatomic, strong) NSString *linkerID;

@end

@implementation LiveRoomViewController

- (instancetype)initWithRoomModel:(LiveRoomInfoModel *)liveRoomModel
                    streamPushUrl:(NSString *)streamPushUrl {
    self = [super init];
    if (self) {
        [UIApplication sharedApplication].idleTimerDisabled = YES;
        _liveRoomModel = liveRoomModel;
        _streamPushUrl = streamPushUrl;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self addSocketListener];
    
    [self joinRoom];
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
        if (self.addGuestsComponent.isConnect) {
            [[ToastComponent shareToastComponent] showWithMessage:@"与观众连线中，无法发起主播连线"];
        } else {
            [self clickBottomCoHost:liveRoomBottomView
                         itemButton:itemButton
                         roleStatus:roleStatus];
        }
    } else if (itemButton.currentState == LiveRoomItemButtonStateChat) {
        if (self.coHostComponent.isConnect) {
            [[ToastComponent shareToastComponent] showWithMessage:@"主播连线中，无法发起观众连线"];
        } else {
            [self clickBottomAddGuests:liveRoomBottomView
                            itemButton:itemButton
                            roleStatus:roleStatus];
        }
    } else if (itemButton.currentState == LiveRoomItemButtonStateBeauty) {
        if (!self.beautyComponent) {
            [[ToastComponent shareToastComponent] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
            return;
        }
        if (roleStatus == BottomRoleStatusHost) {
            [self.beautyComponent showWithType:EffectBeautyRoleTypeHost
                                  fromSuperView:self.view dismissBlock:^(BOOL result) {
                
            }];
        } else if (roleStatus == BottomRoleStatusGuests) {
            [self.beautyComponent showWithType:EffectBeautyRoleTypeGuest
                                  fromSuperView:self.view dismissBlock:^(BOOL result) {
                
            }];
        }
        
    } else if (itemButton.currentState == LiveRoomItemButtonStateSet) {
        [self clickBottomSettingWithRoleStatus:roleStatus];
    } else if (itemButton.currentState == LiveRoomItemButtonStateEnd) {
        [self clickBottomEndLive];
    } else if (itemButton.currentState == LiveRoomItemButtonStateGift) {
        [self.view addSubview:self.giftView];
        [self.giftView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.view);
        }];
    }
}

#pragma mark - Reconnect

- (void)reconnectLiveRoom {
    __weak __typeof(self) wself = self;
    [LiveRTMManager reconnect:self.liveRoomModel.roomID
                               block:^(LiveReconnectModel *reconnectModel, RTMACKModel *model) {
        if (model.result) {
            [wself joinRoom];
        } else if (model.code == RTMStatusCodeUserIsInactive ||
                   model.code == RTMStatusCodeRoomDisbanded ||
                   model.code == RTMStatusCodeUserNotFound) {
            [[ToastComponent shareToastComponent] showWithMessage:@"网络链接已断开，请检查设置" delay:0.8];
            [wself hangUp];
        } else {
            
        }
    }];
}

#pragma mark - Network request

- (void)loadDataWithJoinLiveRoom {
    __weak __typeof(self) wself = self;
    [[ToastComponent shareToastComponent] showLoading];
    [LiveRTMManager liveJoinLiveRoom:self.liveRoomModel.roomID
                               block:^(LiveRoomInfoModel *roomModel,
                                       LiveUserModel *userModel,
                                       RTMACKModel *model) {
        [[ToastComponent shareToastComponent] dismiss];
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
                [wself receivedCoHostJoin:userList
                        otherAnchorRoomId:rtcRoomID
                         otherAnchorToken:rtcToken];
            }
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAnchorLinkmicFinish {
    [LiveRTMManager liveAnchorLinkmicFinish:self.liveRoomModel.roomID
                                          linkerID:self.linkerID
                                             block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
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
                [wself receivedAddGuestsJoin:userList];
            }
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAudienceLinkmicFinish {
    [LiveRTMManager liveAudienceLinkmicFinish:self.liveRoomModel.roomID
                                               block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
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
                // 主播邀请，观众同意加入连麦
                [wself.addGuestsComponent joinRTCRoomByToken:rtcToken
                                                    rtcRoomID:rtcRoomID
                                                       userID:[LocalUserComponent userModel].uid];
                [wself receivedAddGuestsJoin:userList];
                // Guests update bottom ui
                [wself.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
            }
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
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
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithupdateRes:(LiveUserModel *)loginUserModel {
    BOOL isHost = (loginUserModel.role == 2);
    CGSize videoSize = isHost ? [LiveSettingVideoConfig defultVideoConfig].videoSize : [LiveSettingVideoConfig defultVideoConfig].guestVideoSize;
    [LiveRTMManager liveUpdateResWithSize:videoSize
                                   roomID:self.liveRoomModel.roomID
                                    block:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            // 主播更新合流分辨率和 RTC 编码分辨率，观众更新 RTC 编码分辨率
            if (isHost) {
                [[LiveRTCManager shareRtc] updateLiveTranscodingRes:videoSize];
                [[LiveRTCManager shareRtc] updateVideoEncoderRes:videoSize];
            } else {
                [[LiveRTCManager shareRtc] updateVideoEncoderRes:videoSize];
            }
        }
    }];
}

#pragma mark - SocketControl

- (void)addUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount {
    LiveIMModel *imModel = [[LiveIMModel alloc] init];
    imModel.isJoin = YES;
    imModel.userModel = userModel;
    [self.imComponent addIM:imModel];
    
    [self.addGuestsComponent updateList];
    [self.peopleNumView updateTitleLabel:audienceCount];
}

- (void)removeUser:(LiveUserModel *)userModel audienceCount:(NSInteger)audienceCount {
    LiveIMModel *imModel = [[LiveIMModel alloc] init];
    imModel.isJoin = NO;
    imModel.userModel = userModel;
    [self.imComponent addIM:imModel];
    
    [self.addGuestsComponent updateList];
    [self.peopleNumView updateTitleLabel:audienceCount];
}

- (void)receivedIMMessage:(NSString *)message sendUserModel:(LiveUserModel *)sendUserModel {
    if (![sendUserModel.uid isEqualToString:[LocalUserComponent userModel].uid]) {
        BOOL isFlower = [message containsString:@"鲜花"];
        BOOL isRocket = [message containsString:@"火箭"];
        NSString *imageName = @"";
        if (isFlower) {
            imageName = @"flower";
        }
        if (isRocket) {
            imageName = @"rocket";
        }
        // Local display
        LiveIMModel *imModel = [[LiveIMModel alloc] init];
        imModel.imageName = imageName;
        imModel.isJoin = NO;
        imModel.message = message;
        [self.imComponent addIM:imModel];
    }
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
    [[ToastComponent shareToastComponent] showWithMessage:message];
    [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusNone];
}

- (void)receivedCoHostSucceedWithUser:(LiveUserModel *)invitee
                             linkerID:(NSString *)linkerID {
    self.linkerID = linkerID;
}

- (void)receivedCoHostJoin:(NSArray<LiveUserModel *> *)userlList
         otherAnchorRoomId:(NSString *)otherRoomId
          otherAnchorToken:(NSString *)otherToken {
    __weak __typeof(self) wself = self;
    [self.coHostComponent showCoHost:self.liveView
                        streamPushUrl:self.streamPushUrl
                        userModelList:userlList
                       loginUserModel:self.currentUserModel
                    otherAnchorRoomId:otherRoomId
                    otherAnchorToken:otherToken
                       completeBlock:^{
        [wself.livePushStreamComponent close];
    }];
    [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK
                            touchStatus:LiveRoomItemTouchStatusClose];
}

- (void)receivedCoHostEnd {
    if ([self isHost]) {
        NSString *message = @"主播已断开连线";
        [[ToastComponent shareToastComponent] showWithMessage:message];
        [self.coHostComponent closeCoHost];
        [self.livePushStreamComponent openWithUserModel:self.currentUserModel];
        [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusNone];
    }
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
    if (![uid isEqualToString:[LocalUserComponent userModel].uid]) {
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
    if (self.addGuestsComponent.isConnect) {
        [self.addGuestsComponent updateGuestsCamera:cameraBool uid:uid];
        [self.addGuestsComponent updateGuestsMic:micBool uid:uid];
        if (camera != -1 && !cameraBool) {
            [[ToastComponent shareToastComponent] showWithMessage:@"你的摄像头已被主播关闭"];
        }
        if (mic != -1 && !micBool) {
            [[ToastComponent shareToastComponent] showWithMessage:@"你已被主播禁麦"];
        }
    }
    self.currentUserModel.camera = cameraBool;
    self.currentUserModel.mic = micBool;
    [self.settingComponent refreshGuestSettingView];
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
    if (![self isHost]) {
        // 观众称为嘉宾更新底部UI
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
        // 观众申请，主播同意加入连麦
        [self.addGuestsComponent joinRTCRoomByToken:rtcToken
                                          rtcRoomID:rtcRoomID
                                             userID:[LocalUserComponent userModel].uid];
        [[ToastComponent shareToastComponent] showWithMessage:@"主播接受了您的连麦申请，即将开始连麦。"];
    }
}

- (void)receivedAddGuestsRefuseWithUser:(LiveUserModel *)invitee {
    NSString *message = @"";
    if ([self isHost]) {
        message = [NSString stringWithFormat:@"观众%@拒绝连线", invitee.name];
        [self.addGuestsComponent updateList];
    } else {
        message = @"主播拒绝和你连线";
        self.currentUserModel.status = LiveInteractStatusOther;
        [self.addGuestsComponent closeApply];
    }
    [[ToastComponent shareToastComponent] showWithMessage:message];
}

- (void)receivedAddGuestsJoin:(NSArray<LiveUserModel *> *)userList {
    if ([self isHost]) {
        [self.livePushStreamComponent close];
        [self.addGuestsComponent showAddGuests:self.liveView
                                  streamPushUrl:_streamPushUrl
                                        hostUid:self.liveRoomModel.anchorUserID
                                       userList:userList];
        [self.addGuestsComponent updateGuests:userList];
        [self.addGuestsComponent updateList];
        
        if (userList.count == 2) {
            [[ToastComponent shareToastComponent] showWithMessage:@"点击观众画面可进行麦位管理"];
        }
    } else {
        [self.livePullStreamComponent close];
        [self.addGuestsComponent showAddGuests:self.liveView
                                  streamPushUrl:_streamPushUrl
                                        hostUid:self.liveRoomModel.anchorUserID
                                       userList:userList];
        [self.addGuestsComponent updateGuests:userList];
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
        self.currentUserModel.status = LiveInteractStatusAudienceLink;
        [self.addGuestsComponent closeApply];
    }
}

- (void)receivedAddGuestsRemoveWithUser:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList {
    if ([self isHost]) {
        NSString *userName = [self.addGuestsComponent removeAddGuestsUid:uid userList:userList];
        if (userName != nil) {
            NSString *message = [NSString stringWithFormat:@"%@断开了与您的连麦", userName];
            [[ToastComponent shareToastComponent] showWithMessage:message];
        }
    } else {
        if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
            [[ToastComponent shareToastComponent] showWithMessage:@"主播已和你断开连线"];
            [self.addGuestsComponent closeAddGuests];
            [self.addGuestsComponent closeApply];
            [self.addGuestsComponent leaveRTCRoom];
            [self.livePullStreamComponent open:self.liveRoomModel];
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
            self.currentUserModel.status = LiveInteractStatusOther;
        } else {
            NSString *userName = [self.addGuestsComponent removeAddGuestsUid:uid userList:userList];
            if (userName != nil) {
                NSString *message = [NSString stringWithFormat:@"%@断开了与您的连麦", userName];
                [[ToastComponent shareToastComponent] showWithMessage:message];
            }
        }
    }
}

- (void)receivedAddGuestsEnd {
    if ([self isHost]) {
        [self.addGuestsComponent closeAddGuests];
        [self.addGuestsComponent updateList];
        [self.livePushStreamComponent openWithUserModel:self.currentUserModel];
    } else {
        [[ToastComponent shareToastComponent] showWithMessage:@"主播已和你断开连线"];
        [self.addGuestsComponent closeAddGuests];
        [self.addGuestsComponent closeApply];
        [self.addGuestsComponent leaveRTCRoom];
        [self.livePullStreamComponent open:self.liveRoomModel];
        [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
        self.currentUserModel.status = LiveInteractStatusOther;
    }
}

- (void)receivedLiveEnd:(BOOL)isKick type:(NSString *)type {
    if (!isKick) {
        if ([type integerValue] == 2) {
            [[ToastComponent shareToastComponent] showWithMessage:@"本次体验时间已超过20分钟" delay:0.8];
        } else if ([type integerValue] == 3) {
            [[ToastComponent shareToastComponent] showWithMessage:@"直播间内容违规，直播间已被关闭" delay:0.8];
        } else {
            if (![self isHost]) {
                [[ToastComponent shareToastComponent] showWithMessage:@"主播已关闭直播" delay:0.8];
            }
        }
    } else {
        [[ToastComponent shareToastComponent] showWithMessage:@"相同ID用户已登录，您已被强制下线！" delay:0.8];
    }
    [self navigationControllerPop:isKick];
}

- (void)receivedRoomStatus:(LiveInteractStatus)status {
    self.liveRoomModel.hostUserModel.status = status;
    if (![self isHost]) {
        // 若接入播放器不支持 SEI
        // If the connected player does not support SEI
        if (![[LiveRTCManager shareRtc] isSupportSEI]) {
            // 视频流和业务消息之间会有时间查，推荐使用视频SEI方案。
            // 若接入的播放器不支持解析SEI，也可以通过业务状态更新渲染布局
            // There will be time to check between the video stream and the service message. It is recommended to use the video SEI solution.
            // If the connected player does not support parsing SEI, the rendering layout can also be updated through the business status
            SEL selector = @selector(delayUpdatePullStatus:);
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:selector object:nil];
            [self performSelector:selector withObject:@(status) afterDelay:3];
        }
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
    [[ToastComponent shareToastComponent] showWithMessage:message];
}

#pragma mark - Private Action

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.settingComponent close];
    [self.beautyComponent close];
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
    
    [self imComponent];
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
    if (userModel.role == LiveUserRoleHost) {
        // Host
        if (userModel.status == LiveInteractStatusHostLink) {
            // Co Hsot
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.bottomView updateButtonStatus:LiveRoomItemButtonStatePK touchStatus:LiveRoomItemTouchStatusClose];
        } else if (userModel.status == LiveInteractStatusAudienceLink) {
            // Add Guests
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.addGuestsComponent showAddGuests:self.liveView
                                      streamPushUrl:_streamPushUrl
                                            hostUid:self.liveRoomModel.anchorUserID
                                           userList:rtcUserList];
        } else {
            // None
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusHost];
            [self.livePushStreamComponent openWithUserModel:userModel];
            [self.livePushStreamComponent updateHostMic:userModel.mic
                                                  camera:userModel.camera];
            if (self.addGuestsComponent.isConnect) {
                [self.addGuestsComponent closeAddGuests];
            }
            if (self.coHostComponent.isConnect) {
                [self.coHostComponent closeCoHost];
            }
        }
    } else {
        // Audience
        if (userModel.status == LiveInteractStatusAudienceLink) {
            // Add Guests
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusGuests];
            [self.addGuestsComponent showAddGuests:self.liveView
                                      streamPushUrl:_streamPushUrl
                                            hostUid:self.liveRoomModel.anchorUserID
                                           userList:rtcUserList];
        } else {
            // None
            [self.bottomView updateButtonRoleStatus:BottomRoleStatusAudience];
            [self.livePullStreamComponent open:self.liveRoomModel];
            if (self.addGuestsComponent.isConnect) {
                [self.addGuestsComponent closeAddGuests];
                [self.addGuestsComponent leaveRTCRoom];
            }
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
            [self.beautyComponent resumeLocalEffect];
        });
    }
}

- (void)clickBottomAddGuests:(LiveRoomBottomView *)bottomView
                  itemButton:(LiveRoomItemButton *_Nullable)itemButton
                  roleStatus:(BottomRoleStatus)roleStatus {
    __weak __typeof(self) wself = self;
    if (roleStatus == BottomRoleStatusAudience) {
        if (self.liveRoomModel.hostUserModel.status == LiveInteractStatusHostLink) {
            [[ToastComponent shareToastComponent] showWithMessage:@"主播正在发起双主播连线"];
        } else {
            [self.addGuestsComponent showApply:self.currentUserModel
                                         hostID:self.liveRoomModel.anchorUserID];
        }
    } else if (roleStatus == BottomRoleStatusHost) {
        if ([bottomView getButtonTouchStatus:LiveRoomItemButtonStatePK]) {
            [[ToastComponent shareToastComponent] showWithMessage:@"正在等待被邀主播的应答"];
        } else {
            [self.addGuestsComponent showList:^(LiveAddGuestsDismissState state) {
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
        [self.coHostComponent showInviteList:nil];
    }
}

- (void)clickBottomSettingWithRoleStatus:(BottomRoleStatus)roleStatus {
    if (roleStatus == BottomRoleStatusHost) {
        if (self.addGuestsComponent.isConnect || self.coHostComponent.isConnect) {
            [self.settingComponent showWithType:LiveRoomSettingTypeHostChat
                                   fromSuperView:self.view
                                          roomID:self.liveRoomModel
                                       userModel:self.currentUserModel];
        } else {
            [self.settingComponent showWithType:LiveRoomSettingTypeHostLiving
                                   fromSuperView:self.view
                                          roomID:self.liveRoomModel
                                       userModel:self.currentUserModel];
        }
    } else if (roleStatus == BottomRoleStatusGuests) {
        [self.settingComponent showWithType:LiveRoomSettingTypeGuest
                               fromSuperView:self.view
                                      roomID:self.liveRoomModel
                                   userModel:self.currentUserModel];
    } else if (roleStatus == BottomRoleStatusAudience) {
        [self.settingComponent showWithType:LiveRoomSettingTypeAudience
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
        
        NSString *message = [NSString stringWithFormat:@"正在与%ld位观众连线，是否确认关闭观众连线？", (long)self.addGuestsComponent.guestList.count];
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

- (void)joinRoom {
    if ([self isHost]) {
        [self restoreRoomWithRoomInfoModel:self.liveRoomModel
                                 userModel:self.liveRoomModel.hostUserModel
                               rtcUserList:@[]];
    } else {
        [self loadDataWithJoinLiveRoom];
        [self.livePullStreamComponent open:self.liveRoomModel];
    }
}

- (void)restoreRoomWithRoomInfoModel:(LiveRoomInfoModel *)roomModel
                           userModel:(LiveUserModel *)userModel
                         rtcUserList:(NSArray<LiveUserModel *> *)rtcUserList {
    self.liveRoomModel = roomModel;
    self.currentUserModel = userModel;
    
    // All join RTS room
    [[LiveRTCManager shareRtc] joinMultiRTSRoomByToken:roomModel.rtmToken
                                                roomID:roomModel.roomID
                                                userID:[LocalUserComponent userModel].uid];
    
    // Host join RTC room
    if ([self isHost]) {
        [[LiveRTCManager shareRtc] joinRTCRoomByToken:roomModel.rtcToken
                                             rtcRoomID:roomModel.rtcRoomId
                                                userID:[LocalUserComponent userModel].uid];
    }
    // Join RTS/RTC room callback
    __weak __typeof(self) wself = self;
    [LiveRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString * _Nonnull roomId, NSInteger errorCode, NSInteger joinType) {
        if (errorCode == 0) {
            [wself joinRTCRoomResults:joinType];
        }
    };
    
    // UI
    [self addSubviewAndConstraints];
    self.hostAvatarView.hostName = roomModel.anchorUserName;
    [self.peopleNumView updateTitleLabel:roomModel.audienceCount];
    [self updateLayoutToRole:userModel rtcUserList:rtcUserList];
    [self updatePullStatus:roomModel.hostUserModel.status];
    [self changeMediaWithUser:roomModel.hostUserModel.uid
                       camera:roomModel.hostUserModel.camera
                          mic:roomModel.hostUserModel.mic];
    
    // Update Res
    [self loadDataWithupdateRes:self.currentUserModel];
}

- (void)joinRTCRoomResults:(NSInteger)joinType {
    if (joinType == 0) {
        // Entering the room for the first time
        if ([self isHost]) {
            // turn on the merge and retweet
            [[LiveRTCManager shareRtc]
             startMixStreamRetweetWithPushUrl:self.streamPushUrl
             hostUser:self.liveRoomModel.hostUserModel
             rtcRoomId:self.liveRoomModel.rtcRoomId];
        }
    } else {
        // Entering the room after reconnection
        [self reconnectLiveRoom];
    }
}

- (BOOL)isHost {
    return [self.liveRoomModel.anchorUserID isEqualToString:[LocalUserComponent userModel].uid];
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
        [self.livePullStreamComponent updateWithStatus:pullStatus];
    }
}

- (void)changeMediaWithUser:(NSString *)uid
                     camera:(BOOL)camera
                        mic:(BOOL)mic {
    if ([uid isEqualToString:self.liveRoomModel.anchorUserID]) {
        self.liveRoomModel.hostUserModel.camera = camera;
        self.liveRoomModel.hostUserModel.mic = mic;
        if (self.livePushStreamComponent.isConnect) {
            [self.livePushStreamComponent updateHostMic:mic camera:camera];
        }
        if (self.livePullStreamComponent.isConnect) {
            [self.livePullStreamComponent updateHostMic:mic camera:camera];
        }
    }
    if (self.addGuestsComponent.isConnect) {
        [self.addGuestsComponent updateGuestsCamera:camera uid:uid];
        [self.addGuestsComponent updateGuestsMic:mic uid:uid];
        if ([self isHost]) {
            [self.addGuestsComponent closeSheet:uid];
        }
    }
    if (self.coHostComponent.isConnect) {
        [self.coHostComponent updateGuestsMic:mic uid:uid];
        [self.coHostComponent updateGuestsCamera:camera uid:uid];
    }
    if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
        self.currentUserModel.mic = mic;
        self.currentUserModel.camera = camera;
        [self.settingComponent refreshGuestSettingView];
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

- (LiveIMComponent *)imComponent {
    if (!_imComponent) {
        _imComponent = [[LiveIMComponent alloc] initWithSuperView:self.view];
    }
    return _imComponent;
}

- (UIView *)liveView {
    if (!_liveView) {
        _liveView = [[UIView alloc] init];
    }
    return _liveView;
}

- (LiveCoHostComponent *)coHostComponent {
    if (!_coHostComponent) {
        _coHostComponent = [[LiveCoHostComponent alloc] initWithRoomID:self.liveRoomModel];
    }
    return _coHostComponent;
}

- (LiveAddGuestsComponent *)addGuestsComponent {
    if (!_addGuestsComponent) {
        _addGuestsComponent = [[LiveAddGuestsComponent alloc]
                                initWithRoomID:self.liveRoomModel];
    }
    return _addGuestsComponent;
}

- (LivePushStreamComponent *)livePushStreamComponent {
    if (!_livePushStreamComponent) {
        _livePushStreamComponent = [[LivePushStreamComponent alloc]
                                     initWithSuperView:self.liveView
                                     roomModel:self.liveRoomModel
                                     streamPushUrl:self.streamPushUrl];
    }
    return _livePushStreamComponent;
}

- (LivePullStreamComponent *)livePullStreamComponent {
    if (!_livePullStreamComponent) {
        _livePullStreamComponent = [[LivePullStreamComponent alloc] initWithSuperView:self.liveView];
    }
    return _livePullStreamComponent;
}

- (LiveRoomSettingComponent *)settingComponent {
    if (!_settingComponent) {
        _settingComponent = [[LiveRoomSettingComponent alloc] init];
    }
    return _settingComponent;
}

- (BytedEffectProtocol *)beautyComponent {
    if (!_beautyComponent) {
        _beautyComponent = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[LiveRTCManager shareRtc].rtcEngineKit];
    }
    return _beautyComponent;
}

#pragma mark - Gift

- (void)giftViewMaskAction {
    [self.giftView removeFromSuperview];
    self.giftView = nil;
}

- (void)senderFlowerGift {
    [self senderGift:@"flower"];
}

- (void)senderRocketGift {
    [self senderGift:@"rocket"];
}

- (void)senderGift:(NSString *)imageName {
    NSString *title = [imageName isEqualToString:@"flower"] ? @"鲜花" : @"火箭";
    NSString *message = [NSString stringWithFormat:@"%@送出%@", [LocalUserComponent userModel].name, title];
    
    // Local display
    LiveIMModel *imModel = [[LiveIMModel alloc] init];
    imModel.imageName = imageName;
    imModel.isJoin = NO;
    imModel.message = message;
    [self.imComponent addIM:imModel];
    
    // Send to the room
    [LiveRTMManager sendIMMessage:message block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
    }];
    
    // Close the panel
    [self giftViewMaskAction];
}

- (UIView *)giftView {
    if (!_giftView) {
        _giftView = [[UIView alloc] init];
        _giftView.backgroundColor = [UIColor clearColor];
        
        _giftView.userInteractionEnabled = YES;
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(giftViewMaskAction)];
        [_giftView addGestureRecognizer:tap];
        
        UIButton *contentView = [[UIButton alloc] init];
        contentView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
        [_giftView addSubview:contentView];
        [contentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.width.bottom.equalTo(_giftView);
            make.height.mas_equalTo(148 + [DeviceInforTool getVirtualHomeHeight]);
        }];
        
        UILabel *titleLabel = [[UILabel alloc] init];
        titleLabel.text = @"礼物";
        titleLabel.font = [UIFont systemFontOfSize:16];
        titleLabel.textColor = [UIColor whiteColor];
        [contentView addSubview:titleLabel];
        [titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(contentView);
            make.top.mas_equalTo(16);
        }];
        
        UIButton *flowerGiftButton = [[UIButton alloc] init];
        [flowerGiftButton addTarget:self action:@selector(senderFlowerGift) forControlEvents:UIControlEventTouchUpInside];
        flowerGiftButton.backgroundColor = [UIColor clearColor];
        [contentView addSubview:flowerGiftButton];
        [flowerGiftButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(40, 69));
            make.top.mas_equalTo(55);
            make.right.equalTo(titleLabel.mas_left).offset(-25);
        }];
        
        UIButton *rocketGiftButton = [[UIButton alloc] init];
        [rocketGiftButton addTarget:self action:@selector(senderRocketGift) forControlEvents:UIControlEventTouchUpInside];
        rocketGiftButton.backgroundColor = [UIColor clearColor];
        [contentView addSubview:rocketGiftButton];
        [rocketGiftButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(40, 69));
            make.top.mas_equalTo(55);
            make.left.equalTo(titleLabel.mas_right).offset(25);
        }];
        
        [self addImageAndTitleL:flowerGiftButton imageName:@"flower" message:@"鲜花"];
        [self addImageAndTitleL:rocketGiftButton imageName:@"rocket" message:@"火箭"];
    }
    return _giftView;
}

- (void)addImageAndTitleL:(UIButton *)button
                imageName:(NSString *)imageName
                  message:(NSString *)message {
    UIImageView *imageView = [[UIImageView alloc] init];
    imageView.image = [UIImage imageNamed:imageName bundleName:HomeBundleName];
    [button addSubview:imageView];
    [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.equalTo(button.mas_width);
        make.centerX.top.equalTo(button);
    }];
    
    UILabel *messageLabel = [[UILabel alloc] init];
    messageLabel.text = message;
    messageLabel.font = [UIFont systemFontOfSize:12];
    messageLabel.textColor = [UIColor whiteColor];
    [button addSubview:messageLabel];
    [messageLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.centerX.equalTo(button);
    }];
}

- (void)dealloc {
    [UIApplication sharedApplication].idleTimerDisabled = NO;
    [[LiveRTCManager shareRtc] leaveLiveRoom];
    [self.livePullStreamComponent close];
}

@end
