//
//  LiveCoHostView.m
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveAddGuestsListsView.h"

@interface LiveAddGuestsListsView () <UITableViewDelegate, UITableViewDataSource, LiveAddGuestsUserListtCellDelegate>

@property (nonatomic, strong) UILabel *emptyLabel;
@property (nonatomic, strong) UITableView *roomTableView;

@end

@implementation LiveAddGuestsListsView

- (instancetype)init {
    self = [super init];
    if (self) {
        [self addSubview:self.emptyLabel];
        [self.emptyLabel mas_makeConstraints:^(MASConstraintMaker *make) {
          make.center.equalTo(self);
        }];

        [self addSubview:self.roomTableView];
        [self.roomTableView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.edges.equalTo(self);
        }];
    }
    return self;
}

#pragma mark - Publish Action

- (void)setDataLists:(NSArray<LiveUserModel *> *)dataLists {
    _dataLists = dataLists;

    self.emptyLabel.hidden = dataLists.count > 0 ? YES : NO;
    self.roomTableView.hidden = dataLists.count > 0 ? NO : YES;

    [self.roomTableView reloadData];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    LiveAddGuestsUserListtCell *cell = [tableView dequeueReusableCellWithIdentifier:@"LiveAddGuestsUserListtCellID" forIndexPath:indexPath];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.model = self.dataLists[indexPath.row];
    cell.delegate = self;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataLists.count;
}

#pragma mark - LiveAddGuestsUserListtCellDelegate

- (void)liveAddGuestsUserListtCell:(LiveAddGuestsUserListtCell *)liveAddGuestsUserListtCell clickButton:(LiveUserModel *)model {
    if ([self.delegate respondsToSelector:@selector(liveAddGuestsListsView:clickButton:)]) {
        [self.delegate liveAddGuestsListsView:self clickButton:model];
    }
}

#pragma mark - getter

- (UITableView *)roomTableView {
    if (!_roomTableView) {
        _roomTableView = [[UITableView alloc] init];
        _roomTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _roomTableView.delegate = self;
        _roomTableView.dataSource = self;
        _roomTableView.hidden = YES;
        [_roomTableView registerClass:LiveAddGuestsUserListtCell.class forCellReuseIdentifier:@"LiveAddGuestsUserListtCellID"];
        _roomTableView.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    }
    return _roomTableView;
}

- (UILabel *)emptyLabel {
    if (!_emptyLabel) {
        _emptyLabel = [[UILabel alloc] init];
        _emptyLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
        _emptyLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
        _emptyLabel.text = @"暂无观众在线";
        _emptyLabel.hidden = YES;
    }
    return _emptyLabel;
}

- (void)dealloc {
    NSLog(@"dealloc %@", NSStringFromClass([self class]));
}

@end