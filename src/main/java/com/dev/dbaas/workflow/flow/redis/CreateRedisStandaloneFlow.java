package com.dev.dbaas.workflow.flow.redis;

import com.dev.dbaas.workflow.steps.redis.standalone.*;
import com.dev.dbaas.workflow.steps.redis.standalone.undo.*;
import com.dev.dbaas.workflow.steps.redis.standalone.*;
import com.dev.dbaas.workflow.steps.redis.standalone.undo.*;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateRedisStandaloneFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateRedisStandaloneFlow.class);

    @Override
    public String getId() {
        return CreateRedisStandaloneFlow.class.getSimpleName();
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

        builder.startsWithAction(context -> {
                    LOGGER.info("Begin workflow");
                })
                .saga(saga -> saga
                        .startsWith(CheckPrerequisitesStandalone.class)
                            .compensateWith(CheckPrerequisitesStandaloneUndo.class)
//                        .then(LoadConfigGroupStandalone.class)
//                            .compensateWith(LoadConfigGroupStandaloneUndo.class)
                        .then(CreateNetworkStandalone.class)
                            .compensateWith(CreateNetworkStandaloneUndo.class)
                        .then(CreateVolumeStandalone.class)
                            .compensateWith(CreateVolumeStandaloneUndo.class)
                        .then(GenerateCloudInitStandalone.class)
                            .compensateWith(GenerateCloudInitStandaloneUndo.class)
                        .then(CreateComputeStandalone.class)
                            .compensateWith(CreateComputeStandaloneUndo.class)
                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
