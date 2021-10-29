package fr.uge.localkube;

import java.util.Objects;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import java.beans.ConstructorProperties;


public record Log(String app, String logs){
    /**
     * class which represent a log
     * @field app the full app name
     * @field logs the full logs of the app, in a given time
     */

    /**
     * Constructor of a log object, taking an app name and its logs.
     * @param app the full app name
     * @param logs the full logs of the app, in a given time
     */
    @ConstructorProperties({"app", "logs"})
    public Log {
        Objects.requireNonNull(app);
        Objects.requireNonNull(logs);
        if (app.equals("")) {
            throw new LKArgumentsException("The app name must not be empty");
        }
    }

    /**
     * Getter of the app name of a log object.
     * @return the app name of the log object
     */
    @ColumnName("app")
    public String getApp(){
        return app;
    }

    /**
     * Getter of the logs of a log object.
     * @return the logs of the log object
     */
    @ColumnName("logs")
    public String getLogs(){
        return logs;
    }

    /**
     * Setter of the app name of a log object.
     * @param app the full app name
     */
    public void setApp(String app){
        Objects.requireNonNull(app);
        if (app.equals("")){
            throw new LKArgumentsException("The app name must not be empty");
        }
    }

    /**
     * Setter of the logs of a log object.
     * @param logs the full logs of the app, in a given time
     */
    public void setLogs(String logs){
        Objects.requireNonNull(logs);
    }

    /**
     * An override of toString to print in a human-readable way the content of a log.
     * @return a String (JSON-style) of a log
     */
    @Override
    public String toString(){
        return "{\n\tapp:" + app + ",\n\tlogs:"
                + logs + "\n}";
    }

}
