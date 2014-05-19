package com.demo.app.dao.db.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.TransactionStatus;

import com.demo.app.dao.db.AddressDAO;
import com.demo.app.domain.address.Address;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.util.ThreatConstants;
import com.icesoft.faces.component.ext.RowSelectorEvent;


public class AddressDAOImpl extends BaseSpringJdbcDAO implements AddressDAO {

	static private Logger log = Logger.getLogger(AddressDAOImpl.class.getName());
   
	//Delete the data in the address table
	static private String SQL_DELETE_ALL_ADDRESSS =
	"Delete FROM " + ThreatConstants.THREAT_SCHEMA_NAME +".ADDRESS";
	
	static private String SQL_ROW_SELECTED_ADDRESS =
		"SELECT ADDRESS_SEQ, ADDRESS, CITY, STATE, ZIP, EFFECTIVE_DT " +
		"  FROM " + ThreatConstants.THREAT_SCHEMA_NAME + ".ADDRESS " +
		" WHERE ROWNUM=?";

	
	static private String SQL_GET_ALL_ADDRESSS =
		"SELECT ADDRESS_SEQ, ADDRESS, CITY, STATE, ZIP, EFFECTIVE_DT FROM " + ThreatConstants.THREAT_SCHEMA_NAME +".ADDRESS";
	
	public void svAddress(Address address) {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		// check if address already exists.
		if (address == null) {
			throwJDBCException("Address is null.");
		}
		 template.update(SQL_INSERT_ADDRESS_EXT, new Object[] {
				address.getAddress(),
				address.getCity(),
				address.getState(),
				address.getZip(),
				new Date()});
	}
	
	
	private void throwJDBCException(String exceptionMsg) {
		throw new JDBCException(getClass() + exceptionMsg);
	}

	static private String SQL_INSERT_ADDRESS_EXT =
		" INSERT INTO " + ThreatConstants.THREAT_SCHEMA_NAME + ".ADDRESS " +
		" 		 (ADDRESS, CITY, STATE, ZIP, EFFECTIVE_DT, ADDRESS_SEQ)" +
		" VALUES (?,?,?,?,?, " + ThreatConstants.THREAT_SCHEMA_NAME + ".ADDRESS_SEQUENCE.nextVAL)";

	public List<Address> getAddresss() {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.query(SQL_GET_ALL_ADDRESSS,
				new AddressParameterizedRowMapper<Address>());
		
	}

	private class AddressParameterizedRowMapper<T> implements ParameterizedRowMapper<Address> {

		public Address mapRow(ResultSet resultSet, int row) throws SQLException {
			
			Address address = new Address();
			address.setAddressID(resultSet.getLong("ADDRESS_SEQ"));
			address.setAddress(resultSet.getString("ADDRESS"));
			address.setCity(resultSet.getString("CITY"));
			address.setState(resultSet.getString("STATE"));
			address.setZip(resultSet.getString("ZIP"));
			address.setEffectiveDate(resultSet.getDate("EFFECTIVE_DT"));
						
			return address;
		}
	}

	public void removeAddress() {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		
		
		try{
			   template.update(SQL_DELETE_ALL_ADDRESSS);
		}
		catch (Exception ex){
			
		}
	}

}
	