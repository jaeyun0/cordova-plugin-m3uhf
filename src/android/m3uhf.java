package net.m3mobile.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class m3uhf extends CordovaPlugin {

    private final String TAG = "m3uhf";
    private final String UGR_ACTION_ENABLE = "com.android.server.ugrservice.m3onoff";
    private final String UGR_EXTRA_ENABLE = "ugronoff";
    private final String UGR_ACTION_START = "android.intent.action.M3UGR_BUTTON_DOWN";
    private final String UGR_ACTION_CANCEL = "android.intent.action.M3UGR_BUTTON_UP";
    private final String UGR_ACTION_EPC = "com.android.server.ugrservice.broadcast";
    private final String UGR_EXTRA_EPC_DATA = "m3ugrdata";
    private final String UGR_ACTION_GET_SETTING = "com.android.server.ugrservice.getsetting";
    private final String UGR_ACTION_SETTING = "com.android.server.ugrservice.setting";
    private final String UGR_EXTRA_POWER = "m3ugr_power";
    private final String UGR_ACTION_SETTING_CHANGE = "com.android.server.ugrservice.settingchange";
    private final String UGR_ACTION_MEMORY_READING = "com.android.server.ugrservice.reading";
    private final String UGR_ACTION_MEMORY_RESPONSE = "com.android.server.ugrservice.memory.response";
    private final String UGR_EXTRA_MEMORY = "m3ugr_memory";
    private final String UGR_ACTION_LOCK = "com.android.server.ugrservice.lock";
    public static final String UGR_ACTION_KILL = "com.android.server.ugrservice.kill";
    public static final String UGR_ACTION_LOCK_RESPONSE = "com.android.server.ugrservice.lock.response";
    public static final String UGR_ACTION_KILL_RESPONSE = "com.android.server.ugrservice.kill.response";

    CallbackContext getPowerCallbackContext;
    CallbackContext setPowerCallbackContext;
    CallbackContext memoryReadCallbackContext;
    CallbackContext memoryWriteCallbackContext;
    CallbackContext memoryLockCallbackContext;
    CallbackContext memoryKillCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "action: " + action);
        if (action.equals("RFIDEnable")) {
            Boolean bEnable = args.getBoolean(0);
            this.RFIDEnable(bEnable);
            return true;
        } else if (action.equals("inventory")) {
            Boolean bStart = args.getBoolean(0);
            this.inventory(bStart);
            return true;
        } else if (action.equals("getPower")) {
            this.getPower();
            getPowerCallbackContext = callbackContext;
            return true;
        } else if (action.equals("setPower")) {
            int nPower = args.getInt(0);
            this.setPower(nPower);
            setPowerCallbackContext = callbackContext;
            return true;
        } else if (action.equals("memoryRead")) {
            int nOffset = Integer.parseInt(args.getString(0), 16);
            int nLength = Integer.parseInt(args.getString(1), 16);
            int nMemBank = args.getInt(2); // 0: reserved, 1: epc, 2: tid, 3: user
            String strPwd = args.getString(3);

            this.memoryRead(nOffset, nLength, nMemBank, strPwd);
            memoryReadCallbackContext = callbackContext;
            return true;
        } else if (action.equals("memoryWrite")) {
            int nOffset = Integer.parseInt(args.getString(0), 16);
            int nLength = Integer.parseInt(args.getString(1), 16);
            int nMemBank = args.getInt(2); // 0: reserved, 1: epc, 2: tid, 3: user
            String strData = args.getString(3);
            String strPwd = args.getString(4);

            this.memoryWrite(nOffset, nLength, nMemBank, strData, strPwd);
            memoryWriteCallbackContext = callbackContext;
            return true;
        } else if (action.equals("memoryLock")) {
            // 0: ACCESSIBLE, 1: ALWAYS_ACCESSIBLE, 2: SECURED_ACCESSIBLE, 3: ALWAYS_NOT_ACCESSIBLE, 4: NO_CHANGE
            int nAccPermission = args.getInt(0);
            int nKillPermission = args.getInt(1);
            int nEpcPermission = args.getInt(2);
            int nTidPermission = args.getInt(3);
            int nUserPermission = args.getInt(4);
            String strAccPwd = args.getString(5);

            this.memoryLock(nAccPermission, nKillPermission, nEpcPermission, nTidPermission, nUserPermission, strAccPwd);
            memoryLockCallbackContext = callbackContext;
            return true;
        } else if (action.equals("memoryKill")) {
            String strKillPwd = args.getString(0);

            this.memoryKill(strKillPwd);
            memoryKillCallbackContext = callbackContext;
            return true;
        }
        return false;
    }

    private void RFIDEnable(boolean bOn) {
        Log.d(TAG, "RFIDEnable");
        int nExtra;
        if(bOn)
            nExtra = 1;
        else
            nExtra = 0;
        Intent intent = new Intent(UGR_ACTION_ENABLE, null);
        intent.putExtra(UGR_EXTRA_ENABLE, nExtra);
        intent.putExtra("module_reset", false);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void inventory(boolean bStart) {
        Intent intent;
        if(bStart) {
            intent = new Intent(UGR_ACTION_START, null);
        } else {
            intent = new Intent(UGR_ACTION_CANCEL, null);
        }
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void getPower() {
        Intent powerIntent = new Intent(UGR_ACTION_GET_SETTING);
        powerIntent.putExtra("setting", "power");
        cordova.getActivity().sendOrderedBroadcast(powerIntent, null);
    }

    private void setPower(int nPower) {
        if(nPower < 0 || nPower > 300) {
            setPowerCallbackContext.error("error-setPower");
            return;
        }
        Intent intent = new Intent(UGR_ACTION_SETTING_CHANGE);
        intent.putExtra("setting", "power");
        intent.putExtra("power_value", nPower);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void memoryRead(int nOffset, int nLength, int nMemBank, String strPwd) {
        Intent intent = new Intent(UGR_ACTION_MEMORY_READING);
        intent.putExtra("memory_bank", nMemBank);
        intent.putExtra("offset", nOffset);
        intent.putExtra("length", nLength);
        intent.putExtra("password", strPwd);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void memoryWrite(int nOffset, int nLength, int nMemBank, String strData, String strPwd) {
        Intent intent = new Intent(UGR_ACTION_MEMORY_READING);
        intent.putExtra("memory_bank", nMemBank);
        intent.putExtra("offset", nOffset);
        intent.putExtra("length", nLength);
        intent.putExtra("data", strData.replaceAll(" ", ""));
        intent.putExtra("password", strPwd);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void memoryLock(int nAccPermission, int nKillPermission, int nEpcPermission, int nTidPermission, int nUserPermission, String strAccPwd) {
        Intent intent = new Intent(UGR_ACTION_LOCK);
        intent.putExtra("acc_permission", nAccPermission);
        intent.putExtra("kill_permission", nKillPermission);
        intent.putExtra("epc_permission", nEpcPermission);
        intent.putExtra("tid_permission", nTidPermission);
        intent.putExtra("user_permission", nUserPermission);
        intent.putExtra("acc_pwd", strAccPwd);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    private void memoryKill(String strKillPwd) {
        Intent intent = new Intent(UGR_ACTION_KILL);
        intent.putExtra("kill_pwd", strKillPwd);
        cordova.getActivity().sendOrderedBroadcast(intent, null);
    }

    public BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive [" + intent.getAction() + "]");
            String epc;
            if (intent.getAction().equals(UGR_ACTION_EPC)) {
                epc = intent.getExtras().getString(UGR_EXTRA_EPC_DATA);
                if (epc != null) {
                    webView.loadUrl("javascript:setMessage('"+ epc + "')");
                }
            }
        }
    };

    public BroadcastReceiver UGRSettingIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive [" + intent.getAction() + "]");
            if(intent.getAction().equals(UGR_ACTION_SETTING)) {
                String extra = intent.getStringExtra("setting");
                if(extra.equals("power")) {
                    int nPower = intent.getExtras().getInt(UGR_EXTRA_POWER);
                    if(nPower >= 0 && nPower <= 300)
                        getPowerCallbackContext.success(nPower);
                    else
                        getPowerCallbackContext.error("error-getPower");
                }
            }
        }
    };

    public BroadcastReceiver UGRAccessReceiver = new BroadcastReceiver() {
        String strData;
        String strType;
        String strMessage;
        boolean bSuccess;
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive [" + intent.getAction() + "]");
            if(intent.getAction().equals(UGR_ACTION_MEMORY_RESPONSE)) {
                strData = intent.getExtras().getString(UGR_EXTRA_MEMORY);
                bSuccess = intent.getExtras().getBoolean("success");
                strType = intent.getExtras().getString("type");
                strMessage = intent.getExtras().getString("message");
                switch (strType) {
                    case "read":
                        if(bSuccess)
                            memoryReadCallbackContext.success(strData);
                        else
                            memoryReadCallbackContext.error("Result: Reading Failed.");
                        break;
                    case "write":
                        if(bSuccess)
                            memoryWriteCallbackContext.success("Result: Writing Success.");
                        else
                            memoryWriteCallbackContext.error("Result: Writing Failed.\n" + strMessage);
                        break;
                }
            } else if(intent.getAction().equals(UGR_ACTION_LOCK_RESPONSE)) {
                bSuccess = intent.getExtras().getBoolean("success");
                String strMessage = intent.getExtras().getString("message");
                Log.d("onReceive", "bSuccess = " + bSuccess);
                if(bSuccess)
                    memoryLockCallbackContext.success("Lock success");
                else
                    memoryLockCallbackContext.error("Result: Fail to change permissions.\n" + strMessage);
            } else if(intent.getAction().equals(UGR_ACTION_KILL_RESPONSE)) {
                bSuccess = intent.getExtras().getBoolean("success");
                Log.d("onReceive", "bSuccess = " + bSuccess);
                if(bSuccess)
                    memoryKillCallbackContext.success("Kill success");
                else
                    memoryKillCallbackContext.error("Result: Fail to Kill Tag.");
            }
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(TAG, "initialize");
        super.initialize(cordova, webView);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UGR_ACTION_EPC);
        cordova.getActivity().registerReceiver(resultReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(UGR_ACTION_SETTING);
        cordova.getActivity().registerReceiver(UGRSettingIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(UGR_ACTION_MEMORY_RESPONSE);
        cordova.getActivity().registerReceiver(UGRAccessReceiver, filter);

    }

    @Override
    public void onDestroy() {
        cordova.getActivity().unregisterReceiver(resultReceiver);
        cordova.getActivity().unregisterReceiver(UGRSettingIntentReceiver);
        cordova.getActivity().unregisterReceiver(UGRAccessReceiver);
        super.onDestroy();
    }
}
