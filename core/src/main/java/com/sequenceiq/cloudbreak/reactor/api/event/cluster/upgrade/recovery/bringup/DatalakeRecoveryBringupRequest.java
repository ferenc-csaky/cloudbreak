package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryBringupRequest extends StackEvent {

    public DatalakeRecoveryBringupRequest(Long stackId) {
        super(stackId);
    }
}
