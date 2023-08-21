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

package com.dtforce.migen.test;

import org.apache.ddlutils.alteration.AddTableChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestTools {

	public static Map<String, AddTableChange> filterAddedTablesNames(List<ModelChange> changes)
	{
		return changes.stream()
			.filter(it -> it instanceof AddTableChange)
			.map(it -> (AddTableChange) it)
			.collect(Collectors.toMap(it -> it.getNewTable().getName(), it -> it));
	}

	public static LinkedHashMap<String, Column> tableColumns(Table table)
	{
		LinkedHashMap<String, Column> columns = new LinkedHashMap<>(table.getColumnCount());
		for (Column column : table.getColumns()) {
			columns.put(column.getName(), column);
		}
		return columns;
	}

}
