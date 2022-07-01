//
//  LiveCoHostCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveCoHostCompoments.h"
#import "LiveCoHostRoomView.h"
#import "LiveCoHostTopSelectView.h"
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"
#import "NetworkingTool.h"

@interface LiveCoHostCompoments () <LiveCoHostTopSelectViewDelegate, LiveCoHostRaiseHandListsViewDelegate, LiveCoHostAudienceListsViewDelegate>

@property (nonatomic, weak) LiveCoHostRaiseHandListsView *raiseHandListsView;
@property (nonatomic, weak) LiveCoHostAudienceListsView *audienceListsView;
@property (nonatomic, strong) UIButton *maskButton;
@property (nonatomic, copy) void (^dismissBlock)(LiveCoHostDismissState state);
@property (nonatomic, weak) LiveCoHostRoomView *liveCoHostRoomView;
@property (nonatomic, copy) NSString *roomID;
@end

@implementation LiveCoHostCompoments

- (instancetype)initWithRoomID:(NSString *)roomID {
    self = [super init];
    if (self) {
        _roomID = roomID;
    }
    return self;
}

#pragma mark - Publish List Action

- (void)showInviteList:(void (^)(LiveCoHostDismissState state))dismissBlock {
    self.dismissBlock = dismissBlock;
    UIViewController *rootVC = [DeviceInforTool topViewController];
    
    [rootVC.view addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.left.height.equalTo(rootVC.view);
        make.top.equalTo(rootVC.view).offset(SCREEN_HEIGHT);
    }];
    
    LiveCoHostAudienceListsView *audienceListsView = [[LiveCoHostAudienceListsView alloc] init];
    audienceListsView.delegate = self;
    audienceListsView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self.maskButton addSubview:audienceListsView];
    [audienceListsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(0);
        make.right.mas_equalTo(0);
        make.height.mas_offset(204 + [DeviceInforTool getVirtualHomeHeight]);
        make.bottom.mas_offset(0);
    }];
    _audienceListsView = audienceListsView;
    
    LiveCoHostRaiseHandListsView *raiseHandListsView = [[LiveCoHostRaiseHandListsView alloc] init];
    raiseHandListsView.delegate = self;
    raiseHandListsView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self.maskButton addSubview:raiseHandListsView];
    [raiseHandListsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(audienceListsView);
    }];
    _raiseHandListsView = raiseHandListsView;
    
    LiveCoHostTopSelectView *topSelectView = [[LiveCoHostTopSelectView alloc] init];
    topSelectView.delegate = self;
    [self.maskButton addSubview:topSelectView];
    [topSelectView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(rootVC.view);
        make.bottom.equalTo(audienceListsView.mas_top);
        make.height.mas_equalTo(44);
    }];
    
    // Start animation
    [rootVC.view layoutIfNeeded];
    [self.maskButton.superview setNeedsUpdateConstraints];
    [UIView animateWithDuration:0.25
                     animations:^{
        [self.maskButton mas_updateConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(rootVC.view).offset(0);
        }];
        [self.maskButton.superview layoutIfNeeded];
    }];
    
    [self loadDataWithRaiseHandLists];
}

- (void)updateInviteList {
    if (self.raiseHandListsView.superview && !self.raiseHandListsView.hidden) {
        [self loadDataWithRaiseHandLists];
    } else if (self.audienceListsView.superview && !self.audienceListsView.hidden) {
        [self loadDataWithAudienceLists];
    } else {
    }
}

#pragma mark - Publish Room Action

- (void)readyJoinRTCRoomByToken:(NSString *)token
                         roomID:(NSString *)roomID
                         userID:(NSString *)userID {
    [[LiveRTCManager shareRtc] joinRTCRoomByToken:token
                                           roomID:roomID
                                           userID:userID];
}

- (void)leaveRTCRoom {
    [[LiveRTCManager shareRtc] leaveRTCRoom];
}

