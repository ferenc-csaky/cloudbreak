package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.bringup;

import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.bringup.DatalakeRecoveryBringupEvent.RECOVERY_BRINGUP_FAIL_HANDLED_EVENT;

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
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupSuccess;
import com.sequenceiq.flow.core.Flow;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.flow.core.FlowState;

@Configuration
public class DatalakeRecoveryBringupActions {

    @Inject
    private DatalakeRecoveryBringupService datalakeRecoveryBringupStatusService;

    @Bean(name = "RECOVERY_BRINGUP_STATE")
    public Action<?, ?> startDatalakeRecoveryBringup() {
        return new AbstractDatalakeRecoveryBringupAction<>(ClusterRecoveryTriggerEvent.class) {

            @Override
            protected void doExecute(DatalakeRecoveryBringupContext context, ClusterRecoveryTriggerEvent payload, Map<Object, Object> variables) {
//                datalakeRecoveryBringupStatusService.bringupDatalakeRecovery(context.getStackId(), context.getDatalakeRecoveryId());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(DatalakeRecoveryBringupContext context) {
                return new DatalakeRecoveryBringupRequest(context.getStackId());
            }

            @Override
            protected Object getFailurePayload(ClusterRecoveryTriggerEvent payload, Optional<DatalakeRecoveryBringupContext> flowContext, Exception ex) {
                return DatalakeRecoveryBringupFailedEvent.from(payload, ex, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
            }
        };
    }

    @Bean(name = "RECOVERY_BRINGUP_FINISHED_STATE")
    public Action<?, ?> datalakeRecoveryBringupFinished() {
        return new AbstractDatalakeRecoveryBringupAction<>(DatalakeRecoveryBringupSuccess.class) {

            @Override
            protected void doExecute(DatalakeRecoveryBringupContext context, DatalakeRecoveryBringupSuccess payload, Map<Object, Object> variables) {
//                datalakeRecoveryBringupStatusService.bringupDatalakeRecoveryFinished(context.getStackId());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(DatalakeRecoveryBringupContext context) {
                return new StackEvent(DatalakeRecoveryBringupEvent.RECOVERY_BRINGUP_FINALIZED_EVENT.event(), context.getStackId());
            }
        };
    }

    @Bean(name = "RECOVERY_BRINGUP_FAILED_STATE")
    public Action<?, ?> datalakeRecoveryBringupFailedAction() {
        return new AbstractDatalakeRecoveryBringupAction<>(DatalakeRecoveryBringupFailedEvent.class) {

            @Override
            protected DatalakeRecoveryBringupContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeRecoveryBringupFailedEvent payload) {
                Flow flow = getFlow(flowParameters.getFlowId());
                flow.setFlowFailed(payload.getException());
                return DatalakeRecoveryBringupContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(DatalakeRecoveryBringupContext context, DatalakeRecoveryBringupFailedEvent payload, Map<Object, Object> variables) {
//                datalakeRecoveryBringupStatusService.handleDatalakeRecoveryBringupFailure(context.getStackId(), payload.getException().getMessage(), payload.getDetailedStatus());
                sendEvent(context, RECOVERY_BRINGUP_FAIL_HANDLED_EVENT.event(), payload);
            }
        };
    }
}
