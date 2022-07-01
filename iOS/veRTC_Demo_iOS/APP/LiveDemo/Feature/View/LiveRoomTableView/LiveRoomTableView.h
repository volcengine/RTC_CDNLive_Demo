//
//  LiveRoomTableView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
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
