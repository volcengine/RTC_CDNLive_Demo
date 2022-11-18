//
//  LiveAddGuestsRoomView.h
//  veRTC_Demo
//
//  Created by on 2021/10/18.
//  
//

#import "LiveRTCManager.h"
#import "LiveUserModel.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveAddGuestsRoomView : UIView

@property (nonatomic, copy) void (^clickGuestsBlock)(LiveUserModel *userModel);

@property (nonatomic, strong, readonly) NSArray *guestList;

- (instancetype)initWithHostID:(NSString *)hostID
                 roomInfoModel:(LiveRoomInfoModel *)roomInfoModel;

- (void)updateGuests:(NSArray<LiveUserModel *> *)userList;

- (void)removeGuests:(NSString *)uid;

- (void)updateGuestsMic:(BOOL)mic uid:(NSString *)uid;

- (void)updateGuestsCamera:(BOOL)camera uid:(NSString *)uid;

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status uid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END