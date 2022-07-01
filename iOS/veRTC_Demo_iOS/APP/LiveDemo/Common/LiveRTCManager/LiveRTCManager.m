//
//  LiveRTCManager.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/24.
//  Copyright © 2021 . All rights reserved.
//

#import <VolcEngineRTC/objc/rtc/ByteRTCEngineKit.h>
#import <LFLiveKit/LFLiveKit.h>
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"
#import "libyuv.h"
#import "BytedPlayerProtocol.h"

typedef NS_ENUM(NSUInteger, RTCMixType) {
    RTCMixTypeClose = 0,
    RTCMixTypeServer,
    RTCMixTypeClient,
};

typedef NS_ENUM(NSUInteger, RTCMixStatus) {
    RTCMixStatusNone = 0,
    RTCMixStatusAddGuests,
    RTCMixStatusCoHost,
};

@interface LiveRTCManager () <ByteRTCEngineDelegate, LiveTranscodingDelegate, ByteRTCVideoSinkDelegate, LFLiveSessionDelegate, ByteRTCAudioProcessor>

//RTMP Push Engine
@property (nonatomic, strong) LFLiveSession *livePushEngine;

//RTMP Pull Player
@property (nonatomic, strong) BytedPlayerProtocol *player;

//RTMP Push streaming settings
@property (nonatomic, strong) LFLiveStreamInfo *pushConfig;

//Mix streaming type
@property (nonatomic, assign) RTCMixType mixType;

//Mix streaming status
@property (nonatomic, assign) RTCMixStatus mixStatus;

//Mix streaming settings
@property (nonatomic, strong) ByteRTCLiveTranscoding *transcodingSetting;

//RTC Push video streaming settings
@property (nonatomic, strong) ByteRTCVideoSolution *pushRTCVideoConfig;

//Video stream and user model binding use
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, copy) NSString *currentPullUrl;
@property (nonatomic, copy) NSString *rtcRoomID;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, assign) BOOL isVideoCaptued;
@property (nonatomic, assign) BOOL isAudioCaptued;
@property (nonatomic, assign) BOOL isPush;
@property (nonatomic, assign) BOOL isPushStarted;
@property (nonatomic, copy) void (^networkQualityBlock)(LiveNetworkQualityStatus status,
                                                        NSString *uid);
@property (nonatomic, copy) void (^pushStateBlock)(BOOL isStarted);

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
    // Video Capture
    ByteRTCVideoCaptureConfig *captureConfig = [[ByteRTCVideoCaptureConfig alloc] init];
    captureConfig.videoSize = CGSizeMake(1280, 720);
    captureConfig.frameRate = 15;
    [self.rtcEngineKit setVideoCaptureConfig:captureConfig];
    
    // Video Mirror
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    
    // Audio Capture
    ByteRTCAudioFormat *audioFormat = [[ByteRTCAudioFormat alloc] init];
    audioFormat.sampleRate = 44100;
    audioFormat.channel = 2;
    [self.rtcEngineKit registerLocalAudioProcessor:self format:audioFormat];
}

#pragma mark - Base

