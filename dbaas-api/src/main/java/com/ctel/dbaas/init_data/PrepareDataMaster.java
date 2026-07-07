//package com.ctel.dbaas.init_data;
//
//import com.ctel.dbaas.common.enums.Datastore;
//import com.ctel.dbaas.common.enums.Status;
//import com.ctel.dbaas.config.EnvConfig;
//import com.ctel.dbaas.utils.CryptoUtils;
//import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
//import com.ctel.dbaas.entity.*;
//import com.ctel.dbaas.repository.*;
//import com.ctel.dbaas.test.InitConfigParam;
//import com.ctel.dbaas.utils.CommonUtils;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class PrepareDataMaster {
//
//    @Autowired
//    private DatastoreRepository datastoreRepository;
//
//    @Autowired
//    private DatastoreVersionRepository datastoreVersionRepository;
//
//    @Autowired
//    private DatastoreModeRepository datastoreModeRepository;
//
//    @Autowired
//    private DatastoreConfigurationRepository datastoreConfigurationRepository;
//
//    @Autowired
//    private GroupConfigurationRepository groupConfigurationRepository;
//
//    @Autowired
//    private ConfigurationRepository configurationRepository;
//
//    @Autowired
//    private BackupStrategyRepository backupStrategyRepository;
//
//    public void init() {
//
//        // insert datastore redis
//        DatastoreEntity datastore = datastoreRepository.findFirstByName(Datastore.REDIS.getDatastoreName()).orElse(new DatastoreEntity());
//        if (datastore.getId() == null) {
//            datastore.setName(Datastore.REDIS.getDatastoreName());
//            datastore.setStatus(Status.ACTIVE.getStatus());
//            datastoreRepository.save(datastore);
//        }
//
//        // insert datastore version redis 6.0
//        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
//                .findFirstByDatastoreIdAndVersion(datastore.getId(), "6.0").orElse(new DatastoreVersionEntity());
//        if (datastoreVersion.getId() == null) {
//            datastoreVersion.setRepoInformation("{}");
//            datastoreVersion.setProvider("CMC");
//            datastoreVersion.setPlatformSupports("docker");
//            datastoreVersion.setVersion("6.0");
//            datastoreVersion.setGlanceImageTags("cmc-dbaas-agent");
//            datastoreVersion.setStatus(Status.ACTIVE.getStatus());
//            datastoreVersion.setDatastoreId(datastore.getId());
//            datastoreVersionRepository.save(datastoreVersion);
//
//            datastore.setDefaultVersionId(datastoreVersion.getId());
//            datastoreRepository.save(datastore);
//        }
//
//        // insert datastore mode standalone for redis
//        DatastoreModeEntity datastoreModeStandalone = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
//                "Standalone", datastoreVersion.getId()).orElse(new DatastoreModeEntity());
//        if (datastoreModeStandalone.getId() == null) {
//            datastoreModeStandalone.setName("Standalone");
//            datastoreModeStandalone.setCode("standalone");
//            datastoreModeStandalone.setDatastoreVersionId(datastoreVersion.getId());
//            datastoreModeStandalone.setRequirement("{}");
//            datastoreModeStandalone.setDeploymentSteps("{}");
//            datastoreModeStandalone.setStatus(Status.ACTIVE.getStatus());
//
//            datastoreModeRepository.save(datastoreModeStandalone);
//        }
//
//        // insert datastore mode cluster for redis
//        DatastoreModeEntity datastoreModeMasterSlave = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
//                "Master/Slave", datastoreVersion.getId()).orElse(new DatastoreModeEntity());
//        if (datastoreModeMasterSlave.getId() == null) {
//            datastoreModeMasterSlave.setName("Master/Slave");
//            datastoreModeMasterSlave.setCode("master_slave");
//            datastoreModeMasterSlave.setDatastoreVersionId(datastoreVersion.getId());
//            datastoreModeMasterSlave.setRequirement("{}");
//            datastoreModeMasterSlave.setDeploymentSteps("{}");
//            datastoreModeMasterSlave.setStatus(Status.ACTIVE.getStatus());
//
//            datastoreModeRepository.save(datastoreModeMasterSlave);
//        }
//
//        // insert config for redis standalone
//        boolean existsDefaultConfigStandalone = datastoreConfigurationRepository
//                .existsByDatastoreModeId(datastoreModeStandalone.getId());
//        if (!existsDefaultConfigStandalone) {
//            String jsonConfigStandalone = """
//                    {
//                      "params": [
//                          {
//                              "proxy": false,
//                              "param_id": "1",
//                              "param_name": "timeout",
//                              "default_value": "0",
//                              "value_range": "0-7200",
//                              "value_type": "Integer",
//                              "description": "The maximum amount of time (in seconds) a connection between the a client and the DCS instance can be allowed to remain idle before the connection is terminated. A setting of 0 means that this function is disabled.",
//                              "param_value": "0",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "timeout",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "3",
//                              "param_name": "hash-max-ziplist-entries",
//                              "default_value": "512",
//                              "value_range": "1-10000",
//                              "value_type": "Integer",
//                              "description": "The maximum number of hashes that can be encoded using ziplist, a data structure optimized to reduce memory use.",
//                              "param_value": "512",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "hash-max-ziplist-entries",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "4",
//                              "param_name": "hash-max-ziplist-value",
//                              "default_value": "64",
//                              "value_range": "1-10000",
//                              "value_type": "Integer",
//                              "description": "The largest value allowed for a hash encoded using ziplist, a special data structure optimized for memory use.",
//                              "param_value": "64",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "hash-max-ziplist-value",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "7",
//                              "param_name": "set-max-intset-entries",
//                              "default_value": "512",
//                              "value_range": "1-10000",
//                              "value_type": "Integer",
//                              "description": "If a set is composed entirely of strings that are integers in radix 10 within the range of 64 bit signed integers, sets are encoded using intset, a data structure optimized for memory use.",
//                              "param_value": "512",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "set-max-intset-entries",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "8",
//                              "param_name": "zset-max-ziplist-entries",
//                              "default_value": "128",
//                              "value_range": "1-10000",
//                              "value_type": "Integer",
//                              "description": "The maximum number of sorted set entries allowed before they are encoded using ziplist, a data structure optimized for memory use.",
//                              "param_value": "128",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "zset-max-ziplist-entries",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "9",
//                              "param_name": "zset-max-ziplist-value",
//                              "default_value": "64",
//                              "value_range": "1-10000",
//                              "value_type": "Integer",
//                              "description": "The maximum length allowed for a sorted set before it is encoded using ziplist, a data structure optimized for memory use.",
//                              "param_value": "64",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "zset-max-ziplist-value",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "10",
//                              "param_name": "latency-monitor-threshold",
//                              "default_value": "0",
//                              "value_range": "0-86400000",
//                              "value_type": "Integer",
//                              "description": "The minimum amount of latency that will be logged as latency spikes\\nSet to 0: Latency monitoring is disabled.\\nSet to more than 0: All with at least this many ms of latency will be logged.",
//                              "param_value": "0",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "latency-monitor-threshold",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "13",
//                              "param_name": "notify-keyspace-events",
//                              "default_value": "Ex",
//                              "value_range": "([KE]+([A]|[g$lshzxe]+)){0,11}",
//                              "value_type": "Regular",
//                              "description": "Controls which keyspace events notifications are enabled for. If this parameter is left empty, keyspace event notification is disabled. A string of different values can be used to enable notifications for multiple event types: Possible values include: \\nK: Keyspace events, published with the __keyspace@__ prefix \\nE: Keyevent events, published with __keyevent@__ prefix \\ng: Generic commands (non-type specific) such as DEL, EXPIRE, and RENAME \\n$: String commands \\nl: List commands \\ns: Set commands \\nh: Hash commands \\nz: Sorted set commands \\nx: Expired events (events generated every time a key expires) \\ne: Evicted events (events generated when a key is evicted for maxmemory) \\nA: Alias for \\"g$lshzxe\\", so that the \\"AKE\\" string means all the events. \\nFor example, the value Kl means that Redis can notify Pub/Sub clients about keyspace events and list commands. The parameter setting must contain at least a \\"K\\" or \\"E\\". \\"A\\" cannot be selected together with \\"g$lshzxe\\", and duplicate characters are not allowed.",
//                              "param_value": "Ex",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "notify-keyspace-events",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "18",
//                              "param_name": "slowlog-log-slower-than",
//                              "default_value": "10000",
//                              "value_range": "0-1000000",
//                              "value_type": "Integer",
//                              "description": "The maximum time allowed, in microseconds, for command execution. If this threshold is exceeded, Slow Queries will record the command.",
//                              "param_value": "10000",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "slowlog-log-slower-than",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "19",
//                              "param_name": "slowlog-max-len",
//                              "default_value": "128",
//                              "value_range": "0-1000",
//                              "value_type": "Integer",
//                              "description": "The maximum allowed number of slow queries. Slow queries consume memory, but you can reclaim this memory by running the SLOWLOG RESET command.",
//                              "param_value": "128",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "slowlog-max-len",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          },
//                          {
//                              "proxy": false,
//                              "param_id": "20",
//                              "param_name": "lua-time-limit",
//                              "default_value": "5000",
//                              "value_range": "100-5000",
//                              "value_type": "Integer",
//                              "description": "Maximum time allowed for executing a Lua script (in milliseconds)",
//                              "param_value": "5000",
//                              "node_role": null,
//                              "param_type": "user",
//                              "user_permission": "write",
//                              "need_restart": false,
//                              "supported_version": "",
//                              "international_key": "lua-time-limit",
//                              "new_version_only": false,
//                              "support_data_version": null,
//                              "customized": false
//                          }
//                      ]
//                    }
//                    """;
//
//            this.initConfigParams(jsonConfigStandalone, datastoreModeStandalone.getId());
//
//        }
//
//        // insert config for redis cluster
//        boolean existsDefaultConfigMasterSlave = datastoreConfigurationRepository
//                .existsByDatastoreModeId(datastoreModeMasterSlave.getId());
//        if (!existsDefaultConfigMasterSlave) {
//            String jsonConfigMasterSlave = """
//                    {
//                        "params": [
//                            {
//                                "proxy": false,
//                                "param_id": "1",
//                                "param_name": "timeout",
//                                "default_value": "0",
//                                "value_range": "0-7200",
//                                "value_type": "Integer",
//                                "description": "Close the connection after a client is idle for N seconds (0 to disable)",
//                                "param_value": "0",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "timeout",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "2",
//                                "param_name": "maxmemory-policy",
//                                "default_value": "volatile-lru",
//                                "value_range": "volatile-lru,allkeys-lru,volatile-lfu,allkeys-lfu,volatile-random,allkeys-random,volatile-ttl,noeviction",
//                                "value_type": "Enum",
//                                "description": "How Redis will select what to remove when maxmemory is reached, You can select among five behaviors: volatile-lru : remove the key with an expire set using an LRU algorithm; allkeys-lru : remove any key according to the LRU algorithm .volatile-lfu:remove the key with an expire set using an LFU algorithm. allkeys-lfu:remove any key according to the LFU algorithm volatile-random: remove a random key with an expire set allkeys-random: remove a random key, any key volatile-ttl : remove the key with the nearest expire time (minor TTL) noeviction : don't expire at all, just return an error on write operations",
//                                "param_value": "volatile-lru",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "maxmemory-policy",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "3",
//                                "param_name": "hash-max-ziplist-entries",
//                                "default_value": "512",
//                                "value_range": "1-10000",
//                                "value_type": "Integer",
//                                "description": "Hashes are encoded using a memory efficient data structure when they have a small number of entries",
//                                "param_value": "512",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "hash-max-ziplist-entries",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "4",
//                                "param_name": "hash-max-ziplist-value",
//                                "default_value": "64",
//                                "value_range": "1-10000",
//                                "value_type": "Integer",
//                                "description": "Hashes are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold",
//                                "param_value": "64",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "hash-max-ziplist-value",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "7",
//                                "param_name": "set-max-intset-entries",
//                                "default_value": "512",
//                                "value_range": "1-10000",
//                                "value_type": "Integer",
//                                "description": "When a set is composed of just strings that happen to be integers in radix 10 in the range of 64 bit signed integers.",
//                                "param_value": "512",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "set-max-intset-entries",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "8",
//                                "param_name": "zset-max-ziplist-entries",
//                                "default_value": "128",
//                                "value_range": "1-10000",
//                                "value_type": "Integer",
//                                "description": "Sorted sets are encoded using a memory efficient data structure when they have a small number of entries",
//                                "param_value": "128",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "zset-max-ziplist-entries",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "9",
//                                "param_name": "zset-max-ziplist-value",
//                                "default_value": "64",
//                                "value_range": "1-10000",
//                                "value_type": "Integer",
//                                "description": "Sorted sets are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold",
//                                "param_value": "64",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "zset-max-ziplist-value",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "10",
//                                "param_name": "latency-monitor-threshold",
//                                "default_value": "0",
//                                "value_range": "0-86400000",
//                                "value_type": "Integer",
//                                "description": "Only events that run in more time than the configured latency-monitor-threshold will be logged as latency spikes. If latency-monitor-threshold is set to 0, latency monitoring is disabled. If latency-monitor-threshold is set to a value greater than 0, all events blocking the server for a time equal to or greater than the configured latency-monitor-threshold will be logged.",
//                                "param_value": "0",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "latency-monitor-threshold",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "13",
//                                "param_name": "notify-keyspace-events",
//                                "default_value": "Ex",
//                                "value_range": "([KE]+([A]|[g$lshzxe]+)){0,11}",
//                                "value_type": "Regular",
//                                "description": "Redis can notify Pub or Sub clients about events happening in the key space",
//                                "param_value": "Ex",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "notify-keyspace-events",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "14",
//                                "param_name": "repl-backlog-size",
//                                "default_value": "1048576",
//                                "value_range": "16384-1073741824",
//                                "value_type": "Integer",
//                                "description": "The replication backlog size in bytes for PSYNC. This is the size of the buffer which accumulates slave data when slave is disconnected for some time, so that when slave reconnects again, only transfer the portion of data which the slave missed.",
//                                "param_value": "1048576",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "repl-backlog-size",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "15",
//                                "param_name": "repl-backlog-ttl",
//                                "default_value": "3600",
//                                "value_range": "0-604800",
//                                "value_type": "Integer",
//                                "description": "The amount of time in seconds after the master no longer have any slaves connected for the master to free the replication backlog. A value of 0 means to never release the backlog.",
//                                "param_value": "3600",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "repl-backlog-ttl",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "16",
//                                "param_name": "appendfsync",
//                                "default_value": "no",
//                                "value_range": "no,always,everysec",
//                                "value_type": "Enum",
//                                "description": "The fsync() call tells the Operating System to actually write data on disk instead of waiting for more data in the output buffer. Some OS will really flush data on disk, some other OS will just try to do it ASAP.\\nRedis supports three different modes:\\nno: don't fsync, just let the OS flush the data when it wants. Faster.\\nalways: fsync after every write to the append only log. Slow, Safest.\\neverysec: fsync only one time every second. Compromise.",
//                                "param_value": "no",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "appendfsync",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "17",
//                                "param_name": "appendonly",
//                                "default_value": "yes",
//                                "value_range": "yes,no",
//                                "value_type": "Enum",
//                                "description": "Configuration item for AOF persistence",
//                                "param_value": "yes",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "appendonly",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "18",
//                                "param_name": "slowlog-log-slower-than",
//                                "default_value": "10000",
//                                "value_range": "0-1000000",
//                                "value_type": "Integer",
//                                "description": "The Redis Slow Log is a system to log queries that exceeded a specified execution time.\\nslowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the\\ncommand to get logged\\n",
//                                "param_value": "10000",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "slowlog-log-slower-than",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "19",
//                                "param_name": "slowlog-max-len",
//                                "default_value": "128",
//                                "value_range": "0-1000",
//                                "value_type": "Integer",
//                                "description": "The Redis Slow Log is a system to log queries that exceeded a specified execution time.\\nslowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the command to get logged",
//                                "param_value": "128",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "slowlog-max-len",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "20",
//                                "param_name": "lua-time-limit",
//                                "default_value": "5000",
//                                "value_range": "100-5000",
//                                "value_type": "Integer",
//                                "description": "Max execution time of a Lua script in milliseconds.",
//                                "param_value": "5000",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "lua-time-limit",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "21",
//                                "param_name": "repl-timeout",
//                                "default_value": "60",
//                                "value_range": "30-3600",
//                                "value_type": "Integer",
//                                "description": "Replication timeout in seconds.",
//                                "param_value": "60",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "repl-timeout",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "22",
//                                "param_name": "proto-max-bulk-len",
//                                "default_value": "536870912",
//                                "value_range": "1048576-536870912",
//                                "value_type": "Integer",
//                                "description": "Max bulk request size in bytes.",
//                                "param_value": "536870912",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "proto-max-bulk-len",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "23",
//                                "param_name": "master-read-only",
//                                "default_value": "no",
//                                "value_range": "yes,no",
//                                "value_type": "Enum",
//                                "description": "Set redis to read only state and all write commands will fail.",
//                                "param_value": "no",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "master-read-only",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "26",
//                                "param_name": "client-output-buffer-limit-slave-soft-seconds",
//                                "default_value": "60",
//                                "value_range": "0-60",
//                                "value_type": "Integer",
//                                "description": "Set redis to read only state and all write commands will fail.",
//                                "param_value": "60",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "client-output-buffer-limit-slave-soft-seconds",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            },
//                            {
//                                "proxy": false,
//                                "param_id": "35",
//                                "param_name": "active-expire-num",
//                                "default_value": "20",
//                                "value_range": "1-1000",
//                                "value_type": "Integer",
//                                "description": "How many keys can be freed by expire cycle.",
//                                "param_value": "20",
//                                "node_role": null,
//                                "param_type": "user",
//                                "user_permission": "write",
//                                "need_restart": false,
//                                "supported_version": "",
//                                "international_key": "active-expire-num",
//                                "new_version_only": false,
//                                "support_data_version": null,
//                                "customized": false
//                            }
//                        ]
//                    }
//                    """;
//            this.initConfigParams(jsonConfigMasterSlave, datastoreModeMasterSlave.getId());
//        }
//
//        // insert group config default for redis standalone
//        boolean existsGroupConfigDefaultStandalone = groupConfigurationRepository
//                .existsByDatastoreModeIdAndOrgId(datastoreModeStandalone.getId(), "system");
//        if (!existsGroupConfigDefaultStandalone) {
//            GroupConfigurationEntity groupConfigDefaultStandalone = new GroupConfigurationEntity();
//            groupConfigDefaultStandalone.setName("default_config_redis_6.0_standalone");
//            groupConfigDefaultStandalone.setDescription("Default config of redis 6.0 standalone");
//            groupConfigDefaultStandalone.setDatastoreModeId(datastoreModeStandalone.getId());
//            groupConfigDefaultStandalone.setOrgId("system");
//            groupConfigDefaultStandalone.setDefault(true);
//            groupConfigurationRepository.save(groupConfigDefaultStandalone);
//
//            List<DatastoreConfigurationEntity> lstConfigDefault = datastoreConfigurationRepository
//                    .findAllByDatastoreModeIdAndStatus(datastoreModeStandalone.getId(), Status.ACTIVE.getStatus());
//            List<ConfigurationEntity> configs = new ArrayList<>();
//
//            for (DatastoreConfigurationEntity cfDefault : lstConfigDefault) {
//                ConfigurationEntity configCustom = new ConfigurationEntity();
//                configCustom.setParamName(cfDefault.getParamName());
//                configCustom.setParamValue(cfDefault.getDefaultValue());
//                configCustom.setGroupConfigurationId(groupConfigDefaultStandalone.getId());
//                configCustom.setDatastoreConfigurationId(cfDefault.getId());
//                configCustom.setOrgId("system");
//                configs.add(configCustom);
//            }
//            configurationRepository.saveAll(configs);
//        }
//
//        // insert group config default for redis cluster
//        boolean existsGroupConfigDefaultCluster = groupConfigurationRepository
//                .existsByDatastoreModeIdAndOrgId(datastoreModeMasterSlave.getId(), "system");
//        if (!existsGroupConfigDefaultCluster) {
//            GroupConfigurationEntity groupConfigDefaultCluster = new GroupConfigurationEntity();
//            groupConfigDefaultCluster.setName("default_config_redis_6.0_cluster");
//            groupConfigDefaultCluster.setDescription("Default config of redis 6.0 cluster");
//            groupConfigDefaultCluster.setDatastoreModeId(datastoreModeMasterSlave.getId());
//            groupConfigDefaultCluster.setOrgId("system");
//            groupConfigDefaultCluster.setDefault(true);
//            groupConfigurationRepository.save(groupConfigDefaultCluster);
//
//            List<DatastoreConfigurationEntity> lstConfigDefault = datastoreConfigurationRepository
//                    .findAllByDatastoreModeIdAndStatus(datastoreModeMasterSlave.getId(), Status.ACTIVE.getStatus());
//            List<ConfigurationEntity> configs = new ArrayList<>();
//
//            for (DatastoreConfigurationEntity cfDefault : lstConfigDefault) {
//                ConfigurationEntity configCustom = new ConfigurationEntity();
//                configCustom.setParamName(cfDefault.getParamName());
//                configCustom.setParamValue(cfDefault.getDefaultValue());
//                configCustom.setGroupConfigurationId(groupConfigDefaultCluster.getId());
//                configCustom.setDatastoreConfigurationId(cfDefault.getId());
//                configCustom.setOrgId("system");
//                configs.add(configCustom);
//            }
//            configurationRepository.saveAll(configs);
//        }
//
//        // insert backup strategy S3
//        BackupStrategyEntity backupStrategy = backupStrategyRepository.findFirstByType("S3");
//        if (backupStrategy == null) {
//            S3StorageConfigDto storageConfigDto = new S3StorageConfigDto();
//            storageConfigDto.setEndpoint(EnvConfig.S3_ENDPOINT);
//            storageConfigDto.setRegion(EnvConfig.S3_REGION);
//            storageConfigDto.setAccessKey(EnvConfig.S3_ACCESS_KEY);
//            storageConfigDto.setSecretKey(EnvConfig.S3_SECRET_KEY);
//
//            String encryptedConfig = CryptoUtils.encrypt(CommonUtils.toJson(storageConfigDto),
//                    EnvConfig.KEY_DECRYPT_CONFIG_BACKUP);
//
//            backupStrategy = new BackupStrategyEntity();
//            backupStrategy.setType("S3");
//            backupStrategy.setActive(true);
//            backupStrategy.setConfiguration(encryptedConfig);
//            backupStrategyRepository.save(backupStrategy);
//        }
//    }
//
//    @Data
//    public static class Config {
//        @JsonProperty("params")
//        private List<DatastoreConfigurationEntity> params;
//    }
//
//    @SneakyThrows
//    private void initConfigParams(String jsonConfig, String datastoreModeId) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        InitConfigParam.Config lstConfig = objectMapper.readValue(jsonConfig, InitConfigParam.Config.class);
//
//        for (DatastoreConfigurationEntity entity : lstConfig.getParams()) {
//            entity.setDatastoreModeId(datastoreModeId);
//            entity.setStatus("active");
//            try {
//                datastoreConfigurationRepository.save(entity);
//            } catch (Exception e) {
//                System.out.println("message: " + e.getMessage());
//                System.out.println(entity.getParamName() + " - length description: " + entity.getDescription().length());
//                return;
//            }
//
//        }
//    }
//
//}
