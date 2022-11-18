//
//  LiveRTCManager.m
//  veRTC_Demo
//
//  Created by on 2021/10/24.
//  
//

#import <VolcEngineRTC/objc/ByteRTCVideo.h>
#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"
#import "BytedPlayerProtocol.h"

@interface LiveRTCManager () <ByteRTCVideoDelegate, LiveTranscodingDelegate>

//RTMP Pull Player
@property (nonatomic, strong) BytedPlayerProtocol *player;

//Mix streaming status
@property (nonatomic, assign) RTCMixStatus mixStatus;

//Mix streaming settings
@property (nonatomic, strong) ByteRTCLiveTranscoding *transcodingSetting;

//RTC Push video streaming settings
@property (nonatomic, strong) ByteRTCVideoEncoderConfig *pushRTCVideoConfig;

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
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    
    // Video Mirror
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    
    // Confluence retweet Setting
    _transcodingSetting = [ByteRTCLiveTranscoding defaultTranscoding];
    
    // Initialize player configuration
    [self player];
}

- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID {
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileInteractivePodcast;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:rtcRoomID];
    self.rtcRoom.delegate = self;
    [self.rtcRoom joinRoomByToken:token userInfo:userInfo roomConfig:config];
    
    NSLog(@"Manager RTCSDK joinRoomByToken %@|%@", rtcRoomID, userID);
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
    
    [self.rtcRoom leaveRoom];
    [self.rtcRoom destroy];
    self.rtcRoom = nil;
    NSLog(@"Manager RTCSDK leaveRTCRoom");
}

- (void)startMixStreamRetweetWithPushUrl:(NSString *)pushUrl
                                hostUser:(LiveUserModel *)hostUser
                               rtcRoomId:(NSString *)rtcRoomId {
    // 开启合流
    // Enable confluence
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
        _transcodingSetting.userId = [LocalUserComponent userModel].uid;
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
    // 更新合流布局
    // Update the merge layout
    
    // 设置合流 SEI
    // Set mix SEI
    NSString *json = [self getSEIJsonWithMixStatus:mixStatus];
    
    // Servers merge, take half of the maximum resolution
    CGSize pushRTCVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (mixStatus == RTCMixStatusCoHost) {
        pushRTCVideoSize = [self getMaxUserVideSize:userList];
    }
    self.pushRTCVideoConfig.width = pushRTCVideoSize.width;
    self.pushRTCVideoConfig.height = pushRTCVideoSize.height;
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    
    _transcodingSetting.layout.appData = json;
    _transcodingSetting.layout.regions = [self getRegionWithUserList:userList
                                                           mixStatus:mixStatus
                                                           rtcRoomId:rtcRoomId];
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    NSLog(@"Manager RTCSDK updateTranscodingLayout");
}

#pragma mark - Start CoHost PK

- (void)startForwardStreamToRooms:(NSString *)roomId token:(NSString *)token {
    // 开启跨 RTC 房间转推
    // Enable cross-RTC room retweets
    ForwardStreamConfiguration *configuration = [[ForwardStreamConfiguration alloc] init];
    configuration.roomId = roomId;
    configuration.token = token;
    
    [self.rtcRoom startForwardStreamToRooms:@[configuration]];
    
    NSLog(@"Manager RTCSDK startForwardStreamToRooms %@", roomId);
}