- (void)configEngineWithPushUrl:(NSString *)pushUrl {
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    if (NOEmptyStr(pushUrl)) {
        [self startCapture];
        //ByteRTCVideoSinkDelegate
        [self.rtcEngineKit setLocalVideoSink:ByteRTCStreamIndexMain
                                    withSink:self
                             withPixelFormat:ByteRTCVideoSinkPixelFormatI420];
        
        _transcodingSetting = [ByteRTCLiveTranscoding defaultTranscoding];
        self.pushConfig.url = pushUrl;
        NSLog(@"Manager Push setupLivePushSession %@", pushUrl);
    }
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
    
    [self closeTranscoding];
    [self stopCapture];
    [self leaveRTCRoom];
    [self leaveMultiRoom];
    [self.streamViewDic removeAllObjects];
    self.cameraID = ByteRTCCameraIDFront;
    
    if (self.livePushEngine) {
        [self stopPush];
        self.livePushEngine = nil;
    }
    [self stopPull];
    _pushConfig = nil;
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

- (void)startPush:(void (^)(BOOL isStarted))block {
    _pushStateBlock = block;
    
    if (!self.livePushEngine) {
        self.livePushEngine = [[LFLiveSession alloc] initWithAudioConfiguration:self.pushConfig.audioConfiguration
                                                             videoConfiguration:self.pushConfig.videoConfiguration
                                                                    captureType:LFLiveInputMaskAll];
        self.livePushEngine.reconnectCount = 10;
        self.livePushEngine.reconnectInterval = 5;
        self.livePushEngine.adaptiveBitrate = YES;
        self.livePushEngine.delegate = self;
        NSLog(@"Manager Push create livePushEngine");
    }
    
    if (!_isPush) {
        _isPush = YES;
        [self.livePushEngine startLive:self.pushConfig];
        NSLog(@"Manager PushEngine startPush");
    } else {
        if (block) {
            block(YES);
        }
    }
}

- (void)stopCapture {
    [self enableLocalAudio:NO];
    [self enableLocalVideo:NO];
}

- (void)stopPush {
    if (_isPush) {
        _isPush = NO;
        _isPushStarted = NO;
        [self.livePushEngine stopLive];
        NSLog(@"Manager PushEngine stopPush");
    }
}

- (void)updateRes:(CGSize)size {
    self.pushConfig.videoConfiguration.videoSize = size;
    self.pushRTCVideoConfig.videoSize = size;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateRes");
}

- (void)updateFPS:(CGFloat)fps {
    self.pushConfig.videoConfiguration.videoFrameRate = fps;
    self.pushRTCVideoConfig.frameRate = fps;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateFPS");
}

- (void)updateKBitrate:(NSInteger)kbitrate min:(NSInteger)min max:(NSInteger)max {
    self.pushConfig.videoConfiguration.videoBitRate = kbitrate * 1000;
    self.pushConfig.videoConfiguration.videoMinBitRate = min * 1000;
    self.pushConfig.videoConfiguration.videoMaxBitRate = max * 1000;
    self.pushRTCVideoConfig.maxKbps = kbitrate;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    NSLog(@"Manager PushEngine updateKBitrate");
}

- (LFLiveStreamInfo *)pushConfig {
    if (!_pushConfig) {
        LFLiveAudioConfiguration *audio = [LFLiveAudioConfiguration defaultConfiguration];
        audio.audioSampleRate = 44100;
        LFLiveVideoConfiguration *video = [LFLiveVideoConfiguration defaultConfiguration];
        video.videoSizeRespectingAspectRatio = YES;
        video.videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        video.videoBitRate = [LiveSettingVideoConfig defultVideoConfig].bitrate * 1000;
        video.videoMinBitRate = [LiveSettingVideoConfig defultVideoConfig].minBitrate * 1000;
        video.videoMaxBitRate = [LiveSettingVideoConfig defultVideoConfig].maxBitrate * 1000;
        video.videoFrameRate = [LiveSettingVideoConfig defultVideoConfig].fps;
        
        _pushConfig = [[LFLiveStreamInfo alloc] init];
        _pushConfig.audioConfiguration = audio;
        _pushConfig.videoConfiguration = video;
    }
    return _pushConfig;
}

- (ByteRTCVideoSolution *)pushRTCVideoConfig {
    if (!_pushRTCVideoConfig) {
        _pushRTCVideoConfig = [[ByteRTCVideoSolution alloc] init];
        _pushRTCVideoConfig.videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        _pushRTCVideoConfig.frameRate = [LiveSettingVideoConfig defultVideoConfig].fps;
        _pushRTCVideoConfig.maxKbps = [LiveSettingVideoConfig defultVideoConfig].bitrate;
    }
    return _pushRTCVideoConfig;
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
                    roomID:(NSString *)roomID
                    userID:(NSString *)userID {
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;
    
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileLiveBroadcasting;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    
    [self.rtcEngineKit joinRoomByKey:token
                        roomId:roomID
                      userInfo:userInfo
                 rtcRoomConfig:config];
    
    _rtcRoomID = roomID;
    NSLog(@"Manager RTCSDK joinRoomByKey %@|%@", roomID, userID);
}

- (void)leaveRTCRoom {
    _rtcRoomID = @"";
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

- (void)openTranscodingByUserList:(NSArray<LiveUserModel *> *)userList
                          pushUrl:(NSString *)pushUrl
                      isMixServer:(BOOL)isMixServer
                         isCoHost:(BOOL)isCoHost {
    _mixType = isMixServer ? RTCMixTypeServer : RTCMixTypeClient;
    _mixStatus = isCoHost ? RTCMixStatusCoHost : RTCMixStatusAddGuests;
    CGSize pushRTCVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (isMixServer) {
        // Close push, use mix server to merge and push.
        [self stopPush];
        // Sender mix SEI
        NSString *value = isCoHost ? kLiveCoreSEIValueSourceCoHost : kLiveCoreSEIValueSourceNone;
        NSDictionary *dic = @{kLiveCoreSEIKEYSource : value};
        NSString *json = [dic yy_modelToJSONString];
        _transcodingSetting.layout.appData = json;
    }
    if (_mixStatus == RTCMixStatusCoHost) {
        if (isMixServer) {
            // 服务器合流，取最大分辨率的一半
            pushRTCVideoSize = [self getMaxUserVideSize:userList];
        } else {
            // 客户端合流，取对方分辨率的一半
            pushRTCVideoSize = [self getOtherUserVideSize:userList];
        }
    }
    self.pushRTCVideoConfig.videoSize = pushRTCVideoSize;
    [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    
    _transcodingSetting.roomId = _rtcRoomID;
    _transcodingSetting.userId = [LocalUserComponents userModel].uid;
    _transcodingSetting.url = pushUrl;
    _transcodingSetting.expectedMixingType = isMixServer ? ByteRTCStreamMixingTypeByServer : ByteRTCStreamMixingTypeByClient;
    _transcodingSetting.layout.regions = [self getRegionWithUserList:userList
                                                            isCoHost:isCoHost
                                                         isMixServer:isMixServer];
    _transcodingSetting.audio.sampleRate = 44100;
    _transcodingSetting.audio.channels = 2;
    _transcodingSetting.video.fps = [LiveSettingVideoConfig defultVideoConfig].fps;
    _transcodingSetting.video.kBitRate = [LiveSettingVideoConfig defultVideoConfig].bitrate;
    _transcodingSetting.video.width = [LiveSettingVideoConfig defultVideoConfig].videoSize.width;
    _transcodingSetting.video.height = [LiveSettingVideoConfig defultVideoConfig].videoSize.height;
    
    [self.rtcEngineKit startLiveTranscoding:@"" transcoding:_transcodingSetting observer:self];
    NSLog(@"Manager RTCSDK startLiveTranscoding");
}

- (void)closeTranscoding {
    [self.rtcEngineKit stopLiveTranscoding:@""];
    if (_mixStatus == RTCMixStatusCoHost) {
        self.pushRTCVideoConfig.videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        [self.rtcEngineKit setVideoEncoderConfig:@[self.pushRTCVideoConfig]];
    }
    _mixType = RTCMixTypeClose;
    _mixStatus = RTCMixStatusNone;
    
    NSLog(@"Manager RTCSDK closeTranscoding");
}

- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                       isCoHost:(BOOL)isCoHost
                    isMixServer:(BOOL)isMixServer {
    _transcodingSetting.layout.regions = [self getRegionWithUserList:userList
                                                            isCoHost:isCoHost
                                                         isMixServer:isMixServer];
    [self.rtcEngineKit updateLiveTranscoding:@"" transcoding:_transcodingSetting];
    NSLog(@"Manager RTCSDK updateTranscodingLayout");
}

#pragma mark - LFLiveSessionDelegate

- (void)liveSession:(LFLiveSession *)session liveStateDidChange:(LFLiveState)state {
    BOOL isStarted = NO;
    if (state == LFLiveStart) {
        isStarted = YES;
        self.isPushStarted = isStarted;
        if (self.networkQualityBlock) {
            self.networkQualityBlock(LiveNetworkQualityStatusNone, [LocalUserComponents userModel].uid);
        }
    }
    if (self.pushStateBlock) {
        self.pushStateBlock(isStarted);
    }
    NSLog(@"Manager PushEngine state: %ld", (long)state);
}

- (void)liveSession:(nullable LFLiveSession *)session errorCode:(LFLiveSocketErrorCode)errorCode {
    NSLog(@"Manager PushEngine errorCode: %ld", errorCode);
}

#pragma mark - ByteRTCVideoSinkDelegate

- (void)renderPixelBuffer:(CVPixelBufferRef _Nonnull)pixelBuffer
                 rotation:(ByteRTCVideoRotation)rotation
             extendedData:(NSData * _Nullable)extendedData {
    if (RTCMixTypeClose == _mixType) {
        int srcWidth = (int)CVPixelBufferGetWidth(pixelBuffer);
        int srcHeight = (int)CVPixelBufferGetHeight(pixelBuffer);
        CVPixelBufferRef pixelBufferRotate = NULL;
        CFDictionaryRef empty = CFDictionaryCreate(kCFAllocatorDefault, NULL, NULL, 0, &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks);
        NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                                 [NSNumber numberWithBool:YES], kCVPixelBufferCGImageCompatibilityKey, [NSNumber numberWithBool:YES], kCVPixelBufferCGBitmapContextCompatibilityKey, empty, kCVPixelBufferIOSurfacePropertiesKey, nil];
        //swap width&height
        CVReturn status = CVPixelBufferCreate(kCFAllocatorDefault, srcHeight, srcWidth, kCVPixelFormatType_420YpCbCr8Planar, (__bridge CFDictionaryRef)options, &pixelBufferRotate);
        if (status == kCVReturnSuccess) {
            CVPixelBufferLockBaseAddress(pixelBuffer, 0);
            CVPixelBufferLockBaseAddress(pixelBufferRotate, 0);
            
            uint8_t *src_y = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0);
            uint8_t *src_u = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1);
            uint8_t *src_v = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 2);
            int src_stride_y = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0);
            int src_stride_u = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1);
            int src_stride_v = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 2);
            
            uint8_t *dst_y = CVPixelBufferGetBaseAddressOfPlane(pixelBufferRotate, 0);
            uint8_t *dst_u = CVPixelBufferGetBaseAddressOfPlane(pixelBufferRotate, 1);
            uint8_t *dst_v = CVPixelBufferGetBaseAddressOfPlane(pixelBufferRotate, 2);
            int dst_stride_y = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBufferRotate, 0);
            int dst_stride_u = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBufferRotate, 1);
            int dst_stride_v = (int)CVPixelBufferGetBytesPerRowOfPlane(pixelBufferRotate, 2);
            
            I420Rotate(src_y, src_stride_y, src_u, src_stride_u, src_v, src_stride_v, dst_y, dst_stride_y, dst_u, dst_stride_u, dst_v, dst_stride_v, srcWidth, srcHeight, kRotate90);
        } else {
            NSLog(@"I420BufferRotate Create failed");
        }
        
        
        CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
        CVPixelBufferUnlockBaseAddress(pixelBufferRotate, 0);
        
        [self.livePushEngine pushVideo:pixelBufferRotate];
        CVPixelBufferRelease(pixelBufferRotate);
    }
}

