package com.zx.unique.plugin.id.postdetect;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.ui.MessageType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;

/**
 * @author: zx
 * @date: 2022/10/11
 */
public class JavaExecutePythonUtils {
    public static Process proc;
    private static String pythonFileName = "post-detect.py";

    /**
     * 解压jar包
     *
     * @param jarpath
     * @param targetDir
     */
    public static void UnAllFile(String jarpath, String targetDir) {
        if (jarpath == null || targetDir == null) {
            throw new NullPointerException("参数为空");
        }
        try {
            File file = new File(jarpath);
            JarFile jar = new JarFile(file);
            // fist get all directories,
            // then make those directory on the destination Path
            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) enums.nextElement();
                String fileName = targetDir + File.separator + entry.getName();
                File f = new File(fileName);
                if (fileName.endsWith("/")) {
                    f.mkdirs();
                }
            }
            //now create all files
            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) enums.nextElement();
                String fileName = targetDir + File.separator + entry.getName();
                File f = new File(fileName);
                if (!fileName.endsWith("/")) {
                    InputStream is = jar.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(f);
                    // write contents of 'is' to 'fos'
                    while (is.available() > 0) {
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                }
            }
        } catch (JarException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startCarema(Boolean isCheck, String tip) {
        try {
            PluginId pluginId = PluginId.getId("com.zx.unique.plugin.id");
            IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginId);
            File path = plugin.getPath();
            String pluginPath = path.getAbsolutePath();
            if (System.getProperty("os.name").startsWith("Windows")) {

                pluginPath += "\\lib\\" + "instrumented-" + "post-detect.jar";
            }
            JavaExecutePythonUtils.executePython(pluginPath, isCheck);
        } catch (Exception execp) {
            System.out.println(execp);
        }

        NotificationGroup notificationGroup = new NotificationGroup("action_switch_id", NotificationDisplayType.TOOL_WINDOW, false);
        Notification notification = notificationGroup.createNotification(tip, MessageType.INFO);
        Notifications.Bus.notify(notification);
    }

    public static void executePython(String path, Boolean isCheck) {

        try {
            String pluginPath = path.replace(".jar", "");
            File folder = new File(pluginPath);
            if (!folder.exists() && !folder.isDirectory()) {
                JavaExecutePythonUtils.UnAllFile(path, pluginPath);
            }
            PropertiesComponent applicationComponent = PropertiesComponent.getInstance();
            PostDetectParams.path = applicationComponent.getValue("com.zx.unique.plugin.id_path");
            PostDetectParams.focalDistance = applicationComponent.getValue("com.zx.unique.plugin.id_focalDistance");

            String[] args = null;
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                args = new String[]{PostDetectParams.path, pluginPath + File.separator + JavaExecutePythonUtils.pythonFileName, PostDetectParams.focalDistance};
            } else if (osName.startsWith("Windows")) {
                args = new String[]{PostDetectParams.path, pluginPath + File.separator + "META-INF" + File.separator + JavaExecutePythonUtils.pythonFileName, PostDetectParams.focalDistance};
            } else {
                // linux或其他系统
            }
            if (proc != null) {
                proc.destroyForcibly();
            }
            if (isCheck) {
                String[] newArgs = new String[args.length + 1];
                for (int i = 0; i < args.length; i++) {
                    newArgs[i] = args[i];
                }
                newArgs[3] = "True";
                proc = Runtime.getRuntime().exec(newArgs);// 执行py文件
            } else {
                String[] newArgs = new String[args.length + 1];
                for (int i = 0; i < args.length; i++) {
                    newArgs[i] = args[i];
                }
                newArgs[3] = "False";
                proc = Runtime.getRuntime().exec(newArgs);// 执行py文件
            }

            new Thread(new OutputHandlerRunnable(proc.getInputStream()),"input").start();
            new Thread(new OutputHandlerRunnable(proc.getErrorStream()),"error").start();

            ProjectManager.getInstance().addProjectManagerListener(new VetoableProjectManagerListener() {
                @Override
                public boolean canClose(@NotNull Project project) {
                    proc.destroyForcibly();
                    return true;
                }
            });
        } catch (Exception execp) {
            System.out.println(execp);
            throw new RuntimeException("执行python脚本异常:" + execp.getMessage());
        }
    }

    private static class OutputHandlerRunnable implements Runnable {
        private InputStream in;

        public OutputHandlerRunnable(InputStream inputStream) {
            this.in = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader bufr = new BufferedReader(new InputStreamReader(this.in))) {
                String line = null;
                while ((line = bufr.readLine()) != null) {
                    System.out.println("来自python脚本：" + line);
                    if (line.contains("write buffer to send content")) {
                        NotificationGroup notificationGroup = new NotificationGroup("action_switch_id", NotificationDisplayType.TOOL_WINDOW, false);
                        Notification notification = notificationGroup.createNotification("请调整坐姿哦", MessageType.INFO);
                        Notifications.Bus.notify(notification);
                    }

                    if (line.contains("focalDistance")) {
                        PostDetectParams.postDetect.getFocalDistanceTF().setText(line.replace("focalDistance", ""));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
