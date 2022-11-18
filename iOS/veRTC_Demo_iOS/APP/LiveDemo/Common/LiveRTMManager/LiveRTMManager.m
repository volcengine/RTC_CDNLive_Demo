//
//  LiveRTMManager.m
//  SceneRTCDemo
//
//  Created by on 2021/3/16.
//

#import "LiveRTMManager.h"
#import "LiveRTCManager.h"
#import "JoinRTSParams.h"
#import "Core.h"

@implementation LiveRTMManager

#pragma mark - Host Live API

+ (void)liveCreateLive:(NSString *)userName
                 block:(void (^)(LiveRoomInfoModel *roomInfoModel,
                                 LiveUserModel *hostUserModel,
                                 NSString *pushUrl,
                                 RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(userName)) {
        dic = @{@"user_name" : userName ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveCreateLive"with:dic block:^(RTMACKModel * _Nonnull ackModel) {
        LiveRoomInfoModel *roomModel = nil;
        LiveUserModel *hostUserModel = nil;
        NSString *pushUrl = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            roomModel = [LiveRoomInfoModel yy_modelWithJSON:ackModel.response[@"live_room_info"]];
            roomModel.rtmToken = [NSString stringWithFormat:@"%@", ackModel.response[@"rtm_token"]];
            roomModel.rtcToken = [NSString stringWithFormat:@"%@", ackModel.response[@"rtc_token"]];
            roomModel.rtcRoomId = [NSString stringWithFormat:@"%@", ackModel.response[@"rtc_room_id"]];
            hostUserModel = [LiveUserModel yy_modelWithJSON:ackModel.response[@"user_info"]];
            pushUrl = [NSString stringWithFormat:@"%@", ackModel.response[@"stream_push_url"]];
        }
        if (block) {
            block(roomModel, hostUserModel, pushUrl, ackModel);
        }
    }];
}

+ (void)liveStartLive:(NSString *)roomID
                block:(void (^)(LiveUserModel *hostUserModel,
                                RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveStartLive"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        LiveUserModel *userModel = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            userModel = [LiveUserModel yy_modelWithJSON:ackModel.response[@"user_info"]];
        }
        if (block) {
            block(userModel, ackModel);
        }
    }];
}

+ (void)liveGetActiveAnchorList:(NSString *)roomID
                          block:(void (^)(NSArray<LiveUserModel *> *, RTMACKModel *))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveGetActiveAnchorList"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSMutableArray *dataList = [[NSMutableArray alloc] init];
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            NSArray *list = ackModel.response[@"anchor_list"];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
            }
        }
        if (block) {
            block([dataList copy], ackModel);
        }
    }];
}

+ (void)liveGetAudienceList:(NSString *)roomID
                      block:(void (^)(NSArray<LiveUserModel *> *userList,
                                      RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveGetAudienceList"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSMutableArray *dataList = [[NSMutableArray alloc] init];
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            NSArray *list = ackModel.response[@"audience_list"];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    NSDictionary *userDic = list[i];
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:userDic];
                    [dataList addObject:userModel];
                }
            }
        }
        if (block) {
            block([dataList copy], ackModel);
        }
    }];
}

