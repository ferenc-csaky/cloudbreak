package com.sequenceiq.environment.experience.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sequenceiq.environment.environment.dto.EnvironmentExperienceDto;
import com.sequenceiq.environment.experience.ExperienceCluster;
import com.sequenceiq.environment.experience.ExperienceSource;
import com.sequenceiq.environment.experience.common.responses.CpInternalCluster;
import com.sequenceiq.environment.experience.config.ExperienceServicesConfig;

@ExtendWith(MockitoExtension.class)
class CommonExperienceServiceTest {

    private static final String TENANT = "someTenantValue";

    private static final String ENV_CRN = "someEnvCrnValue";

    private static final String XP_API = "https://127.0.0.1:9999";

    private static final String XP_INTERNAL_ENV_ENDPOINT = "/somexp/api/v3/cp-internal/environment/{crn}";

    private static final String TEST_XP_NAME = "AWESOME_XP";

    private static final int ONCE = 1;

    @Mock
    private CommonExperienceConnectorService mockExperienceConnectorService;

    @Mock
    private CommonExperiencePathCreator mockCommonExperiencePathCreator;

    @Mock
    private ExperienceServicesConfig mockExperienceServicesConfig;

    @Mock
    private CommonExperienceValidator mockExperienceValidator;

    @Mock
    private EnvironmentExperienceDto mockEnvironment;

    @Mock
    private CommonExperience mockCommonExperience;

    private CommonExperienceService underTest;

    @BeforeEach
    void setUp() {
        lenient().when(mockEnvironment.getCrn()).thenReturn(ENV_CRN);
        lenient().when(mockEnvironment.getAccountId()).thenReturn(TENANT);
        lenient().when(mockEnvironment.getName()).thenReturn(TEST_XP_NAME);
        lenient().when(mockCommonExperience.getName()).thenReturn(TEST_XP_NAME);
        lenient().when(mockCommonExperience.getInternalEnvironmentEndpoint()).thenReturn(XP_INTERNAL_ENV_ENDPOINT);
        lenient().when(mockCommonExperience.getAddress()).thenReturn(XP_API);
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenEnvironmentCrnIsNullThenIllegalArgumentExceptionComes() {
        when(mockEnvironment.getCrn()).thenReturn(null);

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.getConnectedClustersForEnvironment(mockEnvironment));

        assertNotNull(exception);
        assertEquals("Unable to check environment - experience relation, since the " +
                "given environment crn is null or empty!", exception.getMessage());
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenEnvironmentCrnIsEmptyThenIllegalArgumentExceptionComes() {
        when(mockEnvironment.getCrn()).thenReturn("");

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.getConnectedClustersForEnvironment(mockEnvironment));

        assertNotNull(exception);
        assertEquals("Unable to check environment - experience relation, since the " +
                "given environment crn is null or empty!", exception.getMessage());
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenNoConfiguredExperienceExistsThenNoXpConnectorServiceCallHappens() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(Collections.emptyList());

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        underTest.getConnectedClustersForEnvironment(mockEnvironment);

        verify(mockExperienceConnectorService, never()).getExperienceClustersConnectedToEnv(any(), any());
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenNoConfiguredExperienceExistsThenEmptySetReturns() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(Collections.emptyList());

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        Set<ExperienceCluster> clusters = underTest.getConnectedClustersForEnvironment(mockEnvironment);

        assertThat(clusters).isEmpty();
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenHaveConfiguredExperienceButItsNotProperlyFilledThenNoXpConnectorServiceCallHappens() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(false);

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        underTest.getConnectedClustersForEnvironment(mockEnvironment);

        verify(mockExperienceConnectorService, never()).getExperienceClustersConnectedToEnv(any(), any());
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenHaveConfiguredExperienceButItsNotProperlyFilledThenEmptySetReturns() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(false);

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        Set<ExperienceCluster> clusters = underTest.getConnectedClustersForEnvironment(mockEnvironment);

        assertThat(clusters).isEmpty();
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenExperienceIsConfiguredThenPathToExperienceShouldBeCombindedProperly() {
        String expectedPath = XP_API + XP_INTERNAL_ENV_ENDPOINT;
        when(mockCommonExperiencePathCreator.createPathToExperience(mockCommonExperience)).thenReturn(expectedPath);
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(true);

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        underTest.getConnectedClustersForEnvironment(mockEnvironment);

        verify(mockExperienceConnectorService, times(ONCE)).getExperienceClustersConnectedToEnv(any(), any());
        verify(mockExperienceConnectorService, times(ONCE)).getExperienceClustersConnectedToEnv(expectedPath, ENV_CRN);
        verify(mockCommonExperiencePathCreator, times(ONCE)).createPathToExperience(any());
        verify(mockCommonExperiencePathCreator, times(ONCE)).createPathToExperience(mockCommonExperience);
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenExperienceIsConfiguredButHasNoActiveWorkspaceForEnvThenEmptySetReturns() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(true);

        when(mockExperienceConnectorService.getExperienceClustersConnectedToEnv(any(), eq(ENV_CRN))).thenReturn(Collections.emptySet());

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        Set<ExperienceCluster> clusters = underTest.getConnectedClustersForEnvironment(mockEnvironment);

        assertThat(clusters).isEmpty();
    }

    @Test
    void testHasExistingClusterForEnvironmentWhenExperienceIsConfiguredAndHasActiveWorkspaceForEnvThenItemReturns() {
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(true);

        CpInternalCluster cluster = new CpInternalCluster();
        cluster.setName("Workload1");
        cluster.setCrn("crn1");
        cluster.setStatus("AVAILABLE");
        when(mockExperienceConnectorService.getExperienceClustersConnectedToEnv(any(), eq(ENV_CRN))).thenReturn(Set.of(cluster));

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        Set<ExperienceCluster> clusters = underTest.getConnectedClustersForEnvironment(mockEnvironment);

        ExperienceCluster expected = ExperienceCluster.builder()
                .withExperienceName(TEST_XP_NAME)
                .withName("Workload1")
                .withStatus("AVAILABLE")
                .build();

        assertThat(clusters).containsOnly(expected);
    }

    @Test
    void testDeleteConnectedExperiences() {
        String xpPath = "somePath";
        when(mockExperienceValidator.isExperienceFilled(mockCommonExperience)).thenReturn(true);
        when(mockExperienceServicesConfig.getConfigs()).thenReturn(List.of(mockCommonExperience));
        when(mockCommonExperiencePathCreator.createPathToExperience(mockCommonExperience)).thenReturn(xpPath);

        CpInternalCluster cluster = new CpInternalCluster();
        cluster.setName("Workload1");
        cluster.setCrn("crn1");
        cluster.setStatus("AVAILABLE");
        when(mockExperienceConnectorService.getExperienceClustersConnectedToEnv(any(), eq(ENV_CRN))).thenReturn(Set.of(cluster));

        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        underTest.deleteConnectedExperiences(mockEnvironment);

        verify(mockExperienceConnectorService, times(ONCE)).deleteWorkspaceForEnvironment(any(), any());
        verify(mockExperienceConnectorService, times(ONCE)).deleteWorkspaceForEnvironment(xpPath, ENV_CRN);
    }

    @Test
    void testGetSourceReturnsBasicType() {
        underTest = new CommonExperienceService(mockExperienceConnectorService, mockExperienceServicesConfig, mockExperienceValidator,
                mockCommonExperiencePathCreator);

        ExperienceSource expectedSource = underTest.getSource();

        assertEquals(expectedSource, ExperienceSource.BASIC);
    }

}
