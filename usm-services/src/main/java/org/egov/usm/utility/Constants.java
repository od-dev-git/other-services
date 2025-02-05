package org.egov.usm.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
	
	public Constants() {}

	public static final String ACTIVE = "ACTIVE";
	public static final String INACTIVE = "INACTIVE";

	public static final String RADIO_BUTTON = "RADIOBUTTON_ANSWER_TYPE";

	public static final String ROLE_CITIZEN = "CITIZEN";
	public static final String ROLE_EMPLOYEE = "EMPLOYEE";
	public static final String ROLE_SDA_MEMBER = "USM_SDA";
	public static final String ROLE_NODAL_OFFICER = "USM_NODAL_OFFICER";
	public static final String ROLE_ESCALATION_OFFICER = "USM_ESCALATION_OFFICER";
	
	public static final String SDA_MEMBER = "SDA MEMBER";
	public static final String USM_OFFICIAL = "USM OFFICIAL";
	
	public static final String EMPTY_STRING = "";
	
	public static final String CATEGORY_WATER = "WATER";
	public static final String CATEGORY_SANITATION= "SANITATION";
	public static final String CATEGORY_STREETLIGHT = "STREET_LIGHT";
	public static final int ADD_COMMENT_TIME_IN_HOUR = 48;
	
	//notification
	
	public static final String NOTIFICATION_LOCALE = "en_IN";
	
	public static final String LOCAL_ZONE_ID = "Asia/Kolkata";
	
	public static final String MODULE = "rainmaker-common";
	
	public static final String NOTIFICATION_OWNER_NAME_KEY = "{OWNER_NAME}";
	
	public static final String EMAIL_SUBJECT_ID_KEY = "<%application no%>";
	
	
	// ACTION_STATUS combinations for notification

    public static final String TICKET_STATUS_OPEN = "OPEN";
    public static final String TICKET_STATUS_CLOSED = "CLOSED";
    
    
    
    // SMS Template for notification
    
    public static final String TICKET_CREATED_SDA_MEMBER = "USM_TICKET_CREATED_SDA_MEMBER_SMS";
    public static final String TICKET_CREATED_NODAL_OFFICER = "USM_TICKET_CREATED_NODAL_OFFICER_SMS";
    public static final String TICKET_CLOSED_SDA_MEMBER = "USM_TICKET_CLOSE_SDA_MEMBER_SMS";
    public static final String TICKET_SATISFIED_SDA_MEMBER = "USM_TICKET_SATISFIED_SDA_MEMBER_SMS";
    public static final String TICKET_UNSATISFIED_SDA_MEMBER = "USM_TICKET_UNSATISFIED_SDA_MEMBER_SMS";
    public static final String TICKET_UNSATISFIED_NODAL_OFFICER = "USM_TICKET_UNSATISFIED_NODAL_OFFICER_SMS";
    public static final String TICKET_UNSATISFIED_ESCALATION_OFFICER  = "USM_TICKET_UNSATISFIED_ESCALATION_OFFICER_SMS";
    
    public static final String USM_SLUM_CODE_KEY  = "USM_SLUM_CODE_KEY";
    
    public static final List<String> NOTIFICATION_CODES = Collections.unmodifiableList(
    									Arrays.asList(TICKET_CREATED_SDA_MEMBER, TICKET_CREATED_NODAL_OFFICER, 
    												  TICKET_CLOSED_SDA_MEMBER, TICKET_SATISFIED_SDA_MEMBER, 
    												  TICKET_UNSATISFIED_SDA_MEMBER, TICKET_UNSATISFIED_NODAL_OFFICER,
    												  TICKET_UNSATISFIED_ESCALATION_OFFICER, USM_SLUM_CODE_KEY));
    
    
}
