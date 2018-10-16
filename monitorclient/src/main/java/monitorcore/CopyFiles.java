package monitorcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Enumeration;

public class CopyFiles {

    public static  String masterdir = "";

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
            System.err.println(CopyFiles.class.getName()
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

    public static void addmd5data(String ip,String direction,String data,String masterdir) {
        //String url = "jdbc:mysql://fsmanager:3306/atlas?autoReconnect=true";
        //遍历查询结果集
        try {
            PreparedStatement psql;
            psql = con.prepareStatement("insert into md5data (ip,direction,DATA,masterdir) values(?,?,?,?)");
            psql.setString(1, ip);
            psql.setString(2, direction);
            psql.setString(3, data);
            psql.setString(4, masterdir);
            psql.executeUpdate();
            psql.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

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

//----------------------------------------------


//    public String copyFile1(String infile,String outFile){
//
//        String successOrFail = "success";
//        try{
//
//            FileInputStream fis = new FileInputStream(infile);
//            FileOutputStream fos = new FileOutputStream(outFile);
//            int read = fis.read();
//            while (read!=-1){
//                fos.write(read);
//                read = fis.read();
//            }
//            fis.close();
//            fos.close();
//        }catch (Exception e){
//            successOrFail = "fail";
//            e.printStackTrace();
//        }
//        return successOrFail;
//    }

    public  void copyDir(String oldPath, String newPath) throws IOException {
        File file = new File(oldPath);
        String[] filePath = file.list();
//        System.out.println("filePath:"+filePath);
//        for (int i=0;i<filePath.length;i++){
//            System.out.println("file.separator:"+file.separator+"  filePath:"+filePath[i]);
//        }
        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }

        for (int i = 0; i < filePath.length; i++) {
            if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                copyDir(oldPath  + file.separator  + filePath[i], newPath  + file.separator + filePath[i]);
            }

            if (new File(oldPath  + file.separator + filePath[i]).isFile()) {
                copyFile(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }

        }
    }
    public static void copyFile(String oldPath, String newPath) throws IOException {
        FileChannel inputChannel = null;

        FileChannel outputChannel = null;

        try{
            inputChannel = new FileInputStream(oldPath).getChannel();
            outputChannel = new FileOutputStream(newPath).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }finally{
            inputChannel.close();
            outputChannel.close();
        }



        File file = new File(oldPath);
        if (!file.exists()) {
            System.out.println("不存在");
        }
        String md5 = getFileMD5String(file);


        addmd5data(getLinuxLocalIp(),file.getPath(),md5,masterdir);
       // System.out.println(file.getPath()+"  md5:" + md5 );

//        File oldFile = new File(oldPath);
//        File file = new File(newPath);
//        FileInputStream in = new FileInputStream(oldFile);
//        FileOutputStream out = new FileOutputStream(file);;
//
//        byte[] buffer=new byte[2097152];
//
//        while((in.read(buffer)) != -1){
//            out.write(buffer);
//        }


    }
    public static void main(String[] args) throws IOException {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("请输入源目录：");
//        String sourcePath = sc.nextLine();
//        System.out.println("请输入新目录：");
//        String path = sc.nextLine();
//
//        //String sourcePath = "D://aa";
//        //String path = "D://bb";
//
//        copyDir(sourcePath, path);
        CopyFiles copyFiles = new CopyFiles();
        copyFiles.copyDir("C:\\apache-maven-3.5.0", "C:\\feitest5");
    }

}
