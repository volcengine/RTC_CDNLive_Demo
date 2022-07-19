//
//  CreateRoomViewController.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveCreateRoomViewController.h"
#import "LiveCreateRoomControlView.h"
#import "LiveCreateRoomTipView.h"
#import "LiveRTCManager.h"
#import "LiveRoomSettingCompoments.h"
#import "LiveRoomViewController.h"
#import "BytedEffectProtocol.h"

@interface LiveCreateRoomViewController () <LiveCreateRoomControlViewDelegate>
@property (nonatomic, strong) LiveCreateRoomTipView *tipView;
@property (nonatomic, strong) UIView *renderView;

@property (nonatomic, strong) UIButton *startButton;

@property (nonatomic, strong) LiveCreateRoomControlView *controlView;
@property (nonatomic, strong) LiveRoomSettingCompoments *settingCompoments;
@property (nonatomic, strong) BytedEffectProtocol *beautyCompoments;

@property (nonatomic, strong) LiveRoomInfoModel *roomInfoModel;
@property (nonatomic, copy) NSString *pushUrl;

@end

@implementation LiveCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];

    [self loadDataWithCreateLive];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)startButtonAction:(UIButton *)sender {
    sender.userInteractionEnabled = NO;
    __weak __typeof(self) wself = self;
    [PublicParameterCompoments share].roomId = self.roomInfoModel.roomID;
    [LiveRTMManager liveStartLive:self.roomInfoModel.roomID
                            block:^(LiveUserModel *hostUserModel,
                                           RTMACKModel * _Nonnull model) {
        if (model.result) {
            self.roomInfoModel.hostUserModel = hostUserModel;
            LiveRoomViewController *next = [[LiveRoomViewController alloc]
                                            initWithRoomModel:self.roomInfoModel
                                            streamPushUrl:wself.pushUrl];
            next.settingCompoments = wself.settingCompoments;
            next.beautyCompoments = wself.beautyCompoments;
            [wself.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
        sender.userInteractionEnabled = YES;
    }];
}

#pragma mark - Private Action

- (void)addSubviewAndConstraints {
    [self.view addSubview:self.renderView];
    [self.renderView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self.view);
    }];

    [self.view bringSubviewToFront:self.navView];
    self.navView.backgroundColor = [UIColor clearColor];

    [self.view addSubview:self.tipView];
    [self.tipView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.mas_equalTo(48);
      make.centerY.equalTo(self.leftButton);
      make.right.equalTo(self.view);
    }];

    [self.view addSubview:self.startButton];
    [self.startButton mas_makeConstraints:^(MASConstraintMaker *make) {
      make.size.mas_equalTo(CGSizeMake(170, 50));
      make.centerX.equalTo(self.view);
      make.bottom.equalTo(self.view).offset(-20 - [DeviceInforTool getVirtualHomeHeight]);
    }];
    [self.view addSubview:self.controlView];
    [self.controlView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.bottom.equalTo(self.startButton.mas_top).offset(-10);
      make.centerX.equalTo(self.view);
      make.width.mas_equalTo(200);
      make.height.mas_equalTo(85);
    }];
}

- (void)loadDataWithCreateLive {
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveCreateLive:[LocalUserComponents userModel].name
                                    block:^(LiveRoomInfoModel *roomInfoModel, LiveUserModel *hostUserModel, NSString *pushUrl, RTMACKModel *model) {
        if (model.result) {
            wself.roomInfoModel = roomInfoModel;
            wself.pushUrl = pushUrl;
            [wself addSubviewAndConstraints];
            [wself setupLocalRenderView];
        } else {
            AlertActionModel *alertModel = [[AlertActionModel alloc] init];
            alertModel.title = @"确定";
            alertModel.alertModelClickBlock = ^(UIAlertAction *_Nonnull action) {
                if ([action.title isEqualToString:@"确定"]) {
                    [wself.navigationController popViewControllerAnimated:YES];
                }
            };
            [[AlertActionManager shareAlertActionManager] showWithMessage:model.message actions:@[ alertModel ]];
        }
    }];
}

- (void)setupLocalRenderView {
    [[LiveRTCManager shareRtc] enableLocalVideo:YES];
    [[LiveRTCManager shareRtc] enableLocalAudio:YES];
    [[LiveRTCManager shareRtc] bingCanvasViewToUid:[LocalUserComponents userModel].uid];

    UIView *rtcStreamView = [[LiveRTCManager shareRtc] getStreamViewWithUid:[LocalUserComponents userModel].uid];
    rtcStreamView.hidden = NO;
    [self.renderView addSubview:rtcStreamView];
    [rtcStreamView mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self.renderView);
    }];

    // add effect
    [self.beautyCompoments resumeLocalEffect];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [_settingCompoments close];
    [self.beautyCompoments close];
}

#pragma mark - LiveCreateRoomControlViewDelegate

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedSwitchCameraButton:(UIButton *)button {
    [[LiveRTCManager shareRtc] switchCamera];
}

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedBeautyButton:(UIButton *)button {
    if (self.beautyCompoments) {
        [self.beautyCompoments showWithType:EffectBeautyRoleTypeHost fromSuperView:self.view dismissBlock:^(BOOL result) {
            
        }];
    } else {
        [[ToastComponents shareToastComponents] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
    }
}

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedSettingButton:(UIButton *)button {
    [self.settingCompoments showWithType:LiveRoomSettingTypeCreateRoom
                           fromSuperView:self.view
                                  roomID:self.roomInfoModel
                               userModel:nil];
}

#pragma mark - getter

- (UIButton *)startButton {
    if (!_startButton) {
        _startButton = [[UIButton alloc] init];
        _startButton.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [_startButton setTitle:@"开始直播" forState:UIControlStateNormal];
        [_startButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _startButton.titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        [_startButton addTarget:self action:@selector(startButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _startButton.layer.cornerRadius = 25;
        _startButton.layer.masksToBounds = YES;
    }
    return _startButton;
}

- (LiveCreateRoomTipView *)tipView {
    if (!_tipView) {
        _tipView = [[LiveCreateRoomTipView alloc] init];
        _tipView.message = @"本产品仅用于功能体验，单次直播时长不超20mins";
    }
    return _tipView;
}

- (UIView *)renderView {
    if (!_renderView) {
        _renderView = [[UIView alloc] init];
    }
    return _renderView;
}

- (LiveCreateRoomControlView *)controlView {
    if (!_controlView) {
        _controlView = [[LiveCreateRoomControlView alloc] init];
        _controlView.delegate = self;
    }
    return _controlView;
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
    [[LiveRTCManager shareRtc] updateCameraID:YES];
    [[LiveRTCManager shareRtc] leaveLiveRoom];
    NSLog(@"%@,%s", [NSThread currentThread], __func__);
}

@end
