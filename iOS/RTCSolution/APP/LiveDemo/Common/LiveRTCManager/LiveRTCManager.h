// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveUserModel.h"
#import <Foundation/Foundation.h>
#import "LivePlayerManager.h"
@class LiveRTCManager;
NS_ASSUME_NONNULL_BEGIN

static NSString *const kLiveCoreSEIKEYSource = @"kLiveCoreSEIKEYSource";
static NSString *const kLiveCoreSEIValueSourceNone = @"kLiveCoreSEIValueSourceNone";
static NSString *const kLiveCoreSEIValueSourceCoHost = @"kLiveCoreSEIValueSourceCoHost";

typedef NS_ENUM(NSInteger, LiveNetworkQualityStatus) {
    LiveNetworkQualityStatusNone,
    LiveNetworkQualityStatusGood,
    LiveNetworkQualityStatusBad,
};

typedef NS_ENUM(NSUInteger, RTCMixStatus) {
    RTCMixStatusSingleLive = 0,
    RTCMixStatusAddGuests,
    RTCMixStatusCoHost,
};

@protocol LiveRTCManagerDelegate <NSObject>

/**
 * @brief 房间状态改变时的回调。 通过此回调，您会收到与房间相关的警告、错误和事件的通知。 例如，用户加入房间，用户被移出房间等。
 * @param manager GameRTCManager 模型
 * @param joinModel RTCJoinModel模型房间信息、加入成功失败等信息。
 */
- (void)liveRTCManager:(LiveRTCManager *)manager
    onRoomStateChanged:(RTCJoinModel *)joinModel;

@end

@interface LiveRTCManager : BaseRTCManager

@property (nonatomic, copy, nullable) void (^onUserPublishStreamBlock)(NSString *uid);

@property (nonatomic, weak) id<LiveRTCManagerDelegate> delegate;

+ (LiveRTCManager *_Nullable)shareRtc;

/**
 * @brief 加入直播房间。因观众加入直播房间时不需要加入 RTC 房间。观众需要先加入直播房间，成为上麦嘉宾时再加入 RTC 房间。
 * @param token RTS Token
 * @param roomID RTS 房间 ID
 * @param userID RTS 用户 ID
 */
- (void)joinLiveRoomByToken:(NSString *)token
                     roomID:(NSString *)roomID
                     userID:(NSString *)userID;

/**
 * @brief 离开直播房间
 */
- (void)leaveLiveRoom;
- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID;

/**
 * @brief 离开 RTC 房间
 */
- (void)leaveRTCRoom;


#pragma mark - Mix Stream & Forward Stream

/**
 * @brief 开启服务器合流
 * @param pushUrl 转推 CDN 地址
 * @param hostUser 主播用户模型
 * @param rtcRoomId RTC 房间 ID
 */
- (void)startMixStreamRetweetWithPushUrl:(NSString *)pushUrl
                                hostUser:(LiveUserModel *)hostUser
                               rtcRoomId:(NSString *)rtcRoomId;

/**
 * @brief 更新合流布局
 * @param userList 合流的用户数组
 * @param mixStatus 合流的业务类型。例如主播PK合流和主播观众合流布局不同
 * @param rtcRoomId RTC 房间 ID
 */
- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                      mixStatus:(RTCMixStatus)mixStatus
                      rtcRoomId:(NSString *)rtcRoomId;

/**
 * @brief 开启跨房间转推
 * @param roomId 对方房间 ID
 * @param token 加入对方房间所需要的 RTC Token
 */
- (void)startForwardStreamToRooms:(NSString *)roomId
                            token:(NSString *)token;

/**
 * @brief 关闭跨房间转推
 */
- (void)stopForwardStreamToRooms;


#pragma mark - Device Setting

/**
 * @brief 开启/关闭本地视频采集
 * @param isStart ture:开启视频采集 false：关闭视频采集
 */
- (void)switchVideoCapture:(BOOL)isStart;

/**
 * @brief 开启/关闭本地音频采集
 * @param isStart ture:开启音频采集 false：关闭音频采集
 */
- (void)switchAudioCapture:(BOOL)isStart;

/**
 * @brief 获取当前视频采集状态
 */
- (BOOL)getCurrentVideoCapture;

/**
 * @brief 前后摄像头切换
 */
- (void)switchCamera;

/**
 * @brief 暂停远端音频订阅流
 */
- (void)pauseRemoteAudioSubscribedStream:(BOOL)isPause;

/**
 * @brief 更新 RTC 编码分辨率，主播/上麦嘉宾使用。
 * @param size 分辨率
 */
- (void)updateVideoEncoderResolution:(CGSize)size;

/**
 * @brief 更新合流分辨率，主播使用。
 * @param size 分辨率
 */
- (void)updateLiveTranscodingResolution:(CGSize)size;

/**
 * @brief 更新合流帧率，主播使用。
 * @param fps 帧率
 */
- (void)updateLiveTranscodingFrameRate:(CGFloat)fps;

/**
 * @brief 更新合流码率，主播使用。
 * @param bitRate 码率
 */
- (void)updateLiveTranscodingBitRate:(NSInteger)bitRate;

#pragma mark - NetworkQuality

/**
 * @brief 监听 RTC 网络状态回调
 */
- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status,
                                          NSString *uid))block;

#pragma mark - RTC Render View

/**
 * @brief 获取 RTC 渲染 UIView
 * @param uid 用户ID
 */
- (UIView *)getStreamViewWithUid:(NSString *)uid;
- (void)bindCanvasViewToUid:(NSString *)uid;

/**
 * @brief 移除本地用户的渲染绑定
 */
- (void)removeCanvasLocalUid;

@end

NS_ASSUME_NONNULL_END
