package com.sequenceiq.cloudbreak.cloud.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.microsoft.azure.management.resources.Subscription;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClientService;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.encryption.DiskEncryptionSetCreationRequest;

@RunWith(MockitoJUnitRunner.class)
public class AzureEncryptionResourcesTest {

    @InjectMocks
    private AzureEncryptionResources underTest;

    @Mock
    private AzureClientService azureClientService;

    @Mock
    private AzureClient azureClient;

    @Mock
    private AzureUtils azureUtils;

    @Test
    public void testPlatformShouldReturnAzurePlatform() {
        Platform actual = underTest.platform();

        assertEquals(AzureConstants.PLATFORM, actual);
    }

    @Test
    public void testVariantShouldReturnAzurePlatform() {
        Variant actual = underTest.variant();

        assertEquals(AzureConstants.VARIANT, actual);
    }

    @Test
    public void testExceptionIsThrownWhenNotIsSingleResourceGroup() {
        DiskEncryptionSetCreationRequest requestedSet = new DiskEncryptionSetCreationRequest.Builder()
                .withCloudCredential(new CloudCredential())
                .withRegion(Region.region("dummyRegion"))
                .withEnvironmentName("dummyEnvName")
                .withEnvironmentId(1L)
                .withSingleResourceGroup(false)
                .withResourceGroupName("dummyResourceGroup")
                .withTags(new HashMap<>())
                .withEncryptionKeyUrl("https://dummyVaultName.vault.azure.net/keys/dummyKeyName/dummyKeyVersion")
                .build();
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        assertThrows(IllegalArgumentException.class, () -> underTest.createDiskEncryptionSet(requestedSet));
    }

    @Test
    public void testExceptionIsThrownWhenVaultNameIsNotFound() {
        DiskEncryptionSetCreationRequest requestedSet = new DiskEncryptionSetCreationRequest.Builder()
                .withCloudCredential(new CloudCredential())
                .withRegion(Region.region("dummyRegion"))
                .withEnvironmentName("dummyEnvName")
                .withEnvironmentId(1L)
                .withSingleResourceGroup(true)
                .withResourceGroupName("dummyResourceGroup")
                .withTags(new HashMap<>())
                .withEncryptionKeyUrl("wrongKeyUrl")
                .build();
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        assertThrows(IllegalArgumentException.class, () -> underTest.createDiskEncryptionSet(requestedSet));
    }

    @Test
    public void testCreateDiskEncryptionSetShoulMakeCloudCallAndThrowException() {
        DiskEncryptionSetCreationRequest requestedSet = new DiskEncryptionSetCreationRequest.Builder()
                .withCloudCredential(new CloudCredential())
                .withRegion(Region.region("dummyRegion"))
                .withEnvironmentName("dummyEnvName")
                .withEnvironmentId(1L)
                .withSingleResourceGroup(true)
                .withResourceGroupName("dummyResourceGroup")
                .withTags(new HashMap<>())
                .withEncryptionKeyUrl("https://dummyVaultName.vault.azure.net/keys/dummyKeyName/dummyKeyVersion")
                .build();
        Subscription subscription = mock(Subscription.class);
        when(subscription.subscriptionId()).thenReturn("dummySubscriptionId");
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.getCurrentSubscription()).thenReturn(subscription);
        //Call to createOrUpdateDiskEncryptionSet is made and exception is thrown because of dummy parameters.
        assertThrows(RuntimeException.class, () -> underTest.createDiskEncryptionSet(requestedSet));
    }
}