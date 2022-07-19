//
//  LiveRoomSettingCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/25.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRoomSettingCompoments.h"
#import "LiveCreateRoomSettingView.h"
#import "LiveRTCManager.h"
#import "LiveRoomAudienceSettingView.h"
#import "LiveRoomGuestSettingView.h"
#import "LiveRoomHostSettingView.h"
#import "LiveSettingVideoConfig.h"

@interface LiveRoomSettingCompoments () <LiveCreateRoomSettingViewDelegate, LiveRoomHostSettingViewDelegate, LiveRoomGuestSettingViewDelegate, LiveRoomAudienceSettingViewDelegate>

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) LiveCreateRoomSettingView *createRoomSettingView;
@property (nonatomic, strong) LiveRoomHostSettingView *hostSettingView;
@property (nonatomic, strong) LiveRoomGuestSettingView *guestSettingView;
@property (nonatomic, strong) LiveRoomAudienceSettingView *audienceSettingView;
@property (nonatomic, strong) LiveRoomInfoModel *liveRoomModel;
@property (nonatomic, strong) LiveUserModel *liveUserModel;
@property (nonatomic, assign) BOOL mic;
@property (nonatomic, assign) BOOL camera;
@property (nonatomic, assign) BOOL isConnect;
@end

@implementation LiveRoomSettingCompoments

- (void)dealloc {
    NSLog(@"%@,%s", [NSThread currentThread], __func__);
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _isConnect = NO;
    }
    return self;
}

- (void)showWithType:(LiveRoomSettingType)type
       fromSuperView:(UIView *)superView
              roomID:(LiveRoomInfoModel *)liveRoomModel
           userModel:(LiveUserModel *)liveUserModel {
    if (_isConnect) {
        return;
    }
    _isConnect = YES;
    self.superView = superView;
    _liveRoomModel = liveRoomModel;
    _liveUserModel = liveUserModel;

    if (liveUserModel) {
        _mic = liveUserModel.mic;
        _camera = liveUserModel.camera;
    } else {
        _mic = YES;
        _camera = YES;
    }

    switch (type) {
        case LiveRoomSettingTypeCreateRoom: {
            [superView addSubview:self.createRoomSettingView];
            [self.createRoomSettingView mas_makeConstraints:^(MASConstraintMaker *make) {
              CGFloat height = 300 + [DeviceInforTool getVirtualHomeHeight];
              make.left.right.equalTo(superView);
              make.height.mas_equalTo(height);
              make.bottom.equalTo(superView).offset(height);
            }];
            [superView layoutIfNeeded];
            [superView setNeedsUpdateConstraints];

            [UIView animateWithDuration:0.25
                             animations:^{
                               [self.createRoomSettingView mas_updateConstraints:^(MASConstraintMaker *make) {
                                 make.bottom.equalTo(superView);
                               }];
                               [superView layoutIfNeeded];
                             }];
        } break;
        case LiveRoomSettingTypeHostLiving: {
            self.hostSettingView.allowChangeConfig = YES;
            self.hostSettingView.isMicOn = _mic;
            self.hostSettingView.isCameraOn = _camera;
            [self updateSwitchCamera:[[LiveRTCManager shareRtc] getCurrentCameraCaptued]];
            [superView addSubview:self.hostSettingView];
            [self.hostSettingView mas_makeConstraints:^(MASConstraintMaker *make) {
              CGFloat height = 352 + [DeviceInforTool getVirtualHomeHeight];
              make.left.right.equalTo(superView);
              make.height.mas_equalTo(height);
              make.bottom.equalTo(superView).offset(height);
            }];
            [superView layoutIfNeeded];
            [superView setNeedsUpdateConstraints];

            [UIView animateWithDuration:0.25
                             animations:^{
                               [self.hostSettingView mas_updateConstraints:^(MASConstraintMaker *make) {
                                 make.bottom.equalTo(superView);
                               }];
                               [superView layoutIfNeeded];
                             }];
        } break;
        case LiveRoomSettingTypeHostChat: {
            self.hostSettingView.allowChangeConfig = NO;
            self.hostSettingView.isMicOn = _mic;
            self.hostSettingView.isCameraOn = _camera;

            [superView addSubview:self.hostSettingView];
            [self.hostSettingView mas_makeConstraints:^(MASConstraintMaker *make) {
              CGFloat height = 352 + [DeviceInforTool getVirtualHomeHeight];
              make.left.right.equalTo(superView);
              make.height.mas_equalTo(height);
              make.bottom.equalTo(superView).offset(height);
            }];
            [superView layoutIfNeeded];
            [superView setNeedsUpdateConstraints];

            [UIView animateWithDuration:0.25
                             animations:^{
                               [self.hostSettingView mas_updateConstraints:^(MASConstraintMaker *make) {
                                 make.bottom.equalTo(superView);
                               }];
                               [superView layoutIfNeeded];
                             }];
        } break;
        case LiveRoomSettingTypeGuest: {
            self.guestSettingView.isMicOn = _mic;
            self.guestSettingView.isCameraOn = _camera;
            [superView addSubview:self.guestSettingView];
            [self.guestSettingView mas_makeConstraints:^(MASConstraintMaker *make) {
              CGFloat height = 149 + [DeviceInforTool getVirtualHomeHeight];
              make.left.right.equalTo(superView);
              make.height.mas_equalTo(height);
              make.bottom.equalTo(superView).offset(height);
            }];
            [superView layoutIfNeeded];
            [superView setNeedsUpdateConstraints];

            [UIView animateWithDuration:0.25
                             animations:^{
                               [self.guestSettingView mas_updateConstraints:^(MASConstraintMaker *make) {
                                 make.bottom.equalTo(superView);
                               }];
                               [superView layoutIfNeeded];
                             }];
        } break;
        case LiveRoomSettingTypeAudience: {
            [superView addSubview:self.audienceSettingView];
            [self.audienceSettingView mas_makeConstraints:^(MASConstraintMaker *make) {
              CGFloat height = 149 + [DeviceInforTool getVirtualHomeHeight];
              make.left.right.equalTo(superView);
              make.height.mas_equalTo(height);
              make.bottom.equalTo(superView).offset(height);
            }];
            [superView layoutIfNeeded];
            [superView setNeedsUpdateConstraints];

            [UIView animateWithDuration:0.25
                             animations:^{
                               [self.audienceSettingView mas_updateConstraints:^(MASConstraintMaker *make) {
                                 make.bottom.equalTo(superView);
                               }];
                               [superView layoutIfNeeded];
                             }];
        } break;

        default:
            break;
    }
}

