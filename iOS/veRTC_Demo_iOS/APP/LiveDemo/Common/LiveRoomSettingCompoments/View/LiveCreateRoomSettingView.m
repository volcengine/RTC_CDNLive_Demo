//
//  LiveCreateRoomSettingView.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/24.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveCreateRoomSettingView.h"
#import "LiveSettingBitrateView.h"
#import "LiveSettingSingleSelectView.h"

@interface LiveCreateRoomSettingView ()
@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) UILabel *mixStreamLabel;
@property (nonatomic, strong) UISwitch *mixStreamSwitch;

@property (nonatomic, strong) LiveSettingSingleSelectView *fpsSelectView;
@property (nonatomic, strong) LiveSettingSingleSelectView *resolutoinSelectView;
@property (nonatomic, strong) LiveSettingBitrateView *bitrateSelectView;

@end

@implementation LiveCreateRoomSettingView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];

        [self addSubview:self.titleLabel];
        [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerX.equalTo(self);
          make.top.mas_equalTo(16);
        }];

        [self addSubview:self.mixStreamLabel];
        [self.mixStreamLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.mas_equalTo(76);
          make.left.mas_equalTo(16);
        }];

        [self addSubview:self.mixStreamSwitch];
        [self.mixStreamSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerY.equalTo(self.mixStreamLabel);
          make.right.mas_equalTo(-16);
        }];

        [self addSubview:self.fpsSelectView];
        [self.fpsSelectView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.right.equalTo(self);
          make.top.equalTo(self.mixStreamLabel.mas_bottom).offset(24);
          make.height.mas_equalTo(60);
        }];

        __weak __typeof(self) wself = self;
        [self.fpsSelectView setItemChangeBlock:^(NSInteger index) {
          [wself fpsDidChanged:index];
        }];

        [self addSubview:self.resolutoinSelectView];
        [self.resolutoinSelectView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.right.height.equalTo(self.fpsSelectView);
          make.top.equalTo(self.fpsSelectView.mas_bottom);
        }];

        [self.resolutoinSelectView setItemChangeBlock:^(NSInteger index) {
          [wself resolutionDidChanged:index];
        }];

        [self addSubview:self.bitrateSelectView];
        [self.bitrateSelectView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.right.equalTo(self.fpsSelectView);
          make.top.equalTo(self.resolutoinSelectView.mas_bottom).offset(20);
          make.height.mas_equalTo(60);
        }];

        [self.bitrateSelectView setBitrateDidChangedBlock:^(NSInteger bitrate) {
          [wself bitrateDidChanged:bitrate];
        }];
    }
    return self;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
}

- (void)setVideoConfig:(LiveSettingVideoConfig *)videoConfig {
    _videoConfig = videoConfig;
    self.mixStreamSwitch.on = videoConfig.allowMixOnClientAndCloud;
    [self.fpsSelectView setSelectedIndex:videoConfig.fpsType];
    [self.resolutoinSelectView setSelectedIndex:videoConfig.resolutionType];

    self.bitrateSelectView.maxBitrate = videoConfig.maxBitrate;
    self.bitrateSelectView.minBitrate = videoConfig.minBitrate;
    self.bitrateSelectView.bitrate = videoConfig.bitrate;
}

- (void)mixStreamSwitchValueChanged:(UISwitch *)sender {
    self.videoConfig.allowMixOnClientAndCloud = sender.isOn;
    if ([self.delegate respondsToSelector:@selector(liveCreateRoomSettingView:didChangeMixStreamState:)]) {
        [self.delegate liveCreateRoomSettingView:self didChangeMixStreamState:sender.isOn];
    }
}

- (void)fpsDidChanged:(NSInteger)index {
    self.videoConfig.fpsType = index;

    if ([self.delegate respondsToSelector:@selector(liveCreateRoomSettingView:didChangefpsType:)]) {
        [self.delegate liveCreateRoomSettingView:self didChangefpsType:index];
    }
}

- (void)resolutionDidChanged:(NSInteger)index {
    self.videoConfig.resolutionType = index;
    self.bitrateSelectView.maxBitrate = self.videoConfig.maxBitrate;
    self.bitrateSelectView.minBitrate = self.videoConfig.minBitrate;
    self.bitrateSelectView.bitrate = self.videoConfig.bitrate;

    if ([self.delegate respondsToSelector:@selector(liveCreateRoomSettingView:didChangeResolution:)]) {
        [self.delegate liveCreateRoomSettingView:self didChangeResolution:index];
    }
    if ([self.delegate respondsToSelector:@selector(liveCreateRoomSettingView:didChangeBitrate:)]) {
        [self.delegate liveCreateRoomSettingView:self didChangeBitrate:self.videoConfig.bitrate];
    }
}

- (void)bitrateDidChanged:(NSInteger)bitrate {
    self.videoConfig.bitrate = bitrate;
    if ([self.delegate respondsToSelector:@selector(liveCreateRoomSettingView:didChangeBitrate:)]) {
        [self.delegate liveCreateRoomSettingView:self didChangeBitrate:bitrate];
    }
}

#pragma mark - getter

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = @"设置";
        _titleLabel.font = [UIFont systemFontOfSize:16];
        _titleLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
    }
    return _titleLabel;
}

- (UILabel *)mixStreamLabel {
    if (!_mixStreamLabel) {
        _mixStreamLabel = [[UILabel alloc] init];
        _mixStreamLabel = [[UILabel alloc] init];
        _mixStreamLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        _mixStreamLabel.font = [UIFont systemFontOfSize:16];
        _mixStreamLabel.text = @"端云一体合流";
    }
    return _mixStreamLabel;
}

- (UISwitch *)mixStreamSwitch {
    if (!_mixStreamSwitch) {
        _mixStreamSwitch = [[UISwitch alloc] init];
        _mixStreamSwitch.onTintColor = [UIColor colorFromHexString:@"#165DFF"];
        [_mixStreamSwitch addTarget:self action:@selector(mixStreamSwitchValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _mixStreamSwitch;
}

- (LiveSettingSingleSelectView *)fpsSelectView {
    if (!_fpsSelectView) {
        _fpsSelectView = [[LiveSettingSingleSelectView alloc] initWithTitle:@"帧率" optionArray:@[ @"15", @"20" ]];
    }
    return _fpsSelectView;
}

- (LiveSettingSingleSelectView *)resolutoinSelectView {
    if (!_resolutoinSelectView) {
        _resolutoinSelectView = [[LiveSettingSingleSelectView alloc] initWithTitle:@"分辨率" optionArray:@[@"540p", @"720p", @"1080p" ]];
    }
    return _resolutoinSelectView;
}

- (LiveSettingBitrateView *)bitrateSelectView {
    if (!_bitrateSelectView) {
        _bitrateSelectView = [[LiveSettingBitrateView alloc] init];
    }
    return _bitrateSelectView;
}

@end
