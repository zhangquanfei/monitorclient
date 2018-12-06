package monitorcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class FileCompare {

    Mysql mysql=new Mysql();
    /**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     */
    public static List readTxtFile(String filePath){
        List list=new ArrayList();
        try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    list.add(lineTxt);
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list;

    }
    public static void main(String argv[]) {

    }


    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    public  static String getLinuxLocalIp(){
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

    public  void compare(String newfile,String copyfile) {
        System.out.println(newfile+"     "+copyfile);
       // String filePath = "C:\\feitest4\\a.txt";//原来的文件
        List yuanlist = readTxtFile(copyfile);
        //String filePath2 = "C:\\a\\a.txt";//进行对比的文件，现在的文件
        List xianlist = readTxtFile(newfile);


        if (yuanlist.size() > xianlist.size()) {
            for (int i = 0; i < yuanlist.size(); i++) {
                if (!xianlist.contains(yuanlist.get(i))) {

                    if (i <= xianlist.size()-1){
                        System.out.println("第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + xianlist.get(i));
                        Date day=new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mysql.addCheckoutData(getLinuxLocalIp(),"第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + xianlist.get(i),df.format(day));
                    }else {
                        System.out.println("第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + "不存在");
                        Date day=new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mysql.addCheckoutData(getLinuxLocalIp(),"第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + "不存在",df.format(day));
                    }

                }
            }
        } else {
            for (int i = 0; i < xianlist.size(); i++) {
                if (!yuanlist.contains(xianlist.get(i))) {
                    if (i <= yuanlist.size()-1){
                        System.out.println("第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + xianlist.get(i));
                        Date day=new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mysql.addCheckoutData(getLinuxLocalIp(),"第" + (i+1) + "行不同，" + "备份文件：" + yuanlist.get(i) +" 现文件：" + xianlist.get(i),df.format(day));
                    }else {
                        System.out.println("第" + (i+1) + "行不同，" + "备份文件：" +"不存在" +" 现文件：" + xianlist.get(i));
                        Date day=new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mysql.addCheckoutData(getLinuxLocalIp(),"第" + (i+1) + "行不同，" + "备份文件：" +"不存在" +" 现文件：" + xianlist.get(i),df.format(day));
                    }
                }
            }

//        for(int i = 0; i < list.size(); i++) {
//            if(!list2.contains(list.get(i))){
//                System.out.println("第"+i+1+"行不同，"+"不相同内容：现文件："+list.get(i));
//            }
//        }
        }
    }
}
