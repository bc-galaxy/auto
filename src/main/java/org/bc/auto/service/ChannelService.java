package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;

public interface ChannelService {

    void createChannel(JSONObject jsonObject)throws BaseRuntimeException;

}
