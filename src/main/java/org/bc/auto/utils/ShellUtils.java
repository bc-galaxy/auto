package org.bc.auto.utils;

import org.apache.commons.lang.ArrayUtils;
import org.bc.auto.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ShellUtils {
    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    /**
     * 执行shell脚本的方法；根据执行的脚本来确认是否要传入对应的参数。
     * @param cmd 脚本的文可执行文件路径，如：/home/test.sh
     * @param args 执行脚本的参数列表，可选参数。执行脚本的时候可以为空，也可以不为空。
     * @return
     */
    public static boolean exec(String cmd, String... args) {
        String tip = (null == cmd || "".equals(cmd)) ? Arrays.toString(args) : cmd;
        try {
            // 启动独立线程等待process执行完成
            CommandWaitForThread commandThread = new CommandWaitForThread(cmd, args);
            commandThread.start();

            while (!commandThread.isFinish()) {
                logger.info("shell {} has not been executed finish. system will auto detect again after 1 seconds.", tip);
                Thread.sleep(1000L);
            }

            // 检查脚本执行结果状态码
            int res = commandThread.getExitValue();
            if (res == 0) {
                logger.info("shell script: {} execute successful, exitValue = {}", tip, res);
                return true;
            }
            logger.error("shell script: {} execute failed, exitValue = {}", tip, res);
            return false;
        } catch (Exception e) {
            logger.error("shell script: {} execute failed because of: {}", tip, e.getMessage());
            return false;
        }
    }

}
