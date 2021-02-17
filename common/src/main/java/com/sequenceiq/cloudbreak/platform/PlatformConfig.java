package com.sequenceiq.cloudbreak.platform;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;

@Configuration
public class PlatformConfig {

    @Value("${cdp.platforms.supportedPlatforms}")
    private Set<CloudPlatform> cdpSupportedPlatforms;

    @Value("${cdp.platforms.experimentalPlatforms}")
    private Set<CloudPlatform> cdpExperimentalPlatforms;

    @Value("${cdp.platforms.supportedFeature.externalDatabase}")
    private Set<CloudPlatform> dbServiceSupportedPlatforms;

    @Value("${cdp.platforms.supportedFeature.stopDatabase}")
    private Set<CloudPlatform> dbServicePauseSupportedPlatforms;

    @Value("${cdp.platforms.supportedFeature.sslEnforcement}")
    private Set<CloudPlatform> dbServiceSslEnforcementSupportedPlatforms;

    /**
     * Contains platforms that are GA and should be available for all customers. UI enablement should be based on this field.
     **/
    public Set<CloudPlatform> getCdpSupportedPlatforms() {
        return dbServiceSupportedPlatforms;
    }

    /**
     * Contains platforms that should only be enabled for internal testing. ycloud,mock.
     **/
    @Deprecated
    public Set<CloudPlatform> getCdpExperimentalPlatforms() {
        return dbServiceSupportedPlatforms;
    }

    public Set<CloudPlatform> getSupportedExternalDatabasePlatforms() {
        return dbServiceSupportedPlatforms;
    }

    public Set<CloudPlatform> getExperimentalExternalDatabasePlatforms() {
        return cdpExperimentalPlatforms;
    }

    public Set<CloudPlatform> getDatabasePauseSupportedPlatforms() {
        return dbServicePauseSupportedPlatforms;
    }

    public Set<CloudPlatform> getDatabaseSslEnforcementSupportedPlatforms() {
        return dbServiceSslEnforcementSupportedPlatforms;
    }

}
