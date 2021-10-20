package org.egov.mr.web.models;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
public class MarriagePlace {
	
	@JsonProperty("id")
    @Size(max=64)
    private String id;
	
	
	@Size(max=64)
    @JsonProperty("ward")
    private String ward ;
	
	
	@Size(max=256)
    @JsonProperty("placeOfMarriage")
    private String placeOfMarriage = null;
	
	@Valid
    @JsonProperty("locality")
    private Boundary locality = null;
	
	@Size(max=64)
    @JsonProperty("pinCode")
	@Pattern(regexp="(^$|[0-9]{6})", message = "Pincode should be 6 digit number")
    private String pinCode = null;
	
	
	@JsonProperty("additionalDetail")
    private JsonNode additionalDetail = null;
	
	  @JsonProperty("auditDetails")
      private AuditDetails auditDetails = null;


}
