//
//  LiveRoomAudienceSettingView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/25.
//  Copyright © 2021 . All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class LiveRoomAudienceSettingView;
@protocol LiveRoomAudienceSettingViewDelegate <NSObject>

- (void)liveRoomAudienceSettingView:(LiveRoomAudienceSettingView *)settingView didChangeResolution:(NSInteger)index;

@end

@interface LiveRoomAudienceSettingView : UIView
@property (nonatomic, weak) id<LiveRoomAudienceSettingViewDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
