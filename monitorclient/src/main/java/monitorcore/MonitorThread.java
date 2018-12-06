package monitorcore;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.LinkedList;
import java.util.List;

public class MonitorThread extends Thread{
    private String name;
    private String path;
    List< WatchEvent.Kind<Path>> events;
    private String excludes;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<WatchEvent.Kind<Path>> getEvents() {
        return events;
    }

    public void setEvents(List<WatchEvent.Kind<Path>> events) {
        this.events = events;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }







    @Override
    public void run() {

        WatchRecursiveRafaelNadal watchRecursiveRafaelNadal = new WatchRecursiveRafaelNadal();
        try {
            watchRecursiveRafaelNadal.watchRNDir(Paths.get(path),excludes,events);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    @Override
    public String toString() {
        return super.getName();
    }
}