+ (void)liveManageGuestMedia:(NSString *)roomID
                 guestRoomID:(NSString *)guestRoomID
                 guestUserID:(NSString *)guestUserID
                         mic:(NSInteger)mic
                      camera:(NSInteger)camera
                       block:(void (^)(RTMACKModel * _Nonnull))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"host_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"host_user_id"];
    }
    if (NOEmptyStr(guestRoomID)) {
        [dic setValue:guestRoomID forKey:@"guest_room_id"];
    }
    if (NOEmptyStr(guestUserID)) {
        [dic setValue:guestUserID forKey:@"guest_user_id"];
    }
    [dic setValue:@(mic) forKey:@"mic"];
    [dic setValue:@(camera) forKey:@"camera"];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveManageGuestMedia"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)liveFinishLive:(NSString *)roomID
                 block:(void (^)(RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveFinishLive"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

#pragma mark Linkmic API

+ (void)liveAudienceLinkmicInvite:(NSString *)roomID
                   audienceRoomID:(NSString *)audienceRoomID
                   audienceUserID:(NSString *)audienceUserID
                            extra:(NSString *)extra
                            block:(void (^)(NSString * _Nullable, RTMACKModel * _Nonnull))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"host_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"host_user_id"];
    }
    if (NOEmptyStr(audienceRoomID)) {
        [dic setValue:audienceRoomID forKey:@"audience_room_id"];
    }
    if (NOEmptyStr(audienceUserID)) {
        [dic setValue:audienceUserID forKey:@"audience_user_id"];
    }
    if (NOEmptyStr(extra)) {
        [dic setValue:extra forKey:@"extra"];
    }
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicInvite"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *linkerID = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            linkerID = ackModel.response[@"linker_id"];
        }
        if (block) {
            block(linkerID, ackModel);
        }
    }];
}

+ (void)liveAudienceLinkmicPermit:(NSString *)roomID
                   audienceRoomID:(NSString *)audienceRoomID
                   audienceUserID:(NSString *)audienceUserID
                         linkerID:(NSString *)linkerID
                       permitType:(LiveInviteReply)permitType
                            block:(void (^)(NSString *,
                                            NSString *,
                                            NSArray<LiveUserModel *> *,
                                            RTMACKModel *))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"host_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"host_user_id"];
    }
    if (NOEmptyStr(audienceRoomID)) {
        [dic setValue:audienceRoomID forKey:@"audience_room_id"];
    }
    if (NOEmptyStr(audienceUserID)) {
        [dic setValue:audienceUserID forKey:@"audience_user_id"];
    }
    if (NOEmptyStr(linkerID)) {
        [dic setValue:linkerID forKey:@"linker_id"];
    }
    [dic setValue:@(permitType) forKey:@"permit_type"];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicPermit"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *rtcRoomID = nil;
        NSString *rtcToken = nil;
        NSArray *rtcUserList = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            rtcRoomID = ackModel.response[@"rtc_room_id"];
            rtcToken = ackModel.response[@"rtc_token"];
            NSArray *list = ackModel.response[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(rtcRoomID, rtcToken, rtcUserList, ackModel);
        }
    }];
}

+ (void)liveAudienceLinkmicKick:(NSString *)roomID
                 audienceRoomID:(NSString *)audienceRoomID
                 audienceUserID:(NSString *)audienceUserID
                          block:(void (^)(RTMACKModel * _Nonnull))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"host_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"host_user_id"];
    }
    if (NOEmptyStr(audienceRoomID)) {
        [dic setValue:audienceRoomID forKey:@"audience_room_id"];
    }
    if (NOEmptyStr(audienceUserID)) {
        [dic setValue:audienceUserID forKey:@"audience_user_id"];
    }
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicKick"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)liveAudienceLinkmicFinish:(NSString *)roomID block:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id": roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicFinish"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)liveAnchorLinkmicInvite:(NSString *)roomID
                  inviteeRoomID:(NSString *)inviteeRoomID
                  inviteeUserID:(NSString *)inviteeUserID
                          extra:(NSString *)extra
                          block:(void (^)(NSString * _Nullable, RTMACKModel * _Nonnull))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"inviter_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"inviter_user_id"];
    }
    if (NOEmptyStr(inviteeRoomID)) {
        [dic setValue:inviteeRoomID forKey:@"invitee_room_id"];
    }
    if (NOEmptyStr(inviteeUserID)) {
        [dic setValue:inviteeUserID forKey:@"invitee_user_id"];
    }
    if (NOEmptyStr(extra)) {
        [dic setValue:extra forKey:@"extra"];
    }
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAnchorLinkmicInvite"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *linkerID = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            linkerID = ackModel.response[@"linker_id"];
        }
        if (block) {
            block(linkerID, ackModel);
        }
    }];
}

