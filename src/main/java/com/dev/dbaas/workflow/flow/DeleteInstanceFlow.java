package com.dev.dbaas.workflow.flow;

import com.dev.dbaas.workflow.steps.instance.DeleteComputeNova;
import com.dev.dbaas.workflow.steps.instance.DeleteNetworkNeutron;
import com.dev.dbaas.workflow.steps.instance.DeleteVolumeCinder;
import com.dev.dbaas.workflow.steps.instance.undo.DeleteComputeNovaUndo;
import com.dev.dbaas.workflow.steps.instance.undo.DeleteNetworkNeutronUndo;
import com.dev.dbaas.workflow.steps.instance.undo.DeleteVolumeCinderUndo;
import net.jworkflow.kernel.interfaces.Workflow;
import net.jworkflow.kernel.interfaces.WorkflowBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class DeleteInstanceFlow implements Workflow<JSONObject> {

    private static final Logger LOGGER = Logger.getLogger(DeleteInstanceFlow.class);

    @Override
    public String getId() {
        return DeleteInstanceFlow.class.getSimpleName();
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

//        builder.startsWithAction(context -> {
//                    LOGGER.info("Begin workflow");
//                })
//                .saga(saga -> saga
//                        // delete on openstack
//                        .startsWith(DeleteComputeNova.class)
//                            .compensateWith(DeleteComputeNovaUndo.class)
//                        .then(DeleteNetworkNeutron.class)
//                            .compensateWith(DeleteNetworkNeutronUndo.class)
//                        .then(DeleteVolumeCinder.class)
//                            .compensateWith(DeleteVolumeCinderUndo.class)
//                        .then(CleanDatabaseInstance.class)
//                            .compensateWith(CleanDatabaseInstanceUndo.class)
//                )
//                .thenAction(context -> {
//                    LOGGER.info("End workflow");
//                });

        builder.startsWithAction(context -> {
                    LOGGER.info("Begin workflow");
                })
                .saga(saga -> saga
                        // delete volume ops
                        .startsWith(DeleteVolumeCinder.class)
                            .compensateWith(DeleteVolumeCinderUndo.class)
                        // delete port, security group ops
                        .then(DeleteNetworkNeutron.class)
                            .compensateWith(DeleteNetworkNeutronUndo.class)
                        // delete compute server ops
                        .then(DeleteComputeNova.class)
                            .compensateWith(DeleteComputeNovaUndo.class)
                )
                .thenAction(context -> {
                    LOGGER.info("End workflow");
                });
    }
}
