//
//  IMConversationListener.m
//  ACM
//
//  Created by 黎剑锋 on 2021/1/14.
//
#import "IMConversationListener.h"
#import "IMMessageBuilder.h"
#import "IMManager.h"

@implementation IMConversationListener

- (void)onNewConversation:(NSArray<V2TIMConversation *> *)conversationList{
  //新会话
  [self sendEventWithCode:6206 msg:@"用户签名过期"];
}

- (void)onConversationChanged:(NSArray<V2TIMConversation *> *)conversationList{
  //会话更新
  [self sendEventWithCode:6206 msg:@"用户签名过期"];
}

@end
