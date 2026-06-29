package com.dev.dbaas.workflow.flow.mongodb;

import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.arbiter.CreateComputeArbiterReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.arbiter.CreateNetworkArbiterReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.arbiter.GenerateCloudInitArbiterReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateComputePrimaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateNetworkPrimaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateVolumePrimaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.GenerateCloudInitPrimaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary.CreateComputeSecondaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary.CreateNetworkSecondaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary.CreateVolumeSecondaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary.GenerateCloudInitSecondaryReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.CheckPrerequisitesReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.arbiter.CreateComputeArbiterReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.arbiter.CreateNetworkArbiterReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.arbiter.GenerateCloudInitArbiterReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.primary.CreateComputePrimaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.primary.CreateNetworkPrimaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.primary.CreateVolumePrimaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.primary.GenerateCloudInitPrimaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.secondary.CreateComputeSecondaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.secondary.CreateNetworkSecondaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.secondary.CreateVolumeSecondaryReplicasetUndo;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.undo.secondary.GenerateCloudInitSecondaryReplicasetUndo;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CreateMongodbReplicasetFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(CreateMongodbReplicasetFlow.class);

    @Override
    public String getId() {
        return CreateMongodbReplicasetFlow.class.getSimpleName();
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
                        .startsWith(CheckPrerequisitesReplicaset.class)
                            .compensateWith(CheckPrerequisitesReplicasetUndo.class)
//                        .then(LoadConfigGroupReplicaset.class)
//                            .compensateWith(LoadConfigGroupReplicasetUndo.class)

                        // create primary node
                        .then(CreateNetworkPrimaryReplicaset.class)
                            .compensateWith(CreateNetworkPrimaryReplicasetUndo.class)
                        .then(CreateVolumePrimaryReplicaset.class)
                            .compensateWith(CreateVolumePrimaryReplicasetUndo.class)
                        .then(GenerateCloudInitPrimaryReplicaset.class)
                            .compensateWith(GenerateCloudInitPrimaryReplicasetUndo.class)
                        .then(CreateComputePrimaryReplicaset.class)
                            .compensateWith(CreateComputePrimaryReplicasetUndo.class)

                        // create secondary
                        .then(CreateNetworkSecondaryReplicaset.class)
                            .compensateWith(CreateNetworkSecondaryReplicasetUndo.class)
                        .then(CreateVolumeSecondaryReplicaset.class)
                            .compensateWith(CreateVolumeSecondaryReplicasetUndo.class)
                        .then(GenerateCloudInitSecondaryReplicaset.class)
                            .compensateWith(GenerateCloudInitSecondaryReplicasetUndo.class)
                        .then(CreateComputeSecondaryReplicaset.class)
                            .compensateWith(CreateComputeSecondaryReplicasetUndo.class)

                        // create arbiter
                        .then(CreateNetworkArbiterReplicaset.class)
                            .compensateWith(CreateNetworkArbiterReplicasetUndo.class)
                        .then(GenerateCloudInitArbiterReplicaset.class)
                            .compensateWith(GenerateCloudInitArbiterReplicasetUndo.class)
                        .then(CreateComputeArbiterReplicaset.class)
                            .compensateWith(CreateComputeArbiterReplicasetUndo.class)

                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
