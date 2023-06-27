package com.zx.unique.plugin.id.postdetect;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @author: zx
 * @date: 2022/10/29
 */

public class PostDetectParams extends AnAction {
    public static String path = "";
    public static String focalDistance = "0";

    public static PostDetect postDetect;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前项目对象
        final Project project = e.getProject();

        // 创建GUI对象
        PostDetect postTP = new PostDetect();
        postTP.register();
        postDetect = postTP;

        // 构建对话框
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        // 设置对话框显示内容
        dialogBuilder.setCenterPanel(postTP.getRootPanel());
        PropertiesComponent applicationComponent = PropertiesComponent.getInstance();
        dialogBuilder.setTitle("坐姿检测输入参数");
        if (StringUtils.isNotEmpty(applicationComponent.getValue("com.zx.unique.plugin.id_path"))) {
            postTP.getPathTF().setText(applicationComponent.getValue("com.zx.unique.plugin.id_path"));
        }
        if (StringUtils.isNotEmpty(applicationComponent.getValue("com.zx.unique.plugin.id_focalDistance"))) {
            postTP.getFocalDistanceTF().setText(applicationComponent.getValue("com.zx.unique.plugin.id_focalDistance"));
        }

        dialogBuilder.setOkOperation(() -> {
            path = postTP.getPathTF().getText();
            focalDistance = postTP.getFocalDistanceTF().getText();
            applicationComponent.setValue("com.zx.unique.plugin.id" + "_path", path);
            applicationComponent.setValue("com.zx.unique.plugin.id" + "_focalDistance", focalDistance);
            if (StringUtils.isEmpty(path) || "0".equals(focalDistance)) {
                dialogBuilder.getDialogWrapper().close(0);
                dialogBuilder.show();
            }
            dialogBuilder.getDialogWrapper().close(0);

            if (postDetect.getAutoStartFlagComponent().isSelected()) {
                applicationComponent.setValue("com.zx.unique.plugin.id-autoCheck", "true");
            } else {
                applicationComponent.setValue("com.zx.unique.plugin.id-autoCheck", "false");
            }

            if (JavaExecutePythonUtils.proc != null) {
                JavaExecutePythonUtils.proc.destroyForcibly();
            }
        });
        // 显示对话框
        dialogBuilder.show();

    }
}
