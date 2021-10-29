package fr.uge.localkube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {
    @Test
    public void shouldGetErrorWhenConstructingLogWhenAppIsNull(){
        assertThrows(NullPointerException.class,
                () -> new Log(null, ""));
    }

    @Test
    public void shouldGetErrorWhenConstructingLogWhenLogsIsNull(){
        assertThrows(NullPointerException.class,
                () -> new Log("test", null));
    }

    @Test
    public void shouldGetErrorWhenConstructingLogWhenAppIsVoid(){
        assertThrows(LKArgumentsException.class,
                () -> new Log("", "test"));
    }

    @Test
    public void shouldGetValuesWhenConstructingLogWhenLogsIsVoid(){
        var log = new Log("test1", "").toString();
        assertEquals("{\n\tapp:" + "test1" + ",\n\tlogs:" + "" + "\n}", log);
    }

    @Test
    public void shouldGetAppNameWhenUsingGetApp(){
        var log = new Log("test1", "").getApp();
        assertEquals("test1", log);
    }

    @Test
    public void shouldGetLogsWhenUsingGetLogs(){
        var log = new Log("test1", "test2");
        assertEquals("test2", log.getLogs());
    }

    @Test
    public void shouldDoSomethingWhenUsingSetApp(){
        var log = new Log("test1", "test2");
        log.setApp("test1");
        assertEquals("test1", log.getApp());
    }

    @Test
    public void shouldDoSomethingWhenUsingSetLogs(){
        var log = new Log("test1", "test2");
        log.setLogs("test2");
        assertEquals("test2", log.getLogs());
    }

    @Test
    public void shouldGetDisplayWhenUsingToString(){
        var log = new Log("test1", "").toString();
        assertEquals("{\n\tapp:" + "test1" + ",\n\tlogs:" + "" + "\n}", log);
    }
}