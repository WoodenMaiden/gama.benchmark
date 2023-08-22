package gama.benchmark.JMX;

import java.lang.management.*;
import com.sun.management.OperatingSystemMXBean;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProcessMetricsCollector {

    private OperatingSystemMXBean operatingSystemMXBean;
    private RuntimeMXBean runtimeMXBean;
    private MemoryMXBean memoryMXBean;


    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Results {

        @Getter
        private long totalPhysicalMemorySize;

        @Getter
        private double cpuLoad;

        @Getter
        private long duration;
    }


    public Results pollStats() {
        long duration = runtimeMXBean.getUptime();
        long totalPhysicalMemorySize = operatingSystemMXBean.getTotalPhysicalMemorySize();
        double cpuLoad = operatingSystemMXBean.getSystemCpuLoad();
        return new Results(totalPhysicalMemorySize, cpuLoad, duration);
    }
}
