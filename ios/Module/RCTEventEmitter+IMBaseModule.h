#import <React/RCTEventEmitter.h>

NS_ASSUME_NONNULL_BEGIN

@protocol IMBaseModule

@optional
/**
 * 配置监听器
 */
- (void)configListener;

@end

@interface RCTEventEmitter (IMBaseModule) <IMBaseModule>

/**
 * 发送事件消息
 * @param eventName 事件名称
 * @param body 事件内容
 */
- (void)sendEvent:(NSString *)eventName body:(id)body;

/**
 * 发送事件消息
 * @param name 事件名称
 * @param code 消息代码
 * @param msg 消息内容
 */
- (void)sendEvent:(NSString *)name code:(int)code msg:(NSString *)msg;


/**
 * 设置是否有监听器
 */
- (void)setHasListeners:(BOOL)has;

@end

NS_ASSUME_NONNULL_END
