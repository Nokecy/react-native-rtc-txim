//
//  IMInitializeModule.h
//  RCTTxim
//
//  Created by 张建军 on 2019/5/5.
//  Copyright © 2019 feewee. All rights reserved.
//

#import "RCTEventEmitter+IMBaseModule.h"

NS_ASSUME_NONNULL_BEGIN

@interface IMInitializeModule : RCTEventEmitter<RCTBridgeModule,V2TIMConversationListener, V2TIMAdvancedMsgListener>

@end

NS_ASSUME_NONNULL_END
