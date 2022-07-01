//
//  LiveAddGuestsCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveAddGuestsCompoments.h"
#import "LiveAddGuestsApplyView.h"
#import "LiveAddGuestsRoomView.h"
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"
#import "LiveSheetCompoments.h"
#import "NetworkingTool.h"

NSTimeInterval const LiveApplyOvertimeInterval = 4.0;

@interface LiveAddGuestsCompoments () <LiveAddGuestsListsViewDelegate>

@property (nonatomic, weak) LiveAddGuestsListsView *listsView;
@property (nonatomic, weak) LiveAddGuestsApplyView *applyView;
@property (nonatomic, strong) UIButton *maskButton;
@property (nonatomic, copy) void (^dismissBlock)(LiveAddGuestsDismissState state);
@property (nonatomic, copy) NSString *roomID;
@property (nonatomic, weak) LiveAddGuestsRoomView *liveAddGuestsRoomView;
@property (nonatomic, copy) NSString *hostUid;
@property (nonatomic, copy) NSArray<LiveUserModel *> *userList;
@property (nonatomic, weak) BaseButton *closeConnectButton;
@property (nonatomic, strong) LiveUserModel *sheetUserModel;
@property (nonatomic, assign) CFAbsoluteTime applyTime;

@end

@implementation LiveAddGuestsCompoments

- (instancetype)initWithRoomID:(NSString *)roomID {
    self = [super init];
    if (self) {
        _roomID = roomID;
    }
    return self;
}

#pragma mark - Publish List Action

- (void)showList:(void (^)(LiveAddGuestsDismissState state))dismissBlock {
    self.dismissBlock = dismissBlock;
    UIViewController *rootVC = [DeviceInforTool topViewController];
    
    [rootVC.view addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.left.height.equalTo(rootVC.view);
        make.top.equalTo(rootVC.view).offset(SCREEN_HEIGHT);
    }];
    
    LiveAddGuestsListsView *listsView = [[LiveAddGuestsListsView alloc] init];
    listsView.delegate = self;
    listsView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self.maskButton addSubview:listsView];
    [listsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(0);
        make.right.mas_equalTo(0);
        make.height.mas_offset(204 + [DeviceInforTool getVirtualHomeHeight]);
        make.bottom.mas_offset(0);
    }];
    _listsView = listsView;
    
    UILabel *label = [[UILabel alloc] init];
    label.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    label.text = @"观众连线";
    label.textColor = [UIColor whiteColor];
    label.font = [UIFont systemFontOfSize:16];
    label.textAlignment = NSTextAlignmentCenter;
    [self.maskButton addSubview:label];
    [label mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(rootVC.view);
        make.bottom.equalTo(listsView.mas_top);
        make.height.mas_equalTo(44);
    }];
    
    BaseButton *closeConnectButton = [[BaseButton alloc] init];
    [closeConnectButton addTarget:self action:@selector(closeConnectAction) forControlEvents:UIControlEventTouchUpInside];
    [closeConnectButton setBackgroundColor:[UIColor clearColor]];
    [closeConnectButton setTitle:@"关闭连线" forState:UIControlStateNormal];
    closeConnectButton.titleLabel.font = [UIFont systemFontOfSize:12];
    [self.maskButton addSubview:closeConnectButton];
    [closeConnectButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-16);
        make.size.mas_equalTo(CGSizeMake(68, 22));
        make.centerY.equalTo(label);
    }];
    closeConnectButton.hidden = _isConnect ? NO : YES;
    _closeConnectButton = closeConnectButton;
    
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
    
    [self loadDataWithGetAudienceList];
}

- (void)updateList {
    if (_listsView) {
        [self loadDataWithGetAudienceList];
        _closeConnectButton.hidden = _isConnect ? NO : YES;
    }
}

#pragma mark - Publish Live Room Action

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

