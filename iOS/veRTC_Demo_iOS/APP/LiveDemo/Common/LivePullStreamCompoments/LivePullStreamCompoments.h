//
//  LivePullStreamCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import <Foundation/Foundation.h>
#import "LivePullRenderView.h"
#import "LiveRoomInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LivePullStreamCompoments : NSObject

@property (nonatomic, assign, readonly) BOOL isConnect;

- (instancetype)initWithSuperView:(UIView *)superView;

- (void)open:(LiveRoomInfoModel *)roomModel;

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera;

- (void)updateWithStatus:(PullRenderStatus)status;

- (void)close;

@end

NS_ASSUME_NONNULL_END
