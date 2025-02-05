package org.egov.integration.repository.builder;

import java.util.List;

import org.egov.integration.config.IntegrationConfiguration;
import org.egov.integration.model.BPAVerificationSearchCriteria;
import org.egov.integration.model.revenue.RevenueNotificationSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RevenueNotificationQueryBuilder {

	private IntegrationConfiguration config;

	public RevenueNotificationQueryBuilder(IntegrationConfiguration config) {
		this.config = config;
	}

	private static final String SELECT = " SELECT ";
	private static final String INNER_JOIN = " INNER JOIN ";
	private static final String AND = " AND ";
	private static final String FROM = " from ";
	private static final String WHERE = "where ";
	private static final String ON = " on ";
	private static final String AS = " as ";
	private static final String IN = " in ";
	private static final String GROUP_BY = " group by ";
	private static final String ORDER_BY = " order by ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY lastmodifiedtime DESC , id ) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";

	private static final String revenueNotificationValues =  " rn.id, rn.districtname, rn.tenantid, rn.revenuevillage, rn.plotno, rn.flatno,"
			+ " rn.address, rn.actiontaken, rn.action , rn.waterconsumerno, rn.propertyid,"
			+ " rn.additionaldetails, rn.createdby, rn.createdtime, rn.lastmodifiedby, rn.lastmodifiedtime ";

	private static final String revenueNotificationOwnerValues = " rno.revenuenotificationid, rno.ownername, rno.mobilenumber, rno.ownertype ";
	
	private static final String revenueNotificationTable = " eg_uis_revenuenotification rn ";
	
	private static final String revenueNotificationOwnersTable = " eg_uis_revenuenotification_owners rno ";

	private static final String QUERY_FOR_REVENUE_NOTIFICATIONS_SEARCH = SELECT 
			+ revenueNotificationValues + " , " + revenueNotificationOwnerValues
			+ FROM + revenueNotificationTable
			+ INNER_JOIN + revenueNotificationOwnersTable + ON+ " rn.id = rno.revenuenotificationid ";

	private static final String BPA_SEARCH_QUERY = "select approvalno, approvaldate from eg_bpa_buildingplan where approvalno = ?";

	private String addPaginationWrapper(String query, List<Object> preparedStmtList,
			RevenueNotificationSearchCriteria criteria) {
		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);

		log.info("Final Query : " + finalQuery);
		
		return finalQuery;
	}

	public String getNotificationsSearchQuery(RevenueNotificationSearchCriteria searchCriteria,
			List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(QUERY_FOR_REVENUE_NOTIFICATIONS_SEARCH);

		query.append(WHERE).append(" rn.tenantid = ? ");
		preparedStmtList.add(searchCriteria.getTenantId());
		
		if((searchCriteria.getActiontaken() != null)) {
        	query.append(AND).append(" rn.actiontaken = ? ");
        	preparedStmtList.add(searchCriteria.getActiontaken());
        }
		
		if(StringUtils.hasText(searchCriteria.getPropertyid())) {
        	query.append(AND).append(" rn.propertyid = ? ");
        	preparedStmtList.add(searchCriteria.getPropertyid());
        }
		
		if(StringUtils.hasText(searchCriteria.getWsconsumerno())) {
        	query.append(AND).append(" rn.waterconsumerno = ? ");
        	preparedStmtList.add(searchCriteria.getWsconsumerno());
        }

		return addPaginationWrapper(query.toString(), preparedStmtList, searchCriteria);

	}

	public String getBPADataSearchQuery(BPAVerificationSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(BPA_SEARCH_QUERY);

		if (!StringUtils.isEmpty(criteria.getPermitNumber())) {
			preparedStmtList.add(criteria.getPermitNumber());
		}

		return query.toString();
	}

}
