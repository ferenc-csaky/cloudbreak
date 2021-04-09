package com.sequenceiq.cloudbreak.cloud.azure;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.azure.management.compute.implementation.DiskEncryptionSetInner;
import com.sequenceiq.cloudbreak.cloud.EncryptionResources;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClientService;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.encryption.CreatedDiskEncryptionSet;
import com.sequenceiq.cloudbreak.cloud.model.encryption.DiskEncryptionSetCreationRequest;

@Service
public class AzureEncryptionResources implements EncryptionResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEncryptionResources.class);

    private static final Pattern ENCRYPTION_KEY_URL_PATTERN = Pattern.compile("https://([^.]+)\\.vault.*");

    @Inject
    private AzureClientService azureClientService;

    @Inject
    private AzureUtils azureUtils;

    @Override
    public Platform platform() {
        return AzureConstants.PLATFORM;
    }

    @Override
    public Variant variant() {
        return AzureConstants.VARIANT;
    }

    @Override
    public CreatedDiskEncryptionSet createDiskEncryptionSet(DiskEncryptionSetCreationRequest diskEncryptionSetCreationRequest) {
        AzureClient azureClient = azureClientService.getClient(diskEncryptionSetCreationRequest.getCloudCredential());
        String encryptionKeyUrl = diskEncryptionSetCreationRequest.getEncryptionKeyUrl();
        String vaultName;
        String vaultResourceGroupName;
        String desResourceGroupName;

        Matcher matcher = ENCRYPTION_KEY_URL_PATTERN.matcher(encryptionKeyUrl);
        if (matcher.matches()) {
            vaultName = matcher.group(1);
        } else {
            throw new IllegalArgumentException("vaultName cannot be fetched from encryptionKeyUrl. encryptionKeyUrl should be of format - " +
                    "'https://<vaultName>.vault.azure.net/keys/<keyName>/<keyVersion>'");
        }
        if (diskEncryptionSetCreationRequest.isSingleResourceGroup()) {
            desResourceGroupName = diskEncryptionSetCreationRequest.getResourceGroup();
            vaultResourceGroupName = desResourceGroupName;
        } else {
            throw new IllegalArgumentException("Customer Managed Key Encryption for managed Azure disks is supported for single resource group only.");
        }
        String sourceVaultId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.KeyVault/vaults/%s",
                azureClient.getCurrentSubscription().subscriptionId(), vaultResourceGroupName, vaultName);

        CreatedDiskEncryptionSet diskEncryptionSet = getOrCreateDiskEncryptionSetOnCloud(
                azureClient,
                encryptionKeyUrl,
                desResourceGroupName,
                sourceVaultId,
                diskEncryptionSetCreationRequest);
        azureClient.grantKeyVaultAccessPolicyToServicePrincipal(vaultResourceGroupName, vaultName,
                diskEncryptionSet.getDiskEncryptionSetPrincipalId());
        return diskEncryptionSet;
    }

    public CreatedDiskEncryptionSet getOrCreateDiskEncryptionSetOnCloud(AzureClient azureClient, String encryptionKeyUrl, String desResourceGroupName,
            String sourceVaultId, DiskEncryptionSetCreationRequest diskEncryptionSetCreationRequest) {
        String diskEncryptionSetName = azureUtils.generateDesNameByNameAndId(
                String.format("%s-DES-", diskEncryptionSetCreationRequest.getEnvironmentName()),
                diskEncryptionSetCreationRequest.getId());
        DiskEncryptionSetInner createdSet;
        LOGGER.info("Checking if Disk Encryption Set {} exists on cloud Azure", diskEncryptionSetName);
        createdSet = azureClient.getDiskEncryptionSet(desResourceGroupName, diskEncryptionSetName);
        if (Objects.isNull(createdSet)) {
            LOGGER.info("Creating Disk Encryption Set {}", diskEncryptionSetName);
            createdSet = azureClient.createOrUpdateDiskEncryptionSet(diskEncryptionSetName, encryptionKeyUrl,
                    diskEncryptionSetCreationRequest.getRegion().getRegionName(), desResourceGroupName, sourceVaultId,
                    diskEncryptionSetCreationRequest.getTags());
        }
        if (!Objects.isNull(createdSet)) {
            return new CreatedDiskEncryptionSet.Builder()
                    .withDiskEncryptionSetId(createdSet.id())
                    .withDiskEncryptionSetPrincipalId(createdSet.identity().principalId())
                    .withDiskEncryptionSetLocation(createdSet.location())
                    .withDiskEncryptionSetName(createdSet.name())
                    .withTags(createdSet.getTags())
                    .withDiskEncryptionSetResourceGroup(desResourceGroupName)
                    .build();
        } else {
            throw new CloudConnectorException("Failed to create Disk Encryption Set.");
        }
    }
}