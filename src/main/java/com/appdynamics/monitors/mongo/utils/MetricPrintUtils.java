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

import static com.appdynamics.monitors.mongo.utils.Constants.METRICS_SEPARATOR;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class MetricPrintUtils {

    private static final Logger logger = LoggerFactory.getLogger(MetricPrintUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, String> parsedData = new HashMap<>();


    public Map<String, String> getNumericMetricsFromMap(Map<String, Object> map, String key) {

        String metricName;
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof BasicDBObject) {
                    // Map found, digging further
                    getNumericMetricsFromMap((Map<String, Object>) entry.getValue(), entry.getKey());
                } else {
                    if (entry.getValue() instanceof Number) {
                        metricName =  (key!=null ? key + "|" : "") + entry.getKey();
                        if (entry.getKey().contains(",")) {
                            metricName = metricName.replaceAll(",", ":");
                        }
                        parsedData.put(metricName, entry.getValue().toString());
                    }
                    if (entry.getKey().equalsIgnoreCase("stateStr") && entry.getValue().toString().equalsIgnoreCase("PRIMARY"))
                        parsedData.put("primaryElected", "1");

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
        return metrics;
    }

}
