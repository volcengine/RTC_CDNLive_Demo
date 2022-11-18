//
//  LiveSheetComponent.h
//  veRTC_Demo
//
//  Created by on 2021/10/15.
//  
//

#import "LiveSheetModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveSheetComponent : NSObject

+ (LiveSheetComponent *_Nullable)shareSheet;

- (void)show:(NSArray<LiveSheetModel *> *)list;

- (void)dismissUserListView;

@end

NS_ASSUME_NONNULL_END
