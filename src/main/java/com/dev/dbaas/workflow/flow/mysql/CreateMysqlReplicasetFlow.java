package com.dev.dbaas.workflow.flow.mysql;

import com.dev.dbaas.workflow.steps.mysql.replicaset.*;
import com.dev.dbaas.workflow.steps.mysql.replicaset.undo.*;
import com.dev.dbaas.workflow.steps.mysql.replicaset.*;
import com.dev.dbaas.workflow.steps.mysql.replicaset.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateMysqlReplicasetFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateMysqlReplicasetFlow.class);

    @Override
    public String getId() {
        return CreateMysqlReplicasetFlow.class.getSimpleName();
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
        builder
                .startsWithAction(context -> LOGGER.info("Begin workflow"))
                .saga(saga -> saga
                        .startsWith(CheckPrerequisites.class)
                            .compensateWith(CheckPrerequisitesUndo.class)
                        .then(CreateNetwork.class)
                            .compensateWith(CreateNetworkUndo.class)
                        .then(CreateVolume.class)
                            .compensateWith(CreateVolumeUndo.class)
                        .then(GenerateCloudInit.class)
                            .compensateWith(GenerateCloudInitUndo.class)
                        .then(CreateCompute.class)
                            .compensateWith(CreateComputeUndo.class)
                )
                .thenAction(context -> LOGGER.info("End workflow"));
    }
}
