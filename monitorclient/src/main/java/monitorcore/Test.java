package monitorcore;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import rpc.AddMonitorImpl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class Test {

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
    public static void main(String[] args) throws Exception{
       // System.out.println(getLinuxLocalIp());

      //  File f = new File(Test.class.getResource("/").getPath());
       // System.out.println(f);


        //1.读取xml文档，返回Document对象
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File("a.xml"));
        //2.得到根标签
        Element rootElem = doc.getRootElement();
        System.out.println(rootElem.getName());

        //3.获取根标签下的snippet节点
        Element portElem = rootElem.element("port");
        System.out.println(portElem.getName()+" "+portElem.getStringValue());

        Element zookeeperElem = rootElem.element("zookeeper");
        System.out.println(zookeeperElem.getName()+" "+zookeeperElem.getStringValue());



//        List< WatchEvent.Kind<Path>> sq = new LinkedList<WatchEvent.Kind<Path>>();
//        sq.clear();
//        sq.add(StandardWatchEventKinds.ENTRY_CREATE);
//        sq.add(StandardWatchEventKinds.ENTRY_MODIFY);
//        sq.add(StandardWatchEventKinds.ENTRY_DELETE);
//
//        // watchRecursiveRafaelNadal.watchRNDir(Paths.get("/home/quanfei"),"/home/quanfei/app;/home/quanfei/rvm",events);
//        MonitorThread thread1 = new MonitorThread();
//        thread1.setEvents(sq);
//        thread1.setPath("/home/quanfei");
//        thread1.setExcludes("/home/quanfei/app;/home/quanfei/rvm");
//        thread1.start();


    }
}
