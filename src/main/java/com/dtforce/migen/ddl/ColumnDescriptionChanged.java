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

package com.dtforce.migen.ddl;

import lombok.Getter;
import org.apache.ddlutils.alteration.ColumnChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

@Getter
public class ColumnDescriptionChanged implements ColumnChange
{

	private final String comment;

	private final Column column;

	private final Table table;

	/**
	 * Creates a new change object.
	 *
	 * @param table   The table of the column
	 * @param column  The column
	 * @param comment The JDBC type code of the new type
	 */
	public ColumnDescriptionChanged(Table table, RawTypedColumn column, String description)
	{
		this.table = table;
		this.column = column;
		this.comment = comment;
	}

	@Override
	public Column getChangedColumn()
	{
		return column;
	}

	@Override
	public Table getChangedTable()
	{
		return table;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public void apply(Database database, boolean caseSensitive)
	{
		database.findTable(table.getName())
				.findColumn(column.getName())
				.setDescription(comment);
	}

}
