package org.egov.dx.web.models.BPA;

import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
public class DscDetails {
	
	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("tenantId")
	private String tenantId = null;

	@Size(max = 64)
	@JsonProperty("documentType")
	private String documentType = null;

	@Size(max = 64)
	@JsonProperty("documentId")
	private String documentId = null;

	@Size(max = 64)
	@JsonProperty("applicationNo")
	private String applicationNo;

	@Size(max = 64)
	@JsonProperty("approvedBy")
	private String approvedBy;

	@JsonProperty("additionalDetails")
	private JsonNode additionalDetails = null;

}
