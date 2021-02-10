package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FAILED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FINALIZED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownEvent.RECOVERY_TEARDOWN_FINISHED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownState.FINAL_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownState.INIT_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownState.RECOVERY_TEARDOWN_FAILED_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownState.RECOVERY_TEARDOWN_FINISHED_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown.DatalakeRecoveryTeardownState.RECOVERY_TEARDOWN_STATE;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sequenceiq.flow.core.config.AbstractFlowConfiguration;
import com.sequenceiq.flow.core.config.RetryableFlowConfiguration;

@Component
public class DatalakeRecoveryTeardownFlowConfig extends AbstractFlowConfiguration<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent>
    implements RetryableFlowConfiguration<DatalakeRecoveryTeardownEvent> {

    private static final List<Transition<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent>> TRANSITIONS =
        new Transition.Builder<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent>()
                .defaultFailureEvent(RECOVERY_TEARDOWN_FAILED_EVENT)

                .from(INIT_STATE).to(RECOVERY_TEARDOWN_STATE)
                .event(RECOVERY_TEARDOWN_EVENT)
                .defaultFailureEvent()

                .from(RECOVERY_TEARDOWN_STATE).to(RECOVERY_TEARDOWN_FINISHED_STATE)
                .event(RECOVERY_TEARDOWN_FINISHED_EVENT)
                .defaultFailureEvent()

                .from(RECOVERY_TEARDOWN_FINISHED_STATE).to(FINAL_STATE)
                .event(RECOVERY_TEARDOWN_FINALIZED_EVENT)
                .defaultFailureEvent()

                .build();

    private static final FlowEdgeConfig<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent> EDGE_CONFIG =
        new FlowEdgeConfig<>(INIT_STATE, FINAL_STATE, RECOVERY_TEARDOWN_FAILED_STATE, RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT);

    public DatalakeRecoveryTeardownFlowConfig() {
        super(DatalakeRecoveryTeardownState.class, DatalakeRecoveryTeardownEvent.class);
    }

    @Override
    protected List<Transition<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent>> getTransitions() {
        return TRANSITIONS;
    }

    @Override
    protected FlowEdgeConfig<DatalakeRecoveryTeardownState, DatalakeRecoveryTeardownEvent> getEdgeConfig() {
        return EDGE_CONFIG;
    }

    @Override
    public DatalakeRecoveryTeardownEvent[] getEvents() {
        return DatalakeRecoveryTeardownEvent.values();
    }

    @Override
    public DatalakeRecoveryTeardownEvent[] getInitEvents() {
        return new DatalakeRecoveryTeardownEvent[]{
            RECOVERY_TEARDOWN_EVENT
        };
    }

    @Override
    public String getDisplayName() {
        return "Tearing down SDX instances for recovery";
    }

    @Override
    public DatalakeRecoveryTeardownEvent getRetryableEvent() {
        return RECOVERY_TEARDOWN_FAILED_EVENT;
    }
}
