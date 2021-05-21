import { NativeEventEmitter, NativeModules } from 'react-native';
import { EventName, MessageType } from './constant';

const { IMMessageModule: module } = NativeModules;
const emitter = new NativeEventEmitter(module);

export default {
  addMessageReceiveListener(listener: any, context: any) {
    return emitter.addListener(EventName.onNewMessage, listener, context);
  },

  getConversation(identify: any, userSig: any) {
    try {
      return module.getConversation(identify, userSig);
    } catch (e) {
      return Promise.reject(e);
    }
  },

  destroyConversation() {
    return module.destroyConversation();
  },

  sendTextMsg(text: any) {
    try {
      return module.sendMessage(MessageType.Text, text, {});
    } catch (e) {
      return Promise.reject(e);
    }
  },
};
