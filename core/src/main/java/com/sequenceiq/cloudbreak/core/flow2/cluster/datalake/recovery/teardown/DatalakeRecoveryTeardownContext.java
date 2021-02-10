package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.flow.core.CommonContext;
import com.sequenceiq.flow.core.FlowParameters;

public class DatalakeRecoveryTeardownContext extends CommonContext {

    private Long stackId;

    public DatalakeRecoveryTeardownContext(FlowParameters flowParameters, StackEvent event) {
        super(flowParameters);
        stackId = event.getResourceId();
    }

    public DatalakeRecoveryTeardownContext(FlowParameters flowParameters, Long stackId) {
        super(flowParameters);
        this.stackId = stackId;
    }

    public static DatalakeRecoveryTeardownContext from(FlowParameters flowParameters, StackEvent event) {
        return new DatalakeRecoveryTeardownContext(flowParameters, event);
    }

    public Long getStackId() {
        return stackId;
    }

    public void setStackId(Long stackId) {
        this.stackId = stackId;
    }
}
