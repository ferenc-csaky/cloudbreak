package com.sequenceiq.cloudbreak.reactor.api.event.recipe;

import com.sequenceiq.cloudbreak.common.event.AcceptResult;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

import reactor.rx.Promise;

public class CcmKeyDeregisterSuccess extends StackEvent {

    public CcmKeyDeregisterSuccess(Long stackId) {
        super(stackId);
    }

    public CcmKeyDeregisterSuccess(String selector, Long stackId, Promise<AcceptResult> accepted) {
        super(selector, stackId, accepted);
    }
}
