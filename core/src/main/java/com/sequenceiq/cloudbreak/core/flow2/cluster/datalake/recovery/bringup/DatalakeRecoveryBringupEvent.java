package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.bringup;

import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupSuccess;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.event.EventSelectorUtil;

public enum DatalakeRecoveryBringupEvent implements FlowEvent {

    RECOVERY_BRINGUP_EVENT("RECOVERY_BRINGUP_EVENT"),
    RECOVERY_BRINGUP_FINISHED_EVENT(EventSelectorUtil.selector(DatalakeRecoveryBringupSuccess.class)),
    RECOVERY_BRINGUP_FAILED_EVENT(EventSelectorUtil.selector(DatalakeRecoveryBringupFailedEvent.class)),
    RECOVERY_BRINGUP_FINALIZED_EVENT("RECOVERY_BRINGUP_FINALIZED_EVENT"),
    RECOVERY_BRINGUP_FAIL_HANDLED_EVENT("RECOVERY_BRINGUP_FAIL_HANDLED_EVENT");

    private final String event;

    DatalakeRecoveryBringupEvent(String event) {
        this.event = event;
    }

    @Override
    public String event() {
        return event;
    }
}