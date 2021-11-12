package org.egov.mr.web.models;

import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
@EqualsAndHashCode
public class DscDetails   {

        @Size(max=64)
        @JsonProperty("id")
        private String id;

        @Size(max=64)
        @JsonProperty("tenantId")
        private String tenantId = null;

        @Size(max=64)
        @JsonProperty("documentType")
        private String documentType = null;

        @Size(max=64)
        @JsonProperty("documentId")
        private String documentId = null;
        
        @Size(max=64)
        @JsonProperty("applicationNumber")
        private String applicationNumber;

        @Size(max=64)
        @JsonProperty("approvedBy")
        private String approvedBy;

        @JsonProperty("additionalDetail")
        private JsonNode additionalDetail = null;
        
        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;


}

