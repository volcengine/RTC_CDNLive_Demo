//
//  LiveSheetModel.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/15.
//  Copyright © 2021 . All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
@class LiveSheetModel;

typedef void (^SheetModelClickBlock)(LiveSheetModel *_Nonnull action);

@interface LiveSheetModel : NSObject

@property (nonatomic, strong) NSString *titleStr;

@property (nonatomic, assign) BOOL isDisable;

@property (nonatomic, copy) SheetModelClickBlock clickBlock;

@end

NS_ASSUME_NONNULL_END
