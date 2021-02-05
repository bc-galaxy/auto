package org.bc.auto.dao;

import org.bc.auto.model.entity.BCCluster;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCClusterMapper {

    int insertCluster(BCCluster bcCluster);

    List<BCCluster> getAllCluster();

}
