//
//  LiveAddGuestsItemView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "BaseButton.h"
#import "LiveRTCManager.h"
#import "LiveUserModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveAddGuestsItemView : BaseButton

@property (nonatomic, strong) LiveUserModel *userModel;

@property (nonatomic, copy) void (^clickBlock)(LiveUserModel *userModel);

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status;

@end

NS_ASSUME_NONNULL_END
