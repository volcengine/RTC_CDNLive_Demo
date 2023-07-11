// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveRTCManager.h"
#import "LiveSettingVideoConfig.h"

@interface LiveRTCManager () <ByteRTCVideoDelegate, ByteRTCMixedStreamObserver>
// 业务 RTS 房间。观众用户在不上麦时无需加入 RTC 房间，但需要加入 RTS 房间进行业务逻辑处理。
@property (nonatomic, strong) ByteRTCRoom *businessRoom;
// RTC 房间
@property (nonatomic, strong, nullable) ByteRTCRoom *rtcRoom;
// RTC 房间 ID
@property (nonatomic, copy) NSString *rtcRoomID;
// 合流类型
@property (nonatomic, assign) RTCMixStatus mixStatus;
// 合流设置信息
@property (nonatomic, strong) ByteRTCMixedStreamConfig *mixedStreamConfig;
// RTC 视频推流配置信息
@property (nonatomic, strong) ByteRTCVideoEncoderConfig *pushRTCVideoConfig;
// 视频流和用户模型绑定字典
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, assign) BOOL isVideoCaptued;
@property (nonatomic, assign) BOOL isAudioCaptued;
// 网络质量 Bolck
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
    
    // 设置 RTC 采集分辨率、帧率。
    ByteRTCVideoCaptureConfig *captureConfig = [[ByteRTCVideoCaptureConfig alloc] init];
    captureConfig.videoSize = CGSizeMake(1280, 720);
    captureConfig.frameRate = 15;
    captureConfig.preference = ByteRTCVideoCapturePreferenceAutoPerformance;
    [self.rtcEngineKit setVideoCaptureConfig:captureConfig];
    
    // 设置 RTC 编码分辨率、帧率、码率。
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    // 设置视频镜像
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    // 获取合流配置信息
    _mixedStreamConfig = [ByteRTCMixedStreamConfig defaultMixedStreamConfig];
}

- (void)joinLiveRoomByToken:(NSString *)token
                     roomID:(NSString *)roomID
                     userID:(NSString *)userID {
    if (self.businessRoom) {
        [self leaveLiveRoom];
    }
    // 加入 RTS 业务房间，主播和观众都需要加入。
    self.businessRoom = [self.rtcEngineKit createRTCRoom:roomID];
    self.businessRoom.delegate = self;
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;

    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileInteractivePodcast;
    config.isAutoPublish = NO;
    config.isAutoSubscribeAudio = NO;
    config.isAutoSubscribeVideo = NO;

    [self.businessRoom joinRoom:token
                       userInfo:userInfo
                     roomConfig:config];
}

- (void)leaveLiveRoom {
    // 离开 RTS 业务房间。
    CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    [self updateVideoEncoderResolution:videoSize];
    [self.rtcEngineKit stopPushStreamToCDN:@""];
    [self leaveRTCRoom];
    
    [self switchAudioCapture:NO];
    [self switchVideoCapture:NO];
    
    [self.businessRoom leaveRoom];
    [self.businessRoom destroy];
    self.businessRoom = nil;
    
    [self.streamViewDic removeAllObjects];
    self.cameraID = ByteRTCCameraIDFront;
    [self switchCamera:ByteRTCCameraIDFront];
    _pushRTCVideoConfig = nil;
}

- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID {
    // 加入 RTC 房间，主播和上麦观众开启音视频通话时需要加入。
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileInteractivePodcast;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    self.rtcRoomID = rtcRoomID;
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:rtcRoomID];
    self.rtcRoom.delegate = self;
    [self.rtcRoom joinRoom:token userInfo:userInfo roomConfig:config];
}

- (void)leaveRTCRoom {
    // 离开 RTC 房间，音视频通话结束时需要离开房间。
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
}

#pragma mark - Mix Stream & Forward Stream

