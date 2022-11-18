//
//  LiveCreateRoomControlView.h
//  veRTC_Demo
//
//  Created by on 2021/10/21.
//  
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class LiveCreateRoomControlView;

@protocol LiveCreateRoomControlViewDelegate <NSObject>

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedSwitchCameraButton:(UIButton *)button;

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedBeautyButton:(UIButton *)button;

- (void)liveCreateRoomControlView:(LiveCreateRoomControlView *)liveCreateRoomControlView didClickedSettingButton:(UIButton *)button;

@end

@interface LiveCreateRoomControlView : UIView

@property (nonatomic, weak) id<LiveCreateRoomControlViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
