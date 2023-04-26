// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "BytePlayerComponent.h"
#import <IJKMediaFramework/IJKMediaFramework.h>

@interface BytePlayerComponent ()

@property (nonatomic, strong) IJKFFMoviePlayerController *player;
@property (nonatomic, copy) NSString *currentURLString;

@end

@implementation BytePlayerComponent

#pragma mark - Methods
- (void)startPlayWithUrl:(NSString *)urlString
               superView:(UIView *)superView {
    _currentURLString = urlString;
    
    IJKFFOptions *options = [IJKFFOptions optionsByDefault];
    [options setFormatOptionIntValue:1 forKey:@"reconnect"];
    
    NSURL *url = [NSURL URLWithString:urlString];
    self.player = [[IJKFFMoviePlayerController alloc] initWithContentURL:url withOptions:options];
    self.player.shouldAutoplay = YES;
    [self installMovieNotificationObservers];
    self.player.scalingMode = IJKMPMovieScalingModeAspectFill;
    [self.player prepareToPlay];
    [superView addSubview:self.player.view];
    [self.player.view mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(superView);
    }];
    self.player.view.backgroundColor = [UIColor clearColor];
}

- (void)updatePlayScaleMode:(PullScalingMode)scalingMode {
    if (scalingMode == PullScalingModeNone) {
        self.player.scalingMode = IJKMPMovieScalingModeNone;
    } else if (scalingMode == PullScalingModeAspectFit) {
        self.player.scalingMode = IJKMPMovieScalingModeAspectFit;
    } else if (scalingMode == IJKMPMovieScalingModeAspectFill) {
        self.player.scalingMode = IJKMPMovieScalingModeAspectFill;
    } else if (scalingMode == IJKMPMovieScalingModeFill) {
        self.player.scalingMode = IJKMPMovieScalingModeFill;
    } else {
        self.player.scalingMode = IJKMPMovieScalingModeNone;
    }
}

- (void)play {
    [self.player play];
}

- (void)replacePlayWithUrl:(NSString *)url {
    if (self.player.view.superview) {
        UIView *superView = self.player.view.superview;
        [self stop];
        [self startPlayWithUrl:url superView:superView];
    }
}

- (void)stop {
    if (self.player) {
        [self.player shutdown];
    }
}

- (void)destroy {
    if (self.player) {
        [self removeMovieNotificationObservers];
        [self.player.view removeFromSuperview];
        self.player = nil;
    }
}

- (void)installMovieNotificationObservers {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadStateDidChange:)
                                                 name:IJKMPMoviePlayerLoadStateDidChangeNotification
                                               object:self.player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackFinish:)
                                                 name:IJKMPMoviePlayerPlaybackDidFinishNotification
                                               object:self.player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(mediaIsPreparedToPlayDidChange:)
                                                 name:IJKMPMediaPlaybackIsPreparedToPlayDidChangeNotification
                                               object:self.player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackStateDidChange:)
                                                 name:IJKMPMoviePlayerPlaybackStateDidChangeNotification
                                               object:self.player];
}

- (void)removeMovieNotificationObservers {
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:IJKMPMoviePlayerLoadStateDidChangeNotification
                                                  object:self.player];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:IJKMPMoviePlayerPlaybackDidFinishNotification
                                                  object:self.player];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:IJKMPMediaPlaybackIsPreparedToPlayDidChangeNotification
                                                  object:self.player];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:IJKMPMoviePlayerPlaybackStateDidChangeNotification
                                                  object:self.player];
}

#pragma mark - observer
- (void)loadStateDidChange:(NSNotification *)notification{
    IJKMPMovieLoadState loadState = self.player.loadState;
    NSLog(@"LivePullStreamCompoments loadStateDidChange : %d",(int)loadState);
}

- (void)moviePlayBackFinish:(NSNotification *)notification {
    int reason = [[[notification userInfo] valueForKey:IJKMPMoviePlayerPlaybackDidFinishReasonUserInfoKey] intValue];
    NSLog(@"LivePullStreamCompoments playBackFinish : %d",reason);
    if (reason == IJKMPMovieFinishReasonPlaybackError) {
        NSString *urlStr = _currentURLString;
        UIView *superView = self.player.view.superview;
        [self stop];
        [self startPlayWithUrl:urlStr superView:superView];
    }
}

- (void)mediaIsPreparedToPlayDidChange:(NSNotification *)notification{
    NSLog(@"LivePullStreamCompoments mediaIsPrepareToPlayDidChange");
}

- (void)moviePlayBackStateDidChange:(NSNotification *)notification{
    switch (_player.playbackState) {
        case IJKMPMoviePlaybackStateStopped:
            NSLog(@"LivePullStreamCompoments playBackState %d: stoped", (int)self.player.playbackState);
            break;
        case IJKMPMoviePlaybackStatePlaying:
            NSLog(@"LivePullStreamCompoments playBackState %d: playing", (int)self.player.playbackState);
            break;
        case IJKMPMoviePlaybackStatePaused:
            NSLog(@"LivePullStreamCompoments playBackState %d: paused", (int)self.player.playbackState);
            break;
        case IJKMPMoviePlaybackStateInterrupted:
            NSLog(@"LivePullStreamCompoments playBackState %d: interrupted", (int)self.player.playbackState);
            break;
        case IJKMPMoviePlaybackStateSeekingForward:
            break;
        case IJKMPMoviePlaybackStateSeekingBackward:
            break;
        default:
            break;
    }
}

#pragma mark - BytedPlayerDelegate

- (void)protocolStartWithConfiguration {
    // 启动配置 Player
}

- (void)protocol:(BytedPlayerProtocol *)protocol
setPlayerWithURL:(NSString *)urlString
       superView:(UIView *)superView
        SEIBlcok:(void (^)(NSDictionary * _Nonnull))SEIBlcok {
    [self startPlayWithUrl:urlString superView:superView];
}

- (void)protocol:(BytedPlayerProtocol *)protocol
updatePlayScaleMode:(PullScalingMode)scalingMode {
    [self updatePlayScaleMode:scalingMode];
}

- (void)protocolDidPlay:(BytedPlayerProtocol *)protocol {
    [self play];
}

- (void)protocolDidStop:(BytedPlayerProtocol *)protocol {
    [self stop];
}

- (void)protocol:(BytedPlayerProtocol *)protocol replacePlayWithUrl:(NSString *)url {
    [self replacePlayWithUrl:url];
}

- (void)protocolDestroy:(BytedPlayerProtocol *)protocol {
    [self destroy];
}

- (BOOL)protocolIsSupportSEI {
    return NO;
}


@end
