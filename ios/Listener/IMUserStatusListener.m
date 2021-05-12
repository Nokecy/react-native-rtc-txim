

#import "IMUserStatusListener.h"

#define IM_LOG_WARN(MSG, ...) IM_LOG_TAG_WARN(@"用户状态改变", MSG, ##__VA_ARGS__)

@implementation IMUserStatusListener

- (void)onForceOffline {
//  IM_LOG_WARN(@"用户被踢下线");
  [self sendEventWithCode:6208 msg:@"用户被踢下线"];
}

- (void)onReConnFailed:(int)code err:(NSString *)err {
//  IM_LOG_WARN(@"用户断线重连失败，错误码：%d，原因：%@", code, err);
  [self sendEventWithCode:code msg:err];
}

- (void)onUserSigExpired {
//  IM_LOG_WARN(@"用户签名过期");
  [self sendEventWithCode:6206 msg:@"用户签名过期"];
}

@end
