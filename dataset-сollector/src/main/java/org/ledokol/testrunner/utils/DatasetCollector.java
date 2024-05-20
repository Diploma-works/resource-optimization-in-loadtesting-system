package org.ledokol.testrunner.utils;

import org.ledokol.testrunner.model.ParameterSet;
import org.ledokol.testrunner.repository.ParameterSetRepository;
import org.ledokol.testrunner.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DatasetCollector {
    @Autowired
    private TimeConversion timeConversion;

    @Autowired
    private ParameterSetRepository parameterSetRepository;
    @Autowired
    private PrometheusService prometheusService;

    private final String cpuQuery = "sum(rate(container_cpu_usage_seconds_total{namespace=\"default\", container!=\"POD\",  pod=~\".*generator.*\", name!=\"\"}[90s])) by (pod)";
    private final String ramQuery = "sum(container_memory_working_set_bytes{namespace=\"default\", container!=\"POD\",  pod=~\".*generator.*\", name!=\"\"}) by (pod)";

    public void run() {
        System.out.println("Start collecting data");
        collectDataSet();
    }


    public void collectDataSet(){
        ParameterSet parameterSet;
        List<ParameterSet> allParamSets = parameterSetRepository.findAll();
        int counter = 0;
        for (ParameterSet allParamSet : allParamSets) {
            parameterSet = allParamSet;
            if (parameterSet.getIs_done() && parameterSet.getRedone() != null && parameterSet.getRedone()) {
                System.out.println("Id: " + parameterSet.getId());
                double maxCPU = getMaxCPU(parameterSet);
                double maxRAM = getMaxRAM(parameterSet);
                double meanCPU = getMeanCPU(parameterSet);
                double meanRAM = getMeanRAM(parameterSet);

                parameterSet.setMeanCPU(meanCPU);
                parameterSet.setMaxCPU(maxCPU);
                parameterSet.setMeanRAM(meanRAM);
                parameterSet.setMaxRAM(maxRAM);
                parameterSetRepository.save(parameterSet);
                counter++;
            }
        }
        System.out.printf("Обработано %d записей", counter);
    }

    private double getMeanCPU(ParameterSet parameterSet){
        return getMeanMetricValue(cpuQuery, parameterSet);
    }

    private double getMeanRAM(ParameterSet parameterSet){
        return getMeanMetricValue(ramQuery, parameterSet)/1024/1024;
    }
    private double getMaxCPU(ParameterSet parameterSet){
        return getMaxMetricValue(cpuQuery, parameterSet);
    }

    private double getMaxRAM(ParameterSet parameterSet){
        return getMaxMetricValue(ramQuery, parameterSet)/1024/1024;
    }
    private double getMeanMetricValue(String query, ParameterSet parameterSet){
        String generatorId = handleGeneratorId(parameterSet.getGeneratorInstanceId());
        long startDate = parameterSet.getStartDate().getTime()/1000;
        long endDate = parameterSet.getEndDate().getTime()/1000;
//        System.out.println("executing query to prometheus: " + query);
        return prometheusService.getMeanValueFromQuery(query, startDate, endDate, generatorId);
    }

    private double getMaxMetricValue(String query, ParameterSet parameterSet){
        String generatorId = handleGeneratorId(parameterSet.getGeneratorInstanceId());
        long startDate = parameterSet.getStartDate().getTime()/1000;
        long endDate = parameterSet.getEndDate().getTime()/1000;
//        System.out.println("executing query to prometheus: " + query);
        return prometheusService.getMaxValueFromQuery(query, startDate, endDate, generatorId);
    }

    private String handleGeneratorId(String generatorId) {
        Pattern pattern = Pattern.compile("(generator-\\d+)");
        Matcher matcher = pattern.matcher(generatorId);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
