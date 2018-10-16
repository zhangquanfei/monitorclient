package monitorcore;


import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
public class WatchRecursiveRafaelNadal {
    Mysql mysql = new Mysql();


//    // 频道1
//    private static final String CHANNEL1 = "res1";
//    // 频道2
//    private static final String CHANNEL2 = "res2";
//
//    // 通过频道1推送给前台的变量1
//    private static int number1 = 0;
//    // 通过频道2推送给前台的变量2
//    private static int number2 = 100;
//
//
//    public static String sss = "";





    private WatchService watchService;
    private final Map<WatchKey, Path> directories = new HashMap<>();

    private void registerPath(Path path,List< WatchEvent.Kind<Path>> events) throws IOException {
        if(events.size() == 1){
            //register the received path
            WatchKey key = path.register(watchService, events.get(0));
            //store the key and path
            directories.put(key, path);
        }
        if(events.size() == 2){
            //register the received path
            WatchKey key = path.register(watchService, events.get(0),events.get(1));
            //store the key and path
            directories.put(key, path);
        }
        if(events.size() == 3){
            //register the received path
            WatchKey key = path.register(watchService, events.get(0),events.get(1),events.get(2));
            //store the key and path
            directories.put(key, path);
        }

//        //register the received path
//        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
//                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
//        directories.put(key, path);

    }
    private void registerTree(Path start,final String exclude,List< WatchEvent.Kind<Path>> events) throws IOException {
        System.out.println("thread.....333......");
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {

                if ("".equals(exclude) || exclude == null) {
                    System.out.println("register:" + dir);
                    registerPath(dir, events);
                }else {


                    String[] ss = exclude.split(";");

                    boolean b = false;

                    for (int i = 0; i < ss.length; i++) {
                        if (dir.toString().equals(ss[i])) {
                            b = true;
                        }
                        if (dir.toString().length() > ss[i].length()) {
                            if (dir.toString().contains(ss[i])) {
                                b = true;
                            }
                        }
                    }
                    if (b == false) {
                        System.out.println("register:" + dir);
                        registerPath(dir, events);
                        b = false;
                    }


                }

                return FileVisitResult.CONTINUE;
            }
        });

    }

    public void watchRNDir(Path start,String exclude,List< Kind<Path>> events) throws IOException, InterruptedException {
        System.out.println("thread.....222......");
        watchService = FileSystems.getDefault().newWatchService();

        registerTree(start,exclude,events);

        //start an infinite loop
        while (true) {

            //retrieve and remove the next watch key
            final WatchKey key = watchService.take();

//            if (data.getMap().get("a") != null){
//                break;
//            }

            //get list of events for the watch key
            for (WatchEvent<?> watchEvent : key.pollEvents()) {

                //get the kind of event (create, modify, delete)
                final Kind<?> kind = watchEvent.kind();

                //get the filename for the event
                final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                final Path filename = watchEventPath.context();

                //handle OVERFLOW event
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                //handle CREATE event
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    final Path directory_path = directories.get(key);
                    final Path child = directory_path.resolve(filename);

                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addEvent(getLinuxLocalIp(),kind.toString(),child.toString(),df.format(day));

                    System.out.println(kind + "-> " + child);
                    //this.sss = kind + "-> " + child;
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerTree(child,exclude,events);
                    }
                }

                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    final Path directory_path = directories.get(key);
                    final Path child = directory_path.resolve(filename);
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addEvent(getLinuxLocalIp(),kind.toString(),child.toString(),df.format(day));
                    System.out.println(kind + "-> " + child);
                    //this.sss = kind + "-> " + child;
                }

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    final Path directory_path = directories.get(key);
                    final Path child = directory_path.resolve(filename);
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addEvent(getLinuxLocalIp(),kind.toString(),child.toString(),df.format(day));
                    System.out.println(kind + "-> " + child);


                    //this.sss = kind + "-> " + child;





                }

            }

            //reset the key
            boolean valid = key.reset();

            //remove the key if it is not valid
            if (!valid) {
                directories.remove(key);

                //there are no more keys registered
                if (directories.isEmpty()) {
                    break;
                }
            }
        }
        watchService.close();
    }

    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    private static String getLinuxLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                ip = ipaddress;
                                //System.out.println(ipaddress);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println("获取ip地址异常");
            ip = "127.0.0.1";
            ex.printStackTrace();
        }
        //System.out.println("IP:"+ip);
        return ip;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List< WatchEvent.Kind<Path>> sq = new LinkedList<WatchEvent.Kind<Path>>();
        sq.clear();
        sq.add(StandardWatchEventKinds.ENTRY_CREATE);
        sq.add(StandardWatchEventKinds.ENTRY_MODIFY);
        sq.add(StandardWatchEventKinds.ENTRY_DELETE);
        WatchRecursiveRafaelNadal watchRecursiveRafaelNadal = new WatchRecursiveRafaelNadal();
        watchRecursiveRafaelNadal.watchRNDir(Paths.get("/home/quanfei"),"/home/quanfei/app;/home/quanfei/rvm",sq);

    }
}






