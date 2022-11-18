//
//  LiveCoHostView.h
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveCoHostUserListtCell.h"
#import <UIKit/UIKit.h>
@class LiveCoHostAudienceListsView;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveCoHostAudienceListsViewDelegate <NSObject>

- (void)liveCoHostAudienceListsView:(LiveCoHostAudienceListsView *)liveCoHostAudienceListsView clickButton:(LiveUserModel *)model;

@end

@interface LiveCoHostAudienceListsView : UIView

@property (nonatomic, copy) NSArray<BaseUserModel *> *dataLists;

@property (nonatomic, weak) id<LiveCoHostAudienceListsViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
