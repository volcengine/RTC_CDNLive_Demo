// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveRoomItemButton.h"
#import <UIKit/UIKit.h>
@class LiveRoomBottomView;

typedef NS_ENUM(NSInteger, BottomRoleStatus) {
    BottomRoleStatusAudience = 0,
    BottomRoleStatusGuests,
    BottomRoleStatusHost,
};

@protocol LiveRoomBottomViewDelegate <NSObject>

- (void)liveRoomBottomView:(LiveRoomBottomView *_Nonnull)liveRoomBottomView
                itemButton:(LiveRoomItemButton *_Nullable)itemButton
                roleStatus:(BottomRoleStatus)roleStatus;

@end

NS_ASSUME_NONNULL_BEGIN

@interface LiveRoomBottomView : UIView

@property (nonatomic, weak) id<LiveRoomBottomViewDelegate> delegate;

- (void)updateButtonStatus:(LiveRoomItemButtonState)status
               touchStatus:(LiveRoomItemTouchStatus)touchStatus;

- (void)updateButtonRoleStatus:(BottomRoleStatus)status;

- (LiveRoomItemTouchStatus)getButtonTouchStatus:(LiveRoomItemButtonState)buttonState;

@end

NS_ASSUME_NONNULL_END
