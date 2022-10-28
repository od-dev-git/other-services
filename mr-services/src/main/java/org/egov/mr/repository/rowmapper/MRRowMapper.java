package org.egov.mr.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.mr.web.models.AuditDetails;
import org.egov.mr.web.models.Boundary;
import org.egov.mr.web.models.Couple;
import org.egov.mr.web.models.CoupleDetails;
import org.egov.mr.web.models.Address;
import org.egov.mr.web.models.AppointmentDetails;
import org.egov.mr.web.models.Document;
import org.egov.mr.web.models.DscDetails;
import org.egov.mr.web.models.GuardianDetails;
import org.egov.mr.web.models.MarriagePlace;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.Witness;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class MRRowMapper  implements ResultSetExtractor<List<MarriageRegistration>> {


	@Autowired
	private ObjectMapper mapper;



	public List<MarriageRegistration> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, MarriageRegistration> marriageRegistrationMap = new LinkedHashMap<>();
		Map<String, CoupleDetails> coupleMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("mr_originalId");
			log.info("mapper receive id"+id);
			MarriageRegistration currentMarriageRegistration = marriageRegistrationMap.get(id);
			String tenantId = rs.getString("mr_tenantId");

			if(currentMarriageRegistration == null){
				Long lastModifiedTime = rs.getLong("mr_lastModifiedTime");
				if(rs.wasNull()){lastModifiedTime = null;}

				Long commencementDate = (Long) rs.getObject("marriageDate");
				Long issuedDate = (Long) rs.getObject("issueddate");
				Long applicationDate = (Long) rs.getObject("applicationdate");

				AuditDetails auditdetails = AuditDetails.builder()
						.createdBy(rs.getString("mr_createdBy"))
						.createdTime(rs.getLong("mr_createdTime"))
						.lastModifiedBy(rs.getString("mr_lastModifiedBy"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				currentMarriageRegistration = MarriageRegistration.builder().auditDetails(auditdetails)
						.workflowCode(rs.getString("workflowcode"))
						.applicationDate(applicationDate)
						.applicationNumber(rs.getString("mr_applicationnumber"))
						.applicationType(MarriageRegistration.ApplicationTypeEnum.fromValue(rs.getString( "applicationType")))
						.mrNumber(rs.getString("mrnumber"))
						.marriageDate(commencementDate)
						.issuedDate(issuedDate)
						.accountId(rs.getString("accountid"))
						.action(rs.getString("action"))
						.status(rs.getString("status"))
						.tenantId(tenantId)
						.businessService(rs.getString("businessservice"))
						.id(id)
						.build();

				marriageRegistrationMap.put(id,currentMarriageRegistration);
			}


			if(rs.getString("mrc_id")!=null && rs.getBoolean("isgroom")) {
				log.info("Groom details present");
				Address coupleAddress = null ;

				if(rs.getString("mrca_id")!=null )
				{
					coupleAddress = Address.builder()
							.id(rs.getString("mrca_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.addressLine1(rs.getString("mrca_addressLine1"))
							.country(rs.getString("mrca_country"))
							.contact(rs.getString("mrca_contact"))
							.emailAddress(rs.getString("mrca_emailaddress"))
							.state(rs.getString("mrca_state"))
							.district(rs.getString("mrca_district"))
							.pinCode(rs.getString("mrca_pincode"))
							.build();
				}


				Witness witness = null ;

				if(rs.getString("mrw_id")!=null ) {

					witness = Witness.builder()
							.id(rs.getString("mrw_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.title(rs.getString("mrw_title"))
							.address(rs.getString("address"))
							.firstName(rs.getString("mrw_firstName"))
							.country(rs.getString("mrw_country"))
							.state(rs.getString("mrw_state"))
							.district(rs.getString("mrw_district"))
							.pinCode(rs.getString("mrw_pincode"))
							.contact(rs.getString("mrw_contact"))
							.build();

				}



				GuardianDetails guardianDetails = null ;

				if(rs.getString("mrgd_id")!=null )
				{
					guardianDetails = GuardianDetails.builder()
							.id(rs.getString("mrgd_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.addressLine1(rs.getString("mrgd_addressLine1"))
							.country(rs.getString("mrgd_country"))
							.state(rs.getString("mrgd_state"))
							.district(rs.getString("mrgd_district"))
							.pinCode(rs.getString("mrgd_pincode"))
							.relationship(rs.getString("relationship"))
							.relationshipDesc(rs.getString("relationshipdesc"))
							.name(rs.getString("name"))
							.contact(rs.getString("mrgd_contact"))
							.emailAddress(rs.getString("mrgd_emailaddress"))
							.build();

				}



				CoupleDetails groom = CoupleDetails.builder()
						.id(rs.getString("mrc_id"))
						.isDivyang(rs.getBoolean("isdivyang"))
						.isGroom(rs.getBoolean("isgroom"))
						.title(rs.getString("mrc_title"))
						.firstName(rs.getString("mrc_firstName"))
						.dateOfBirth(rs.getLong("dateofbirth"))
						.fatherName(rs.getString("fathername"))
						.motherName(rs.getString("mothername"))
						.build();

				if(coupleAddress !=  null)
				{
					groom.setAddress(coupleAddress);
				}

				if(guardianDetails !=  null)
				{
					groom.setGuardianDetails(guardianDetails);
				}

				if(witness !=  null)
				{
					groom.setWitness(witness);
				}

				if(coupleMap.get(rs.getString("mrc_id")) == null)
				{
					coupleMap.put(rs.getString("mrc_id"), groom);

					currentMarriageRegistration.addCoupleDetailsItem(groom);
				}
			}

			if(rs.getString("mrc_id")!=null && !rs.getBoolean("isgroom")) {
				log.info("Bride details present");
				Address coupleAddress = null ;

				if(rs.getString("mrca_id")!=null )
				{
					coupleAddress = Address.builder()
							.id(rs.getString("mrca_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.addressLine1(rs.getString("mrca_addressLine1"))
							.country(rs.getString("mrca_country"))
							.contact(rs.getString("mrca_contact"))
							.emailAddress(rs.getString("mrca_emailaddress"))
							.state(rs.getString("mrca_state"))
							.district(rs.getString("mrca_district"))
							.pinCode(rs.getString("mrca_pincode"))
							.build();
				}


				Witness witness = null ;

				if(rs.getString("mrw_id")!=null ) {

					witness = Witness.builder()
							.id(rs.getString("mrw_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.title(rs.getString("mrw_title"))
							.address(rs.getString("address"))
							.firstName(rs.getString("mrw_firstName"))
							.country(rs.getString("mrw_country"))
							.state(rs.getString("mrw_state"))
							.district(rs.getString("mrw_district"))
							.pinCode(rs.getString("mrw_pincode"))
							.contact(rs.getString("mrw_contact"))
							.build();

				}



				GuardianDetails guardianDetails = null ;

				if(rs.getString("mrgd_id")!=null )
				{
					guardianDetails = GuardianDetails.builder()
							.id(rs.getString("mrgd_id"))
							.tenantId(rs.getString("mr_tenantId"))
							.addressLine1(rs.getString("mrgd_addressLine1"))
							.country(rs.getString("mrgd_country"))
							.state(rs.getString("mrgd_state"))
							.district(rs.getString("mrgd_district"))
							.pinCode(rs.getString("mrgd_pincode"))
							.relationship(rs.getString("relationship"))
							.relationshipDesc(rs.getString("relationshipdesc"))
							.name(rs.getString("name"))
							.contact(rs.getString("mrgd_contact"))
							.emailAddress(rs.getString("mrgd_emailaddress"))
							.build();

				}



				CoupleDetails bride = CoupleDetails.builder()
						.id(rs.getString("mrc_id"))
						.isDivyang(rs.getBoolean("isdivyang"))
						.isGroom(rs.getBoolean("isgroom"))
						.title(rs.getString("mrc_title"))
						.firstName(rs.getString("mrc_firstName"))
						.dateOfBirth(rs.getLong("dateofbirth"))
						.fatherName(rs.getString("fathername"))
						.motherName(rs.getString("mothername"))
						.build();

				if(coupleAddress !=  null)
				{
					bride.setAddress(coupleAddress);
				}

				if(guardianDetails !=  null)
				{
					bride.setGuardianDetails(guardianDetails);
				}

				if(witness !=  null)
				{
					bride.setWitness(witness);
				}

				if(coupleMap.get(rs.getString("mrc_id")) == null)
				{
					coupleMap.put(rs.getString("mrc_id"), bride);

					currentMarriageRegistration.addCoupleDetailsItem(bride);
				}
			}




			if(rs.getString("mr_ap_doc_id")!=null && rs.getBoolean("mr_ap_doc_active")) {
				Document applicationDocument = Document.builder()
						.documentType(rs.getString("mr_ap_doc_documenttype"))
						.fileStoreId(rs.getString("mr_ap_doc_filestoreid"))
						.id(rs.getString("mr_ap_doc_id"))
						.tenantId(tenantId)
						.active(rs.getBoolean("mr_ap_doc_active"))
						.build();
				currentMarriageRegistration.addApplicationDocumentsItem(applicationDocument);
			}
			
			if(rs.getString("mr_apt_dtl_id")!=null && rs.getBoolean("mr_apt_dtl_active")) {
				log.info("Appointment details present");
				try {
					Long startTime = (Long) rs.getObject("mr_apt_dtl_startTime");
					Long endTime = (Long) rs.getObject("mr_apt_dtl_endTime");
					
					PGobject pgObj = (PGobject) rs.getObject("mr_apt_dtl_additionalDetail");

					
					
					AppointmentDetails applicationDocument = AppointmentDetails.builder()
							.startTime(startTime)
							.endTime(endTime)
							.id(rs.getString("mr_apt_dtl_id"))
							.description(rs.getString("mr_apt_dtl_description"))
							.tenantId(tenantId)
							.active(rs.getBoolean("mr_apt_dtl_active"))
							.build();
					
					if(pgObj!=null){
						JsonNode additionalDetail = mapper.readTree(pgObj.getValue());
						applicationDocument.setAdditionalDetail(additionalDetail);
					}
					
					currentMarriageRegistration.addAppointmentDetailsItem(applicationDocument);
				} catch (JsonProcessingException e) {
					throw new CustomException("PARSING ERROR","The additionalDetail json cannot be parsed");
				} 
			}

			if(rs.getString("mr_ver_doc_id")!=null && rs.getBoolean("mr_ver_doc_active")) {
				log.info("Document present");
				Document verificationDocument = Document.builder()
						.documentType(rs.getString("mr_ver_doc_documenttype"))
						.fileStoreId(rs.getString("mr_ver_doc_filestoreid"))
						.id(rs.getString("mr_ver_doc_id"))
						.tenantId(tenantId)
						.active(rs.getBoolean("mr_ver_doc_active"))
						.build();
				currentMarriageRegistration.addVerificationDocumentsItem(verificationDocument);
			}

			if(rs.getString("mr_dsc_details_id")!=null) {
				log.info("DSC details present");
	        	DscDetails dscDetail = DscDetails.builder()
	                    .documentType(rs.getString("mr_dsc_details_documenttype"))
	                    .documentId(rs.getString("mr_dsc_details_documentid"))
	                    .applicationNumber(rs.getString("mr_dsc_details_applicationnumber"))
	                    .id(rs.getString("mr_dsc_details_id"))
	                    .tenantId(tenantId)
	                    .approvedBy(rs.getString("approvedby"))
	                    .build();

	        	try {
	        		PGobject pgObj  = (PGobject) rs.getObject("mr_dsc_details_additionaldetail");

	        		if(pgObj!=null){
	        			JsonNode additionalDetail = mapper.readTree(pgObj.getValue());
	        			dscDetail.setAdditionalDetail(additionalDetail);
	        		}
	        	} catch (Exception e) {
					throw new CustomException("PARSING ERROR","The DSC Details additionalDetail json cannot be parsed");
				}


	        	currentMarriageRegistration.addDscDetailsItem(dscDetail);
	        }




			addChildrenToProperty(rs, currentMarriageRegistration);

		}
		log.info("Mapper returned: "+marriageRegistrationMap.values().size());
		return new ArrayList<>(marriageRegistrationMap.values());

	}



	private void addChildrenToProperty(ResultSet rs, MarriageRegistration marriageRegistration) throws SQLException {

		String marriagePlaceId = rs.getString("mrp_id");

		if(marriageRegistration.getMarriagePlace()==null){



			Boundary locality = Boundary.builder().code(rs.getString("mrp_locality"))
					.build();

			PGobject pgObj = (PGobject) rs.getObject("mrp_additionalDetail");

			AuditDetails auditdetails = AuditDetails.builder()
					.createdBy(rs.getString("mr_createdBy"))
					.createdTime(rs.getLong("mr_createdTime"))
					.lastModifiedBy(rs.getString("mr_lastModifiedBy"))
					.lastModifiedTime(rs.getLong("mr_lastModifiedTime"))
					.build();

			try {
				MarriagePlace marriagePalce = MarriagePlace.builder().locality(locality)
						.id(marriagePlaceId)
						.auditDetails(auditdetails)
						.ward(rs.getString("ward"))
						.pinCode(rs.getString("mrp_pincode"))
						.placeOfMarriage(rs.getString("placeofmarriage"))
						.build();


				if(pgObj!=null){
					JsonNode additionalDetail = mapper.readTree(pgObj.getValue());
					marriagePalce.setAdditionalDetail(additionalDetail);
				}

				marriageRegistration.setMarriagePlace(marriagePalce);
			}
			catch (IOException e){
				throw new CustomException("PARSING ERROR","The additionalDetail json cannot be parsed");

			}



		}


	}

}
