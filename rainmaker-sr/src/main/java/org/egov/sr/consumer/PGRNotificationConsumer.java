package org.egov.sr.consumer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.sr.contract.Action;
import org.egov.sr.contract.ActionItem;
import org.egov.sr.contract.EmailRequest;
import org.egov.sr.contract.Event;
import org.egov.sr.contract.EventRequest;
import org.egov.sr.contract.Recepient;
import org.egov.sr.contract.SMSRequest;
import org.egov.sr.contract.ServiceReqSearchCriteria;
import org.egov.sr.contract.ServiceRequest;
import org.egov.sr.contract.ServiceResponse;
import org.egov.sr.model.ActionInfo;
import org.egov.sr.model.Email;
import org.egov.sr.model.Service;
import org.egov.sr.model.Source;
import org.egov.sr.producer.PGRProducer;
import org.egov.sr.service.GrievanceService;
import org.egov.sr.service.NotificationService;
import org.egov.sr.utils.PGRConstants;
import org.egov.sr.utils.PGRUtils;
import org.egov.sr.utils.SRUtils;
import org.egov.sr.utils.WorkFlowConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class PGRNotificationConsumer {

    @Autowired
    private PGRProducer pGRProducer;

    @Value("${egov.hr.employee.v2.host}")
    private String hrEmployeeV2Host;

    @Value("${egov.hr.employee.v2.search.endpoint}")
    private String hrEmployeeV2SearchEndpoint;

    @Value("${kafka.topics.notification.sms}")
    private String smsNotifTopic;

    @Value("${kafka.topics.notification.email}")
    private String emailNotifTopic;

    @Value("${notification.sms.enabled}")
    private Boolean isSMSNotificationEnabled;

    @Value("${notification.email.enabled}")
    private Boolean isEmailNotificationEnabled;
    
    @Value("${text.for.subject.email.notif}")
    private String emailSubject ;

    @Value("${reassign.complaint.enabled}")
    private Boolean isReassignNotifEnaled;

    @Value("${reopen.complaint.enabled}")
    private Boolean isReopenNotifEnaled;

    @Value("${comment.by.employee.notif.enabled}")
    private Boolean isCommentByEmpNotifEnaled;

    @Value("${email.template.path}")
    private String emailTemplatePath;

    @Value("${date.format.notification}")
    private String notificationDateFormat;

    @Value("${egov.ui.app.host}")
    private String uiAppHost;

    @Value("${egov.ui.feedback.url}")
    private String uiFeedbackUrl;

    @Value("${notification.allowed.on.status}")
    private String notificationEnabledStatuses;

    @Value("${egov.pgr.app.playstore.link}")
    private String appDownloadLink;

    @Value("${notification.fallback.locale}")
    private String fallbackLocale;

    @Value("${egov.usr.events.notification.enabled}")
    private Boolean isUsrEventNotificationEnabled;

    @Value("${egov.usr.events.create.topic}")
    private String saveUserEventsTopic;

    @Value("${egov.usr.events.review.link}")
    private String reviewLink;

    @Value("${egov.usr.events.review.code}")
    private String reviewCode;

    @Value("${egov.usr.events.reopen.code}")
    private String reopenCode;


    @Autowired
    private PGRUtils pGRUtils;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GrievanceService requestService;
    
    @Autowired 
    private SRUtils srUtils;

    @KafkaListener(topics = {"${kafka.topics.save.service}", "${kafka.topics.update.service}"})

    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        ServiceRequest serviceReqRequest = new ServiceRequest();
        try {
            serviceReqRequest = mapper.convertValue(record, ServiceRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        process(serviceReqRequest);
    }


    /**
     * Sends notifications on different topics for the consumer to pick.
     *
     * @param serviceReqRequest
     */
    public void process(ServiceRequest serviceReqRequest) {
        if (!CollectionUtils.isEmpty(serviceReqRequest.getActionInfo())) {
            for (ActionInfo actionInfo : serviceReqRequest.getActionInfo()) {
                if (null != actionInfo && (!StringUtils.isEmpty(actionInfo.getStatus()) || !StringUtils.isEmpty(actionInfo.getComment()))) {
                    Service service = serviceReqRequest.getServices()
                            .get(serviceReqRequest.getActionInfo().indexOf(actionInfo));
                    if (isNotificationEnabled(actionInfo.getStatus(), serviceReqRequest.getRequestInfo().getUserInfo().getType(), actionInfo.getComment(), actionInfo.getAction())) {
                        if (isSMSNotificationEnabled) {
                            List<SMSRequest> smsRequests = prepareSMSRequestForSR(service, actionInfo, serviceReqRequest.getRequestInfo());
                            if (CollectionUtils.isEmpty(smsRequests)) {
                                log.info("Messages from localization couldn't be fetched!");
                            }
                            for (SMSRequest smsRequest : smsRequests) {
                                pGRProducer.push(smsNotifTopic, smsRequest);
                            }
                        }
                        if (isEmailNotificationEnabled)
                        {
                        	List<EmailRequest> emailRequests = prepareEmailRequestForSR(service, actionInfo, serviceReqRequest.getRequestInfo());
                        	 if (CollectionUtils.isEmpty(emailRequests)) {
                                 log.info("Messages from localization couldn't be fetched!");
                             }
                             for (EmailRequest emailRequest : emailRequests) {
                                 pGRProducer.push(emailNotifTopic, emailRequest);
                             }
                        
                        
                        }

                        if (isUsrEventNotificationEnabled) {
                            EventRequest request = prepareuserEvents(service, actionInfo, serviceReqRequest.getRequestInfo());
                            pGRProducer.push(saveUserEventsTopic, request);
                        }

                    } else {
                        log.info("Notification disabled for this case!");
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }
    }


	private List<EmailRequest> prepareEmailRequestForSR(Service serviceReq, ActionInfo actionInfo,
			RequestInfo requestInfo) {
		List<EmailRequest> emailRequestsTobeSent = new ArrayList<>();
		String emailSubjectForEmail = emailSubject;
		try {

			String emailId = serviceReq.getEmail();
			if (StringUtils.isEmpty(emailId) || "null".equalsIgnoreCase(emailId.trim())) {
				log.info(" No emailId found for the Service Request Id " + serviceReq.getEmail());
			}

			String message = getMessageFromLocalization(serviceReq, actionInfo, requestInfo);
			if (StringUtils.isEmpty(message))
				log.info("No Message Fetched from Localization for EMAIL");;

			Set<String> emailTo = new LinkedHashSet<String>();
			emailTo.add(emailId);

			emailSubjectForEmail = emailSubjectForEmail.replaceAll(PGRConstants.EMAIL_SUBJECT_ID_KEY,
					serviceReq.getServiceRequestId());

			emailRequestsTobeSent.add(EmailRequest.builder().requestInfo(requestInfo)
					.email(new Email(emailTo, emailSubjectForEmail, message, true)).build());
		} catch (Exception e) {
			log.info("  Unable to sent email for service id   ", serviceReq.getServiceRequestId());
			e.printStackTrace();
		}

		return emailRequestsTobeSent;

	}


	private List<SMSRequest> prepareSMSRequestForSR(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
    	List<SMSRequest> smsRequestsTobeSent = new ArrayList<>();
		try {

			String phone = serviceReq.getPhone();
			if (StringUtils.isEmpty(phone) || "null".equalsIgnoreCase(phone.trim())) {
				log.info(" No phone number found for service Request " + serviceReq.getServiceRequestId());
			}
			String message = getMessageFromLocalization(serviceReq, actionInfo, requestInfo);
			if (StringUtils.isEmpty(message))
				log.info("SMS Not fetched from Localization");
			smsRequestsTobeSent.add(SMSRequest.builder().mobileNumber(phone).message(message).build());

			return smsRequestsTobeSent;
		} catch (Exception e) {
			if(actionInfo.getStatus()==null)
			{
				log.error(" Status is empty , so unable get the Receptors of Notification.");
			}
			e.printStackTrace();
			return smsRequestsTobeSent;
		}
	}


	private String getMessageFromLocalization(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
		
		String tenantId = "od";
		String customMessage = "";
		String localizationMessages = srUtils.getLocalizationMessages(tenantId, requestInfo);
		if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
			String notificationCode = PGRConstants.CREATE_MSG_CODE;
			customMessage = srUtils.getMessageTemplate(notificationCode, localizationMessages);
			customMessage = customMessage.replace("<1>", serviceReq.getServiceRequestId());
		} else if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_FORWARD_TO_L2)) {
			String notificationCode = PGRConstants.L2_FORWARD_MSG_CODE;
			customMessage = srUtils.getMessageTemplate(notificationCode, localizationMessages);
			customMessage = customMessage.replace("<1>", serviceReq.getServiceRequestId());
		} else if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_FORWARD_TO_L3)) {
			String notificationCode = PGRConstants.L3_FORWARD_MSG_CODE;
			customMessage = srUtils.getMessageTemplate(notificationCode, localizationMessages);
			customMessage = customMessage.replace("<1>", serviceReq.getServiceRequestId());
		} else if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_CLOSE)) {
			String notificationCode = PGRConstants.CLOSED_MSG_CODE;
			customMessage = srUtils.getMessageTemplate(notificationCode, localizationMessages);
			customMessage = customMessage.replace("<1>", serviceReq.getServiceRequestId());
		} 
		return customMessage;
	}


	/**
     * Prepares event to be registered in user-event service.
     * Currently, only the notifications addressed to CITIZEN are considered.
     *
     * @param serviceReq
     * @param actionInfo
     * @param requestInfo
     * @return
     */
    public EventRequest prepareuserEvents(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
        List<Event> events = new ArrayList<>();
        if (StringUtils.isEmpty(actionInfo.getAssignee()) && !actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
            try {
                actionInfo.setAssignee(notificationService.getCurrentAssigneeForTheServiceRequest(serviceReq, requestInfo));
            } catch (Exception e) {
                log.error("Exception while explicitly setting assignee!");
            }
        }
        for (String role : pGRUtils.getReceptorsOfNotification(actionInfo.getStatus(), actionInfo.getAction())) {
            if (role.equals(PGRConstants.ROLE_EMPLOYEE))
                continue;
            String message = getMessageForSMS(serviceReq, actionInfo, requestInfo, role);
            String data = notificationService.getMobileAndIdForNotificationService(requestInfo, serviceReq.getAccountId(), serviceReq.getTenantId(), actionInfo.getAssignee(), role);
            if (StringUtils.isEmpty(message))
                continue;
            List<String> toUsers = new ArrayList<>();
            toUsers.add(data.split("[|]")[1]);
            Recepient recepient = Recepient.builder()
                    .toUsers(toUsers).toRoles(null).build();

            Action action = null;
            if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_RESOLVED)) {
                List<ActionItem> items = new ArrayList<>();
                String actionLink = reviewLink.replace("$mobile", data.split("[|]")[0]).replace("$servicerequestid", serviceReq.getServiceRequestId().replaceAll("[/]", "%2F"));
                actionLink = uiAppHost + actionLink;
                ActionItem item = ActionItem.builder().actionUrl(actionLink).code(reviewCode).build();
                items.add(item);
                action = Action.builder().actionUrls(items).build();
            }
            Event event = Event.builder()
                    .tenantId(serviceReq.getTenantId())
                    .description(message)
                    .eventType(PGRConstants.USREVENTS_EVENT_TYPE)
                    .name(PGRConstants.USREVENTS_EVENT_NAME)
                    .postedBy(PGRConstants.USREVENTS_EVENT_POSTEDBY)
                    .source(Source.WEBAPP)
                    .recepient(recepient)
                    .eventDetails(null)
                    .actions(action).build();

            events.add(event);
        }
        return EventRequest.builder().requestInfo(requestInfo).events(events).build();
    }

    public List<SMSRequest> prepareSMSRequest(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
    	List<SMSRequest> smsRequestsTobeSent = new ArrayList<>();
    	try {
			
			if (StringUtils.isEmpty(actionInfo.getAssignee()) && actionInfo.getAction()!=null && !actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
			    try {
			        actionInfo.setAssignee(notificationService.getCurrentAssigneeForTheServiceRequest(serviceReq, requestInfo));
			    } catch (Exception e) {
			        log.error("Exception while explicitly setting assignee!");
			    }
			}
			for (String role : pGRUtils.getReceptorsOfNotification(actionInfo.getStatus(), actionInfo.getAction())) {
			    String phoneNumberRetrived = notificationService.getMobileAndIdForNotificationService(requestInfo, serviceReq.getAccountId(), serviceReq.getTenantId(), actionInfo.getAssignee(), role);
			    phoneNumberRetrived = phoneNumberRetrived.split("[|]")[0];
			    String phone = StringUtils.isEmpty(phoneNumberRetrived) ? serviceReq.getPhone() : phoneNumberRetrived;
			    if(StringUtils.isEmpty(phone) || "null".equalsIgnoreCase(phone.trim()))
			    {
			    	log.info(" No phone number found for the role "+role);
			    	continue;
			    }
			    List<String> roleCodes = requestInfo.getUserInfo()
						.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
				String precedentRole = pGRUtils.getPrecedentRole(roleCodes);
				if(actionInfo.getAction()!=null && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_CLOSE) && PGRConstants.ROLE_SYSTEM.equalsIgnoreCase(precedentRole))
				{
					log.info(" Message is not sent in case of  running batch for closure of complaints for role "+precedentRole);
					continue;
				}
			    
			    String message = getMessageForSMS(serviceReq, actionInfo, requestInfo, role);
			    if (StringUtils.isEmpty(message))
			        continue;
			    smsRequestsTobeSent.add(SMSRequest.builder().mobileNumber(phone).message(message).build());
			}
			
			//code added to send the  messages for multiple employees for status open,escaltedlevel1pending,escaltedlevel2pending,escaltedlevel3pending,escaltedlevel4pending
			
			if(actionInfo.getAction()!=null && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN))
			{
				setSMSRequestUsingRoleAndDepartment(smsRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_GRO,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL1_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setSMSRequestUsingRoleAndDepartment(smsRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER1,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL2_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setSMSRequestUsingRoleAndDepartment(smsRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER2,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL3_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setSMSRequestUsingRoleAndDepartment(smsRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER3,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL4_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				//In case of escalation level 4 employee tenant id is od .
				
//				String department =  notificationService.getDepartmentFromServiceCode(serviceReq, requestInfo);
//				
//				if(!StringUtils.isEmpty(department))
//				{
					setSMSRequestUsingRoleAndDepartment(smsRequestsTobeSent,PGRConstants.TENANT_ID,PGRConstants.ROLE_ESCALATION_OFFICER4,null,serviceReq,actionInfo,requestInfo);
//				}else
//					log.info(" Department not found for the complaint with service code ",serviceReq.getServiceCode());				
				
			}
			
			
			
			return smsRequestsTobeSent;
		} catch (Exception e) {
			if(actionInfo.getStatus()==null)
			{
				log.error(" Status is empty , so unable get the Receptors of Notification.");
			}
			e.printStackTrace();
			return smsRequestsTobeSent;
		}
    }
    
    
    public List<EmailRequest> prepareEmailRequest(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
        
    	List<EmailRequest> emailRequestsTobeSent = new ArrayList<>();
    	String emailSubjectForEmail = emailSubject ;
    	try {
			
			if (StringUtils.isEmpty(actionInfo.getAssignee()) && actionInfo.getAction()!=null && !actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
			    try {
			        actionInfo.setAssignee(notificationService.getCurrentAssigneeForTheServiceRequest(serviceReq, requestInfo));
			    } catch (Exception e) {
			        log.error("Exception while explicitly setting assignee!");
			    }
			}
			for (String role : pGRUtils.getReceptorsOfNotification(actionInfo.getStatus(), actionInfo.getAction())) {
			    String emailRetrived = notificationService.getEmailIdForNotificationService(requestInfo, serviceReq.getAccountId(), serviceReq.getTenantId(), actionInfo.getAssignee(), role);
			    emailRetrived = emailRetrived.split("[|]")[0];
			    String emailId = StringUtils.isEmpty(emailRetrived) ? serviceReq.getEmail() : emailRetrived;
			    if(StringUtils.isEmpty(emailId) || "null".equalsIgnoreCase(emailId.trim()))
			    {
			    	log.info(" No emailId found for the role "+role);
			    	continue;
			    }
			    List<String> roleCodes = requestInfo.getUserInfo()
						.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
				String precedentRole = pGRUtils.getPrecedentRole(roleCodes);
				if(actionInfo.getAction()!=null && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_CLOSE) && PGRConstants.ROLE_SYSTEM.equalsIgnoreCase(precedentRole))
				{
					log.info(" Message is not sent in case of  running batch for closure of complaints for role "+precedentRole);
					continue;
				}
			    String message = getMessageForEmail(serviceReq, actionInfo, requestInfo, role);
			    if (StringUtils.isEmpty(message))
			        continue;
			    
			    Set<String> emailTo = new LinkedHashSet<String>();
			    emailTo.add(emailId);
			    
			    emailSubjectForEmail = emailSubjectForEmail.replaceAll(PGRConstants.EMAIL_SUBJECT_ID_KEY, serviceReq.getServiceRequestId());
			    
			    emailRequestsTobeSent.add(EmailRequest.builder().requestInfo(requestInfo).email(new  Email(emailTo, emailSubjectForEmail, message, true)).build());
			}
			
			//code added to send the  messages for multiple employees for status open,escaltedlevel1pending,escaltedlevel2pending,escaltedlevel3pending,escaltedlevel4pending
			
			if(actionInfo.getAction()!=null && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN))
			{
				setEmailRequestUsingRoleAndDepartment(emailRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_GRO,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL1_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setEmailRequestUsingRoleAndDepartment(emailRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER1,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL2_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setEmailRequestUsingRoleAndDepartment(emailRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER2,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL3_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				setEmailRequestUsingRoleAndDepartment(emailRequestsTobeSent,serviceReq.getTenantId(),PGRConstants.ROLE_ESCALATION_OFFICER3,null,serviceReq,actionInfo,requestInfo);
			}else if(WorkFlowConfigs.STATUS_ESCALATED_LEVEL4_PENDING.equalsIgnoreCase(serviceReq.getStatus().toString()))
			{
				
//				String department =  notificationService.getDepartmentFromServiceCode(serviceReq, requestInfo);
//				
//				if(!StringUtils.isEmpty(department))
//				{
					setEmailRequestUsingRoleAndDepartment(emailRequestsTobeSent,PGRConstants.TENANT_ID,PGRConstants.ROLE_ESCALATION_OFFICER4,null,serviceReq,actionInfo,requestInfo);
//				}else
//					log.info(" Department not found for the complaint with service code ",serviceReq.getServiceCode());				
				
			}
			
			
			
			
			
			
			
			return emailRequestsTobeSent;
		} catch (Exception e) {
			if(actionInfo.getStatus()==null)
			{
				log.error(" Status is empty , so unable get the Receptors of Notification.");
			}
			e.printStackTrace();
			return emailRequestsTobeSent;
		}
    }
    

    public String getMessageForSMS(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo, String role) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(notificationDateFormat);
        String date = dateFormat.format(new Date(serviceReq.getAuditDetails().getCreatedTime()));
        String tenantId = serviceReq.getTenantId().split("[.]")[0]; // localization values are for now state-level.
        String locale = null;
        try {
            locale = requestInfo.getMsgId().split("[|]")[1]; // Conventionally locale is sent in the first index of msgid split by |
            if (StringUtils.isEmpty(locale))
                locale = fallbackLocale;
        } catch (Exception e) {
            locale = fallbackLocale;
        }
        if (null == NotificationService.localizedMessageMap.get(locale + "|" + tenantId)) // static map that saves code-message pair against locale | tenantId.
            notificationService.getLocalisedMessages(requestInfo, tenantId, locale, PGRConstants.LOCALIZATION_MODULE_NAME);
        Map<String, String> messageMap = NotificationService.localizedMessageMap.get(locale + "|" + tenantId);
        if (null == messageMap)
            return null;
        List<Object> listOfValues = notificationService.getServiceType(serviceReq, requestInfo, locale);

        return getMessage(listOfValues, date, serviceReq, actionInfo, requestInfo, messageMap, role);

    }
    
    
    public String getMessageForEmail(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo, String role) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(notificationDateFormat);
        String date = dateFormat.format(new Date(serviceReq.getAuditDetails().getCreatedTime()));
        String tenantId = serviceReq.getTenantId().split("[.]")[0]; // localization values are for now state-level.
        String locale = null;
        try {
            locale = requestInfo.getMsgId().split("[|]")[1]; // Conventionally locale is sent in the first index of msgid split by |
            if (StringUtils.isEmpty(locale))
                locale = fallbackLocale;
        } catch (Exception e) {
            locale = fallbackLocale;
        }
        if (null == NotificationService.localizedMessageMap.get(locale + "|" + tenantId)) // static map that saves code-message pair against locale | tenantId.
            notificationService.getLocalisedMessages(requestInfo, tenantId, locale, PGRConstants.LOCALIZATION_MODULE_NAME);
        Map<String, String> messageMap = NotificationService.localizedMessageMap.get(locale + "|" + tenantId);
        if (null == messageMap)
            return null;
        List<Object> listOfValues = notificationService.getServiceType(serviceReq, requestInfo, locale);

        return getMessageForEmail(listOfValues, date, serviceReq, actionInfo, requestInfo, messageMap, role);

    }
    
    
    
    

    public String getMessage(List<Object> listOfValues, String date, Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo, Map<String, String> messageMap, String role) {
        if (null == listOfValues.get(0)) {
            return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
        }
        String text = null;
        String[] reasonForRejection = new String[2];
        Map<String, String> employeeDetails = null;
        String department = null;
        String designation = null;
        if (StringUtils.isEmpty(actionInfo.getStatus()) && !StringUtils.isEmpty(actionInfo.getComment())) {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_COMMENT);
            text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_COMMENT_KEY, actionInfo.getComment())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_USER_NAME_KEY, requestInfo.getUserInfo().getName());
        } else {
            text = messageMap.get(PGRConstants.getStatusRoleLocalizationKeyMap().get(actionInfo.getStatus() + "|" + role));
            if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL1_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL2_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL3_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL4_PENDING)) {
                if (null != actionInfo.getAction() && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_REOPEN)) {
                    text = messageMap.get(PGRConstants.getActionRoleLocalizationKeyMap().get(WorkFlowConfigs.ACTION_REOPEN + "|" + role));
                    employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actionInfo.getAssignee(), requestInfo);
               //     text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
                }
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ASSIGNED)) {
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actionInfo.getAssignee(), requestInfo);
                if (null != employeeDetails) {
                   //  List<String> deptCodes = new ArrayList<>();
                    //deptCodes.add(employeeDetails.get("department"));
                   // department = notificationService.getDepartmentForNotification(serviceReq, deptCodes, requestInfo);
                   //  if(!StringUtils.isEmpty(department))
                        department = notificationService.getDepartment(serviceReq, employeeDetails.get("department") , requestInfo);
                    	designation = notificationService.getDesignation(serviceReq, employeeDetails.get("designation"), requestInfo);
                } else {
                    return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                }
                if (StringUtils.isEmpty(department) || StringUtils.isEmpty(designation) || StringUtils.isEmpty(employeeDetails.get("name")))
                    return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());

                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"))
                		.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_DESIGNATION_KEY, designation)
                		.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_DEPT_KEY, department);
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_REJECTED)) {
                if (StringUtils.isEmpty(actionInfo.getComment()))
                    return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                reasonForRejection = actionInfo.getComment().split(";");
                if (reasonForRejection.length < 2)
                    return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_REASON_FOR_REOPEN_KEY, reasonForRejection[0])
                        .replaceAll(PGRConstants.SMS_NOTIFICATION_ADDITIONAL_COMMENT_KEY, reasonForRejection[1]);
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_RESOLVED)) {
                String assignee = notificationService.getCurrentAssigneeForTheServiceRequest(serviceReq, requestInfo);
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), assignee, requestInfo);

                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
                //if complaint is resolved by escalation level4 officer, then citizen cannot re-open it.
                //For this purpose we are trimming the last part of the message
                if(notificationService.isEscalatedToLevel4(serviceReq, requestInfo)) {
                	if(!ArrayUtils.isEmpty(text.split("\\.")))
                		text = text.split("\\.")[0]+".";
                }
                
            }
            if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_CLOSED)) {
                ServiceReqSearchCriteria serviceReqSearchCriteria = ServiceReqSearchCriteria.builder().tenantId(serviceReq.getTenantId())
                        .serviceRequestId(Arrays.asList(serviceReq.getServiceRequestId())).build();
                ServiceResponse response = (ServiceResponse) requestService.getServiceRequestDetails(requestInfo, serviceReqSearchCriteria);
                List<ActionInfo> actions = response.getActionHistory().get(0).getActions().stream()
                        .filter(obj -> !StringUtils.isEmpty(obj.getAssignee())).collect(Collectors.toList());
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actions.get(0).getAssignee(), requestInfo);
                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_RATING_KEY,
                        StringUtils.isEmpty(response.getServices().get(0).getRating()) ? "5" : response.getServices().get(0).getRating())
                        .replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
            }
        }
        if (null != text) {
            String ulb = null;
            if (StringUtils.isEmpty(serviceReq.getTenantId().split("[.]")[1]))
                ulb = "Odisha";
            else {
                ulb = StringUtils.capitalize(serviceReq.getTenantId().split("[.]")[1]);
            }
            return text.replaceAll(PGRConstants.SMS_NOTIFICATION_COMPLAINT_TYPE_KEY, listOfValues.get(0).toString())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_ID_KEY, serviceReq.getServiceRequestId())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_DATE_KEY, date)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_APP_LINK_KEY, uiAppHost + uiFeedbackUrl + serviceReq.getServiceRequestId())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_APP_DOWNLOAD_LINK_KEY, appDownloadLink)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_AO_DESIGNATION, PGRConstants.ROLE_NAME_GRO)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_ULB_NAME, ulb)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_SLA_NAME, listOfValues.get(1).toString());

        }
        return text;
    }
    
    
    public String getMessageForEmail(List<Object> listOfValues, String date, Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo, Map<String, String> messageMap, String role) {
        if (null == listOfValues.get(0)) {
            return getDefaultMessage(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
        }
        String text = null;
        String[] reasonForRejection = new String[2];
        Map<String, String> employeeDetails = null;
        String department = null;
        String designation = null;
        if (StringUtils.isEmpty(actionInfo.getStatus()) && !StringUtils.isEmpty(actionInfo.getComment())) {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_COMMENT_EMAIL);
            text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_COMMENT_KEY, actionInfo.getComment())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_USER_NAME_KEY, requestInfo.getUserInfo().getName());
        } else {
            text = messageMap.get(PGRConstants.getStatusRoleLocalizationKeyMapForEmail().get(actionInfo.getStatus() + "|" + role));
            if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL1_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL2_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL3_PENDING)
            		|| actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ESCALATED_LEVEL4_PENDING)) {
                if (null != actionInfo.getAction() && actionInfo.getAction().equals(WorkFlowConfigs.ACTION_REOPEN)) {
                    text = messageMap.get(PGRConstants.getActionRoleLocalizationKeyMapForEmail().get(WorkFlowConfigs.ACTION_REOPEN + "|" + role));
                    employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actionInfo.getAssignee(), requestInfo);
                  //  text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
                }
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_ASSIGNED)) {
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actionInfo.getAssignee(), requestInfo);
                if (null != employeeDetails) {
                   // List<String> deptCodes = new ArrayList<>();
                   //deptCodes.add(employeeDetails.get("department"));
                   //department = notificationService.getDepartmentForNotification(serviceReq, deptCodes, requestInfo);
                   // if(!StringUtils.isEmpty(department))
                    department = notificationService.getDepartment(serviceReq, employeeDetails.get("department") , requestInfo);
                    designation = notificationService.getDesignation(serviceReq, employeeDetails.get("designation"), requestInfo);
                } else {
                    return getDefaultMessageForEmail(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                }
                if (StringUtils.isEmpty(department) || StringUtils.isEmpty(designation) || StringUtils.isEmpty(employeeDetails.get("name")))
                    return getDefaultMessageForEmail(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());

                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"))
                		.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_DESIGNATION_KEY, designation)
                		.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_DEPT_KEY, department);
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_REJECTED)) {
                if (StringUtils.isEmpty(actionInfo.getComment()))
                    return getDefaultMessageForEmail(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                reasonForRejection = actionInfo.getComment().split(";");
                if (reasonForRejection.length < 2)
                    return getDefaultMessageForEmail(messageMap, actionInfo.getStatus(), actionInfo.getAction(), actionInfo.getComment());
                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_REASON_FOR_REOPEN_KEY, reasonForRejection[0])
                        .replaceAll(PGRConstants.SMS_NOTIFICATION_ADDITIONAL_COMMENT_KEY, reasonForRejection[1]);
            } else if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_RESOLVED)) {
                String assignee = notificationService.getCurrentAssigneeForTheServiceRequest(serviceReq, requestInfo);
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), assignee, requestInfo);

                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
                //if complaint is resolved by escalation level4 officer, then citizen cannot re-open it.
                //For this purpose we are trimming the last part of the message
                if(notificationService.isEscalatedToLevel4(serviceReq, requestInfo)) {
                	if(!ArrayUtils.isEmpty(text.split("\\.")))
                		text = text.split("\\.")[0]+".";
                }
                
            }
            if (actionInfo.getStatus().equals(WorkFlowConfigs.STATUS_CLOSED)) {
                ServiceReqSearchCriteria serviceReqSearchCriteria = ServiceReqSearchCriteria.builder().tenantId(serviceReq.getTenantId())
                        .serviceRequestId(Arrays.asList(serviceReq.getServiceRequestId())).build();
                ServiceResponse response = (ServiceResponse) requestService.getServiceRequestDetails(requestInfo, serviceReqSearchCriteria);
                List<ActionInfo> actions = response.getActionHistory().get(0).getActions().stream()
                        .filter(obj -> !StringUtils.isEmpty(obj.getAssignee())).collect(Collectors.toList());
                employeeDetails = notificationService.getEmployeeDetails(serviceReq.getTenantId(), actions.get(0).getAssignee(), requestInfo);
                text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_RATING_KEY,
                        StringUtils.isEmpty(response.getServices().get(0).getRating()) ? "5" : response.getServices().get(0).getRating())
                        .replaceAll(PGRConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeDetails.get("name"));
            }
        }
        if (null != text) {
            String ulb = null;
            if (StringUtils.isEmpty(serviceReq.getTenantId().split("[.]")[1]))
                ulb = "Odisha";
            else {
                ulb = StringUtils.capitalize(serviceReq.getTenantId().split("[.]")[1]);
            }
            return text.replaceAll(PGRConstants.SMS_NOTIFICATION_COMPLAINT_TYPE_KEY, listOfValues.get(0).toString())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_ID_KEY, serviceReq.getServiceRequestId())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_DATE_KEY, date)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_APP_LINK_KEY, uiAppHost + uiFeedbackUrl + serviceReq.getServiceRequestId())
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_APP_DOWNLOAD_LINK_KEY, appDownloadLink)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_AO_DESIGNATION, PGRConstants.ROLE_NAME_GRO)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_ULB_NAME, ulb)
                    .replaceAll(PGRConstants.SMS_NOTIFICATION_SLA_NAME, listOfValues.get(1).toString());

        }
        return text;
    }

    public String getDefaultMessage(Map<String, String> messageMap, String status, String action, String comment) {
        String text = null;
        if (StringUtils.isEmpty(status) && !StringUtils.isEmpty(comment)) {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_COMMENT_DEFAULT);
        } else {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_DEFAULT);
            text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_STATUS_KEY, PGRConstants.getStatusNotifKeyMap().get(status));
            if (status.equals(WorkFlowConfigs.STATUS_OPENED)) {
                if (null != action && action.equals(WorkFlowConfigs.ACTION_REOPEN))
                    text = text.replaceAll(PGRConstants.getStatusNotifKeyMap().get(status), PGRConstants.getActionNotifKeyMap().get(WorkFlowConfigs.ACTION_REOPEN));
            } else if (status.equals(WorkFlowConfigs.STATUS_ASSIGNED)) {
                if (null != action && action.equals(WorkFlowConfigs.ACTION_REASSIGN))
                    text = text.replaceAll(PGRConstants.getStatusNotifKeyMap().get(status), PGRConstants.getActionNotifKeyMap().get(WorkFlowConfigs.ACTION_REASSIGN));
            }
        }

        return text;
    }
    
    
    public String getDefaultMessageForEmail(Map<String, String> messageMap, String status, String action, String comment) {
        String text = null;
        if (StringUtils.isEmpty(status) && !StringUtils.isEmpty(comment)) {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_COMMENT_DEFAULT_EMAIL);
        } else {
            text = messageMap.get(PGRConstants.LOCALIZATION_CODE_DEFAULT_EMAIL);
            text = text.replaceAll(PGRConstants.SMS_NOTIFICATION_STATUS_KEY, PGRConstants.getStatusNotifKeyMap().get(status));
            if (status.equals(WorkFlowConfigs.STATUS_OPENED)) {
                if (null != action && action.equals(WorkFlowConfigs.ACTION_REOPEN))
                    text = text.replaceAll(PGRConstants.getStatusNotifKeyMap().get(status), PGRConstants.getActionNotifKeyMap().get(WorkFlowConfigs.ACTION_REOPEN));
            } else if (status.equals(WorkFlowConfigs.STATUS_ASSIGNED)) {
                if (null != action && action.equals(WorkFlowConfigs.ACTION_REASSIGN))
                    text = text.replaceAll(PGRConstants.getStatusNotifKeyMap().get(status), PGRConstants.getActionNotifKeyMap().get(WorkFlowConfigs.ACTION_REASSIGN));
            }
        }

        return text;
    }

    public boolean isNotificationEnabled(String status, String userType, String comment, String action) {
        boolean isNotifEnabled = true;
        if ((null != comment && !comment.isEmpty()) && !isCommentByEmpNotifEnaled && userType.equalsIgnoreCase("EMPLOYEE")) {
            isNotifEnabled = false;
        }
        return isNotifEnabled;
    }
    
	/**
	 * 
	 * @param smsRequets
	 * @param role
	 * @param department
	 * @param serviceReq
	 * @param actionInfo
	 * @param requestInfo
	 * @return
	 */
	public List<SMSRequest> setSMSRequestUsingRoleAndDepartment(List<SMSRequest> smsRequets ,String tenantId , String role ,String department ,Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo)
	{


		List<Map<String, String>> employeesList = null;
		try {
			employeesList = notificationService.getEmployeeDetailsOnDepartmentRoleBased(tenantId,department,role,requestInfo);
		} catch (Exception e1) {
			log.error("  Error in fetching the employee details with name   ");
			e1.printStackTrace();
		}

		for (Map<String, String> employee : employeesList) {
			try {
				String phone = employee.get("phone");
				if (StringUtils.isEmpty(phone))
				{
					log.info("  No mobile number found for the employee with name   {}",employee.get("name"));
					continue;
				}
				//Escalated level4 employee can get notifications only if the complaints are assigned to him
				if(role.equalsIgnoreCase(PGRConstants.ROLE_ESCALATION_OFFICER4))
				{
					String employeeUuid = employee.get("uuid");;
					List<String> applicableServiceCodes = pGRUtils.getApplicableServiceCodes(requestInfo,employeeUuid);
					
					if(applicableServiceCodes!=null && !applicableServiceCodes.isEmpty() )
					{
						List<String> applicableServiceCodesList = new ArrayList<String>(Arrays.asList(applicableServiceCodes.get(0).split(",")));
						if(!applicableServiceCodesList.contains(serviceReq.getServiceCode()))
							continue ;
					}else
						continue;
				}
				
				if(role.equalsIgnoreCase(PGRConstants.ROLE_ESCALATION_OFFICER1))
				{

					String roleCodes = employee.get("roleCodes").replace("[", "").replace("]", "");
					
					List<String> rolesList = new ArrayList<String>(Arrays.asList(roleCodes.split(",")));
					
					if(rolesList.contains(PGRConstants.ROLE_ESCALATION_OFFICER_BUILDING_PLAN_ASSIGNEE))
					{
						List<String>  menuPathList = new ArrayList<>();
						menuPathList.add("BuildingPermission");
						
						List<String> applicableServiceCodes = pGRUtils.getBuildingPermissionServiceCodes(tenantId, "menuPath", menuPathList.toString(), requestInfo) ;
						
						if(applicableServiceCodes!=null)
						{
							if(!applicableServiceCodes.contains(serviceReq.getServiceCode()))
								continue ;
						}
						
					}
				}
				
				
				String message = getMessageForSMS(serviceReq, actionInfo, requestInfo, PGRConstants.ROLE_EMPLOYEE);
				if (StringUtils.isEmpty(message))
					continue;
				smsRequets.add(SMSRequest.builder().mobileNumber(phone).message(message).build());
			} catch (Exception e) {
				log.error("  Error in fetching the employee details with name   {}",employee.get("name"));
				e.printStackTrace();
			}
		}

		return smsRequets;


	}
	
	
	/**
	 * 
	 * @param emailRequestsTobeSent
	 * @param role
	 * @param department
	 * @param serviceReq
	 * @param actionInfo
	 * @param requestInfo
	 * @return
	 */
	public List<EmailRequest> setEmailRequestUsingRoleAndDepartment(List<EmailRequest> emailRequestsTobeSent ,String tenantId , String role ,String department ,Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo)
	{


		
		String emailSubjectForEmail = emailSubject ;


		
		List<Map<String, String>>  employeesList =  notificationService.getEmployeeDetailsOnDepartmentRoleBased(tenantId,department,role,requestInfo);

		for (Map<String, String> employee : employeesList) {
			try {
				String emailId = employee.get("emailId");
				if (StringUtils.isEmpty(emailId))
				{
					log.info("  No email Id found for the employee with name  {} ",employee.get("name"));
					continue;
				}
				
				//Escalated level4 employee can get notifications only if the complaints are assigned to him
				if(role.equalsIgnoreCase(PGRConstants.ROLE_ESCALATION_OFFICER4))
				{
					String employeeUuid = employee.get("uuid");;
					List<String> applicableServiceCodes = pGRUtils.getApplicableServiceCodes(requestInfo,employeeUuid);
					
					if(applicableServiceCodes!=null && !applicableServiceCodes.isEmpty() )
					{
						List<String> applicableServiceCodesList = new ArrayList<String>(Arrays.asList(applicableServiceCodes.get(0).split(",")));
						if(!applicableServiceCodesList.contains(serviceReq.getServiceCode()))
							continue ;
					}else
						continue;
				}
				
				if(role.equalsIgnoreCase(PGRConstants.ROLE_ESCALATION_OFFICER1))
				{

					String roleCodes = employee.get("roleCodes").replace("[", "").replace("]", "");
					
					List<String> rolesList = new ArrayList<String>(Arrays.asList(roleCodes.split(",")));
					
					if(rolesList.contains(PGRConstants.ROLE_ESCALATION_OFFICER_BUILDING_PLAN_ASSIGNEE))
					{
						List<String>  menuPathList = new ArrayList<>();
						menuPathList.add("BuildingPermission");
						
						List<String> applicableServiceCodes = pGRUtils.getBuildingPermissionServiceCodes(tenantId, "menuPath", menuPathList.toString(), requestInfo) ;
						
						if(applicableServiceCodes!=null)
						{
							if(!applicableServiceCodes.contains(serviceReq.getServiceCode()))
								continue ;
						}
						
					}
				}
				
				String message = getMessageForEmail(serviceReq, actionInfo, requestInfo, PGRConstants.ROLE_EMPLOYEE);
			    if (StringUtils.isEmpty(message))
			        continue;
			    
			    Set<String> emailTo = new LinkedHashSet<String>();
			    emailTo.add(emailId);
			    
			    emailSubjectForEmail = emailSubjectForEmail.replaceAll(PGRConstants.EMAIL_SUBJECT_ID_KEY, serviceReq.getServiceRequestId());
			    
			    emailRequestsTobeSent.add(EmailRequest.builder().requestInfo(requestInfo).email(new  Email(emailTo, emailSubjectForEmail, message, true)).build());
			} catch (Exception e) {
				log.error("  Error in fetching the employee details with name   ",employee.get("name"));
				e.printStackTrace();
			}
		}

		return emailRequestsTobeSent;


	}
    
    
}