- (void)close {
    if (!_isConnect) {
        return;
    }
    _isConnect = NO;
    if (_createRoomSettingView.superview) {
        [_createRoomSettingView removeFromSuperview];
    }
    if (_hostSettingView.superview) {
        [_hostSettingView removeFromSuperview];
    }
    if (_guestSettingView.superview) {
        [_guestSettingView removeFromSuperview];
    }
    if (_audienceSettingView.superview) {
        [_audienceSettingView removeFromSuperview];
    }
}

- (void)refreshGuestSettingView {
    _mic = self.liveUserModel.mic;
    _camera = self.liveUserModel.camera;

    self.guestSettingView.isMicOn = _mic;
    self.guestSettingView.isCameraOn = _camera;
}

- (void)updateAudienceResolution:(NSInteger)index {
    NSString *streamKey;
    switch (index) {
        case 0: {
            streamKey = @"480";
        } break;
        case 1: {
            streamKey = @"540";
        } break;
        case 2: {
            streamKey = @"720";
        } break;
        case 3: {
            streamKey = @"1080";
        } break;
        default: {
            streamKey = @"720";
        } break;
    }

    NSString *pullUrl = [self.liveRoomModel.streamPullStreamList objectForKey:streamKey];
    [[LiveRTCManager shareRtc] replacePlayWithUrl:pullUrl];
}

#pragma mark - LiveCreateRoomSettingViewDelegate

- (void)liveCreateRoomSettingView:(nonnull LiveCreateRoomSettingView *)settingView didChangeBitrate:(NSInteger)bitrate {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [[LiveRTCManager shareRtc] updateKBitrate:videoConfig.bitrate min:videoConfig.minBitrate max:videoConfig.maxBitrate];
}

- (void)liveCreateRoomSettingView:(nonnull LiveCreateRoomSettingView *)settingView didChangeResolution:(NSInteger)index {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [[LiveRTCManager shareRtc] updateRes:videoConfig.videoSize];
}

- (void)liveCreateRoomSettingView:(nonnull LiveCreateRoomSettingView *)settingView didChangefpsType:(LiveSettingVideoFpsType)fps {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [[LiveRTCManager shareRtc] updateFPS:videoConfig.fps];
}

