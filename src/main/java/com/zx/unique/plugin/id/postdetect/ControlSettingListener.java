package com.zx.unique.plugin.id.postdetect;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class ControlSettingListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        ProjectManagerListener.super.projectClosing(project);
        //关闭摄像头
        if (JavaExecutePythonUtils.proc != null){
            JavaExecutePythonUtils.proc.destroyForcibly();
        }
    }
}
