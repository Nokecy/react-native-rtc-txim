import { NativeModules } from 'react-native';

type RtcTximType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RtcTxim } = NativeModules;

export default RtcTxim as RtcTximType;