- (void)showCoHost:(UIView *)superView
     streamPushUrl:(NSString *)streamPushUrl
     userModelList:(NSArray<LiveUserModel *> *)userModelList
    loginUserModel:(LiveUserModel *)loginUserModel {
    _isConnect = YES;
    
    [[LiveRTCManager shareRtc] muteAllRemoteAudio:NO];
    // Start Capture
    [[LiveRTCManager shareRtc] startCapture];
    // Open Trans Coding
    [[LiveRTCManager shareRtc] startPush:nil];
    BOOL isMixServer = ![LiveSettingVideoConfig defultVideoConfig].allowMixOnClientAndCloud;
    [[LiveRTCManager shareRtc] openTranscodingByUserList:userModelList
                                                 pushUrl:streamPushUrl
                                             isMixServer:isMixServer
                                                isCoHost:YES];
    
    if (!_liveCoHostRoomView) {
        LiveCoHostRoomView *liveCoHostRoomView = [[LiveCoHostRoomView alloc] init];
        [superView addSubview:liveCoHostRoomView];
        CGFloat coHostWidth = SCREEN_WIDTH;
        CGFloat coHostHeight = ceilf((SCREEN_WIDTH / 2) * 16 / 9);
        [liveCoHostRoomView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(coHostWidth, coHostHeight));
            make.center.equalTo(superView);
        }];
        _liveCoHostRoomView = liveCoHostRoomView;
    }
    _liveCoHostRoomView.userModelList = userModelList;
    [_liveCoHostRoomView updateGuestsMic:loginUserModel.mic
                                     uid:loginUserModel.uid];
    [_liveCoHostRoomView updateGuestsCamera:loginUserModel.camera
                                        uid:loginUserModel.uid];
    
    // enable network monitoring
    __weak __typeof(self) wself = self;
    [[LiveRTCManager shareRtc] didChangeNetworkQuality:^(LiveNetworkQualityStatus status, NSString *_Nonnull uid) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
            [wself.liveCoHostRoomView updateNetworkQuality:status uid:uid];
        }));
    }];
}

- (void)closeCoHost {
    _isConnect = NO;
    
    [[LiveRTCManager shareRtc] closeTranscoding];
    
    if (_liveCoHostRoomView) {
        [_liveCoHostRoomView removeAllSubviews];
        [_liveCoHostRoomView removeFromSuperview];
        _liveCoHostRoomView = nil;
    }
}

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid {
    [self.liveCoHostRoomView updateGuestsMic:mic uid:uid];
}

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid {
    [self.liveCoHostRoomView updateGuestsCamera:camera uid:uid];
}

#pragma mark - Load Data

- (void)loadDataWithRaiseHandLists {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveGetActiveAnchorList:self.roomID
                                             block:^(NSArray<LiveUserModel *> * _Nullable userList, RTMACKModel * _Nonnull model) {
        if (model.result) {
            wself.raiseHandListsView.dataLists = userList;
        }
    }];
}

- (void)loadDataWithAudienceLists {
    self.audienceListsView.dataLists = @[];
}

#pragma mark - LiveCoHostTopSelectViewDelegate

- (void)liveCoHostTopSelectView:(LiveCoHostTopSelectView *)liveCoHostTopSelectView clickSwitchItem:(BOOL)isAudience {
    if (isAudience) {
        self.raiseHandListsView.hidden = YES;
        self.audienceListsView.hidden = NO;
        [self loadDataWithAudienceLists];
    } else {
        self.raiseHandListsView.hidden = NO;
        self.audienceListsView.hidden = YES;
        [self loadDataWithRaiseHandLists];
    }
}

#pragma mark - LiveCoHostRaiseHandListsViewDelegate

- (void)liveCoHostRaiseHandListsView:(LiveCoHostRaiseHandListsView *)liveCoHostRaiseHandListsView clickButton:(LiveUserModel *)userModel {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAnchorLinkmicInvite:self.roomID
                                     inviteeRoomID:userModel.roomID
                                     inviteeUserID:userModel.uid
                                             extra:@""
                                             block:^(NSString *linkerID,
                                                     RTMACKModel *model) {
        if (model.result || model.code == RTMStatusCodeUserIsInviting) {
            // Initiate an invitation
            NSString *name = userModel.name;
            [wself dismissUserListView:LiveCoHostDismissStateInviteIng];
            [[ToastComponents shareToastComponents] showWithMessage:[NSString stringWithFormat:@"已向%@发出邀请，等待对方应答", name]];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

#pragma mark - LiveCoHostAudienceListsViewDelegate

- (void)liveCoHostAudienceListsView:(LiveCoHostAudienceListsView *)liveCoHostAudienceListsView clickButton:(BaseUserModel *)model {
}

#pragma mark - Private Action

- (void)maskButtonAction {
    [self dismissUserListView:LiveCoHostDismissStateNone];
}

- (void)dismissUserListView:(LiveCoHostDismissState)state {
    [self.maskButton removeAllSubviews];
    [self.maskButton removeFromSuperview];
    self.maskButton = nil;
    
    if (self.dismissBlock) {
        self.dismissBlock(state);
    }
}

#pragma mark - Getter

- (UIButton *)maskButton {
    if (!_maskButton) {
        _maskButton = [[UIButton alloc] init];
        [_maskButton addTarget:self action:@selector(maskButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [_maskButton setBackgroundColor:[UIColor clearColor]];
    }
    return _maskButton;
}

- (void)dealloc {
    NSLog(@"dealloc %@", NSStringFromClass([self class]));
}

@end
