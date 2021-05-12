//
//  IMMessageBuilder.m
//  RCTTxim
//
//  Created by 张建军 on 2019/5/5.
//  Copyright © 2019 feewee. All rights reserved.
//

#import "IMMessageBuilder.h"

#define CURRENT_TIMESTAMP [[NSDate date] timeIntervalSince1970] * 1000

@implementation IMMessageBuilder

+ (IMMessageInfo *)buildMessageWithTIMMessage:(V2TIMMessage *)msg {
  if (!msg || msg.status == TIM_MSG_STATUS_HAS_DELETED) {
    return nil;
  }
  IMMessageInfo *info = [IMMessageInfo new];
  // 消息类型，内容
  if ([msg elemType] == V2TIM_ELEM_TYPE_TEXT) {
    info.msgType = IMMessageTypeText;
    info.extra = [[msg textElem] text];
  } else if([msg elemType] == V2TIM_ELEM_TYPE_CUSTOM) {
    info.msgType = IMMessageTypeCustom;
    NSDictionary* data = [IMMessageBuilder jsonData2Dictionary:[[msg customElem] data] ];
    info.customerData = data;
  } else {
    return nil;
  }
  // 推送信息字段
//  TIMOfflinePushInfo *pushInfo = [msg getOfflinePushInfo];
//  if (pushInfo) {
//    info.desc = [pushInfo desc];
//    info.extra = [pushInfo ext];
//  }
  // 消息基本信息
  info.msg = msg;
  info.msgId = [msg msgID];
  info.msgTime = [[msg timestamp] timeIntervalSince1970] * 1000;
  info.isSelf = [msg isSelf];
  info.status = [msg status];
  info.sender = [msg sender];
  info.senderNickName = [msg nickName];
  info.senderAvatar = [msg faceURL];
  info.receiver = [msg userID];
  info.isRead = [msg isPeerRead];
  return info;
}

+ (NSDictionary *)jsonData2Dictionary:(NSData *)jsonData {
    if (jsonData == nil) {
        return nil;
    }
    NSError *err = nil;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&err];
    if (err || ![dic isKindOfClass:[NSDictionary class]]) {
        NSLog(@"Json parse failed");
        return nil;
    }
    return dic;
}

@end