- (void)showAddGuests:(UIView *)superView
        streamPushUrl:(NSString *)streamPushUrl
              hostUid:(NSString *)hostUid
             userList:(NSArray<LiveUserModel *> *)userList {
    _isConnect = YES;
    _hostUid = hostUid;
    _userList = userList;
    
    // Start Capture
    [[LiveRTCManager shareRtc] startCapture];
    [[LiveRTCManager shareRtc] muteAllRemoteAudio:NO];
    
    if ([hostUid isEqualToString:[LocalUserComponents userModel].uid]) {
        // Open Trans Coding
        BOOL isMixServer = ![LiveSettingVideoConfig defultVideoConfig].allowMixOnClientAndCloud;
        if (!isMixServer) {
            // 客户端合流 恢复推流
            [[LiveRTCManager shareRtc] startPush:nil];
        }
        [[LiveRTCManager shareRtc] openTranscodingByUserList:userList
                                                     pushUrl:streamPushUrl
                                                 isMixServer:isMixServer
                                                    isCoHost:NO];
    } else {
        // Update the guest's own resolution
        [self loadDataWithupdateRes:YES];
    }
    
    // Update UI
    if (!_liveAddGuestsRoomView) {
        LiveAddGuestsRoomView *liveAddGuestsRoomView = [[LiveAddGuestsRoomView alloc] initWithHostID:hostUid];
        [superView addSubview:liveAddGuestsRoomView];
        [liveAddGuestsRoomView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(superView);
        }];
        _liveAddGuestsRoomView = liveAddGuestsRoomView;
    }
    [_liveAddGuestsRoomView updateGuests:userList];
    
    // Sheet
    __weak __typeof(self) wself = self;
    _liveAddGuestsRoomView.clickGuestsBlock = ^(LiveUserModel *_Nonnull userModel) {
        if ([hostUid isEqualToString:[LocalUserComponents userModel].uid]) {
            // Is host
            [wself showSheetView:userModel];
        }
    };
    
    // enable network monitoring
    [[LiveRTCManager shareRtc] didChangeNetworkQuality:^(LiveNetworkQualityStatus status, NSString *_Nonnull uid) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
            [wself.liveAddGuestsRoomView updateNetworkQuality:status uid:uid];
        }));
    }];
}

- (NSString *)removeAddGuestsUid:(NSString *)uid userList:(NSArray<LiveUserModel *> *)userList {
    LiveUserModel *deleteUserModel = nil;
    if (_liveAddGuestsRoomView) {
        NSArray *list = [_userList copy];
        for (LiveUserModel *userModel in list) {
            if ([userModel.uid isEqualToString:uid]) {
                deleteUserModel = userModel;
                break;
            }
        }
        _userList = [userList copy];
        [_liveAddGuestsRoomView updateGuests:_userList];
    }
    return deleteUserModel.name;
}

- (void)closeAddGuests {
    _isConnect = NO;
    
    if ([_hostUid isEqualToString:[LocalUserComponents userModel].uid]) {
        // anchor
        [[LiveRTCManager shareRtc] closeTranscoding];
    } else {
        // audience
        [[LiveRTCManager shareRtc] stopCapture];
        // Update the guest's own resolution
        [self loadDataWithupdateRes:NO];
    }
    
    if (_liveAddGuestsRoomView) {
        [_liveAddGuestsRoomView removeAllSubviews];
        [_liveAddGuestsRoomView removeFromSuperview];
        _liveAddGuestsRoomView = nil;
    }
}

- (void)updateGuests:(NSArray<LiveUserModel *> *)userList {
    _userList = userList;
    if (_liveAddGuestsRoomView) {
        [_liveAddGuestsRoomView updateGuests:userList];
    }
}

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid {
    [self.liveAddGuestsRoomView updateGuestsMic:mic uid:uid];
}

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid {
    [self.liveAddGuestsRoomView updateGuestsCamera:camera uid:uid];
}

- (void)closeSheet:(NSString *)uid {
    if ([uid isEqualToString:_sheetUserModel.uid]) {
        [[LiveSheetCompoments shareSheet] dismissUserListView];
        _sheetUserModel = nil;
    }
}

#pragma mark - Publish Audience apply

- (void)showApply:(LiveUserModel *)loginUserModel hostID:(NSString *)hostID {
    UIViewController *rootVC = [DeviceInforTool topViewController];
    
    [rootVC.view addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.left.height.equalTo(rootVC.view);
        make.top.equalTo(rootVC.view).offset(SCREEN_HEIGHT);
    }];
    
    if (loginUserModel.status == LiveInteractStatusApplying
        && CFAbsoluteTimeGetCurrent() - self.applyTime > LiveApplyOvertimeInterval) {
        loginUserModel.status = LiveInteractStatusOther;
    }
    LiveAddGuestsApplyView *applyView = [[LiveAddGuestsApplyView alloc] init];
    applyView.userModel = loginUserModel;
    applyView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    [self.maskButton addSubview:applyView];
    [applyView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(0);
        make.right.mas_equalTo(0);
        make.height.mas_offset(149 + [DeviceInforTool getVirtualHomeHeight]);
        make.bottom.mas_offset(0);
    }];
    _applyView = applyView;
    
    __weak __typeof(self) wself = self;
    applyView.clickApplyBlock = ^{
        [wself clickApplyAction:hostID];
    };
    
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
}

- (void)closeApply {
    if (_applyView) {
        [_applyView removeAllSubviews];
        [_applyView removeFromSuperview];
        _applyView = nil;
    }
}

- (NSArray *)guestList {
    return self.liveAddGuestsRoomView.guestList;
}

#pragma mark - Private Action

