#import "IMMessageModule.h"
#import "IMEventNameConstant.h"
#import "IMManager.h"
#import "IMMessageBuilder.h"
#import "IMMessageInfo.h"

#define TM_INSTANCE [V2TIMManager sharedInstance]
#define DEFINE_TM(NAME) V2TIMManager *NAME = TM_INSTANCE

@implementation IMMessageModule

#pragma mark - RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents {
  return @[EventNameOnNewMessage];
}

- (void)startObserving {
  [self setHasListeners:YES];
}

- (void)stopObserving {
  [self setHasListeners:NO];
}

#pragma mark - RCTBridgeModule

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

/// 导出模块名称
RCT_EXPORT_MODULE(IMMessageModule);

RCT_REMAP_METHOD(getC2CHistoryMessageList,
                 userID:(NSString*)userID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  [[IMManager getInstance] loadMessages:^(NSArray<V2TIMMessage *> *msgs) {
    NSMutableArray *list = [NSMutableArray new];
    if (msgs.count > 0) {
        for (V2TIMMessage* msg in msgs) {
          IMMessageInfo* info = [IMMessageBuilder buildMessageWithTIMMessage:msg];
          NSDictionary *dic = info == nil ? @{} :[info toDict];
          [list addObject:dic];
        }
    }
    resolve(list);
  } fail:^(int code, NSString *desc) {
    reject(@"",desc,nil);
  }];
}

/// 获取会话
// @formatter:off
RCT_REMAP_METHOD(getConversation,
                 conversationID:(NSString *)conversationID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  // @formatter:on
  IMManager *manager = [IMManager getInstance];
  [manager getConversationWithType:conversationID
                              succ:^{
                                resolve(@{
                                  @"code": @(0),
                                  @"msg": @"获取会话成功!",
                                });
                              }
                              fail:^(int code, NSString *msg) {
                                reject([NSString stringWithFormat:@"%d", code], msg, nil);
                              }];
}

RCT_REMAP_METHOD(getConversationList, resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  DEFINE_TM(tm);
  [tm getConversationList:0 count:100 succ:^(NSArray<V2TIMConversation *> *conversationList, uint64_t nextSeq, BOOL isFinished) {
    NSMutableArray *list = [NSMutableArray new];
     for (V2TIMConversation *info in conversationList) {
       NSString* userId = [info userID];
       NSString* name = [info showName];
       NSString* faceUrl = [info faceUrl];
       int unreadCount  = [info unreadCount];
       
       V2TIMMessage* lastMsg  = [info lastMessage];
       IMMessageInfo* lastMsgInfo = [IMMessageBuilder buildMessageWithTIMMessage:lastMsg];
       NSDictionary *lastMsgDic = lastMsgInfo == nil ? @{} :[lastMsgInfo toDict];
       NSDictionary* map=@{
                      @"conversationID": [info conversationID],
                      @"userId":userId == nil ?@"":userId,
                      @"showName": name,
                      @"faceUrl": faceUrl == nil ? @"" : faceUrl,
                      @"unreadCount": @(unreadCount),
                      @"lastMsg": lastMsgDic
                  };
      [list addObject:map];
     }
       resolve(list);
  } fail:^(int code, NSString *desc) {
    
  }];
}

/// 发送消息
// @formatter:off
RCT_REMAP_METHOD(sendMessage,
                 content:(NSString *)content
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  // @formatter:on
  IMManager *manager = [IMManager getInstance];
  __weak typeof(self) weakSelf = self;
  [manager sendMessage:content
                  succ:^() {
//                    [weakSelf sendEvent:EventNameOnNewMessage body:@[[msg toDict]]];
                    resolve(@{
                      @"code": @(0),
                      @"msg": @"获取会话成功!",
                    });
                  }
                  fail:^(int code, NSString *msg) {
                    reject([NSString stringWithFormat:@"%d", code], msg, nil);
                  }];
}

/// 销毁会话
// @formatter:off
RCT_EXPORT_METHOD(destroyConversation) {
  // @formatter:on
  IMManager *manager = [IMManager getInstance];
  [manager destroyConversation];
}

@end
