package com.ztn.web.javascript_interface;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.webkit.JavascriptInterface;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;
import com.ficat.easyble.gatt.callback.BleReadCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.ficat.easyble.scan.BleScanCallback;
import com.ztn.web.bean.call.IJavaScriptFunction;
import com.ztn.web.utils.HexUtil;
import com.ztn.web.utils.JsonUtil;
import com.ztn.web.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleMethod implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final static String TAG = BleMethod.class.getSimpleName();
    private Map<String, BleDevice> scanResultList;
    private Map<com.ficat.easyble.gatt.bean.ServiceInfo, List<CharacteristicInfo>> serviceList;
    private IJavaScriptFunction javaScriptFunction;
    private BleManager bleManager;
    private Activity context;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
        }
    }


    private static class MyDeviceServiceInfo {
        private String uuid;
        private com.ficat.easyble.gatt.bean.ServiceInfo serviceInfo;
        private List<CharacteristicInfo> characteristicInfoList;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public ServiceInfo getServiceInfo() {
            return serviceInfo;
        }

        public void setServiceInfo(ServiceInfo serviceInfo) {
            this.serviceInfo = serviceInfo;
        }

        public List<CharacteristicInfo> getCharacteristicInfoList() {
            return characteristicInfoList;
        }

        public void setCharacteristicInfoList(List<CharacteristicInfo> characteristicInfoList) {
            this.characteristicInfoList = characteristicInfoList;
        }
    }

    public BleMethod(Activity context, IJavaScriptFunction javaScriptFunction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        this.javaScriptFunction = javaScriptFunction;
        this.context = context;
    }

    @JavascriptInterface
    public boolean isSupportBle() {
        LogUtil.i(TAG, "ble isSupportBle");
        return BleManager.supportBle(context);
    }

    @JavascriptInterface
    public boolean isBlueEnable() {
        LogUtil.i(TAG, "ble isBlueEnable");
        return true;
    }

    @JavascriptInterface
    public void enableBluetooth() {
        LogUtil.i(TAG, "ble enableBluetooth");
        if (!BleManager.supportBle(context)) {
            LogUtil.e(TAG, "不支持BLE");
        }
        bleManager = BleManager.getInstance(context.getApplication());
        BleManager.toggleBluetooth(true);
        BleManager.Options options = new BleManager.Options();
        options.loggable = true; //does it print log?
        options.connectTimeout = 10000; //connection time out
        options.scanPeriod = 12000; //scan period
        bleManager.option(options);

        bleManager.startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
                scanResultList.put(device.address, device);
                javaScriptFunction.evak("onScanning('" + JsonUtil.toJson(device) + "')");
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                if (startScanSuccess) {
                    scanResultList = new HashMap<>();
                } else {
                    LogUtil.e(TAG, "ble 扫描失败");
                }
            }

            @Override
            public void onFinish() {
                javaScriptFunction.evak("onScanFinished('" + JsonUtil.toJson(scanResultList) + "')");
            }

        });
    }


    @JavascriptInterface
    public void cancelScan() {
        LogUtil.i(TAG, "ble cancelScan");
        bleManager.stopScan();
    }


    @JavascriptInterface
    public Boolean connect(String mac) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return false;
        }

        bleManager.connect(bleDevice, new BleConnectCallback() {
            @Override
            public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
                LogUtil.i(TAG, "ble onStartConnect");
            }

            @Override
            public void onTimeout(BleDevice device) {
                LogUtil.i(TAG, "ble onConnectFail onTimeout");
                javaScriptFunction.evak("onConnectFail('" + JsonUtil.toJson(device) + "')");
            }

            @Override
            public void onConnected(BleDevice device) {
                LogUtil.i(TAG, "ble onConnectSuccess" + JsonUtil.toJson(device));
                javaScriptFunction.evak("onConnectSuccess('" + JsonUtil.toJson(device) + "')");
            }

            @Override
            public void onDisconnected(BleDevice device) {
                LogUtil.i(TAG, "ble onDisConnected " + JsonUtil.toJson(device));
                javaScriptFunction.evak("onDisConnected('" + JsonUtil.toJson(device) + "')");
            }

        });

        return true;
    }

    @JavascriptInterface
    public String listService(String mac) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return null;
        }
        serviceList = bleManager.getDeviceServices(bleDevice);
        List<MyDeviceServiceInfo> serviceInfoList = new ArrayList<>();
        if (serviceList != null) {
            for (Map.Entry<com.ficat.easyble.gatt.bean.ServiceInfo, List<CharacteristicInfo>> entry : serviceList.entrySet()) {
                MyDeviceServiceInfo myDeviceServiceInfo = new MyDeviceServiceInfo();
                com.ficat.easyble.gatt.bean.ServiceInfo serviceInfo = entry.getKey();
                myDeviceServiceInfo.setUuid(serviceInfo.uuid);
                myDeviceServiceInfo.setServiceInfo(serviceInfo);
                myDeviceServiceInfo.setCharacteristicInfoList(entry.getValue());
                serviceInfoList.add(myDeviceServiceInfo);
            }
        }
        return JsonUtil.toJson(serviceInfoList);
    }


    @JavascriptInterface
    public void notify(String mac, String serviceUuid, String notifyUuid) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return;
        }
        bleManager.notify(bleDevice, serviceUuid, notifyUuid, new BleNotifyCallback() {
            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                LogUtil.i(TAG, "ble onNotifyFail" + JsonUtil.toJson(device) + failCode);
                javaScriptFunction.evak("onNotifyFail('" + JsonUtil.toJson(device) + "')");
            }

            @Override
            public void onCharacteristicChanged(byte[] data, BleDevice device) {
                LogUtil.i(TAG, "ble onCharacteristicChanged" + HexUtil.bytesToHexString(data));
                javaScriptFunction.evak("onCharacteristicChanged('" + HexUtil.bytesToHexString(data) + "')");
            }

            @Override
            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                LogUtil.i(TAG, "ble onNotifySuccess" + notifySuccessUuid);
                javaScriptFunction.evak("onNotifySuccess('" + notifySuccessUuid + "')");
            }

        });
    }

    @JavascriptInterface
    public boolean cancelNotify(String mac, String serviceUuid, String notifyUuid) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return false;
        }
        bleManager.cancelNotify(bleDevice, serviceUuid, notifyUuid);
        return true;
    }

    @JavascriptInterface
    public void read(String mac, String serviceUuid, String readUuid) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return;
        }
        bleManager.read(bleDevice, serviceUuid, readUuid, new BleReadCallback() {
            @Override
            public void onFail(int failCode, String info, BleDevice device) {

            }

            @Override
            public void onRead(byte[] data, BleDevice device) {

            }
        });
    }

    @JavascriptInterface
    public boolean write(String mac, String serviceUuid, String writeUuid, String data) {
        BleDevice bleDevice = null;
        for (String macKey : scanResultList.keySet()) {
            if (macKey.equals(mac)) {
                bleDevice = scanResultList.get(macKey);
                break;
            }
        }
        if (null == bleDevice) {
            return false;
        }
        byte[] bytes = HexUtil.hexStringToBytes(data);
        if (null == bytes || 1 > bytes.length) {
            return false;
        }
        bleManager.write(bleDevice, serviceUuid, writeUuid, bytes, new BleWriteCallback() {
            @Override
            public void onFail(int failCode, String info, BleDevice device) {

            }

            @Override
            public void onWrite(byte[] data, BleDevice device) {

            }

        });
        return true;
    }

    @JavascriptInterface
    public String getConnectedDevices() {
        return JsonUtil.toJson(bleManager.getConnectedDevices());
    }

    @JavascriptInterface
    public boolean isConnected(String mac) {
        return bleManager.isConnected(mac);
    }
}
