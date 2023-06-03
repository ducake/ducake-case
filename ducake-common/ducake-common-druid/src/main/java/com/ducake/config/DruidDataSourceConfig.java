package com.ducake.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.ducake.properties.DataSourceProperties;
import com.ducake.properties.DruidDataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


/**
 * @author 93477
 */
@Configuration
@EnableConfigurationProperties(DruidDataSourceProperties.class)
public class DruidDataSourceConfig {
    @Resource
    private DruidDataSourceProperties properties;

/*    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }*/

/*    @Bean
    public DynamicRoutingDataSource dynamicDataSource(DataSourceProperties dataSourceProperties) {
        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
        DataSourceContextHolder.dataSourcesMap.putAll(getDynamicDataSource());
        dynamicDataSource.setTargetDataSources(DataSourceContextHolder.dataSourcesMap);

        //默认数据源
        DruidDataSource defaultDataSource = DruidDataSourceFactory.buildDruidDataSource(dataSourceProperties);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }*/

    @Bean
    public DynamicRoutingDataSource dynamicDataSource() {
        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
        DataSourceContextHolder.DATASOURCES_MAP.putAll(getDynamicDataSource());
        dynamicDataSource.setTargetDataSources(DataSourceContextHolder.DATASOURCES_MAP);

        //默认数据源
/*        DruidDataSource defaultDataSource = DruidDataSourceFactory.buildDruidDataSource(dataSourceProperties);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);*/

        return dynamicDataSource;
    }

    private Map<Object, Object> getDynamicDataSource() {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourcePropertiesMap.size());
        dataSourcePropertiesMap.forEach((k, v) -> {
            DruidDataSource druidDataSource = DruidDataSourceFactory.buildDruidDataSource(v);
            targetDataSources.put(k, druidDataSource);
        });

        return targetDataSources;
    }


}