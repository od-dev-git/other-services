package org.egov.report.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.report.web.model.User;

//import org.egov.demand.model.AuditDetails;
//import org.egov.demand.model.Demand;
//import org.egov.demand.model.DemandDetail;
//import org.egov.demand.model.Demand.StatusEnum;
//import org.egov.demand.web.contract.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Demand   {
    
        @JsonProperty("id")
        private String id;

        @NotNull
        @JsonProperty("tenantId")
        private String tenantId;

        @NotNull
        @JsonProperty("consumerCode")
        private String consumerCode;

        @NotNull
        @JsonProperty("consumerType")
        private String consumerType;

        @NotNull
        @JsonProperty("businessService")
        private String businessService;

        @Valid
        @JsonProperty("payer")
        private User payer;

        @NotNull
        @JsonProperty("taxPeriodFrom")
        private Long taxPeriodFrom;

        @NotNull
        @JsonProperty("taxPeriodTo")
        private Long taxPeriodTo;

        @Default
        @JsonProperty("demandDetails")
        @Valid
        @NotNull
        @Size(min=1)
        private List<DemandDetail> demandDetails = new ArrayList<>();

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;

        @JsonProperty("fixedBillExpiryDate")
        private Long fixedBillExpiryDate;

        @JsonProperty("billExpiryTime")
        private Long billExpiryTime;

        @JsonProperty("additionalDetails")
        private Object additionalDetails;

        @Default
        @JsonProperty("minimumAmountPayable")
        private BigDecimal minimumAmountPayable = BigDecimal.ZERO;
        
        @Default
        private Boolean isPaymentCompleted = false;

              /**
   * Gets or Sets status
   */
  public enum StatusEnum {
      
    ACTIVE("ACTIVE"),
    
    CANCELLED("CANCELLED"),
    
    ADJUSTED("ADJUSTED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equalsIgnoreCase(text)) {
          return b;
        }
      }
      return null;
    }
  }

        @JsonProperty("status")
        private StatusEnum status;


        public Demand addDemandDetailsItem(DemandDetail demandDetailsItem) {
        this.demandDetails.add(demandDetailsItem);
        return this;
        }

}

