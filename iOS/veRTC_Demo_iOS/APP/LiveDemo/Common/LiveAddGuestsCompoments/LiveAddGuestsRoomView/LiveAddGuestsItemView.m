//
//  LiveAddGuestsItemView.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveAddGuestsItemView.h"
#import "LiveAvatarCompoments.h"
#import "LiveStateIconView.h"

@interface LiveAddGuestsItemView ()

@property (nonatomic, strong) BaseButton *maskButton;
@property (nonatomic, strong) LiveStateIconView *netQualityView;
@property (nonatomic, strong) UIImageView *micImageView;
@property (nonatomic, strong) UILabel *userNameLabel;
@property (nonatomic, strong) LiveAvatarCompoments *avatarCompoments;
@property (nonatomic, strong) UIView *renderView;

@end

@implementation LiveAddGuestsItemView

- (instancetype)init {
    self = [super init];
    if (self) {
        self.layer.cornerRadius = 4;
        self.layer.maskedCorners = YES;
        self.backgroundColor = [UIColor colorFromHexString:@"#1D2129"];
        
        [self addSubview:self.renderView];
        [self.renderView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
        
        [self addSubview:self.netQualityView];
        [self.netQualityView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.height.mas_equalTo(12);
            make.left.mas_equalTo(2);
            make.top.mas_equalTo(3);
        }];
        
        [self addSubview:self.micImageView];
        [self.micImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(12, 12));
            make.left.mas_equalTo(2);
            make.bottom.mas_equalTo(-2);
        }];
        
        [self addSubview:self.userNameLabel];
        [self.userNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(16);
            make.bottom.mas_equalTo(-2);
            make.right.mas_lessThanOrEqualTo(self.mas_right).offset(-2);
        }];
        
        [self addSubview:self.avatarCompoments];
        [self.avatarCompoments mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(32, 32));
            make.center.equalTo(self);
        }];
        
        [self addSubview:self.maskButton];
        [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
    }
    return self;
}

- (void)setUserModel:(LiveUserModel *)userModel {
    _userModel = userModel;
    
    [[LiveRTCManager shareRtc] bingCanvasViewToUid:userModel.uid];
    UIView *rtcStreamView = [[LiveRTCManager shareRtc] getStreamViewWithUid:userModel.uid];
    if (userModel.camera) {
        rtcStreamView.backgroundColor = [UIColor clearColor];
        [self.renderView addSubview:rtcStreamView];
        [rtcStreamView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.renderView);
        }];
        rtcStreamView.hidden = NO;
        self.avatarCompoments.hidden = YES;
    } else {
        rtcStreamView.hidden = YES;
        self.avatarCompoments.hidden = NO;
    }
    self.avatarCompoments.text = userModel.name;
    self.userNameLabel.text = userModel.name;
    self.micImageView.hidden = userModel.mic;
    [self.userNameLabel mas_updateConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.micImageView.hidden ? 2 : 16);
    }];
    
    if ([userModel.uid isEqualToString:[LocalUserComponents userModel].uid]) {
        [[LiveRTCManager shareRtc] enableLocalVideo:userModel.camera];
        [[LiveRTCManager shareRtc] enableLocalAudio:userModel.mic];
    }
}

- (void)maskButtonAction {
    if (self.clickBlock) {
        self.clickBlock(self.userModel);
    }
}

- (void)updateNetworkQuality:(LiveNetworkQualityStatus)status {
    if (status == LiveNetworkQualityStatusGood) {
        [self.netQualityView updateState:LiveIconStateNetQuality];
    } else if (status == LiveNetworkQualityStatusNone) {
        [self.netQualityView updateState:LiveIconStateHidden];
    } else {
        [self.netQualityView updateState:LiveIconStateNetQualityBad];
    }
}

#pragma mark - getter

- (BaseButton *)maskButton {
    if (!_maskButton) {
        _maskButton = [[BaseButton alloc] init];
        _maskButton.backgroundColor = [UIColor clearColor];
        [_maskButton addTarget:self action:@selector(maskButtonAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _maskButton;
}

- (LiveStateIconView *)netQualityView {
    if (!_netQualityView) {
        _netQualityView = [[LiveStateIconView alloc] initWithState:LiveIconStateHidden];
    }
    return _netQualityView;
}

- (UIImageView *)micImageView {
    if (!_micImageView) {
        _micImageView = [[UIImageView alloc] init];
        _micImageView.image = [UIImage imageNamed:@"InteractiveLive_mic_icon" bundleName:HomeBundleName];
    }
    return _micImageView;
}

- (UILabel *)userNameLabel {
    if (!_userNameLabel) {
        _userNameLabel = [[UILabel alloc] init];
        _userNameLabel.textColor = [UIColor whiteColor];
        _userNameLabel.font = [UIFont systemFontOfSize:11];
    }
    return _userNameLabel;
}

- (LiveAvatarCompoments *)avatarCompoments {
    if (!_avatarCompoments) {
        _avatarCompoments = [[LiveAvatarCompoments alloc] init];
        _avatarCompoments.layer.cornerRadius = 16;
        _avatarCompoments.layer.masksToBounds = YES;
        _avatarCompoments.fontSize = 16;
        _avatarCompoments.hidden = YES;
    }
    return _avatarCompoments;
}

- (UIView *)renderView {
    if (!_renderView) {
        _renderView = [[UIView alloc] init];
    }
    return _renderView;
}

@end
