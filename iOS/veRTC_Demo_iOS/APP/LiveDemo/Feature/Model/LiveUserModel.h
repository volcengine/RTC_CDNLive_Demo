//
//  LiveUserModel.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/19.
//  Copyright © 2021 . All rights reserved.
//

#import "BaseUserModel.h"
#import "LiveConstants.h"
#import "Core.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveUserModel : BaseUserModel

@property (nonatomic, copy) NSString *roomID;
@property (nonatomic, assign) LiveUserRole role;
@property (nonatomic, assign) LiveInteractStatus status;
@property (nonatomic, assign) BOOL mic;
@property (nonatomic, assign) BOOL camera;
@property (nonatomic, strong) NSString *extra;
@property (nonatomic, assign, readonly) BOOL isLoginUser;
@property (nonatomic, assign) CGFloat videoWidth;
@property (nonatomic, assign) CGFloat videoHeight;
@property (nonatomic, assign) CGSize videoSize;

@end

NS_ASSUME_NONNULL_END
