package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import com.sequenceiq.flow.core.FlowState;
import com.sequenceiq.flow.core.RestartAction;

public enum DatalakeRecoveryTeardownState implements FlowState {
    INIT_STATE,
//    // ClusterTerminationFlow
//    PREPARE_CLUSTER_STATE,
//    DISABLE_KERBEROS_STATE,
//    CLUSTER_TERMINATION_FINISH_STATE,
//
//    // StackTerminationFlow
//    PRE_TERMINATION_STATE,
//    CLUSTER_PROXY_DEREGISTER_STATE,
//    CCM_KEY_DEREGISTER_STATE,
//    TERMINATION_STATE,
//    TERMINATION_FINISHED_STATE,
    RECOVERY_TEARDOWN_STATE,
    RECOVERY_TEARDOWN_FAILED_STATE,
    RECOVERY_TEARDOWN_FINISHED_STATE,
    FINAL_STATE;

    // TODO: fill
    @Override
    public Class<? extends RestartAction> restartAction() {
        return null;
    }
}
