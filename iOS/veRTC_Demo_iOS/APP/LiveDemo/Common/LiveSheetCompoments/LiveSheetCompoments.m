//
//  LiveSheetCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/10/15.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveSheetCompoments.h"

@interface LiveSheetCompoments ()

@property (nonatomic, weak) UIView *contentView;
@property (nonatomic, strong) UIButton *maskButton;
@property (nonatomic, copy) NSArray *sheetModelList;

@end

@implementation LiveSheetCompoments

+ (LiveSheetCompoments *_Nullable)shareSheet {
    static LiveSheetCompoments *liveSheetCompoments = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      liveSheetCompoments = [[LiveSheetCompoments alloc] init];
    });
    return liveSheetCompoments;
}

- (instancetype)init {
    self = [super init];
    if (self) {
    }
    return self;
}

- (void)show:(NSArray<LiveSheetModel *> *)list {
    _sheetModelList = list;
    UIViewController *rootVC = [DeviceInforTool topViewController];

    [rootVC.view addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
      make.width.left.height.equalTo(rootVC.view);
      make.top.equalTo(rootVC.view).offset(SCREEN_HEIGHT);
    }];

    UIView *contentView = [[UIView alloc] init];
    contentView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    contentView.layer.cornerRadius = 4;
    contentView.layer.masksToBounds = YES;
    [self.maskButton addSubview:contentView];
    [contentView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.mas_equalTo(0);
      make.right.mas_equalTo(0);
      make.height.mas_offset(list.count * 48 + [DeviceInforTool getVirtualHomeHeight]);
      make.bottom.mas_offset(0);
    }];
    _contentView = contentView;

    NSMutableArray *buttonList = [[NSMutableArray alloc] init];
    for (int i = 0; i < list.count; i++) {
        LiveSheetModel *model = list[i];
        BaseButton *button = [[BaseButton alloc] init];
        button.tag = 3000 + i;
        button.backgroundColor = [UIColor clearColor];
        [button setTitle:model.titleStr forState:UIControlStateNormal];
        if (model.isDisable) {
            [button setTitleColor:[UIColor colorFromRGBHexString:@"#E5E6EB" andAlpha:0.34 * 255] forState:UIControlStateNormal];
        } else {
            [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        }
        button.titleLabel.font = [UIFont systemFontOfSize:16];
        [button addTarget:self action:@selector(touchAction:) forControlEvents:UIControlEventTouchUpInside];
        [buttonList addObject:button];
        [contentView addSubview:button];
    }
    CGFloat itemHeight = 48;
    [buttonList mas_distributeViewsAlongAxis:MASAxisTypeVertical
                         withFixedItemLength:itemHeight
                                 leadSpacing:0
                                 tailSpacing:[DeviceInforTool getVirtualHomeHeight]];
    [buttonList mas_updateConstraints:^(MASConstraintMaker *make) {
      make.left.right.equalTo(self.contentView);
    }];

    // Start animation
    [rootVC.view layoutIfNeeded];
    [self.maskButton.superview setNeedsUpdateConstraints];
    [UIView animateWithDuration:0.25
                     animations:^{
                       [self.maskButton mas_updateConstraints:^(MASConstraintMaker *make) {
                         make.top.equalTo(rootVC.view).offset(0);
                       }];
                       [self.maskButton.superview layoutIfNeeded];
                     }];
}

- (void)touchAction:(BaseButton *)sender {
    NSInteger tag = sender.tag - 3000;
    LiveSheetModel *model = self.sheetModelList[tag];
    if (!model.isDisable) {
        if (model.clickBlock) {
            model.clickBlock(model);
        }

        [self dismissUserListView];
    }
}

- (void)maskButtonAction {
    [self dismissUserListView];
}

- (void)dismissUserListView {
    [self.maskButton removeAllSubviews];
    [self.maskButton removeFromSuperview];
    self.maskButton = nil;
}

#pragma mark - Getter

- (UIButton *)maskButton {
    if (!_maskButton) {
        _maskButton = [[UIButton alloc] init];
        [_maskButton addTarget:self action:@selector(maskButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [_maskButton setBackgroundColor:[UIColor clearColor]];
    }
    return _maskButton;
}

@end
