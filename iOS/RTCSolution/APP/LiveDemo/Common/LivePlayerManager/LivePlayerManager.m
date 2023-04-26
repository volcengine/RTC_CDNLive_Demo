// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LivePlayerManager.h"

@interface LivePlayerManager ()

@property (nonatomic, strong) BytedPlayerProtocol *player;

@property (nonatomic, copy) NSString *currentPullUrl;

@end

@implementation LivePlayerManager

+ (LivePlayerManager *_Nullable)sharePlayer {
    static LivePlayerManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[LivePlayerManager alloc] init];
    });
    return manager;
}

#pragma mark - Publish Action

- (void)startWithConfiguration {
    [self.player startWithConfiguration];
}

- (void)setPlayerWithURL:(NSString *)urlStr
               superView:(UIView *)superView
                SEIBlcok:(void (^)(NSDictionary *SEIDic))SEIBlcok {
    // 使用播放器，拉取 CDN 音视频流
    if (![_currentPullUrl isEqualToString:urlStr]) {
        _currentPullUrl = urlStr;
        [self.player setPlayerWithURL:urlStr
                            superView:superView
                             SEIBlcok:SEIBlcok];
    }
}

- (BOOL)isSupportSEI {
    // 播放器是否支持解析 SEI
    return [self.player isSupportSEI];
}

- (void)replacePlayWithUrl:(NSString *)url {
    // 播放器更新拉流地址
    if ([_currentPullUrl isEqualToString:url]) {
        return;
    }
    _currentPullUrl = url;
    [self.player replacePlayWithUrl:url];
    [self.player play];
}

- (void)stopPull {
    // 播放器停止拉流
    _currentPullUrl = @"";
    if (self.player) {
        [self.player stop];
        [self.player destroy];
    }
}

- (void)playPull {
    if (self.player) {
        [self.player play];
    }
}

- (void)updatePlayScaleMode:(PullScalingMode)scalingMode {
    [self.player updatePlayScaleMode:scalingMode];
}

#pragma mark - Getter

- (BytedPlayerProtocol *)player {
    if (!_player) {
        _player = [[BytedPlayerProtocol alloc] init];
    }
    return _player;
}

@end
