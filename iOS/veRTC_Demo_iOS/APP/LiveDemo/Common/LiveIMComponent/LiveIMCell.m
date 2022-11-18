//
//  LiveIMCell.m
//  veRTC_Demo
//
//  Created by on 2021/5/23.
//  
//

#import "LiveIMCell.h"

@interface LiveIMCell ()

@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UILabel *roomNameLabel;

@end

@implementation LiveIMCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];
        [self createUIComponent];
    }
    return self;
}

- (void)setModel:(LiveIMModel *)model {
    _model = model;
    
    if (NOEmptyStr(model.message)) {
        [self setLineSpace:5 withText:model.message inLabel:self.roomNameLabel imageName:model.imageName];
    } else {
        NSString *unitStr = model.isJoin ? @"加入了房间" : @"退出了房间";
        NSString *roomName = [NSString stringWithFormat:@"%@ %@", model.userModel.name, unitStr];
        [self setLineSpace:5 withText:roomName inLabel:self.roomNameLabel imageName:@""];
    }
}

#pragma mark - Private Action

- (void)createUIComponent {
    [self.contentView addSubview:self.bgView];
    [self.bgView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.top.equalTo(self.contentView).offset(10);
      make.bottom.left.equalTo(self.contentView);
    }];

    [self.bgView addSubview:self.roomNameLabel];
    [self.roomNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.equalTo(self.bgView).offset(16);
      make.top.equalTo(self.bgView).offset(5);
      make.right.equalTo(self.bgView).offset(-16);
      make.bottom.equalTo(self.bgView).offset(-5);
      make.right.mas_lessThanOrEqualTo(self.contentView.mas_right);
    }];
}

#pragma mark - Private Action

- (void)setLineSpace:(CGFloat)lineSpace withText:(NSString *)text inLabel:(UILabel *)label imageName:(NSString *)imageName {
    if (!text || !label) {
        return;
    }
    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:text];
    
    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.lineSpacing = lineSpace;
    paragraphStyle.lineBreakMode = label.lineBreakMode;
    paragraphStyle.alignment = label.textAlignment;
    [attributedString addAttribute:NSParagraphStyleAttributeName value:paragraphStyle range:NSMakeRange(0, [text length])];
    
    if (NOEmptyStr(imageName)) {
        NSTextAttachment *attch = [[NSTextAttachment alloc] init];
        attch.image = [UIImage imageNamed:imageName bundleName:HomeBundleName];
        attch.bounds = CGRectMake(0, label.font.descender + 1.5, 16, 16);
        NSAttributedString *imageString = [NSAttributedString attributedStringWithAttachment:attch];
        [attributedString appendAttributedString:imageString];
    }

    label.attributedText = attributedString;
}

#pragma mark - getter

- (UILabel *)roomNameLabel {
    if (!_roomNameLabel) {
        _roomNameLabel = [[UILabel alloc] init];
        _roomNameLabel.textColor = [UIColor whiteColor];
        _roomNameLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        _roomNameLabel.numberOfLines = 0;
    }
    return _roomNameLabel;
}

- (UIView *)bgView {
    if (!_bgView) {
        _bgView = [[UIView alloc] init];
        _bgView.backgroundColor = [UIColor colorFromRGBHexString:@"#1D2129" andAlpha:0.9 * 255];
        _bgView.layer.cornerRadius = 15;
        _bgView.layer.masksToBounds = YES;
    }
    return _bgView;
}

@end