- (void)startMixStreamRetweetWithPushUrl:(NSString *)pushUrl
                                hostUser:(LiveUserModel *)hostUser
                               rtcRoomId:(NSString *)rtcRoomId {
    // 开启合流
    if (NOEmptyStr(pushUrl)) {
        [self switchVideoCapture:YES];
        [self switchAudioCapture:YES];
        
        if (IsEmptyStr(rtcRoomId)) {
            NSLog(@"Manager RTCSDK startPushMixedStreamToCDN error : roomid nil");
        }
        
        // 设置合流SEI
        NSString *json = [self getSEIJsonWithMixStatus:RTCMixStatusSingleLive];
        // 设置合流布局
        NSArray *regions = [self getRegionWithUserList:@[hostUser]
                                             mixStatus:RTCMixStatusSingleLive
                                             rtcRoomId:rtcRoomId];
        
        _mixedStreamConfig.layoutConfig.userConfigExtraInfo = json;
        _mixedStreamConfig.layoutConfig.regions = regions;
        _mixedStreamConfig.roomID = rtcRoomId;
        _mixedStreamConfig.userID = [LocalUserComponent userModel].uid;
        _mixedStreamConfig.pushURL = pushUrl;
        _mixedStreamConfig.expectedMixingType = ByteRTCMixedStreamByServer;
        _mixedStreamConfig.audioConfig.sampleRate = 44100;
        _mixedStreamConfig.audioConfig.channels = 2;
        _mixedStreamConfig.videoConfig.fps = [LiveSettingVideoConfig defultVideoConfig].fps;
        _mixedStreamConfig.videoConfig.bitrate = [LiveSettingVideoConfig defultVideoConfig].bitrate;
        _mixedStreamConfig.videoConfig.width = [LiveSettingVideoConfig defultVideoConfig].videoSize.width;
        _mixedStreamConfig.videoConfig.height = [LiveSettingVideoConfig defultVideoConfig].videoSize.height;
        
        [self.rtcEngineKit startPushMixedStreamToCDN:@""
                                         mixedConfig:_mixedStreamConfig
                                            observer:self];
    }
}

- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                      mixStatus:(RTCMixStatus)mixStatus
                      rtcRoomId:(NSString *)rtcRoomId {
    // 更新合流布局
    
    // 设置合流 SEI
    NSString *json = [self getSEIJsonWithMixStatus:mixStatus];
    // 服务器合流，取最大分辨率的一半
    CGSize pushRTCVideoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    if (mixStatus == RTCMixStatusCoHost) {
        pushRTCVideoSize = [self getMaxUserVideSize:userList];
    }
    self.pushRTCVideoConfig.width = pushRTCVideoSize.width;
    self.pushRTCVideoConfig.height = pushRTCVideoSize.height;
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    
    _mixedStreamConfig.layoutConfig.userConfigExtraInfo = json;
    _mixedStreamConfig.layoutConfig.regions = [self getRegionWithUserList:userList
                                                           mixStatus:mixStatus
                                                           rtcRoomId:rtcRoomId];
    [self.rtcEngineKit updatePushMixedStreamToCDN:@""
                                      mixedConfig:_mixedStreamConfig];
}

- (void)startForwardStreamToRooms:(NSString *)roomId token:(NSString *)token {
    // 开启跨 RTC 房间转推
    ForwardStreamConfiguration *configuration = [[ForwardStreamConfiguration alloc] init];
    configuration.roomId = roomId;
    configuration.token = token;
    
    [self.rtcRoom startForwardStreamToRooms:@[configuration]];
}

- (void)stopForwardStreamToRooms {
    // 结束跨 RTC 房间转推
    CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
    self.pushRTCVideoConfig.width = videoSize.width;
    self.pushRTCVideoConfig.height = videoSize.height;
    
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.pushRTCVideoConfig];
    _mixStatus = RTCMixStatusSingleLive;
    [self.rtcRoom stopForwardStreamToRooms];
}

#pragma mark - Device Setting

- (void)switchVideoCapture:(BOOL)isStart {
    // 开关相机采集
    if (_isVideoCaptued != isStart) {
        _isVideoCaptued = isStart;
        if (isStart) {
            [self.rtcEngineKit startVideoCapture];
        } else {
            [self.rtcEngineKit stopVideoCapture];
        }
    }
}

- (void)switchAudioCapture:(BOOL)isStart {
    // 开关麦克风采集
    if (_isAudioCaptued != isStart) {
        _isAudioCaptued = isStart;
        if (isStart) {
            [self.rtcEngineKit startAudioCapture];
        } else {
            [self.rtcEngineKit stopAudioCapture];
        }
    }
}

