//
//  LiveNoStreamingView.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/18.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveNoStreamingView.h"
#import "LiveAvatarCompoments.h"
#import "LiveStateIconView.h"

@interface LiveNoStreamingView ()

@property (nonatomic, strong) UIView *bgMaskView;
@property (nonatomic, strong) LiveAvatarCompoments *avatarCompoments;
@property (nonatomic, assign) BOOL hasAddItemLayer;

@end

@implementation LiveNoStreamingView

- (instancetype)init {
    self = [super init];
    if (self) {
        [self addSubview:self.bgMaskView];
        [self.bgMaskView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.edges.equalTo(self);
        }];

        [self addSubview:self.avatarCompoments];
        [self.avatarCompoments mas_makeConstraints:^(MASConstraintMaker *make) {
          make.width.height.mas_equalTo(160);
          make.center.equalTo(self);
        }];
        self.avatarCompoments.text = @"B";
    }
    return self;
}

- (void)setUserName:(NSString *)userName {
    self.avatarCompoments.text = userName;
}

- (void)layoutSubviews {
    [super layoutSubviews];

    if (_hasAddItemLayer == NO) {
        [self addBgGradientLayer];
        _hasAddItemLayer = YES;
    }
}

- (void)addBgGradientLayer {
    UIColor *startColor = [UIColor colorFromHexString:@"#30394A"];
    UIColor *endColor = [UIColor colorFromRGBHexString:@"#1D2129"];
    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
    gradientLayer.frame = self.bgMaskView.bounds;
    gradientLayer.colors = @[ (__bridge id)[startColor colorWithAlphaComponent:1.0].CGColor,
                              (__bridge id)[endColor colorWithAlphaComponent:1.0].CGColor ];
    gradientLayer.startPoint = CGPointMake(.0, .0);
    gradientLayer.endPoint = CGPointMake(1.0, 1.0);
    [self.bgMaskView.layer addSublayer:gradientLayer];
}

- (LiveAvatarCompoments *)avatarCompoments {
    if (!_avatarCompoments) {
        _avatarCompoments = [[LiveAvatarCompoments alloc] init];
        _avatarCompoments.layer.cornerRadius = 80;
        _avatarCompoments.layer.masksToBounds = YES;
        _avatarCompoments.fontSize = 80;
    }
    return _avatarCompoments;
}

- (UIView *)bgMaskView {
    if (!_bgMaskView) {
        _bgMaskView = [[UIView alloc] init];
    }
    return _bgMaskView;
}

@end