+ (void)liveAnchorLinkmicReply:(NSString *)roomID
                 inviterRoomID:(NSString *)inviterRoomID
                 inviterUserID:(NSString *)inviterUserID
                      linkerID:(NSString *)linkerID
                     replyType:(LiveInviteReply)replyType
                         block:(void (^)(NSString *,
                                         NSString *,
                                         NSArray<LiveUserModel *> *,
                                         RTMACKModel *))block {
    NSMutableDictionary *dic = [[JoinRTSParams addTokenToParams:nil] mutableCopy];
    if (NOEmptyStr([LocalUserComponent userModel].loginToken)) {
        [dic setValue:[LocalUserComponent userModel].loginToken forKey:@"login_token"];
    }
    if (NOEmptyStr(roomID)) {
        [dic setValue:roomID forKey:@"invitee_room_id"];
    }
    if (NOEmptyStr([LocalUserComponent userModel].uid)) {
        [dic setValue:[LocalUserComponent userModel].uid forKey:@"invitee_user_id"];
    }
    if (NOEmptyStr(inviterRoomID)) {
        [dic setValue:inviterRoomID forKey:@"inviter_room_id"];
    }
    if (NOEmptyStr(inviterUserID)) {
        [dic setValue:inviterUserID forKey:@"inviter_user_id"];
    }
    if (NOEmptyStr(linkerID)) {
        [dic setValue:linkerID forKey:@"linker_id"];
    }
    [dic setValue:@(replyType) forKey:@"reply_type"];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAnchorLinkmicReply"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *rtcRoomID = nil;
        NSString *rtcToken = nil;
        NSArray *rtcUserList = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            rtcRoomID = ackModel.response[@"rtc_room_id"];
            rtcToken = ackModel.response[@"rtc_token"];
            NSArray *list = ackModel.response[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(rtcRoomID, rtcToken, rtcUserList, ackModel);
        }
    }];
}

+ (void)liveAnchorLinkmicFinish:(NSString *)roomID
                       linkerID:(NSString *)linkerID
                          block:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id": roomID ?: @"",
                @"linker_id": linkerID ?: @""
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAnchorLinkmicFinish"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

#pragma mark - Audience Live API

+ (void)liveJoinLiveRoom:(NSString *)roomID
                   block:(void (^)(LiveRoomInfoModel *roomModel,
                                   LiveUserModel *userModel,
                                   RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @"",
                @"user_name" : [LocalUserComponent userModel].name};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveJoinLiveRoom"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        LiveRoomInfoModel *roomModel = nil;
        LiveUserModel *userModel = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            roomModel = [LiveRoomInfoModel yy_modelWithJSON:ackModel.response[@"live_room_info"]];
            roomModel.rtmToken = [NSString stringWithFormat:@"%@", ackModel.response[@"rtm_token"]];
            roomModel.hostUserModel = [LiveUserModel yy_modelWithJSON:ackModel.response[@"host_user_info"]];
            userModel = [LiveUserModel yy_modelWithJSON:ackModel.response[@"user_info"]];
        }
        if (block) {
            block(roomModel, userModel, ackModel);
        }
    }];
}

+ (void)liveLeaveLiveRoom:(NSString *)roomID
                    block:(void (^)(RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveLeaveLiveRoom"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
    }];
}

#pragma mark Linkmic API

+ (void)liveAudienceLinkmicApply:(NSString *)roomID
                           block:(void (^)(NSString * _Nullable, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id" : roomID ?: @""};
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicApply"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        NSString *linkerID = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            linkerID = ackModel.response[@"linker_id"];
        }
        if (block) {
            block(linkerID, ackModel);
        }
    }];
}

