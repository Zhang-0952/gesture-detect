package com.zx.unique.plugin.id.postdetect;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author: zx
 * @date: 2022/10/20
 *
 */
public class SwitchAction extends AnAction {
    public static String pluginPath = "";

    @Override
    public void actionPerformed(AnActionEvent e) {
        JavaExecutePythonUtils.startCarema(false,"坐姿检测开启");
    }
}
