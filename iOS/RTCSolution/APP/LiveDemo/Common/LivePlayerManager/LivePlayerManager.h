// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "BytedPlayerProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@interface LivePlayerManager : NSObject

+ (LivePlayerManager *_Nullable)sharePlayer;

/**
 * @brief 启动配置 Player
 */
- (void)startWithConfiguration;

/**
 * @brief 设置播放地址、父视图
 * @param urlStr 拉流地址
 * @param superView 父类视图
 * @param SEIBlcok SEI 回调
 */
- (void)setPlayerWithURL:(NSString *)urlStr
               superView:(UIView *)superView
                SEIBlcok:(void (^)(NSDictionary *SEIDic))SEIBlcok;

/**
 * @brief 开始播放
 */
- (void)playPull;

/**
 * @brief 停止播放
 */
- (void)stopPull;

/**
 * @brief 播放器是否支持 SEI 功能
 * @return BOOL YES 支持SEI，NO 不支持 SEI
 */
- (BOOL)isSupportSEI;

/**
 * @brief 更新播放比例模式
 * @param scalingMode 播放比例模式
 */
- (void)updatePlayScaleMode:(PullScalingMode)scalingMode;

/**
 * @brief 更新新的播放地址
 * @param url 新的播放地址
 */
- (void)replacePlayWithUrl:(NSString *)url;

@end

NS_ASSUME_NONNULL_END
