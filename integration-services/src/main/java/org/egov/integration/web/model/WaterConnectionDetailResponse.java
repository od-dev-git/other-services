package org.egov.integration.web.model;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.model.WSConnection;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class WaterConnectionDetailResponse {

	 @JsonProperty("responseInfo")
	 ResponseInfo responseInfo;
	 
	 @JsonProperty("WaterConnection")
	 List<WSConnection> connections;

}