- (void)stopForwardStreamToRooms {
    // 结束跨 RTC 房间转推
    // End retweet across RTC room
    CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    self.pushRTCVideoConfig.width = videoSize.width;
    self.pushRTCVideoConfig.height = videoSize.height;
    
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    _mixStatus = RTCMixStatusSingleLive;
    [self.rtcRoom stopForwardStreamToRooms];
    
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
        [self.rtcRoom pauseAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    } else {
        [self.rtcRoom resumeAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    }
    
    NSArray *regions = _transcodingSetting.layout.regions;
    for (ByteRTCVideoCompositingRegion *region in regions) {
        if (![region.uid isEqualToString:[LocalUserComponent userModel].uid]) {
            region.contentControl = isMute ? ByteRTCTranscoderContentControlTypeHasVideoOnly : ByteRTCTranscoderContentControlTypeHasAudioAndVideo;
        }
    }
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    NSLog(@"Manager RTCSDK muteRemoteAudio");
}

- (void)leaveLiveRoom {
    CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    [self updateVideoEncoderRes:videoSize];
    [self.rtcEngineKit setAudioFrameObserver:nil];
    [self.rtcEngineKit stopLiveTranscoding:@""];
    [self stopCapture];
    [self leaveRTCRoom];
    [self leaveMultiRTSRoom];
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

- (void)updateVideoEncoderRes:(CGSize)size {
    self.pushRTCVideoConfig.width = size.width;
    self.pushRTCVideoConfig.height = size.height;
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.pushRTCVideoConfig];
}

- (void)updateLiveTranscodingRes:(CGSize)size {
    _transcodingSetting.video.width = size.width;
    _transcodingSetting.video.height = size.height;
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    
    NSLog(@"Manager PushEngine updateRes");
}

- (void)updateLiveTranscodingFPS:(CGFloat)fps {
    _transcodingSetting.video.fps = fps;
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    
    NSLog(@"Manager PushEngine updateFPS");
}

- (void)updateLiveTranscodingKBitrate:(NSInteger)kbitrate {
    _transcodingSetting.video.kBitRate = kbitrate;
    [self.rtcEngineKit updateLiveTranscoding:@""
                                 transcoding:_transcodingSetting];
    NSLog(@"Manager PushEngine updateKBitrate");
}

#pragma mark - Pull

- (void)startPlayWithUrl:(NSString *)urlStr
               superView:(UIView *)superView
                SEIBlcok:(void (^)(NSDictionary *SEIDic))SEIBlcok {
    // 使用播放器，拉取 CDN 音视频流
    // Use the player to pull CDN audio and video streams
    if (![_currentPullUrl isEqualToString:urlStr]) {
        _currentPullUrl = urlStr;
        [self.player startPlayWithUrl:urlStr
                            superView:superView
                             SEIBlcok:SEIBlcok];
    }
    [self.player play];
    NSLog(@"Manager Pull startPlayWithUrl %@", urlStr);
}

- (BOOL)isSupportSEI {
    // 播放器是否支持解析 SEI
    // Whether the player supports parsing SEI
    return [self.player isSupportSEI];
}

- (void)replacePlayWithUrl:(NSString *)url {
    // 播放器更新拉流地址
    // The player updates the pull stream address
    if ([_currentPullUrl isEqualToString:url]) {
        return;
    }
    _currentPullUrl = url;
    [self.player replacePlayWithUrl:url];
    [self.player play];
}

- (void)stopPull {
    // 播放器停止拉流
    // The player stops pulling the stream
    _currentPullUrl = @"";
    if (self.player) {
        [self.player stop];
    }
    NSLog(@"Manager Pull stopPull");
}

- (void)updatePlayScaleMode:(BOOL)isFill {
    [self.player updatePlayScaleMode:isFill];
    NSLog(@"Manager Pull updatePlayScaleMode %d", isFill);
}

- (void)muteAllRemoteAudio:(BOOL)isMute {
    if (isMute) {
        [self.rtcRoom pauseAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    } else {
        [self.rtcRoom resumeAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    }
}

#pragma mark - LiveTranscodingDelegate

- (void)onStreamMixingEvent:(ByteRTCStreamMixingEvent)event taskId:(NSString *)taskId error:(ByteRtcTranscoderErrorCode)Code mixType:(ByteRTCStreamMixingType)mixType {
    NSLog(@"Manager RTCSDK onStreamMixingEvent %lu %lu %lu", (unsigned long)Code, (unsigned long)mixType, (unsigned long)event);
}

- (BOOL)isSupportClientPushStream {
    return NO;
}

#pragma mark - ByteRTCVideoDelegate

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    [super rtcRoom:rtcRoom onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    if ([rtcRoom.getRoomId isEqualToString:self.rtcRoom.getRoomId] ) {
        [self bingCanvasViewToUid:uid];
    }
    NSLog(@"Manager RTCSDK join %@|%ld", uid, (long)state);
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserJoined:(ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    NSLog(@"Manager RTCSDK onUserJoined %@", userInfo.userId);
    if ([rtcRoom.getRoomId isEqualToString:self.rtcRoom.getRoomId] ) {
        [self bingCanvasViewToUid:userInfo.userId];
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserPublishStream:(NSString *)userId type:(ByteRTCMediaStreamType)type {
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

- (void)rtcEngine:(ByteRTCVideo *)engine onFirstRemoteVideoFrameRendered:(ByteRTCRemoteStreamKey *)streamKey withFrameInfo:(ByteRTCVideoFrameInfo *)frameInfo {
    NSLog(@"Manager RTCSDK onFirstRemoteVideoFrameRendered %@", streamKey.userId);
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onLocalStreamStats:(ByteRTCLocalStreamStats *)stats {

    LiveNetworkQualityStatus liveStatus = LiveNetworkQualityStatusNone;
    if (stats.tx_quality == ByteRTCNetworkQualityExcellent ||
        stats.tx_quality == ByteRTCNetworkQualityGood) {
        liveStatus = LiveNetworkQualityStatusGood;
    } else {
        liveStatus = LiveNetworkQualityStatusBad;
    }
    if (self.networkQualityBlock) {
        self.networkQualityBlock(liveStatus, [LocalUserComponent userModel].uid);
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRemoteStreamStats:(ByteRTCRemoteStreamStats *)stats {
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

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onForwardStreamStateChanged:(NSArray<ForwardStreamStateInfo *> *)infos {
    NSLog(@"Manager RTCSDK onForwardStreamStateChanged %@", infos);
}

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status, NSString *uid))block {
    self.networkQualityBlock = block;
}

#pragma mark - Getter

- (ByteRTCVideoEncoderConfig *)pushRTCVideoConfig {
    if (!_pushRTCVideoConfig) {
        _pushRTCVideoConfig =[[ByteRTCVideoEncoderConfig alloc] init];
        CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        _pushRTCVideoConfig.width = videoSize.width;
        _pushRTCVideoConfig.height = videoSize.height;
        _pushRTCVideoConfig.frameRate = [LiveSettingVideoConfig defultVideoConfig].fps;
        _pushRTCVideoConfig.maxBitrate = [LiveSettingVideoConfig defultVideoConfig].bitrate;
    }
    return _pushRTCVideoConfig;
}

- (BytedPlayerProtocol *)player {
    if (!_player) {
        _player = [[BytedPlayerProtocol alloc] init];
        [_player startWithConfiguration];
    }
    return _player;
}

#pragma mark - Private Action

- (NSString *)getSEIJsonWithMixStatus:(RTCMixStatus)mixStatus {
    NSString *value = (mixStatus == RTCMixStatusCoHost) ? kLiveCoreSEIValueSourceCoHost : kLiveCoreSEIValueSourceNone;
    NSDictionary *dic = @{kLiveCoreSEIKEYSource : value ?: @""};
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
        region.localUser = [userModel.uid isEqualToString:[LocalUserComponent userModel].uid] ? YES : NO;
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
    if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
        typeStr = @"self";
    } else {
        typeStr = @"remote";
    }
    NSString *key = [NSString stringWithFormat:@"%@_%@", typeStr, uid];
    UIView *view = self.streamViewDic[key];
    if (!view && [uid isEqualToString:[LocalUserComponent userModel].uid]) {
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
        view = streamView;
    }
    NSLog(@"getStreamViewWithUid : %@|%@", view, uid);
    return view;
}

- (void)removeCanvasLocalUid {
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.uid = [LocalUserComponent userModel].uid;
    canvas.renderMode = ByteRTCRenderModeHidden;
    canvas.view.backgroundColor = [UIColor clearColor];
    canvas.view = nil;
    [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                          withCanvas:canvas];
    NSString *key = [NSString stringWithFormat:@"self_%@", [LocalUserComponent userModel].uid];
    [self.streamViewDic removeObjectForKey:key];
}

- (void)bingCanvasViewToUid:(NSString *)uid {
    if (uid.length == 0) {
        return;
    }
    
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
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
                canvas.roomId = self.rtcRoom.getRoomId;
                [self.rtcEngineKit setRemoteVideoCanvas:uid
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
