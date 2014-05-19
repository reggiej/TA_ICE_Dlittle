package com.demo.app.dao.db.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.TransactionStatus;

import com.demo.app.dao.db.SiteAdminDAO;
import com.demo.app.domain.UserSearchCriteria;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.util.ThreatConstants;
import com.demo.app.domain.security.Orgs;

/**
 * @author Kunta L.
 *
 */
public class SiteAdminDAOImpl extends BaseSpringJdbcDAO implements SiteAdminDAO {

	static private Logger log = Logger.getLogger(SiteAdminDAOImpl.class.getName());

	static private String SQL_UNIQUE_USERNAME =
		"SELECT COUNT(*) as uniqueUsername " +
		"  FROM " + ThreatConstants.THREAT_SCHEMA_NAME + ".USERS " +
		" WHERE USERNAME=?";

	static private String SQL_UPDATE_USER =
		"UPDATE " + 
		ThreatConstants.THREAT_SCHEMA_NAME + ".USERS " +
		"SET FIRSTNAME=?, LASTNAME=?, USER_MI_NAME=?, USERNAME=?, USER_TITLE=?, USER_SECURE_PH=?, USER_COMM_PH=?, USER_CELL_PH=?, PASSWORD=?, ENABLED=?, " +
			"EFFECTIVE_DT=?, PASSWORD_CH_EFFECTIVE_TS=?, CHANGE_PASSWORD=?, ORG_SEQ=? " +
		"WHERE USER_SEQ=?";

	static private String SQL_DELETE_AUTHORITIES_BY_USER_SEQ =
		"DELETE " +
		"FROM " + 
		ThreatConstants.THREAT_SCHEMA_NAME + ".AUTHORITIES " +
		"WHERE USER_SEQ=?";

	static private String SQL_ASSIGN_USER_ROLES =
		  "INSERT INTO " + 
		  ThreatConstants.THREAT_SCHEMA_NAME + ".AUTHORITIES (USER_SEQ, ROLE_SEQ, EFFECTIVE_DT, AUTH_SEQ) " +
		  "SELECT USERS.USER_SEQ, ROLES.ROLE_SEQ, USERS.EFFECTIVE_DT, AUTHORITIES_SEQUENCE.nextVAL " +  
		  "FROM " + 
		  ThreatConstants.THREAT_SCHEMA_NAME + ".USERS, " +
		  ThreatConstants.THREAT_SCHEMA_NAME + ".ROLES " +		  
		  "WHERE " +
		  "USERS.USERNAME=? " +
		  "AND USERS.EFFECTIVE_DT=? " +
		  "AND ROLES.ROLENAME=?	";

	static private String SQL_GET_ALL_USERS_ASSIGNED_ROLES_BY_USER_SEQ =
		"SELECT ROLES.ROLENAME, ROLES.ROLE_DESCRIPTION, ROLES.ROLE_SEQ, AUTHORITIES.EFFECTIVE_DT " +
		"FROM " + 
		ThreatConstants.THREAT_SCHEMA_NAME + ".USERS, " +
		ThreatConstants.THREAT_SCHEMA_NAME + ".ROLES, " +
		ThreatConstants.THREAT_SCHEMA_NAME + ".AUTHORITIES " +
		"WHERE AUTHORITIES.user_seq=? " +
		" AND USERS.user_seq=? " +
		" AND AUTHORITIES.role_seq = ROLES.ROLE_SEQ " +
		" AND AUTHORITIES.role_seq != 10";

	static private String SQL_GET_ALL_ROLES =
		"SELECT ROLENAME, ROLE_DESCRIPTION, ROLE_SEQ, EFFECTIVE_DT " +
		"  FROM " + ThreatConstants.THREAT_SCHEMA_NAME + ".ROLES";

	static private String SQL_GET_ALL_ORGS =
		"SELECT ORGNAME, ORG_DESCRIPTION, ORG_SEQ, EFFECTIVE_DT " +
		" FROM " + ThreatConstants.THREAT_SCHEMA_NAME + ".ORGS";
	
	static private String SQL_GET_ALL_USERS =
		"SELECT USER_SEQ, FIRSTNAME, LASTNAME, USER_MI_NAME, USERNAME, USER_TITLE, USER_SECURE_PH, USER_COMM_PH, USER_CELL_PH, EFFECTIVE_DT, PASSWORD, ENABLED, CHANGE_PASSWORD, PASSWORD_CH_EFFECTIVE_TS, ORG_SEQ " +
		" FROM " + ThreatConstants.THREAT_SCHEMA_NAME +".USERS";
	
	static private String SQL_GET_USER =
		"SELECT USER_SEQ, FIRSTNAME, LASTNAME, USER_MI_NAME, USERNAME, USER_TITLE, USER_SECURE_PH, USER_COMM_PH, USER_CELL_PH, EFFECTIVE_DT, PASSWORD, ENABLED, CHANGE_PASSWORD, PASSWORD_CH_EFFECTIVE_TS, ORG_SEQ " +
		" FROM " + ThreatConstants.THREAT_SCHEMA_NAME +".USERS " +
		"WHERE USERNAME=? ";

