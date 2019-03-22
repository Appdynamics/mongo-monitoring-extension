/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.mongo.utils;

import com.appdynamics.extensions.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.monitors.mongo.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 3/22/19.
 */
public class MetricPrintUtils {
    private static final Logger logger = LoggerFactory.getLogger(MetricPrintUtils.class);


    public static List<Metric> getNumericMetricsFromMap(Map<String, Object> map, String metricPath) {
        List<Metric> metricList = new ArrayList<Metric>();


        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                // Map found, digging further
               getNumericMetricsFromMap((Map<String, Object>) entry.getValue(), metricPath + entry.getKey() + METRICS_SEPARATOR);
            } else {
                if (entry.getValue() instanceof Number) {
                    metricList.add(getMetric( entry.getKey(), entry.getValue().toString(), metricPath));
                }
            }
        }

        return metricList;
    }

    public static Metric getMetric(String metricName, String metricValue, String metricPrefix) {
        if (metricValue != null) {
            if (metricName.contains(",")) {
                metricName = metricName.replaceAll(",", ":");
            }
            return new Metric(metricName, metricValue, metricPrefix);
        } else {
            logger.warn("Metric " + metricName + " is null");
        }
        return null;
    }

    public static String getMetricPathPrefix(String metricPathPrefix) {
        if (!metricPathPrefix.endsWith(METRICS_SEPARATOR)) {
            metricPathPrefix += METRICS_SEPARATOR;
        }
        return metricPathPrefix;
    }


}
