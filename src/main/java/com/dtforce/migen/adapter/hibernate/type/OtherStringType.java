package com.dtforce.migen.adapter.hibernate.type;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Representation of object, that is load from JDBC as String, but is not one of the normal string
 * types - e.g. LTREE. This type gives a mark of {@link JDBCType#OTHER} so the migration generator
 * does not think, there is a difference in column type, when the type of columns is read from the DB
 * metadata also as {@link JDBCType#OTHER}.
 */
public class OtherStringType implements UserType<String> {

	@Override
	public int getSqlType() {
		return Types.OTHER;
	}

	@Override
	public Class<String> returnedClass() {
		return String.class;
	}

	@Override
	public boolean equals(String x, String y) throws HibernateException {
		return x.equals(y);
	}

	@Override
	public int hashCode(String x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public String nullSafeGet(
		ResultSet rs,
		int position,
		SharedSessionContractImplementor session,
		Object owner
	) throws SQLException {
		return rs.getString(position);
	}

	@Override
	public void nullSafeSet(
		final PreparedStatement st,
		final String value,
		final int index,
		final SharedSessionContractImplementor session
	) throws HibernateException, SQLException {
		st.setObject(index, value, Types.OTHER);
	}

	@Override
	public String deepCopy(String value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(String value) throws HibernateException {
		return value;
	}

	@Override
	public String assemble(Serializable cached, Object owner)
		throws HibernateException {
		return (String) cached;
	}

	@Override
	public String replace(String original, String target, Object owner)
		throws HibernateException {
		return deepCopy(original);
	}

}
