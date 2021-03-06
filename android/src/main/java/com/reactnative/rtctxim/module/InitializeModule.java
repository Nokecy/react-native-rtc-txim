package com.reactnative.rtctxim.module;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.reactnative.rtctxim.IMApplication;
import com.reactnative.rtctxim.constants.IMEventNameConstant;
import com.reactnative.rtctxim.R;
import com.reactnative.rtctxim.business.config.BaseConfigs;
import com.reactnative.rtctxim.business.InitializeBusiness;
import com.reactnative.rtctxim.business.config.CustomFaceGroupConfigs;
import com.reactnative.rtctxim.business.config.FaceConfig;
import com.reactnative.rtctxim.listener.ConnListener;
import com.reactnative.rtctxim.listener.GroupEventListener;
import com.reactnative.rtctxim.listener.MessageEventListener;
import com.reactnative.rtctxim.listener.MessageRevokedListener;
import com.reactnative.rtctxim.listener.RefreshListener;
import com.reactnative.rtctxim.listener.UserStatusListener;
import com.reactnative.rtctxim.utils.messageUtils.MessageInfo;
import com.reactnative.rtctxim.utils.thirdpush.ConstantsKey;
import com.reactnative.rtctxim.utils.thirdpush.ThirdPushTokenMgr;
import com.meizu.cloud.pushsdk.PushManager;
import com.meizu.cloud.pushsdk.util.MzSystemUtils;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMOfflinePushSettings;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.log.QLog;
import com.tencent.imsdk.session.SessionWrapper;
import com.tencent.imsdk.utils.IMFunc;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;


/**
 * @author kurisu
 */

public class InitializeModule extends BaseModule {

    private Context context;

