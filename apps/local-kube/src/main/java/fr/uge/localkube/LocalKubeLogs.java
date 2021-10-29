package fr.uge.localkube;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;


@RestController
@RequestMapping("/logs")
@Component
public class LocalKubeLogs {
    /**
     * class which represent a REST service that can give the logs of all the apps, or one in particular,
     * wit a given time.
     * @field LKApp the LocalKubeApplication of the program (needed to actualize the list of apps running).
     * @field app the full app name
     * @field port the app port
     * @field dockerInstance the short app name
     */

    private final LocalKubeApplication LKApp;

    /**
     * Constructor of a LocalKubeLogs, that needs a LocalKubeApplication to work.
     * @param LKApp the localKube application
     */
    @Autowired
    public LocalKubeLogs(LocalKubeApplication LKApp){
        this.LKApp = LKApp;
    }

    /**
     * Get the logs since a time given.
     * @param time the variable time to print the logs since
     * @return a string (JSON-style) of the app logs
     * @throws IOException for the buffered reader
     * @throws SQLException when a problem occur in the DB
     */
    @GetMapping("/{time}")
    @ResponseBody
    public String logPerTime(@PathVariable String time) throws IOException, SQLException { //récupère command docker logs -> parse le res -> envoie dans la bd
        Objects.requireNonNull(time);
        var apps = LKApp.getAppDatas().getAppDatas();
        var logs = new StringJoiner(",\n", "[\n", "\n]");
        for (var app : apps){
            var log = new ProcessBuilder("docker", "logs", "--since=" + time + "m", app.dockerInstance()).start();
            logs.add(logWriting(app, new BufferedReader(new InputStreamReader(log.getInputStream()))));
        }
        return logs.toString();
    }

    /**
     * Get the logs since a time given and filter it by a given name (two types of name) or by index.
     * @param filter the variable to filter with
     * @param time the variable time to print the logs since
     * @return a string (JSON-style) of the app logs
     * @throws IOException for the buffered reader
     * @throws SQLException when a problem occur in the DB
     */
    @GetMapping("/{time}/{filter}")
    @ResponseBody
    public String logPerTimeFiltered(@PathVariable String time, @PathVariable String filter) throws IOException, SQLException { //récupère command docker logs -> parse le res -> envoie dans la bd
        Objects.requireNonNull(time);
        Objects.requireNonNull(filter);
        var apps = LKApp.getAppDatas().getAppDatas();
        var logs = new StringJoiner(",\n", "[\n", "\n]");
        for (var app : apps){
            if (String.valueOf(app.id()).equals(filter) || app.dockerInstance().equals(filter) || app.app().equals(filter)){
                var log = new ProcessBuilder("docker", "logs", "--since=" + time + "m", app.dockerInstance()).start();
                logs.add(logWriting(app, new BufferedReader(new InputStreamReader(log.getInputStream()))));
            }
        }
        return logs.toString();
    }

    /**
     * Get the logs since a time given and filter it by a given name (two types of name) or by index.
     * @param by the variable to focus on
     * @param filter the variable to filter with
     * @param time the variable time to print the logs since
     * @return a string (JSON-style) of the app logs
     * @throws IOException for the buffered reader
     * @throws SQLException when a problem occur in the DB
     */
    @GetMapping("/{time}/{by}/{filter}")
    @ResponseBody
    public String logPerTimeByFilter(@PathVariable String time, @PathVariable String by, @PathVariable String filter) throws IOException, SQLException { //récupère command docker logs -> parse le res -> envoie dans la bd
        Objects.requireNonNull(time);
        Objects.requireNonNull(by);
        Objects.requireNonNull(filter);
        var apps = LKApp.getAppDatas().getAppDatas();
        var logs = new StringJoiner(",\n", "[\n", "\n]");
        for (var app : apps){
            if ((String.valueOf(app.id()).equals(filter) && by.equals("byId")) || (app.dockerInstance().equals(filter) && by.equals("byInstance")) || (app.app().equals(filter) && by.equals("byApp")))  {
                var log = new ProcessBuilder("docker", "logs", "--since=" + time + "m", app.dockerInstance()).start();
                logs.add(logWriting(app, new BufferedReader(new InputStreamReader(log.getInputStream()))));
            }
        }
        return logs.toString();
    }


    /**
     * Write the logs in a human-readable way (JSON-style) to be returned.
     * @param app the concern app to print the logs of
     * @param reader the text containing the logs
     * @return a string of the app logs
     * @throws IOException for the buffered reader
     * @throws SQLException when a problem occur in the DB
     */
    private static String logWriting(ApplicationData app, BufferedReader reader) throws IOException, SQLException {
        Objects.requireNonNull(app);
        Objects.requireNonNull(reader);
        var ret = "{\n\tid:" + app.id() + ",\n\tapp:" + app.app() + ",\n\tport:" + app.port()
                + ",\n\tdocker-instance:" + app.dockerInstance();
        var sj = parsingLogs(reader);
        logWritingInDB(app, sj);
        return ret + sj.toString();
    }

    /**
     * Write the logs in the SQLite database, using JDBC and JDBI
     * @param app the concern app to print the logs of
     * @param sj the text containing the logs
     * @throws SQLException when a problem occur in the DB
     */
    private static void logWritingInDB(ApplicationData app, StringJoiner sj) throws SQLException {
        Objects.requireNonNull(app);
        Objects.requireNonNull(sj);
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:logs/logs.db");
        List<Log> logs = jdbi.withHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS log");
            handle.execute("CREATE TABLE log (app VARCHAR PRIMARY KEY, logs VARCHAR)");
            handle.registerRowMapper(ConstructorMapper.factory(Log.class));
            handle.createUpdate("INSERT INTO log(app, logs) VALUES (:app, :logs)").bindBean(new Log(app.app(), sj.toString())).execute();
            return handle.createQuery("SELECT * FROM log ORDER BY app").mapTo(Log.class).list();
        });
        System.out.println(logs.get(0));
    }

    /**
     * Parse the logs to build a string, concatenating all the logs get in the given times.
     * @param reader the text containing the logs
     * @return a StringJoiner concatenating the logs
     * @throws IOException for the buffered reader
     */
    private static StringJoiner parsingLogs(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
        var sj = new StringJoiner("\n\t", "\n\tmessage :\n\t", "\n}");
        for (var line = reader.readLine(); line != null; line = reader.readLine()){
            var message = line.split("INFO .* : ");
            if (message.length >= 2) {
                sj.add("[" + message[0].substring(0, message[0].length() - 2) + "] : " + message[1]);
            }
        }
        return sj;
    }

}
