//
//  LiveSheetCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/15.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveSheetModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveSheetCompoments : NSObject

+ (LiveSheetCompoments *_Nullable)shareSheet;

- (void)show:(NSArray<LiveSheetModel *> *)list;

- (void)dismissUserListView;

@end

NS_ASSUME_NONNULL_END
