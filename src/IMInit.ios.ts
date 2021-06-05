import { NativeEventEmitter, NativeModules } from 'react-native';

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

  login(identify: any, userSig: any) {
    try {
      return module.login(identify, userSig);
    } catch (e) {
      return Promise.reject(e);
    }
  },

  getUsersInfo(userList: any) {
    return module.getUsersInfo(userList);
  },

  logout() {
    try {
      return module.logout();
    } catch (e) {
      return Promise.reject(e);
    }
  },
};