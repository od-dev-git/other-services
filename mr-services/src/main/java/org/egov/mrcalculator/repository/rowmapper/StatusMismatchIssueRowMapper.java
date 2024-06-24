package org.egov.mrcalculator.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.mr.web.models.issuefix.StatusMismatchIssueFix;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class StatusMismatchIssueRowMapper implements ResultSetExtractor<List<StatusMismatchIssueFix>> {

	List<StatusMismatchIssueFix> statusMismatchIssueFixList = new ArrayList<>();

	@Override
	public List<StatusMismatchIssueFix> extractData(ResultSet rs) throws SQLException, DataAccessException {
		while (rs.next()) {
			StatusMismatchIssueFix ps = StatusMismatchIssueFix.builder().tenantId(rs.getString("tenantid"))
					.applicationNo(rs.getString("applicationnumber"))
					.actionInProcessInstance(rs.getString("actioninprocessinstance"))
					.currentStatus(rs.getString("currentstatus")).expectedStatus(rs.getString("expectedstatus"))
					.build();
			statusMismatchIssueFixList.add(ps);
		}
		return statusMismatchIssueFixList;
	}

}
