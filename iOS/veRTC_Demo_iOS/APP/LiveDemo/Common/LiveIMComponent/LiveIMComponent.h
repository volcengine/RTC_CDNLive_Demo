//
//  LiveIMComponent.h
//  veRTC_Demo
//
//  Created by on 2021/5/23.
//  
//

#import "LiveIMModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveIMComponent : NSObject

- (instancetype)initWithSuperView:(UIView *)superView;

- (void)addIM:(LiveIMModel *)model;

@end

NS_ASSUME_NONNULL_END
