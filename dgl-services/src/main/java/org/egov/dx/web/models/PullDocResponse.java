package org.egov.dx.web.models;

import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement
@XStreamAlias("PullDocResponse")
public class PullDocResponse {

	@XStreamAlias("xmlns")
	@XStreamAsAttribute
	private String xmlns = "http://tempuri.org/";
	
	@XStreamAlias("ResponseStatus")
	private ResponseStatus responseStatus;
    
	@XStreamAlias("DocDetails")
    private DocDetailsResponse docDetails;
  
}