#pragma mark - LiveRoomHostSettingViewDelegate

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didChangeCameraState:(BOOL)isOn {
    _camera = !isOn;
    __weak __typeof(self) wself = self;
    [LiveRTMManager liveUpdateMediaStatus:self.liveRoomModel.roomID
                                             mic:_mic
                                          camera:_camera
                                           block:^(RTMACKModel *_Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
        else {
            [wself updateSwitchCamera:!isOn];
        }
    }];
}

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didChangeMicState:(BOOL)isOn {
    _mic = !isOn;
    [LiveRTMManager liveUpdateMediaStatus:self.liveRoomModel.roomID
                                             mic:_mic
                                          camera:_camera
                                           block:^(RTMACKModel *_Nonnull model) {
                                             if (!model.result) {
                                                 [[ToastComponents shareToastComponents] showWithMessage:model.message];
                                             }
                                           }];
}

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didChangeBitrate:(NSInteger)bitrate {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [[LiveRTCManager shareRtc] updateKBitrate:videoConfig.bitrate min:videoConfig.minBitrate max:videoConfig.maxBitrate];
}

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didChangeResolution:(NSInteger)index {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [LiveRTMManager liveUpdateResWithSize:videoConfig.videoSize
                                      roomID:self.liveRoomModel.roomID
                                       block:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            [[LiveRTCManager shareRtc] updateRes:videoConfig.videoSize];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        }
    }];
}

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didChangefpsType:(LiveSettingVideoFpsType)fps {
    LiveSettingVideoConfig *videoConfig = settingView.videoConfig;
    [[LiveRTCManager shareRtc] updateFPS:videoConfig.fps];
}

- (void)liveRoomHostSettingView:(nonnull LiveRoomHostSettingView *)settingView didSwitchCamera:(BOOL)isFront {
    [[LiveRTCManager shareRtc] switchCamera];
}

#pragma mark - LiveRoomGuestSettingViewDelegate

- (void)liveRoomGuestSettingView:(nonnull LiveRoomGuestSettingView *)settingView didChangeCameraState:(BOOL)isOn {
    _camera = !isOn;
    [LiveRTMManager liveUpdateMediaStatus:self.liveRoomModel.roomID
                                             mic:_mic
                                          camera:_camera
                                           block:^(RTMACKModel *_Nonnull model) {
                                             if (!model.result) {
                                                 [[ToastComponents shareToastComponents] showWithMessage:model.message];
                                             }
                                           }];
}

- (void)liveRoomGuestSettingView:(nonnull LiveRoomGuestSettingView *)settingView didChangeMicState:(BOOL)isOn {
    _mic = !isOn;
    [LiveRTMManager liveUpdateMediaStatus:self.liveRoomModel.roomID
                                             mic:_mic
                                          camera:_camera
                                           block:^(RTMACKModel *_Nonnull model) {
                                             if (!model.result) {
                                                 [[ToastComponents shareToastComponents] showWithMessage:model.message];
                                             }
                                           }];
}

- (void)liveRoomGuestSettingView:(nonnull LiveRoomGuestSettingView *)settingView didSwitchCamera:(BOOL)isFront {
    [[LiveRTCManager shareRtc] switchCamera];
}

#pragma mark - LiveRoomAudienceSettingViewDelegate

- (void)liveRoomAudienceSettingView:(nonnull LiveRoomAudienceSettingView *)settingView didChangeResolution:(NSInteger)index {
    [self updateAudienceResolution:index];
}

#pragma mark - Private Action

- (void)updateSwitchCamera:(BOOL)isCaptued {
    self.hostSettingView.isSwitchCamera = isCaptued;
}

#pragma mark - getter

- (LiveRoomHostSettingView *)hostSettingView {
    if (!_hostSettingView) {
        _hostSettingView = [[LiveRoomHostSettingView alloc] init];
        _hostSettingView.videoConfig = self.createRoomSettingView.videoConfig;
        _hostSettingView.delegate = self;
    }
    return _hostSettingView;
}

- (LiveRoomGuestSettingView *)guestSettingView {
    if (!_guestSettingView) {
        _guestSettingView = [[LiveRoomGuestSettingView alloc] init];
        _guestSettingView.delegate = self;
    }
    return _guestSettingView;
}

- (LiveCreateRoomSettingView *)createRoomSettingView {
    if (!_createRoomSettingView) {
        _createRoomSettingView = [[LiveCreateRoomSettingView alloc] init];
        _createRoomSettingView.videoConfig = [LiveSettingVideoConfig defultVideoConfig];
        _createRoomSettingView.delegate = self;
    }
    return _createRoomSettingView;
}

- (LiveRoomAudienceSettingView *)audienceSettingView {
    if (!_audienceSettingView) {
        _audienceSettingView = [[LiveRoomAudienceSettingView alloc] init];
        _audienceSettingView.delegate = self;
    }
    return _audienceSettingView;
}

@end
