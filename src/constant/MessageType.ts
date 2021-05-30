import { NativeModules, Platform } from 'react-native';

const { IMInitializeModule: module } = NativeModules;

const msgType = (name:any) => {
  return name;
};

export default {
  Text: msgType('Text'),
  Image: msgType('Image'),
  Sound: msgType('Sound'),
  Video: msgType('Video'),
  File: msgType('File'),
  Location: msgType('Location'),
  Face: msgType('Face'),
  Custom: msgType('Custom'),
};
