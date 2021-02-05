package org.bc.auto.utils;

import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.config.BlockChainAutoConstant;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.K8SException;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.PolicyV1beta1Api;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class K8SUtils {
    private static final Logger logger = LoggerFactory.getLogger(K8SUtils.class);

    private static final String API_VERSION = "v1";
    private static final String APPS_API_VERSION = "apps/v1";
    private static final String POLICY_API_VERSION = "policy/v1beta1";
    private static final String NAMESPACE_KIND = "Namespace";
    private static final String PDB_KIND = "PodDisruptionBudget";
    private static final String STATEFUL_SET_KIND = "StatefulSet";
    private static final String DEPLOYMENT_KIND = "Deployment";
    private static final String SERVICE_KIND = "Service";
    private static final String PERSISTENT_VOLUME_KIND = "PersistentVolume";
    private static final String PERSISTENT_VOLUME_CLAIM_KIND = "PersistentVolumeClaim";

    //检查K8S返回的状态常量
    private static final String CHECK_STATUS="Running";

    /**
     * 按照传入的参数，创建K8S的nameSpace
     * @param namespace
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean createNamespace(String namespace)throws BaseRuntimeException {
        V1Namespace body = new V1Namespace()
                .apiVersion(API_VERSION)
                .kind(NAMESPACE_KIND)
                .metadata(new V1ObjectMeta().name(namespace));

        try {
            new CoreV1Api().createNamespace(body, null, null, null);
            return true;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_NAMESPACE_FAIL.getMsg());
            throw new K8SException(K8SResultCode.CREATE_NAMESPACE_FAIL);
        }
    }

    /**
     * 删除指定nameSpace
     * @param namespace
     * @param coreV1Api
     * @return
     */
    public static boolean deleteNamespace(String namespace, CoreV1Api coreV1Api) {
        try {
            // 删除namespace
            V1Status v1Status = coreV1Api.deleteNamespace(namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_NAMESPACE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_NAMESPACE_ERROR);
        }
    }

    /**
     * 检查K8S内的服务状态，根据chooseType的类型（如：nameSpace、Pod等）
     * @param chooseType
     * @param nameSpace
     * @param podName
     * @return
     */
    public static boolean checkStatus(String chooseType,String nameSpace, String podName) {
        CoreV1Api coreV1Api = new CoreV1Api();

        try{
            switch (chooseType){
                case "nameSpace":{
                    for(int i=0;i<10;i++){
                        Thread.sleep(1000L);
                        V1Namespace v1Namespace = coreV1Api.readNamespace(nameSpace, null, null,null);
                        String statusString = v1Namespace.getStatus().getPhase();
                        logger.info("nameSpace状态检查，当前是第[{}]次检查,结果为[{}]",i+1,statusString);
                        if(CHECK_STATUS.equals(statusString)){
                            return true;
                        }
                    }
                    break;
                }
                case "podName": {
                    for(int i=0;i<10;i++){
                        Thread.sleep(1000L);
                        V1Pod v1Pod = coreV1Api.readNamespacedPod(nameSpace, podName,null, null,null);
                        String statusString = v1Pod.getStatus().getPhase();
                        logger.info("pod状态检查，当前是第[{}]次检查,结果为[{}]",i+1,statusString);
                        if(CHECK_STATUS.equals(statusString)){
                            return true;
                        }
                    }
                    break;
                }
                default:{
                    logger.error("请确认检查参数项是否正确，并未找到对应的检查分支");
                    break;
                }
            }
        }catch (InterruptedException e1){
            logger.error("线程执行异常，请确认是否有线程等待、线程死锁等情况");
            throw new K8SException();
        }catch (ApiException e2){
            logApiException(e2, K8SResultCode.CHECK_K8S_STATUS_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CHECK_K8S_STATUS_ERROR);
        }
        return false;
    }


    public static void createPersistentVolume(String pvName, String nfsHost, String nfsPath, String storageSize) {
        // 创建pv
        V1PersistentVolume v1PersistentVolume = new V1PersistentVolume()
                .apiVersion(API_VERSION)
                .kind(PERSISTENT_VOLUME_KIND)
                .metadata(new V1ObjectMeta().name(pvName))
                .spec(new V1PersistentVolumeSpec()
                                .capacity(new HashMap<String, Quantity>(1) {{
                                    put("storage", Quantity.fromString(storageSize));
                                }})
                                .accessModes(Arrays.asList("ReadWriteMany"))
                                .nfs(new V1NFSVolumeSource()
                                        .server(nfsHost)
                                        .path(nfsPath)
                                )
                );

        try {
            new CoreV1Api().createPersistentVolume(v1PersistentVolume, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_PV_FAIL.getMsg());
            throw new K8SException(K8SResultCode.CREATE_PV_FAIL);
        }
    }

    public static void createPersistentVolumeClaim(String namespace, String pvcName, String pvName, String storageSize) {
        // 创建pvc
        V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim()
                .apiVersion(API_VERSION)
                .kind(PERSISTENT_VOLUME_CLAIM_KIND)
                .metadata(new V1ObjectMeta().namespace(namespace).name(pvcName))
                .spec(new V1PersistentVolumeClaimSpec()
                        .accessModes(Arrays.asList("ReadWriteMany"))
                        .storageClassName("")
                        .resources(new V1ResourceRequirements()
                                .requests(new HashMap<String, Quantity>(1) {{
                                    put("storage", Quantity.fromString(storageSize));
                                }})
                        )
                        .volumeName(pvName)
                );

        try {
            new CoreV1Api().createNamespacedPersistentVolumeClaim(namespace, v1PersistentVolumeClaim, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_PVC_FAIL.getMsg());
            throw new K8SException(K8SResultCode.CREATE_PVC_FAIL);
        }
    }

    public static void createPodDisruptionBudget(String namespace, String pdbName, Map<String, String> labels, int maxUnavailable) {
        V1beta1PodDisruptionBudget v1beta1PodDisruptionBudget = new V1beta1PodDisruptionBudget()
                .apiVersion(POLICY_API_VERSION)
                .kind(PDB_KIND)
                .metadata(new V1ObjectMeta().namespace(namespace).name(pdbName))
                .spec(new V1beta1PodDisruptionBudgetSpec()
                        .selector(new V1LabelSelector().matchLabels(labels))
                        .maxUnavailable(new IntOrString(maxUnavailable)));

        // 创建PodDisruptionBudget
        try {
            new PolicyV1beta1Api().createNamespacedPodDisruptionBudget(namespace, v1beta1PodDisruptionBudget, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_POD_FAIL.getMsg());
            throw new K8SException(K8SResultCode.CREATE_POD_FAIL);
        }
    }

    public static void createStatefulSet(String namespace, String podName, String svcName, String volumeName, Map<String, String> labels, int replicas, String storageClassName, V1PodSpec spec, String storageSize) {
        // 创建StatefulSet
        V1StatefulSet v1StatefulSet = new V1StatefulSet()
                .apiVersion(APPS_API_VERSION)
                .kind(STATEFUL_SET_KIND)
                .metadata(new V1ObjectMeta().namespace(namespace).name(podName))
                .spec(new V1StatefulSetSpec()
                        .serviceName(svcName)
                        .replicas(replicas)
                        .selector(new V1LabelSelector().matchLabels(labels))
                        .updateStrategy(new V1StatefulSetUpdateStrategy().type("RollingUpdate"))
                        .podManagementPolicy("Parallel")
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(labels))
                                .spec(spec))
                        .volumeClaimTemplates(new ArrayList<V1PersistentVolumeClaim>() {{
                            add(new V1PersistentVolumeClaim()
                                    .metadata(new V1ObjectMeta().name(volumeName))
                                    .spec(new V1PersistentVolumeClaimSpec()
                                            .accessModes(Arrays.asList("ReadWriteMany"))
                                            .storageClassName(storageClassName)
                                            .resources(new V1ResourceRequirements().requests(new HashMap<String, Quantity>() {{
                                                put("storage", Quantity.fromString(storageSize));
                                            }}))));
                        }})
                );

        try {
            new AppsV1Api().createNamespacedStatefulSet(namespace, v1StatefulSet, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_STATEFUL_SET_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CREATE_STATEFUL_SET_ERROR);
        }
    }

    public static void createDeployment(String namespace, String podName, Map<String, String> labels, V1PodSpec spec) {
        // 创建deployment
        V1Deployment v1Deployment = new V1Deployment()
                .apiVersion(APPS_API_VERSION)
                .kind(DEPLOYMENT_KIND)
                .metadata(new V1ObjectMeta().namespace(namespace).name(podName))
                .spec(new V1DeploymentSpec()
                        .replicas(1)
                        .selector(new V1LabelSelector().matchLabels(labels))
                        .strategy(new V1DeploymentStrategy())
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(labels))
                                .spec(spec)
                        )
                );

        try {
            new AppsV1Api().createNamespacedDeployment(namespace, v1Deployment, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_DEPLOYMENT_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CREATE_DEPLOYMENT_ERROR);
        }
    }

    public static void createService(String namespace, String svcName, Map<String, String> labels, V1ServiceSpec spec) {
        // 创建service
        V1Service v1Service = new V1Service()
                .apiVersion(API_VERSION)
                .kind(SERVICE_KIND)
                .metadata(new V1ObjectMeta().name(svcName).namespace(namespace).labels(labels))
                .spec(spec);

        try {
            new CoreV1Api().createNamespacedService(namespace, v1Service, null, null, null);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CREATE_SERVICE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CREATE_SERVICE_ERROR);
        }
    }

    public static boolean deletePersistentVolume(String pvName) {
        try {
            V1Status v1Status = new CoreV1Api().deletePersistentVolume(pvName, null, null, null, null, null, null);

            // TODO 正常应该将pv对应的物理存储目录也一块删除，目前为了防止误删，先不做此处理，手动去删除
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_PERSISTENT_VOLUME_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_PERSISTENT_VOLUME_ERROR);
        }
    }



    public static boolean deletePersistentVolumeClaim(String pvcName, String namespace, CoreV1Api coreV1Api) {
        // 删除 pvc
        try {
            V1Status v1Status = coreV1Api.deleteNamespacedPersistentVolumeClaim(pvcName, namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_PERSISTENT_VOLUME_CLAIM_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_PERSISTENT_VOLUME_CLAIM_ERROR);
        }
    }

    public static boolean deletePodDisruptionBudget(String pdbName, String namespace, PolicyV1beta1Api policyV1beta1Api) {
        // 删除PodDisruptionBudget
        try {
            V1Status v1Status = policyV1beta1Api.deleteNamespacedPodDisruptionBudget(pdbName, namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_POD_DISRUPTION_BUDGET_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_POD_DISRUPTION_BUDGET_ERROR);
        }
    }

    public static boolean deleteStatefulSet(String podName, String namespace, AppsV1Api appsV1Api) {
        // 删除StatefulSet
        try {
            V1Status v1Status = appsV1Api.deleteNamespacedStatefulSet(podName, namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_STATEFUL_SET_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_STATEFUL_SET_ERROR);
        }
    }

    public static boolean deleteDeployment(String podName, String namespace, AppsV1Api appsV1Api) {
        // 删除deployment
        try {
            V1Status v1Status = appsV1Api.deleteNamespacedDeployment(podName, namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_DEPLOYMENT_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_DEPLOYMENT_ERROR);
        }
    }

    public static boolean deleteService(String svcName, String namespace, CoreV1Api coreV1Api) {
        // 删除service
        try {
            V1Status v1Status = coreV1Api.deleteNamespacedService(svcName, namespace, null, null, null, null, null, null);
            return v1Status.getCode() == null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_SERVICE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_SERVICE_ERROR);
        }
    }

    public static boolean cleanAll(String namespace) {
        try {
            // 删除PodDisruptionBudget
            PolicyV1beta1Api policyV1beta1Api = new PolicyV1beta1Api();
            V1beta1PodDisruptionBudgetList v1beta1PodDisruptionBudgetList = policyV1beta1Api.listNamespacedPodDisruptionBudget(namespace, null, null, null, null, null, null, null, null, null);
            for (V1beta1PodDisruptionBudget podDisruptionBudget : v1beta1PodDisruptionBudgetList.getItems()) {
                // 只要有一个PodDisruptionBudget删除没成功，就立即返回false
                if (!deletePodDisruptionBudget(podDisruptionBudget.getMetadata().getName(), namespace, policyV1beta1Api)) {
                    return false;
                }
            }

            // 删除StatefulSet
            AppsV1Api appsV1Api = new AppsV1Api();
            V1StatefulSetList v1StatefulSetList = appsV1Api.listNamespacedStatefulSet(namespace, null, null, null, null, null, null, null, null, null);
            for (V1StatefulSet v1StatefulSet : v1StatefulSetList.getItems()) {
                // 只要有一个StatefulSet删除没成功，就立即返回false
                if (!deleteStatefulSet(v1StatefulSet.getMetadata().getName(), namespace, appsV1Api)) {
                    return false;
                }
            }

            // 删除deployment
            V1DeploymentList deployList = appsV1Api.listNamespacedDeployment(namespace, null, null, null, null, null, null, null, null, null);
            for (V1Deployment deployment : deployList.getItems()) {
                // 只要有一个deployment删除没成功，就立即返回false
                if (!deleteDeployment(deployment.getMetadata().getName(), namespace, appsV1Api)) {
                    return false;
                }
            }

            CoreV1Api coreV1Api = new CoreV1Api();

            // 删除service
            V1ServiceList svcList = coreV1Api.listNamespacedService(namespace, null, null, null, null, null, null, null, null, null);
            for (V1Service svc : svcList.getItems()) {
                // 只要有一个service删除没成功，就立即返回false
                if (!deleteService(svc.getMetadata().getName(), namespace, coreV1Api)) {
                    return false;
                }
            }

            // 删除pvc
            V1PersistentVolumeClaimList pvcList = coreV1Api.listNamespacedPersistentVolumeClaim(namespace, null, null, null, null, null, null, null, null, null);
            for (V1PersistentVolumeClaim pvc : pvcList.getItems()) {
                // 只要有一个persistentVolumeClaim删除没成功，就立即返回false
                if (!deletePersistentVolumeClaim(pvc.getMetadata().getName(), namespace, coreV1Api)) {
                    return false;
                }
            }

            // 删除namespace
            if (!deleteNamespace(namespace, coreV1Api)) {
                return false;
            }

            logger.info("Namespace {} relation [deployment service persistentVolumeClaim] has been cleaned successful.", namespace);
            return true;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.DELETE_NAMESPACE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.DELETE_NAMESPACE_ERROR);
        }
    }

    public static boolean checkPodStatus(String namespace) {
        try {
            CoreV1Api coreV1Api = new CoreV1Api();

            // 最多重试20次
            final int count = 20;
            for (int i = 0; i <= count; i++) {
                // 为了避免在某些情况下pod启动较慢而导致在第一次检查时检索不到pod，因此把等待放置于检索前
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    logger.error("wait for retry -> {} check pod status error", i + 1);
                }

                V1PodList podList = coreV1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
                if (null == podList || null == podList.getItems() || podList.getItems().isEmpty()) {
                    logger.warn("Not found any pod by namespace -> {}", namespace);
                    return false;
                }

                boolean done = true;
                for (V1Pod pod : podList.getItems()) {
                    if ("Running".equalsIgnoreCase(pod.getStatus().getPhase())) {
                        logger.info("pod {} is ready and started successfully.", pod.getMetadata().getName());
                    } else {
                        done = false;
                    }
                }

                // 说明所有的pod都正常运行起来了
                if (done) {
                    if (i > 0) {
                        logger.info("All pods under namespace -> {} are successfully running when retry {}", namespace, i + 1);
                    }
                    return true;
                }
            }
            logger.warn("Not all pods under namespace -> {} are successfully running when retry {}", namespace, count);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CHECK_POD_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CHECK_POD_ERROR);
        }
        return false;
    }

    public static boolean ifAllPodsCleaned(String namespace) {
        try {
            CoreV1Api coreV1Api = new CoreV1Api();

            // 最多重试12次
            final int count = 12;
            for (int i = 0; i <= count; i++) {
                V1PodList podList = coreV1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
                if (null == podList || null == podList.getItems() || podList.getItems().isEmpty()) {
                    logger.info("Not found any pod by namespace -> {}", namespace);
                    if (i > 0) {
                        logger.info("All pods under namespace -> {} are successfully cleaned when retry {}", namespace, i + 1);
                    }
                    return true;
                }

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    logger.error("wait for retry -> {} check pod if cleaned error", i + 1);
                }
            }
            logger.warn("Not all pods under namespace -> {} are successfully cleaned when retry {}", namespace, count);
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.CLEAN_ALL_POD_ERROR.getMsg());
            throw new K8SException(K8SResultCode.CLEAN_ALL_POD_ERROR);
        }
        return false;
    }

    public static String queryClusterIp(String svcName, String namespace) {
        try {
            V1Service v1Service = new CoreV1Api().readNamespacedService(svcName, namespace, null, null, null);
            // 最长等待两分钟
            final int count = 120;
            for (int i = 0; i <= count; i++) {
                String clusterIP = v1Service.getSpec().getClusterIP();
                if (null != clusterIP && !"".equals(clusterIP)) {
                    return clusterIP;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    logger.error("wait for service -> {} ready error", svcName);
                }
            }
            logger.warn("query cluster ip for service -> {} fail.", svcName);
            return null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.READ_SERVICE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.READ_SERVICE_ERROR);
        }
    }

    public static  String queryNodeIp() {
        try {
            V1NodeList v1NodeList = new CoreV1Api().listNode(null, null, null, null, null, null, null, null, null);
            List<V1Node> nodeItems = v1NodeList.getItems();
            if (null == nodeItems || nodeItems.isEmpty()) {
                logger.error("Query kubernetes cluster node list error.");
                return null;
            }

            List<V1NodeAddress> addresses = nodeItems.get(new Random().nextInt(nodeItems.size())).getStatus().getAddresses();
            for (V1NodeAddress address : addresses) {
                // 地址类型为“InternalIP”对应的地址即为节点ip
                if ("InternalIP".equalsIgnoreCase(address.getType())) {
                    return address.getAddress();
                }
            }
            return null;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.READ_NODE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.READ_NODE_ERROR);
        }
    }

    public static Integer queryNodePort(String svcName, String namespace) {
        try {
            V1Service v1Service = new CoreV1Api().readNamespacedService(svcName, namespace, null, null, null);
            // 最长等待两分钟
            final int count = 120;
            for (int i = 0; i <= count; i++) {
                List<V1ServicePort> servicePorts = v1Service.getSpec().getPorts();
                if (null != servicePorts && !servicePorts.isEmpty()) {
                    return servicePorts.get(0).getNodePort();
                }

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    logger.error("wait for node port -> {} ready error", svcName);
                }
            }
            logger.warn("query node port for service -> {} fail.", svcName);
            return 0;
        } catch (ApiException e) {
            logApiException(e, K8SResultCode.READ_SERVICE_ERROR.getMsg());
            throw new K8SException(K8SResultCode.READ_SERVICE_ERROR);
        }
    }

    private static void logApiException(ApiException e, String logInfo){
        logger.error(logInfo);
        logger.error("Status code: " + e.getCode());
        logger.error("Reason: " + e.getResponseBody());
        logger.error("Response headers: " + e.getResponseHeaders());
    }












    public static String getPvName(String name) {
        ValidatorUtils.isNotNull(name);
        return name + BlockChainAutoConstant.PV_SUFFIX;
    }

    public static String getPvcName(String name) {
        return name + BlockChainAutoConstant.PVC_SUFFIX;
    }

    public static String getVolumeName(String name) {
        return name + BlockChainAutoConstant.VOLUME_DATA_SUFFIX;
    }

    public static String getPdbName(String name) {
        ValidatorUtils.isNotNull(name);
        return name + BlockChainAutoConstant.PDB_SUFFIX;
    }

}
