#import "IMManager.h"
#import "IMMessageBuilder.h"
#import <AVFoundation/AVFoundation.h>

@implementation IMManager {
  /// 是否初始化
  BOOL isInit;
  /// 应用ID
  int sdkAppId;
  /// 会话
  V2TIMConversation *conversation;
  /// 会话
  NSString *currentReceiver;
  /// 设备token
  NSData *deviceToken;
  /// IM配置
  NSDictionary *configDict;
  /// 声音ID
  SystemSoundID soundID;
  
  V2TIMMessage *msgForGet;
}

+ (instancetype)getInstance {
  __strong static IMManager *instance;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [self new];
  });
  return instance;
}

- (BOOL)initSdk {
  return [self initSdk:nil];
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "ResourceNotFoundInspection"

- (BOOL)initSdk:(NSString *)configFilePath {
  if (isInit) {
    return YES;
  }
  DEFINE_TM(tm);
  // 初始化SDK基本配置
  V2TIMSDKConfig *sdkConfig = [V2TIMSDKConfig new];
  NSString *path;
  if (configFilePath) {
    path = configFilePath;
  } else {
    path = [[NSBundle mainBundle] pathForResource:@"txim" ofType:@"plist"];
  }
  if (!path) {
//    [[TIMManager sharedInstance] log:<#(TIMLogLevel)#> tag:<#(NSString *)#> msg:<#(NSString *)#>]
//    IM_LOG_TAG_ERROR(@"Init", @"未找到IM配置文件");
    return NO;
  }
  configDict = [[NSDictionary alloc] initWithContentsOfFile:path];
  // 用户标识接入SDK的应用ID
  id sdkAppIdValue = [configDict valueForKey:@"sdkAppId"];
  if (!sdkAppIdValue) {
//    IM_LOG_TAG_ERROR(@"Init", @"未配置sdkAppId");
    return NO;
  }
  sdkAppId = [sdkAppIdValue intValue];
  // Log输出级别, 默认DEBUG等级
  id logLevelValue = [configDict valueForKey:@"logLevel"];
  if (logLevelValue) {
    sdkConfig.logLevel = (V2TIMLogLevel) [logLevelValue integerValue];
  }
  // 消息提示声音
  id soundValue = [configDict valueForKey:@"sound"];
  if (soundValue) {
    NSURL *soundUrl = [[NSBundle mainBundle] URLForResource:soundValue withExtension:nil];
    if (soundUrl) {
      AudioServicesCreateSystemSoundID((__bridge CFURLRef) (soundUrl), &soundID);
    }
  }
  int result = [tm initSDK:sdkAppId config:sdkConfig listener:nil];
  if (result != 0) {
    return NO;
  }
  isInit = YES;
  return isInit;
}

#pragma clang diagnostic pop

- (void)loginWithIdentify:(NSString *)identify
                  userSig:(NSString *)userSig
                     succ:(V2TIMSucc)succ
                     fail:(V2TIMFail)fail {
  DEFINE_TM(tm);
  void (^login)(void) = ^(void) {
    [tm login:identify userSig:userSig
                      succ:^{
                        [self configAppAPNSDeviceToken];
                        succ();
                      } fail:fail];
  };
  // 判断是否已经登录
  if ([tm getLoginStatus] == TIM_STATUS_LOGINED) {
    // 判断是否已经登录了当前账号
    if ([[tm getLoginUser] isEqualToString:identify]) {
      login();
    } else {
      // 登出之前的账号
      [tm logout:^{
        login();
      }fail:fail];
    }
  } else {
    login();
  }
}

- (void)logoutWithSucc:(TIMLoginSucc)succ fail:(V2TIMFail)fail {
  DEFINE_TM(tm);
  if ([tm getLoginStatus] == TIM_STATUS_LOGOUT) {
    succ();
  } else {
    [tm logout:succ fail:fail];
  }
}

- (void)getConversationWithType:(NSString *)conversationID
                           succ:(V2TIMSucc)succ
                           fail:(V2TIMFail)fail {
  DEFINE_TM(tm);
  //如果是 C2C 单聊，组成方式为 c2c_userID，如果是群聊，组成方式为 group_groupID
  [tm getConversation:conversationID succ:^(V2TIMConversation *conv) {
    NSString *userID = conv.userID;
    [tm markC2CMessageAsRead:userID succ:^{

    } fail:^(int code, NSString *desc) {

    }];
    self->currentReceiver = conversationID;
    self->conversation = conv;
    msgForGet = nil;
    succ();
  } fail:^(int code, NSString *desc) {
    fail(code,desc);
  }];
}

- (void)setMessageRead:(V2TIMMessage *)message {
  if ([[message userID] isEqualToString:[conversation userID]]) {
    DEFINE_TM(tm);
    [tm markC2CMessageAsRead:conversation.userID succ:^{
      
    } fail:^(int code, NSString *desc) {
      
    }];
  }
}

- (void)sendMessage:(NSString *)content
               succ:(V2TIMSucc)succ
               fail:(V2TIMFail)fail {
  DEFINE_TM(tm);
  if ([tm getLoginStatus] != TIM_STATUS_LOGINED) {
    fail(-1, @"请先登录");
    return;
  }
  if (!conversation) {
    fail(-1, @"当前会话已被销毁，请重新获取");
    return;
  }
  [tm sendC2CTextMessage:content to:conversation.userID succ:^{
    succ();
  } fail:^(int code, NSString *desc) {
    fail(code,desc);
  }];
}

-(void)loadMessages:(V2TIMMessageListSucc)succ fail:(V2TIMFail)fail{
  if (!conversation) {
    fail(-1, @"当前会话已被销毁，请重新获取");
    return;
  }
  V2TIMMessageListGetOption* option = [V2TIMMessageListGetOption new];
  option.count = 20;
  option.getType = V2TIM_GET_LOCAL_OLDER_MSG;
  option.userID = conversation.userID;
  option.lastMsg = msgForGet;
  [[V2TIMManager sharedInstance] getHistoryMessageList:option succ:^(NSArray<V2TIMMessage *> *msgs) {
    succ(msgs);
  } fail:^(int code, NSString *desc) {
    fail(code,desc);
  }];
//  [[V2TIMManager sharedInstance] getC2CHistoryMessageList:conversation.userID count:20
//  lastMsg:msgForGet succ:^(NSArray<V2TIMMessage *> *msgs) {
//    succ(msgs);
//  } fail:^(int code, NSString *msg) {
//    fail(code,msg);
//  }];
}

- (void)destroyConversation {
  if (conversation) {
    DEFINE_TM(tm);
    [tm deleteConversation:conversation.conversationID succ:^{
      
    } fail:^(int code, NSString *desc) {
      
    }];
    conversation = nil;
    currentReceiver = nil;
    msgForGet = nil;
  }
}

- (void)configDeviceToken:(NSData *)token {
  deviceToken = token;
}

- (void)playSound {
  // 播放
  if (soundID) {
    AudioServicesPlaySystemSound(soundID);
  }
}

/**
 * 配置设备token
 */
- (void)configAppAPNSDeviceToken {
  DEFINE_TM(tm);
//  // APNS配置
//  TIMAPNSConfig *apnsConfig = [TIMAPNSConfig new];
//  [apnsConfig setOpenPush:1];
//  [tm setAPNS:apnsConfig succ:^{
////    IM_LOG_TAG_INFO(@"APNS", @"APNS配置成功");
//  } fail:^(int code, NSString *msg) {
////    IM_LOG_TAG_WARN(@"APNS", @"APNS配置失败，错误码：%d，原因：%@", code, msg);
//  }];
//  NSString *token = [NSString stringWithFormat:@"%@", deviceToken];
////  IM_LOG_TAG_INFO(@"SetToken", @"Token is : %@", token);
//  TIMTokenParam *param = [TIMTokenParam new];
//#if kAppStoreVersion// AppStore 版本
//  #if DEBUG
//  param.busiId = (uint32_t) [[configDict valueForKey:@"debugBusiId"] unsignedIntegerValue];
//#else
//  param.busiId = (uint32_t) [[configDict valueForKey:@"busiId"] unsignedIntegerValue];
//#endif
//#else// 企业证书 ID
//  param.busiId = (uint32_t) [[configDict valueForKey:@"busiId"] unsignedIntegerValue];
//#endif
//  [param setToken:deviceToken];
}

@end