+ (void)liveAudienceLinkmicReply:(NSString *)roomID
                        linkerID:(NSString *)linkerID
                       replyType:(LiveInviteReply)replyType
                           block:(void (^)(NSString * _Nullable, NSString * _Nullable, NSArray<LiveUserModel *> * _Nullable, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id": roomID ?: @"",
                @"linker_id": linkerID ?: @"",
                @"reply_type": @(replyType)
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicReply"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        NSString *rtcRoomID = nil;
        NSString *rtcToken = nil;
        NSArray *rtcUserList = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            rtcRoomID = ackModel.response[@"rtc_room_id"];
            rtcToken = ackModel.response[@"rtc_token"];
            NSArray *list = ackModel.response[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(rtcRoomID, rtcToken, rtcUserList, ackModel);
        }
    }];
}

+ (void)liveAudienceLinkmicLeave:(NSString *)roomID
                        linkerID:(NSString *)linkerID
                           block:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{@"room_id": roomID ?: @"",
                @"linker_id": linkerID ?: @""
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveAudienceLinkmicLeave"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

#pragma mark - Publish API

+ (void)liveUpdateResWithSize:(CGSize)videoSize
                       roomID:(NSString *)roomID
                        block:(void (^)(RTMACKModel * _Nonnull))block {
    if (IsEmptyStr(roomID)) {
        return;
    }
    NSDictionary *dic = @{
        @"width" : @(videoSize.width),
        @"height" : @(videoSize.height),
        @"room_id" : roomID ?: @"",
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveUpdateResolution"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)liveClearUserWithBlock:(void (^)(RTMACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveClearUser"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)liveGetActiveLiveRoomListWithBlock:(void (^)(NSArray<LiveRoomInfoModel *> *roomList,
                                                     RTMACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveGetActiveLiveRoomList"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        NSMutableArray *dataList = [[NSMutableArray alloc] init];
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            NSArray *list = ackModel.response[@"live_room_list"];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveRoomInfoModel *roomInfoModel = [LiveRoomInfoModel yy_modelWithJSON:list[i]];
                    [dataList addObject:roomInfoModel];
                }
            }
        }
        if (block) {
            block([dataList copy], ackModel);
        }
    }];
}

+ (void)liveUpdateMediaStatus:(NSString *)roomID
                          mic:(NSInteger)mic
                       camera:(NSInteger)camera
                        block:(void (^)(RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{
            @"room_id" : roomID ?: @"",
            @"mic" : @(mic),
            @"camera" : @(camera),
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    [[LiveRTCManager shareRtc] emitWithAck:@"liveUpdateMediaStatus"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
    }];
}

+ (void)sendIMMessage:(NSString *)message
                block:(void (^ __nullable)(RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(message)) {
        dic = @{
            @"message" : message ?: @"",
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[LiveRTCManager shareRtc] emitWithAck:@"liveSendMessage"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-liveReconnect %@|%@", [self class], dic, ackModel.response);
    }];
}

#pragma mark - Reconnect

+ (void)reconnect:(NSString *)roomID
            block:(void (^)(LiveReconnectModel * _Nullable, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID)) {
        dic = @{
            @"room_id" : roomID ?: @"",
        };
    }
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[LiveRTCManager shareRtc] emitWithAck:@"liveReconnect"
                                      with:dic
                                     block:^(RTMACKModel * _Nonnull ackModel) {
        
        LiveReconnectModel *reconnectModel = nil;
        if ([LiveRTMManager ackModelResponseClass:ackModel]) {
            reconnectModel = [LiveReconnectModel yy_modelWithJSON:ackModel.response[@"reconnect_info"]];
            LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:ackModel.response[@"user"]];
            reconnectModel.interactStatus = [ackModel.response[@"linkmic_status"] integerValue];
            reconnectModel.loginUserModel = userModel;
        }
        if (block) {
            block(reconnectModel, ackModel);
        }
        NSLog(@"[%@]-liveReconnect %@|%@", [self class], dic, ackModel.response);
    }];
}

#pragma mark - Global Notification message