	static private String SQL_GET_USER_BY_USERNAME =
		"SELECT USER_SEQ, FIRSTNAME, LASTNAME, USER_MI_NAME, USERNAME, USER_TITLE, USER_SECURE_PH, USER_COMM_PH, USER_CELL_PH, EFFECTIVE_DT, PASSWORD, ENABLED, CHANGE_PASSWORD, PASSWORD_CH_EFFECTIVE_TS, ORG_SEQ " +
		"  FROM " + ThreatConstants.THREAT_SCHEMA_NAME + ".USERS " +
		" WHERE USERNAME=?";	
	
	public Integer createUser(User user) {
		Long orgID = null;
		orgID = 1L;
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		// check if user already exists.
		if (getUser(user) != null) {
			throwJDBCException("User already exists.");
		}
		return template.update(SQL_INSERT_USERS_EXT, new Object[] {
				user.getFirstName(),
				user.getLastName(), 
				user.getUserMidname(), 
				user.getUsername(), 
				user.getUserTitle(), 
				user.getUserSecurePH(), 
				user.getUserCommPH(), 
				user.getUserCellPH(), 
				user.getPassword(),
				user.getEnabled(), 
				user.getEffectiveDate(),
				orgID,
				user.getPasswordChangedOnDate(),
				user.getPasswordExpired()});
	}
	
	static private String SQL_INSERT_USERS_EXT =
		" INSERT INTO " + ThreatConstants.THREAT_SCHEMA_NAME + ".USERS " +
		" 		 (FIRSTNAME, LASTNAME, USER_MI_NAME, USERNAME, USER_TITLE, USER_SECURE_PH, USER_COMM_PH, " +
		"USER_CELL_PH, PASSWORD, ENABLED, EFFECTIVE_DT, ORG_SEQ, PASSWORD_CH_EFFECTIVE_TS, CHANGE_PASSWORD, USER_SEQ) " +
		" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?, " + ThreatConstants.THREAT_SCHEMA_NAME + ".USERS_SEQUENCE.nextVAL)";

	
	public Integer isUsernameUnique(String username) {
		log.fine("uniqueUsername: " + username);
		if(username == null) {
			throwJDBCException("param username cannot be null.");
		}

		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.queryForInt(SQL_UNIQUE_USERNAME, username);
	}

	public Integer updateUser(User user) {

		if(user == null) {
			throwJDBCException("param user cannot be null.");
		}
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();

		return template.update(SQL_UPDATE_USER, user.getFirstName(),
			user.getLastName(), user.getUserMidname(), user.getUsername(), user.getUserTitle(), 
			user.getUserSecurePH(), user.getUserCommPH(), user.getUserCellPH(), user.getPassword(),
			user.getEnabled(), user.getEffectiveDate(), user.getPasswordChangedOnDate(),
			user.getPasswordExpired(), user.getOrgId(), user.getUserID());
	}
	
	public User getUser(User user) {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		try {
			return template.queryForObject(SQL_GET_USER_BY_USERNAME, new UserParameterizedRowMapper<User>(),
				user.getUsername());
		}
		catch (IncorrectResultSizeDataAccessException e){
			return null;
		}
	}

	public List<User> getAllUsers() {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.query(SQL_GET_ALL_USERS,
				new UserParameterizedRowMapper<User>());
	}

	public UserSearchCriteria getSelUser(UserSearchCriteria userSearchCriteria){
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.queryForObject(SQL_GET_USER, new UserSearchCriteriaParameterizedRowMapper<UserSearchCriteria>(),
	userSearchCriteria.getUsername());
	}
	
	
	private class UserSearchCriteriaParameterizedRowMapper<T> implements ParameterizedRowMapper<UserSearchCriteria> {

		public UserSearchCriteria mapRow(ResultSet resultSet, int row) throws SQLException {
			UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
			userSearchCriteria.setUserID(resultSet.getLong("USER_SEQ"));
			userSearchCriteria.setFirstName(resultSet.getString("FIRSTNAME"));
			userSearchCriteria.setLastName(resultSet.getString("LASTNAME"));
			userSearchCriteria.setUserMidname(resultSet.getString("USER_MI_NAME"));
			userSearchCriteria.setUsername(resultSet.getString("USERNAME"));
			userSearchCriteria.setUserTitle(resultSet.getString("USER_TITLE"));
			userSearchCriteria.setUserSecurePH(resultSet.getString("USER_SECURE_PH"));
			userSearchCriteria.setUserCommPH(resultSet.getString("USER_COMM_PH"));
			userSearchCriteria.setUserCellPH(resultSet.getString("USER_CELL_PH"));
			userSearchCriteria.setEffectiveDate(resultSet.getDate("EFFECTIVE_DT"));
			userSearchCriteria.setPassword(resultSet.getString("PASSWORD"));
			userSearchCriteria.setEnabled(resultSet.getBoolean("ENABLED"));
			userSearchCriteria.setChangePassword(resultSet.getBoolean("CHANGE_PASSWORD"));
			userSearchCriteria.setPasswordChangedOnDate(resultSet.getDate("PASSWORD_CH_EFFECTIVE_TS"));
			userSearchCriteria.setOrgId(resultSet.getLong("ORG_SEQ"));
			return userSearchCriteria;
		}
	}
	
