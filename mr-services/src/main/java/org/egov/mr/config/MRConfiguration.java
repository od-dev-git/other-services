package org.egov.mr.config;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Import({TracerConfiguration.class})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class MRConfiguration {



    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    @Value("${egov.user.username.prefix}")
    private String usernamePrefix;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.idgen.mr.applicationNum.name}")
    private String applicationNumberIdgenNameMR;

    @Value("${egov.idgen.mr.applicationNum.format}")
    private String applicationNumberIdgenFormatMR;

    @Value("${egov.idgen.mr.mrnumber.name}")
    private String mrNumberIdgenNameMR;

    @Value("${egov.idgen.mr.mrnumber.format}")
    private String mrNumberIdgenFormatMR;

  

    //Persister Config
    @Value("${persister.save.marriageregistration.topic}")
    private String saveTopic;

    @Value("${persister.update.marriageregistration.topic}")
    private String updateTopic;

    @Value("${persister.update.marriageregistration.workflow.topic}")
    private String updateWorkflowTopic;

    @Value("${persister.update.marriageregistration.dscdetails.topic}")
    private String updateDscDetailsTopic;


    //Location Config
    @Value("${egov.location.host}")
    private String locationHost;

    @Value("${egov.location.context.path}")
    private String locationContextPath;

    @Value("${egov.location.endpoint}")
    private String locationEndpoint;

    @Value("${egov.location.hierarchyTypeCode}")
    private String hierarchyTypeCode;

    @Value("${egov.mr.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.mr.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.mr.max.limit}")
    private Integer maxSearchLimit;



    @Value("${egov.billingservice.host}")
    private String billingHost;

    @Value("${egov.bill.gen.endpoint}")
    private String fetchBillEndpoint;




    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.localization.statelevel}")
    private Boolean isLocalizationStateLevel;



    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;


 // Workflow
    @Value("${create.mr.workflow.name}")
    private String mrBusinessServiceValue;

    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;

    @Value("${workflow.process.search.path}")
    private String wfProcessSearchPath;

    //Allowed Search Parameters
    @Value("${citizen.allowed.search.params}")
    private String allowedCitizenSearchParameters;

    @Value("${employee.allowed.search.params}")
    private String allowedEmployeeSearchParameters;
    
    // url shortner

    @Value("${egov.url.shortner.host}")
    private String urlShortnerHost;

    @Value("${mr.url.shortner.endpoint}")
    private String urlShortnerEndpoint;

    @Value("${egov.usr.events.view.application.triggers}")
    private String viewApplicationTriggers;

    @Value("${egov.usr.events.view.application.link}")
    private String viewApplicationLink;

    @Value("${egov.usr.events.view.application.code}")
    private String viewApplicationCode;
    
    //USER EVENTS
	@Value("${egov.ui.app.host}")
	private String uiAppHost;
    
	@Value("${egov.usr.events.create.topic}")
	private String saveUserEventsTopic;
		
	@Value("${egov.usr.events.pay.link}")
	private String payLink;

    @Value("${egov.msg.pay.link}")
    private String payLinkSMS;
	
	@Value("${egov.usr.events.pay.code}")
	private String payCode;
	
	@Value("${egov.user.event.notification.enabledForMR}")
	private Boolean isUserEventsNotificationEnabledForMR;

    @Value("${egov.user.event.notification.enabledForMRCorrection}")
    private Boolean isUserEventsNotificationEnabledForMRCorrection;

	@Value("${egov.usr.events.pay.triggers}")
	private String payTriggers;

	
    //SMS
    @Value("${kafka.topics.notification.sms}")
    private String smsNotifTopic;

    @Value("${notification.sms.enabled.forMR}")
    private Boolean isMRSMSEnabled;

    @Value("${notification.sms.enabled.forMRCORRECTION}")
    private Boolean isMRCORRECTIONSMSEnabled;
    
    
    @Value("${egov.receipt.businessserviceMR}")
    private String businessServiceMR;
    
    //Email
    @Value("${notification.email.enabled.forMR}")
    private Boolean isEmailEnabled;
    
    @Value("${kafka.topics.notification.email}")
    private String emailNotifTopic;
    
    @Value("${text.for.subject.email.notif}")
    private String emailSubject;
    
    @Value("${mr.payment.issuefix}")
	private Boolean mrPaymentIssueFIx;
    
    @Value("${mr.issuefix.rolecode}")
	private String mrIssueFixRoleCode;
    
    @Value("${mr.issuefix.tenantid}")
	private String mrIssueFixTenantId;
    
    @Value("${mr.issue.resolver.uuid}")
	private String mrIssueFixUUID;
    
    @Value("${mr.status.mismatch.issuefix}")
	private Boolean mrStatusMismatchIssueFIx;

}
