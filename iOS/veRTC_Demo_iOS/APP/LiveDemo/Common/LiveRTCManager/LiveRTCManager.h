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

@interface LiveRTCManager : BaseRTCManager

/*
 * Singleton
 */
+ (LiveRTCManager *_Nullable)shareRtc;

/*
 * Set the push stream address and configuration parameters
 */
- (void)configEngineWithPushUrl:(NSString *)pushUrl;

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
 * Start push
 */
- (void)startPush:(void (^__nullable)(BOOL isStarted))block;

/*
 * Stop capture
 */
- (void)stopCapture;

/*
 * Stop push
 */
- (void)stopPush;

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
                    roomID:(NSString *)roomID
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
 * turn on transcoding
 */
- (void)openTranscodingByUserList:(NSArray<LiveUserModel *> *)userList
                          pushUrl:(NSString *)pushUrl
                      isMixServer:(BOOL)isMixServer
                         isCoHost:(BOOL)isCoHost;

/*
 * turn off transcoding
 */
- (void)closeTranscoding;

/*
 * update transcoding
 */
- (void)updateTranscodingLayout:(NSArray<LiveUserModel *> *)userList
                       isCoHost:(BOOL)isCoHost
                    isMixServer:(BOOL)isMixServer;

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
