package org.egov.sr.util;

import org.egov.sr.web.models.AuditDetails;
import org.egov.sr.web.models.Service;
import org.springframework.stereotype.Component;

@Component
public class PGRUtils {



    /**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Service service, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(service.getAuditDetails().getCreatedBy()).lastModifiedBy(by)
                    .createdTime(service.getAuditDetails().getCreatedTime()).lastModifiedTime(time).build();
    }

}
