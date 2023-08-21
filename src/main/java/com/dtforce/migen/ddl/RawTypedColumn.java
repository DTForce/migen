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
import lombok.Setter;
import org.apache.ddlutils.model.Column;

import com.dtforce.migen.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RawTypedColumn extends Column
{

	private static final List<Integer> typeCodesWithSize = Arrays.asList(3, 12, -3, 2, 1111);

	private String rawType;

	private String rawCompleteType;

	/**
	 * Create from original column.
	 */
	public static RawTypedColumn fromColumn(Column column)
	{
		RawTypedColumn result = new RawTypedColumn();

		result.setName(column.getName());
		result.setJavaName(column.getJavaName());
		result.setPrimaryKey(column.isPrimaryKey());
		result.setRequired(column.isRequired());
		result.setAutoIncrement(column.isAutoIncrement());
		result.setTypeCode(column.getTypeCode());
		result.setType(column.getType());
		result.setSize(column.getSize());
		result.setDefaultValue(column.getDefaultValue());
		result.setSizeAndScale(column.getSizeAsInt(), column.getScale());
		result.setScale(column.getScale());
		result.setSize(column.getSize());

		return result;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final RawTypedColumn that = (RawTypedColumn) o;
		return Objects.equals(getRawType(), that.getRawType()) || Objects.equals(getRawCompleteType(),
			that.getRawCompleteType()
		);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), getRawType(), getRawCompleteType());
	}

	public String getTypeToCompare()
	{
		if (rawCompleteType == null) {
			return rawType;
		}
		return StringUtils.cutToParent(rawCompleteType);
	}

	boolean hasSize()
	{
		return typeCodesWithSize.contains(getTypeCode());
	}

}
