
#import "IMBaseListener.h"

@interface IMBaseListener ()

@property(nonatomic, weak, readwrite) RCTEventEmitter *module;

@end

@implementation IMBaseListener

- (instancetype)initWithModule:(RCTEventEmitter *_Nonnull)module eventName:(NSString *_Nullable)eventName {
  self = [super init];
  if (self) {
    _module = module;
    _eventName = eventName;
  }
  return self;
}

- (void)sendEventWithCode:(int)code msg:(NSString *)msg {
  if (_eventName) {
    [_module sendEvent:_eventName code:code msg:msg];
  }
}

@end
