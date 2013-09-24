
/*
 Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
 This program is distributed under the terms of the GNU General Public License.

 This file is part of xFace.

 xFace is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 xFace is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with xFace.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.polyvi.xface.extension;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.polyvi.xface.view.XAppWebView;

public class XIdleWatcherExt extends CordovaPlugin {
    private static final String COMMAND_START = "start";
    private static final String COMMAND_STOP = "stop";
    //默认的等待无操作时间是5分钟，即(5*60*1000)毫秒
    private static final long DEFAULT_INTERVAL = 300000;
    //标识应用是否在后台运行
    private boolean mRunningInBackground = false;
    //用于标识应用是否切换到前台
    private int mCountNotifiedInBackground = 0;
    //用于记录等待时间
    private long mInterval = DEFAULT_INTERVAL;
    private Runnable mTask;
    private CallbackContext mCallbackCtx;
    //超时时向JS返回的字符串
    private final String TIMEOUT = "timeout";

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackCtx)
            throws JSONException {

        mCallbackCtx = callbackCtx;
        XAppWebView xAppWebView = (XAppWebView) webView;
        if (COMMAND_START.equals(action)) {
            String timeout = args.getString(0);
            if (null != timeout) {
                mInterval = Long.parseLong(timeout) * 1000;
            }
            if (mInterval < 0) {
                PluginResult r = new PluginResult(PluginResult.Status.ERROR);
                callbackCtx.sendPluginResult(r);
                return true;
            }
            mTask = new Runnable() {
                @Override
                public void run() {
                    if (isRunningInBackground()) {
                        // 处于后台状态
                        mCountNotifiedInBackground ++;
                    } else {
                        PluginResult r = new PluginResult(PluginResult.Status.OK, TIMEOUT);
                        r.setKeepCallback(true);
                        callbackCtx.sendPluginResult(r);
                    }
                }
            };
            xAppWebView.getOwnerApp().startIdleWatcher(mInterval, mTask);
        } else if (COMMAND_STOP.equals(action)) {
            xAppWebView.getOwnerApp().stopIdleWatcher();
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        result.setKeepCallback(true);
        callbackCtx.sendPluginResult(result);
        return true;
    }

    /**
     * 实时获取是否在后台状态的值
     * @return
     */
    private boolean isRunningInBackground() {
        return mRunningInBackground;
    }

    @Override
    public void onPause(boolean b) {
        //app已经运行在后台
        mRunningInBackground = true;
    }

    @Override
    public void onResume(boolean b) {
        //app切换在前台
        //程序首次启动会走这里
        mRunningInBackground = false;
        if (mCountNotifiedInBackground > 0 ) {
            mCountNotifiedInBackground = 0;
            PluginResult result = new PluginResult(PluginResult.Status.OK, TIMEOUT);
            result.setKeepCallback(true);
            mCallbackCtx.sendPluginResult(result);
            //从后台切换到前台时，从新计时
            XAppWebView xAppWebView = (XAppWebView) webView;
            xAppWebView.getOwnerApp().startIdleWatcher(mInterval, mTask);
        }
    }
}
