package org.egov.mrcalculator.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@Data
public class MRCalculatorConfigs {



    @Value("${egov.billingservice.host}")
    private String billingHost;

    @Value("${egov.taxhead.search.endpoint}")
    private String taxHeadSearchEndpoint;

    @Value("${egov.taxperiod.search.endpoint}")
    private String taxPeriodSearchEndpoint;

    @Value("${egov.demand.create.endpoint}")
    private String demandCreateEndpoint;

    @Value("${egov.demand.update.endpoint}")
    private String demandUpdateEndpoint;

    @Value("${egov.demand.search.endpoint}")
    private String demandSearchEndpoint;

    @Value("${egov.bill.gen.endpoint}")
    private String billGenerateEndpoint;

    @Value("${egov.demand.minimum.payable.amount}")
    private BigDecimal minimumPayableAmount;

    @Value("${egov.demand.businessserviceMR}")
    private String businessServiceMR;



    //TaxHeads
    @Value("${egov.taxhead.basetax}")
    private String baseTaxHead;
    
    
    @Value("${egov.taxhead.challanFee}")
    private String challanFeeTaxHead;
    
    @Value("${egov.taxhead.registrationFee}")
    private String registrationFeeTaxHead;
    
    @Value("${egov.taxhead.developmentFee}")
    private String developmentFeeTaxHead;
    
    @Value("${egov.taxhead.redcrossFee}")
    private String redcrossFeeTaxHead;
    
    @Value("${egov.taxhead.userFee}")
    private String userFeeTaxHead;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;


    //Kafka Topics
    @Value("${persister.save.mr.calculation.topic}")
    private String saveTopic;


    //CalculaterType Default Values
    @Value("${egov.mr.calculationtype.default}")
    private String defaultCalculationType;

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


    @Value("${phc.noregistrationfees.tenantid}")
    private String phcNoRegistrationFeesTenants;
    
    
}
