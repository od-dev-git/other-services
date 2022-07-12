package org.egov.mrcalculator.service;

import static org.egov.mrcalculator.utils.MRCalculatorConstants.BILLINGSLAB_KEY;
import static org.egov.mrcalculator.utils.MRCalculatorConstants.MDMS_ROUNDOFF_TAXHEAD;
import static org.egov.mrcalculator.utils.MRCalculatorConstants.businessService_MR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.mr.model.user.Citizen;
import org.egov.mr.model.user.UserResponse;
import org.egov.mr.model.user.UserSearchRequest;
import org.egov.mr.repository.ServiceRequestRepository;
import org.egov.mr.util.MRConstants;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.calculation.TaxHeadEstimate;
import org.egov.mrcalculator.config.MRCalculatorConfigs;
import org.egov.mrcalculator.repository.CalculationRepository;
import org.egov.mrcalculator.repository.DemandRepository;
import org.egov.mrcalculator.repository.builder.CalculationQueryBuilder;
import org.egov.mrcalculator.utils.CalculationUtils;
import org.egov.mrcalculator.web.models.BillAndCalculations;
import org.egov.mrcalculator.web.models.BillingSlabIds;
import org.egov.mrcalculator.web.models.Calculation;
import org.egov.mrcalculator.web.models.CalculationSearchCriteria;
import org.egov.mrcalculator.web.models.RequestInfoWrapper;
import org.egov.mrcalculator.web.models.demand.BillResponse;
import org.egov.mrcalculator.web.models.demand.Demand;
import org.egov.mrcalculator.web.models.demand.DemandDetail;
import org.egov.mrcalculator.web.models.demand.DemandResponse;
import org.egov.mrcalculator.web.models.demand.GenerateBillCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class DemandService {


    @Autowired
    private CalculationUtils utils;

    @Autowired
    private MRCalculatorConfigs config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DemandRepository demandRepository;


    @Autowired
    private CalculationRepository calculationRepository;

    @Autowired
    private CalculationQueryBuilder calculationQueryBuilder;
    



    /**
     * Creates or updates Demand
     * @param requestInfo The RequestInfo of the calculation request
     * @param calculations The Calculation Objects for which demand has to be generated or updated
     */
    public void generateDemand(RequestInfo requestInfo,List<Calculation> calculations,String businessService){

        //List that will contain Calculation for new demands
        List<Calculation> createCalculations = new LinkedList<>();

        //List that will contain Calculation for old demands
        List<Calculation> updateCalculations = new LinkedList<>();

        if(!CollectionUtils.isEmpty(calculations)){

            //Collect required parameters for demand search
            String tenantId = calculations.get(0).getTenantId();
            Set<String> applicationNumbers = calculations.stream().map(calculation -> calculation.getMarriageRegistration().getApplicationNumber()).collect(Collectors.toSet());
            List<Demand> demands = searchDemand(tenantId,applicationNumbers,requestInfo,businessService);
            Set<String> applicationNumbersFromDemands = new HashSet<>();
            if(!CollectionUtils.isEmpty(demands))
                applicationNumbersFromDemands = demands.stream().map(Demand::getConsumerCode).collect(Collectors.toSet());

            //If demand already exists add it updateCalculations else createCalculations
            for(Calculation calculation : calculations)
            {      if(!applicationNumbersFromDemands.contains(calculation.getMarriageRegistration().getApplicationNumber()))
                        createCalculations.add(calculation);
                    else
                        updateCalculations.add(calculation);
            }
        }

        if(!CollectionUtils.isEmpty(createCalculations))
            createDemand(requestInfo,createCalculations);

        if(!CollectionUtils.isEmpty(updateCalculations))
            updateDemand(requestInfo,updateCalculations,businessService);
    }


    /**
     * Generates bill
     * @param requestInfo The RequestInfo of the calculation request
     * @param billCriteria The criteria for bill generation
     * @return The generate bill response along with ids of slab used for calculation
     */
    public BillAndCalculations getBill(RequestInfo requestInfo, GenerateBillCriteria billCriteria, String serviceFromPath){
        BillResponse billResponse = generateBill(requestInfo,billCriteria,serviceFromPath);
        BillingSlabIds billingSlabIds = getBillingSlabIds(billCriteria);
        BillAndCalculations getBillResponse = new BillAndCalculations();
        getBillResponse.setBillingSlabIds(billingSlabIds);
        getBillResponse.setBillResponse(billResponse);
        return getBillResponse;
    }


    /**
     * Gets the billingSlabs from the db
     * @param billCriteria The criteria on which bill has to be generated
     * @return The billingSlabIds used for calculation
     */
    private BillingSlabIds getBillingSlabIds(GenerateBillCriteria billCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        CalculationSearchCriteria criteria = new CalculationSearchCriteria();
        criteria.setTenantId(billCriteria.getTenantId());
        criteria.setAplicationNumber(billCriteria.getConsumerCode());

        String query = calculationQueryBuilder.getSearchQuery(criteria,preparedStmtList);
        return calculationRepository.getDataFromDB(query,preparedStmtList);
    }


    /**
     * Creates demand for the given list of calculations
     * @param requestInfo The RequestInfo of the calculation request
     * @param calculations List of calculation object
     * @return Demands that are created
     */
    private List<Demand> createDemand(RequestInfo requestInfo,List<Calculation> calculations){
        List<Demand> demands = new LinkedList<>();
        for(Calculation calculation : calculations) {
        	MarriageRegistration marriageRegistration = null;

            if(calculation.getMarriageRegistration()!=null)
                marriageRegistration = calculation.getMarriageRegistration();

            else if(calculation.getApplicationNumber()!=null)
                marriageRegistration = utils.getMarriageRegistration(requestInfo, calculation.getApplicationNumber()
                        , calculation.getTenantId());


            if (marriageRegistration == null)
                throw new CustomException("INVALID APPLICATIONNUMBER", "Demand cannot be generated for applicationNumber " +
                        calculation.getApplicationNumber() + " Marriage Registration with this number does not exist ");

            String tenantId = calculation.getTenantId();
            String consumerCode = calculation.getMarriageRegistration().getApplicationNumber();

            
            User owner = getUser(calculation.getMarriageRegistration().getAccountId(), requestInfo, tenantId);

            List<DemandDetail> demandDetails = new LinkedList<>();

            calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
                demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
                        .taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode())
                        .collectionAmount(BigDecimal.ZERO)
                        .tenantId(tenantId)
                        .build());
            });
            Long taxPeriodFrom = System.currentTimeMillis();
            Long taxPeriodTo = System.currentTimeMillis();
            String businessService = marriageRegistration.getBusinessService();
            if (businessService == null)
                businessService = businessService_MR;
            switch (businessService) {
            
            //TLR Changes
                case businessService_MR:
                    

                    	taxPeriodFrom = 347241599000L;
                        taxPeriodTo = 3493930939000L;
                        break;
                    
            }
            addRoundOffTaxHead(calculation.getTenantId(), demandDetails);
            List<String> combinedBillingSlabs = new LinkedList<>();
            if (calculation.getBillingIds() != null && !CollectionUtils.isEmpty(calculation.getBillingIds().getBillingSlabIds()))
                combinedBillingSlabs.addAll(calculation.getBillingIds().getBillingSlabIds());

            Demand singleDemand = Demand.builder()
                    .consumerCode(consumerCode)
                    .demandDetails(demandDetails)
                    .payer(owner)
                    .minimumAmountPayable(config.getMinimumPayableAmount())
                    .tenantId(tenantId)
                    .taxPeriodFrom(taxPeriodFrom)
                    .taxPeriodTo(taxPeriodTo)
                    .consumerType("marriageregistration")
                    .businessService(config.getBusinessServiceMR())
                    .additionalDetails(Collections.singletonMap(BILLINGSLAB_KEY, combinedBillingSlabs))
                    .billExpiryTime(2629800000L)
                    .build();

            demands.add(singleDemand);
            }
            
        
        return demandRepository.saveDemand(requestInfo,demands);
    }



    /**
     * Updates demand for the given list of calculations
     * @param requestInfo The RequestInfo of the calculation request
     * @param calculations List of calculation object
     * @return Demands that are updated
     */
    private List<Demand> updateDemand(RequestInfo requestInfo,List<Calculation> calculations,String businessService){
        List<Demand> demands = new LinkedList<>();
        for(Calculation calculation : calculations) {

            List<Demand> searchResult = searchDemand(calculation.getTenantId(),Collections.singleton(calculation.getMarriageRegistration().getApplicationNumber())
                    , requestInfo,businessService);

            if(CollectionUtils.isEmpty(searchResult))
                throw new CustomException("INVALID UPDATE","No demand exists for applicationNumber: "+calculation.getMarriageRegistration().getApplicationNumber());

            Demand demand = searchResult.get(0);
            List<DemandDetail> demandDetails = demand.getDemandDetails();
            List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(calculation,demandDetails);
            demand.setDemandDetails(updatedDemandDetails);
            demands.add(demand);
        }
         return demandRepository.updateDemand(requestInfo,demands);
    }


    /**
     * Searches demand for the given consumerCode and tenantIDd
     * @param tenantId The tenantId of the marriageRegistration
     * @param consumerCodes The set of consumerCode of the demands
     * @param requestInfo The RequestInfo of the incoming request
     * @return Lis to demands for the given consumerCode
     */
    private List<Demand> searchDemand(String tenantId,Set<String> consumerCodes,RequestInfo requestInfo, String businessService){
        String uri = utils.getDemandSearchURL();
        uri = uri.replace("{1}",tenantId);
        uri = uri.replace("{2}",businessService);
        uri = uri.replace("{3}",StringUtils.join(consumerCodes, ','));

        Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),RequestInfoWrapper.builder()
                                                      .requestInfo(requestInfo).build());

        DemandResponse response;
        try {
             response = mapper.convertValue(result,DemandResponse.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response from Demand Search");
        }

        if(CollectionUtils.isEmpty(response.getDemands()))
            return null;

        else return response.getDemands();

    }


    /**
     * Generates bill by calling BillingService
     * @param requestInfo The RequestInfo of the getBill request
     * @param billCriteria The criteria for bill generation
     * @return The response of the bill generate
     */
    private BillResponse generateBill(RequestInfo requestInfo,GenerateBillCriteria billCriteria,String businessServiceFromPath){

        String consumerCode = billCriteria.getConsumerCode();
        String tenantId = billCriteria.getTenantId();

        List<Demand> demands = searchDemand(tenantId,Collections.singleton(consumerCode),requestInfo,billCriteria.getBusinessService());

        if(!StringUtils.equals(businessServiceFromPath,billCriteria.getBusinessService()))
            throw new CustomException("BUSINESSSERVICE_MISMATCH","Business Service in Path variable and bill criteria are different");

        if(CollectionUtils.isEmpty(demands))
            throw new CustomException("INVALID CONSUMERCODE","Bill cannot be generated.No demand exists for the given consumerCode");

        String uri = utils.getBillGenerateURI();
        uri = uri.replace("{1}",billCriteria.getTenantId());
        uri = uri.replace("{2}",billCriteria.getConsumerCode());
        uri = uri.replace("{3}",billCriteria.getBusinessService());

        Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),RequestInfoWrapper.builder()
                                                             .requestInfo(requestInfo).build());
        BillResponse response;
         try{
              response = mapper.convertValue(result,BillResponse.class);
         }
         catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Unable to parse response of generate bill");
         }
         return response;
    }


    /**
     * Returns the list of new DemandDetail to be added for updating the demand
     * @param calculation The calculation object for the update tequest
     * @param demandDetails The list of demandDetails from the existing demand
     * @return The list of new DemandDetails
     */
    private List<DemandDetail> getUpdatedDemandDetails(Calculation calculation, List<DemandDetail> demandDetails){

        List<DemandDetail> newDemandDetails = new ArrayList<>();
        Map<String, List<DemandDetail>> taxHeadToDemandDetail = new HashMap<>();

        demandDetails.forEach(demandDetail -> {
            if(!taxHeadToDemandDetail.containsKey(demandDetail.getTaxHeadMasterCode())){
                List<DemandDetail> demandDetailList = new LinkedList<>();
                demandDetailList.add(demandDetail);
                taxHeadToDemandDetail.put(demandDetail.getTaxHeadMasterCode(),demandDetailList);
            }
            else
              taxHeadToDemandDetail.get(demandDetail.getTaxHeadMasterCode()).add(demandDetail);
        });

        BigDecimal diffInTaxAmount;
        List<DemandDetail> demandDetailList;
        BigDecimal total;

        for(TaxHeadEstimate taxHeadEstimate : calculation.getTaxHeadEstimates()){
            if(!taxHeadToDemandDetail.containsKey(taxHeadEstimate.getTaxHeadCode()))
                newDemandDetails.add(
                        DemandDetail.builder()
                                .taxAmount(taxHeadEstimate.getEstimateAmount())
                                .taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode())
                                .tenantId(calculation.getTenantId())
                                .collectionAmount(BigDecimal.ZERO)
                                .build());
            else {
                 demandDetailList = taxHeadToDemandDetail.get(taxHeadEstimate.getTaxHeadCode());
                 total = demandDetailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                 diffInTaxAmount = taxHeadEstimate.getEstimateAmount().subtract(total);
                 if(diffInTaxAmount.compareTo(BigDecimal.ZERO)!=0) {
                     newDemandDetails.add(
                             DemandDetail.builder()
                                     .taxAmount(diffInTaxAmount)
                                     .taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode())
                                     .tenantId(calculation.getTenantId())
                                     .collectionAmount(BigDecimal.ZERO)
                                     .build());
                 }
            }
        }
        List<DemandDetail> combinedBillDetials = new LinkedList<>(demandDetails);
        combinedBillDetials.addAll(newDemandDetails);
        addRoundOffTaxHead(calculation.getTenantId(),combinedBillDetials);
        return combinedBillDetials;
    }



    /**
     * Adds roundOff taxHead if decimal values exists
     * @param tenantId The tenantId of the demand
     * @param demandDetails The list of demandDetail
     */
    private void addRoundOffTaxHead(String tenantId,List<DemandDetail> demandDetails){
        BigDecimal totalTax = BigDecimal.ZERO;

        DemandDetail prevRoundOffDemandDetail = null;

        /*
        * Sum all taxHeads except RoundOff as new roundOff will be calculated
        * */
        for (DemandDetail demandDetail : demandDetails){
            if(!demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(MDMS_ROUNDOFF_TAXHEAD))
                totalTax = totalTax.add(demandDetail.getTaxAmount());
            else prevRoundOffDemandDetail = demandDetail;
        }

        BigDecimal decimalValue = totalTax.remainder(BigDecimal.ONE);
        BigDecimal midVal = new BigDecimal(0.5);
        BigDecimal roundOff = BigDecimal.ZERO;

        /*
        * If the decimal amount is greater than 0.5 we subtract it from 1 and put it as roundOff taxHead
        * so as to nullify the decimal eg: If the tax is 12.64 we will add extra tax roundOff taxHead
        * of 0.36 so that the total becomes 13
        * */
        if(decimalValue.compareTo(midVal) > 0)
            roundOff = BigDecimal.ONE.subtract(decimalValue);


        /*
         * If the decimal amount is less than 0.5 we put negative of it as roundOff taxHead
         * so as to nullify the decimal eg: If the tax is 12.36 we will add extra tax roundOff taxHead
         * of -0.36 so that the total becomes 12
         * */
        if(decimalValue.compareTo(midVal) < 0)
            roundOff = decimalValue.negate();

        /*
        * If roundOff already exists in previous demand create a new roundOff taxHead with roundOff amount
        * equal to difference between them so that it will be balanced when bill is generated. eg: If the
        * previous roundOff amount was of -0.36 and the new roundOff excluding the previous roundOff is
        * 0.2 then the new roundOff will be created with 0.2 so that the net roundOff will be 0.2 -(-0.36)
        * */
        if(prevRoundOffDemandDetail!=null){
            roundOff = roundOff.subtract(prevRoundOffDemandDetail.getTaxAmount());
        }

        if(roundOff.compareTo(BigDecimal.ZERO)!=0){
                 DemandDetail roundOffDemandDetail = DemandDetail.builder()
                    .taxAmount(roundOff)
                    .taxHeadMasterCode(MDMS_ROUNDOFF_TAXHEAD)
                    .tenantId(tenantId)
                    .collectionAmount(BigDecimal.ZERO)
                    .build();

            demandDetails.add(roundOffDemandDetail);
        }
    }



    private User getUser(String uuid, RequestInfo requestInfo, String tenantId) {
    	List<String> uuidList = new ArrayList<>();
    	uuidList.add(uuid);
		UserSearchRequest searchRequest = UserSearchRequest.builder().uuid(uuidList)
				.tenantId(tenantId).userType(MRConstants.ROLE_CITIZEN).requestInfo(requestInfo).build();
		StringBuilder url = new StringBuilder(config.getUserHost()+config.getUserSearchEndpoint()); 
		UserResponse res = mapper.convertValue(serviceRequestRepository.fetchResult(url, searchRequest), UserResponse.class);
		if(CollectionUtils.isEmpty(res.getUser())) {
			return null;
		}
		return res.getUser().get(0).toCommonUser();
	}









}
