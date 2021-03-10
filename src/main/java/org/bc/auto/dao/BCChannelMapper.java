package org.bc.auto.dao;

import org.bc.auto.model.entity.BCChannel;
import org.springframework.stereotype.Component;

@Component
public interface BCChannelMapper {

    int insertChannel(BCChannel bcChannel);
}
