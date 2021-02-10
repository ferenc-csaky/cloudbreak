package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake.recovery.teardown;

import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownSuccess;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.event.EventSelectorUtil;

public enum DatalakeRecoveryTeardownEvent implements FlowEvent {

    RECOVERY_TEARDOWN_EVENT("RECOVERY_TEARDOWN_EVENT"),
    RECOVERY_TEARDOWN_FINISHED_EVENT(EventSelectorUtil.selector(DatalakeRecoveryTeardownSuccess.class)),
    RECOVERY_TEARDOWN_FAILED_EVENT(EventSelectorUtil.selector(DatalakeRecoveryTeardownFailedEvent.class)),
    RECOVERY_TEARDOWN_FINALIZED_EVENT("RECOVERY_TEARDOWN_FINALIZED_EVENT"),
    RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT("RECOVERY_TEARDOWN_FAIL_HANDLED_EVENT");

    private final String event;

    DatalakeRecoveryTeardownEvent(String event) {
        this.event = event;
    }

    @Override
    public String event() {
        return event;
    }
}
