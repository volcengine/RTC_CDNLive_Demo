// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, PullRenderStatus) {
    // 单主播&观众连麦模式
    PullRenderStatusNone = 0,
    // PK 模式
    PullRenderStatusCoHst,
};

NS_ASSUME_NONNULL_BEGIN

@interface LivePullRenderView : UIView

@property (nonatomic, strong, readonly) UIView *liveView;

@property (nonatomic, assign) PullRenderStatus status;

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera;

- (void)setUserName:(NSString *)userName;

@end

NS_ASSUME_NONNULL_END
