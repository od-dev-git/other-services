package org.egov.dx.web.models.TL;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.Role;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
	
	@JsonProperty("id")
    private Long id;

    @Size(max=64)
    @JsonProperty("uuid")
    private String uuid;

    @Size(max=64)
    @JsonProperty("userName")
    private String userName;

    @Size(max=64)
    @JsonProperty("password")
    private String password;

    @JsonProperty("salutation")
    private String salutation;

    @NotNull
    @Size(max=100)
    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @NotNull
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @Size(max=128)
    @JsonProperty("emailId")
    @Pattern(regexp = "^$|^(?=^.{1,64}$)((([^<>()\\[\\]\\\\.,;:\\s$*@'\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@'\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,})))$", message = "Invalid emailId")
    private String emailId;

    @Size(max=50)
    @JsonProperty("altContactNumber")
    private String altContactNumber;

    @Size(max=10)
    @JsonProperty("pan")
    private String pan;

    @Pattern(regexp = "^[0-9]{12}$", message = "AdharNumber should be 12 digit number")
    @JsonProperty("aadhaarNumber")
    private String aadhaarNumber;

    @Size(max=300)
    @JsonProperty("permanentAddress")
    private String permanentAddress;

    @Size(max=300)
    @JsonProperty("permanentCity")
    private String permanentCity;

    @Size(max=10)
    @JsonProperty("permanentPinCode")
    private String permanentPincode;

    @Size(max=300)
    @JsonProperty("correspondenceCity")
    private String correspondenceCity;

    @Size(max=10)
    @JsonProperty("correspondencePinCode")
    private String correspondencePincode;

    @Size(max=300)
    @JsonProperty("correspondenceAddress")
    private String correspondenceAddress;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("dob")
    private Long dob;

    @JsonProperty("pwdExpiryDate")
    private Long pwdExpiryDate;

    @Size(max=16)
    @JsonProperty("locale")
    private String locale;

    @Size(max=50)
    @JsonProperty("type")
    private String type;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("accountLocked")
    private Boolean accountLocked;

    @JsonProperty("roles")
    @Valid
    private List<Role> roles;

    @Size(max=100)
    @JsonProperty("fatherOrHusbandName")
    private String fatherOrHusbandName;

    @Size(max=32)
    @JsonProperty("bloodGroup")
    private String bloodGroup;

    @JsonProperty("identificationMark")
    private String identificationMark;

    @JsonProperty("photo")
    private String photo;

    @Size(max=64)
    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("createdDate")
    private Long createdDate;

    @Size(max=64)
    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("lastModifiedDate")
    private Long lastModifiedDate;

    @JsonProperty("otpReference")
    private String otpReference;

    @Size(max=256)
    @JsonProperty("tenantId")
    private String tenantId;

}
