package org.bc.auto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockChainAutoConstant {

    //pv存储挂载后缀
    public static String PV_SUFFIX ;
    //pvc存储挂载后缀
    public static String PVC_SUFFIX ;
    //VOLUME_DATA后缀
    public static String VOLUME_DATA_SUFFIX;
    //podDisruptionBudget后缀
    public static String PDB_SUFFIX;
    //nfs绑定的地址
    public static String NFS_HOST;
    //nfs绑定的路径
    public static String NFS_PATH;
    //K8S work path
    public static String K8S_WORK_PATH;

    //K8S work path
    public static String K8S_DATA_PATH;

    //设置根MSP,CA的名称
    public static String MSP_CA_NAME;

    //设置TLS，CA的名称
    public static String TLS_CA_NAME;

    /**
     * 需要绑定的nfsHost的值
     * @param nfsHost
     */
    @Value("${bc-auto.nfs-host}")
    public void setNfsHost(String nfsHost){
        BlockChainAutoConstant.NFS_HOST=nfsHost;
    }

    /**
     * 需要绑定的nfsHost的值
     * @param nfsPath
     */
    @Value("${bc-auto.nfs-path}")
    public void setNfsPath(String nfsPath){
        BlockChainAutoConstant.NFS_PATH=nfsPath;
    }

    /**
     * 需要绑定的pv后缀的的值
     * @param pvSuffix
     */
    @Value("${bc-auto.pv-suffix}")
    public void setPvSuffix(String pvSuffix){
        BlockChainAutoConstant.PV_SUFFIX=pvSuffix;
    }

    /**
     * 需要绑定的pv后缀的的值
     * @param pvcSuffix
     */
    @Value("${bc-auto.pvc-suffix}")
    public void setPvcSuffix(String pvcSuffix){
        BlockChainAutoConstant.PVC_SUFFIX=pvcSuffix;
    }

    /**
     * 需要绑定的volumeData后缀的的值
     * @param volumeDataSuffix
     */
    @Value("${bc-auto.volume-data-suffix}")
    public void setVolumeDataSuffix(String volumeDataSuffix){
        BlockChainAutoConstant.VOLUME_DATA_SUFFIX=volumeDataSuffix;
    }

    /**
     * 需要绑定的podDisruptionBudget后缀的值
     * @param pdbSuffix
     */
    @Value("${bc-auto.pdb-suffix}")
    public void setPdbSuffix(String pdbSuffix){
        BlockChainAutoConstant.PDB_SUFFIX=pdbSuffix;
    }

    /**
     *
     * @param k8sWorkPath
     */
    @Value("${bc-auto.k8s-work-path}")
    public void setK8sWorkPath(String k8sWorkPath){
        BlockChainAutoConstant.K8S_WORK_PATH=k8sWorkPath;
    }

    @Value("${bc-auto.msp-ca-name}")
    public void setMspCaName(String mspCaName){
        BlockChainAutoConstant.MSP_CA_NAME=mspCaName;
    }

    @Value("${bc-auto.tls-ca-name}")
    public void setTlsCaName(String tlsCaName){
        BlockChainAutoConstant.TLS_CA_NAME=tlsCaName;
    }

    @Value("${bc-auto.k8s-data-path}")
    public void setK8sDataPath(String k8sDataPath){
        BlockChainAutoConstant.K8S_DATA_PATH=k8sDataPath;
    }
}
