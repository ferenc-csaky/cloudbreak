package com.sequenceiq.cloudbreak.reactor.handler.cluster.upgrade.recover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown.DatalakeRecoveryTeardownSuccess;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.ExceptionCatcherEventHandler;

import reactor.bus.Event;

@Component
public class DatalakeRecoveryTeardownHandler extends ExceptionCatcherEventHandler<DatalakeRecoveryTeardownRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatalakeRecoveryTeardownHandler.class);

    @Override
    public String selector() {
        return EventSelectorUtil.selector(DatalakeRecoveryTeardownRequest.class);
    }

    @Override
    protected Selectable defaultFailureEvent(Long resourceId, Exception e, Event<DatalakeRecoveryTeardownRequest> event) {
        return new DatalakeRecoveryTeardownFailedEvent(resourceId, e, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
    }

    @Override
    protected Selectable doAccept(HandlerEvent event) {
        DatalakeRecoveryTeardownRequest request = event.getData();
        Selectable result;
        Long stackId = request.getResourceId();
        LOGGER.debug("Terminating instance for stack {}", stackId);
        try {
            result = new DatalakeRecoveryTeardownSuccess(stackId);
        } catch (Exception e) {
            LOGGER.error("Terminating instance for stack failed", e);
            result = new DatalakeRecoveryTeardownFailedEvent(stackId, e, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
        }
        return result;
    }
}
