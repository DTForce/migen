package com.dtforce.migen.intergrations.spring;

import org.apache.ddlutils.Platform;
import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.dtforce.migen.MigrationGenerator;
import com.dtforce.migen.adapter.MetadataAdapter;
import com.dtforce.migen.adapter.hibernate.HibernateAdapter;
import com.dtforce.migen.adapter.hibernate.HibernateInfoHolder;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.PlatformFactory;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

@Configuration
@Profile("migrationDiff")
public class MigrationGeneratorConfiguration
{

	@Bean
	@ConditionalOnMissingBean
	public HibernateInfoHolder hibernateInfoHolder()
	{
		return HibernateInfoHolder.INSTANCE;
	}

	@Bean
	@ConditionalOnMissingBean
	public MetadataAdapter metadataAdapter(final HibernateInfoHolder hibernateInfoHolder)
	{
		// TODO support more adapters
		return new HibernateAdapter(hibernateInfoHolder, DefaultSchemaFilter.INSTANCE);
	}

	@Bean
	@ConditionalOnMissingBean
	MigrationGenerator migrationGenerator(
		final MetadataAdapter metadataAdapter,
		final DataSource dataSource,
		final MigenPlatform migenPlatform
	)
	{
		return new MigrationGenerator(metadataAdapter, dataSource, migenPlatform);
	}

	@Bean
	@ConditionalOnMissingBean
	MigenPlatform migenPlatform(MetadataAdapter metadataAdapter, DataSource dataSource) throws SQLException
	{
		Connection connection = DataSourceUtils.getConnection(dataSource);

		final String databaseProductName;

		try {
			databaseProductName = connection.getMetaData().getDatabaseProductName();
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}

		return PlatformFactory.INSTANCE.createPlatform(
			databaseProductName,
			metadataAdapter.getTypeMappping()
		);
	}

}
