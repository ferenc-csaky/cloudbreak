package com.sequenceiq.cloudbreak.core.flow2.stack.termination;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.cloud.event.resource.TerminateStackResult;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.stack.TerminationType;
import com.sequenceiq.cloudbreak.service.recovery.RecoveryService;

@Component("StackTerminationFinishedAction")
public class StackTerminationFinishedAction extends AbstractStackTerminationAction<TerminateStackResult> {

    @Inject
    private StackTerminationService stackTerminationService;

    @Inject
    private RecoveryService recoveryService;

    public StackTerminationFinishedAction() {
        super(TerminateStackResult.class);
    }

    @Override
    protected void doExecute(StackTerminationContext context, TerminateStackResult payload, Map<Object, Object> variables) {
        boolean recovery = (boolean) variables.getOrDefault(TerminationType.RECOVERY.name(), false);
        if (recovery) {
            recoveryService.finishTeardown(context, payload);
        } else {
            Boolean forcedTermination = (Boolean) variables.getOrDefault(TerminationType.FORCEDTERMINATION.name(), Boolean.FALSE);
            stackTerminationService.finishStackTermination(context, payload, forcedTermination);
        }
        sendEvent(context);
    }

    @Override
    protected Selectable createRequest(StackTerminationContext context) {
        return new StackEvent(StackTerminationEvent.TERMINATION_FINALIZED_EVENT.event(), context.getStack().getId());
    }
}
