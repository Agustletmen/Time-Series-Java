package com.zzl.tsbd.po;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Data
@Measurement(name = "demo_men")
public class Mem {
    @Column(timestamp = true)
    private Instant time;

    @Column(tag = true)
    private String host;
    @Column
    private Double used_percent;

    @Column
    private Double usage;

    @Column
    private Boolean is_used;
}