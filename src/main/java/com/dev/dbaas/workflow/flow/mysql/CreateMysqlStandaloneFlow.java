package com.dev.dbaas.workflow.flow.mysql;

import com.dev.dbaas.workflow.steps.mysql.standalone.*;
import com.dev.dbaas.workflow.steps.mysql.standalone.undo.*;
import com.dev.dbaas.workflow.steps.mysql.standalone.*;
import com.dev.dbaas.workflow.steps.mysql.standalone.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateMysqlStandaloneFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateMysqlStandaloneFlow.class);

    @Override
    public String getId() {
        return CreateMysqlStandaloneFlow.class.getSimpleName();
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
                        .startsWith(CheckPrerequisitesStandalone.class)
                            .compensateWith(CheckPrerequisitesStandaloneUndo.class)
                        .then(CreateNetworkStandalone.class)
                            .compensateWith(CreateNetworkStandaloneUndo.class)
                        .then(CreateVolumeStandalone.class)
                            .compensateWith(CreateVolumeStandaloneUndo.class)
                        .then(GenerateCloudInitStandalone.class)
                            .compensateWith(GenerateCloudInitStandaloneUndo.class)
                        .then(CreateComputeStandalone.class)
                            .compensateWith(CreateComputeStandaloneUndo.class)
                )
                .thenAction(context -> LOGGER.info("End workflow"));
    }
}
