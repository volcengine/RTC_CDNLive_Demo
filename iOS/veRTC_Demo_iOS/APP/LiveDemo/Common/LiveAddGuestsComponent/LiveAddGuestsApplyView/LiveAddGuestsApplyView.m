//
//  LiveAddGuestsApplyView.m
//  veRTC_Demo
//
//  Created by on 2021/10/20.
//  
//

#import "LiveAddGuestsApplyView.h"

@interface LiveAddGuestsApplyView ()

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) BaseButton *applyButton;
@property (nonatomic, strong) UILabel *tipLabel;

@end

@implementation LiveAddGuestsApplyView

- (instancetype)init {
    self = [super init];
    if (self) {
        [self addSubview:self.titleLabel];
        [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.mas_equalTo(16);
          make.centerX.equalTo(self);
        }];

        [self addSubview:self.applyButton];
        [self.applyButton mas_makeConstraints:^(MASConstraintMaker *make) {
          make.size.mas_equalTo(CGSizeMake(132, 44));
          make.centerX.equalTo(self);
          make.top.equalTo(self.titleLabel.mas_bottom).offset(24);
        }];

        [self addSubview:self.tipLabel];
        [self.tipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerX.equalTo(self);
          make.top.equalTo(self.applyButton.mas_bottom).offset(8);
        }];
    }
    return self;
}

- (void)setUserModel:(LiveUserModel *)userModel {
    _userModel = userModel;

    if (userModel.status == LiveInteractStatusApplying) {
        // Inviting
        self.tipLabel.hidden = NO;
        self.applyButton.alpha = 0.34;
        self.applyButton.userInteractionEnabled = NO;
        [self.applyButton setTitle:@"申请中" forState:UIControlStateNormal];
    } else {
        // None
        self.tipLabel.hidden = YES;
        self.applyButton.alpha = 1;
        self.applyButton.userInteractionEnabled = YES;
        [self.applyButton setTitle:@"发起申请" forState:UIControlStateNormal];
    }
}

- (void)applyButtonAction {
    if (self.clickApplyBlock) {
        self.clickApplyBlock();
    }
}

#pragma mark - Publish Action

- (void)updateApplying {
    self.userModel.status = LiveInteractStatusApplying;
    [self setUserModel:self.userModel];
}

- (void)resetStatus {
    self.userModel.status = LiveInteractStatusOther;
    [self setUserModel:self.userModel];
}

#pragma mark - getter

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = @"与主播连线";
        _titleLabel.font = [UIFont systemFontOfSize:16];
        _titleLabel.textColor = [UIColor whiteColor];
    }
    return _titleLabel;
}

- (UILabel *)tipLabel {
    if (!_tipLabel) {
        _tipLabel = [[UILabel alloc] init];
        _tipLabel.text = @"等待主播通过";
        _tipLabel.font = [UIFont systemFontOfSize:12];
        _tipLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        _tipLabel.hidden = YES;
    }
    return _tipLabel;
}

- (BaseButton *)applyButton {
    if (!_applyButton) {
        _applyButton = [[BaseButton alloc] init];
        _applyButton.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [_applyButton setTitle:@"发起申请" forState:UIControlStateNormal];
        [_applyButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _applyButton.titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        [_applyButton addTarget:self action:@selector(applyButtonAction) forControlEvents:UIControlEventTouchUpInside];
        _applyButton.layer.cornerRadius = 22;
        _applyButton.layer.masksToBounds = YES;
    }
    return _applyButton;
}

@end
