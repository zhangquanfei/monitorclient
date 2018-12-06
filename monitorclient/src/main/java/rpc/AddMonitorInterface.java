package rpc;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

public interface AddMonitorInterface {
    public static final long versionID=1L;
    public String addmonitor(String path, String exclude, String event,String addOrDel,String row);
}
