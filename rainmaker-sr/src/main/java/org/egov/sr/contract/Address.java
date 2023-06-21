package org.egov.sr.contract;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.egov.sr.model.AuditDetails;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {

	@JsonProperty("uuid")
	public String uuid;

	@JsonProperty("houseNoAndStreetName")
	@Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&'-]*$")
	@Size(max=160)
	public String houseNoAndStreetName;

	@JsonProperty("mohalla")
	@Pattern(regexp="^[a-zA-Z0-9._-]*$")
	@Size(max=30)
	public String mohalla;
	
	@JsonProperty("locality")
	@Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&'-]*$")
	@Size(max=100)
	public String locality;

	@JsonProperty("city")
	@Pattern(regexp="^[a-zA-Z0-9._]*$")
	@Size(max=30)
	public String city;

	@JsonProperty("latitude")
	private Double latitude;

	@JsonProperty("longitude")
	private Double longitude;

	@JsonProperty("landmark")
	@Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&'-]*$")
	@Size(max=160)
	public String landmark;
	
	@JsonProperty("tenantId")
	@Size(min=2,max=25)
	@Pattern(regexp="^[a-zA-Z.]*$")
	public String tenantId;
	
	@JsonProperty("auditDetails")
	public AuditDetails auditDetails;
	
	
	  @JsonProperty("pinCode")
	  @Pattern(regexp="(^$|[0-9]{6})", message = "Pincode should be 6 digit number")
	  private String pinCode;

}