- (BOOL)getCurrentVideoCapture {
    return self.isVideoCaptued;
}

- (void)switchCamera {
    // 前后摄像头切换
    if (self.cameraID == ByteRTCCameraIDFront) {
        self.cameraID = ByteRTCCameraIDBack;
    } else {
        self.cameraID = ByteRTCCameraIDFront;
    }
    [self switchCamera:self.cameraID];
}

- (void)switchCamera:(ByteRTCCameraID)cameraID {
    if (cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    [self.rtcEngineKit switchCamera:cameraID];
}

- (void)pauseRemoteAudioSubscribedStream:(BOOL)isPause {
    if (isPause) {
        [self.rtcRoom pauseAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    } else {
        [self.rtcRoom resumeAllSubscribedStream:ByteRTCControlMediaTypeAudio];
    }
    
    NSArray *regions = _mixedStreamConfig.layoutConfig.regions;
    for (ByteRTCMixedStreamLayoutRegionConfig *region in regions) {
        if (![region.userID isEqualToString:[LocalUserComponent userModel].uid]) {
            region.mediaType = isPause ? ByteRTCMixedStreamMediaTypeVideoOnly : ByteRTCMixedStreamMediaTypeAudioAndVideo;
        }
    }
    [self.rtcEngineKit updatePushMixedStreamToCDN:@""
                                      mixedConfig:_mixedStreamConfig];
}

- (void)updateVideoEncoderResolution:(CGSize)size {
    // 更新 RTC 编码分辨率
    self.pushRTCVideoConfig.width = size.width;
    self.pushRTCVideoConfig.height = size.height;
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.pushRTCVideoConfig];
}

- (void)updateLiveTranscodingResolution:(CGSize)size {
    // 更新合流编码分辨率
    _mixedStreamConfig.videoConfig.width = size.width;
    _mixedStreamConfig.videoConfig.height = size.height;
    [self.rtcEngineKit updatePushMixedStreamToCDN:@""
                                      mixedConfig:_mixedStreamConfig];
}

- (void)updateLiveTranscodingFrameRate:(CGFloat)fps {
    // 更新合流编码帧率
    _mixedStreamConfig.videoConfig.fps = fps;
    [self.rtcEngineKit updatePushMixedStreamToCDN:@""
                                      mixedConfig:_mixedStreamConfig];
}

- (void)updateLiveTranscodingBitRate:(NSInteger)bitRate {
    // 更新合流编码码率
    _mixedStreamConfig.videoConfig.bitrate = bitRate;
    [self.rtcEngineKit updatePushMixedStreamToCDN:@""
                                      mixedConfig:_mixedStreamConfig];
}

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status, NSString *uid))block {
    self.networkQualityBlock = block;
}

#pragma mark - ByteRTCMixedStreamObserver

- (BOOL)isSupportClientPushStream {
    return NO;
}

- (void)onMixingEvent:(ByteRTCStreamMixingEvent)event
               taskId:(NSString *_Nonnull)taskId
                error:(ByteRTCStreamMixingErrorCode)Code
              mixType:(ByteRTCMixedStreamType)mixType {
    NSLog(@"Manager RTCSDK onStreamMixingEvent %lu %lu %lu", (unsigned long)Code, (unsigned long)mixType, (unsigned long)event);
}


