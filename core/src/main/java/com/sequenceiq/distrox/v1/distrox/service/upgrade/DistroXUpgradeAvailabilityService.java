package com.sequenceiq.distrox.v1.distrox.service.upgrade;

import static com.sequenceiq.common.model.UpgradeShowAvailableImages.LATEST_ONLY;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.dto.NameOrCrn;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.tags.upgrade.UpgradeV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.image.ImageInfoV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.upgrade.UpgradeV4Response;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.auth.altus.CrnParseException;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.cluster.service.ClusterComponentConfigProvider;
import com.sequenceiq.cloudbreak.common.exception.BadRequestException;
import com.sequenceiq.cloudbreak.controller.validation.stack.StackRuntimeVersionValidator;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.view.StackView;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.StackViewService;
import com.sequenceiq.distrox.v1.distrox.StackOperations;

@Service
public class DistroXUpgradeAvailabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistroXUpgradeAvailabilityService.class);

    @Inject
    private EntitlementService entitlementService;

    @Inject
    private StackOperations stackOperations;

    @Inject
    private StackService stackService;

    @Inject
    private StackViewService stackViewService;

    @Inject
    private ClusterComponentConfigProvider clusterComponentConfigProvider;

    @Inject
    private StackRuntimeVersionValidator stackRuntimeVersionValidator;

    public boolean isRuntimeUpgradeEnabled(String userCrn) {
        try {
            String accountId = Crn.safeFromString(userCrn).getAccountId();
            return entitlementService.datahubRuntimeUpgradeEnabled(accountId);
        } catch (NullPointerException | CrnParseException e) {
            LOGGER.warn("Can not parse CRN to find account ID: {}", userCrn, e);
            throw new BadRequestException("Can not parse CRN to find account ID: " + userCrn);
        }
    }

    public UpgradeV4Response checkForUpgrade(NameOrCrn nameOrCrn, Long workspaceId, UpgradeV4Request request, String userCrn) {
        verifyRuntimeUpgradeEntitlement(userCrn, request);
        Stack stack = stackService.getByNameOrCrnInWorkspace(nameOrCrn, workspaceId);
        UpgradeV4Response response = stackOperations.checkForClusterUpgrade(stack, workspaceId, request);
        List<ImageInfoV4Response> filteredCandidates = filterCandidates(stack, request, response);
        response.setUpgradeCandidates(filteredCandidates);
        return response;
    }

    private List<ImageInfoV4Response> filterCandidates(Stack stack, UpgradeV4Request request, UpgradeV4Response upgradeV4Response) {
        List<ImageInfoV4Response> filteredCandidates = filterForDatalakeVersion(stack, upgradeV4Response);
        if (CollectionUtils.isNotEmpty(filteredCandidates) && Objects.nonNull(request)) {
            if (LATEST_ONLY == request.getShowAvailableImages()) {
                filteredCandidates = filterForLatestImagePerRuntime(filteredCandidates);
            } else if (request.isDryRun()) {
                filteredCandidates = filterForLatestImage(filteredCandidates);
            }
        }
        return filteredCandidates;
    }

    private List<ImageInfoV4Response> filterForLatestImage(List<ImageInfoV4Response> candidates) {
        ImageInfoV4Response latestImage = candidates.stream().max(ImageInfoV4Response.creationBasedComparator()).orElseThrow();
        LOGGER.debug("Choosing latest image with id {} as dry-run is specified", latestImage.getImageId());
        return List.of(latestImage);
    }

    private List<ImageInfoV4Response> filterForLatestImagePerRuntime(List<ImageInfoV4Response> candidates) {
        Map<String, Optional<ImageInfoV4Response>> latestImageByRuntime = candidates.stream()
                .collect(Collectors.groupingBy(imageInfoV4Response -> imageInfoV4Response.getComponentVersions().getCdp(),
                        Collectors.maxBy(ImageInfoV4Response.creationBasedComparator())));
        List<ImageInfoV4Response> filteredCandidates = latestImageByRuntime.values()
                .stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        LOGGER.debug("Filtering for latest image per runtimes {}", latestImageByRuntime.keySet());
        return filteredCandidates;
    }

    private List<ImageInfoV4Response> filterForDatalakeVersion(Stack stack, UpgradeV4Response upgradeV4Response) {
        List<ImageInfoV4Response> candidates = upgradeV4Response.getUpgradeCandidates();
        List<ImageInfoV4Response> result = candidates;
        if (CollectionUtils.isNotEmpty(candidates)) {
            Optional<StackView> datalakeViewOpt = stackViewService.findDatalakeViewByEnvironmentCrn(stack.getEnvironmentCrn());
            if (datalakeViewOpt.isPresent()) {
                Optional<String> datalakeVersionOpt = stackRuntimeVersionValidator.getCdhVersionFromClouderaManagerProducts(
                        clusterComponentConfigProvider.getClouderaManagerProductDetails(datalakeViewOpt.get().getClusterView().getId()));
                if (datalakeVersionOpt.isPresent()) {
                    String dlVersion = datalakeVersionOpt.get();
                    result = filterForDatalakeVersion(dlVersion, candidates);
                    if (result.isEmpty() && !candidates.isEmpty()) {
                        upgradeV4Response.setReason(String.format("Data Hub can only be upgraded to the same version as the Data Lake (%s)."
                                + " To upgrade your Data Hub, please upgrade your Data Lake first.", dlVersion));
                    }
                }
            }
        }
        return result;
    }

    private List<ImageInfoV4Response> filterForDatalakeVersion(String datalakeVersion, List<ImageInfoV4Response> candidates) {
        return candidates.stream().filter(imageInfo -> imageInfo.getComponentVersions().getCdp().equals(datalakeVersion)).collect(Collectors.toList());
    }

    private void verifyRuntimeUpgradeEntitlement(String userCrn, UpgradeV4Request request) {
        if (request != null && !Boolean.TRUE.equals(request.getLockComponents()) && !isRuntimeUpgradeEnabled(userCrn)) {
            throw new BadRequestException("Runtime upgrade feature is not enabled");
        }
    }
}
