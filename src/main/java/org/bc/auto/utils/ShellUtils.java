package org.bc.auto.utils;

import org.apache.commons.lang.ArrayUtils;
import org.bc.auto.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtils {
    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    /**
     * 执行shell脚本的方法；根据执行的脚本来确认是否要传入对应的参数。
     * @param cmd 脚本的文可执行文件路径，如：/home/test.sh
     * @param args 执行脚本的参数列表，可选参数。执行脚本的时候可以为空，也可以不为空。
     * @return
     */
    public static boolean exec(String cmd, String... args) {
        BufferedReader infoInput = null;
        BufferedReader errorInput = null;

        logger.info("[shell->exec]脚本的执行地址是=>{}",cmd);
        boolean resultFlag = false;
        try {
            //检查脚本的执行路径是否存在，
            //路径必须存在，如果不存在在抛除异常。
            ValidatorUtils.isNotNull(cmd);

            //把脚本转化为字符串数组
            String[] execCmd = new String[]{cmd};

            //检查参数是否存在，执行脚本可以不传参数。
            //如果有参数的话，传入待执行的字符串数组中。
            if(!ValidatorUtils.isNull(args)){
                execCmd = (String[]) ArrayUtils.addAll(execCmd, args);
            }

            //定义执行脚本的对象
            Process process;
            //先根据传入的脚本路径给待执行的脚本赋权
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/chmod", "755", cmd);
            process = processBuilder.start();
            //等待赋权完成
            process.waitFor();
            //执行脚本文件
            process = Runtime.getRuntime().exec(execCmd);
            // 写出脚本执行中的过程信息
            infoInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = infoInput.readLine()) != null) {
                logger.info("[shell->exec]=>{}",line);
            }
            while ((line = errorInput.readLine()) != null) {
                logger.error("[shell->exec]=>{}",line);
            }

            // 阻塞执行线程直至脚本执行完成后返回
            int exitValue = process.waitFor();
            resultFlag = exitValue>0?false:true;
        } catch (BaseRuntimeException e4){
            logger.error("[shell->exec]执行脚本自定义异常，异常信息为:{}", e4.getMsg());
        } catch (IOException e1) {
            logger.error("[shell->exec]IO异常，异常信息为:{}", e1.getMessage());
        } catch (InterruptedException e2){
            logger.error("[shell->exec]执行脚本阻塞异常，异常信息为:{}", e2.getMessage());
        } finally {
            try{
                if(null != infoInput){
                    infoInput.close();
                }
                if(null != errorInput){
                    errorInput.close();
                }
            }catch (IOException e3){
                logger.error("[shell->exec]关闭流异常，异常信息为:{}", e3.getMessage());
            }
            return resultFlag;
        }
    }

}
