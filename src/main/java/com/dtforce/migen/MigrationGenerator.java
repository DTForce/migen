package com.dtforce.migen;

import org.apache.ddlutils.alteration.ColumnOrderChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.alteration.RemoveTableChange;
import org.apache.ddlutils.model.Database;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.dtforce.migen.adapter.MetadataAdapter;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.MigenSqlBuilder;

import java.io.StringWriter;
import java.sql.Connection;
import java.util.List;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class MigrationGenerator
{

	private static final String SCHEMA_VERSION_TABLE = "flyway_schema_history";

	private static final Predicate<ModelChange> MODEL_CHANGE_PREDICATE = e -> {
		if (e instanceof RemoveTableChange) {
			return !((RemoveTableChange) e).getChangedTable()
				.getName()
				.equalsIgnoreCase(SCHEMA_VERSION_TABLE);
		} else {
			return !(e instanceof ColumnOrderChange);
		}
	};

	private final MetadataAdapter metadataAdapter;

	private final DataSource dataSource;

	private final MigenPlatform platform;

	private final String schemaPattern;

	public MigrationGenerator(
		final MetadataAdapter metadataAdapter,
		final DataSource dataSource,
		final MigenPlatform platform
	) {
		this(metadataAdapter, dataSource, platform, null);
	}

	/**
	 * @param schemaPattern Some DatabaseMetaData methods take arguments that are String patterns. These arguments all have names such as fooPattern.
	 * Within a pattern String, "%" means match any substring of 0 or more characters, and "_" means match any one character.
	 * Only metadata entries matching the search pattern are returned. If a search pattern argument is set to null,
	 * that argument's criterion will be dropped from the search. See {@link java.sql.DatabaseMetaData}.
	 */
	public MigrationGenerator(
		final MetadataAdapter metadataAdapter,
		final DataSource dataSource,
		final MigenPlatform platform,
		final String schemaPattern
	) {
		this.metadataAdapter = metadataAdapter;
		this.dataSource = dataSource;
		this.platform = platform;
		this.schemaPattern = schemaPattern;
	}

	/**
	 * Returns SQL required to run to migrate DB to match the entities.
	 */
	public String generateMigrationSQL()
	{
		return generateMigrationSQL(MODEL_CHANGE_PREDICATE);
	}

	public String generateMigrationSQL(Predicate<ModelChange> changeFilter)
	{
		final Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			final Database desiredModel = metadataAdapter.getSchemaDatabase();
			final Database currentModel = platform.readModelFromDatabase(connection, desiredModel.getName(), schemaPattern);

			final MigenSqlBuilder customPostgresqlBuilder = platform.getSqlBuilder();
			final StringWriter ex = new StringWriter();
			customPostgresqlBuilder.setWriter(ex);
			final var changes = platform.diff(currentModel, desiredModel, changeFilter);
			customPostgresqlBuilder.writeSqlPatch(currentModel, desiredModel, changes);
			return ex.toString();
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	public List<ModelChange> generateMigrationChanges()
	{
		return generateMigrationChanges(MODEL_CHANGE_PREDICATE);
	}

	public List<ModelChange> generateMigrationChanges(Predicate<ModelChange> changeFilter)
	{
		final Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			final Database desiredModel = metadataAdapter.getSchemaDatabase();
			final Database currentModel = platform.readModelFromDatabase(connection, desiredModel.getName(), schemaPattern);

			return platform.diff(currentModel, desiredModel, changeFilter);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

}
