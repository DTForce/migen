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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;
import org.apache.ddlutils.platform.postgresql.PostgreSqlModelReader;

import com.dtforce.migen.ddl.FilterIndexDef;
import com.dtforce.migen.ddl.RawTypedColumn;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class CustomPostgresqlModelReader extends PostgreSqlModelReader
{

	private static final String FILTER_CONDITION = "FILTER_CONDITION";

	private static final String TYPE_NAME = "TYPE_NAME";

	private final Map<String, String> typeTable;

	/**
	 * Creates a new model reader for Postgres databases.
	 *
	 * @param platform The platform that this model reader belongs to
	 */
	public CustomPostgresqlModelReader(Platform platform, Map<String, String> typeTable)
	{
		super(platform);
		this.typeTable = typeTable;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected List initColumnsForColumn()
	{
		List result = super.initColumnsForColumn();
		result.add(new MetaDataColumnDescriptor(TYPE_NAME, Types.VARCHAR));
		return result;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected List initColumnsForIndex()
	{
		List result = super.initColumnsForIndex();
		result.add(new MetaDataColumnDescriptor(FILTER_CONDITION, Types.VARCHAR));
		return result;
	}

	@Override
	protected Column readColumn(
		DatabaseMetaDataWrapper metaData,
		Map values
	) throws SQLException
	{
		RawTypedColumn rawTypedColumn = RawTypedColumn.fromColumn(super.readColumn(metaData, values));
		rawTypedColumn.setRawType(convertDBType(values));
		return rawTypedColumn;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void readIndex(
		final DatabaseMetaDataWrapper metaData,
		final Map values,
		final Map knownIndices
	) throws SQLException
	{
		super.readIndex(metaData, values, knownIndices);
		if (values.get(FILTER_CONDITION) != null) {
			String indexName = (String)values.get("INDEX_NAME");
			if (indexName != null) {
				Index index = (Index) knownIndices.get(indexName);
				knownIndices.put(indexName, FilterIndexDef.fromIndex(index, (String)values.get(FILTER_CONDITION)));
			}
		}
	}

	private String convertDBType(@SuppressWarnings("rawtypes") Map values)
	{
		String dbType = ((String) values.get(TYPE_NAME)).toUpperCase();
		if (typeTable.containsKey(dbType)) {
			return typeTable.get(dbType);
		}
		return dbType;
	}

}
