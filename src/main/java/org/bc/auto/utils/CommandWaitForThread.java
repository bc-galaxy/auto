package org.bc.auto.utils;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandWaitForThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(CommandWaitForThread.class);
    private String cmd;
    private String[] args;
    private boolean finish = false;
    private int exitValue = -1;

    public CommandWaitForThread(String[] args) {
        this.args = args;
    }

    public CommandWaitForThread(String cmd, String... args) {
        this.cmd = cmd;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            Process process;
            if (null != cmd && !"".equals(cmd)) {
                // 将shell命令或脚本 和参数放到一个数组中，然后将数组传入exec()方法，目的：避免传递的参数字符串中包含空格时，会将参数截断，默认为参数只到空格处
                String[] execCmd = new String[]{cmd};
                execCmd = (String[]) ArrayUtils.addAll(execCmd, args);
                logger.info("execCmd ---> {}", execCmd);

                // 解决脚本没有执行权限
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/chmod", "755", cmd);
                process = processBuilder.start();
                process.waitFor();

                // 执行脚本并等待脚本执行完成
                process = Runtime.getRuntime().exec(execCmd);
            } else {
                process = Runtime.getRuntime().exec(args);
            }

            // 写出脚本执行中的过程信息
            BufferedReader infoInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = infoInput.readLine()) != null) {
                logger.info(line);
            }
            while ((line = errorInput.readLine()) != null) {
                logger.info(line);
            }
            infoInput.close();
            errorInput.close();

            // 阻塞执行线程直至脚本执行完成后返回
            this.exitValue = process.waitFor();
        } catch (
                Throwable e) {
            logger.error("CommandWaitForThread occur exception, shell " + cmd, e);
            exitValue = 110;
        } finally {
            finish = true;
        }

    }

    public boolean isFinish() {
        return finish;
    }

    public int getExitValue() {
        return exitValue;
    }

}
