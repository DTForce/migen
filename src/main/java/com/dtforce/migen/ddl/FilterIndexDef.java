package com.dtforce.migen.ddl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.IndexImpBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FilterIndexDef extends IndexImpBase
{

	private boolean isUnique = false;

	private String filterCondition;

	public FilterIndexDef(final String name, final boolean isUnique, final String filterCondition)
	{
		this._name = name;
		this._columns = new ArrayList<>(5);
		this.isUnique = isUnique;
		this.filterCondition = filterCondition;
	}

	@SuppressWarnings("unchecked")
	private FilterIndexDef(
		final String name,
		final Collection<IndexColumn> columns,
		final boolean isUnique,
		final String filterCondition
	)
	{
		this._name = name;
		this._columns = new ArrayList<>(columns);
		this.isUnique = isUnique;
		this.filterCondition = filterCondition;
	}

	@Override
	public boolean isUnique()
	{
		return isUnique;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException
	{
		FilterIndexDef result = new FilterIndexDef(_name, (Collection<IndexColumn>)_columns, isUnique, filterCondition);

		result._name    = _name;
		result._columns = (ArrayList<String>)_columns.clone();

		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FilterIndexDef) {
			FilterIndexDef other = (FilterIndexDef)obj;

			return new EqualsBuilder()
				.append(_name,    other._name)
				.append(_columns, other._columns)
				.append(filterCondition, other.filterCondition)
				.append(isUnique, other.isUnique)
				.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return (new HashCodeBuilder(23, 31))
			.append(_name)
			.append(_columns)
			.append(filterCondition)
			.append(isUnique)
			.toHashCode();
	}

	@Override
	public boolean equalsIgnoreCase(final Index other)
	{
		if (other instanceof FilterIndexDef)
		{
			FilterIndexDef otherIndex = (FilterIndexDef)other;

			if (this.isUnique != ((FilterIndexDef) other).isUnique) {
				return false;
			}

			boolean checkName = (_name != null) && (_name.length() > 0) &&
								(otherIndex._name != null) && (otherIndex._name.length() > 0);

			if (filterCondition != null && !filterCondition.equalsIgnoreCase(otherIndex.filterCondition)) {
				return false;
			}

			if (filterCondition == null && otherIndex.filterCondition != null) {
				return false;
			}

			if ((!checkName || _name.equalsIgnoreCase(otherIndex._name)) &&
				(getColumnCount() == otherIndex.getColumnCount()))
			{
				for (int idx = 0; idx < getColumnCount(); idx++)
				{
					if (!getColumn(idx).equalsIgnoreCase(otherIndex.getColumn(idx)))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer result = new StringBuffer();

		result.append("Native index [name=");
		result.append(getName());
		result.append("; ");
		result.append(getColumnCount());
		result.append(" columns]");

		return result.toString();
	}

	@Override
	public String toVerboseString()
	{
		StringBuffer result = new StringBuffer();

		result.append("Native index [");
		result.append(getName());
		result.append("] columns:");
		for (int idx = 0; idx < getColumnCount(); idx++)
		{
			result.append(" ");
			result.append(getColumn(idx).toString());
		}

		return result.toString();
	}

	public String getFilterCondition()
	{
		return filterCondition;
	}

	public static FilterIndexDef fromIndex(final Index index, final String filterCondition)
	{
		return new FilterIndexDef(
			index.getName(),
			Arrays.asList(index.getColumns()),
			index.isUnique(),
			filterCondition
		);
	}

}
