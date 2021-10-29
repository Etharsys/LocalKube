package fr.uge.localkube;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDataCreatorTest {
    @Test @Tag("toApplicationData")
    public void shouldGetErrorWhenProcessingWithNullJsonData(){
        assertThrows(NullPointerException.class,
                () -> new ApplicationDataCreator().toApplicationData(null));
    }

    @Test @Tag("toApplicationData")
    public void shouldGetErrorWhenProcessingWithJsonDataNoEnoughBig(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationDataCreator().toApplicationData(""));
    }

    @Test @Tag("toApplicationData")
    public void shouldRunWhenProcessingWithValidJsonData() {
        ApplicationData test = null;
        try {
            test = new ApplicationDataCreator().toApplicationData("{\"app\": \"hello:8081\"}");
        } catch (LKArgumentsException e) {
            System.out.println("Problem in test \"shouldRunWhenProcessingWithValidJsonData\"");
        }
        assertEquals("{\n\tid:1,\n\tapp:hello:8081,\n\tport:8081,\n\tdocker-instance:hello-1\n}",
                test.toString());
    }

    @Test @Tag("add")
    public void shouldGetErrorWhenProcessingWithNullAppToAdd(){
        assertThrows(NullPointerException.class,
                () -> new ApplicationDataCreator().add(null));
    }

    @Test @Tag("add")
    public void shouldAddWithValidApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertEquals(app, test.stop(1));
    }

    @Test @Tag("add")
    public void shouldGetErrorWhenProcessingWithExistingRunningApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertThrows(LKArgumentsException.class,
                () -> test.add(app));
    }

    @Test @Tag("add")
    public void shouldGetErrorWhenProcessingWithExistingStoppedApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        test.stop(1);
        assertThrows(LKArgumentsException.class,
                () -> test.add(app));
    }

    @Test @Tag("stop")
    public void shouldStopWithValidApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        test.stop(1);
        assertEquals("[\n\n]", test.toString());
    }

    @Test @Tag("stop")
    public void shouldGetErrorWhenProcessingWithUnexistingRunningApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertThrows(NullPointerException.class,
                () -> test.stop(2));
    }

    @Test @Tag("kill")
    public void shouldKillWithValidApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        test.stop(1);
        test.kill(1);
        test.add(app);
        assertEquals(app, test.stop(1));
    }

    @Test @Tag("kill")
    public void shouldGetErrorWhenProcessingWithUnexistingStoppedApp(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertThrows(NullPointerException.class,
                () -> test.kill(1));
    }

    @Test @Tag("getAppDatas")
    public void shouldGetAllDataOfRunningsApps(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertEquals(List.of(app), test.getAppDatas());
    }

    @Test @Tag("stopAll")
    public void shouldStopAllAppsWhenProcessing(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        var app2 = new ApplicationData(2, "hello:8082", 8082, "hello-2");
        test.add(app);
        test.add(app2);
        test.stopAll();
        assertEquals(Collections.emptyList(), test.getAppDatas());
    }

    @Test @Tag("killAll")
    public void shouldKillAllAppsWhenProcessing(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        var app2 = new ApplicationData(2, "hello:8082", 8082, "hello-2");
        test.add(app); test.add(app2);
        test.stopAll();
        test.killAll();
        test.add(app); test.add(app2);
        assertAll(
                () -> assertEquals(app, test.stop(1)),
                () -> assertEquals(app2, test.stop(2))
        );
    }

    @Test @Tag("toString")
    public void shouldHaveTheGreatDisplayOfRunningsApps(){
        var test = new ApplicationDataCreator();
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        test.add(app);
        assertEquals("[\n{\n\tid:1,\n\tapp:hello:8081,\n\tport:8081,\n\tdocker-instance:hello-1\n}\n]", test.toString());
    }
}