import { NativeModules } from 'react-native';

const { IMMessageModule: module } = NativeModules;

export default {
  getConversation(peer: any) {
    try {
      return module.getConversation(peer);
    } catch (e) {
      return Promise.reject(e);
    }
  },

  getConversationList() {
    return module.getConversationList();
  },

  destroyConversation() {
    return module.destroyConversation();
  },

  getC2CHistoryMessageList(userId: any) {
    return module.getC2CHistoryMessageList(userId);
  },

  sendTextMsg(text: any) {
    try {
      return module.sendMessage(text);
    } catch (e) {
      return Promise.reject(e);
    }
  },
};