- (int)getRenderElapse {
    return 0;
}

#pragma mark - ByteRTCAudioProcessor

- (int)processAudioFrame:(ByteRTCAudioFrame *)audioFrame {
    if (RTCMixTypeClose == _mixType) {
        [self.livePushEngine pushAudio:audioFrame.buffer];
    }
    return 0;
}

#pragma mark - LiveTranscodingDelegate

- (void)onStreamMixingEvent:(ByteRTCStreamMixingEvent)event
                     taskId:(NSString *)taskId
                      error:(ByteRtcTranscoderErrorCode)Code
                    mixType:(ByteRTCStreamMixingType)mixType {
    if (event == ByteRTCStreamMixingEventStartSuccess) {
        if (mixType == ByteRTCStreamMixingTypeByClient) {
            _mixType = RTCMixTypeClient;
        } else if (mixType == ByteRTCStreamMixingTypeByServer) {
            _mixType = RTCMixTypeServer;
            [self stopPush];
        } else {
            // Error
        }
    }
    NSLog(@"Manager RTCSDK onStreamMixingEvent %lu %lu", (unsigned long)_mixType, (unsigned long)event);
}

- (void)onMixingAudioFrame:(ByteRTCAudioFrame *_Nonnull)audioFrame taskId:(NSString * _Nonnull)taskId {
    if (_mixType == RTCMixTypeClient) {
        [self.livePushEngine pushAudio:audioFrame.buffer];
    }
}

