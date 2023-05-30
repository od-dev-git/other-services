package org.egov.dx.web.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressMR {

	  @Size(max=64)
      @JsonProperty("id")
      private String id;

      @Size(max=64)
      @JsonProperty("tenantId")
      private String tenantId = null;

      @Size(max=256)
      @JsonProperty("addressLine1")
      private String addressLine1 = null;

      @Size(max=64)
      @JsonProperty("country")
      private String country = null;

      @Size(max=64)
      @JsonProperty("state")
      private String state = null;
      
      @Size(max=64)
      @JsonProperty("district")
      private String district = null;

      @Size(max=64)
      @JsonProperty("pinCode")
      @Pattern(regexp="(^$|[0-9]{6})", message = "Pincode should be 6 digit number")
      private String pinCode = null;

      @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
      @JsonProperty("contact")
      private String contact;

      @Size(max=128)
      @JsonProperty("emailAddress")
      @Pattern(regexp = "^$|^(?=^.{1,64}$)((([^<>()\\[\\]\\\\.,;:\\s$*@'\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@'\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,})))$", message = "Invalid emailId")
      private String emailAddress;
	
}
