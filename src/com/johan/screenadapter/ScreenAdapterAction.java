package com.johan.screenadapter;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScreenAdapterAction extends AnAction {

    // 屏幕分辨率
    private static final int[] SCREENS = {
            240, 320, 360, 400, 480, 540, 600
    };

    // 项目
    private Project project;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getProject();
        showScreenListPop(event.getDataContext());
    }

    /**
     * 显示选择原始屏幕尺寸
     */
    private void showScreenListPop(DataContext context) {
        ActionGroup actionGroup = new ActionGroup() {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                AnAction[] actions = new AnAction[SCREENS.length];
                for (int i = 0; i < SCREENS.length; i++) {
                    final int screen = SCREENS[i];
                    actions[i] = new AnAction("----- " + String.valueOf(screen) + " -----") {
                        @Override
                        public void actionPerformed(AnActionEvent anActionEvent) {
                            createAllDimensFile(screen);
                        }
                    };
                }
                return actions;
            }
        };
        ListPopup listPopup = JBPopupFactory.getInstance().createActionGroupPopup("选择原始屏幕尺寸", actionGroup, context, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false);
        listPopup.showInFocusCenter();
    }

    /**
     * 创建所有的dimens文件
     * @param screen
     */
    private void createAllDimensFile(int screen) {
        File projectDirectory = new File(project.getBasePath());
        List<File> dimensFileList = new ArrayList<>();
        findAllDimensFile(projectDirectory, dimensFileList);
        for (File dimensFile : dimensFileList) {
            createDimensFile(dimensFile, screen);
        }
    }

    /**
     * 查找所有的dimens文件
     * @param directory
     * @param dimensFileList
     */
    private void findAllDimensFile(File directory, List<File> dimensFileList) {
        if (!directory.isDirectory()) return;
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                findAllDimensFile(file, dimensFileList);
                continue;
            }
            if (!file.getParentFile().getName().equals("values")) continue;
            if (file.getName().equals("dimens.xml")) {
                dimensFileList.add(file);
            }
        }
    }

    /**
     * 创建dimens文件
     * @param dimensFile
     * @param screen
     */
    private void createDimensFile(File dimensFile, int screen) {
        BufferedReader reader = null;
        StringBuilder sw240Builder = new StringBuilder();
        StringBuilder sw320Builder = new StringBuilder();
		StringBuilder sw360Builder = new StringBuilder();
        StringBuilder sw400Builder = new StringBuilder();
        StringBuilder sw480Builder = new StringBuilder();
        StringBuilder sw540Builder = new StringBuilder();
        StringBuilder sw600Builder = new StringBuilder();
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(dimensFile),"UTF-8");
            reader = new BufferedReader(read);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("</dimen>")) {
                    int startIndex = line.indexOf(">") + 1;
                    int endIndex = line.lastIndexOf("</dimen>") - 2;
                    String start = line.substring(0,  startIndex);
                    String end = line.substring(endIndex);
                    float data = Float.parseFloat(line.substring(startIndex, endIndex));
                    float sw240Data = data * 240 / screen;
                    float sw320Data = data * 320 / screen;
					float sw360Data = data * 360 / screen;
                    float sw400Data = data * 400 / screen;
                    float sw480Data = data * 480 / screen;
                    float sw540Data = data * 540 / screen;
                    float sw600Data = data * 600 / screen;
                    sw240Builder.append(start).append(formatData(sw240Data)).append(end).append("\n");
                    sw320Builder.append(start).append(formatData(sw320Data)).append(end).append("\n");
					sw360Builder.append(start).append(formatData(sw360Data)).append(end).append("\n");
                    sw400Builder.append(start).append(formatData(sw400Data)).append(end).append("\n");
                    sw480Builder.append(start).append(formatData(sw480Data)).append(end).append("\n");
                    sw540Builder.append(start).append(formatData(sw540Data)).append(end).append("\n");
                    sw600Builder.append(start).append(formatData(sw600Data)).append(end).append("\n");
                } else {
                    sw240Builder.append(line).append("\n");
                    sw320Builder.append(line).append("\n");
					sw360Builder.append(line).append("\n");
                    sw400Builder.append(line).append("\n");
                    sw480Builder.append(line).append("\n");
                    sw540Builder.append(line).append("\n");
                    sw600Builder.append(line).append("\n");
                }
            }
            reader.close();
            String resPath = dimensFile.getParentFile().getParent();
            String sw240Path = resPath + "/values-sw240dp";
            String sw320Path = resPath + "/values-sw320dp";
			String sw360Path = resPath + "/values-sw360dp";
            String sw400Path = resPath + "/values-sw400dp";
            String sw480Path = resPath + "/values-sw480dp";
            String sw540Path = resPath + "/values-sw540dp";
            String sw600Path = resPath + "/values-sw600dp";
            createDir(sw240Path);
            createDir(sw320Path);
			createDir(sw360Path);
            createDir(sw400Path);
            createDir(sw480Path);
            createDir(sw540Path);
            createDir(sw600Path);
            writeFile(sw240Path, sw240Builder.toString());
            writeFile(sw320Path, sw320Builder.toString());
			writeFile(sw360Path, sw360Builder.toString());
            writeFile(sw400Path, sw400Builder.toString());
            writeFile(sw480Path, sw480Builder.toString());
            writeFile(sw540Path, sw540Builder.toString());
            writeFile(sw600Path, sw600Builder.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notifyCreatedFail();
        } catch (IOException e) {
            e.printStackTrace();
            notifyCreatedFail();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            notifyCreatedComplete();
        }
    }

    /**
     * 格式化数字
     * @param data
     * @return
     */
    private static String formatData(float data) {
        if (data * 10 % 10 == 0) {
            return String.valueOf((int) data);
        } else {
            return String.valueOf((float) (Math.round(data*10))/10);
        }
    }

    /**
     * 创建文件夹
     * @param fileDir
     */
    private static void createDir(String fileDir) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 创建文件
     * @param filePath
     * @param content
     */
    private static void writeFile(String filePath, String content) {
        BufferedWriter writer = null;
        try {
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filePath + "/dimens.xml"), "UTF-8");
            writer = new BufferedWriter(write);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 通知创建失败
     */
    private void notifyCreatedFail() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("各个屏幕尺寸的dimens.xml文件自动创建失败", MessageType.ERROR, null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.above);
    }

    /**
     * 通知创建完成
     */
    private void notifyCreatedComplete() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("各个屏幕尺寸的dimens.xml文件自动创建完成", MessageType.INFO, null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.above);
    }

}
