/**
 * 
 * ble模块
 */
var BleManager = {
    'status': false,
    'scanResultList': [],
    onScanFinished: null,
    onConnectFail: null,
    onConnectSuccess: null,
    onDisConnected: null,
    onCharacteristicChanged: null,
    enableBluetooth: function (onScanFinished) {
        ble.enableBluetooth()
        BleManager.onScanFinished = onScanFinished
    },
    isSupportBle: function () {
        return ble.isSupportBle()
    },
    isBlueEnable: function () {
        return ble.isBlueEnable()
    },
    cancelScan: function () {
        return ble.cancelScan()
    },
    connect: function (mac, onConnectSuccess, onConnectFail, onDisConnected) {
        BleManager.onConnectSuccess = onConnectSuccess
        BleManager.onConnectFail = onConnectFail
        BleManager.onDisConnected = onDisConnected
        ble.connect(mac)
        return BleManager
    },
    setOnConnectSuccess: function (onConnectSuccess) {
        BleManager.onConnectSuccess = onConnectSuccess
        return BleManager
    },
    setOnConnectFail: function (onConnectFail) {
        BleManager.onConnectFail = onConnectFail
        return BleManager
    },
    setOnDisConnected: function (onDisConnected) {
        BleManager.onDisConnected = onDisConnected
        return BleManager
    },
    listService: function (mac) {
        var serviceList = ble.listService(mac)
        // console.log("listService"+serviceList)
        return JSON.parse(serviceList)
    }, 
    notify: function (mac, serviceUuid, notifyUuid, onCharacteristicChanged) {
        ble.notify(mac, serviceUuid, notifyUuid)
        BleManager.onCharacteristicChanged = onCharacteristicChanged
    },
    write:function(mac, serviceUuid, writeUuid,data){
        ble.write(mac,serviceUuid,writeUuid,data)
    }

}

function onScanStarted(success) {
    // console.info("ble onScanStarted " + success)
    if (success) {
        BleManager.status = true
    } else {
        BleManager.status = false
    }
}

function onScanning(bleDevice) {
    // console.info("ble onScanning " + bleDevice)
    var bleDevice = JSON.parse(bleDevice)
}

function onScanFinished(scanResultList) {
    // console.info("ble onScanFinished " + scanResultList)
    BleManager.scanResultList = JSON.parse(scanResultList)
    BleManager.onScanFinished(BleManager.scanResultList)
}
function onConnectFail(bleDevice) {
    // console.info("ble onConnectFail " + bleDevice)
    BleManager.onConnectFail(JSON.parse(bleDevice), 'exception')
}

function onConnectSuccess(bleDevice) {
    // console.info("ble onConnectSuccess " + bleDevice)
    BleManager.onConnectSuccess(JSON.parse(bleDevice), 'status')
}

function onDisConnected(bleDevice) {
    // console.info("ble onDisConnected " + bleDevice)
    BleManager.onDisConnected(JSON.parse(bleDevice), 'isSelf')
}
function onCharacteristicChanged(data) {
    console.log("onCharacteristicChanged " + data)
    BleManager.onCharacteristicChanged(data)
}
