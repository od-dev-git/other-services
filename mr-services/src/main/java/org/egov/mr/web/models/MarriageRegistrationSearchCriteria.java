package org.egov.mr.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarriageRegistrationSearchCriteria {


    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("applicationNumber")
    private String applicationNumber;

    @JsonProperty("mrNumbers")
    private List<String> mrNumbers;


    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonIgnore
    private String accountId;
    
    @JsonIgnore
    private String ownerId;
    
    @JsonProperty("applicationType")
    private String applicationType;


    @JsonProperty("fromDate")
    private Long fromDate = null;

    @JsonProperty("toDate")
    private Long toDate = null;

    @JsonProperty("businessService")
    private String businessService = null;
    
    @JsonProperty("employeeUuid")
    private String employeeUuid;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;
    
    @JsonProperty("isTatkalApplication")
    private Boolean isTatkalApplication;
    
    @JsonProperty("isInworkflow")
    private Boolean isInworkflow;

    @JsonIgnore
    private List<String> mrIdList;

    public boolean isEmpty() {
        return (this.tenantId == null && this.status == null  && this.applicationType == null && this.ids == null && this.applicationNumber == null
        		&& this.mrNumbers == null &&  this.mobileNumber == null &&
                this.fromDate == null && this.toDate == null 
        );
    }

    public boolean tenantIdOnly() {
        return (this.tenantId != null && this.status == null && this.ids == null  && this.applicationType == null && this.applicationNumber == null
        		&& this.mrNumbers == null && this.mobileNumber == null &&
                this.fromDate == null && this.toDate == null 
        );
    }

}
