//
//  LiveRoomSettingView.h
//  veRTC_Demo
//
//  Created by on 2021/10/24.
//  
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class LiveRoomGuestSettingView;
@protocol LiveRoomGuestSettingViewDelegate <NSObject>

//- (void)liveRoomGuestSettingView:(LiveRoomGuestSettingView *)settingView didChangeResolution:(NSInteger)index;
- (void)liveRoomGuestSettingView:(LiveRoomGuestSettingView *)settingView didSwitchCamera:(BOOL)isFront;
- (void)liveRoomGuestSettingView:(LiveRoomGuestSettingView *)settingView didChangeMicState:(BOOL)isOn;
- (void)liveRoomGuestSettingView:(LiveRoomGuestSettingView *)settingView didChangeCameraState:(BOOL)isOn;

@end

@interface LiveRoomGuestSettingView : UIView
@property (nonatomic, weak) id<LiveRoomGuestSettingViewDelegate> delegate;

@property (nonatomic, assign) BOOL isMicOn;
@property (nonatomic, assign) BOOL isCameraOn;
@end

NS_ASSUME_NONNULL_END
