package rpc;


import monitorcore.Mysql;
import org.I0Itec.zkclient.ZkClient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class AddMonitorStart {
    //public static ZkClient zkClient = new ZkClient("192.168.33.133:2181,192.168.33.134:2181,192.168.33.135:2181");;

    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    public  static String getLinuxLocalIp() throws SocketException {
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

    public static void main(String[] args) throws Exception {

        new Thread(){
            Mysql mysql = new Mysql();
            @Override
            public void run() {
                try {
                    while (true){
                        sleep(1000);
                        mysql.addRegMachineAndHeart(getLinuxLocalIp(),"10000",String.valueOf(System.currentTimeMillis()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        RPC.Builder builder = new RPC.Builder(new Configuration());

        builder.setBindAddress(getLinuxLocalIp()).setPort(Integer.parseInt("10000")).setProtocol(AddMonitorInterface.class).setInstance(new AddMonitorImpl());

        RPC.Server server = builder.build();

        server.start();

        /*
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File("../conf/monitor-core.xml"));
        //2.得到根标签
        Element rootElem = doc.getRootElement();
        System.out.println(rootElem.getName());

        //3.获取根标签下的snippet节点
        Element portElem = rootElem.element("port");
        String port =portElem.getStringValue();
        System.out.println(portElem.getName()+" "+port);

        Element zookeeperElem = rootElem.element("zookeeper");
        String zookeeper = zookeeperElem.getStringValue();
        System.out.println(zookeeperElem.getName()+" "+zookeeper);

        ZkClient zkClient =  new ZkClient(zookeeper);


        String node = "/monitor/"+getLinuxLocalIp();
        if (!zkClient.exists(node)) {

            zkClient.createPersistent(node, getLinuxLocalIp()+":"+port);

            RPC.Builder builder = new RPC.Builder(new Configuration());

            builder.setBindAddress(getLinuxLocalIp()).setPort(Integer.parseInt(port)).setProtocol(AddMonitorInterface.class).setInstance(new AddMonitorImpl());

            RPC.Server server = builder.build();

            server.start();
        }else{
            System.out.println("fail to setup");
        }
        */




    }
}
