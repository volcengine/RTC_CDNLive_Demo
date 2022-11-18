//
//  LiveRoomTableView.h
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveRoomCell.h"
#import <UIKit/UIKit.h>
@class LiveRoomTableView;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveRoomTableViewDelegate <NSObject>

- (void)LiveRoomTableView:(LiveRoomTableView *)LiveRoomTableView didSelectRowAtIndexPath:(id)model;

@end

@interface LiveRoomTableView : UIView

@property (nonatomic, copy) NSArray *dataLists;

@property (nonatomic, weak) id<LiveRoomTableViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
