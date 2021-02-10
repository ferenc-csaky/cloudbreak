package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import java.util.Optional;

import org.springframework.statemachine.StateContext;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownFailedEvent;
import com.sequenceiq.flow.core.AbstractAction;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.flow.core.FlowState;

public abstract class AbstractDatalakeRecoveryTeardownAction<P extends StackEvent>
        extends AbstractAction<FlowState, FlowEvent, DatalakeRecoveryTeardownContext, P> {

    protected AbstractDatalakeRecoveryTeardownAction(Class<P> payloadClass) {
        super(payloadClass);
    }

    @Override
    protected DatalakeRecoveryTeardownContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext, P payload) {
        return DatalakeRecoveryTeardownContext.from(flowParameters, payload);
    }

    @Override
    protected Object getFailurePayload(P payload, Optional<DatalakeRecoveryTeardownContext> flowContext, Exception ex) {
        return DatalakeRecoveryTeardownFailedEvent.from(payload, ex, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
    }
}
