//
//  LiveStateIconView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/15.
//  Copyright © 2021 . All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveIconState) {
    LiveIconStateHidden,
    LiveIconStateNetQuality,
    LiveIconStateNetQualityBad,
    LiveIconStateMic,
    LiveIconStateCamera,
};

@interface LiveStateIconView : UIView

- (instancetype)initWithState:(LiveIconState)state;

- (void)updateState:(LiveIconState)state;

@end

NS_ASSUME_NONNULL_END
