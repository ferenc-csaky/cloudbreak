package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryBringupSuccess extends StackEvent {

    public DatalakeRecoveryBringupSuccess(Long stackId) {
        super(stackId);
    }
}
