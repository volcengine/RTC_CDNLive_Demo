//
//  LiveRTCManager.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/24.
//  Copyright © 2021 . All rights reserved.
//

#import <VolcEngineRTC/objc/rtc/ByteRTCEngineKit.h>
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"
#import "BytedPlayerProtocol.h"

@interface LiveRTCManager () <ByteRTCEngineDelegate, LiveTranscodingDelegate, ByteRTCVideoSinkDelegate, ByteRTCAudioProcessor>

//RTMP Pull Player
@property (nonatomic, strong) BytedPlayerProtocol *player;

//Mix streaming status
@property (nonatomic, assign) RTCMixStatus mixStatus;

//Mix streaming settings
@property (nonatomic, strong) ByteRTCLiveTranscoding *transcodingSetting;

//RTC Push video streaming settings
@property (nonatomic, strong) ByteRTCVideoSolution *pushRTCVideoConfig;

//Video stream and user model binding use
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, copy) NSString *currentPullUrl;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, assign) BOOL isVideoCaptued;
@property (nonatomic, assign) BOOL isAudioCaptued;
@property (nonatomic, copy) void (^networkQualityBlock)(LiveNetworkQualityStatus status,
                                                        NSString *uid);
@end

@implementation LiveRTCManager

+ (LiveRTCManager *_Nullable)shareRtc {
    static LiveRTCManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[LiveRTCManager alloc] init];
    });
    return manager;
}

- (instancetype)init {
    if (self = [super init]) {
        self.cameraID = ByteRTCCameraIDFront;
    }
    return self;
}

- (void)configeRTCEngine {
    [super configeRTCEngine];
    
    // Video Capture
    ByteRTCVideoCaptureConfig *captureConfig = [[ByteRTCVideoCaptureConfig alloc] init];
    captureConfig.videoSize = CGSizeMake(1280, 720);
    captureConfig.frameRate = 15;
    [self.rtcEngineKit setVideoCaptureConfig:captureConfig];
    
    // Encoder config
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    
    // Video Mirror
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    
    // Confluence retweet Setting
    _transcodingSetting = [ByteRTCLiveTranscoding defaultTranscoding];
}

- (void)startMixStreamRetweetWithPushUrl:(NSString *)pushUrl
                                hostUser:(LiveUserModel *)hostUser
                               rtcRoomId:(NSString *)rtcRoomId {
    if (NOEmptyStr(pushUrl)) {
        [self startCapture];
        
        if (IsEmptyStr(rtcRoomId)) {
            NSLog(@"Manager RTCSDK startLiveTranscoding error : roomid nil");
        }
        
        // Set mix SEI
        NSString *json = [self getSEIJsonWithMixStatus:RTCMixStatusSingleLive];
        
        // Regions
        NSArray *regions = [self getRegionWithUserList:@[hostUser]
                                        mixStatus:RTCMixStatusSingleLive
                                             rtcRoomId:rtcRoomId];
        
        _transcodingSetting.layout.appData = json;
        _transcodingSetting.layout.regions = regions;
        _transcodingSetting.roomId = rtcRoomId;
        _transcodingSetting.userId = [LocalUserComponents userModel].uid;
        _transcodingSetting.url = pushUrl;
        _transcodingSetting.expectedMixingType = ByteRTCStreamMixingTypeByServer;
        _transcodingSetting.audio.sampleRate = 44100;
        _transcodingSetting.audio.channels = 2;
        _transcodingSetting.video.fps = [LiveSettingVideoConfig defultVideoConfig].fps;
        _transcodingSetting.video.kBitRate = [LiveSettingVideoConfig defultVideoConfig].bitrate;
        _transcodingSetting.video.width = [LiveSettingVideoConfig defultVideoConfig].videoSize.width;
        _transcodingSetting.video.height = [LiveSettingVideoConfig defultVideoConfig].videoSize.height;
        
        int result = [self.rtcEngineKit startLiveTranscoding:@""
                                                 transcoding:_transcodingSetting
                                                    observer:self];
        NSLog(@"Manager RTCSDK startMixStreamRetweetWithPushUrl %@ %d", pushUrl, result);
    }
}

- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                      mixStatus:(RTCMixStatus)mixStatus
                      rtcRoomId:(NSString *)rtcRoomId {
    // Set mix SEI
    NSString *json = [self getSEIJsonWithMixStatus:mixStatus];
    
    // Servers merge, take half of the maximum resolution
    CGSize pushRTCVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (mixStatus == RTCMixStatusCoHost) {
        pushRTCVideoSize = [self getMaxUserVideSize:userList];
    }
    self.pushRTCVideoConfig.videoSize = pushRTCVideoSize;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    
    _transcodingSetting.layout.appData = json;
    _transcodingSetting.layout.regions = [self getRegionWithUserList:userList
                                                           mixStatus:mixStatus
                                                           rtcRoomId:rtcRoomId];
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    NSLog(@"Manager RTCSDK updateTranscodingLayout");
}

- (void)startForwardStreamToRooms:(NSString *)roomId token:(NSString *)token {
    ForwardStreamConfiguration *configuration = [[ForwardStreamConfiguration alloc] init];
    configuration.roomId = roomId;
    configuration.token = token;
    [self.rtcEngineKit startForwardStreamToRooms:@[configuration]];
    
    NSLog(@"Manager RTCSDK startForwardStreamToRooms %@", roomId);
}

- (void)stopForwardStreamToRooms {
    self.pushRTCVideoConfig.videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    _mixStatus = RTCMixStatusSingleLive;
    [self.rtcEngineKit stopForwardStreamToRooms];
    
    NSLog(@"Manager RTCSDK stopForwardStreamToRooms");
}

#pragma mark - Setting

- (void)enableLocalVideo:(BOOL)enable {
    if (_isVideoCaptued != enable) {
        _isVideoCaptued = enable;
        if (enable) {
            [self.rtcEngineKit startVideoCapture];
            NSLog(@"Manager RTCSDK startVideoCapture");
        } else {
            [self.rtcEngineKit stopVideoCapture];
            NSLog(@"Manager RTCSDK stopVideoCapture");
        }
    }
}

- (void)enableLocalAudio:(BOOL)enable {
    if (_isAudioCaptued != enable) {
        _isAudioCaptued = enable;
        if (enable) {
            [self.rtcEngineKit startAudioCapture];
            NSLog(@"Manager RTCSDK startAudioCapture");
        } else {
            [self.rtcEngineKit stopAudioCapture];
            NSLog(@"Manager RTCSDK stopAudioCapture");
        }
    }
}

- (void)switchCamera {
    if (self.cameraID == ByteRTCCameraIDFront) {
        self.cameraID = ByteRTCCameraIDBack;
    } else {
        self.cameraID = ByteRTCCameraIDFront;
    }
    
    [self switchCamera:self.cameraID];
    
    NSLog(@"Manager RTCSDK switchCamera");
}

- (void)updateCameraID:(BOOL)isFront {
    if (isFront) {
        self.cameraID = ByteRTCCameraIDFront;
    } else {
        self.cameraID = ByteRTCCameraIDBack;
    }
    [self switchCamera:self.cameraID];
    
    NSLog(@"Manager RTCSDK updateCameraID");
}

