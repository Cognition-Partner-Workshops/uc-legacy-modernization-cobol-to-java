package com.carddemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

/**
 * Reactive Cassandra configuration.
 *
 * Replaces legacy VSAM KSDS data storage. Cassandra provides:
 *   - High-throughput write performance (replacing VSAM sequential writes)
 *   - Distributed storage (replacing single-node DASD)
 *   - Flexible schema (replacing fixed-length COBOL records)
 *   - Time-series optimization (ideal for transaction data)
 *
 * VSAM to Cassandra mapping:
 *   VSAM KSDS (Key Sequenced)  -> Cassandra table with partition key
 *   VSAM AIX (Alternate Index) -> Cassandra secondary index or materialized view
 *   VSAM STARTBR/READNEXT      -> Cassandra range query / token-based pagination
 */
@Configuration
@EnableReactiveCassandraRepositories(basePackages = "com.carddemo.repository.cassandra")
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name:carddemo}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points:localhost}")
    private String contactPoints;

    @Value("${spring.cassandra.port:9042}")
    private int port;

    @Value("${spring.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.carddemo.model"};
    }
}
