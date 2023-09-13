package gama.benchmark.JMX;

import static java.lang.management.ManagementFactory.*;
import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

import java.io.*;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;


import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.tools.attach.*;
import com.sun.management.OperatingSystemMXBean;

public class Main {
    private static final ProcessBuilder process =
            new ProcessBuilder()
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError (ProcessBuilder.Redirect.DISCARD);

    public static void main(String[] args) throws IOException {
        process.command(args);

        if (args.length < 1) {
            System.err.println("Usage: java CmdLineTool <pid>");
            System.err.println("OR: ");
            System.err.println("Usage: java CmdLineTool <a command spawning a JVM>");
        }
        else if (isCreatable(args[0]) && pollStats(args[0])) return; // to attach to a running process
        else if ( // to create a new process and attach to it
            pollStats(String.valueOf(process.start().pid()))
        ) return;

        System.out.println("Currently running");
        for (VirtualMachineDescriptor vmd : VirtualMachine.list())
            System.out.println(vmd.id() + "\t" + vmd.displayName());
    }

    private static boolean pollStats(String id) {
        try {
            Thread.sleep(1000);
            System.out.println("Attaching to " + id);

            VirtualMachine vm = VirtualMachine.attach(id);

            System.out.println("Attached to the vm");

            try {
                Main.writeObject("sys-properties", vm.getSystemProperties());

                MBeanServerConnection sc = connect(vm);

                ProcessMetricsCollector collector = new ProcessMetricsCollector(
                    newPlatformMXBeanProxy(sc, OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class),
                    newPlatformMXBeanProxy(sc, RUNTIME_MXBEAN_NAME         , RuntimeMXBean.class),
                    newPlatformMXBeanProxy(sc, MEMORY_MXBEAN_NAME          , MemoryMXBean.class)
                );


                ProcessMetricsCollector.Results results = new ProcessMetricsCollector.Results();

                while (true) { // it is not documented but an Exception is thrown when the attached process stops
                    try {
                        System.out.print("Polling data" + ".".repeat((int) Instant.now().getEpochSecond() % 10) + "\r");
                        results = collector.pollStats();
                    } catch (UndeclaredThrowableException exception) {
                        System.out.println("Process has stopped!");
                        break;
                    }
                }

                String resultFile = Main.writeObject("results" , results);
                System.out.println("Result File: " + resultFile);

            } catch (IOException ex) {
                System.out.print("JMX: ");
                ex.printStackTrace();
            }

            try {
                vm.detach();
            } catch (Exception ex) {
                System.out.print("Detach: ");
                ex.printStackTrace();
            }

            return true;
        } catch (AttachNotSupportedException | IOException | InterruptedException ex) {
            System.out.print("Attach: ");
            ex.printStackTrace();
        }
        return false;
    }

    // requires Java 8, alternative below the code
    static MBeanServerConnection connect(VirtualMachine vm) throws IOException {
        String connectorAddress = vm.startLocalManagementAgent();
        System.out.println("Connecting to: " + connectorAddress);
        JMXConnector c = JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
        System.out.println("Connected!");
        return c.getMBeanServerConnection();
    }

    /**
     * Writes an object into a json file under the /tmp dir, it returns the file's absolute path
     */
    static String writeObject(String suffix, Object toBeWritten) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = File.createTempFile("benchmark", suffix);
        file.setReadable(true, /*ownerOnly*/ false);

        new FileOutputStream(
                file,
                /* append */ false
        ).write(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(toBeWritten)
        );

        return file.getAbsolutePath();

    }
}