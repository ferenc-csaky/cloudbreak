package com.sequenceiq.datalake.service.upgrade.recovery;

import static com.sequenceiq.cloudbreak.util.Benchmark.measure;

import java.util.Collections;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.dto.NameOrCrn;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.StackV4Endpoint;
import com.sequenceiq.cloudbreak.auth.ClouderaManagerLicenseProvider;
import com.sequenceiq.cloudbreak.auth.PaywallAccessChecker;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.common.exception.BadRequestException;
import com.sequenceiq.cloudbreak.event.ResourceEvent;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.message.CloudbreakMessagesService;
import com.sequenceiq.datalake.controller.sdx.SdxUpgradeClusterConverter;
import com.sequenceiq.datalake.entity.DatalakeStatusEnum;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.entity.SdxStatusEntity;
import com.sequenceiq.datalake.flow.SdxReactorFlowManager;
import com.sequenceiq.datalake.service.sdx.SdxService;
import com.sequenceiq.datalake.service.sdx.status.SdxStatusService;
import com.sequenceiq.flow.api.model.FlowIdentifier;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryType;

@Component
public class SdxUpgradeRecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdxUpgradeRecoveryService.class);

    private static final long WORKSPACE_ID = 0L;

    @Value("${sdx.paywall.url}")
    private String paywallUrl;

    @Inject
    private StackV4Endpoint stackV4Endpoint;

    @Inject
    private SdxService sdxService;

    @Inject
    private SdxStatusService sdxStatusService;

    @Inject
    private SdxReactorFlowManager sdxReactorFlowManager;

    @Inject
    private CloudbreakMessagesService messagesService;

    @Inject
    private SdxUpgradeClusterConverter sdxUpgradeClusterConverter;

    @Inject
    private EntitlementService entitlementService;

    @Inject
    private PaywallAccessChecker paywallAccessChecker;

    @Inject
    private ClouderaManagerLicenseProvider clouderaManagerLicenseProvider;

    public SdxRecoveryResponse triggerRecovery(String userCrn, NameOrCrn clusterNameOrCrn, SdxRecoveryRequest recoverRequest) {
        SdxCluster cluster;
        if (clusterNameOrCrn.hasName()) {
            cluster = sdxService.getByNameInAccount(userCrn, clusterNameOrCrn.getName());
        } else {
            cluster = sdxService.getByCrn(userCrn, clusterNameOrCrn.getCrn());
        }
        return initSdxRecovery(userCrn, recoverRequest, cluster);
    }

    private SdxRecoveryResponse initSdxRecovery(String userCrn, SdxRecoveryRequest request, SdxCluster cluster) {

        // Request time validations here
        validateDatalakeStatus(cluster);
        // Fetch latest backup id for given runtime from datalake-dr service
        String backupId = "";

        FlowIdentifier flowIdentifier = triggerDatalakeUpgradeRecoveryFlow(request.getType(), cluster);
        String message = getMessage(backupId);
        return new SdxRecoveryResponse(message, flowIdentifier);
    }

    private void validateDatalakeStatus(SdxCluster cluster) {
        String clusterName = cluster.getClusterName();
        SdxStatusEntity actualStatusForSdx = measure(() -> sdxStatusService.getActualStatusForSdx(cluster), LOGGER,
                "Fetching SDX status took {}ms from DB. Name: [{}]", clusterName);
        if (Objects.isNull(actualStatusForSdx)) {
            throw new BadRequestException(String.format("Datalake cluster status with name %s could not be determined", clusterName));
        } else if (actualStatusForSdx.getStatus() != DatalakeStatusEnum.DATALAKE_UPGRADE_FAILED) {
//            throw new BadRequestException(String.format("Datalake cluster status is %s, it should be in "
//                            + "DATALAKE_UPGRADE_FAILED status to be able to start the recovery", actualStatusForSdx.getStatus().name()));
        }
    }

    private FlowIdentifier triggerDatalakeUpgradeRecoveryFlow(SdxRecoveryType recoveryType, SdxCluster cluster) {
        MDCBuilder.buildMdcContext(cluster);
        return sdxReactorFlowManager.triggerDatalakeRuntimeUpgradeRecoveryFlow(cluster, recoveryType);
    }

    private String getMessage(String imageId) {
        return messagesService.getMessage(ResourceEvent.DATALAKE_UPGRADE.getMessage(), Collections.singletonList(imageId));
    }

}
