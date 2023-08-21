package com.dtforce.migen.ddl;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.ColumnDataTypeChange;
import org.apache.ddlutils.alteration.ColumnSizeChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import java.util.List;
import java.util.Objects;

public class CustomModelComparator extends ModelComparator
{

	private boolean caseSensitive;

	/**
	 * Creates a new model comparator object.
	 *
	 * @param platformInfo  The platform info
	 * @param caseSensitive Whether comparison is case sensitive
	 */
	public CustomModelComparator(PlatformInfo platformInfo, boolean caseSensitive)
	{
		super(platformInfo, caseSensitive);
		this.caseSensitive = caseSensitive;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TableChange> compareColumns(
			Table sourceTable,
			Column sourceColumn,
			Table targetTable,
			Column targetColumn
	)
	{
		List<TableChange> changes = super.compareColumns(sourceTable, sourceColumn, targetTable, targetColumn);
		if (isRawTyped(sourceColumn, targetColumn) &&
			bothRawType((RawTypedColumn) sourceColumn, (RawTypedColumn) targetColumn)
		) {
			changes.removeIf(o -> o instanceof ColumnDataTypeChange);
			changes.removeIf(o -> o instanceof ColumnSizeChange);

			if (!isSameType((RawTypedColumn) sourceColumn, (RawTypedColumn) targetColumn)) {
				changes.add(new ColumnRawTypeChanged(
						targetTable,
						(RawTypedColumn) targetColumn,
						((RawTypedColumn) sourceColumn).getRawType()
				));
			}
		}

		return changes;
	}

	@Override
	public List compareTables(
		final Database sourceModel, final Table sourceTable, final Database targetModel, final Table targetTable
	)
	{
		final List<?> changes = super.compareTables(sourceModel, sourceTable, targetModel, targetTable);
		// remove primary key changes based on reordering columns
		changes.removeIf(o -> {
			if (o instanceof PrimaryKeyChange) {
				Column[] sourcePK = sourceTable.getPrimaryKeyColumns();
				Column[] targetPK = targetTable.getPrimaryKeyColumns();
				if (sourcePK.length == targetPK.length) {
					boolean pkChanged = false;
					for (Column sourceCol : sourcePK) {
						boolean isFound = false;
						for (Column targetCol : targetPK) {
							if(compareColumnName(sourceCol, targetCol)) {
								isFound = true;
								break;
							}
						}
						if (!isFound) {
							pkChanged = true;
							break;
						}
					}
					return !pkChanged;
				} else {
					return false;
				}
			} else {
				return false;
			}
		});
		return changes;
	}

	private boolean compareColumnName(final Column sourceCol, final Column targetCol)
	{
		return this.caseSensitive && !sourceCol.getName().equals(targetCol.getName()) || !this.caseSensitive && !sourceCol.getName().equalsIgnoreCase(targetCol.getName());
	}

	private boolean isRawTyped(Column sourceColumn, Column targetColumn)
	{
		return sourceColumn instanceof RawTypedColumn &&
				targetColumn instanceof RawTypedColumn;
	}

	private boolean bothRawType(RawTypedColumn sourceColumn, RawTypedColumn targetColumn)
	{
		return (sourceColumn.getTypeToCompare() != null && targetColumn.getTypeToCompare() != null);
	}

	private boolean isSameType(RawTypedColumn rawTypedColumn1, RawTypedColumn rawTypedColumn2)
	{
		return Objects.equals(rawTypedColumn1.getTypeToCompare(), rawTypedColumn2.getTypeToCompare()) &&
			(
				!rawTypedColumn1.hasSize() ||
				!rawTypedColumn2.hasSize() ||
				Objects.equals(rawTypedColumn1.getSize(), rawTypedColumn2.getSize())
			);
	}

}
