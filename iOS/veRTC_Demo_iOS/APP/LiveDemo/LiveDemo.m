//
//  LiveDemo.m
//  LiveDemo-LiveDemo
//
//  Created by bytedance on 2022/4/29.
//

#import "LiveDemo.h"
#import "LiveRoomListsViewController.h"
#import <Core/NetworkReachabilityManager.h>

@implementation LiveDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [LiveRTCManager shareRtc].networkDelegate = [NetworkReachabilityManager sharedManager];
    [[LiveRTCManager shareRtc] connect:@"live"
                            loginToken:[LocalUserComponents userModel].loginToken
                                 block:^(BOOL result) {
        if (result) {
            LiveRoomListsViewController *next = [[LiveRoomListsViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:@"连接失败"];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
