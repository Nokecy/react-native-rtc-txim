#import "IMMessageInfo.h"

@implementation IMMessageInfo

- (instancetype)initWithType:(IMMessageType)type {
  self = [super init];
  if (self) {
    _msgType = type;
    _msgId = [[NSUUID UUID] UUIDString];
  }
  return self;
}

- (NSDictionary *)toDict {
  NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithDictionary:@{
    @"peer": _receiver,
    @"sender": _sender,
    @"msgId": _msgId,
    @"msgType": @(_msgType),
    @"msgTime": @(_msgTime),
    @"self": @(_isSelf),
    @"read": @(_isRead),
    @"status": @(_status),
    @"imgWidth": @"",
    @"imgHeight": @"",
    @"lat": @"",
    @"lng": @"",
  }];
  if (_customerData) {
    dict[@"customerData"] = _customerData;
  }
  if (_senderNickName) {
    dict[@"nickName"] = _senderNickName;
  }
  if (_senderAvatar) {
    dict[@"senderAvatar"] = _senderAvatar;
  }
  if (_extra) {
    dict[@"extra"] = _extra;
  }
  if (_desc) {
    dict[@"desc"] = _desc;
  }
  return dict;
}


@end
