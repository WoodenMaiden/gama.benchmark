package gama.benchmark.JMX;

import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;

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

    public static ArrayList<Double> CPUTimes = new ArrayList<>();

    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Results {

        @Getter
        private long totalMemoryUsed;

        @Getter
        private double cpuLoad;

        @Getter
        private long duration;
    }


    public Results pollStats() {
        long duration = runtimeMXBean.getUptime();
        long totalMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed() + memoryMXBean.getNonHeapMemoryUsage().getUsed();

        CPUTimes.add(operatingSystemMXBean.getProcessCpuLoad());

        return new Results(
                totalMemoryUsed,
            CPUTimes.stream().reduce(0.0, Double::sum) / CPUTimes.size(),
            duration
        );
    }
}
