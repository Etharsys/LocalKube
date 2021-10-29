package fr.uge.localkube;

import com.google.cloud.tools.jib.api.*;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@SpringBootApplication
@RestController
@RequestMapping("/app")
@Component
public class LocalKubeApplication {
	/**
	 * Class of LocalKube which handle '/app' request (plus is main app)
	 * @field appList refer to data apps
	 * @field thread the code to run when LocalKube end
	 */
	private final ApplicationDataCreator appDatas;
	private final Thread LKend = new Thread(){
		/**
		 * run this code when exiting LocalKube (stop all running apps)
		 */
		@Override
		public void run() {
			try {
				stopAll();
				//killAll(); //pas obligatoire
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	private final String additionalPath = Arrays.stream(System.getProperty("user.dir").split("/")).map(e -> {
			if (e.equals("local-kube")){
				return "../../";
			}
			return "";
		}).collect(Collectors.joining(""));

	/**
	 * Constructor for LocalKubeApplication
	 */
	public LocalKubeApplication(){
		appDatas = new ApplicationDataCreator();
	}

	/**
	 * main method, start local-kube app
	 * @param args arguments from cmd line (not needed)
	 */
	public static void main(String[] args) {
		new SpringApplication(LocalKubeApplication.class, LocalKubeLogs.class).run(args);
	}

	/**
	 * get the data app list
	 * @return the data app list
	 */
	public ApplicationDataCreator getAppDatas(){
		return appDatas;
	}

	/**
	 * get the launched app list (get request)
	 * @return the list on expected format to be print
	 */
	@GetMapping("/list")
	public String list () {
		return appDatas.toString();
	}

	/**
	 * request to post a new application
	 * @param jsonString the application to start on json entry format
	 * @return the application on json application format
	 * @throws RuntimeException for containerize
	 * @throws IOException for start in ProcessBuilder and addLayer in Jib
	 * @throws InvalidImageReferenceException for Jib.from
	 * @throws InterruptedException for waitFor in ProcessBuilder and containerize
	 * @throws RegistryException for containerize
	 * @throws CacheDirectoryCreationException for containerize
	 */
	@PostMapping(path="/start")
	public String start (@RequestBody String jsonString) throws RuntimeException, IOException, InvalidImageReferenceException,
			InterruptedException, RegistryException, CacheDirectoryCreationException {
		Runtime.getRuntime().removeShutdownHook(LKend);
		Runtime.getRuntime().addShutdownHook(LKend);

		try {
			ApplicationData app = appDatas.toApplicationData(jsonString);
			setJibFrom(app);
			applyProcess(app);
			appDatas.add(app);
			return app.toString();
		} catch (LKArgumentsException e) {
			return e.toString();
		} catch (ExecutionException e) {
			return new LKArgumentsException("The specified application in " + jsonString + " is not found").toString();
		}
	}

	/**
	 * Use the Jib library to create an image of an app
	 * @param app the futur image
	 * @throws IOException for addLayer in JibRestClient
	 * @throws InvalidImageReferenceException for Jib.from
	 * @throws InterruptedException for containerize
	 * @throws ExecutionException for containerize
	 * @throws RegistryException for containerize
	 * @throws CacheDirectoryCreationException for containerize
	 */
	private void setJibFrom (ApplicationData app) throws InvalidImageReferenceException, IOException, InterruptedException,
			ExecutionException, RegistryException, CacheDirectoryCreationException {
		Jib.from("openjdk:15") //Local version
				.addLayer(Arrays.asList(Paths.get(additionalPath + "apps/" + app.getNameApp() + ".jar")), AbsoluteUnixPath.get("/"))
				.setEntrypoint("java", "-jar", "-Dserver.port=" + app.port(), app.getNameApp() + ".jar")
				.containerize(Containerizer.to(TarImage.at(Paths.get("docker-images/" + app.dockerInstance() + ".tar")).named(app.dockerInstance())));
	}

	/**
	 * apply all needed process to start or 'restart' an image
	 * @param app the app image to be started or restarted
	 * @throws IOException for start in ProcessBuilder
	 * @throws InterruptedException for waitFor in ProcessBuilder
	 */
	private void applyProcess (ApplicationData app) throws IOException, InterruptedException {
		new ProcessBuilder("docker", "load", "-i", "docker-images/" + app.dockerInstance() + ".tar").inheritIO().start().waitFor();
		var exist = new ProcessBuilder("docker", "ps", "-a", "-f", "name=" + app.dockerInstance()).start();
		var count = new BufferedReader(new InputStreamReader(exist.getInputStream())).lines().count();
		if (count == 2) {
			new ProcessBuilder("docker", "start", app.dockerInstance()).inheritIO().start();
		}
		else {
			new ProcessBuilder("docker", "run", "-p", app.port()+":"+app.port(),
					"--name", app.dockerInstance(), app.dockerInstance()).inheritIO().start();
		}
	}

	/**
	 * request to stop an application
	 * @param idS the id of the application to be stop on json format {"id":[id]}
	 * @return the application on json application format, an error if the app does not exist
	 * @throws IOException for start in ProcessBuilder
	 */
	@PostMapping(path="/stop")
	public String stop(@RequestBody String idS) throws IOException {
		var id = idS.split(":")[1];
		try {
			ApplicationData app = appDatas.stop(Integer.parseInt(id.substring(1, id.length()-1)));
			new ProcessBuilder("docker", "stop", app.dockerInstance()).inheritIO().start();
			System.out.println("app stopped :\n" + app.toStringStop());
			return app.toStringStop();
		} catch (NullPointerException e){
			return new LKArgumentsException("The requested app to stop with id:" + id.substring(0, id.length() - 1) + " is not running").toString();
		} catch (NumberFormatException e){
			return new LKArgumentsException("Input format for id is Integer, get : " + idS).toString();
		}
	}

	/**
	 * request to stop all launched application
	 * @return A list of stopped apps, an error if the app does not exist
	 * @throws IOException for start in ProcessBuilder
	 */
	@PostMapping(path="/stopall")
	public String stopAll() throws IOException {
		var apps = appDatas.stopAll();
		if (apps.size() == 0){
			return new LKArgumentsException("No apps to stop").toString();
		}
		System.out.println("Apps stopped :");
		apps.forEach(System.out::println);
		for (var app : apps){
			new ProcessBuilder("docker", "stop", app.dockerInstance()).inheritIO().start();
		}
		return apps.toString();
	}

	/**
	 * request to kill a stopped application
	 * @param idS the id of the application to be kill on json format {"id":[id]}
	 * @return the application on json application format, an error if the app does not exist
	 * @throws IOException for start in ProcessBuilder
	 */
	@PostMapping(path="/kill")
	public String kill(@RequestBody String idS) throws IOException {
		var id = idS.split(":")[1];
		try {
			var app = appDatas.kill(Integer.parseInt(id.substring(1, id.length()-1)));
			new ProcessBuilder("docker", "rm", "--force", app.dockerInstance()).inheritIO().start();
			System.out.println("App killed :\n" + app);
			return app.toString();
		} catch (NullPointerException e){
			return new LKArgumentsException("App with id:" + id.substring(0, id.length() - 1) + " is not stopped or does not exist, can't be killed").toString();
		} catch (NumberFormatException e){
			return new LKArgumentsException("Input format for id is Integer, get : " + idS).toString();
		}
	}

	/**
	 * request to kill all stopped application
	 * @return A list of killed apps, an error if the app does not exist
	 * @throws IOException for start in ProcessBuilder
	 */
	@PostMapping(path="/killall")
	public String killAll() throws IOException {
		var apps = appDatas.killAll();
		if (apps.size() == 0){
			return new LKArgumentsException("No apps to kill").toString();
		}
		System.out.println("Apps killed :");
		apps.forEach(System.out::println);
		for (var app : apps){
			new ProcessBuilder("docker", "rm", "--force", app.dockerInstance()).inheritIO().start();
		}
		return apps.toString();
	}

}
