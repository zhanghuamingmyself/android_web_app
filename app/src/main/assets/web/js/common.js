/**
 * 
 * 公共模块
 */
var Common = {
    checkEnv: function () {
        return typeof Common != "undefined" ? true : false
    },
    textLog: function (msg) {
        if (!Common.checkEnv) {
            throw 'env error'
        }
        common.textLog(msg)
    },
    sysInfo:function(){
        return JSON.parse(common.sysInfo())
    },
    showNativeMenu:function(){
        common.showNativeMenu()
    },
    setDebug:function(isDebug){
        common.setDebug(isDebug)
    }
}