- (void)clickApplyAction:(NSString *)hostID {
    __weak __typeof(self) wself = self;
    // 观众申请上麦
    [LiveRTMManager liveAudienceLinkmicApply:self.roomID
                                              block:^(NSString *linkerID,
                                                      RTMACKModel *model) {
        if (model.result || model.code == RTMStatusCodeUserIsInviting) {
            // Initiate an invitation
            [[ToastComponents shareToastComponents] showWithMessage:[NSString stringWithFormat:@"您已向主播发起连麦申请，等待主播应答。"]];
            [wself.applyView updateApplying];
            wself.applyTime = CFAbsoluteTimeGetCurrent();
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)showSheetView:(LiveUserModel *)userModel {
    _sheetUserModel = userModel;
    NSMutableArray *list = [[NSMutableArray alloc] init];
    LiveSheetModel *sheet1Model = [[LiveSheetModel alloc] init];
    sheet1Model.isDisable = NO;
    sheet1Model.titleStr = @"断开连线";
    __weak __typeof(self) wself = self;
    sheet1Model.clickBlock = ^(LiveSheetModel *_Nonnull action) {
        __strong __typeof(wself) strongSelf = wself;
        [strongSelf loadDataWithAudienceLinkmicKick:userModel];
    };
    
    LiveSheetModel *sheet2Model = [[LiveSheetModel alloc] init];
    sheet2Model.isDisable = !userModel.mic;
    sheet2Model.titleStr = @"关闭麦克风";
    sheet2Model.clickBlock = ^(LiveSheetModel *_Nonnull action) {
        __strong __typeof(wself) strongSelf = wself;
        [strongSelf loadDataWithUpdateMediaStatus:userModel mic:0 camera:-1];
    };
    
    LiveSheetModel *sheet3Model = [[LiveSheetModel alloc] init];
    sheet3Model.isDisable = !userModel.camera;
    sheet3Model.titleStr = @"关闭摄像头";
    sheet3Model.clickBlock = ^(LiveSheetModel *_Nonnull action) {
        __strong __typeof(wself) strongSelf = wself;
        [strongSelf loadDataWithUpdateMediaStatus:userModel mic:-1 camera:0];
    };
    
    LiveSheetModel *sheet4Model = [[LiveSheetModel alloc] init];
    sheet4Model.isDisable = NO;
    sheet4Model.titleStr = @"取消";
    
    [list addObject:sheet1Model];
    [list addObject:sheet2Model];
    [list addObject:sheet3Model];
    [list addObject:sheet4Model];
    [[LiveSheetCompoments shareSheet] show:[list copy]];
}

- (void)loadDataWithUpdateMediaStatus:(LiveUserModel *)userModel
                                  mic:(NSInteger)mic
                               camera:(NSInteger)camera {
    [LiveRTMManager liveManageGuestMedia:self.roomID
                                    guestRoomID:userModel.roomID
                                    guestUserID:userModel.uid
                                            mic:mic
                                         camera:camera
                                          block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)loadDataWithAudienceLinkmicKick:(LiveUserModel *)userModel {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAudienceLinkmicKick:self.roomID
                                    audienceRoomID:userModel.roomID
                                    audienceUserID:userModel.uid
                                             block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        } else {
            [wself.liveAddGuestsRoomView removeGuests:userModel.uid];
        }
    }];
}

#pragma mark - Load Data

- (void)loadDataWithGetAudienceList {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveGetAudienceList:self.roomID
                                         block:^(NSArray<LiveUserModel *> *userList,
                                                 RTMACKModel *_Nonnull model) {
        if (model.result) {
            wself.listsView.dataLists = userList;
        }
    }];
}

- (void)loadDataWithupdateRes:(BOOL)isOnMic {
    CGSize videoSize = isOnMic ? [LiveSettingVideoConfig defultVideoConfig].guestVideoSize : CGSizeZero;
    [LiveRTMManager liveUpdateResWithSize:videoSize
                                          roomID:self.roomID
                                           block:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            [[LiveRTCManager shareRtc] updateRes:videoSize];
        }
    }];
}

#pragma mark - LiveCoHostRaiseHandListsViewDelegate

- (void)liveAddGuestsListsView:(LiveAddGuestsListsView *)liveAddGuestsListsView
                   clickButton:(LiveUserModel *)model {
    // 主播邀请观众
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveAudienceLinkmicInvite:self.roomID
                                      audienceRoomID:model.roomID
                                      audienceUserID:model.uid
                                               extra:@""
                                               block:^(NSString * _Nullable linkerID,
                                                       RTMACKModel * _Nonnull model) {
        if (model.result || model.code == RTMStatusCodeUserIsInviting) {
            [wself dismissUserListView:LiveAddGuestsDismissStateInvite];
            [[ToastComponents shareToastComponents] showWithMessage:[NSString stringWithFormat:@"已发出邀请，等待对方应答"]];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

#pragma mark - Private Action

- (void)maskButtonAction {
    [self dismissUserListView:LiveAddGuestsDismissStateNone];
}

- (void)closeConnectAction {
    [self dismissUserListView:LiveAddGuestsDismissStateCloseConnect];
}

- (void)dismissUserListView:(LiveAddGuestsDismissState)state {
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
