package rpc;

import com.mysql.jdbc.MysqlParameterMetadata;
import monitorcore.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.*;

public class AddMonitorImpl implements AddMonitorInterface {
    public static List<MonitorThread> list = new ArrayList<MonitorThread>();
    public static List<Test> lists = new ArrayList<Test>();
    public static Map<String, MonitorThread> map = new HashMap<String, MonitorThread>();
    String ss = "";


    @Override
    public String addmonitor(String path, String exclude,  String event,String addOrDel,String row) {

        if ("heartbeat".equals(addOrDel)) {
            System.out.println("heartbeatsuccess");
            ss = "heartbeatsuccess";
        }

        if ("add".equals(addOrDel)) {
            List<WatchEvent.Kind<Path>> sq = new LinkedList<WatchEvent.Kind<Path>>();
            sq.clear();
            for (String s : event.split(",")) {
                if ("Create".equals(s)) {
                    sq.add(StandardWatchEventKinds.ENTRY_CREATE);
                }
                if ("Modify".equals(s)) {
                    sq.add(StandardWatchEventKinds.ENTRY_MODIFY);
                }
                if ("Delete".equals(s)) {
                    sq.add(StandardWatchEventKinds.ENTRY_DELETE);
                }
            }

           MonitorThread thread1 = new MonitorThread();
            thread1.setEvents(sq);
            thread1.setPath(path);
            thread1.setExcludes(exclude);
            thread1.start();

            ss = thread1.toString();

            map.put(thread1.toString(),thread1);
            System.out.println("-------------------"+map);


        }else if ("del".equals(addOrDel)){
            try {
                MonitorThread thread1 = map.get(row);
                thread1.stop();
                map.remove(row);
            }catch (Exception e){
                return ss;
            }
        } else if ("copyfile".equals(addOrDel)){
            CopyFiles copyFiles = new CopyFiles();

            try {

                copyFiles.masterdir = exclude;
                copyFiles.copyDir(path,exclude);
                ss = "success";
            } catch (IOException e) {
                e.printStackTrace();
                ss = "fail";
            }
        }else if ("checkoutfile".equals(addOrDel)){
            if ((new File(path).isDirectory())){
                System.out.println("isDirectoryisDirectoryisDirectoryisDirectoryisDirectory:::");

                Mysql mysql = new Mysql();
                System.out.println("exclude:::"+exclude+"   path:::"+path);
                String copy = mysql.queryjia(exclude,path);
               // System.out.println("copy:::"+copy);
               // String copyFile = path.replace(path.substring(0, path.lastIndexOf("/")),copy);
                System.out.println("copycopycopycopy:::"+copy);
                MD5Util2 md5Util2= new MD5Util2();
                md5Util2.mapbeifen.clear();
                md5Util2.map.clear();
                md5Util2.checkout(path,copy);
                //......
            } else if ((new File(path).isFile())){
                System.out.println("isFileisFileisFileisFileisFile:::");
                Mysql mysql = new Mysql();
                System.out.println("exclude:::"+exclude+"   path:::"+path);
                String copy = mysql.query(exclude.split(":")[0],path);
                System.out.println("copy:::"+copy);
                String copyFile = path.replace(path.substring(0, path.lastIndexOf("/")),copy);
                System.out.println("copyFile:::"+copyFile);
                FileCompare fileCompare = new FileCompare();
                fileCompare.compare(path,copyFile);
            }
        }

        return ss;
    }
}