- (void)onMixingVideoFrame:(ByteRTCVideoFrame *_Nonnull)videoFrame taskId:(NSString * _Nonnull)taskId {
    if (_mixType == RTCMixTypeClient) {
        [self.livePushEngine pushVideo:videoFrame.textureBuf];
    }
}

- (BOOL)isSupportClientPushStream {
    if (_mixType == RTCMixTypeClient) {
        return YES;
    } else {
        return NO;
    }
}

#pragma mark - ByteRTCEngineDelegate

- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    [super rtcEngine:engine onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    [self bingCanvasViewToUid:uid];
    NSLog(@"Manager RTCSDK join %@|%ld", uid, state);
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onUserJoined:(nonnull ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    NSLog(@"Manager RTCSDK onUserJoined %@", userInfo.userId);
    [self bingCanvasViewToUid:userInfo.userId];
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

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status, NSString *uid))block {
    self.networkQualityBlock = block;
}

#pragma mark - Private Action

- (NSArray <ByteRTCVideoCompositingRegion *> *)getRegionWithUserList:(NSArray <LiveUserModel *> *)userList
                                                            isCoHost:(BOOL)isCoHost
                                                         isMixServer:(BOOL)isMixServer {
    NSInteger audienceIndex = 0;
    NSMutableArray *list = [[NSMutableArray alloc] init];
    for (int i = 0; i < userList.count; i++) {
        LiveUserModel *userModel = userList[i];
        ByteRTCVideoCompositingRegion *region = [[ByteRTCVideoCompositingRegion alloc] init];
        region.uid = userModel.uid;
        region.roomId = _rtcRoomID;
        region.localUser = [userModel.uid isEqualToString:[LocalUserComponents userModel].uid] ? YES : NO;
        NSLog(@"Manager RTCSDK region user %d|%@", region.localUser, userModel.uid);
        if (isCoHost) {
            if (userList.count < 2) {
                break;
            }
            region.renderMode = ByteRTCRenderModeHidden;
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
        } else {
            if (userList.count < 2) {
                break;
            }
            region.renderMode = ByteRTCRenderModeHidden;
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
        }
        [list addObject:region];
    }
    return [list copy];
}

- (CMTime)getCMTime {
    int64_t value = (int64_t)(CACurrentMediaTime() * 1000000000);
    CMTime time = CMTimeMake(value, 1000000000);
    return time;
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