#pragma mark - ByteRTCRoomDelegate

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomStateChanged:(NSString *)roomId
        withUid:(NSString *)uid
          state:(NSInteger)state
      extraInfo:(NSString *)extraInfo {
    [super rtcRoom:rtcRoom onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    if ([roomId isEqualToString:self.rtcRoomID] ) {
        // 加入 RTC 房间成功
        [self bindCanvasViewToUid:uid];
        dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
            RTCJoinModel *joinModel = [RTCJoinModel modelArrayWithClass:extraInfo state:state roomId:roomId];
            if ([self.delegate respondsToSelector:@selector(liveRTCManager:onRoomStateChanged:)]) {
                [self.delegate liveRTCManager:self onRoomStateChanged:joinModel];
            }
        });
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserJoined:(ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    if (rtcRoom == self.rtcRoom) {
        [self bindCanvasViewToUid:userInfo.userId];
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

#pragma mark - RTC Render View

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
        canvas.renderMode = ByteRTCRenderModeHidden;
        canvas.view.backgroundColor = [UIColor clearColor];
        canvas.view = streamView;
        
        [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                                    withCanvas:canvas];
        NSString *key = [NSString stringWithFormat:@"self_%@", uid];
        [self.streamViewDic setValue:streamView forKey:key];
        view = streamView;
    }
    return view;
}

- (void)removeCanvasLocalUid {
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.renderMode = ByteRTCRenderModeHidden;
    canvas.view.backgroundColor = [UIColor clearColor];
    canvas.view = nil;
    [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                                withCanvas:canvas];
    NSString *key = [NSString stringWithFormat:@"self_%@", [LocalUserComponent userModel].uid];
    [self.streamViewDic removeObjectForKey:key];
}

- (void)bindCanvasViewToUid:(NSString *)uid {
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
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view.backgroundColor = [UIColor clearColor];
                canvas.view = remoteRoomView;
                
                ByteRTCRemoteStreamKey *streamKey = [[ByteRTCRemoteStreamKey alloc] init];
                streamKey.userId = uid;
                streamKey.roomId = self.rtcRoomID;
                streamKey.streamIndex = ByteRTCStreamIndexMain;
                
                [self.rtcEngineKit setRemoteVideoCanvas:streamKey
                                             withCanvas:canvas];
                
                NSString *groupKey = [NSString stringWithFormat:@"remote_%@", uid];
                [self.streamViewDic setValue:remoteRoomView forKey:groupKey];
            }
        }
    }));
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
        ByteRTCMixedStreamLayoutRegionConfig *region = [[ByteRTCMixedStreamLayoutRegionConfig alloc] init];
        region.userID = userModel.uid;
        region.roomID = rtcRoomId;
        region.isLocalUser = [userModel.uid isEqualToString:[LocalUserComponent userModel].uid] ? YES : NO;
        region.renderMode = ByteRTCMixedStreamRenderModeHidden;
        switch (mixStatus) {
            case RTCMixStatusSingleLive: {
                // 单主播布局
                region.locationX = 0.0;
                region.locationY = 0.0;
                region.widthProportion = 1.0;
                region.heightProportion = 1.0;
                region.zOrder = 1;
                region.alpha = 1.0;
            } break;
                
            case RTCMixStatusCoHost: {
                // 主播连麦播布局
                if (region.isLocalUser) {
                    region.locationX = 0.0;
                    region.locationY = 0.25;
                    region.widthProportion = 0.5;
                    region.heightProportion = 0.5;
                    region.zOrder = 0;
                    region.alpha = 1.0;
                } else {
                    region.locationX = 0.5;
                    region.locationY = 0.25;
                    region.widthProportion = 0.5;
                    region.heightProportion = 0.5;
                    region.zOrder = 0;
                    region.alpha = 1.0;
                }
            } break;
                
            case RTCMixStatusAddGuests: {
                // 主播和嘉宾连麦播布局
                if (region.isLocalUser) {
                    region.locationX = 0.0;
                    region.locationY = 0.0;
                    region.widthProportion = 1.0;
                    region.heightProportion = 1.0;
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

                    region.locationX = regionX;
                    region.locationY = regionY;
                    region.widthProportion = regionWidth;
                    region.heightProportion = regionHeight;
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

#pragma mark - Getter

- (ByteRTCVideoEncoderConfig *)pushRTCVideoConfig {
    if (!_pushRTCVideoConfig) {
        _pushRTCVideoConfig = [[ByteRTCVideoEncoderConfig alloc] init];
        CGSize videoSize = [LiveSettingVideoConfig defultVideoConfig].videoSize;
        _pushRTCVideoConfig.width = videoSize.width;
        _pushRTCVideoConfig.height = videoSize.height;
        _pushRTCVideoConfig.frameRate = [LiveSettingVideoConfig defultVideoConfig].fps;
        _pushRTCVideoConfig.maxBitrate = [LiveSettingVideoConfig defultVideoConfig].bitrate;
    }
    return _pushRTCVideoConfig;
}

- (NSMutableDictionary<NSString *, UIView *> *)streamViewDic {
    if (!_streamViewDic) {
        _streamViewDic = [[NSMutableDictionary alloc] init];
    }
    return _streamViewDic;
}
@end
