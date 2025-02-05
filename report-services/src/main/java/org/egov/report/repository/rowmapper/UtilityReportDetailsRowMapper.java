package org.egov.report.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.report.model.AuditDetails;
import org.egov.report.model.UtilityReportDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class UtilityReportDetailsRowMapper implements ResultSetExtractor<List<UtilityReportDetails>> {

	@Override
	public List<UtilityReportDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, UtilityReportDetails> reportDetailsMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("id");
			UtilityReportDetails reportDetails = reportDetailsMap.get(id);

			if (reportDetails == null) {
				Long lastModifiedTime = rs.getLong("lastmodifiedtime");
				if (rs.wasNull())
					lastModifiedTime = null;

				AuditDetails auditdetails = AuditDetails.builder()
						.createdBy(rs.getString("createdby"))
						.createdTime(rs.getLong("createdtime"))
						.lastModifiedBy(rs.getString("lastModifiedby"))
						.lastModifiedTime(lastModifiedTime)
						.build();
				
				Object additionalDetails = new Gson().fromJson(rs.getString("additionalDetails").equals("{}")
						|| rs.getString("additionalDetails").equals("null") ? null : rs.getString("additionalDetails"),
						Object.class);

				reportDetails = UtilityReportDetails.builder()
						.id(id)
						.tenantId(rs.getString("tenantId"))
						.reportType(rs.getString("reporttype"))
						.fileStoreId(rs.getString("filestoreid"))
						.fileName(rs.getString("filename"))
						.financialYear(rs.getString("financialyear"))
						.additionalDetails(additionalDetails)
						.auditDetails(auditdetails)
						.build();

				reportDetailsMap.put(id, reportDetails);
			}
		}
		return new ArrayList<>(reportDetailsMap.values());
	}

    
}
