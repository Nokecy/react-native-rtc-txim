#import "RCTEventEmitter+IMBaseModule.h"
#import <ImSDK/ImSDK.h>

NS_ASSUME_NONNULL_BEGIN

@interface IMInitializeModule : RCTEventEmitter<RCTBridgeModule,V2TIMConversationListener, V2TIMAdvancedMsgListener>

@end

NS_ASSUME_NONNULL_END
