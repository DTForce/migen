package com.dtforce.migen.platform.postgres;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.PlatformImplBase;
import org.apache.ddlutils.platform.postgresql.PostgreSqlPlatform;

import com.dtforce.migen.ddl.CustomModelComparator;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.MigenSqlBuilder;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CustomPostgreSqlPlatform implements MigenPlatform
{

	@Getter(AccessLevel.PACKAGE)
	private PlatformImplBase wrappedPlatform;

	public CustomPostgreSqlPlatform(Map<String, String> platformTypeMapping)
	{
		wrappedPlatform = new DdlUtilsWrapper(platformTypeMapping, this);
	}

	@Override
	public MigenSqlBuilder getSqlBuilder() {
		return ((CustomPostgresqlBuilder) wrappedPlatform.getSqlBuilder()).clone();
	}

	@Override
	public Database readModelFromDatabase(Connection connection, String name)
	{
		return wrappedPlatform.readModelFromDatabase(connection, name);
	}

	@Override
	public Database readModelFromDatabase(Connection connection, String name, String schemaPattern)
	{
		return wrappedPlatform.readModelFromDatabase(connection, name, null, schemaPattern, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ModelChange> diff(Database currentModel, Database desiredModel, Predicate<ModelChange> predicate) {
		CustomModelComparator comparator = new CustomModelComparator(
			wrappedPlatform.getPlatformInfo(),
			false
		);
		List<ModelChange> changes = comparator.compare(currentModel, desiredModel);
		changes = changes.stream().filter(predicate).collect(Collectors.toList());
		return changes;
	}

	private static class DdlUtilsWrapper extends PostgreSqlPlatform
	{

		private DdlUtilsWrapper(Map<String, String> platformTypeMapping, CustomPostgreSqlPlatform platform)
		{
			super();
			setModelReader(new CustomPostgresqlModelReader(this, platformTypeMapping));
			getPlatformInfo().setMaxIdentifierLength(63);

			CustomPostgresqlBuilder builder = new CustomPostgresqlBuilder(platform);
			setSqlBuilder(builder);
			setDelimitedIdentifierModeOn(true);
		}
	}

}
