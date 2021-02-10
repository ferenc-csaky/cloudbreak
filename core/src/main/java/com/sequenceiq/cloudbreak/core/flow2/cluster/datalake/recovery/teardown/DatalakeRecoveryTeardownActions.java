package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.event.ClusterRecoveryTriggerEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownSuccess;
import com.sequenceiq.flow.core.Flow;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.flow.core.FlowState;

@Configuration
public class DatalakeRecoveryTeardownActions {

    @Inject
    private DatalakeRecoveryTeardownService datalakeRecoveryTeardownStatusService;

    @Bean(name = "RECOVERY_TEARDOWN_STATE")
    public Action<?, ?> startDatalakeRecoveryTeardown() {
        return new AbstractDatalakeRecoveryTeardownAction<>(ClusterRecoveryTriggerEvent.class) {

            @Override
            protected void doExecute(DatalakeRecoveryTeardownContext context, ClusterRecoveryTriggerEvent payload, Map<Object, Object> variables) {
//                datalakeRecoveryTeardownStatusService.teardownDatalakeRecovery(context.getStackId(), context.getDatalakeRecoveryId());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(DatalakeRecoveryTeardownContext context) {
                return new DatalakeRecoveryTeardownRequest(context.getStackId());
            }

            @Override
            protected Object getFailurePayload(ClusterRecoveryTriggerEvent payload, Optional<DatalakeRecoveryTeardownContext> flowContext, Exception ex) {
                return DatalakeRecoveryTeardownFailedEvent.from(payload, ex, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
            }
        };
    }

    @Bean(name = "RECOVERY_TEARDOWN_FINISHED_STATE")
    public Action<?, ?> datalakeRecoveryTeardownFinished() {
        return new AbstractDatalakeRecoveryTeardownAction<>(DatalakeRecoveryTeardownSuccess.class) {

            @Override
            protected void doExecute(DatalakeRecoveryTeardownContext context, DatalakeRecoveryTeardownSuccess payload, Map<Object, Object> variables) {
//                datalakeRecoveryTeardownStatusService.teardownDatalakeRecoveryFinished(context.getStackId());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(DatalakeRecoveryTeardownContext context) {
                return new StackEvent(DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FINALIZED_EVENT.event(), context.getStackId());
            }
        };
    }

    @Bean(name = "RECOVERY_TEARDOWN_FAILED_STATE")
    public Action<?, ?> datalakeRecoveryTeardownFailedAction() {
        return new AbstractDatalakeRecoveryTeardownAction<>(DatalakeRecoveryTeardownFailedEvent.class) {

            @Override
            protected DatalakeRecoveryTeardownContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeRecoveryTeardownFailedEvent payload) {
                Flow flow = getFlow(flowParameters.getFlowId());
                flow.setFlowFailed(payload.getException());
                return DatalakeRecoveryTeardownContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(DatalakeRecoveryTeardownContext context, DatalakeRecoveryTeardownFailedEvent payload, Map<Object, Object> variables) {
//                datalakeRecoveryTeardownStatusService.handleDatalakeRecoveryTeardownFailure(context.getStackId(), payload.getException().getMessage(), payload.getDetailedStatus());
                sendEvent(context, RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT.event(), payload);
            }
        };
    }
}
