//
//  LivePushRenderView.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LivePushRenderView.h"
#import "LiveNoStreamingView.h"
#import "LiveStateIconView.h"

@interface LivePushRenderView ()

@property (nonatomic, strong) LiveNoStreamingView *noStreamingView;
@property (nonatomic, strong) LiveStateIconView *netQualityView;
@property (nonatomic, strong) LiveStateIconView *micView;
@property (nonatomic, strong) LiveStateIconView *cameraView;

@end

@implementation LivePushRenderView

- (instancetype)init {
    self = [super init];
    if (self) {
        [self addSubview:self.noStreamingView];
        [self.noStreamingView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.edges.equalTo(self);
        }];

        [self addSubview:self.streamView];
        [self.streamView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.edges.equalTo(self);
        }];

        [self addSubview:self.netQualityView];
        [self.netQualityView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.height.mas_equalTo(17);
          make.left.mas_equalTo(16);
          make.top.mas_equalTo(50 + [DeviceInforTool getStatusBarHight]);
        }];

        [self addSubview:self.micView];
        [self.micView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.height.left.equalTo(self.netQualityView);
          make.top.mas_equalTo(75 + [DeviceInforTool getStatusBarHight]);
        }];

        [self addSubview:self.cameraView];
        [self.cameraView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.height.left.equalTo(self.netQualityView);
          make.top.mas_equalTo(100 + [DeviceInforTool getStatusBarHight]);
        }];
    }
    return self;
}

#pragma mark - Publish Action

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera {
    self.micView.hidden = mic;
    self.cameraView.hidden = camera;

    CGFloat top = self.micView.hidden ? 75 + [DeviceInforTool getStatusBarHight] : 100 + [DeviceInforTool getStatusBarHight];
    [self.cameraView mas_updateConstraints:^(MASConstraintMaker *make) {
      make.top.mas_equalTo(top);
    }];

    [[LiveRTCManager shareRtc] enableLocalVideo:camera];
    [[LiveRTCManager shareRtc] enableLocalAudio:mic];
}

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status {
    if (status == LiveNetworkQualityStatusGood) {
        [self.netQualityView updateState:LiveIconStateNetQuality];
    } else if (status == LiveNetworkQualityStatusNone) {
        [self.netQualityView updateState:LiveIconStateHidden];
    } else {
        [self.netQualityView updateState:LiveIconStateNetQualityBad];
    }
}

- (void)setUserName:(NSString *)userName {
    [self.noStreamingView setUserName:userName];
}

#pragma mark - getter

- (LiveNoStreamingView *)noStreamingView {
    if (!_noStreamingView) {
        _noStreamingView = [[LiveNoStreamingView alloc] init];
    }
    return _noStreamingView;
}

- (LiveStateIconView *)netQualityView {
    if (!_netQualityView) {
        _netQualityView = [[LiveStateIconView alloc] initWithState:LiveIconStateHidden];
    }
    return _netQualityView;
}

- (LiveStateIconView *)micView {
    if (!_micView) {
        _micView = [[LiveStateIconView alloc] initWithState:LiveIconStateMic];
        _micView.hidden = YES;
    }
    return _micView;
}

- (LiveStateIconView *)cameraView {
    if (!_cameraView) {
        _cameraView = [[LiveStateIconView alloc] initWithState:LiveIconStateCamera];
        _cameraView.hidden = YES;
    }
    return _cameraView;
}

- (UIView *)streamView {
    if (!_streamView) {
        _streamView = [[UIView alloc] init];
    }
    return _streamView;
}

@end
