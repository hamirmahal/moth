package edu.sjsu.moth.server;

import edu.sjsu.moth.server.util.MothConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.HashMap;

@Command(name = "moth-server", mixinStandardHelpOptions = true)
public class MothCommandLine implements Runnable {
    @Parameters(index = "0", description = "Config file")
    private File configFile;

    @Parameters(index = "1..*", description = "extra spring arguments")
    private String[] springArgs;

    public static void main(String[] args) {
        var rc = new CommandLine(new MothCommandLine()).execute(args);
        // SpringApplication.run() will return when the application
        // starts up, so even though execute returns, that doesn't mean
        // that the application is done. thus, we should only exit if
        // we get a non-zero return code.
        if (rc != 0) System.exit(rc);
    }

    public void run() {
        final var prefix = "spring.";
        try {
            MothConfiguration config;
            config = new MothConfiguration(configFile);
            HashMap<String, Object> defaults = new HashMap<String, Object>();
            defaults.put("server.port", config.getServerPort());
            defaults.put("server.name", config.getServerName());
            defaults.put("spring.data.mongodb.host", config.getDBServer());
            // add all the properties that start with "spring." into the defaults
            config.properties.entrySet().stream().filter(e -> e.getKey().toString().startsWith(prefix)).forEach(e -> {
                defaults.put(e.getKey().toString().substring(prefix.length()), e.getValue());
            });
            SpringApplication moth = new SpringApplication(MothServerMain.class);
            moth.setDefaultProperties(defaults);
            moth.setWebApplicationType(WebApplicationType.REACTIVE);
            moth.run(springArgs == null ? new String[0] : springArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



