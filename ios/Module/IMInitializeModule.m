#import "IMInitializeModule.h"
#import "IMManager.h"
#import "IMEventNameConstant.h"
#import "IMConnListener.h"
#import "IMMessageListener.h"
#import "IMUserStatusListener.h"
#import "IMMessageInfo.h"

@implementation IMInitializeModule

#pragma mark - RCTEventEmitter

- (void)configListener {
  [[V2TIMManager sharedInstance] setConversationListener:self];
  [[V2TIMManager sharedInstance] addAdvancedMsgListener:self];
}

- (void)startObserving {
  [self setHasListeners:YES];
}

- (void)stopObserving {
  [self setHasListeners:NO];
}

#pragma mark - RCTBridgeModule

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

-(NSArray<NSString *> *)supportedEvents{
  NSArray<NSString*>* array = @[
      @"onNewConversation",
      @"onConversationChanged",
      
      @"onRecvMessageRevoked",
      @"onRecvNewMessage",
      @"onRecvC2CReadReceipt"
  ];
  return array;
}

/// 导出模块名称
RCT_EXPORT_MODULE(IMInitializeModule);

RCT_REMAP_METHOD(getUsersInfo,
                 userIds:(NSArray<NSString *> *)userIds
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  // @formatter:on
  V2TIMManager *manager = [V2TIMManager sharedInstance];
  [manager getUsersInfo:userIds succ:^(NSArray<V2TIMUserFullInfo *> *infoList) {
    resolve(@{
      @"code": @(0),
    });
  } fail:^(int code, NSString *desc) {
    reject([NSString stringWithFormat:@"%d", code], desc, nil);
  }];
}

/// 用户登录
// @formatter:off
RCT_REMAP_METHOD(login,
                 loginWithAccount:(NSString *)account
                 andUserSig:(NSString *)userSig
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  // @formatter:on
  IMManager *manager = [IMManager getInstance];
  [manager loginWithIdentify:account
                     userSig:userSig
                        succ:^{
                          resolve(@{
                            @"code": @(0),
                            @"msg": @"登录成功!",
                          });
                        }
                        fail:^(int code, NSString *msg) {
                          reject([NSString stringWithFormat:@"%d", code], msg, nil);
                        }];
}

/// 用户注销
// @formatter:off
RCT_REMAP_METHOD(logout,
                 logoutWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  // @formatter:on
  IMManager *manager = [IMManager getInstance];
  [manager logoutWithSucc:^{
    resolve(@(YES));
  }                  fail:^(int code, NSString *msg) {
    reject([NSString stringWithFormat:@"%@", @(code)], msg, nil);
  }];
}

#pragma mark - V2TIMConversationListener

- (void)onNewConversation:(NSArray<V2TIMConversation *> *)conversationList{
  //新会话
  [self sendEventWithName:@"onNewConversation" body:@{}];
}

- (void)onConversationChanged:(NSArray<V2TIMConversation *> *)conversationList{
  //会话更新
  [self sendEventWithName:@"onConversationChanged" body:@{}];
}

#pragma mark - V2TIMAdvancedMsgListener
- (void)onRecvMessageRevoked:(NSString *)msgID{
  //消息撤回
  [self sendEventWithName:@"onRecvMessageRevoked" body:@{}];
}

- (void)onRecvNewMessage:(V2TIMMessage *)msg{
  //收到新消息
  [self sendEventWithName:@"onRecvNewMessage" body:@{}];
}

- (void)onRecvC2CReadReceipt:(NSArray<V2TIMMessageReceipt *> *)receiptList{
  //c2c 消息已读
  [self sendEventWithName:@"onRecvC2CReadReceipt" body:@{}];
}
@end
