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
	/**
	 * Clones the sql builder.
	 */
	MigenSqlBuilder clone();

	void writeSqlColumnType(Column column);

	void writeSqlPatch(Database currentModel, Database desiredModel, List<ModelChange> changes);

	void setWriter(Writer ex);
}
