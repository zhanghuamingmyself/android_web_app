package com.ztn.web.bean;

import lombok.Data;

@Data
public class FileConfig {
    private String appKey;
    private String appSecret;
    private String deviceCore;
    private Boolean isDebug = false;
}