    public InitializeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Nonnull
    @Override
    public String getName() {
        return "IMInitializeModule";
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????im?????????
     */
    @ReactMethod
    private void imLogin(String account, String userSig) {
        final WritableMap map = Arguments.createMap();
        InitializeBusiness.login(account, userSig, new TIMCallBack() {

            @Override
            public void onError(int errCode, String errMsg) {
                //????????? errCode ??????????????? errMsg????????????????????????????????????
                //????????? errCode ???????????????????????????
                map.putInt("code", errCode);
                map.putString("msg", errMsg);

                Toast.makeText(getReactApplicationContext(), String.valueOf(errCode), Toast.LENGTH_SHORT).show();
                QLog.e("????????????", "code:" + errCode + "    " + "msg:" + errMsg);
                sendEvent(IMEventNameConstant.LOGIN_STATUS, map);
            }

            @Override
            public void onSuccess() {
                map.putInt("code", 0);
                map.putString("msg", "????????????!");
                TIMOfflinePushSettings settings = new TIMOfflinePushSettings();
                settings.setEnabled(true);
                TIMManager.getInstance().setOfflinePushSettings(settings);

                ThirdPushTokenMgr.getInstance().setIsLogin(true);
                ThirdPushTokenMgr.getInstance().setPushTokenToTIM();

                Toast.makeText(getReactApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                sendEvent(IMEventNameConstant.LOGIN_STATUS, map);
            }
        });
    }

    /**
     * ????????????
     *
     * @param promise
     */
    @ReactMethod
    public void logout(Promise promise) {
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                promise.reject(String.valueOf(i), s);
            }

            @Override
            public void onSuccess() {
                promise.resolve(true);
            }
        });
    }

    /**
     * ???????????????
     *
     */

    public void init(int logLevel) {
        WritableMap map = Arguments.createMap();
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            int appid = info.metaData.getInt("IM_APPID");
            this.setPushKey(info);
            boolean init = InitializeBusiness.init(context, appid, BaseConfigs.getDefaultConfigs());
            //???????????????????????????
            customConfig();
            setPushConfig();

            if(IMFunc.isBrandHuawei()){
                // ??????????????????
//                HMSAgent.connect(getCurrentActivity(), new ConnectHandler() {
//                    @Override
//                    public void onConnect(int rst) {
//                        QLog.i("huaweipush", "HMS connect end:" + rst);
//                    }
//                });
                getHuaWeiPushToken();
            }
            if(IMFunc.isBrandVivo()){
                // vivo????????????
                PushClient.getInstance(IMApplication.getContext()).turnOnPush(new IPushActionListener() {
                    @Override
                    public void onStateChanged(int state) {
                        if(state == 0){
                            String regId = PushClient.getInstance(IMApplication.getContext()).getRegId();
                            QLog.i("vivopush", "open vivo push success regId = " + regId);
                            ThirdPushTokenMgr.getInstance().setThirdPushToken(regId);
                            ThirdPushTokenMgr.getInstance().setPushTokenToTIM();
                        }else {
                            // ??????vivo?????????????????????state = 101 ?????????vivo???????????????????????????vivo??????????????????https://dev.vivo.com.cn/documentCenter/doc/156
                            QLog.i("vivopush", "open vivo push fail state = " + state);
                        }
                    }
                });
            }

            QLog.i("?????????", "?????????" + init);
            if (init) {
                map.putInt("code", 0);
                map.putString("msg", "IM???????????????");
                sendEvent(IMEventNameConstant.INITIALIZE_STATUS, map);
            } else {
                map.putInt("code", -1);
                map.putString("msg", "IM???????????????: ????????????");
                sendEvent(IMEventNameConstant.INITIALIZE_STATUS, map);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            map.putInt("code", -1);
            map.putString("msg", e.getMessage());
            sendEvent(IMEventNameConstant.INITIALIZE_STATUS, map);

        } catch (Exception ex) {
            ex.printStackTrace();
            map.putInt("code", -1);
            map.putString("msg", ex.getMessage());
            sendEvent(IMEventNameConstant.INITIALIZE_STATUS, map);
        }
    }

    private void customConfig() {
        if (InitializeBusiness.getBaseConfigs() != null) {
            TIMUserConfig userConfig = new TIMUserConfig();
            //????????????????????????
            userConfig.setUserStatusListener(new UserStatusListener(this));
            //????????????????????????
            userConfig.setConnectionListener(new ConnListener(this));
            //?????????????????????
            userConfig.setRefreshListener(new RefreshListener(this));
            //????????????????????????
            userConfig.setGroupEventListener(new GroupEventListener(this));
            //?????????????????????
            TIMManager.getInstance().addMessageListener(new MessageEventListener(this));
            userConfig.setMessageRevokedListener(new MessageRevokedListener(this));
            userConfig.disableAutoReport(false);
            //????????????????????????
            userConfig.enableReadReceipt(true);

            TIMManager.getInstance().setUserConfig(userConfig);
        }
    }

    private ArrayList<CustomFaceGroupConfigs> initCustomConfig() {
        ArrayList<CustomFaceGroupConfigs> groupFaces = new ArrayList<>();
        //???????????????????????????
        CustomFaceGroupConfigs faceConfigs = new CustomFaceGroupConfigs();
        //?????????????????????????????????????????????
        faceConfigs.setPageColumnCount(5);
        //?????????????????????????????????????????????
        faceConfigs.setPageRowCount(2);
        //??????????????????
        faceConfigs.setFaceGroupId(1);
        //?????????????????????ICON
        faceConfigs.setFaceIconPath("4349/xx07@2x.png");
        //????????????????????????
        faceConfigs.setFaceIconName("4350");
        for (int i = 1; i <= 15; i++) {
            //????????????????????????
            FaceConfig faceConfig = new FaceConfig();
            String index = "" + i;
            if (i < 10)
                index = "0" + i;
            //??????????????????Asset??????????????????
            faceConfig.setAssetPath("4349/xx" + index + "@2x.png");
            //?????????????????????
            faceConfig.setFaceName("xx" + index + "@2x");
            //??????????????????
            faceConfig.setFaceWidth(240);
            //??????????????????
            faceConfig.setFaceHeight(240);
            faceConfigs.addFaceConfig(faceConfig);
        }
        groupFaces.add(faceConfigs);


        faceConfigs = new CustomFaceGroupConfigs();
        faceConfigs.setPageColumnCount(5);
        faceConfigs.setPageRowCount(2);
        faceConfigs.setFaceGroupId(1);
        faceConfigs.setFaceIconPath("4350/tt01@2x.png");
        faceConfigs.setFaceIconName("4350");
        for (int i = 1; i <= 16; i++) {
            FaceConfig faceConfig = new FaceConfig();
            String index = "" + i;
            if (i < 10)
                index = "0" + i;
            faceConfig.setAssetPath("4350/tt" + index + "@2x.png");
            faceConfig.setFaceName("tt" + index + "@2x");
            faceConfig.setFaceWidth(240);
            faceConfig.setFaceHeight(240);
            faceConfigs.addFaceConfig(faceConfig);
        }
        groupFaces.add(faceConfigs);


        return groupFaces;
    }


    private void setPushConfig() {
        if (SessionWrapper.isMainProcess(context)) {
            TIMManager.getInstance().setOfflinePushListener(notification -> {
                //??????????????????????????????
                notification.doNotify(context.getApplicationContext(), R.drawable.fw_ic_launcher);
            });

            if (IMFunc.isBrandXiaoMi()) {
                // ??????????????????
                MiPushClient.registerPush(context, ConstantsKey.XM_PUSH_APPID, ConstantsKey.XM_PUSH_APPKEY);
            }
            if (IMFunc.isBrandHuawei()) {
                // ??????????????????
//                HMSAgent.init((Application) IMApplication.getContext());
            }
            if (MzSystemUtils.isBrandMeizu(context)) {
                // ??????????????????
                PushManager.register(context, ConstantsKey.MZ_PUSH_APPID, ConstantsKey.MZ_PUSH_APPKEY);
            }
            if (IMFunc.isBrandVivo()) {
                // vivo????????????
                PushClient.getInstance(IMApplication.getContext()).initialize();
            }
        }
    }

    private void setPushKey(ApplicationInfo info) {
        ConstantsKey.XM_PUSH_BUZID = info.metaData.getInt("XM_PUSH_BUZID", 0);
        ConstantsKey.XM_PUSH_APPID = info.metaData.getString("XM_PUSH_APPID", "").trim();
        ConstantsKey.XM_PUSH_APPKEY = info.metaData.getString("XM_PUSH_APPKEY", "").trim();

        ConstantsKey.HW_PUSH_BUZID = info.metaData.getInt("HW_PUSH_BUZID", 0);

        ConstantsKey.VIVO_PUSH_BUZID = info.metaData.getInt("VIVO_PUSH_BUZID", 0);

        ConstantsKey.MZ_PUSH_APPID = info.metaData.getString("MZ_PUSH_APPID", "").trim();
        ConstantsKey.MZ_PUSH_APPKEY = info.metaData.getString("MZ_PUSH_APPKEY", "").trim();
        ConstantsKey.MZ_PUSH_BUZID = info.metaData.getInt("MZ_PUSH_BUZID", 0);
    }

    private void getHuaWeiPushToken() {
//        HMSAgent.Push.getToken(new GetTokenHandler() {
//            @Override
//            public void onResult(int rtnCode) {
//                QLog.i("huaweipush", "get token: end" + rtnCode);
//            }
//        });
    }

    @Override
    public Map<String, Object> getConstants() {
        //???js??????????????????????????????
        // ????????????
        Map<String, Object> constants = new HashMap<>();
        constants.put("userStatus", IMEventNameConstant.USER_STATUS_CHANGE);
        constants.put("initializeStatus", IMEventNameConstant.INITIALIZE_STATUS);
        constants.put("loginStatus", IMEventNameConstant.LOGIN_STATUS);
        constants.put("onNewMessage", IMEventNameConstant.ON_NEW_MESSAGE);
        constants.put("sendStatus", IMEventNameConstant.SEND_STATUS);
        constants.put("conversationStatus", IMEventNameConstant.CONVERSATION_STATUS);
        constants.put("conversationListStatus", IMEventNameConstant.CONVERSATION_LIST_STATUS);
        constants.put("onConversationRefresh", IMEventNameConstant.ON_CONVERSATION_REFRESH);
        constants.put("onMessageQuery", IMEventNameConstant.ON_MESSAGE_QUERY);

        //????????????
        constants.put("Text", MessageInfo.MSG_TYPE_TEXT);
        constants.put("Image", MessageInfo.MSG_TYPE_IMAGE);
        constants.put("Sound", MessageInfo.MSG_TYPE_AUDIO);
        constants.put("Video", MessageInfo.MSG_TYPE_VIDEO);
        constants.put("File", MessageInfo.MSG_TYPE_FILE);
        constants.put("Location", MessageInfo.MSG_TYPE_LOCATION);
        constants.put("Face", MessageInfo.MSG_TYPE_CUSTOM_FACE);
        constants.put("Custom", MessageInfo.MSG_TYPE_CUSTOM);
        return constants;
    }
}
