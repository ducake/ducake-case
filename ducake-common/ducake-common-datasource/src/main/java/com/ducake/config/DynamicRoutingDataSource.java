package com.ducake.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * @author 93477
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSource();
    }

    /**
     * 运行时动态添加、删除新数据源
     * TODO：将配置文件数据源与后添加数据源区分，配置文件中数据源不可删除
     * @return
     */
    @Override
    protected DataSource determineTargetDataSource() {
        Assert.notNull(DataSourceContextHolder.getDatasourceMap(), "DataSource router not initialized");
        Object lookupKey = this.determineCurrentLookupKey();
        DataSource dataSource = (DataSource)DataSourceContextHolder.getDatasourceMap().get(lookupKey);

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        } else {
            return dataSource;
        }
    }
}
