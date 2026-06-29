package com.dev.dbaas.workflow.flow.kafka;

import com.dev.dbaas.workflow.steps.kafka.cluster.*;
import com.dev.dbaas.workflow.steps.kafka.cluster.undo.*;
import com.dev.dbaas.workflow.steps.kafka.cluster.*;
import com.dev.dbaas.workflow.steps.kafka.cluster.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateKafkaClusterFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateKafkaClusterFlow.class);

    @Override
    public String getId() {
        return CreateKafkaClusterFlow.class.getSimpleName();
    }

    @Override
    public Class getDataType() {
        return JSONObject.class;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void build(WorkflowBuilder<JSONObject> builder) {
        builder.startsWithAction(context -> LOGGER.info("Begin workflow"))
                .saga(saga -> saga
                        .startsWith(CheckPrerequisitesCluster.class)
                            .compensateWith(CheckPrerequisitesClusterUndo.class)
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
