import { NativeEventEmitter, NativeModules } from 'react-native';
import { EventName } from './constant';

const { IMInitializeModule: module } = NativeModules;
const IMEventEmitter = new NativeEventEmitter(module);
export { IMEventEmitter }

export default {
  /**
   * 添加收到新回话监听器
   */
  addNewConversationListener(listener: any) {
    return IMEventEmitter.addListener("onNewConversation", listener);
  },

  /**
   * 添加收到新回话监听器
   */
  addConversationChangedListener(listener: any) {
    return IMEventEmitter.addListener("onConversationChanged", listener);
  },

  /**
 * 添加收到新消息监听器
 */
  addRecvNewMessageListener(listener: any) {
    return IMEventEmitter.addListener("onRecvNewMessage", listener);
  },

  /**
   * 登陆
   * @param identify 用户账号
   * @param userSig 用户签名
   * @returns {*}
   */
  login(identify: any, userSig: any) {
    return new Promise((resolve, reject) => {
      try {
        module.imLogin(identify, userSig);
      } catch (e) {
        reject(e);
        return;
      }
      IMEventEmitter.once(EventName.loginStatus, resp => {
        resolve(resp);
      }, undefined);
    });
  },

  getUsersInfo(userList: any) {
    return module.getUsersInfo(userList);
  },

  /**
   * 退出
   * @returns {*}
   */
  logout() {
    return module.logout();
  },
};