+ (void)onAudienceJoinRoomWithBlock:(void (^)(LiveUserModel *userModel,
                                              NSString *audienceCount))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceJoinRoom"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        LiveUserModel *userModel = nil;
        NSString *audienceCount = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            NSString *uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_user_id"]];
            NSString *name = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_user_name"]];
            LiveUserModel *tempUserModel = [[LiveUserModel alloc] init];
            tempUserModel.uid = uid;
            tempUserModel.name = name;
            userModel = tempUserModel;
            audienceCount = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_count"]];
        }
        if (block) {
            block(userModel, audienceCount);
        }
        NSLog(@"[%@]-onAudienceJoinRoom \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLeaveRoomWithBlock:(void (^)(LiveUserModel *userModel,
                                               NSString *audienceCount))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLeaveRoom"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *userModel = nil;
        NSString *audienceCount = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            NSString *uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_user_id"]];
            NSString *name = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_user_name"]];
            LiveUserModel *tempUserModel = [[LiveUserModel alloc] init];
            tempUserModel.uid = uid;
            tempUserModel.name = name;
            userModel = tempUserModel;
            audienceCount = [NSString stringWithFormat:@"%@", noticeModel.data[@"audience_count"]];
        }
        if (block) {
            block(userModel, audienceCount);
        }
        NSLog(@"[%@]-onAudienceLeaveRoom \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onFinishLiveWithBlock:(void (^)(NSString *roomID, NSString *type))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnFinishLive"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        NSString *type = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
            type = [NSString stringWithFormat:@"%@", noticeModel.data[@"type"]];
        }
        if (block) {
            block(roomID, type);
        }
        NSLog(@"[%@]-onFinishLive \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onLinkmicStatusWithBlock:(void (^)(LiveInteractStatus))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnLinkmicStatus"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        LiveInteractStatus status = LiveInteractStatusOther;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            status = [noticeModel.data[@"linkmic_status"] integerValue];
        }
        if (block) {
            block(status);
        }
        NSLog(@"[%@]-onLinkmicStatus \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicJoinWithBlock:(void (^)(NSString *rtcRoomID,
                                                 NSString *uid,
                                                 NSArray<LiveUserModel *> *userList))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicJoin"
                                         block:^(RTMNoticeModel * _Nonnull
                                                         noticeModel) {
        
        NSMutableArray<LiveUserModel *> *userList = [[NSMutableArray alloc] init];
        NSString *rtcRoomID = @"";
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
            NSArray *list = noticeModel.data[@"user_list"];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [userList addObject:userModel];
                }
            }
        }
        if (block) {
            block(rtcRoomID, uid, userList);
        }
        NSLog(@"[%@]-onAudienceLinkmicJoin \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicLeaveWithBlock:(void (^)(NSString *rtcRoomID,
                                                  NSString *uid,
                                                  NSArray<LiveUserModel *> *userList))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicLeave"
                                         block:^(RTMNoticeModel * _Nonnull
                                                         noticeModel) {
        NSMutableArray<LiveUserModel *> *userList = [[NSMutableArray alloc] init];
        NSString *rtcRoomID = @"";
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
            NSArray *list = noticeModel.data[@"user_list"];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [userList addObject:userModel];
                }
            }
        }
        if (block) {
            block(rtcRoomID, uid, userList);
        }
        NSLog(@"[%@]-onAudienceLinkmicLeave \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicFinishWithBlock:(void (^)(NSString *rtcRoomID))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicFinish"
                                         block:^(RTMNoticeModel * _Nonnull
                                                         noticeModel) {
        
        NSString *rtcRoomID = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
        }
        if (block) {
            block(rtcRoomID);
        }
        NSLog(@"[%@]-onAudienceLinkmicFinish \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAnchorLinkmicFinishWithBlock:(void (^)(NSString * _Nonnull))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAnchorLinkmicFinish"
                                         block:^(RTMNoticeModel * _Nonnull
                                                         noticeModel) {
        
        NSString *rtcRoomID = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
        }
        if (block) {
            block(rtcRoomID);
        }
        NSLog(@"[%@]-onAnchorLinkmicFinish \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onMediaChangeWithBlock:(void (^)(NSString *rtcRoomID,
                                         NSString *uid,
                                         NSString *operatorUid,
                                         NSInteger camera,
                                         NSInteger mic))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnMediaChange"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *rtcRoomID = @"";
        NSString *uid = @"";
        NSInteger camera = -1;
        NSInteger mic = -1;
        NSString *operatorUid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
            operatorUid = [NSString stringWithFormat:@"%@", noticeModel.data[@"operator_user_id"]];
            camera = [noticeModel.data[@"camera"] integerValue];
            mic = [noticeModel.data[@"mic"] integerValue];
        }
        if (block) {
            block(rtcRoomID, uid, operatorUid, camera, mic);
        }
        NSLog(@"[%@]-onMediaChange \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onMessageSendWithBlock:(void (^)(LiveUserModel *userModel,
                                         NSString *message))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnMessageSend"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        NSString *message = @"";
        LiveUserModel *userModel = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            userModel = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"user"]];
            message = [NSString stringWithFormat:@"%@", noticeModel.data[@"message"]];
        }
        if (block) {
            block(userModel, message);
        }
        NSLog(@"[%@]-liveOnMessageSend \n %@", [self class], noticeModel.data);
    }];
}

