package com.zx.unique.plugin.id.postdetect;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.util.PropertiesComponent;

public class ApplicationStartUpListener implements AppLifecycleListener {

    @Override
    public void appStarted() {
        AppLifecycleListener.super.appStarted();
        PropertiesComponent applicationComponent = PropertiesComponent.getInstance();
        if ("true".equals(applicationComponent.getValue("com.zx.unique.plugin.id-autoCheck"))){
            JavaExecutePythonUtils.startCarema(false,"坐姿检测已自动启动");
        }

    }
}
