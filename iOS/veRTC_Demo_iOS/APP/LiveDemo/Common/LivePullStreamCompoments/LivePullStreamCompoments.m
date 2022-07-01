//
//  LivePullStreamCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LivePullStreamCompoments.h"
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"

@interface LivePullStreamCompoments ()

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, weak) LivePullRenderView *renderView;
@property (nonatomic, strong) LiveRoomInfoModel *roomModel;

@end

@implementation LivePullStreamCompoments

- (instancetype)initWithSuperView:(UIView *)superView {
    self = [super init];
    if (self) {
        _superView = superView;
    }
    return self;
}

- (void)open:(LiveRoomInfoModel *)roomModel {
    _roomModel = roomModel;
    _isConnect = YES;
    
    if (!_renderView) {
        LivePullRenderView *renderView = [[LivePullRenderView alloc] init];
        [renderView setUserName:roomModel.anchorUserName];
        [_superView addSubview:renderView];
        [renderView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(_superView);
        }];
        _renderView = renderView;
    }
    
    NSString *defaultResUrl = roomModel.streamPullStreamList[[LiveSettingVideoConfig defultResPullKey]];
    
    if (NOEmptyStr(defaultResUrl)) {
        [[LiveRTCManager shareRtc] startPlayWithUrl:defaultResUrl
                                          superView:_renderView.liveView];
    }
    if (roomModel.hostUserModel) {
        [self updateHostMic:roomModel.hostUserModel.mic camera:roomModel.hostUserModel.camera];
    }
}

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera {
    if (_renderView) {
        [_renderView updateHostMic:mic camera:camera];
    }
}

- (void)updateWithStatus:(PullRenderStatus)status {
    if (_renderView) {
        _renderView.status = status;
    }
}

- (void)close {
    _isConnect = NO;
    if (_renderView) {
        [_renderView removeFromSuperview];
        _renderView = nil;
    }
    [[LiveRTCManager shareRtc] stopPull];
}

#pragma mark - tool

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if (err) {
        return nil;
    }
    return dic;
}

@end
