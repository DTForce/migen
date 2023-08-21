package com.dtforce.migen.platform;

import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Database;

import java.sql.Connection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Adapter interface to isolate ddl utils classes from the rest of code.
 */
public interface MigenPlatform {
	MigenSqlBuilder getSqlBuilder();
	Database readModelFromDatabase(Connection connection, String name);

	Database readModelFromDatabase(Connection connection, String name, String schemaPattern);

	List<ModelChange> diff(
		Database currentModel,
		Database desiredModel,
		Predicate<ModelChange> predicate
	);
}