- (void)switchCamera:(ByteRTCCameraID)cameraID {
    if (cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    [self.rtcEngineKit switchCamera:cameraID];
}

- (BOOL)getCurrentCameraCaptued {
    return self.isVideoCaptued;
}

- (void)muteRemoteAudio:(NSString *)uid mute:(BOOL)isMute {
    if (isMute) {
        [self.rtcEngineKit pauseAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    } else {
        [self.rtcEngineKit resumeAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    }
    
    NSLog(@"Manager RTCSDK muteRemoteAudio");
}

- (void)leaveLiveRoom {
    [self.rtcEngineKit setAudioFrameObserver:nil];
    [self.rtcEngineKit stopLiveTranscoding:@""];
    [self stopCapture];
    [self leaveRTCRoom];
    [self leaveMultiRoom];
    [self.streamViewDic removeAllObjects];
    self.cameraID = ByteRTCCameraIDFront;
    
    [self stopPull];
    _pushRTCVideoConfig = nil;
    NSLog(@"Manager leaveLiveRoom");
}

- (void)destoryEngine {
    [self disconnect];
    NSLog(@"Manager destoryEngine PullEngine");
}

#pragma mark - Push

- (void)startCapture {
    [self enableLocalAudio:YES];
    [self enableLocalVideo:YES];
}

- (void)stopCapture {
    [self enableLocalAudio:NO];
    [self enableLocalVideo:NO];
}

- (void)updateRes:(CGSize)size {
    self.pushRTCVideoConfig.videoSize = size;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateRes");
}

- (void)updateFPS:(CGFloat)fps {
    self.pushRTCVideoConfig.frameRate = fps;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateFPS");
}

- (void)updateKBitrate:(NSInteger)kbitrate min:(NSInteger)min max:(NSInteger)max {
    self.pushRTCVideoConfig.maxKbps = kbitrate;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateKBitrate");
}

#pragma mark - Pull

- (void)startPlayWithUrl:(NSString *)urlStr
               superView:(UIView *)superView {
    if (![_currentPullUrl isEqualToString:urlStr]) {
        _currentPullUrl = urlStr;
        if (!self.player) {
            self.player = [[BytedPlayerProtocol alloc] init];
            [self.player startPlayWithUrl:urlStr superView:superView];
        }
    }
    [self.player play];
    NSLog(@"Manager Pull startPlayWithUrl %@", urlStr);
}

- (void)replacePlayWithUrl:(NSString *)url {
    if ([_currentPullUrl isEqualToString:url]) {
        return;
    }
    [self.player replacePlayWithUrl:url];
    [self.player play];
}

- (void)stopPull {
    _currentPullUrl = @"";
    if (self.player) {
        [self.player stop];
        self.player = nil;
    }
    NSLog(@"Manager Pull stopPull");
}

- (void)updatePlayScaleMode:(BOOL)isFill {
    [self.player updatePlayScaleMode:isFill];
    NSLog(@"Manager Pull updatePlayScaleMode %d", isFill);
}

#pragma mark - Live Cohost / AddGuests

- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID {
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;
    
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileLiveBroadcasting;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    
    [self.rtcEngineKit joinRoomByKey:token
                              roomId:rtcRoomID
                            userInfo:userInfo
                       rtcRoomConfig:config];
    
    NSLog(@"Manager RTCSDK joinRoomByKey %@|%@", rtcRoomID, userID);
}

- (void)leaveRTCRoom {
    NSString *saveKey = @"";
    UIView *saveView = nil;
    for (NSString *key in self.streamViewDic.allKeys) {
        if ([key containsString:@"self_"]) {
            saveKey = key;
            saveView = self.streamViewDic[key];
            break;
        }
    }
    [self.streamViewDic removeAllObjects];
    if (saveView && NOEmptyStr(saveKey)) {
        [self.streamViewDic setValue:saveView forKey:saveKey];
    }
    
    [self.rtcEngineKit leaveRoom];
    NSLog(@"Manager RTCSDK leaveRTCRoom");
}

- (void)muteAllRemoteAudio:(BOOL)isMute {
    if (isMute) {
        [self.rtcEngineKit pauseAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    } else {
        [self.rtcEngineKit resumeAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    }
}


#pragma mark - LiveTranscodingDelegate

- (void)onStreamMixingEvent:(ByteRTCStreamMixingEvent)event taskId:(NSString *)taskId error:(ByteRtcTranscoderErrorCode)Code mixType:(ByteRTCStreamMixingType)mixType {
    NSLog(@"Manager RTCSDK onStreamMixingEvent %lu %lu %lu", (unsigned long)Code, (unsigned long)mixType, (unsigned long)event);
}

- (BOOL)isSupportClientPushStream {
    return NO;
}

#pragma mark - ByteRTCEngineDelegate

- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    [super rtcEngine:engine onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    [self bingCanvasViewToUid:uid];
    NSLog(@"Manager RTCSDK join %@|%ld", uid, (long)state);
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onUserJoined:(nonnull ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    NSLog(@"Manager RTCSDK onUserJoined %@", userInfo.userId);
    [self bingCanvasViewToUid:userInfo.userId];
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine onUserPublishStream:(NSString *)userId type:(ByteRTCMediaStreamType)type {
    if (type == ByteRTCMediaStreamTypeBoth ||
        type == ByteRTCMediaStreamTypeVideo) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
            if (self.onUserPublishStreamBlock) {
                self.onUserPublishStreamBlock(userId);
            }
        }));
    }
    NSLog(@"Manager RTCSDK onUserPublishStream %@ %lu", userId, (unsigned long)type);
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onFirstRemoteVideoFrameRendered:(ByteRTCRemoteStreamKey *_Nonnull)streamKey withFrameInfo:(ByteRTCVideoFrameInfo *_Nonnull)frameInfo {
    NSLog(@"Manager RTCSDK onFirstRemoteVideoFrameRendered %@", streamKey.userId);
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onLocalStreamStats:(const ByteRTCLocalStreamStats *_Nonnull)stats {
    LiveNetworkQualityStatus liveStatus = LiveNetworkQualityStatusNone;
    if (stats.tx_quality == ByteRTCNetworkQualityExcellent ||
        stats.tx_quality == ByteRTCNetworkQualityGood) {
        liveStatus = LiveNetworkQualityStatusGood;
    } else {
        liveStatus = LiveNetworkQualityStatusBad;
    }
    if (self.networkQualityBlock) {
        self.networkQualityBlock(liveStatus, [LocalUserComponents userModel].uid);
    }
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onRemoteStreamStats:(const ByteRTCRemoteStreamStats *_Nonnull)stats {
    LiveNetworkQualityStatus liveStatus = LiveNetworkQualityStatusNone;
    if (stats.tx_quality == ByteRTCNetworkQualityExcellent ||
        stats.tx_quality == ByteRTCNetworkQualityGood) {
        liveStatus = LiveNetworkQualityStatusGood;
    } else {
        liveStatus = LiveNetworkQualityStatusBad;
    }
    if (self.networkQualityBlock) {
        self.networkQualityBlock(liveStatus, stats.uid);
    }
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine onForwardStreamStateChanged:(NSArray<ForwardStreamStateInfo *> *)infos {
    NSLog(@"Manager RTCSDK onForwardStreamStateChanged %@", infos);
}

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status, NSString *uid))block {
    self.networkQualityBlock = block;
}

#pragma mark - Getter

- (ByteRTCVideoSolution *)pushRTCVideoConfig {
    if (!_pushRTCVideoConfig) {
        _pushRTCVideoConfig = [[ByteRTCVideoSolution alloc] init];
        _pushRTCVideoConfig.videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        _pushRTCVideoConfig.frameRate = [LiveSettingVideoConfig defultVideoConfig].fps;
        _pushRTCVideoConfig.maxKbps = [LiveSettingVideoConfig defultVideoConfig].bitrate;
    }
    return _pushRTCVideoConfig;
}


#pragma mark - Private Action

- (NSString *)getSEIJsonWithMixStatus:(RTCMixStatus)mixStatus {
    NSString *value = (mixStatus == RTCMixStatusCoHost) ? kLiveCoreSEIValueSourceCoHost : kLiveCoreSEIValueSourceNone;
    NSDictionary *dic = @{kLiveCoreSEIKEYSource : value};
    NSString *json = [dic yy_modelToJSONString];
    return json;
}

- (NSArray *)getRegionWithUserList:(NSArray <LiveUserModel *> *)userList
                         mixStatus:(RTCMixStatus)mixStatus
                         rtcRoomId:(NSString *)rtcRoomId {
    NSInteger audienceIndex = 0;
    NSMutableArray *list = [[NSMutableArray alloc] init];
    for (int i = 0; i < userList.count; i++) {
        LiveUserModel *userModel = userList[i];
        ByteRTCVideoCompositingRegion *region = [[ByteRTCVideoCompositingRegion alloc] init];
        region.uid = userModel.uid;
        region.roomId = rtcRoomId;
        region.localUser = [userModel.uid isEqualToString:[LocalUserComponents userModel].uid] ? YES : NO;
        region.renderMode = ByteRTCRenderModeHidden;
        NSLog(@"Manager RTCSDK region user %d|%@", region.localUser, userModel.uid);
        switch (mixStatus) {
            case RTCMixStatusSingleLive: {
                region.x = 0.0;
                region.y = 0.0;
                region.width = 1.0;
                region.height = 1.0;
                region.zOrder = 1;
                region.alpha = 1.0;
            } break;
                
            case RTCMixStatusCoHost: {
                if (region.localUser) {
                    region.x = 0.0;
                    region.y = 0.25;
                    region.width = 0.5;
                    region.height = 0.5;
                    region.zOrder = 0;
                    region.alpha = 1.0;
                } else {
                    region.x = 0.5;
                    region.y = 0.25;
                    region.width = 0.5;
                    region.height = 0.5;
                    region.zOrder = 0;
                    region.alpha = 1.0;
                }
            } break;
                
            case RTCMixStatusAddGuests: {
                if (region.localUser) {
                    region.x = 0.0;
                    region.y = 0.0;
                    region.width = 1.0;
                    region.height = 1.0;
                    region.zOrder = 1;
                    region.alpha = 1.0;
                } else {
                    CGFloat screenW = 365.0;
                    CGFloat screenH = 667.0;
                    CGFloat itemHeight = 80.0;
                    CGFloat itemSpace = 6.0;
                    CGFloat itemRightSpace = 52;
                    CGFloat itemTopSpace = 500.0;
                    NSInteger index = audienceIndex++;
                    CGFloat regionHeight = itemHeight / screenH;
                    CGFloat regionWidth = regionHeight * screenH / screenW;
                    CGFloat regionY = (itemTopSpace - (itemHeight + itemSpace) * index) / screenH;
                    CGFloat regionX = 1 - (regionHeight * screenH + itemRightSpace) / screenW;

                    region.x = regionX;
                    region.y = regionY;
                    region.width = regionWidth;
                    region.height = regionHeight;
                    region.zOrder = 2;
                    region.alpha = 1.0;
                }
            } break;

            default:
                break;
        }
        [list addObject:region];
    }
    return [list copy];
}

- (UIView *)getStreamViewWithUid:(NSString *)uid {
    if (IsEmptyStr(uid)) {
        return nil;
    }
    NSString *typeStr = @"";
    if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
        typeStr = @"self";
    } else {
        typeStr = @"remote";
    }
    NSString *key = [NSString stringWithFormat:@"%@_%@", typeStr, uid];
    UIView *view = self.streamViewDic[key];
    NSLog(@"getStreamViewWithUid : %@|%@", view, uid);
    return view;
}

- (void)bingCanvasViewToUid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
            UIView *view = [self getStreamViewWithUid:uid];
            if (!view) {
                UIView *streamView = [[UIView alloc] init];
                streamView.hidden = YES;
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.uid = uid;
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view.backgroundColor = [UIColor clearColor];
                canvas.view = streamView;
                [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                                      withCanvas:canvas];
                NSString *key = [NSString stringWithFormat:@"self_%@", uid];
                [self.streamViewDic setValue:streamView forKey:key];
            }
        } else {
            UIView *remoteRoomView = [self getStreamViewWithUid:uid];
            if (!remoteRoomView) {
                remoteRoomView = [[UIView alloc] init];
                remoteRoomView.hidden = YES;
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.uid = uid;
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view.backgroundColor = [UIColor clearColor];
                canvas.view = remoteRoomView;
                [self.rtcEngineKit setRemoteVideoCanvas:canvas.uid
                                        withIndex:ByteRTCStreamIndexMain
                                       withCanvas:canvas];
                
                NSString *groupKey = [NSString stringWithFormat:@"remote_%@", uid];
                [self.streamViewDic setValue:remoteRoomView forKey:groupKey];
            }
        }
    }));
    NSLog(@"self.streamViewDic%@", self.streamViewDic);
}

