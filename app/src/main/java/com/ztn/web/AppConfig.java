/*
 * Copyright © 2019 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ztn.web;

import android.os.Environment;

import java.io.File;

/**
 * Created by Zhenjie Yan on 2019-06-30.
 */

public class AppConfig {
    public static final Boolean IS_LOG = true;
    public static final String BASE_PATH = Environment.getExternalStoragePublicDirectory("") + File.separator + AppConfig.class.getPackage().getName() + File.separator;
    public static final String WEBSITE_PATH = BASE_PATH + "web";
    public static final String CONFIG_PATH = BASE_PATH + "config" + File.separator;
    public static final String ACTIVITY_PATH = WEBSITE_PATH + File.separator + "activity" + File.separator;
    public static final String DEFAULT_APP_PATH = "file:///android_asset/web/index.html";
//    public static final String CENTER_SERVER_BASE_URL = "http://192.168.0.109:7777/";
    public static final String CENTER_SERVER_BASE_URL = "http://app.ztn-tech.com:7777/";

}