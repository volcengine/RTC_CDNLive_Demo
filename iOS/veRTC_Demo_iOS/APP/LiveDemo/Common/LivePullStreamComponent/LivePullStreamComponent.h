//
//  LivePullStreamComponent.h
//  veRTC_Demo
//
//  Created by on 2021/10/18.
//  
//

#import <Foundation/Foundation.h>
#import "LivePullRenderView.h"
#import "LiveRoomInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LivePullStreamComponent : NSObject

@property (nonatomic, assign, readonly) BOOL isConnect;

- (instancetype)initWithSuperView:(UIView *)superView;

- (void)open:(LiveRoomInfoModel *)roomModel;

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera;

- (void)updateWithStatus:(PullRenderStatus)status;

- (void)close;

@end

NS_ASSUME_NONNULL_END
