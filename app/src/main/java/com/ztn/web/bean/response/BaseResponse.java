package com.ztn.web.bean.response;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Data;

/**
 * @Author:zhm
 * @Date:2019/8/28 18:28
 */
@Data
public class BaseResponse<T> {

    private static String serverAddress;

    static {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
            serverAddress = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 基础返回码code
     */
    public final static String SUCCESS_CODE = "200";//成功
    public final static String DATANULL_CODE = "201";//内容空（查询空，添加失败，删除失败）
    public final static String ERROR_CODE = "501";//服务端出错
    public final static String PARAM_ERROR_CODE = "402";//参数错误
    public final static String POWER_ERROR_CODE = "403";//参数错误
    public final static String DEVICE_ERROR_CODE = "502";//设备返回错误
    public final static String LOGOUT_CODE = "401";//登录超时

    /**
     * code == "502"
     * 设备返回错误码code2
     */
    public final static String SETTING_TOFAST_CODE = "602";//操作快过设备设置的最快反应时间
    public final static String DEVICE_ONONLINE_CODE = "603";//设备不在线
    public final static String DEVICE_OUTIME_CODE = "604";//设备超时
    public final static String SERVER_NOSUPPOSE_CODE = "606";//服务器不支持该功能

    public final static String DEVICE_PORT_WORRY_CODE = "1";//端口错误
    public final static String DEVICE_SETTING_WORRY_CODE = "2";//命令错误
    public final static String DEVICE_PARAM_WORRY_CODE = "3";//参数错误
    public final static String DEVICE_SETTING_ERROR_CODE = "4";//设置失败
    public final static String DEVICE_HARD_ERROR_CODE = "5";//硬件不支持

    public static String DEVICE_LINKAGE_ERROR = "0";

    private Boolean status;
    private String code;
    private String msg;
    private T data;
    private String code2;
    private String address = serverAddress;


    public BaseResponse(Boolean status, String code, String msg, T data, String code2) {
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.code2 = code2;
    }

    public static BaseResponse success(Object object) {
        BaseResponse baseResponse = new BaseResponse<>(true,SUCCESS_CODE,"success",object,SUCCESS_CODE);
        return baseResponse;
    }

    /**
     * 有效数据为空或空数组
     *
     * @return
     */
    public static BaseResponse dataNull(String msg) {
        BaseResponse baseResponse = new BaseResponse<>(true,DATANULL_CODE,msg,null,DATANULL_CODE);
        return baseResponse;
    }


    public static BaseResponse error(String msg) {
        BaseResponse baseResponse = new BaseResponse<>(false,ERROR_CODE,msg,null,ERROR_CODE);
        return baseResponse;
    }

    public static BaseResponse paramError(String param) {
        BaseResponse baseResponse = new BaseResponse<>(false,PARAM_ERROR_CODE,"invalid parameter "+param,null,PARAM_ERROR_CODE);
        return baseResponse;
    }


    public static BaseResponse deviceError(String code2, String msg) {
        BaseResponse baseResponse = new BaseResponse<>(false,DEVICE_ERROR_CODE,msg,null,code2);
        return baseResponse;
    }

    public static BaseResponse loginout() {
        BaseResponse baseResponse = new BaseResponse<>(false,LOGOUT_CODE,"loginout",null,LOGOUT_CODE);
        return baseResponse;
    }

    public static BaseResponse outPower() {
        BaseResponse baseResponse = new BaseResponse<>(false,POWER_ERROR_CODE,"you are no allow",null,POWER_ERROR_CODE);
        return baseResponse;
    }

}
