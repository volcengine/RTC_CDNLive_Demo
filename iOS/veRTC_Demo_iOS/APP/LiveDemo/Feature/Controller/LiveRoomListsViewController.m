//
//  LiveRoomViewController.m
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "LiveRoomListsViewController.h"
#import "LiveCreateRoomViewController.h"
#import "LiveRoomHostSettingView.h"
#import "LiveRoomTableView.h"
#import "LiveRoomViewController.h"

@interface LiveRoomListsViewController () <LiveRoomTableViewDelegate>

@property (nonatomic, strong) UIButton *createButton;
@property (nonatomic, strong) LiveRoomTableView *roomTableView;
@property (nonatomic, strong) UILabel *noDataLabel;
@property (nonatomic, copy) NSString *currentAppid;

@end

@implementation LiveRoomListsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.bgView.hidden = NO;
    self.navView.backgroundColor = [UIColor clearColor];
    
    [self.view addSubview:self.createButton];
    [self.createButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.size.mas_equalTo(CGSizeMake(170, 50));
        make.centerX.equalTo(self.view);
        make.bottom.equalTo(self.view).offset(-20 - [DeviceInforTool getVirtualHomeHeight]);
    }];
    
    [self.view addSubview:self.roomTableView];
    [self.roomTableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.top.equalTo(self.navView.mas_bottom);
        make.bottom.equalTo(self.createButton.mas_top).offset(-20);
    }];
    
    [self.view addSubview:self.noDataLabel];
    [self.noDataLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self.view);
    }];
    
    [self loadDataWithGetLists];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.navTitle = @"互动直播";
    [self.rightButton setImage:[UIImage imageNamed:@"refresh" bundleName:HomeBundleName] forState:UIControlStateNormal];
}

- (void)rightButtonAction:(BaseButton *)sender {
    [super rightButtonAction:sender];
    
    [self loadDataWithGetLists];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

#pragma mark - load data

- (void)loadDataWithGetLists {
    __weak __typeof(self) wself = self;
    [[ToastComponent shareToastComponent] showLoading];
    [LiveRTMManager liveClearUserWithBlock:^(RTMACKModel * _Nonnull model) {
        [LiveRTMManager liveGetActiveLiveRoomListWithBlock:^(NSArray<LiveRoomInfoModel *> *roomList, RTMACKModel *model) {
            [[ToastComponent shareToastComponent] dismiss];
            if (model.result) {
                wself.roomTableView.dataLists = roomList;
                if (roomList.count == 0) {
                    wself.noDataLabel.hidden = NO;
                } else {
                    wself.noDataLabel.hidden = YES;
                }
            } else {
                wself.noDataLabel.hidden = NO;
            }
        }];
    }];
}

#pragma mark - LiveRoomTableViewDelegate

- (void)LiveRoomTableView:(LiveRoomTableView *)LiveRoomTableView didSelectRowAtIndexPath:(LiveRoomInfoModel *)model {
    [PublicParameterComponent share].roomId = model.roomID;
    LiveRoomViewController *next = [[LiveRoomViewController alloc]
                                    initWithRoomModel:model
                                    streamPushUrl:@""];
    [self.navigationController pushViewController:next animated:YES];
    __weak __typeof(self) wself = self;
    next.hangUpBlock = ^(BOOL result) {
        [wself loadDataWithGetLists];
    };
}

#pragma mark - Touch Action

- (void)createButtonAction:(UIButton *)sender {
    sender.userInteractionEnabled = NO;
    LiveCreateRoomViewController *next = [[LiveCreateRoomViewController alloc] init];
    [self.navigationController pushViewController:next animated:YES];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        sender.userInteractionEnabled = YES;
    });
}

#pragma mark - getter

- (UIButton *)createButton {
    if (!_createButton) {
        _createButton = [[UIButton alloc] init];
        _createButton.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [_createButton addTarget:self action:@selector(createButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _createButton.layer.cornerRadius = 25;
        _createButton.layer.masksToBounds = YES;
        
        UIImageView *iconImageView = [[UIImageView alloc] init];
        iconImageView.image = [UIImage imageNamed:@"InteractiveLive_add" bundleName:HomeBundleName];
        [_createButton addSubview:iconImageView];
        [iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(16, 16));
            make.centerY.equalTo(_createButton);
            make.left.mas_equalTo(40);
        }];
        
        UILabel *titleLabel = [[UILabel alloc] init];
        titleLabel.text = @"创建直播";
        titleLabel.textColor = [UIColor whiteColor];
        titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        [_createButton addSubview:titleLabel];
        [titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(_createButton);
            make.left.equalTo(iconImageView.mas_right).offset(10);
        }];
    }
    return _createButton;
}

- (LiveRoomTableView *)roomTableView {
    if (!_roomTableView) {
        _roomTableView = [[LiveRoomTableView alloc] init];
        _roomTableView.delegate = self;
    }
    return _roomTableView;
}

- (UILabel *)noDataLabel {
    if (!_noDataLabel) {
        _noDataLabel = [[UILabel alloc] init];
        _noDataLabel.text = @"当前暂无主播开播";
        _noDataLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
        _noDataLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        _noDataLabel.hidden = YES;
    }
    return _noDataLabel;
}

- (void)dealloc {
    [[LiveRTCManager shareRtc] destoryEngine];
    [PublicParameterComponent clear];
}

@end
