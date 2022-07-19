//
//  LiveRoomViewController+SocketControl.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/28.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRoomViewController+SocketControl.h"

@implementation LiveRoomViewController (SocketControl)

- (void)addSocketListener {
    // Global Notification message
    __weak __typeof(self) wself = self;
    [LiveRTMManager onAudienceJoinRoomWithBlock:^(LiveUserModel *_Nonnull userModel, NSString *_Nonnull audienceCount) {
        if (wself) {
            [wself addUser:userModel audienceCount:[audienceCount integerValue]];
        }
    }];
    
    [LiveRTMManager onAudienceLeaveRoomWithBlock:^(LiveUserModel *userModel, NSString * audienceCount) {
        if (wself) {
            [wself removeUser:userModel audienceCount:[audienceCount integerValue]];
        }
    }];
    
    [LiveRTMManager onFinishLiveWithBlock:^(NSString *roomID, NSString *type) {
        if (wself) {
            [wself receivedLiveEnd:NO type:type];
        }
    }];
    
    [LiveRTMManager onLinkmicStatusWithBlock:^(LiveInteractStatus status) {
        if (wself) {
            [wself receivedRoomStatus:status];
        }
    }];
    
    [LiveRTMManager onAudienceLinkmicJoinWithBlock:^(NSString *rtcRoomID, NSString *uid, NSArray<LiveUserModel *> *userList) {
        [wself receivedAddGuestsJoin:userList];
    }];
    
    [LiveRTMManager onAudienceLinkmicLeaveWithBlock:^(NSString *rtcRoomID, NSString *uid, NSArray<LiveUserModel *> *userList) {
        [wself receivedAddGuestsRemoveWithUser:uid userList:userList];
    }];
    
    [LiveRTMManager onAudienceLinkmicFinishWithBlock:^(NSString * _Nonnull rtcRoomID) {
        [wself receivedAddGuestsEnd];
    }];
    
    [LiveRTMManager onAnchorLinkmicFinishWithBlock:^(NSString * _Nonnull rtcRoomID) {
        [wself receivedCoHostEnd];
    }];

    [LiveRTMManager onMediaChangeWithBlock:^(NSString * _Nonnull rtcRoomID,
                                                    NSString * _Nonnull uid,
                                                    NSString * _Nonnull operatorUid,
                                                    NSInteger camera,
                                                    NSInteger mic) {
        if (wself) {
            BOOL cameraBool = camera == 1 ? YES : NO;
            BOOL micBool = mic == 1 ? YES : NO;
            [wself receivedAddGuestsMediaChangeWithUser:uid
                                            operatorUid:operatorUid
                                                 camera:cameraBool
                                                    mic:micBool];
        }
    }];
    
    [LiveRTMManager onMessageSendWithBlock:^(LiveUserModel * _Nonnull userModel, NSString * _Nonnull message) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
            [wself receivedIMMessage:message sendUserModel:userModel];
        }));
    }];
    
    // Single Point Notification message
    [LiveRTMManager onAudienceLinkmicInviteWithBlock:^(LiveUserModel *inviter, NSString *linkerID, NSString *extra) {
        [wself receivedAddGuestsInviteWithUser:inviter
                                      linkerID:linkerID
                                         extra:extra];
    }];
    
    [LiveRTMManager onAudienceLinkmicApplyWithBlock:^(LiveUserModel *applicant, NSString *linkerID, NSString *extra) {
        [wself receivedAddGuestsApplyWithUser:applicant
                                     linkerID:linkerID
                                        extra:extra];
    }];
    
    [LiveRTMManager onAudienceLinkmicReplyWithBlock:^(LiveUserModel *invitee, NSString * linkerID, LiveInviteReply replyType, NSString *rtcRoomID, NSString *rtcToken, NSArray<LiveUserModel *> *userList) {
        switch (replyType) {
            case LiveInviteReplyPermitted:
                [wself receivedAddGuestsSucceedWithUser:invitee
                                               linkerID:linkerID
                                              rtcRoomID:rtcRoomID
                                               rtcToken:rtcToken];
                [wself receivedAddGuestsJoin:userList];
                break;
            case LiveInviteReplyForbade:
                [wself receivedAddGuestsRefuseWithUser:invitee];
                break;
        }
    }];
    
    [LiveRTMManager onAudienceLinkmicPermitWithBlock:^(NSString *linkerID, LiveInviteReply permitType, NSString *rtcRoomID, NSString *rtcToken, NSArray<LiveUserModel *> *userList) {
        switch (permitType) {
            case LiveInviteReplyPermitted:
                [wself receivedAddGuestsSucceedWithUser:nil
                                               linkerID:linkerID
                                              rtcRoomID:rtcRoomID
                                               rtcToken:rtcToken];
                [wself receivedAddGuestsJoin:userList];
                break;
            case LiveInviteReplyForbade:
                [wself receivedAddGuestsRefuseWithUser:nil];
                break;
        }
    }];
    
    [LiveRTMManager onAudienceLinkmicKickWithBlock:^(NSString * _Nonnull linkerID,
                                                            NSString * _Nonnull rtcRoomID,
                                                            NSString * _Nonnull uid) {
        [wself receivedAddGuestsEnd];
    }];
    
    [LiveRTMManager onManageGuestMediaWithBlock:^(NSString * _Nonnull guestRoomID,
                                                         NSString * _Nonnull guestUserID,
                                                         NSInteger camera,
                                                         NSInteger mic) {
        if (wself) {
            [wself receivedAddGuestsManageGuestMedia:guestUserID
                                              camera:camera
                                                 mic:mic];
        }
    }];
    
    [LiveRTMManager onAnchorLinkmicInviteWithBlock:^(LiveUserModel *inviter, NSString *linkerID, NSString *extra) {
        [wself receivedCoHostInviteWithUser:inviter
                                   linkerID:linkerID
                                      extra:extra];
    }];
    
    [LiveRTMManager onAnchorLinkmicReplyWithBlock:^(LiveUserModel * _Nonnull invitee,
                                                           NSString * _Nonnull linkerID,
                                                           LiveInviteReply replyType,
                                                           NSString * _Nonnull rtcRoomID,
                                                           NSString * _Nonnull rtcToken,
                                                           NSArray<LiveUserModel *> * _Nonnull userList) {
        switch (replyType) {
            case LiveInviteReplyPermitted:
                [wself receivedCoHostSucceedWithUser:invitee
                                            linkerID:linkerID];
                [wself receivedCoHostJoin:userList
                        otherAnchorRoomId:rtcRoomID
                         otherAnchorToken:rtcToken];
                break;
            case LiveInviteReplyForbade:
                [wself receivedCoHostRefuseWithUser:invitee];
                break;
        }
    }];
}

@end