#pragma mark - Single Point Notification message

+ (void)onAudienceLinkmicInviteWithBlock:(void (^)(LiveUserModel *,
                                                   NSString *,
                                                   NSString *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicInvite"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *inviter = nil;
        NSString *linkerID = @"";
        NSString *extra = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            inviter = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"inviter"]];
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            extra = [NSString stringWithFormat:@"%@", noticeModel.data[@"extra"]];
        }
        if (block) {
            block(inviter, linkerID, extra);
        }
        NSLog(@"[%@]-onAudienceLinkmicInvite \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicApplyWithBlock:(void (^)(LiveUserModel *, NSString *, NSString *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicApply"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *applicant = nil;
        NSString *linkerID = @"";
        NSString *extra = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            applicant = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"applicant"]];
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            extra = [NSString stringWithFormat:@"%@", noticeModel.data[@"extra"]];
        }
        if (block) {
            block(applicant, linkerID, extra);
        }
        NSLog(@"[%@]-onAudienceLinkmicApply \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicReplyWithBlock:(void (^)(LiveUserModel *,
                                                  NSString *,
                                                  LiveInviteReply,
                                                  NSString *,
                                                  NSString *,
                                                  NSArray<LiveUserModel *> *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicReply"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *invitee = nil;
        NSString *linkerID = @"";
        LiveInviteReply replyType = LiveInviteReplyForbade;
        NSString *rtcRoomID = @"";
        NSString *rtcToken = @"";
        NSArray *rtcUserList = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            invitee = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"invitee"]];
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            replyType = [noticeModel.data[@"reply_type"] integerValue];
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            rtcToken = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_token"]];
            NSArray *list = noticeModel.data[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(invitee, linkerID, replyType, rtcRoomID, rtcToken, rtcUserList);
        }
        NSLog(@"[%@]-onAudienceLinkmicReply \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicPermitWithBlock:(void (^)(NSString *, 
                                                   LiveInviteReply,
                                                   NSString *,
                                                   NSString *,
                                                   NSArray<LiveUserModel *> *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicPermit"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        NSString *linkerID = @"";
        LiveInviteReply permitType = LiveInviteReplyForbade;
        NSString *rtcRoomID = @"";
        NSString *rtcToken = @"";
        NSArray *rtcUserList = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            permitType = [noticeModel.data[@"permit_type"] integerValue];
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            rtcToken = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_token"]];
            NSArray *list = noticeModel.data[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(linkerID, permitType, rtcRoomID, rtcToken, rtcUserList);
        }
        NSLog(@"[%@]-onAudienceLinkmicPermit \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAudienceLinkmicKickWithBlock:(void (^)(NSString *, NSString *, NSString *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAudienceLinkmicKick"
                                         block:^(RTMNoticeModel * _Nonnull
                                                         noticeModel) {
        
        NSString *linkerID = @"";
        NSString *rtcRoomID = @"";
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            uid = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
        }
        if (block) {
            block(linkerID, rtcRoomID, uid);
        }
        NSLog(@"[%@]-onAudienceLinkmicKick \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAnchorLinkmicInviteWithBlock:(void (^)(LiveUserModel * _Nonnull, NSString * _Nonnull, NSString * _Nonnull))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAnchorLinkmicInvite"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *inviter = nil;
        NSString *linkerID = @"";
        NSString *extra = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            inviter = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"inviter"]];
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            extra = [NSString stringWithFormat:@"%@", noticeModel.data[@"extra"]];
        }
        if (block) {
            block(inviter, linkerID, extra);
        }
        NSLog(@"[%@]-onAnchorLinkmicInvite \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onAnchorLinkmicReplyWithBlock:(void (^)(LiveUserModel *,
                                                NSString *,
                                                LiveInviteReply,
                                                NSString *,
                                                NSString *,
                                                NSArray<LiveUserModel *> *))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnAnchorLinkmicReply"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        
        LiveUserModel *invitee = nil;
        NSString *linkerID = @"";
        LiveInviteReply replyType = LiveInviteReplyForbade;
        NSString *rtcRoomID = @"";
        NSString *rtcToken = @"";
        NSArray *rtcUserList = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            invitee = [LiveUserModel yy_modelWithJSON:noticeModel.data[@"invitee"]];
            linkerID = [NSString stringWithFormat:@"%@", noticeModel.data[@"linker_id"]];
            replyType = [noticeModel.data[@"reply_type"] integerValue];
            rtcRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_room_id"]];
            rtcToken = [NSString stringWithFormat:@"%@", noticeModel.data[@"rtc_token"]];
            NSArray *list = noticeModel.data[@"rtc_user_list"];
            NSMutableArray *dataList = [[NSMutableArray alloc] init];
            if (list && [list isKindOfClass:[NSArray class]]) {
                for (int i = 0; i < list.count; i++) {
                    LiveUserModel *userModel = [LiveUserModel yy_modelWithJSON:list[i]];
                    [dataList addObject:userModel];
                }
                rtcUserList = [dataList copy];
            }
        }
        if (block) {
            block(invitee, linkerID, replyType, rtcRoomID, rtcToken, rtcUserList);
        }
        NSLog(@"[%@]-onAnchorLinkmicReply \n %@", [self class], noticeModel.data);
    }];
}

+ (void)onManageGuestMediaWithBlock:(void (^)(NSString *, NSString *, NSInteger, NSInteger))block {
    [[LiveRTCManager shareRtc] onSceneListener:@"liveOnManageGuestMedia"
                                         block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *guestRoomID = @"";
        NSString *guestUserID = @"";
        NSInteger camera = -1;
        NSInteger mic = -1;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            guestRoomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"guest_room_id"]];
            guestUserID = [NSString stringWithFormat:@"%@", noticeModel.data[@"guest_user_id"]];
            camera = [noticeModel.data[@"camera"] integerValue];
            mic = [noticeModel.data[@"mic"] integerValue];
        }
        if (block) {
            block(guestRoomID, guestUserID, camera, mic);
        }
        NSLog(@"[%@]-liveOnManageGuestMedia \n %@", [self class], noticeModel.data);
    }];
}

#pragma mark - tool

+ (BOOL)ackModelResponseClass:(RTMACKModel *)ackModel {
    if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
        return YES;
    } else {
        return NO;
    }
}

@end
