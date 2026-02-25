package com.kaerna.lab01.config.lab05;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.get();
        if (type == DataSourceType.REPLICA) {
            return DataSourceType.REPLICA;
        }
        return DataSourceType.PRIMARY;
    }
}
