package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryTeardownFailedEvent extends StackEvent {

    private final Exception exception;

    private final DetailedStackStatus detailedStatus;

    public DatalakeRecoveryTeardownFailedEvent(Long stackId, Exception exception, DetailedStackStatus detailedStatus) {
        super(stackId);
        this.exception = exception;
        this.detailedStatus = detailedStatus;
    }

    public static DatalakeRecoveryTeardownFailedEvent from(StackEvent event, Exception exception, DetailedStackStatus detailedStatus) {
        return new DatalakeRecoveryTeardownFailedEvent(event.getResourceId(), exception, detailedStatus);
    }

    public Exception getException() {
        return exception;
    }

    public DetailedStackStatus getDetailedStatus() {
        return detailedStatus;
    }
}
