package fr.uge.localkube;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


public class ApplicationDataCreator {
    /**
     * class which represent the data of applications
     * @field runningApps a map (id, apps) of apps which are running
     * @field stoppedApps a map (id, apps) of apps which are stopped
     * @field instanceApps a map (appName, number of instance) of all docker instance
     * @field maxApp
     */
    private final HashMap<Integer, ApplicationData> runningApps = new HashMap<>();
    private final HashMap<Integer, ApplicationData> stoppedApps = new HashMap<>();
    private final HashMap<String, Integer> instanceApps = new HashMap<>();
    private static final int maxApp = 1000;

    /**
     * Constructor of ApplicationDataCreator
     */
    public ApplicationDataCreator(){
    }

    /**
     * the parsing method from json entry format to an ApplicationData format
     * @param jsonData the json entry format to parse
     * @return the parsed ApplicationData
     */
    public ApplicationData toApplicationData(String jsonData) throws LKArgumentsException {
        requireNonNull(jsonData, "should have a non null json to parse to ApplicationData");
        if (jsonData.length() < 17){
            throw new LKArgumentsException("Not possible to have less than 17 characters in the json entry : " + jsonData);
        }
        var id = calculateNextId();
        var dockerInstance = calculateNextDockerInstance(jsonData.split(":")[1]);

        jsonData = "{\"id\":" + id + ", "
                + jsonData.substring(1, jsonData.length() - 1)
                + ", \"port\":" + jsonData.substring(jsonData.length() - 6, jsonData.length() - 2)
                + ", \"docker-instance\":" + dockerInstance + "\"}";
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(jsonData, ApplicationData.class);
        } catch (JsonProcessingException e){
            throw new LKArgumentsException(e.getOriginalMessage());
        }
    }

    /**
     * add an app to the runningApps
     * @param app the app to add
     * @throws LKArgumentsException when the port is already taken
     */
    public void add(ApplicationData app) throws LKArgumentsException {
        requireNonNull(app, "should have a non null app to add");
        checkInApps(runningApps, app);
        checkInApps(stoppedApps, app);
        runningApps.put(app.id(), app);
    }

    /**
     * check if an new app port is already taken
     * @param apps a list of app to browse
     * @param app the app to add
     * @throws LKArgumentsException when the port is already taken
     */
    private void checkInApps(HashMap<Integer, ApplicationData> apps, ApplicationData app) throws LKArgumentsException {
        requireNonNull(apps, "Should have a non null map to browse");
        requireNonNull(app, "Should have a non null app");
        for (var value : apps.values()){
            if (app.port() == value.port()){
                throw new LKArgumentsException("The specified port " + app.port() + " is already taken by " +
                        value.dockerInstance() + "-> Please retry with a valid port");
            }
        }
    }

    /**
     * calculate a next app id
     * @return the next id
     */
    private int calculateNextId() {
        int id;
        for (id = 1; id < maxApp; id++){
            var app = runningApps.get(id);
            if(app == null){ break; }
        }
        if (id == maxApp){
            throw new IllegalStateException("Number of authorized apps exceed");
        }
        return id;
    }

    /**
     * calculate a next app dockerInstance
     * @param name the app name (short)
     * @return the name-[counter] = dockerInstance
     */
    private String calculateNextDockerInstance(String name){
        requireNonNull(name, "Docker instance can't be null");
        if (name.length() == 0){
            throw new IllegalArgumentException("Docker instance should not be \"\"");
        }
        var nextInstance = instanceApps.getOrDefault(name, 0) + 1;
        instanceApps.put(name, nextInstance);
        return name + "-" + nextInstance;
    }

    /**
     * delete an app from the running apps list and add it to the stopped apps list
     * @param id the id of the app to stop
     * @return the stopped app
     * @throws NullPointerException when the id is invalid (not founded)
     */
    public ApplicationData stop(int id) throws NullPointerException {
        requireNonNull(runningApps.get(id));
        var app = runningApps.remove(id);
        stoppedApps.put(id, app);
        return app;
    }

    /**
     * delete an app from the stopper apps list
     * @param id the id of the app to kill
     * @return the killed app
     * @throws NullPointerException when the id is invalid (not founded)
     */
    public ApplicationData kill(int id) throws NullPointerException {
        requireNonNull(stoppedApps.get(id));
        return stoppedApps.remove(id);
    }

    /**
     * delete all apps from the running apps list and add them to the stopped apps list
     * @return the list of all stopped apps
     */
    public Collection<ApplicationData> killAll(){
        var cpy = new HashMap<>(stoppedApps);
        cpy.forEach((k, v) -> stoppedApps.remove(k));
        return cpy.values();
    }

    /**
     * delete all apps from the stopped apps list
     * @return the list of all killed apps
     */
    public Collection<ApplicationData> stopAll(){
        var cpy = new HashMap<>(runningApps);
        cpy.forEach((k, v) -> stop(k));
        return cpy.values();
    }

    /**
     * get a list of running apps
     * @return the list of all running apps
     */
    public List<ApplicationData> getAppDatas(){
        return runningApps.values().stream().collect(Collectors.toList());
    }

    /**
     * transform the list of running apps to a String
     * @return the string of the runningApps
     */
    @Override
    public String toString() {
        return runningApps.values().stream()
                .map(ApplicationData::toString)
                .collect(Collectors.joining(",\n", "[\n", "\n]"));
    }
}