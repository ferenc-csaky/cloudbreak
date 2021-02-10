package com.sequenceiq.cloudbreak.reactor.handler.cluster.upgrade.recover;

import static com.sequenceiq.cloudbreak.util.Benchmark.measure;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.controller.StackCreatorService;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup.DatalakeRecoveryBringupSuccess;
import com.sequenceiq.cloudbreak.service.stack.InstanceMetaDataService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.ExceptionCatcherEventHandler;

import reactor.bus.Event;

@Component
public class DatalakeRecoveryBringupHandler extends ExceptionCatcherEventHandler<DatalakeRecoveryBringupRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatalakeRecoveryBringupHandler.class);

    @Inject
    private StackService stackService;

    @Inject
    private StackCreatorService stackCreatorService;

    @Inject
    private InstanceMetaDataService instanceMetaDataService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(DatalakeRecoveryBringupRequest.class);
    }

    @Override
    protected Selectable defaultFailureEvent(Long resourceId, Exception e, Event<DatalakeRecoveryBringupRequest> event) {
        return new DatalakeRecoveryBringupFailedEvent(resourceId, e, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
    }

    @Override
    protected Selectable doAccept(HandlerEvent event) {
        DatalakeRecoveryBringupRequest request = event.getData();
        Selectable result;
        Long stackId = request.getResourceId();
        LOGGER.debug("Relaunching instances for stack {}", stackId);
        try {
            Stack stack = stackService.getByIdWithClusterInTransaction(stackId);
            stackCreatorService.prepareInstanceMetadata(stack);
            measure(() -> instanceMetaDataService.saveAll(stack.getInstanceMetaDataAsList()),
                    LOGGER, "Instance metadatas saved in {} ms for stack {}", stack.getName());

            result = new DatalakeRecoveryBringupSuccess(stackId);
        } catch (Exception e) {
            LOGGER.error("Relaunching instances for stack failed", e);
            result = new DatalakeRecoveryBringupFailedEvent(stackId, e, DetailedStackStatus.CLUSTER_RECOVERY_FAILED);
        }
        return result;
    }
}
