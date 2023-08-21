package com.dtforce.migen.platform;

import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;

import java.io.Writer;
import java.util.List;

/**
 * Adapter interface to isolate ddl utils classes from the rest of code.
 */
public interface MigenSqlBuilder extends Cloneable
{
	MigenSqlBuilder clone();
	boolean writeSqlColumnType(Column column);
	void writeSqlPatch(Database currentModel, Database desiredModel, List<ModelChange> changes);
	void setWriter(Writer ex);
}
