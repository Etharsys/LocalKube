package fr.uge.localkube;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public record ApplicationData(@JsonProperty("id") int id,
                              @JsonProperty("app") String app,
                              @JsonProperty("port") int port,
                              @JsonProperty("docker-instance") String dockerInstance) {
    /**
     * record which represent an app
     * @field id the app id
     * @field app the full app name
     * @field port the app port
     * @field dockerInstance the short app name
     */
    private static final long startedTimeStamp = System.currentTimeMillis();

    /**
     * Constructor for ApplicationData
     * @param id the app id on json format
     * @param app the app full name on json format "name:port"
     * @param port the app port on json format
     * @param dockerInstance the short name, instance on json format
     * @throws LKArgumentsException when the id or the port is invalid
     * @throws NullPointerException when the app name is not given
     */
    @JsonCreator
    public ApplicationData {
        if (id <= 0 || port <= 8080){ throw new LKArgumentsException("Port cannot be <= 8080, " + port);}
        requireNonNull(app);
        requireNonNull(dockerInstance);
        var appL = app.split(":");
        if (appL.length != 2)
            throw new LKArgumentsException("Usage for docker name : name:port , get : " + Arrays.toString(appL));
        if (!appL[0].equals(dockerInstance.split("-")[0]))
            throw new LKArgumentsException("docker instance " + dockerInstance + " and appName " + appL[0] + " should be the same name");
        if (Integer.parseInt(appL[1]) != port)
            throw new LKArgumentsException("docker name port " + appL[1] + " and port " + port + " should be the same port");
    }

    /**
     * getter for the short app name
     * @return the app name
     */
    public String getNameApp(){
        return app.split(":")[0];
    }

    /**
     * string format (json format) of an app
     * @return the json app format
     */
    @Override
    public String toString(){
        return "{\n\tid:" + id
                + ",\n\tapp:" + app
                + ",\n\tport:" + port
                + ",\n\tdocker-instance:" + dockerInstance
                + "\n}";
    }

    /**
     * string format (json format) of an app which is stopped
     * @return the json app format
     */
    public String toStringStop(){
        var ellapsed = System.currentTimeMillis() - startedTimeStamp;
        return "{\n\tid:" + id
                + ",\n\tapp:" + app
                + ",\n\tport:" + port
                + ",\n\tdocker-instance:" + dockerInstance
                + ",\n\tellapsed-time:" + ellapsed
                + "\n}";
    }
}
