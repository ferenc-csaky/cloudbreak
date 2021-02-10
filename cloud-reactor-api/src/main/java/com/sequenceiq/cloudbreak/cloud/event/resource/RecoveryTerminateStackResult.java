package com.sequenceiq.cloudbreak.cloud.event.resource;

public class RecoveryTerminateStackResult extends TerminateStackResult {
    // TODO: Delete if not needed
    public RecoveryTerminateStackResult(Long resourceId) {
        super(resourceId);
    }

    public RecoveryTerminateStackResult(String statusReason, Exception errorDetails, Long resourceId) {
        super(statusReason, errorDetails, resourceId);
    }
}
