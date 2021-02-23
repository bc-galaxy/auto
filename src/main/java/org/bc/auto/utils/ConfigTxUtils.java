package org.bc.auto.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigTxUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigTxUtils.class);

    private static final String SPACE = "    ";

    public static boolean generateSysConfigTxYaml(int consensusCode, String ordererOrgName, String ordererOrgMspId, String ordererOrgMspDir, List<String> ordererAddressList, List<String> kafkaAddressList, List<Map<String, Object>> raftConsensus, String yamlFilePath) {
        StringBuilder content = constructOrdererOrganization(ordererOrgName, ordererOrgMspId, ordererOrgMspDir).append(constructOrdererCapabilities());
        // 根据共识类型生成Orderer 共识部分
        switch (consensusCode) {
            case 1:
                content.append(constructOrdererForSolo());
                break;
            case 2:
                content.append(constructOrdererForKafka(kafkaAddressList));
                break;
            case 3:
                content.append(constructOrdererForRaft(raftConsensus));
                break;
            default:
                logger.error("Invalid consensus type. currently only support 'solo' or 'kafka' or 'raft'.");
                return false;
        }
        content.append(constructOrderer(ordererAddressList))
                .append(constructChannel())
                .append(constructGenesisProfiles(Arrays.asList(ordererOrgName))).toString();

        try {
            File file = new File(yamlFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream outStream = new FileOutputStream(file);
            outStream.write(content.toString().getBytes());
            outStream.close();
            return true;
        } catch (Exception e) {
            logger.error("Generate configtx.yaml for genesis block error: {}", e.getMessage());
            return false;
        }
    }

    private static StringBuilder constructOrdererOrganization(String ordererOrgName, String ordererOrgMspId, String ordererOrgMspDir) {
        return new StringBuilder().append("Organizations:\n")
                .append(SPACE).append("- &").append(ordererOrgName).append("\n")
                .append(SPACE).append(SPACE).append("Name: ").append(ordererOrgName).append("\n")
                .append(SPACE).append(SPACE).append("ID: ").append(ordererOrgMspId).append("\n")
                .append(SPACE).append(SPACE).append("MSPDir: ").append(ordererOrgMspDir).append("\n")
                .append(SPACE).append(SPACE).append("Policies:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Readers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(ordererOrgMspId).append(".member')\"\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Writers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(ordererOrgMspId).append(".member')\"\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Admins:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(ordererOrgMspId).append(".admin')\"\n");
    }


    private static StringBuilder constructOrdererCapabilities() {
        return new StringBuilder().append("\nCapabilities:\n")
                .append(SPACE).append("Channel: &ChannelCapabilities\n")
                .append(SPACE).append(SPACE).append("V1_3: true\n")
                .append(SPACE).append("Orderer: &OrdererCapabilities\n")
                .append(SPACE).append(SPACE).append("V1_1: true\n")
                .append(SPACE).append(SPACE).append("V1_4_2: true\n");
    }

    private static StringBuilder constructOrdererForSolo() {
        return new StringBuilder("\nOrderer: &OrdererDefaults\n")
                .append(SPACE).append("OrdererType: solo\n");
    }

    private static StringBuilder constructOrdererForKafka(List<String> kafkaAddressList) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nOrderer: &OrdererDefaults\n")
                .append(SPACE).append("OrdererType: kafka\n")
                .append(SPACE).append("Kafka:\n")
                .append(SPACE).append(SPACE).append("Brokers:\n");
        for (String address : kafkaAddressList) {
            sb.append(SPACE).append(SPACE).append(SPACE).append("- ").append(address).append("\n");
        }

        return sb;
    }

    private static StringBuilder constructOrdererForRaft(List<Map<String, Object>> raftConsensus) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nOrderer: &OrdererDefaults\n")
                .append(SPACE).append("OrdererType: etcdraft\n")
                .append(SPACE).append("EtcdRaft:\n")
                .append(SPACE).append(SPACE).append("Consenters:\n");
        for (Map<String, Object> consensus : raftConsensus) {
            for (String key : consensus.keySet()) {
                sb.append(SPACE).append(SPACE).append(SPACE).append("Host".equalsIgnoreCase(key) ? "- " : "  ").append(key).append(": ").append(consensus.get(key)).append("\n");
            }
        }

        return sb;
    }

    private static StringBuilder constructOrderer(List<String> ordererAddressList) {
        StringBuilder sb = new StringBuilder();
        sb.append(SPACE).append("Addresses:\n");
        for (String address : ordererAddressList) {
            sb.append(SPACE).append(SPACE).append("- ").append(address).append("\n");
        }

        return sb.append(SPACE).append("BatchTimeout: 2s\n")
                .append(SPACE).append("BatchSize:\n")
                .append(SPACE).append(SPACE).append("MaxMessageCount: 10\n")
                .append(SPACE).append(SPACE).append("AbsoluteMaxBytes: 98 MB\n")
                .append(SPACE).append(SPACE).append("PreferredMaxBytes: 512 KB\n\n")
                .append(SPACE).append("Policies:\n")
                .append(SPACE).append(SPACE).append("Readers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Readers\"\n")
                .append(SPACE).append(SPACE).append("Writers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Writers\"\n")
                .append(SPACE).append(SPACE).append("Admins:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Admins\"\n")
                .append(SPACE).append(SPACE).append("BlockValidation:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Writers\"\n")
                .append(SPACE).append("Capabilities:\n")
                .append(SPACE).append(SPACE).append("<<: *OrdererCapabilities\n");
    }

    private static StringBuilder constructChannel() {
        return new StringBuilder().append("\nChannel: &ChannelDefaults\n")
                .append(SPACE).append("Policies:\n")
                .append(SPACE).append(SPACE).append("Readers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Readers\"\n")
                .append(SPACE).append(SPACE).append("Writers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Writers\"\n")
                .append(SPACE).append(SPACE).append("Admins:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Admins\"\n")
                .append(SPACE).append("Capabilities:\n")
                .append(SPACE).append(SPACE).append("<<: *ChannelCapabilities");
    }

    private static StringBuilder constructGenesisProfiles(List<String> ordererOrgNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nProfiles:\n")
                .append(SPACE).append("TwoOrgsOrdererGenesis:\n")
                .append(SPACE).append(SPACE).append("<<: *ChannelDefaults\n")
                .append(SPACE).append(SPACE).append("Capabilities:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("<<: *ChannelCapabilities\n")
                .append(SPACE).append(SPACE).append("Orderer:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("<<: *OrdererDefaults\n\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Organizations:\n");

        for (String ordererOrgName : ordererOrgNameList) {
            sb.append(SPACE).append(SPACE).append(SPACE).append("- *").append(ordererOrgName).append("\n");
        }

        sb.append(SPACE).append(SPACE).append(SPACE).append("Capabilities:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("<<: *OrdererCapabilities\n")
                .append(SPACE).append(SPACE).append("Consortiums:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("SampleConsortium:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Organizations:\n");
        return sb;
    }

    public static boolean generateAppConfigTxYaml(JSONArray arr, String yamlFilePath) {
        if (null == arr || arr.isEmpty()) {
            logger.error("org info is invalid: must not be null or empty.");
            return false;
        }

        StringBuilder sb = new StringBuilder("Organizations:\n");

        List<String> orgNameList = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            orgNameList.add(jo.getString("orgName"));
            sb.append(constructOrganization(jo.getString("orgName"), jo.getString("orgMspId"), jo.getString("orgMspDir")));
        }

        sb.append(constructCapabilities()).append(constructApplication()).append(constructChannelProfiles(orgNameList));

        try {
            File file = new File(yamlFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream outStream = new FileOutputStream(file);
            outStream.write(sb.toString().getBytes());
            outStream.close();
            return true;
        } catch (Exception e) {
            logger.error("Generate configtx.yaml for application channel error: {}", e.getMessage());
            return false;
        }
    }

    private static StringBuilder constructOrganization(String orgName, String orgMspId, String orgMspDir) {
        return new StringBuilder().append(SPACE).append("- &").append(orgName).append("\n")
                .append(SPACE).append(SPACE).append("Name: ").append(orgMspId).append("\n")
                .append(SPACE).append(SPACE).append("ID: ").append(orgMspId).append("\n")
                .append(SPACE).append(SPACE).append("MSPDir: ").append(orgMspDir).append("\n")
                .append(SPACE).append(SPACE).append("Policies:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Readers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(orgMspId).append(".admin', '").append(orgMspId).append(".peer', '").append(orgMspId).append(".client')\"\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Writers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(orgMspId).append(".admin', '").append(orgMspId).append(".client')\"\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Admins:\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Type: Signature\n")
                .append(SPACE).append(SPACE).append(SPACE).append(SPACE).append("Rule: ").append("\"OR('").append(orgMspId).append(".admin')\"\n");
    }

    private static StringBuilder constructCapabilities() {
        return new StringBuilder().append("\nCapabilities:\n")
                .append(SPACE).append("Application: &ApplicationCapabilities\n")
                .append(SPACE).append(SPACE).append("V1_4_2: true\n")
                .append(SPACE).append(SPACE).append("V1_3: false\n")
                .append(SPACE).append(SPACE).append("V1_2: false\n")
                .append(SPACE).append(SPACE).append("V1_1: false\n");
    }

    private static StringBuilder constructApplication() {
        return new StringBuilder().append("\nApplication: &ApplicationDefaults\n")
                .append(SPACE).append("Organizations:\n")
                .append(SPACE).append("Policies:\n")
                .append(SPACE).append(SPACE).append("Readers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Readers\"\n")
                .append(SPACE).append(SPACE).append("Writers:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Writers\"\n")
                .append(SPACE).append(SPACE).append("Admins:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Type: ImplicitMeta\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Rule: \"ANY Admins\"\n")
                .append(SPACE).append("Capabilities:\n")
                .append(SPACE).append(SPACE).append("<<: *ApplicationCapabilities\n");
    }

    private static StringBuilder constructChannelProfiles(List<String> orgNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nProfiles:\n")
                .append(SPACE).append("TwoOrgsChannel:\n")
                .append(SPACE).append(SPACE).append("Consortium: SampleConsortium\n")
                .append(SPACE).append(SPACE).append("Application:\n")
                .append(SPACE).append(SPACE).append(SPACE).append("<<: *ApplicationDefaults\n")
                .append(SPACE).append(SPACE).append(SPACE).append("Organizations:\n");

        for (String orgName : orgNameList) {
            sb.append(SPACE).append(SPACE).append(SPACE).append("- *").append(orgName).append("\n");
        }
        return sb;
    }

    public static boolean generateOrgConfigTxYaml(String orgName, String orgMspId, String orgMspDir, String yamlFilePath) {
        String content = new StringBuilder("Organizations:\n").append(constructOrganization(orgName, orgMspId, orgMspDir)).toString();

        try {
            File file = new File(yamlFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            outStream.close();
            return true;
        } catch (Exception e) {
            logger.error("Generate configtx.yaml for new org -> {} error: {}", orgName, e.getMessage());
            return false;
        }
    }
}
