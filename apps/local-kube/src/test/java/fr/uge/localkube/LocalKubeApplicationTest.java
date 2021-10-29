package fr.uge.localkube;

import com.google.cloud.tools.jib.api.CacheDirectoryCreationException;
import com.google.cloud.tools.jib.api.InvalidImageReferenceException;
import com.google.cloud.tools.jib.api.RegistryException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RestClientTest(LocalKubeApplication.class)
class LocalKubeApplicationTest {

    @Test @Tag("appDatas")
    public void shouldStartApp() throws RegistryException, InterruptedException,
            IOException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        assertEquals("[\n{\n\tid:1,\n\tapp:helloworld:8081,\n\tport:8081,\n\tdocker-instance:helloworld-1\n}\n]",
                LKApp.getAppDatas().toString());
    }

    @Test @Tag("start")
    public void shouldGetErrorWhenStartingWithWrongJson() throws RegistryException, InterruptedException,
            IOException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\" \"helloworld:8081\"}");
        assertEquals("[\n\n]", LKApp.getAppDatas().toString());
    }

    @Test @Tag("start")
    public void shouldGetErrorWhenStartingWithUnexistingName() throws RegistryException, InterruptedException,
            IOException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"a:8081\"}");
        assertEquals("[\n\n]", LKApp.list());
    }

    @Test @Tag("start")
    public void shouldGetErrorWhenStartingWithInvalidPort() throws RegistryException, InterruptedException,
            IOException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8080\"}");
        assertEquals("[\n\n]", LKApp.getAppDatas().toString());
    }

    @Test @Tag("start")
    public void shouldGetErrorWhenStartingWithPortAlreadyUsed() throws RegistryException, InterruptedException,
            IOException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        assertEquals("[\n{\n\tid:1,\n\tapp:helloworld:8081,\n\tport:8081,\n\tdocker-instance:helloworld-1\n}\n]", LKApp.list());
    }

    @Test @Tag("stop")
    public void shouldGetErrorWhenStoppingUnextistingApp() throws IOException {
        var LKApp = new LocalKubeApplication();
        assertEquals("[ERROR] --- The requested app to stop with id: 1 is not running", LKApp.stop("{\"id\": 1}"));
    }

    @Test @Tag("stop")
    public void shouldGetErrorWhenStoppingWithWrongJson() throws IOException {
        var LKApp = new LocalKubeApplication();
        assertEquals("[ERROR] --- Input format for id is Integer, get : {\"id\": \"1\"}", LKApp.stop("{\"id\": \"1\"}"));
    }

    @Test @Tag("stop")
    public void shouldStopARunningApp() throws IOException, InterruptedException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        LKApp.stop("{\"id\": 1}");
        assertEquals("[\n\n]", LKApp.list());
    }

    @Test @Tag("stopall")
    public void shouldStopAll() throws IOException, InterruptedException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        LKApp.start("{\"app\": \"helloworld:8082\"}");
        LKApp.stopAll();
        assertEquals("[\n\n]", LKApp.list());
    }

    @Test @Tag("stopall")
    public void shouldGetErrorWhenNoAppToStop() throws IOException {
        var LKApp = new LocalKubeApplication();
        assertEquals("[ERROR] --- No apps to stop", LKApp.stopAll());
    }

    @Test @Tag("kill")
    public void shouldGetErrorWhenKillingUnextistingApp() throws IOException {
        var LKApp = new LocalKubeApplication();
        assertEquals("[ERROR] --- App with id: 1 is not stopped or does not exist, can't be killed", LKApp.kill("{\"id\": 1}"));
    }

    @Test @Tag("kill")
    public void shouldGetErrorWhenKillingWithWrongJson() throws IOException {
        var LKApp = new LocalKubeApplication();
        assertEquals("[ERROR] --- Input format for id is Integer, get : {\"id\": \"1\"}", LKApp.kill("{\"id\": \"1\"}"));
    }

    @Test @Tag("kill")
    public void shouldKillARunningApp() throws IOException, InterruptedException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        LKApp.stopAll();
        assertEquals("{\n\tid:1,\n\tapp:helloworld:8081,\n\tport:8081,\n\tdocker-instance:helloworld-1\n}", LKApp.kill("{\"id\": 1}"));
    }

    @Test @Tag("killall")
    public void shouldKillAll() throws IOException, InterruptedException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {
        var LKApp = new LocalKubeApplication();
        LKApp.start("{\"app\": \"helloworld:8081\"}");
        LKApp.start("{\"app\": \"helloworld:8082\"}");
        LKApp.stopAll();
        LKApp.killAll();
        assertEquals("[\n\n]", LKApp.list());
    }

    @Test @Tag("killall")
    public void shouldGetErrorWhenNoAppToKill() throws IOException {
        var LKApp = new LocalKubeApplication();
        LKApp.stopAll();
        assertEquals("[ERROR] --- No apps to kill", LKApp.killAll());
    }
}