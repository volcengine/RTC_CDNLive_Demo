//
//  LiveAddGuestsApplyView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/20.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveAddGuestsApplyView : UIView

@property (nonatomic, strong) LiveUserModel *userModel;

@property (nonatomic, copy) void (^clickApplyBlock)(void);

- (void)updateApplying;

- (void)resetStatus;

@end

NS_ASSUME_NONNULL_END
