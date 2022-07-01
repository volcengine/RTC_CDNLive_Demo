//
//  LiveHostAvatarView.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveHostAvatarView.h"
#import "LiveAvatarCompoments.h"

@interface LiveHostAvatarView ()

@property (nonatomic, strong) LiveAvatarCompoments *avatarView;
@property (nonatomic, strong) UILabel *titleLabel;

@end

@implementation LiveHostAvatarView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self addSubview:self.avatarView];
        [self.avatarView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.size.mas_equalTo(CGSizeMake(32, 32));
          make.left.mas_equalTo(2);
          make.centerY.equalTo(self);
        }];

        [self addSubview:self.titleLabel];
        [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerY.equalTo(self);
          make.left.equalTo(self.avatarView.mas_right).offset(8);
        }];

        [self mas_updateConstraints:^(MASConstraintMaker *make) {
          make.right.equalTo(self.titleLabel.mas_right).offset(8);
        }];
    }
    return self;
}

- (void)setHostName:(NSString *)hostName {
    _hostName = hostName;

    self.avatarView.text = hostName;
    self.titleLabel.text = hostName;
}

#pragma mark - getter

- (UILabel *)titleLabel {
    if (_titleLabel == nil) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.textColor = [UIColor whiteColor];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
    }
    return _titleLabel;
}

- (LiveAvatarCompoments *)avatarView {
    if (_avatarView == nil) {
        _avatarView = [[LiveAvatarCompoments alloc] init];
        _avatarView.layer.masksToBounds = YES;
        _avatarView.layer.cornerRadius = 16;
        _avatarView.fontSize = 16;
    }
    return _avatarView;
}

@end
