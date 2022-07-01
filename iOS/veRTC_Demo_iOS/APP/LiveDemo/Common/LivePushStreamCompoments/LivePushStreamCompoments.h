//
//  LivePushStreamCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRoomInfoModel.h"
#import "LiveUserModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LivePushStreamCompoments : NSObject

@property (nonatomic, assign, readonly) BOOL isConnect;

- (instancetype)initWithSuperView:(UIView *)superView
                        roomModel:(LiveRoomInfoModel *)roomModel
                    streamPushUrl:(NSString *)streamPushUrl;

- (void)openWithUserModel:(LiveUserModel *)userModel;

- (void)close;

- (void)updateHostMic:(BOOL)mic camera:(BOOL)camera;

@end

NS_ASSUME_NONNULL_END
