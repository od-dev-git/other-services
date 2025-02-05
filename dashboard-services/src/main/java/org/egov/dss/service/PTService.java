package org.egov.dss.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.Null;

import org.egov.dss.config.ConfigurationLoader;
import org.egov.dss.constants.DashboardConstants;
import org.egov.dss.model.Chart;
import org.egov.dss.model.CommonSearchCriteria;
import org.egov.dss.model.FinancialYearWiseProperty;
import org.egov.dss.model.PayloadDetails;
import org.egov.dss.model.PaymentSearchCriteria;
import org.egov.dss.model.PropertySerarchCriteria;
import org.egov.dss.repository.PTRepository;
import org.egov.dss.web.model.Data;
import org.egov.dss.web.model.Plot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PTService {
	
	@Autowired
	private PTRepository ptRepository;
	
	@Autowired
	private ConfigurationLoader config;

	public List<Data> totalProprties(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatusNotIn(Sets.newHashSet(DashboardConstants.PT_INWORKFLOW_STATUS));
		Integer totalProperties = (Integer) ptRepository.getTotalProperties(criteria);
		return Arrays.asList(Data.builder().headerValue(totalProperties).build());
	}

	public List<Data> propertiesPaid(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setBusinessServices(Sets.newHashSet(DashboardConstants.PT_REVENUE_ALL_BS));
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		Integer totalPropertiesPaid = (Integer) ptRepository.getTotalPropertiesPaid(criteria);
		return Arrays.asList(Data.builder().headerValue(totalPropertiesPaid).build());
	}

	public List<Data> propertiesAssessed(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		//Integer newAssessments = (Integer) totalProprties(payloadDetails).get(0).getHeaderValue();
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setIsPropertyAssessed(Boolean.TRUE);
		criteria.setStatus(DashboardConstants.STATUS_ACTIVE);
		Integer assessedPropertiesCount = (Integer) ptRepository.getAssessedPropertiesCount(criteria);
		//Integer totalAssessed = Integer.sum(newAssessments, reassessments);
		return Arrays.asList(Data.builder().headerValue(assessedPropertiesCount).build());
	}

	public List<Data> activeUlbs(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		Integer activePropertyULBs = (Integer) ptRepository.getActivePRopertyULBs(criteria);
		return Arrays.asList(Data.builder().headerValue(activePropertyULBs).build());
	}

	public List<Data> totalMutationProperties(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		Integer totalMutationPropertiesCount = (Integer) ptRepository.getTotalMutationPropertiesCount(criteria);
		return Arrays.asList(Data.builder().headerValue(totalMutationPropertiesCount).build());
	}

	public List<Data> ptTotalApplications(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		Integer assessedPropertiesCount = (Integer) ptRepository.getTotalApplicationsCount(criteria);
		return Arrays.asList(Data.builder().headerValue(assessedPropertiesCount).build());
	}

	public List<Data> totalnoOfProperties(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setFromDate(null);
		Integer totalPropertiesCount = ptRepository.getTotalPropertiesCount(criteria);
		return Arrays.asList(Data.builder().headerValue(totalPropertiesCount).build());
	}

	public List<Data> ptNewAssessmentShare(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		/*Integer ptTotalAssessmentsCount = (Integer) ptRepository.getPtTotalAssessmentsCount(criteria);
		Integer ptTotalNewAssessmentsCount = (Integer) ptRepository.getPtTotalNewAssessmentsCount(criteria);
		Integer ptTotalReAssessmentsCount = (Integer) ptRepository.getPtTotalReAssessmentsCount(criteria); */
		Integer ptTotalNewAssessmentsCount = (Integer) totalProprties(payloadDetails).get(0).getHeaderValue();
		Integer ptTotalReAssessmentsCount = (Integer) propertiesAssessed(payloadDetails).get(0).getHeaderValue();
		Integer ptTotalAssessmentsCount = Integer.sum(ptTotalNewAssessmentsCount, ptTotalReAssessmentsCount);
		return Arrays.asList(Data.builder()
				.headerValue(Math.round((ptTotalNewAssessmentsCount.doubleValue() / ptTotalAssessmentsCount.doubleValue()) * 100)).build());
	}

	public List<Data> ptReAssessmentShare(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
	/*	Integer ptTotalAssessmentsCount = (Integer) ptRepository.getPtTotalAssessmentsCount(criteria);
		Integer ptTotalNewAssessmentsCount = (Integer) ptRepository.getPtTotalNewAssessmentsCount(criteria);
		Integer ptTotalReAssessmentsCount = (Integer) ptRepository.getPtTotalReAssessmentsCount(criteria); */
		Integer ptTotalNewAssessmentsCount = (Integer) totalProprties(payloadDetails).get(0).getHeaderValue();
		Integer ptTotalReAssessmentsCount = (Integer) propertiesAssessed(payloadDetails).get(0).getHeaderValue();
		Integer ptTotalAssessmentsCount = Integer.sum(ptTotalNewAssessmentsCount, ptTotalReAssessmentsCount);
        return Arrays.asList(Data.builder()
				.headerValue(Math.round((ptTotalReAssessmentsCount.doubleValue() / ptTotalAssessmentsCount.doubleValue()) * 100)).build());
	}
	
	public List<Data> cumulativePropertiesAssessed(PayloadDetails payloadDetails) {
		List<Data> response = new ArrayList<>();
		getPropertySearchCriteria(payloadDetails);
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);

		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatus(DashboardConstants.STATUS_ACTIVE);
		criteria.setIsPropertyAssessed(Boolean.TRUE);
		List<Chart> cumulativePropertiesAssessed = ptRepository.getCumulativePropertiesAssessedNewQuery(criteria);

		List<Plot> plots = new ArrayList<>();
		extractDataForChart(cumulativePropertiesAssessed, plots);	
		BigDecimal total = cumulativePropertiesAssessed.stream().map(usageCategory -> usageCategory.getValue()).reduce(BigDecimal.ZERO,
				BigDecimal::add);	
		response.add(Data.builder().headerName("ReAssessments").headerValue(total).plots(plots).build());
		
		
		// CREATIONREASON CREATE
		criteria.setIsPropertyAssessed(null);
		criteria.setStatus(null);
		criteria.setStatusNotIn(Sets.newHashSet(DashboardConstants.STATUS_INWORKFLOW));
		criteria.setCreationReasons(Sets.newHashSet(DashboardConstants.PT_CREATIONREASON_CREATE));
		List<Chart> cumulativePropertiesCreated = ptRepository.getCumulativeProperties(criteria);
		List<Plot> plotsForCumulativePropertiesCreated = new ArrayList<>();
		extractDataForChart(cumulativePropertiesCreated, plotsForCumulativePropertiesCreated);	
		BigDecimal totalForCumulativePropertiesCreated = cumulativePropertiesCreated.stream().map(usageCategory -> usageCategory.getValue()).reduce(BigDecimal.ZERO,
				BigDecimal::add);	
		response.add(Data.builder().headerName("Create").headerValue(totalForCumulativePropertiesCreated).plots(plotsForCumulativePropertiesCreated).build());

		// CREATIONREASON UPDATE
		criteria.setCreationReasons(Sets.newHashSet(DashboardConstants.PT_CREATIONREASON_UPDATE));
		List<Chart> cumulativePropertiesUpdated = ptRepository.getCumulativeProperties(criteria);
		List<Plot> plotsForCumulativePropertiesUpdated = new ArrayList<>();
		extractDataForChart(cumulativePropertiesUpdated, plotsForCumulativePropertiesUpdated);	
		BigDecimal totalForCumulativePropertiesUpdated = cumulativePropertiesUpdated.stream().map(usageCategory -> usageCategory.getValue()).reduce(BigDecimal.ZERO,
				BigDecimal::add);	
		response.add(Data.builder().headerName("Update").headerValue(totalForCumulativePropertiesUpdated).plots(plotsForCumulativePropertiesUpdated).build());

		// CREATIONREASON MUTATION
		criteria.setCreationReasons(Sets.newHashSet(DashboardConstants.PT_CREATIONREASON_MUTATION));
		List<Chart> cumulativePropertiesMutation = ptRepository.getCumulativeProperties(criteria);
		List<Plot> plotsForCumulativePropertiesMutation= new ArrayList<>();
        extractDataForChart(cumulativePropertiesMutation, plotsForCumulativePropertiesMutation);	
		BigDecimal totalForCumulativePropertiesMutation = cumulativePropertiesMutation.stream().map(usageCategory -> usageCategory.getValue()).reduce(BigDecimal.ZERO,
				BigDecimal::add);	
		response.add(Data.builder().headerName("Mutation").headerValue(totalForCumulativePropertiesMutation).plots(plotsForCumulativePropertiesMutation).build());

		return response;
	}
	
	public List<Data> propertiesByUsageType(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatus(DashboardConstants.STATUS_ACTIVE);
		criteria.setFromDate(null);
		List<Chart> propertiesByUsageType = ptRepository.getpropertiesByUsageType(criteria);

		List<Plot> plots = new ArrayList();
		extractDataForChart(propertiesByUsageType, plots);	

		BigDecimal total = propertiesByUsageType.stream().map(usageCategory -> usageCategory.getValue()).reduce(BigDecimal.ZERO,
				BigDecimal::add);		 

		return Arrays.asList(Data.builder().headerName("DSS_PT_PROPERTIES_BY_USAGE_TYPE").headerValue(total).plots(plots).build());
	}

	public List<Data> topPerformingUlbsCompletionRate(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		HashMap<String, Long> slaCompletionCount = ptRepository.getSlaCompletionCountList(criteria);
		HashMap<String, Long> totalApplicationCompletionCount = ptRepository.getTotalApplicationCompletionCountList(criteria);

		List<Chart> percentList = mapTenantsForPerformanceRate(slaCompletionCount, totalApplicationCompletionCount);

		 Collections.sort(percentList,Comparator.comparing(e -> e.getValue(),(s1,s2)->{
             return s2.compareTo(s1);
         }));

		 List<Data> response = new ArrayList();
		 int Rank = 0;
		 for( Chart obj : percentList) {
			 Rank++;
			 response.add(Data.builder().headerName("Rank").headerValue(Rank).plots(Arrays.asList(Plot.builder().label("DSS_COMPLETION_RATE").name(obj.getName()).value(obj.getValue()).symbol("percentage").build())).headerSymbol("percentage").build());
		 };

		return response;
	}

	public List<Data> bottomPerformingUlbsCompletionRate(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		HashMap<String, Long> slaCompletionCount = ptRepository.getSlaCompletionCountList(criteria);
		HashMap<String, Long> totalApplicationCompletionCount = ptRepository.getTotalApplicationCompletionCountList(criteria);

		List<Chart> percentList = mapTenantsForPerformanceRate(slaCompletionCount, totalApplicationCompletionCount);

		 Collections.sort(percentList,Comparator.comparing(e -> e.getValue(),(s1,s2)->{
             return s1.compareTo(s2);
         }));

		 List<Data> response = new ArrayList();
		 int Rank = percentList.size();
		 for( Chart obj : percentList) {
			 response.add(Data.builder().headerName("Rank").headerValue(Rank).plots(Arrays.asList(Plot.builder().label("DSS_COMPLETION_RATE").name(obj.getName()).value(obj.getValue()).symbol("percentage").build())).headerSymbol("percentage").build());
			 Rank--;
		 };

		return response;
	}
	
	public List<Data> ptShareOfNewAssessment(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		//HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = ptRepository.getPtTotalAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalNewAssessmentsTenantwiseCount = ptRepository.getPtTotalNewAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalReAssessmentsTenantwiseCount = ptRepository.getPtTotalReAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = new HashMap<>();

		for (Map.Entry<String, Long> entry : ptTotalReAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, value);
		}

		for (Map.Entry<String, Long> entry : ptTotalNewAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, ptTotalAssessmentsTenantwiseCount.getOrDefault(key, 0L) + value);
		}
		List<Chart> percentList = mapTenantsForPerformanceRate(ptTotalNewAssessmentsTenantwiseCount, ptTotalAssessmentsTenantwiseCount);

		 Collections.sort(percentList,Comparator.comparing(e -> e.getValue(),(s1,s2)->{
             return s2.compareTo(s1);
         }));

		 List<Data> response = new ArrayList();
		 int Rank = 0;
		 for( Chart obj : percentList) {
			 Rank++;
			 response.add(Data.builder().headerName("Rank").headerValue(Rank).plots(Arrays.asList(Plot.builder().label("DSS_COMPLETION_RATE").name(obj.getName()).value(obj.getValue()).symbol("percentage").build())).headerSymbol("percentage").build());
		 };

		return response;
	}

	public List<Data> ptShareOfReAssessment(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		//HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = ptRepository.getPtTotalAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalNewAssessmentsTenantwiseCount = ptRepository.getPtTotalNewAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalReAssessmentsTenantwiseCount = ptRepository.getPtTotalReAssessmentsTenantwiseCount(criteria);
		
		HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = new HashMap<>();

		for (Map.Entry<String, Long> entry : ptTotalReAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, value);
		}

		for (Map.Entry<String, Long> entry : ptTotalNewAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, ptTotalAssessmentsTenantwiseCount.getOrDefault(key, 0L) + value);
		}
		List<Chart> percentList = mapTenantsForSharePerformanceRate(ptTotalReAssessmentsTenantwiseCount, ptTotalAssessmentsTenantwiseCount);

		 Collections.sort(percentList,Comparator.comparing(e -> e.getValue(),(s1,s2)->{
             return s2.compareTo(s1);
         }));

		 List<Data> response = new ArrayList();
		 int Rank = 0;
		 for( Chart obj : percentList) {
			 Rank++;
			 response.add(Data.builder().headerName("Rank").headerValue(Rank).plots(Arrays.asList(Plot.builder().label("DSS_COMPLETION_RATE").name(obj.getName()).value(obj.getValue()).symbol("percentage").build())).headerSymbol("percentage").build());
		 };

		return response;
	}

	private List<Chart> mapTenantsForPerformanceRate(HashMap<String, Long> numeratorMap,
			HashMap<String, Long> denominatorMap) {
		List<Chart> percentList = new ArrayList();
		numeratorMap.entrySet().stream().forEach(item -> {
			Long numerator = item.getValue();
			Long denominator = item.getValue();
			if (denominatorMap.containsKey(item.getKey())) {
				denominator = denominatorMap.get(item.getKey());
			}
			log.info("Denominator :" + denominator + " Numerator :" + numerator);
			if (denominator != 0) {
				BigDecimal percent = new BigDecimal(numerator * 100).divide(new BigDecimal(denominator), 2,
						RoundingMode.HALF_EVEN);
				percentList.add(Chart.builder().name(item.getKey()).value(percent).build());
			} else {
				percentList.add(Chart.builder().name(item.getKey()).value(BigDecimal.ZERO).build());
			}

		});
		return percentList;
	}

	private List<Chart> mapTenantsForSharePerformanceRate(HashMap<String, Long> numeratorMap,
			HashMap<String, Long> denominatorMap) {
		List<Chart> percentList = new ArrayList();
		numeratorMap.entrySet().stream().forEach(item -> {
			Long numerator = item.getValue();
			Long denominator = item.getValue();
			if (denominatorMap.containsKey(item.getKey())) {
				denominator = denominatorMap.get(item.getKey());
			}
			log.info("Denominator :" + denominator + " Numerator :" + numerator);
			if (denominator != 0) {
				BigDecimal percent = (new BigDecimal(denominator * 100).subtract(new BigDecimal(numerator * 100)))
						.divide(new BigDecimal(denominator), 2, RoundingMode.HALF_EVEN);
				percentList.add(Chart.builder().name(item.getKey()).value(percent).build());
			} else {
				percentList.add(Chart.builder().name(item.getKey()).value(BigDecimal.ZERO).build());
			}

		});
		return percentList;
	}
	
	public List<Data> slaAchieved(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		Integer totalApplication = (Integer) ptRepository.getAssessedPropertiesCount(criteria);
		Integer slaAchievedAppCount = (Integer) ptRepository.getSlaAchievedAppCount(criteria);
		return Arrays.asList(Data.builder()
				.headerValue((slaAchievedAppCount.doubleValue() / totalApplication.doubleValue()) * 100).build());
	}
	
	public Integer slaAchievedCount(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatusNotIn(Sets.newHashSet(DashboardConstants.PT_INWORKFLOW_STATUS));
		criteria.setSlaThreshold(config.getSlaPtThreshold());
		Integer slaAchievedAppCount = (Integer) ptRepository.getTotalProperties(criteria);
		//Integer slaAchievedAppCount = (Integer) ptRepository.getSlaAchievedAppCount(criteria);
		return slaAchievedAppCount;		
	}
    
	public PropertySerarchCriteria getPropertySearchCriteria(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = new PropertySerarchCriteria();

		if (StringUtils.hasText(payloadDetails.getModulelevel())) {
			criteria.setBusinessServices(Sets.newHashSet(payloadDetails.getModulelevel()));
		}
		
		if(StringUtils.hasText(payloadDetails.getTenantid())) {
			criteria.setTenantIds(Sets.newHashSet(payloadDetails.getTenantid()));
		}
		
		if(payloadDetails.getStartdate() != null && payloadDetails.getStartdate() != 0) {
			criteria.setFromDate(payloadDetails.getStartdate());
		}
		
		if(payloadDetails.getEnddate() != null && payloadDetails.getEnddate() != 0) {
			criteria.setToDate(payloadDetails.getEnddate());
		}
		
		return criteria;
	}
	
	private void extractDataForChart(List<Chart> items, List<Plot> plots) {
		
//		Long total = 0L;
//		for(Chart item : items) {
//			plots.add(Plot.builder().name(item.getName()).value(item.getValue()).symbol("number").build());
//			total = total + Long.valueOf(String.valueOf(item.getValue()));
//		}
		items.stream().forEach(item ->{
			plots.add(Plot.builder().name(item.getName()).value(item.getValue()).symbol("number").build());
		});
	}
	

	public List<Data> ptByFinancalYear(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		List<HashMap<String, Object>> PropertiesByFinancialYear = ptRepository.getPropertiesByFinancialYear(criteria);


			LinkedHashMap<String,Boolean> financialYearsMap = new LinkedHashMap<>();
				
				 for( HashMap<String, Object> tenant : PropertiesByFinancialYear) {
			 if(!financialYearsMap.containsKey(tenant.get("createdfinyear"))) 
				 financialYearsMap.put(String.valueOf(tenant.get("createdfinyear")), true); 
			 }
		
			int financialYearCount = financialYearsMap.size();

		
		HashMap<String,Boolean> financialYears = new HashMap<>();
		HashMap<String,List<FinancialYearWiseProperty>> financialYearWiseTenantProperty = new HashMap<>();
		
		for (HashMap<String, Object> tenantForFYMapping : PropertiesByFinancialYear) {

			if (!financialYearWiseTenantProperty.containsKey(tenantForFYMapping.get("tenantid"))) {
				List<FinancialYearWiseProperty> list = new ArrayList<>();
				for (String year : financialYearsMap.keySet()) {
					list.add(FinancialYearWiseProperty.builder().year(year).count(0L).build());

				}
				list.parallelStream().forEach(item ->{
					if(item.getYear().equals(tenantForFYMapping.get("createdfinyear"))) {
						item.setCount(Long.valueOf(String.valueOf(tenantForFYMapping.get("propertycount"))));
					}
				});
				financialYearWiseTenantProperty.put(String.valueOf(tenantForFYMapping.get("tenantid")), list);

			} else {
				List<FinancialYearWiseProperty> lists = financialYearWiseTenantProperty.get(tenantForFYMapping.get("tenantid"));
				lists.parallelStream().forEach(item ->{
					if(item.getYear().equals(tenantForFYMapping.get("createdfinyear"))) {
						item.setCount(Long.valueOf(String.valueOf(tenantForFYMapping.get("propertycount"))));
					}
				});
				financialYearWiseTenantProperty.put(String.valueOf(tenantForFYMapping.get("tenantid")), lists);
				
			}
			

		}
		 
			 List<Data> response = new ArrayList();
			 int serailNumber = 0 ;
			 for( HashMap.Entry<String, List<FinancialYearWiseProperty>> tenant : financialYearWiseTenantProperty.entrySet()) {
				 serailNumber++;
		            String tenantId = String.valueOf(tenant.getKey());
		            String tenantIdStyled = tenantId.replace("od.", "");
		            tenantIdStyled = tenantIdStyled.substring(0, 1).toUpperCase() + tenantIdStyled.substring(1).toLowerCase();
				 List<Plot> row = new ArrayList<>();
				row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
				row.add(Plot.builder().label(tenantIdStyled).name("DDRs").symbol("text").build());
				List<FinancialYearWiseProperty> propertyYearWiseCount = tenant.getValue();
				for(FinancialYearWiseProperty yearWiseCount :  propertyYearWiseCount) {
					row.add(Plot.builder().name(yearWiseCount.getYear()).value(new BigDecimal(String.valueOf(yearWiseCount.getCount()))).symbol("number").build());				
				}
				 response.add(Data.builder().headerName(tenantIdStyled).headerValue(serailNumber).plots(row).insight(null).build());
			 }	
			 
			 if(CollectionUtils.isEmpty(response)) {
				 serailNumber++;
				 List<Plot> row = new ArrayList<>();
					row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
					row.add(Plot.builder().label(payloadDetails.getTenantid()).name("DDRs").symbol("text").build());
                    financialYearsMap.entrySet().stream().forEach(item  -> {
						row.add(Plot.builder().name(item.getKey()).value(BigDecimal.ZERO).symbol("number").build());
					});
					response.add(Data.builder().headerName(payloadDetails.getTenantid()).headerValue(serailNumber).plots(row).insight(null).build());
			 }
			 
		return response;
	}

	public List<Data> ptAsmtStatusDDR(PayloadDetails payloadDetails) {

		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
//		HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = ptRepository
//				.getPtTotalAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalNewAssessmentsTenantwiseCount = ptRepository
				.getPtTotalNewAssessmentsTenantwiseCount(criteria);
		HashMap<String, Long> ptTotalReAssessmentsTenantwiseCount = ptRepository
				.getPtTotalReAssessmentsTenantwiseCount(criteria);

		HashMap<String, Long> ptTotalAssessmentsTenantwiseCount = new HashMap<>();

		for (Map.Entry<String, Long> entry : ptTotalReAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, value);
		}

		for (Map.Entry<String, Long> entry : ptTotalNewAssessmentsTenantwiseCount.entrySet()) {
			String key = entry.getKey();
			Long value = entry.getValue();
			ptTotalAssessmentsTenantwiseCount.put(key, ptTotalAssessmentsTenantwiseCount.getOrDefault(key, 0L) + value);
		}

		List<Chart> newAsmtPercentList = mapTenantsForPerformanceRate(ptTotalNewAssessmentsTenantwiseCount,
				ptTotalAssessmentsTenantwiseCount);
		HashMap<String, BigDecimal> newAsmtShareList = new HashMap<>();
		for (Chart tenant : newAsmtPercentList) {
			newAsmtShareList.put(tenant.getName(), tenant.getValue());
		}

		List<Chart> reAsmtPercentList = mapTenantsForSharePerformanceRate(ptTotalNewAssessmentsTenantwiseCount,
				ptTotalAssessmentsTenantwiseCount);
		HashMap<String, BigDecimal> reAsmtShareList = new HashMap<>();
		for (Chart tenant : reAsmtPercentList) {
			reAsmtShareList.put(tenant.getName(), tenant.getValue());
		}

		List<Data> response = new ArrayList();
		int serailNumber = 0;

		for (HashMap.Entry<String, Long> entry : ptTotalNewAssessmentsTenantwiseCount.entrySet()) {

			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

			serailNumber++;
			String tenantId = String.valueOf(entry.getKey());
			String tenantIdStyled = tenantId.replace("od.", "");
			tenantIdStyled = tenantIdStyled.substring(0, 1).toUpperCase() + tenantIdStyled.substring(1).toLowerCase();
			Long newAsmt = 0L;
			Long reAsmt = 0L;
			BigDecimal newAsmtShare = BigDecimal.ZERO;
			BigDecimal reAsmtShare = BigDecimal.ZERO;
			if (ptTotalAssessmentsTenantwiseCount.get(entry.getKey()) != null) {
				newAsmt = entry.getValue();
				reAsmt = ptTotalAssessmentsTenantwiseCount.get(entry.getKey()) - newAsmt;
				newAsmtShare = newAsmtShareList.get(entry.getKey());
				reAsmtShare = reAsmtShareList.get(entry.getKey());
			}
			List<Plot> row = new ArrayList<>();
			row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
			row.add(Plot.builder().label(tenantIdStyled).name("DDRs").symbol("text").build());

			row.add(Plot.builder().name(String.valueOf("New Assessments"))
					.value(new BigDecimal(String.valueOf(newAsmt))).symbol("number").build());

			row.add(Plot.builder().name(String.valueOf("New_Assesment_Share"))
					.value(new BigDecimal(String.valueOf(newAsmtShare))).symbol("percentage").build());

			row.add(Plot.builder().name(String.valueOf("Reassessed Properties"))
					.value(new BigDecimal(String.valueOf(reAsmt))).symbol("number").build());

			row.add(Plot.builder().name(String.valueOf("Reassessment_Share"))
					.value(new BigDecimal(String.valueOf(reAsmtShare))).symbol("percentage").build());

			response.add(Data.builder().headerName(tenantIdStyled).headerValue(serailNumber).plots(row).insight(null)
					.build());

		}
		
	      if(CollectionUtils.isEmpty(response)) {
	    	    serailNumber++;
	        	List<Plot> row = new ArrayList<>();
				row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
				row.add(Plot.builder().label(String.valueOf(payloadDetails.getTenantid())).name("DDRs").symbol("text").build());

				row.add(Plot.builder().name(String.valueOf("New Assessments"))
						.value(BigDecimal.ZERO).symbol("number").build());

				row.add(Plot.builder().name(String.valueOf("New_Assesment_Share"))
						.value(BigDecimal.ZERO).symbol("percentage").build());

				row.add(Plot.builder().name(String.valueOf("Reassessed Properties"))
						.value(BigDecimal.ZERO).symbol("number").build());

				row.add(Plot.builder().name(String.valueOf("Reassessment_Share"))
						.value(BigDecimal.ZERO).symbol("percentage").build());

				response.add(Data.builder().headerValue(serailNumber).plots(row).insight(null)
						.build());
	        }

		return response;
	}

	public HashMap<String, Long> totalApplicationsTenantWise(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		HashMap<String, Long> totalApplicationCountTenentWise = ptRepository.totalApplicationsTenantWise(criteria);
		return totalApplicationCountTenentWise;
	}
	
	public HashMap<String, Long> totalCompletedApplicationsTenantWise(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatus(DashboardConstants.STATUS_ACTIVE);
		HashMap<String, Long> totalApplicationCompletionCount = ptRepository.getTotalApplicationCompletionCountList(criteria);
		return totalApplicationCompletionCount;
	}
	
	public List<Data> ptClosedApplications(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		criteria.setStatus(DashboardConstants.STATUS_ACTIVE);
		Integer assessedPropertiesCount = (Integer) ptRepository.getTotalApplicationsCount(criteria);
		return Arrays.asList(Data.builder().headerValue(assessedPropertiesCount).build());
	}
	
	public List<Data> ptStatusByBoundary(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		List<HashMap<String, Object>> ptStatusByBoundary = ptRepository.getPtStatusByBoundary(criteria);

		List<Data> response = new ArrayList();
		int serailNumber = 0;
		for (HashMap<String, Object> ptStatus : ptStatusByBoundary) {
			serailNumber++;
			String tenantId = String.valueOf(ptStatus.get("tenantid"));
			String tenantIdStyled = tenantId.replace("od.", "");
			tenantIdStyled = tenantIdStyled.substring(0, 1).toUpperCase() + tenantIdStyled.substring(1).toLowerCase();
			List<Plot> row = new ArrayList<>();
			row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
			row.add(Plot.builder().label(tenantIdStyled).name("ULBs").symbol("text").build());

			row.add(Plot.builder().name("Active").value(new BigDecimal(String.valueOf(ptStatus.get("activecnt"))))
					.symbol("number").build());
			row.add(Plot.builder().name("Inactive").value(new BigDecimal(String.valueOf(ptStatus.get("inactivecnt"))))
					.symbol("number").build());
			row.add(Plot.builder().name("Deactivated").value(new BigDecimal(String.valueOf(ptStatus.get("deactivatedcnt"))))
					.symbol("number").build());
			row.add(Plot.builder().name("In Workflow")
					.value(new BigDecimal(String.valueOf(ptStatus.get("inworkflowcnt")))).symbol("number")
					.build());
			
			response.add(Data.builder().headerName(tenantIdStyled).headerValue(serailNumber).plots(row).insight(null)
					.build());
		}
		
		if (CollectionUtils.isEmpty(response)) {
			serailNumber++;
			List<Plot> row = new ArrayList<>();
			row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
			row.add(Plot.builder().label(payloadDetails.getTenantid()).name("ULBs").symbol("text").build());
            row.add(Plot.builder().name("Active").value(BigDecimal.ZERO).symbol("number").build());
			row.add(Plot.builder().name("Inactive").value(BigDecimal.ZERO).symbol("number").build());
			row.add(Plot.builder().name("Deactivated").value(BigDecimal.ZERO).symbol("number").build());
			row.add(Plot.builder().name("In Workflow").value(BigDecimal.ZERO).symbol("number").build());
		response.add(Data.builder().headerName(payloadDetails.getTenantid()).headerValue(serailNumber).plots(row)
					.insight(null).build());
		}
	   
		return response;
     }

	public List<Data> totalNoOfDeactivatedProperties(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
	//	setFromAndToDateInGMT(criteria);
		Integer totalNoOfDeactivatedProperties = (Integer) ptRepository.getTotalNoOfDeactivatedProperties(criteria);
		if(totalNoOfDeactivatedProperties == null) {
			totalNoOfDeactivatedProperties = 0;
		}
		return Arrays.asList(Data.builder().headerValue(totalNoOfDeactivatedProperties).build());
	}
	
	private void setFromAndToDateInGMT(PropertySerarchCriteria propertySearchCriteria) {
		Long startTimeMillis = propertySearchCriteria.getFromDate();
		Long endTimeMillis = propertySearchCriteria.getToDate();

		Integer istOffsetHours = 5;
		Integer istOffsetMinutes = 30;

		Long offsetMillis = ((long) istOffsetHours * 60 + istOffsetMinutes) * 60 * 1000;

		Long startMillisGMT = startTimeMillis + offsetMillis;
		Long endMillisGMT = endTimeMillis + offsetMillis;

		propertySearchCriteria.setFromDate(startMillisGMT);
		propertySearchCriteria.setToDate(endMillisGMT);
	}

	public List<Data> ptApplicationsAgeing(PayloadDetails payloadDetails) {
		PropertySerarchCriteria criteria = getPropertySearchCriteria(payloadDetails);
		criteria.setExcludedTenantId(DashboardConstants.TESTING_TENANT);
		List<HashMap<String, Object>> ptApplicationsAgeingBreakup = ptRepository.getPTApplicationsAgeing(criteria);

			 List<Data> response = new ArrayList();
			 int serailNumber = 0 ;
			 for( HashMap<String, Object> tenantWiseRow : ptApplicationsAgeingBreakup) {
				 serailNumber++;
		            String tenantId = String.valueOf(tenantWiseRow.get("tenantid"));
		            String tenantIdStyled = tenantId.replace("od.", "");
		            tenantIdStyled = tenantIdStyled.substring(0, 1).toUpperCase() + tenantIdStyled.substring(1).toLowerCase();
				 List<Plot> row = new ArrayList<>();
				row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
				row.add(Plot.builder().label(tenantIdStyled).name("DDRs").symbol("text").build());

				row.add(Plot.builder().name("Pending_from_0_to_3_days").value(new BigDecimal(String.valueOf(tenantWiseRow.get("pending_from_0_to_3_days")))).symbol("number").build());				
				row.add(Plot.builder().name("Pending_from_3_to_7_days").value(new BigDecimal(String.valueOf(tenantWiseRow.get("pending_from_3_to_7_days")))).symbol("number").build());				
				row.add(Plot.builder().name("Pending_from_7_to_15_days").value(new BigDecimal(String.valueOf(tenantWiseRow.get("pending_from_7_to_15_days")))).symbol("number").build());				
				row.add(Plot.builder().name("Pending_from_more_than_15_days").value(new BigDecimal(String.valueOf(tenantWiseRow.get("pending_from_more_than_15_days")))).symbol("number").build());				
				row.add(Plot.builder().name("Total_Pending_Applications").value(new BigDecimal(String.valueOf(tenantWiseRow.get("total_pending_applications")))).symbol("number").build());				

				 response.add(Data.builder().headerName(tenantIdStyled).headerValue(serailNumber).plots(row).insight(null).build());
			 }	

				if (CollectionUtils.isEmpty(response)) {
					serailNumber++;
					List<Plot> row = new ArrayList<>();
					row.add(Plot.builder().label(String.valueOf(serailNumber)).name("S.N.").symbol("text").build());
					row.add(Plot.builder().label(payloadDetails.getTenantid()).name("DDRs").symbol("text").build());
					row.add(Plot.builder().name("Pending_from_0_to_3_days").value(BigDecimal.ZERO).symbol("number")
							.build());
					row.add(Plot.builder().name("Pending_from_3_to_7_days").value(BigDecimal.ZERO).symbol("number")
							.build());
					row.add(Plot.builder().name("Pending_from_7_to_15_days").value(BigDecimal.ZERO).symbol("number")
							.build());
					row.add(Plot.builder().name("Pending_from_more_than_15_days").value(BigDecimal.ZERO)
							.symbol("number").build());
					row.add(Plot.builder().name("Total_Pending_Applications").value(BigDecimal.ZERO).symbol("number")
							.build());
					response.add(Data.builder().headerName(payloadDetails.getTenantid()).headerValue(serailNumber)
							.plots(row).insight(null).build());
				}

		return response;
	}
	
}
