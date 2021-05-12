//
//  IMMessageBuilder.h
//  RCTTxim
//
//  Created by 张建军 on 2019/5/5.
//  Copyright © 2019 feewee. All rights reserved.
//

#import "IMMessageInfo.h"

NS_ASSUME_NONNULL_BEGIN

@interface IMMessageBuilder : NSObject

/**
 * 根据TIMMessage构建消息
 * @param msg 消息
 * @return 消息
 */
+ (IMMessageInfo *)buildMessageWithTIMMessage:(V2TIMMessage *)msg;

@end

NS_ASSUME_NONNULL_END
