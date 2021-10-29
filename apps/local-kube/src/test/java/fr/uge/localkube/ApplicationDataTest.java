package fr.uge.localkube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDataTest {

    @Test
    public void shouldGetErrorWhenAddingNegativId(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationData(-1, "hello:8081", 8081, "hello-1"));
    }

    @Test
    public void shouldGetErrorWhenAddingPortInf8080(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationData(1, "hello:8080", 8080, "hello-1"));
    }

    @Test
    public void shouldGetErrorWhenAddingNullAppName(){
        assertThrows(NullPointerException.class,
                () -> new ApplicationData(1, null, 8081, "hello-1"));
    }

    @Test
    public void shouldGetErrorWhenAddingNullDockerInstance(){
        assertThrows(NullPointerException.class,
                () -> new ApplicationData(1, "hello:8081", 8081, null));
    }

    @Test
    public void shouldGetErrorWhenAddingWrongAppFormat(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationData(1, "hello", 8081, "hello-1"));
    }

    @Test
    public void shouldGetErrorWhenAddingNotSameName(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationData(1, "hello:8081", 8081, "nohello-1"));
    }

    @Test
    public void shouldGetErrorWhenAddingNotSamePort(){
        assertThrows(LKArgumentsException.class,
                () -> new ApplicationData(1, "hello:8082", 8081, "hello-1"));
    }

    @Test
    public void shouldRunWithValidArguments(){
        var app = new ApplicationData(1, "hello:8081", 8081, "hello-1");
        assertAll(
                () -> assertEquals("hello-1", app.dockerInstance()),
                () -> assertEquals(1, app.id()),
                () -> assertEquals(8081, app.port()),
                () -> assertEquals("hello:8081", app.app()),
                () -> assertEquals("hello", app.getNameApp()),
                () -> assertEquals("{\n\tid:1,\n\tapp:hello:8081,\n\tport:8081,\n\tdocker-instance:hello-1\n}", app.toString())
        );
    }
}