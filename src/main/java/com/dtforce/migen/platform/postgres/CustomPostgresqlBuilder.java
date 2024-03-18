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

import lombok.SneakyThrows;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.ColumnChange;
import org.apache.ddlutils.alteration.ColumnDataTypeChange;
import org.apache.ddlutils.alteration.ColumnDefaultValueChange;
import org.apache.ddlutils.alteration.ColumnOrderChange;
import org.apache.ddlutils.alteration.ColumnRequiredChange;
import org.apache.ddlutils.alteration.ColumnSizeChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.ModelException;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.postgresql.PostgreSqlBuilder;

import com.dtforce.migen.ddl.ColumnDescriptionChanged;
import com.dtforce.migen.ddl.FilterIndexDef;
import com.dtforce.migen.ddl.RawTypedColumn;
import com.dtforce.migen.ddl.TableDescriptionChanged;
import com.dtforce.migen.platform.MigenSqlBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomPostgresqlBuilder extends PostgreSqlBuilder implements MigenSqlBuilder
{

	private final CustomPostgresqlPlatform platform;

	public CustomPostgresqlBuilder(CustomPostgresqlPlatform platform)
	{
		super(platform.getWrappedPlatform());
		this.platform = platform;
	}

	/**
	 * Alter database to desired model.
	 */
	@SneakyThrows
	public void writeSqlPatch(
		Database currentModel,
		Database desiredModel,
		List<ModelChange> changes
	)
	{
		processChanges(currentModel, desiredModel, changes, null);
	}

	@Override
	protected void processChanges(Database currentModel, Database desiredModel, List changes, CreationParameters params) throws IOException {
		super.processChanges(currentModel, desiredModel, changes, params);
		for (Object change : changes) {
			if (change instanceof ColumnDescriptionChanged) {
				processColumnCommentChange(currentModel, (ColumnDescriptionChanged) change);
			}
			if (change instanceof TableDescriptionChanged) {
				processTableCommentChange(currentModel, (TableDescriptionChanged) change);
			}
		}
	}

	@Override
	protected void processTableStructureChanges(
			Database currentModel,
			Database desiredModel,
			CreationParameters params,
			Collection changes
	) throws IOException
	{
		ListOrderedMap changesPerTable = new ListOrderedMap();
		boolean caseSensitive = getPlatform().isDelimitedIdentifierModeOn();

		// we first sort the changes for the tables
		// however since the changes might contain source or target tables
		// we use the names rather than the table objects
		for (Object change1 : changes) {
			TableChange change = (TableChange) change1;
			String name = change.getChangedTable().getName();

			if (!caseSensitive) {
				name = name.toUpperCase();
			}

			@SuppressWarnings("unchecked")
			List<TableChange> changesForTable = (ArrayList<TableChange>) changesPerTable.get(name);

			if (changesForTable == null) {
				changesForTable = new ArrayList<>();
				changesPerTable.put(name, changesForTable);
			}
			changesForTable.add(change);
		}

		// We're using a copy of the current model so that the table structure changes can
		// modify it
		final Database copyOfCurrentModel;

		try {
			copyOfCurrentModel = (Database) currentModel.clone();
		} catch (CloneNotSupportedException ex) {
			throw new DdlUtilsException(ex);
		}

		for (Object o : changesPerTable.entrySet()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, List<TableChange>> entry = (Map.Entry<String, List<TableChange>>) o;
			Table targetTable = desiredModel.findTable(entry.getKey(), caseSensitive);

			processTableStructureChanges(
					copyOfCurrentModel,
					desiredModel,
					entry.getKey(),
					params == null ? null : params.getParametersFor(targetTable),
					entry.getValue()
			);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void writeExternalPrimaryKeysCreateStmt(Table table, Column[] primaryKeyColumns) throws IOException
	{
		// TODO store name of the primary key in ddlutils
		if ((primaryKeyColumns.length > 0) && shouldGeneratePrimaryKeys(primaryKeyColumns))
		{
			print("ALTER TABLE ");
			printlnIdentifier(getTableName(table));
			printIndent();
			print("ADD CONSTRAINT ");
			printIdentifier(getConstraintName(null, table, "pkey", null));
			print(" ");
			writePrimaryKeyStmt(table, primaryKeyColumns);
			printEndOfStatement();
		}
	}

	public void writeExternalIndexDropStmt(Table table, Index index) throws IOException {
		if (index instanceof UniqueIndex uniqueIndex) {
			this.writeTableAlterStmt(table);
			this.print("DROP CONSTRAINT ");
			this.printIdentifier(this.getIndexName(uniqueIndex));
			this.printEndOfStatement();
		} else {
			super.writeExternalIndexDropStmt(table, index);
		}
	}

	@Override
	protected void processTableStructureChanges(
			Database currentModel,
			Database desiredModel,
			Table sourceTable,
			Table targetTable,
			Map parameters,
			List changes
	) throws IOException
	{
		@SuppressWarnings("unchecked")
		Iterator<TableChange> changeIt = (Iterator<TableChange>) changes.iterator();

		while (changeIt.hasNext()) {
			TableChange change = changeIt.next();
			boolean handled = true;
			if (change instanceof ColumnDataTypeChange) {
				processTypeChange(currentModel, desiredModel, (ColumnChange) change);
			} else if (change instanceof ColumnOrderChange) {
				// ignored
			} else if (change instanceof ColumnSizeChange) {
				processTypeChange(currentModel, desiredModel, (ColumnSizeChange) change);
			} else if (change instanceof ColumnRequiredChange) {
				processChange(currentModel, (ColumnRequiredChange) change);
			} else if (change instanceof ColumnDescriptionChanged) {
				processColumnCommentChange(currentModel, (ColumnDescriptionChanged) change);
			} else if (change instanceof final AddColumnChange addColumnChange) {
				if (addColumnChange.getNewColumn().isRequired() &&
					!addColumnChange.getNewColumn().isAutoIncrement() &&
					(addColumnChange.getNewColumn().getDefaultValue() == null)) {
					try {
						Column column = (Column) addColumnChange.getNewColumn().clone();
						column.setDefaultValue("-1");
						AddColumnChange addColumnChange1 = new AddColumnChange(
								addColumnChange.getChangedTable(),
								column,
								addColumnChange.getPreviousColumn(),
								addColumnChange.getNextColumn()
						);
						this.printComment("TODO - change default value");
						this.processChange(currentModel, desiredModel, addColumnChange1);
						this.processDefaultChange(
								currentModel,
								desiredModel,
								new ColumnDefaultValueChange(addColumnChange.getChangedTable(), column, null)
						);
					} catch (CloneNotSupportedException e) {
						throw new RuntimeException(e);
					}
				} else {
					this.processChange(currentModel, desiredModel, addColumnChange);
				}
			} else if (change instanceof ColumnDefaultValueChange) {
				this.processDefaultChange(currentModel, desiredModel, (ColumnDefaultValueChange) change);
			} else {
				handled = false;
			}
			if (handled) {
				changeIt.remove();
			}
		}

		super.processTableStructureChanges(currentModel, desiredModel, sourceTable, targetTable, parameters, changes);
	}

	@Override
	protected void processTableStructureChanges(
			Database currentModel,
			Database desiredModel,
			String tableName,
			Map parameters,
			List changes
	) throws IOException
	{
		Table sourceTable = currentModel.findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());
		Table targetTable = desiredModel.findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());

		processTableStructureChanges(currentModel, desiredModel, sourceTable, targetTable, parameters, changes);

		if (!changes.isEmpty()) {
			// we can only copy the data if no required columns without default value and
			// non-autoincrement have been added
			boolean canMigrateData = true;

			@SuppressWarnings("unchecked")
			Iterator<TableChange> it = (Iterator<TableChange>)changes.iterator();
			while (canMigrateData && it.hasNext()) {
				TableChange change = it.next();

				if (change instanceof final AddColumnChange addColumnChange) {

					if (addColumnChange.getNewColumn().isRequired() &&
						!addColumnChange.getNewColumn().isAutoIncrement() &&
						(addColumnChange.getNewColumn().getDefaultValue() == null)) {
						_log.warn("Data cannot be retained in table " +
							change.getChangedTable().getName() +
							" because of the addition of the required column " +
							addColumnChange.getNewColumn().getName()
						);
						canMigrateData = false;
					}
				}
			}

			Table realTargetTable = getRealTargetTableFor(desiredModel, sourceTable, targetTable);

			if (canMigrateData) {
				Table tempTable = getTemporaryTableFor(desiredModel, targetTable);

				createTemporaryTable(desiredModel, tempTable, parameters);
				writeCopyDataStatement(sourceTable, tempTable);
				// Note that we don't drop the indices here because the DROP TABLE will take care of that
				// Likewise, foreign keys have already been dropped as necessary
				dropTable(sourceTable);
				createTable(desiredModel, realTargetTable, parameters);
				writeCopyDataStatement(tempTable, targetTable);
				dropTemporaryTable(desiredModel, tempTable);
			} else {
				dropTable(sourceTable);
				createTable(desiredModel, realTargetTable, parameters);
			}
		}
	}

	@Override
	public void createTable(final Database database, final Table table, final Map parameters) throws IOException
	{
		super.createTable(database, table, parameters);

		// add comments
		if (table.getDescription() != null) {
			processTableCommentChange(database, new TableDescriptionChanged(table, table.getDescription()));
		}

		for (Column column : table.getColumns()) {
			if (column.getDescription() != null) {
				processColumnCommentChange(
					database,
					new ColumnDescriptionChanged(table, column, column.getDescription())
				);
			}
		}
	}

	@Override
	public String getIndexName(Index index)
	{
		return shortenName(index.getName(), getMaxConstraintNameLength());
	}

	@Override
	protected void writeExternalIndexCreateStmt(Table table, Index index) throws IOException
	{
		if (!getPlatformInfo().isIndicesSupported()) {
			return;
		}
		if (index.getName() == null) {
			_log.warn("Cannot write unnamed index " + index);
			return;
		}
		print("CREATE");
		if (index.isUnique()) {
			print(" UNIQUE");
		}
		print(" INDEX ");
		printIdentifier(getIndexName(index));
		print(" ON ");
		printIdentifier(getTableName(table));
		print(" (");

		for (int idx = 0; idx < index.getColumnCount(); idx++)
		{
			IndexColumn idxColumn = index.getColumn(idx);
			Column      col       = table.findColumn(idxColumn.getName());

			if (col == null)
			{
				// would get null pointer on next line anyway, so throw exception
				throw new ModelException(
					"Invalid column '" + idxColumn.getName() +
					"' on index " + index.getName() +
					" for table " + table.getName()
				);
			}
			if (idx > 0)
			{
				print(", ");
			}
			printIdentifier(getColumnName(col));
		}

		print(")");
		if (index instanceof FilterIndexDef) {
			print(" WHERE ");
			print(((FilterIndexDef) index).getFilterCondition());
		}
		printEndOfStatement();
	}

	@Override
	protected String getNativeType(Column column)
	{
		if (column instanceof RawTypedColumn) {
			return ((RawTypedColumn) column).getTypeToCompare();
		} else {
			return super.getNativeType(column);
		}
	}

	@Override
	protected String getSqlType(Column column) {
		if (column instanceof RawTypedColumn && ((RawTypedColumn) column).getRawCompleteType() != null) {
			return ((RawTypedColumn) column).getRawCompleteType();
		} else {
			return super.getSqlType(column);
		}
	}

	private void processColumnCommentChange(Database currentModel, ColumnDescriptionChanged change)
		throws IOException
	{
		processGenericCommentChange(
			"COLUMN",
			this.getTableName(change.getChangedTable()) + "." + this.getColumnName(change.getChangedColumn()),
			change.getDescription()
		);
		change.apply(currentModel, isCaseSensitive());
	}

	private void processTableCommentChange(Database currentModel, TableDescriptionChanged change)
		throws IOException
	{
		processGenericCommentChange("TABLE", this.getTableName(change.getChangedTable()), change.getDescription());
		change.apply(currentModel, isCaseSensitive());
	}

	private void processGenericCommentChange(String type, String ident, String description)
		throws IOException
	{
		this.print("COMMENT ON " + type.toUpperCase() + " ");
		this.printlnIdentifier(ident);
		this.printIndent();
		this.print("IS ");
		if (description != null) {
			this.print("'");
			this.print(description);
			this.print("'");
		} else {
			this.print("NULL");
		}
		this.printEndOfStatement();
	}

	private void processChange(Database currentModel, ColumnRequiredChange change) throws IOException
	{
		this.print("ALTER TABLE ");
		this.printlnIdentifier(this.getTableName(change.getChangedTable()));
		this.printIndent();
		this.print("ALTER COLUMN ");
		this.printIdentifier(this.getColumnName(change.getChangedColumn()));
		if (!change.getChangedColumn().isRequired()) {
			// we are swapping on the basis of a wrong state (is required, but should not be)
			// -> that is why the logic is inverted
			this.print(" SET ");
		} else {
			this.print(" DROP ");
		}
		this.print("NOT NULL ");
		this.printEndOfStatement();
		change.apply(currentModel, isCaseSensitive());
	}

	private void processTypeChange(Database currentModel, Database desiredModel, ColumnChange change) throws IOException
	{
		Column desiredColumn = findDesiredColumn(desiredModel, change);
		this.print("ALTER TABLE ");
		this.printlnIdentifier(this.getTableName(change.getChangedTable()));
		this.printIndent();
		this.print("ALTER COLUMN ");
		this.printIdentifier(this.getColumnName(change.getChangedColumn()));
		this.print(" TYPE ");
		this.print(this.getSqlType(desiredColumn));
		this.printEndOfStatement();
		change.apply(currentModel, isCaseSensitive());
	}

	private void processDefaultChange(
			Database currentModel,
			Database desiredModel,
			ColumnDefaultValueChange change
	) throws IOException
	{
		Column desiredColumn = findDesiredColumn(desiredModel, change);
		this.print("ALTER TABLE ");
		this.printlnIdentifier(this.getTableName(change.getChangedTable()));
		this.printIndent();
		this.print("ALTER COLUMN ");
		this.printIdentifier(this.getColumnName(change.getChangedColumn()));
		boolean hadValue = change.getChangedColumn().getParsedDefaultValue() != null;
		boolean willHaveValue = change.getNewDefaultValue() != null;
		if (willHaveValue) {
			this.print(" SET DEFAULT ");
			this.print(this.getNativeDefaultValue(desiredColumn));
		} else if (hadValue) {
			this.print(" DROP DEFAULT");
		}
		this.printEndOfStatement();
		change.apply(currentModel, isCaseSensitive());
	}

	private Column findDesiredColumn(Database desiredModel, ColumnChange currentColumn)
	{
		Table desiredTable = desiredModel.findTable(currentColumn.getChangedTable().getName(), isCaseSensitive());
		return desiredTable.findColumn(currentColumn.getChangedColumn().getName());
	}

	private boolean isCaseSensitive()
	{
		return this.getPlatform().isDelimitedIdentifierModeOn();
	}

	@Override
	@SneakyThrows
	public MigenSqlBuilder clone() {
		return new CustomPostgresqlBuilder(platform);
	}

	@Override
	@SneakyThrows
	public void writeSqlColumnType(Column column) {
		this.print(this.getSqlType(column));
	}

}
