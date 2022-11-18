//
//  LiveCoHostRoomView.h
//  veRTC_Demo
//
//  Created by on 2021/10/15.
//  
//

#import "LiveRTCManager.h"
#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveCoHostRoomView : UIView

@property (nonatomic, copy) NSArray<LiveUserModel *> *userModelList;

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid;

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid;

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status uid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END
