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
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

import com.dtforce.dokka.json.DokkaDocCodeInline;
import com.dtforce.dokka.json.DokkaDocLink;
import com.dtforce.dokka.json.DokkaDocNode;
import com.dtforce.dokka.json.DokkaJsonClasslike;
import com.dtforce.dokka.json.DokkaJsonModule;
import com.dtforce.dokka.json.DokkaJsonResolver;
import com.dtforce.migen.adapter.MetadataAdapter;
import com.dtforce.migen.adapter.hibernate.integration.HibernateInfoHolder;
import com.dtforce.migen.ddl.RawTypedColumn;
import com.dtforce.migen.platform.type.PlatformTypeMapping;

import java.sql.Types;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@Slf4j
public class HibernateAdapter implements MetadataAdapter
{
	private final HibernateInfoHolder hibernateInfoHolder;

	private final SchemaFilter schemaFilter;

	private final DokkaJsonModule dokkaModel;

	private final PlatformTypeMapping platformTypeMapping;

	public HibernateAdapter(
		final HibernateInfoHolder hibernateInfoHolder,
		final SchemaFilter schemaFilter,
		@Nullable final DokkaJsonModule dokkaModel,
		@Nullable final PlatformTypeMapping platformTypeMapping
	) {
		this.hibernateInfoHolder = hibernateInfoHolder;
		this.schemaFilter = schemaFilter;
		this.dokkaModel = dokkaModel;
		this.platformTypeMapping = platformTypeMapping;
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
		final Metadata metadata = metadata();
		final var entity = metadata.getEntityBindings()
				.stream()
				.filter(it -> it.getTable() == table)
				.findFirst();

		final var collectionOpt = metadata.getCollectionBindings()
				.stream()
				.filter(it -> it.getCollectionTable() == table)
				.findFirst();

		org.apache.ddlutils.model.Table tableResult = new org.apache.ddlutils.model.Table();
		tableResult.setName(table.getName());
		tableResult.setDescription(table.getComment());

		if (dokkaModel != null && table.getComment() == null) {
			if (entity.isPresent() && entity.get().getMappedClass() != null) {
				final DokkaJsonClasslike dokkaJsonClasslike = DokkaJsonResolver.INSTANCE
						.resolveClass(dokkaModel, entity.get().getMappedClass());
				if (dokkaJsonClasslike == null) {
					log.warn("Could not find Dokka for Entity {}", entity.get().getMappedClass().getName());
				} else if (dokkaJsonClasslike.getDocumentation() != null) {
					tableResult.setDescription(translateDocumentationToComment(dokkaJsonClasslike.getDocumentation()));
				}
			} else if (collectionOpt.isPresent()) {
				final Collection collection = collectionOpt.get();
				final var propName = deduceCollectionPropName(collection);
				if (propName.isPresent()) {
					var propertyDokka = DokkaJsonResolver.INSTANCE
							.resolveProperty(dokkaModel, collection.getOwner().getMappedClass(), propName.get());

					if (propertyDokka == null) {
						log.warn("Could not find Dokka for collection json for {}.{}", collection.getOwner().getMappedClass(), propName.get());
					} else if (propertyDokka.getDocumentation() != null) {
						tableResult.setDescription(translateDocumentationToComment(propertyDokka.getDocumentation()));
					}
				} else {
					log.warn("Could not determine collection property name for {}.", collection.getRole());
				}
			}
		}

		if (table.getPrimaryKey() == null) {
			log.warn("Table {} does not have a primary key.", table.getName());
		}

		for (Column column : table.getColumns()) {
			final Optional<Property> property = entity.flatMap(persistentClass -> persistentClass.getProperties()
                    .stream()
                    .filter(it -> it.getColumns().contains(column))
                    .findFirst());

			final org.apache.ddlutils.model.Column convertColumn = convertColumn(table, column, property);
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

	private String translateDocumentationToComment(final DokkaDocNode documentation)
	{
		return documentation.getParagraphs()
			.stream()
			.findFirst()
			.map(dokkaDocParagraph ->
				dokkaDocParagraph.getDocParts()
					.stream()
					.map(dokkaDocPart -> {
						if (dokkaDocPart instanceof DokkaDocLink link) {
							return translateDocumentationLink(link);
						} else if (dokkaDocPart instanceof DokkaDocCodeInline inline) {
							return putInBackTicks(inline.getText());
						} else {
							return dokkaDocPart.getText();
						}
					})
					.collect(Collectors.joining())
			)
			.orElse(null);
	}

	private String translateDocumentationLink(final DokkaDocLink link)
	{
		final var metadata = metadata();

		final var first = metadata.getEntityBindings()
			.stream()
			.filter(
				it -> it.getClassName() != null &&
					it.getClassName().equals(DokkaJsonResolver.INSTANCE.driToClassName(link.getDri()))
			)
			.findFirst();

		if (first.isPresent() && first.get().getTable() != null) {
			return putInBackTicks(first.get().getTable().getName());
		} else {
			return putInBackTicks(link.getText());
		}
	}

	private String putInBackTicks(final String text)
	{
		return "`" + text + "`";
	}

	private Optional<String> deduceCollectionPropName(Collection collection) {
		return collection.getOwner().getProperties()
				.stream().filter(it -> it.getValue() == collection)
				.map(Property::getName)
				.findFirst();
	}

	private void convertIndex(final org.apache.ddlutils.model.Table tableResult, final Index index)
	{
		final NonUniqueIndex nonUniqueIndex = new NonUniqueIndex();
		nonUniqueIndex.setName(index.getName());

		for (var entry : index.getSelectableOrderMap().entrySet()) {
			nonUniqueIndex.addColumn(new IndexColumn(tableResult.findColumn(entry.getKey().getText())));
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


	private org.apache.ddlutils.model.Column convertColumn(
		final Table table,
		final Column column,
		Optional<Property> property
	)
	{
		RawTypedColumn columnResult = new RawTypedColumn();
		columnResult.setName(column.getName());
		columnResult.setAutoIncrement(false);
		columnResult.setPrimaryKey(false);
		columnResult.setDescription(column.getComment());

		if (
			column.getComment() == null &&
			dokkaModel != null &&
			property.isPresent() &&
			property.get().getPersistentClass().getMappedClass() != null
		) {
			final Property prop = property.get();
			var propertyDokka = DokkaJsonResolver.INSTANCE
					.resolveProperty(dokkaModel, prop.getPersistentClass().getMappedClass(), prop.getName());

			if (propertyDokka == null) {
				log.warn("Could not find Dokka property json for {}.{}", prop.getPersistentClass().getMappedClass().getName(), prop.getName());
			} else if (propertyDokka.getDocumentation() != null) {
				columnResult.setDescription(translateDocumentationToComment(propertyDokka.getDocumentation()));
			}
		}

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
		final var rawCompleteType = column.getSqlType(metadata).toUpperCase();
		if (jdbcType == null) {
			columnResult.setTypeCode(Types.OTHER);
			columnResult.setRawCompleteType(rawCompleteType);
		} else {
			columnResult.setTypeCode(jdbcType.getJdbcTypeCode());
			columnResult.setRawCompleteType(rawCompleteType);
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
		if (platformTypeMapping != null) {
			return platformTypeMapping.map(rawCompleteType, columnResult);
		} else {
			return columnResult;
		}
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
