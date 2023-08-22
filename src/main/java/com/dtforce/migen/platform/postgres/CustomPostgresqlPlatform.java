/*
 *    Copyright 2023 Jan Mareš, DTForce s.r.o.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dtforce.migen.platform.postgres;

import com.google.common.base.MoreObjects;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.PlatformImplBase;
import org.apache.ddlutils.platform.postgresql.PostgreSqlPlatform;

import com.dtforce.migen.ddl.CustomModelComparator;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.MigenSqlBuilder;
import com.dtforce.migen.platform.PlatformTypeMapping;

import java.sql.Connection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CustomPostgresqlPlatform implements MigenPlatform
{

	@Getter(AccessLevel.PACKAGE)
	private final PlatformImplBase wrappedPlatform;

	public static final PlatformTypeMapping DEFAULT_TYPE_MAPPING;

	static {
		DEFAULT_TYPE_MAPPING = new PlatformTypeMapping()
			.withGeneralProcessor((dbType, rawTypedColumn) -> {
				if (dbType.startsWith("_")) {
					rawTypedColumn.setRawCompleteType(dbType.substring(1) + "[]");
				}
				return rawTypedColumn;
			})
			.withMapping("BOOL", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType("BOOLEAN");
				return rawTypedColumn;
			})
			.withMapping("VARCHAR", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType(
					String.format("VARCHAR(%d)", rawTypedColumn.getSizeAsInt())
				);
				return rawTypedColumn;
			})
			.withMapping("NUMERIC", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType(
					String.format(
						"NUMERIC(%d,%d)",
						rawTypedColumn.getPrecisionRadix(),
						rawTypedColumn.getScale()
					)
				);
				return rawTypedColumn;
			})
			.withMapping("TIMESTAMP", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType(
					String.format(
						"TIMESTAMP(%d)",
						rawTypedColumn.getScale()
					)
				);
				return rawTypedColumn;
			})
			.withMapping("TIMESTAMPTZ", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType(
					String.format(
						"TIMESTAMP(%d) WITH TIME ZONE",
						rawTypedColumn.getScale()
					)
				);
				return rawTypedColumn;
			})
			.withMapping("INT8", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType("BIGINT");
				return rawTypedColumn;
			})
			.withMapping("INT4", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType("INTEGER");
				return rawTypedColumn;
			})
			.withMapping("INT2", rawTypedColumn -> {
				rawTypedColumn.setRawCompleteType("SMALLINT");
				return rawTypedColumn;
			});
	}


	public CustomPostgresqlPlatform(PlatformTypeMapping platformTypeMapping)
	{
		wrappedPlatform = new DdlUtilsWrapper(
			MoreObjects.firstNonNull(platformTypeMapping, DEFAULT_TYPE_MAPPING),
			this
		);
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

		private DdlUtilsWrapper(PlatformTypeMapping platformTypeMapping, CustomPostgresqlPlatform platform)
		{
			super();
			setModelReader(new CustomPostgresqlModelReader(this, platformTypeMapping));
			getPlatformInfo().setMaxIdentifierLength(63);
			getPlatformInfo().addNativeTypeMapping(2003, "BYTEA", 2003);

			CustomPostgresqlBuilder builder = new CustomPostgresqlBuilder(platform);
			setSqlBuilder(builder);
			setDelimitedIdentifierModeOn(true);
		}
	}

}