- (CGSize)getOtherUserVideSize:(NSArray<LiveUserModel *> *)userList {
    CGSize otherVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (userList.count < 2) {
        return otherVideoSize;
    }
    LiveUserModel *firstUserModel = userList.firstObject;
    LiveUserModel *lastUserModel = userList.lastObject;
    if (otherVideoSize.width == firstUserModel.videoSize.width &&
        otherVideoSize.height == firstUserModel.videoSize.height) {
        otherVideoSize = lastUserModel.videoSize;
    } else {
        otherVideoSize = firstUserModel.videoSize;
    }
    if (otherVideoSize.width == 0 || otherVideoSize.height == 0) {
        otherVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    }
    CGSize newSize = CGSizeMake(otherVideoSize.width / 2,
                                otherVideoSize.height / 2);
    return newSize;
}

- (CGSize)getMaxUserVideSize:(NSArray<LiveUserModel *> *)userList {
    CGSize maxVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (userList.count < 2) {
        return maxVideoSize;
    }
    LiveUserModel *firstUserModel = userList.firstObject;
    LiveUserModel *lastUserModel = userList.lastObject;
    if ((firstUserModel.videoSize.width * firstUserModel.videoSize.height) >
        (lastUserModel.videoSize.width * lastUserModel.videoSize.height)) {
        maxVideoSize = firstUserModel.videoSize;
    } else {
        maxVideoSize = lastUserModel.videoSize;
    }
    if (maxVideoSize.width == 0 || maxVideoSize.height == 0) {
        maxVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    }
    CGSize newSize = CGSizeMake(maxVideoSize.width / 2,
                                maxVideoSize.height / 2);
    return newSize;
}

- (NSMutableDictionary<NSString *, UIView *> *)streamViewDic {
    if (!_streamViewDic) {
        _streamViewDic = [[NSMutableDictionary alloc] init];
    }
    return _streamViewDic;
}

@end
