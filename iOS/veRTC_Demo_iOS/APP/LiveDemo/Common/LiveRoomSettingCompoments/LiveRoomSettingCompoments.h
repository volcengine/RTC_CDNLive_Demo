//
//  LiveRoomSettingCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/25.
//  Copyright © 2021 . All rights reserved.
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

@interface LiveRoomSettingCompoments : NSObject

- (void)showWithType:(LiveRoomSettingType)type
       fromSuperView:(UIView *)superView
              roomID:(LiveRoomInfoModel *)liveRoomModel
           userModel:(LiveUserModel *)liveUserModel;

- (void)refreshGuestSettingView;
- (void)close;

@end
