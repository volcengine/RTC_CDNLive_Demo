//
//  LiveRoomSettingComponent.h
//  veRTC_Demo
//
//  Created by on 2021/10/25.
//  
//

#import "LiveRoomInfoModel.h"
#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, LiveRoomSettingType) {
    LiveRoomSettingTypeCreateRoom,
    LiveRoomSettingTypeHostLiving,
    LiveRoomSettingTypeHostChat,
    LiveRoomSettingTypeAudience,
    LiveRoomSettingTypeGuest
};

@interface LiveRoomSettingComponent : NSObject

- (void)showWithType:(LiveRoomSettingType)type
       fromSuperView:(UIView *)superView
              roomID:(LiveRoomInfoModel *)liveRoomModel
           userModel:(LiveUserModel *)liveUserModel;

- (void)refreshGuestSettingView;
- (void)close;

@end
