//
//  LiveCoHostTopSelectView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/24.
//  Copyright © 2021 . All rights reserved.
//

#import <UIKit/UIKit.h>
@class LiveCoHostTopSelectView;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveCoHostTopSelectViewDelegate <NSObject>

- (void)liveCoHostTopSelectView:(LiveCoHostTopSelectView *)liveCoHostTopSelectView clickSwitchItem:(BOOL)isAudience;

@end

@interface LiveCoHostTopSelectView : UIView

@property (nonatomic, weak) id<LiveCoHostTopSelectViewDelegate> delegate;

@property (nonatomic, copy) NSString *titleStr;

@end

NS_ASSUME_NONNULL_END
