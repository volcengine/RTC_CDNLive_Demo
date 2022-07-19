//
//  LiveRTCManager.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/24.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveUserModel.h"
#import <Foundation/Foundation.h>

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

@interface LiveRTCManager : BaseRTCManager

@property (nonatomic, copy, nullable) void (^onUserPublishStreamBlock)(NSString *uid);

/*
 * Singleton
 */
+ (LiveRTCManager *_Nullable)shareRtc;

/*
 * Start mix stream retweet
 * @param pushUrl Retweeted CDN address
 * @param hostUser Anchor model
 * @param roomId RTC room id
 */

- (void)startMixStreamRetweetWithPushUrl:(NSString *)pushUrl
                                hostUser:(LiveUserModel *)hostUser
                               rtcRoomId:(NSString *)rtcRoomId;

/*
 * update transcoding
 */
- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                      mixStatus:(RTCMixStatus)mixStatus
                      rtcRoomId:(NSString *)rtcRoomId;

/*
 * Start retweeting audio and video streams across rooms
 * @param roomId Other room ID
 * @param token Other room token
 */
- (void)startForwardStreamToRooms:(NSString *)roomId token:(NSString *)token;

/*
 * Stop retweeting audio and video streams across rooms
 */
- (void)stopForwardStreamToRooms;

/*
 * Leave the room
 */
- (void)leaveLiveRoom;

/*
 * destroy RTC engine
 */
- (void)destoryEngine;

#pragma mark - Device Setting

/*
 * Switch local video capture
 * @param enable ture:Turn on audio capture false：Turn off video capture
 */
- (void)enableLocalVideo:(BOOL)enable;

/*
 * Switch local audio capture
 * @param enable ture:Turn on audio capture false：Turn off audio capture
 */
- (void)enableLocalAudio:(BOOL)enable;

/*
 * Switch the camera
 */
- (void)switchCamera;

/*
 * Set the camera is front
 */
- (void)updateCameraID:(BOOL)isFront;

/*
 * Get current camera captued
 */
- (BOOL)getCurrentCameraCaptued;

/*
 * Turn off remote audio
 */
- (void)muteRemoteAudio:(NSString *)uid mute:(BOOL)isMute;

#pragma mark - Push

/*
 * Start capture
 */
- (void)startCapture;

/*
 * Stop capture
 */
- (void)stopCapture;

/*
 * Update resolution
 */
- (void)updateRes:(CGSize)size;

/*
 * Update frame rate
 */
- (void)updateFPS:(CGFloat)fps;

/*
 * Update bit rate
 */
- (void)updateKBitrate:(NSInteger)kbitrate min:(NSInteger)min max:(NSInteger)max;

#pragma mark - Pull

/*
 * Start playing CDN audio and video stream
 */
- (void)startPlayWithUrl:(NSString *)urlStr
               superView:(UIView *)superView;

/*
 * Update pull stream scale mode
 */
- (void)updatePlayScaleMode:(BOOL)isFill;

/*
 * Replace pull stream URL
 */
- (void)replacePlayWithUrl:(NSString *)url;

/*
 * Stop pull stream
 */
- (void)stopPull;

#pragma mark - NetworkQuality

/*
 * Listen for network callbacks
 */
- (void)didChangeNetworkQuality:(void (^)(LiveNetworkQualityStatus status, NSString *uid))block;

#pragma mark - Live Cohost / AddGuests

/*
 * Start PK, join RTC room
 */
- (void)joinRTCRoomByToken:(NSString *)token
                 rtcRoomID:(NSString *)rtcRoomID
                    userID:(NSString *)userID;

/*
 * Leave RTC room
 */
- (void)leaveRTCRoom;

/*
 * Turn off all far-end audio
 */
- (void)muteAllRemoteAudio:(BOOL)isMute;

/*
 * Get rendered view
 */
- (UIView *)getStreamViewWithUid:(NSString *)uid;

/*
 * Bing rendered view
 */
- (void)bingCanvasViewToUid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END
