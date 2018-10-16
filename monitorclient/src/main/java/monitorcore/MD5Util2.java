package monitorcore;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class MD5Util2 {
    Mysql mysql = new Mysql();
    public static  String masterdir = "";
    public static Map<String,String> map = new HashMap<String, String>();
    public static Map<String,String> mapbeifen = new HashMap<String, String>();
    public static void copyDir(String oldPath) throws IOException {
        File file = new File(oldPath);
        String[] filePath = file.list();
        for (int i = 0; i < filePath.length; i++) {
            if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                copyDir(oldPath  + file.separator  + filePath[i]);
            }
            if (new File(oldPath  + file.separator + filePath[i]).isFile()) {
                copyFile(oldPath + file.separator + filePath[i]);
            }
        }
    }
    public  static void copyFile(String oldPath) throws IOException {

        File file = new File(oldPath);
        if (!file.exists()) {
            System.out.println("不存在");
        }
        String md5 = getFileMD5String(file);

        map.put(file.getPath(),md5);
        //addmd5data(file.getPath(),md5);
        //System.out.println(file.getPath()+"  md5:" + md5 );

    }

    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static MessageDigest messagedigest = null;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(MD5Util2.class.getName()
                    + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 生成字符串的md5校验值
     *
     * @param s
     * @return
     */
    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param password  要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     * @return
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /**
     * 生成文件的md5校验值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(messagedigest.digest());
    }



    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }



    //声明Connection对象
    static Connection con;
    //驱动程序名
    static String driver = "com.mysql.jdbc.Driver";
    //URL指向要访问的数据库名mydata
    static String url = "jdbc:mysql://192.168.99.73:3306/db_ssm?autoReconnect=true";
    //MySQL配置时的用户名
    static String user = "root";
    //MySQL配置时的密码
    static String password = "root";

    static Statement statement;

    static{
        try {
            //加载驱动程序
            Class.forName(driver);
            //1.getConnection()方法，连接MySQL数据库！！
            con = DriverManager.getConnection(url,user,password);
            statement = con.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void  query(String masterdir){

        try {
            String sql = "SELECT * FROM md5data WHERE masterdir='"+masterdir+"'";

            ResultSet rs = statement.executeQuery(sql);

            while(rs.next()){
                String direction = rs.getString("direction");
                String data = rs.getString("data");
                mapbeifen.put(direction,data);
                // System.out.println(direction);

            }

        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }



    /**
     * 根据map返回key和value的list
     *
     * @param map
     * @param isKey
     *            true为key,false为value
     * @return
     */
    public static List<String> getListByMap(Map<String, String> map,
                                            boolean isKey) {
        List<String> list = new ArrayList<String>();

        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (isKey) {
                list.add(key);
            } else {
                list.add(map.get(key));
            }
        }

        return list;
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

    public static void main(String [] args) throws IOException {
        MD5Util2 md5Util2 = new MD5Util2();
        md5Util2.checkout("/home/quanfei/app/zookeeper-3.4.5/bin","/home/quanfei/copytest");
    }
    public  void checkout(String nowfile,String copyfile){

        map.clear();
        mapbeifen.clear();
        System.out.println("nowfilenowfilenowfile:::"+nowfile);
        System.out.println("copyfilecopyfilecopyfile:::"+copyfile);
        //masterdir = "C:\\a";
        //masterdir = masterdir.replace("\\",".");


        try {
            copyDir(nowfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> xiankeyList = getListByMap(map, true);
        List<String> xianvaluesList = getListByMap(map, false);


        query(copyfile);
        List<String> beifeikeyList = getListByMap(mapbeifen, true);
        List<String> beifenvaluesList = getListByMap(mapbeifen, false);




        Boolean derectionKey = true;
        int xiansize = xiankeyList.size();
        int beifensize = beifeikeyList.size();
        if (xiansize == beifensize){
            for (int i =0 ;i< beifensize;i++){
                if (!xiankeyList.contains(beifeikeyList.get(i))){
                    System.out.println("丢失的文件："+beifeikeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"丢失的文件："+beifeikeyList.get(i),df.format(day));
                }
                if(!beifeikeyList.contains(xiankeyList.get(i))){
                    System.out.println("增加的文件："+xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"增加的文件："+xiankeyList.get(i),df.format(day));
                }

                //--1
                if (!xianvaluesList.contains(beifenvaluesList.get(i))){
                    System.out.println("文件改变："+xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"文件改变："+xiankeyList.get(i),df.format(day));

                }


            }
        }else if (xiansize > beifensize){
            for (int i=0;i<xiansize;i++){
                if (i< beifensize && !xiankeyList.contains(beifeikeyList.get(i))){
                    System.out.println("丢失的文件："+beifeikeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"丢失的文件："+beifeikeyList.get(i),df.format(day));
                    derectionKey = false;
                }
                if (!beifeikeyList.contains(xiankeyList.get(i))){
                    System.out.println("增加的文件："+xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"增加的文件："+xiankeyList.get(i),df.format(day));
                    derectionKey = false;
                }

                //--1
                if(i<beifensize) {
                    if (!xianvaluesList.contains(beifenvaluesList.get(i))) {
                        System.out.println("文件改变：" + xiankeyList.get(i));
                        Date day=new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mysql.addCheckoutData(getLinuxLocalIp(),"文件改变：" + xiankeyList.get(i),df.format(day));
                    }
                }
            }
        }else if(xiansize < beifensize){
            for (int i = 0;i< beifensize;i++){
                if (!xiankeyList.contains(beifeikeyList.get(i))){
                    System.out.println("丢失的文件："+beifeikeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"丢失的文件："+beifeikeyList.get(i),df.format(day));
                    derectionKey = false;
                }
                if(i < xiansize && !beifeikeyList.contains(xiankeyList.get(i))){
                    System.out.println("增加的文件："+xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"增加的文件："+xiankeyList.get(i),df.format(day));
                    derectionKey = false;
                }

                if (!xianvaluesList.contains(beifenvaluesList.get(i))) {
                    System.out.println("文件改变：" + xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"文件改变：" + xiankeyList.get(i),df.format(day));
                }


            }
        }
/*
        if (derectionKey == true){
            for (int i=0;i< xiansize;i++){
                if (!xianvaluesList.contains(beifenvaluesList.get(i))){
                    System.out.println("文件改变："+xiankeyList.get(i));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mysql.addCheckoutData(getLinuxLocalIp(),"文件改变："+xiankeyList.get(i),df.format(day));
                }
            }
        }*/
    }
}
