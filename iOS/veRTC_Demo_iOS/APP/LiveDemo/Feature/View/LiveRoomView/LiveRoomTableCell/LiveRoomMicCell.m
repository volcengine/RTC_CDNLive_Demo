//
//  LiveRoomMicCell.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/21.
//  Copyright © 2021 . All rights reserved.
//

#import "LiveRoomMicCell.h"
#import "LiveRoomMicUserView.h"

@interface LiveRoomMicCell ()

@property (nonatomic, strong) LiveRoomMicUserView *micUserView;

@property (nonatomic, strong) UIView *micLineView;

@end

@implementation LiveRoomMicCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];

        [self.contentView addSubview:self.micUserView];
        [self.micUserView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.equalTo(self.contentView);
          make.left.equalTo(self.contentView).offset(20);
          make.right.equalTo(self.contentView).offset(-20);
          make.height.mas_equalTo(100);
          make.bottom.equalTo(self.contentView).offset(-30);
        }];

        [self.contentView addSubview:self.micLineView];
        [self.micLineView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.height.mas_equalTo(1);
          make.left.equalTo(self.contentView).offset(20);
          make.right.equalTo(self.contentView).offset(-20);
          make.bottom.equalTo(self.contentView).offset(-1);
        }];
    }
    return self;
}

- (void)setDataLists:(NSArray *)dataLists {
    _dataLists = dataLists;

    self.micUserView.dataLists = dataLists;
    NSInteger row = (dataLists.count / 3);
    NSInteger rowNumber = ((dataLists.count % 3) == 0) ? row : row + 1;
    [self.micUserView mas_updateConstraints:^(MASConstraintMaker *make) {
      make.height.mas_equalTo(100 * rowNumber);
    }];
}

#pragma mark - getter

- (UIView *)micLineView {
    if (!_micLineView) {
        _micLineView = [[UIView alloc] init];
        _micLineView.backgroundColor = [UIColor colorFromRGBHexString:@"#FFFFFF" andAlpha:255 * 0.1];
    }
    return _micLineView;
}

- (LiveRoomMicUserView *)micUserView {
    if (!_micUserView) {
        _micUserView = [[LiveRoomMicUserView alloc] init];
        _micUserView.backgroundColor = [UIColor clearColor];
    }
    return _micUserView;
}

@end
