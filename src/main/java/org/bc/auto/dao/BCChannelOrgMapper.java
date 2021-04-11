package org.bc.auto.dao;

import org.bc.auto.model.entity.BCChannelOrg;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCChannelOrgMapper {

    int insertChannelOrg(List<BCChannelOrg> bcChannelOrgList);
}
