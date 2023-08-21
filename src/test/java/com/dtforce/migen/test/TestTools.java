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
