package org.egov.sr.contract;

import java.util.List;

import javax.validation.Valid;

import org.egov.sr.model.ActionInfo;
import org.egov.sr.model.Service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

  
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ServiceRequestDetails {
	
	  @JsonProperty("services")
	  @Valid
	  private Service services;

	  @JsonProperty("actionhistory")
	  @Valid
	  private List<ActionInfo> actionhistory;

}
