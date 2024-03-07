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

package com.dtforce.migen.adapter.hibernate;

import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.UniqueIndex;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

import com.dtforce.migen.adapter.MetadataAdapter;
import com.dtforce.migen.adapter.hibernate.integration.HibernateInfoHolder;
import com.dtforce.migen.ddl.RawTypedColumn;

import java.sql.Types;
import java.util.Iterator;

@Slf4j
public class HibernateAdapter implements MetadataAdapter
{
	private final HibernateInfoHolder hibernateInfoHolder;

	private final SchemaFilter schemaFilter;

	public HibernateAdapter(
		final HibernateInfoHolder hibernateInfoHolder,
		final SchemaFilter schemaFilter
	) {
		this.hibernateInfoHolder = hibernateInfoHolder;
		this.schemaFilter = schemaFilter;
	}

	@Override
	public Database getSchemaDatabase()
	{
		final Metadata metadata = metadata();

		final Database database = new Database();
		for ( Namespace namespace : metadata.getDatabase().getNamespaces() ) {
			if (schemaFilter.includeNamespace(namespace)) {
				createTables(namespace, database);
			}
		}
		return database;
	}

	private void createTables(final Namespace namespace, final Database database)
	{
		for (Table table : namespace.getTables()) {
			if ( schemaFilter.includeTable( table ) && table.isPhysicalTable() ) {
				database.addTable(convertTable(table));
			}
		}
	}

	private org.apache.ddlutils.model.Table convertTable(final Table table)
	{
		org.apache.ddlutils.model.Table tableResult = new org.apache.ddlutils.model.Table();
		tableResult.setName(table.getName());

		if (table.getPrimaryKey() == null) {
			log.warn("Table {} does not have a primary key.", table.getName());
		}

		for (Column column : table.getColumns()) {
			final org.apache.ddlutils.model.Column convertColumn = convertColumn(table, column);
			tableResult.addColumn(convertColumn);
			if (column.isUnique()) {
				UniqueIndex index = new UniqueIndex();
				index.addColumn(new IndexColumn(convertColumn));
				index.setName(table.getName() + "_" + column.getName() + "_unq");
				tableResult.addIndex(index);
			}
		}

		for (var e : table.getIndexes().entrySet()) {
			final Index index = e.getValue();
			convertIndex(tableResult, index);
		}

		for (var e : table.getForeignKeys().entrySet()) {
			final ForeignKey foreignKey = e.getValue();
			final org.apache.ddlutils.model.ForeignKey foreignKeyResult = convertForeignKey(foreignKey);
			tableResult.addForeignKey(foreignKeyResult);
			tableResult.addIndex(convertForeignKeyIndex(foreignKeyResult));
		}

		for (var e : table.getUniqueKeys().entrySet()) {
			final UniqueKey uniqueKey = e.getValue();
			final UniqueIndex uniqueIndex = new UniqueIndex();
			uniqueIndex.setName(uniqueKey.getName());
			for (Column column : uniqueKey.getColumns()) {
				uniqueIndex.addColumn(new IndexColumn(column.getName()));
			}
			tableResult.addIndex(uniqueIndex);
		}
		return tableResult;
	}

	private void convertIndex(final org.apache.ddlutils.model.Table tableResult, final Index index)
	{
		final NonUniqueIndex nonUniqueIndex = new NonUniqueIndex();
		nonUniqueIndex.setName(index.getName());

		for (var column : index.getColumns()) {
			nonUniqueIndex.addColumn(new IndexColumn(tableResult.findColumn(column.getName())));
		}
		tableResult.addIndex(nonUniqueIndex);
	}

