package com.ztn.web;

import android.app.Application;

import com.google.gson.Gson;
import com.liulishuo.filedownloader.FileDownloader;
import com.ztn.web.bean.FileConfig;

import lombok.NonNull;

public class App extends Application {

	private static App mInstance;
	private Gson gson;
	private FileConfig fileConfig;


	@Override
	public void onCreate() {
		super.onCreate();
		FileDownloader.init(this.getApplicationContext());
		if (mInstance == null) {
			mInstance = this;
			initGson();
		}
	}

	@NonNull
	public static App getInstance() {
		return mInstance;
	}

	private void initGson() {
		gson = new Gson();
	}

	public Gson getGson() {
		return gson;
	}

	public void setFileConfig(FileConfig fileConfig) {
		this.fileConfig = fileConfig;
	}

	public FileConfig getFileConfig() {
		return fileConfig;
	}

}
