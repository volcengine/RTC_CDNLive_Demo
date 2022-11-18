//
//  LiveIMComponent.m
//  veRTC_Demo
//
//  Created by on 2021/5/23.
//  
//

#import "LiveIMComponent.h"
#import "LiveIMView.h"

@interface LiveIMComponent ()

@property (nonatomic, strong) LiveIMView *liveIMView;

@end

@implementation LiveIMComponent

- (instancetype)initWithSuperView:(UIView *)superView {
    self = [super init];
    if (self) {
        [superView addSubview:self.liveIMView];
        [self.liveIMView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.mas_equalTo(20);
          make.bottom.mas_equalTo(-78 - ([DeviceInforTool getVirtualHomeHeight]));
          make.height.mas_equalTo(115);
          make.width.mas_equalTo(275);
        }];
    }
    return self;
}

#pragma mark - Publish Action

- (void)addIM:(LiveIMModel *)model {
    NSMutableArray *datas = [[NSMutableArray alloc] initWithArray:self.liveIMView.dataLists];
    [datas addObject:model];
    self.liveIMView.dataLists = [datas copy];
}

#pragma mark - getter

- (LiveIMView *)liveIMView {
    if (!_liveIMView) {
        _liveIMView = [[LiveIMView alloc] init];
    }
    return _liveIMView;
}

@end
