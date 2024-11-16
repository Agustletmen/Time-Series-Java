package com.zzl.tsbd.controller;

import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.zzl.tsbd.po.Mem;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InfluxDB2Example {

    //String token = System.getenv("akaP-JPJaxngjLE-7wXYhqCwWWShNvkdOOppodNOpsXragU4fKssiYiTib1dKCUSHaz2Byrox9qOPdn0LcG2BQ==");
    private final static String token = "akaP-JPJaxngjLE-7wXYhqCwWWShNvkdOOppodNOpsXragU4fKssiYiTib1dKCUSHaz2Byrox9qOPdn0LcG2BQ==";
    private final static String bucket = "xs";
    private final static String org = "xs";
    private final static String url = "http://localhost:8086";

    public static void main(final String[] args) {
        //test01();
        //test02();
        //test03();
        test04();
    }


    /**
     * Use InfluxDB Line Protocol to write data
     */
    private static void test01() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray());
        String data = "mem,host=host1 used_percent=23.43234543";

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeRecord(bucket, org, WritePrecision.NS, data);
        client.close();
    }


    /**
     * Use a Data Point to write data
     */
    private static void test02() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray());
        Point point = Point
                .measurement("mem")
                .addTag("host", "host1")
                .addField("used_percent", 23.43234543)
                .time(Instant.now(), WritePrecision.NS);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
        client.close();
    }

    /**
     * Use POJO and corresponding class to write data
     */
    private static void test03() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray());
        WriteApi writeApi = client.getWriteApi();

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10000000; i++) {
            executorService.submit(() -> {
                Mem mem = new Mem();
                mem.setTime(Instant.now());
                mem.setHost("host1");
                mem.setUsed_percent(Math.random() * 100);
                mem.setUsage(Math.random() * 100);
                mem.setIs_used(Math.random() > 0.5);
                writeApi.writeMeasurement(bucket, org, WritePrecision.NS, mem);
                System.out.println(Thread.currentThread().getName() + " ===> " + mem.getTime());
            });
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.close();
    }

    /**
     * Execute a Flux query
     */
    private static void test04() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray());
        String query = "from(bucket: \"xs\")\n" +
                //"  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)\n" +
                "  |> range(start: -2h)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"demo_men\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"used_percent\" or r[\"_field\"] == \"usage\")";
        QueryApi queryApi = client.getQueryApi();
        List<FluxTable> tables = queryApi.query(query, org);

        long r = 0;
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                System.out.println(record);
                System.out.println(record.getValues());
                r++;
            }
        }
        System.out.println(r);
        client.close();
    }
}

