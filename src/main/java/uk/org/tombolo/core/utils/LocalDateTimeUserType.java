package uk.org.tombolo.core.utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.EnhancedUserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * FIXME: Delete me if we switch to Hibernate 5 (see note below)
 * 
 * This class comes from 
 * http://stackoverflow.com/questions/27750026/java-8-localdatetime-and-hibernate-4
 * 
 * If we can switch to Hibernate 5 in the future we should use the hibernate-java8 solution instead
 *
 */
public class LocalDateTimeUserType implements EnhancedUserType, Serializable {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP};

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class returnedClass() {
        return LocalDateTime.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        LocalDateTime dtx = (LocalDateTime) x;
        LocalDateTime dty = (LocalDateTime) y;
        return dtx.equals(dty);
    }

    @Override
    public int hashCode(Object object) throws HibernateException {
        return object.hashCode();
    }


    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        Object timestamp = StandardBasicTypes.TIMESTAMP.nullSafeGet(resultSet, names, session, owner);
        if (timestamp == null) {
            return null;
        }
        Date ts = (Date) timestamp;
        Instant instant = Instant.ofEpochMilli(ts.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            StandardBasicTypes.TIMESTAMP.nullSafeSet(preparedStatement, null, index, session);
        } else {
            LocalDateTime ldt = ((LocalDateTime) value);
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
            Date timestamp = Date.from(instant);
            StandardBasicTypes.TIMESTAMP.nullSafeSet(preparedStatement, timestamp, index, session);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object value) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public String objectToSQLString(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toXMLString(Object object) {
        return object.toString();
    }

    @Override
    public Object fromXMLString(String string) {
        return LocalDateTime.parse(string);
    }

}