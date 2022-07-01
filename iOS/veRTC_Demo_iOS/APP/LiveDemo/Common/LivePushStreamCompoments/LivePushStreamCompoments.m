//
//  LivePushStreamCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LivePushStreamCompoments.h"
#import "LivePushRenderView.h"
#import "LiveRTCManager.h"

@interface LivePushStreamCompoments ()

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, weak) LivePushRenderView *renderView;

@end

@implementation LivePushStreamCompoments

- (instancetype)initWithSuperView:(UIView *)superView
                        roomModel:(LiveRoomInfoModel *)roomModel
                    streamPushUrl:(NSString *)streamPushUrl {
    self = [super init];
    if (self) {
        _superView = superView;
    }
    return self;
}

- (void)openWithUserModel:(LiveUserModel *)userModel {
    _isConnect = YES;

    // 开启采集&推流
    [[LiveRTCManager shareRtc] startCapture];
    [[LiveRTCManager shareRtc] startPush:nil];
    [[LiveRTCManager shareRtc] bingCanvasViewToUid:userModel.uid];

    if (!_renderView) {
        LivePushRenderView *renderView = [[LivePushRenderView alloc] init];
        [renderView setUserName:userModel.name];
        [_superView addSubview:renderView];
        [renderView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.edges.equalTo(_superView);
        }];
        _renderView = renderView;
    }
    [_renderView updateHostMic:userModel.mic
                        camera:userModel.camera];

    UIView *rtcStreamView = [[LiveRTCManager shareRtc] getStreamViewWithUid:userModel.uid];
    rtcStreamView.hidden = NO;
    [_renderView.streamView addSubview:rtcStreamView];
    [rtcStreamView mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(_renderView.streamView);
    }];

    // 开启网络监听
    __weak __typeof(self) wself = self;
    [[LiveRTCManager shareRtc] didChangeNetworkQuality:^(LiveNetworkQualityStatus status, NSString *_Nonnull uid) {
      dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
                                  if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
                                      [wself.renderView updateNetworkQuality:status];
                                  }
                                }));
    }];
}

- (void)close {
    _isConnect = NO;
    if (_renderView) {
        [_renderView removeAllSubviews];
        [_renderView removeFromSuperview];
        _renderView = nil;
    }
}

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera {
    if (_renderView) {
        [_renderView updateHostMic:mic camera:camera];
    }
}

@end
