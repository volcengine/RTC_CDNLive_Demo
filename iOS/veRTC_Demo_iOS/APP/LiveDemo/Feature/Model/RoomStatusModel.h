//
//  RoomStatusModel.h
//  veRTC_Demo
//
//  Created by on 2021/11/4.
//  
//

#import "LiveUserModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RoomStatusModel : NSObject

@property (nonatomic, assign) LiveInteractStatus interactStatus;

@property (nonatomic, copy) NSArray<LiveUserModel *> *interactUserList;

@end

NS_ASSUME_NONNULL_END
