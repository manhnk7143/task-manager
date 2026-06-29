package com.dev.dbaas.workflow.flow.redis;

import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.*;
import com.dev.dbaas.workflow.steps.redis.master_slave.slave.*;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.CreateComputeMasterCluster;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.CreateNetworkMasterCluster;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.CreateVolumeMasterCluster;
import com.dev.dbaas.workflow.steps.redis.master_slave.master.GenerateCloudInitMasterCluster;
import com.dev.dbaas.workflow.steps.redis.master_slave.slave.*;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.CheckPrerequisitesMasterSlaveUndo;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.master.*;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.slave.*;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.master.CreateComputeMasterClusterUndo;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.master.CreateNetworkMasterClusterUndo;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.master.CreateVolumeMasterClusterUndo;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.master.GenerateCloudInitMasterClusterUndo;
import com.dev.dbaas.workflow.steps.redis.master_slave.undo.slave.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateRedisMasterSlaveFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateRedisMasterSlaveFlow.class);

    @Override
    public String getId() {
        return CreateRedisMasterSlaveFlow.class.getSimpleName();
    }

    @Override
    public Class getDataType() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void build(WorkflowBuilder<JSONObject> builder) {
        builder.startsWithAction(context -> {
                    LOGGER.info("Begin workflow");
                })
                .saga(saga -> saga
                        // check prerequisites
                        .startsWith(CheckPrerequisitesMasterSlave.class)
                            .compensateWith(CheckPrerequisitesMasterSlaveUndo.class)
//                        .then(LoadConfigGroupMasterSlave.class)
//                            .compensateWith(LoadConfigGroupMasterSlaveUndo.class)

                        // create master node
                        .then(CreateNetworkMasterCluster.class)
                            .compensateWith(CreateNetworkMasterClusterUndo.class)
                        .then(CreateVolumeMasterCluster.class)
                            .compensateWith(CreateVolumeMasterClusterUndo.class)
                        .then(GenerateCloudInitMasterCluster.class)
                            .compensateWith(GenerateCloudInitMasterClusterUndo.class)
                        .then(CreateComputeMasterCluster.class)
                            .compensateWith(CreateComputeMasterClusterUndo.class)

                        // create slave 0
                        .then(CreateNetworkSlave0Cluster.class)
                            .compensateWith(CreateNetworkSlave0ClusterUndo.class)
                        .then(CreateVolumeSlave0Cluster.class)
                            .compensateWith(CreateVolumeSlave0ClusterUndo.class)
                        .then(GenerateCloudInitSlave0Cluster.class)
                            .compensateWith(GenerateCloudInitSlave0ClusterUndo.class)
                        .then(CreateComputeSlave0Cluster.class)
                            .compensateWith(CreateComputeSlave0ClusterUndo.class)

                        // create slave 1
                        .then(CreateNetworkSlave1Cluster.class)
                            .compensateWith(CreateNetworkSlave1ClusterUndo.class)
                        .then(CreateVolumeSlave1Cluster.class)
                            .compensateWith(CreateVolumeSlave1ClusterUndo.class)
                        .then(GenerateCloudInitSlave1Cluster.class)
                            .compensateWith(GenerateCloudInitSlave1ClusterUndo.class)
                        .then(CreateComputeSlave1Cluster.class)
                            .compensateWith(CreateComputeSlave1ClusterUndo.class)

                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
