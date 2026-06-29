package com.dev.dbaas.workflow.flow.redis;

import com.dev.dbaas.workflow.steps.redis.cluster.CheckPrerequisitesCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.CreateComputeCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.CreateNetworkCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.CreateVolumeCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.GenerateCloudInitCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.undo.*;
import com.dev.dbaas.workflow.steps.redis.cluster.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateRedisClusterFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateRedisClusterFlow.class);

    @Override
    public String getId() {
        return CreateRedisClusterFlow.class.getSimpleName();
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
                        .startsWith(CheckPrerequisitesCluster.class)
                            .compensateWith(CheckPrerequisitesClusterUndo.class)
//                        .then(LoadConfigGroupCluster.class)
//                            .compensateWith(LoadConfigGroupClusterUndo.class)
                        .then(CreateNetworkCluster.class)
                            .compensateWith(CreateNetworkClusterUndo.class)
                        .then(CreateVolumeCluster.class)
                            .compensateWith(CreateVolumeClusterUndo.class)
                        .then(GenerateCloudInitCluster.class)
                            .compensateWith(GenerateCloudInitClusterUndo.class)
                        .then(CreateComputeCluster.class)
                            .compensateWith(CreateComputeClusterUndo.class)
                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
