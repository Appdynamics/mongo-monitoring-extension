/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.mongo.input.MetricConfig;
import com.appdynamics.monitors.mongo.input.Stat;
import com.mongodb.BasicDBObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class MetricUtils {

    private static final Logger logger = LoggerFactory.getLogger(MetricUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, String> parsedData = new HashMap<>();


    public Map<String, String> getNumericMetricsFromMap(Map<String, Object> map, String key) {

        String metricName;
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof BasicDBObject || entry.getValue() instanceof Map) {
                    // Map found, digging further
                    getNumericMetricsFromMap((Map<String, Object>) entry.getValue(), entry.getKey());
                } else{
                        metricName = (key!=null ? key + "|" : "") + entry.getKey();
                        if (entry.getKey().contains(",")) {
                            metricName = metricName.replaceAll(",", ":");
                        }
                        parsedData.put(metricName, entry.getValue().toString());
                }
            }
        }catch(Exception e){
            logger.error("Error parsing data ", e);
        }

        return parsedData;
    }

    public List<Metric> generateMetrics(Map<String, String> valueMap, String metricPrefix, Stat childStat) {

        List<Metric> metrics = new ArrayList<>();
        for (MetricConfig metricConfig : childStat.getMetricConfig()) {
            String metricValue =  valueMap.get(metricConfig.getAttr());
            if(metricValue!=null) {
                Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                Metric metric = new Metric(metricConfig.getAttr(), String.valueOf(metricValue), metricPrefix + "|" + metricConfig.getAlias(), propertiesMap);
                metrics.add(metric);
            }
        }
        logger.debug("Returning " + metrics.size() + " metrics for " + metricPrefix);
        return metrics;
    }

    // Separate method for replica metrics as the replica member names are dynamic
    public List<Metric> generateReplicaMetrics(Map<String, String> valueMap, String metricPrefix, Stat childStat, Set<String> membersData, int primaryElected) {

        List<Metric> metrics = new ArrayList<>();
        for(String memberName: membersData) {
            for (MetricConfig metricConfig : childStat.getMetricConfig()) {
                String metricValue = valueMap.get(memberName + "|" + metricConfig.getAttr());
                if (metricValue != null) {
                    Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                    Metric metric = new Metric(metricConfig.getAttr(), String.valueOf(metricValue), metricPrefix + "|" + memberName + "|" + metricConfig.getAlias(), propertiesMap);
                    metrics.add(metric);
                }
            }
        }
        if(primaryElected == 1){
            metrics.add(new Metric("Primary Elected", String.valueOf(primaryElected), metricPrefix + "|Primary Elected", "AVG", "AVG", "IND"));
        }
        logger.debug("Returning " + metrics.size() + " metrics for " + metricPrefix);
        return metrics;
    }

}
