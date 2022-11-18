//
//  LiveCoHostView.h
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveAddGuestsUserListtCell.h"
#import <UIKit/UIKit.h>
@class LiveAddGuestsListsView;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveAddGuestsListsViewDelegate <NSObject>

- (void)liveAddGuestsListsView:(LiveAddGuestsListsView *)liveAddGuestsListsView clickButton:(LiveUserModel *)model;

@end

@interface LiveAddGuestsListsView : UIView

@property (nonatomic, copy) NSArray<LiveUserModel *> *dataLists;

@property (nonatomic, weak) id<LiveAddGuestsListsViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
