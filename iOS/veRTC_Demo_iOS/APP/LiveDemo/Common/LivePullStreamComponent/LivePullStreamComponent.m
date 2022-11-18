//
//  LivePullStreamComponent.m
//  veRTC_Demo
//
//  Created by on 2021/10/18.
//  
//

#import "LivePullStreamComponent.h"
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"

@interface LivePullStreamComponent ()

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, weak) LivePullRenderView *renderView;
@property (nonatomic, strong) LiveRoomInfoModel *roomModel;

@end

@implementation LivePullStreamComponent

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
        __weak __typeof(self) wself = self;
        [[LiveRTCManager shareRtc] startPlayWithUrl:defaultResUrl
                                          superView:_renderView.liveView
                                           SEIBlcok:^(NSDictionary * _Nonnull SEIDic) {
            // 监听并解析 SEI
            // Monitor and parse SEI
            if (SEIDic && [SEIDic isKindOfClass:[NSDictionary class]]) {
                PullRenderStatus status = PullRenderStatusNone;
                NSDictionary *dic = [wself dictionaryWithJsonString:SEIDic[@"app_data"]];
                if ([dic isKindOfClass:[NSDictionary class]] &&
                    [dic[kLiveCoreSEIKEYSource] isEqualToString:kLiveCoreSEIValueSourceCoHost]) {
                    status = PullRenderStatusCoHst;
                }
                [wself updateWithStatus:status];
            }
        }];
        if (roomModel.hostUserModel.videoSize.width > roomModel.hostUserModel.videoSize.height){
            // 横屏流
            // Horizontal video flow
            [[LiveRTCManager shareRtc] updatePlayScaleMode:NO];
        } else {
            // 竖屏流
            // Vertical video flow
            [[LiveRTCManager shareRtc] updatePlayScaleMode:YES];
        }
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
