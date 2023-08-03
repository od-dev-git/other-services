package org.egov.dx.web.models.BPA;

import javax.xml.bind.annotation.XmlAttribute;

import org.egov.dx.web.models.Address;
import org.egov.dx.web.models.MR.PersonMR;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@XStreamAlias("Person")
public class PersonBPA {
	
	@XStreamAsAttribute
    @XStreamAlias("uid")
    private String uid = "";
	
	@XStreamAsAttribute
    @XStreamAlias("title")
    private String title = "";
    
	@XStreamAsAttribute
    @XStreamAlias("name")
    private String name = "";
    
	@XStreamAsAttribute
    @XStreamAlias("dob")
    private String dob = "";
	
	@XStreamAsAttribute
    @XStreamAlias("age")
    private String age = "";
	
	@XStreamAsAttribute
    @XStreamAlias("swd")
    private String swd = "";
	
	@XStreamAsAttribute
    @XStreamAlias("swdIndicator")
    private String swdIndicator = "";
	
	@XStreamAsAttribute
    @XStreamAlias("motherName")
    private String motherName = "";
    
	@XStreamAsAttribute
    @XStreamAlias("gender")
    private String gender = "";
    
	@XStreamAsAttribute
    @XStreamAlias("maritalStatus")
    private String maritalStatus = "";
	
	@XStreamAsAttribute
    @XStreamAlias("relationWithHof")
    private String relationWithHof = "";
	
	@XStreamAsAttribute
    @XStreamAlias("disabilityStatus")
    private String disabilityStatus = "";
	
	@XStreamAsAttribute
    @XStreamAlias("category")
    private String category = "";
    
	@XStreamAsAttribute
    @XStreamAlias("religion")
    private String religion = "";
	
	@XStreamAsAttribute
    @XStreamAlias("phone")
    private String phone = "";
	
	@XStreamAsAttribute
    @XStreamAlias("email")
    private String email = "";
    
    @XmlAttribute
    @XStreamAlias("Address")
    private Address address;
    
    @XmlAttribute
    @XStreamAlias("Photo")
    private Photo photo;

}
