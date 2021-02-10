package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryTeardownSuccess extends StackEvent {

    public DatalakeRecoveryTeardownSuccess(Long stackId) {
        super(stackId);
    }
}
