//
//  LiveCoHostRoomItemView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/15.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRTCManager.h"
#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveCoHostRoomItemView : UIView

@property (nonatomic, strong) LiveUserModel *userModel;

- (instancetype)initWithIsOwn:(BOOL)isOwn;

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status;

@end

NS_ASSUME_NONNULL_END
