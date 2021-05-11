package com.nettyboot.shardingproxy;

import com.google.common.primitives.Ints;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.config.log.ConfigurationLogger;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.impl.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.opentracing.ShardingTracer;
import org.apache.shardingsphere.orchestration.internal.registry.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.config.ShardingConfiguration;
import org.apache.shardingsphere.shardingproxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.frontend.bootstrap.ShardingProxy;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding-Proxy Bootstrap.
 *
 * @author zhangliang
 * @author wangkai
 * @author panjuan
 * @author sunbufu
 */
public final class ProxyBootstrap {

    private static final int DEFAULT_PORT = 3307;

    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        ShardingConfiguration shardingConfig = new ShardingConfigurationLoader().load();
        logRuleConfigurationMap(getRuleConfiguration(shardingConfig.getRuleConfigurationMap()).values());
        int port = getPort(args);
        if (null == shardingConfig.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(shardingConfig.getRuleConfigurationMap(), shardingConfig.getServerConfiguration().getAuthentication(), shardingConfig.getServerConfiguration().getProps(), port);
        } else {
            startWithRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap().keySet(), shardingConfig.getRuleConfigurationMap(), port);
        }
    }

    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        Integer paredPort = Ints.tryParse(args[0]);
        return paredPort == null ? DEFAULT_PORT : paredPort;
    }

    private static void startWithoutRegistryCenter(final Map<String, YamlProxyRuleConfiguration> ruleConfigs,
                                                   final YamlAuthenticationConfiguration authentication, final Properties prop, final int port) throws SQLException {
        Authentication authenticationConfiguration = getAuthentication(authentication);
        ConfigurationLogger.log(authenticationConfiguration);
        ConfigurationLogger.log(prop);
        ShardingProxyContext.getInstance().init(authenticationConfiguration, prop);
        LogicSchemas.getInstance().init(getDataSourceParameterMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
        initOpenTracing();
        ShardingProxy.getInstance().start(port);
    }

    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfig,
                                                final Collection<String> shardingSchemaNames, final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final int port) {
        try (ShardingOrchestrationFacade shardingOrchestrationFacade = new ShardingOrchestrationFacade(
                new OrchestrationConfigurationYamlSwapper().swap(serverConfig.getOrchestration()), shardingSchemaNames)) {
            initShardingOrchestrationFacade(serverConfig, ruleConfigs, shardingOrchestrationFacade);
            Authentication authentication = shardingOrchestrationFacade.getConfigService().loadAuthentication();
            Properties properties = shardingOrchestrationFacade.getConfigService().loadProperties();
            ConfigurationLogger.log(authentication);
            ConfigurationLogger.log(properties);
            ShardingProxyContext.getInstance().init(authentication, properties);
            LogicSchemas.getInstance().init(shardingSchemaNames, getSchemaDataSourceParameterMap(shardingOrchestrationFacade), getSchemaRules(shardingOrchestrationFacade), true);
            initOpenTracing();
            ShardingProxy.getInstance().start(port);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Map<String, YamlDataSourceParameter>> getSchemaDataSourceParameterMap(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, Map<String, YamlDataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigService().getAllShardingSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(shardingOrchestrationFacade.getConfigService().loadDataSourceConfigurations(each)));
        }
        return result;
    }

    private static Map<String, RuleConfiguration> getSchemaRules(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, RuleConfiguration> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigService().getAllShardingSchemaNames()) {
            if (shardingOrchestrationFacade.getConfigService().isEncryptRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigService().loadEncryptRuleConfiguration(each));
            } else if (shardingOrchestrationFacade.getConfigService().isShardingRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigService().loadShardingRuleConfiguration(each));
            } else {
                result.put(each, shardingOrchestrationFacade.getConfigService().loadMasterSlaveRuleConfiguration(each));
            }
        }
        return result;
    }

    private static void initShardingOrchestrationFacade(
            final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        if (ruleConfigs.isEmpty()) {
            shardingOrchestrationFacade.init();
        } else {
            shardingOrchestrationFacade.init(getDataSourceConfigurationMap(ruleConfigs),
                    getRuleConfiguration(ruleConfigs), getAuthentication(serverConfig.getAuthentication()), serverConfig.getProps());
        }
    }

    private static void initOpenTracing() {
        if (ShardingProxyContext.getInstance().getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_OPENTRACING_ENABLED)) {
            ShardingTracer.init();
        }
    }

    private static Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(entry.getValue().getDataSources()));
        }
        return result;
    }

    private static Map<String, Map<String, YamlDataSourceParameter>> getDataSourceParameterMap(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, YamlDataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }

    private static Map<String, RuleConfiguration> getRuleConfiguration(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, RuleConfiguration> result = new HashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            if (null != entry.getValue().getShardingRule()) {
                result.put(entry.getKey(), new ShardingRuleConfigurationYamlSwapper().swap(entry.getValue().getShardingRule()));
            } else if (null != entry.getValue().getMasterSlaveRule()) {
                result.put(entry.getKey(), new MasterSlaveRuleConfigurationYamlSwapper().swap(entry.getValue().getMasterSlaveRule()));
            } else if (null != entry.getValue().getEncryptRule()) {
                result.put(entry.getKey(), new EncryptRuleConfigurationYamlSwapper().swap(entry.getValue().getEncryptRule()));
            }
        }
        return result;
    }

    private static Authentication getAuthentication(final YamlAuthenticationConfiguration yamlAuthenticationConfig) {
        return new AuthenticationYamlSwapper().swap(yamlAuthenticationConfig);
    }

    /**
     * Log rule configurations.
     *
     * @param ruleConfigurations log rule configurations
     */
    private static void logRuleConfigurationMap(final Collection<RuleConfiguration> ruleConfigurations) {
        if (CollectionUtils.isNotEmpty(ruleConfigurations)) {
            for (RuleConfiguration each : ruleConfigurations) {
                ConfigurationLogger.log(each);
            }
        }
    }
}
