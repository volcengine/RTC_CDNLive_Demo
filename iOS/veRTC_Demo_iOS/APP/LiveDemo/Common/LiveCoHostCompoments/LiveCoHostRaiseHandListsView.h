//
//  LiveCoHostRaiseHandListsView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveCoHostUserListtCell.h"
#import <UIKit/UIKit.h>
@class LiveCoHostRaiseHandListsView;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveCoHostRaiseHandListsViewDelegate <NSObject>

- (void)liveCoHostRaiseHandListsView:(LiveCoHostRaiseHandListsView *)liveCoHostRaiseHandListsView clickButton:(LiveUserModel *)model;

@end

@interface LiveCoHostRaiseHandListsView : UIView

@property (nonatomic, copy) NSArray<LiveUserModel *> *dataLists;

@property (nonatomic, weak) id<LiveCoHostRaiseHandListsViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
