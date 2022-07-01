//
//  LivePullRenderView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/21.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, PullRenderStatus) {
    PullRenderStatusNone = 0,
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
