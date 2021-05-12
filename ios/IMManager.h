//
//  IMManager.h
//  RCTTxim
//
//  Created by 张建军 on 2019/5/5.
//  Copyright © 2019 feewee. All rights reserved.
//

#import <ImSDK/ImSDK.h>

@class IMMessageInfo;

/**
 *  消息发送成功回调
 *
 *  @param msg 消息
 */
typedef void (^IMSendMsgSucc)(IMMessageInfo *_Nonnull msg);

NS_ASSUME_NONNULL_BEGIN

@interface IMManager : NSObject

/**
 * 获取实例
 */
+ (instancetype)getInstance;

/**
 * 初始化SDK
 */
- (BOOL)initSdk;

/**
 * 初始化SDK
 * @param configFilePath 配置文件路径，默认为mainBundle下的txim.plist
 */
- (BOOL)initSdk:(NSString *_Nullable)configFilePath;

/**
 * 用户登录
 * @param identify identify
 * @param userSig userSig
 * @param succ 成功回调
 * @param fail 失败回调
 */
- (void)loginWithIdentify:(NSString *)identify
                  userSig:(NSString *)userSig
                     succ:(V2TIMSucc)succ
                     fail:(V2TIMFail)fail;

/**
 * 用户登出
 * @param succ 成功回调
 * @param fail 失败回调
 */
- (void)logoutWithSucc:(V2TIMSucc)succ fail:(V2TIMFail)fail;

/**
 * 获取会话
 * @param type 会话类型
 * @param receiver 会话接收者
 * @param succ 成功回调
 * @param fail 失败回调
 */
- (void)getConversationWithType:(NSString *)receiver
                           succ:(V2TIMSucc)succ
                           fail:(V2TIMFail)fail;

/**
 * 发送消息
 * 参数option:
 * 视频消息参数: (NSString *)imgPath (NSInteger)width (NSInteger)height (NSInteger)duration
 * 图片消息参数: (BOOL)compressed
 * 地理位置参数: (CGFloat)latitude (CGFloat)longitude
 * @param type 消息类型
 * @param content 消息内容
 * @param option 消息参数
 * @param succ 成功回调
 * @param fail 失败回调
 */
- (void)sendMessage:(NSString *)content
               succ:(V2TIMSucc)succ
               fail:(V2TIMFail)fail;


- (void)loadMessages:(V2TIMMessageListSucc)succ fail:(V2TIMFail)fail;

/**
 * 销毁会话
 */
- (void)destroyConversation;

/**
 * 配置设备token
 * @param token token
 */
- (void)configDeviceToken:(NSData *)token;

/**
 * 播放声音
 */
- (void)playSound;

@end

NS_ASSUME_NONNULL_END
