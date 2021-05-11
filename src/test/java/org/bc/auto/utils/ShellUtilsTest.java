package org.bc.auto.utils;

import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;

public class ShellUtilsTest {
    private static final String CMD_FILE_PATH = "/Users/work_space/bc-auto/src/main/resources/auto/script/test.sh";

    @Test
    public void testExec(){
        ShellUtils.exec(CMD_FILE_PATH);
    }

    @Test
    public void testExecEmptyPath(){
        ShellUtils.exec("");
    }
}
