package org.egov.mr.web.models.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSearchCriteria {
	
	@JsonProperty("tenantId")
	private String tenantId=null;
	
	@JsonProperty("id")
	private String id=null;
	
	@JsonProperty("consumerCode")
	private String consumerCode=null;
	
	@JsonProperty("businessService")
	private String businessService=null;
	
	@JsonProperty("status")
	private String status=null;
	
	

}