	private org.apache.ddlutils.model.ForeignKey convertForeignKey(final ForeignKey foreignKey)
	{
		final org.apache.ddlutils.model.ForeignKey foreignKeyResult = new org.apache.ddlutils.model.ForeignKey(
			processForeignKeyName(foreignKey.getName())
		);
		foreignKeyResult.setForeignTableName(foreignKey.getReferencedTable().getName());

		Column[] referencedColumnNames = new Column[foreignKey.getColumnSpan()];

		final Iterator<Column> referencedColumnItr;
		if ( foreignKey.isReferenceToPrimaryKey() ) {
			referencedColumnItr = foreignKey.getReferencedTable().getPrimaryKey().getColumns().iterator();
		}
		else {
			referencedColumnItr = foreignKey.getReferencedColumns().iterator();
		}

		int i = 0;
		while ( referencedColumnItr.hasNext() ) {
			referencedColumnNames[i] = referencedColumnItr.next();
			i++;
		}

		for (i = 0; i < foreignKey.getColumnSpan(); i++) {
			Reference reference = new Reference();
			reference.setLocalColumnName(foreignKey.getColumns().get(i).getName());
			reference.setForeignColumnName(referencedColumnNames[i].getName());
			foreignKeyResult.addReference(reference);
		}
		return foreignKeyResult;
	}

	private String processForeignKeyName(final String name)
	{
		String result = name.toLowerCase();
		return CharMatcher.anyOf(getDialect().closeQuote() + "").trimTrailingFrom(
			CharMatcher.anyOf(getDialect().openQuote() + "").trimLeadingFrom(result)
		);
	}

	private org.apache.ddlutils.model.Index convertForeignKeyIndex(
		final org.apache.ddlutils.model.ForeignKey foreignKey
	) {
		final NonUniqueIndex nonUniqueIndex = new NonUniqueIndex();
		nonUniqueIndex.setName(foreignKey.getName() + "_idx");

		for (Reference reference : foreignKey.getReferences()) {
			nonUniqueIndex.addColumn(new IndexColumn(reference.getLocalColumnName()));
		}

		return nonUniqueIndex;
	}


	private org.apache.ddlutils.model.Column convertColumn(final Table table, final Column column)
	{
		RawTypedColumn columnResult = new RawTypedColumn();
		columnResult.setName(column.getName());
		columnResult.setAutoIncrement(false);
		columnResult.setPrimaryKey(false);
		columnResult.setDescription(column.getComment());
		if (table.getPrimaryKey() != null) {
			columnResult.setPrimaryKey(table.getPrimaryKey().containsColumn(column));
		}
		if (!columnResult.isPrimaryKey()) {
			columnResult.setRequired(!column.isNullable());
		} else {
			columnResult.setRequired(true);
		}

		final var jdbcTypeRegistry = getJdbcTypeRegistry();

		final var metadata = hibernateInfoHolder.getMetadata();


		final var jdbcType = jdbcTypeRegistry.findDescriptor(column.getSqlTypeCode(metadata));
		if (jdbcType == null) {
			columnResult.setTypeCode(Types.OTHER);
			columnResult.setRawCompleteType(column.getSqlType(metadata).toUpperCase());
		} else {
			columnResult.setTypeCode(jdbcType.getJdbcTypeCode());
			columnResult.setRawCompleteType(column.getSqlType(metadata).toUpperCase());
		}

		var size = column.getColumnSize(getDialect(), metadata);

		if (size.getPrecision() == null && size.getLength() != null) {
			columnResult.setSizeAndScale(size.getLength().intValue(), 0);
		} else if (size.getPrecision() != null) {
			if (size.getScale() == null) {
				columnResult.setSizeAndScale(size.getPrecision(), 0);
			} else {
				columnResult.setSizeAndScale(size.getPrecision(), size.getScale());
			}
		}
		return columnResult;
	}

	private JdbcTypeRegistry getJdbcTypeRegistry()
	{
		return hibernateInfoHolder.getBootstrapContext()
			.getTypeConfiguration()
			.getJdbcTypeRegistry();
	}

	private Dialect getDialect()
	{
		return hibernateInfoHolder.getSessionFactory()
			.getServiceRegistry()
			.getService(JdbcServices.class)
			.getDialect();
	}


	private Metadata metadata()
	{
		return hibernateInfoHolder.getMetadata();
	}

}
