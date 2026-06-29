package com.dev.dbaas.workflow.flow.kafka;

import com.dev.dbaas.workflow.steps.kafka.single_node.*;
import com.dev.dbaas.workflow.steps.kafka.single_node.undo.*;
import com.dev.dbaas.workflow.steps.kafka.single_node.*;
import com.dev.dbaas.workflow.steps.kafka.single_node.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateKafkaSingleNodeFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateKafkaSingleNodeFlow.class);

    @Override
    public String getId() {
        return CreateKafkaSingleNodeFlow.class.getSimpleName();
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
                        .startsWith(CheckPrerequisitesSingleNode.class)
                            .compensateWith(CheckPrerequisitesSingleNodeUndo.class)
                        .then(CreateNetworkSingleNode.class)
                            .compensateWith(CreateNetworkSingleNodeUndo.class)
                        .then(CreateVolumeSingleNode.class)
                            .compensateWith(CreateVolumeSingleNodeUndo.class)
                        .then(GenerateCloudInitSingleNode.class)
                            .compensateWith(GenerateCloudInitSingleNodeUndo.class)
                        .then(CreateComputeSingleNode.class)
                            .compensateWith(CreateComputeSingleNodeUndo.class)
                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
