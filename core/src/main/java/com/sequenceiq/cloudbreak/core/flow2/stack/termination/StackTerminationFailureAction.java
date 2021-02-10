package com.sequenceiq.cloudbreak.core.flow2.stack.termination;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.stack.AbstractStackFailureAction;
import com.sequenceiq.cloudbreak.core.flow2.stack.StackFailureContext;
import com.sequenceiq.cloudbreak.domain.view.StackView;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.StackFailureEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.stack.TerminationType;
import com.sequenceiq.cloudbreak.service.recovery.RecoveryService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.flow.core.Flow;
import com.sequenceiq.flow.core.FlowParameters;

@Component("StackTerminationFailureAction")
public class StackTerminationFailureAction extends AbstractStackFailureAction<StackTerminationState, StackTerminationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackTerminationFailureAction.class);

    @Inject
    private StackTerminationService stackTerminationService;

    @Inject
    private RecoveryService recoveryService;

    @Inject
    private StackService stackService;

    @Override
    protected StackFailureContext createFlowContext(FlowParameters flowParameters, StateContext<StackTerminationState, StackTerminationEvent> stateContext,
            StackFailureEvent payload) {
        Flow flow = getFlow(flowParameters.getFlowId());
        StackView stackView = stackService.getViewByIdWithoutAuth(payload.getResourceId());
        MDCBuilder.buildMdcContext(stackView);
        flow.setFlowFailed(payload.getException());
        return new StackFailureContext(flowParameters, stackView);
    }

    @Override
    protected void doExecute(StackFailureContext context, StackFailureEvent payload, Map<Object, Object> variables) {
        Boolean forced = (Boolean) variables.getOrDefault(TerminationType.FORCEDTERMINATION.name(), Boolean.FALSE);
        Boolean recovery = (Boolean) variables.getOrDefault(TerminationType.RECOVERY.name(), Boolean.FALSE);
        Exception payloadException = payload.getException();
        Long stackId = context.getStackView().getId();

        try {
            if (recovery) {
                recoveryService.handleRecoveryError(stackId, payloadException);
            } else {
                stackTerminationService.handleStackTerminationError(context.getStackView(), payloadException, forced);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while Cloudbreak tried to handle stack {} error: ", recovery ? "recovery" : "termination", e);
        }
        sendEvent(context);
    }

    @Override
    protected Selectable createRequest(StackFailureContext context) {
        return new StackEvent(StackTerminationEvent.STACK_TERMINATION_FAIL_HANDLED_EVENT.event(), context.getStackView().getId());
    }

    @Override
    protected Object getFailurePayload(StackFailureEvent payload, Optional<StackFailureContext> flowContext, Exception ex) {
        return super.getFailurePayload(payload, flowContext, ex);
    }
}
