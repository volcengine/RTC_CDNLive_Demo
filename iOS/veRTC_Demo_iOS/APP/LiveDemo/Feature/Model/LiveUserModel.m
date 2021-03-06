//
//  LiveUserModel.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveUserModel.h"
#import "LiveRTCManager.h"

@implementation LiveUserModel

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{@"roomID" : @"room_id",
             @"uid" : @"user_id",
             @"role" : @"user_role",
             @"status": @"linkmic_status",
             @"name" : @"user_name"};
}

- (BOOL)isLoginUser {
    if ([self.uid isEqualToString:[LocalUserComponents userModel].uid]) {
        return YES;
    } else {
        return NO;
    }
}

- (BOOL)modelCustomTransformFromDictionary:(NSDictionary *)dic {
    NSString *extra = dic[@"extra"];
    NSData *jsonData = [extra dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *extraDic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err) {
        NSLog(@"%@ extra解析失败：%@", [self class] , dic);
        return YES;
    }
    self.videoWidth = [extraDic[@"width"] floatValue];
    self.videoHeight = [extraDic[@"height"] floatValue];
    self.videoSize = CGSizeMake(self.videoWidth, self.videoHeight);
    return YES;
}

@end
