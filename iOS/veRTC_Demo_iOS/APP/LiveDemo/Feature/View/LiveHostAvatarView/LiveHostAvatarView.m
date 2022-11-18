//
//  LiveHostAvatarView.m
//  veRTC_Demo
//
//  Created by on 2021/5/19.
//  
//

#import "LiveHostAvatarView.h"
#import "LiveAvatarComponent.h"

@interface LiveHostAvatarView ()

@property (nonatomic, strong) LiveAvatarComponent *avatarView;
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

- (LiveAvatarComponent *)avatarView {
    if (_avatarView == nil) {
        _avatarView = [[LiveAvatarComponent alloc] init];
        _avatarView.layer.masksToBounds = YES;
        _avatarView.layer.cornerRadius = 16;
        _avatarView.fontSize = 16;
    }
    return _avatarView;
}

@end