	public void createRole(Roles role) {
		
		//  createRole
	}

	public void updateRole(Roles role) {
		//  updateRole
	}

	public void removeRole(Roles role) {
		//  removeRole
	}

	public List<Roles> getAllRoles() {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.query(SQL_GET_ALL_ROLES,
				new RolesParameterizedRowMapper<Roles>());
	}

	public List<Orgs> getAllOrg() {
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		return template.query(SQL_GET_ALL_ORGS,
				new OrgsParameterizedRowMapper<Orgs>());
	}
	
	public Integer assignRoles(User user, List<Roles> roles) {
		int rowsAffected = 0;
		if((user == null) || (roles == null)) {
			throwJDBCException("param user or roles cannot be null.");
		}
		// starting transaction
		TransactionStatus status = createTransactionDefinition();
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		// first delete current user in Authorities table
		template.update(SQL_DELETE_AUTHORITIES_BY_USER_SEQ, user.getUserID());
		try {
			//now, set roles for user in AUTHORITIES table
			for(Roles role : roles) {
				rowsAffected = rowsAffected + template.update(SQL_ASSIGN_USER_ROLES,
						user.getUsername(), user.getEffectiveDate(), role.getRoleName() );
			}
			getTransactionManager().commit(status);
			return rowsAffected;
		}
		catch(Exception e) {
			getTransactionManager().rollback(status);
			log.log(Level.WARNING, "Could not persist", e);
			return null;
		}
	}

	public List<Roles> getAssignedRolesForUser(User user) {
		if(user == null) {
			throwJDBCException("param user cannot be null.");
		}
		log.fine("UserID: " + user.getUserID());
		List<Roles>usersAssignedRolesList = new ArrayList<Roles>();
		SimpleJdbcTemplate template = getSimpleJdbcTemplate();
		usersAssignedRolesList = template.query(SQL_GET_ALL_USERS_ASSIGNED_ROLES_BY_USER_SEQ,
				new RolesParameterizedRowMapper<Roles>(), user.getUserID(), user.getUserID());
		return usersAssignedRolesList;
	}

	/*
	 *
	 */
	private void throwJDBCException(String exceptionMsg) {
		throw new JDBCException(getClass() + exceptionMsg);
	}

	private class UserParameterizedRowMapper<T> implements ParameterizedRowMapper<User> {

		public User mapRow(ResultSet resultSet, int row) throws SQLException {
			User user = new User();
			user.setUserID(resultSet.getLong("USER_SEQ"));
			user.setFirstName(resultSet.getString("FIRSTNAME"));
			user.setLastName(resultSet.getString("LASTNAME"));
			user.setUserMidname(resultSet.getString("USER_MI_NAME"));
			user.setUsername(resultSet.getString("USERNAME"));
			user.setUserTitle(resultSet.getString("USER_TITLE"));
			user.setUserSecurePH(resultSet.getString("USER_SECURE_PH"));
			user.setUserCommPH(resultSet.getString("USER_COMM_PH"));
			user.setUserCellPH(resultSet.getString("USER_CELL_PH"));
			user.setEffectiveDate(resultSet.getDate("EFFECTIVE_DT"));
			user.setPassword(resultSet.getString("PASSWORD"));
			user.setEnabled(resultSet.getBoolean("ENABLED"));
			user.setChangePassword(resultSet.getBoolean("CHANGE_PASSWORD"));
			user.setPasswordChangedOnDate(resultSet.getDate("PASSWORD_CH_EFFECTIVE_TS"));

			return user;
		}
	}

	private class RolesParameterizedRowMapper<T> implements ParameterizedRowMapper<Roles> {

		public Roles mapRow(ResultSet resultSet, int row) throws SQLException {
			Roles role = new Roles();
			role.setRoleID(resultSet.getLong("ROLE_SEQ"));
			role.setRoleName(resultSet.getString("ROLENAME"));
			role.setRoleDescription(resultSet.getString("ROLE_DESCRIPTION"));
			role.setEffectiveDate(resultSet.getDate("EFFECTIVE_DT"));

			return role;
		}
	}

	private class OrgsParameterizedRowMapper<T> implements ParameterizedRowMapper<Orgs> {

		public Orgs mapRow(ResultSet resultSet, int row) throws SQLException {
			Orgs org = new Orgs();
			org.setOrgID(resultSet.getLong("ORG_SEQ"));
			org.setOrgName(resultSet.getString("ORGNAME"));
			org.setOrgDescription(resultSet.getString("ORG_DESCRIPTION"));
			org.setEffectiveDate(resultSet.getDate("EFFECTIVE_DT"));

			return org;
		}
	}
	
}