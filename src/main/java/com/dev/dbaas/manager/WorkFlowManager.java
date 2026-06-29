package com.dev.dbaas.manager;

import com.dev.dbaas.workflow.flow.DeleteInstanceFlow;
import com.dev.dbaas.workflow.flow.api_gateway.CreateApiGatewayStandaloneFlow;
import com.dev.dbaas.workflow.flow.kafka.CreateKafkaClusterFlow;
import com.dev.dbaas.workflow.flow.kafka.CreateKafkaSingleNodeFlow;
import com.dev.dbaas.workflow.flow.mongodb.CreateMongodbReplicasetFlow;
import com.dev.dbaas.workflow.flow.mongodb.CreateMongodbStandaloneFlow;
import com.dev.dbaas.workflow.flow.mysql.CreateMysqlReplicasetFlow;
import com.dev.dbaas.workflow.flow.mysql.CreateMysqlStandaloneFlow;
import com.dev.dbaas.workflow.flow.redis.CreateRedisClusterFlow;
import com.dev.dbaas.workflow.flow.redis.CreateRedisMasterSlaveFlow;
import com.dev.dbaas.workflow.flow.redis.CreateRedisStandaloneFlow;
import net.jworkflow.WorkflowModule;
import net.jworkflow.kernel.interfaces.WorkflowHost;
import org.apache.log4j.Logger;
import org.json.JSONObject;


public class WorkFlowManager {

    private static final Logger LOGGER = Logger.getLogger(WorkFlowManager.class);

    public WorkFlowManager() {

    }

    public static WorkFlowManager getInstance() {
        return WorkFlowManagerHolder.INSTANCE;
    }

    public String startCreateRedisStandalone(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateRedisStandaloneFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateRedisStandaloneFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateRedisMasterSlave(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateRedisMasterSlaveFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateRedisMasterSlaveFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateRedisCluster(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateRedisClusterFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateRedisClusterFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateMongodbStandalone(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateMongodbStandaloneFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateMongodbStandaloneFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateMongodbReplicaset(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateMongodbReplicasetFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateMongodbReplicasetFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateKafkaCluster(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateKafkaClusterFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateKafkaClusterFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateKafkaSingleNode(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateKafkaSingleNodeFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateKafkaSingleNodeFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateMysqlStandalone(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateMysqlStandaloneFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateMysqlStandaloneFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateMysqlReplicaset(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateMysqlReplicasetFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateMysqlReplicasetFlow.class.getSimpleName(), 0, data);
    }

    public String startCreateApiGatewayStandalone(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(CreateApiGatewayStandaloneFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(CreateApiGatewayStandaloneFlow.class.getSimpleName(), 0, data);
    }

    public String deleteInstance(JSONObject data) throws Exception {
        WorkflowModule module = new WorkflowModule();
        module.build();
        WorkflowHost workflowHost = module.getHost();
        workflowHost.registerWorkflow(DeleteInstanceFlow.class);
        workflowHost.start();
        return workflowHost.startWorkflow(DeleteInstanceFlow.class.getSimpleName(), 0, data);
    }

    private static class WorkFlowManagerHolder {
        private static final WorkFlowManager INSTANCE = new WorkFlowManager();
    }
}