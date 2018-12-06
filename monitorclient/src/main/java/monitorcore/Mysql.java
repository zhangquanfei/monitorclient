package monitorcore;

import java.sql.*;

public class Mysql {
    //驱动程序名
    static String driver = "com.mysql.jdbc.Driver";
    //MySQL配置时的用户名
    static String user = "root";
    //MySQL配置时的密码
    static String password = "root";

    //声明Connection对象
    static Connection mysql_con;
    static Statement mysql_statement;



    static {
        String mysql_url = "jdbc:mysql://192.168.99.18:3306/db_ssm?autoReconnect=true";
        //
        //com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Data source rejected establishment of connection,  message from server: "Too many connections"
        try {
            //加载驱动程序
            Class.forName(driver);
            //1.getConnection()方法，连接MySQL数据库！！
            mysql_con = DriverManager.getConnection(mysql_url,user,password);
            mysql_statement = mysql_con.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }






    public void addEvent(String ip,String event,String direction,String time) {

        //遍历查询结果集
        try {
            PreparedStatement psql;
            psql = mysql_con.prepareStatement("insert into eventdata (ip,event,direction,time) values(?,?,?,?)");
            psql.setString(1, ip);
            psql.setString(2, event);
            psql.setString(3, direction);
            psql.setString(4, time);
            psql.executeUpdate();
            psql.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCheckoutData(String ip,String data,String time) {

        //遍历查询结果集
        try {
            PreparedStatement psql;
            psql = mysql_con.prepareStatement("insert into checkoutdata(ip,DATA,TIME) values(?,?,?)");
            psql.setString(1, ip);
            psql.setString(2, data);
            psql.setString(3, time);
            psql.executeUpdate();
            psql.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String query(String ip,String direction){
        String masterdir = "";
        //遍历查询结果集
        try {

            String sql = "SELECT masterdir FROM md5data WHERE ip ='"+ip+"' AND direction = '"+direction+"'";
            //3.ResultSet类，用来存放获取的结果集！！
            ResultSet rs = mysql_statement.executeQuery(sql);

            while(rs.next()){
                //masterdir
                masterdir = rs.getString("masterdir");
                System.out.println("masterdir:::"+masterdir);
            }
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return masterdir;
    }


    public String queryjia(String ip,String originaldir){
        String copydir = "";
        //遍历查询结果集
        try {

            String sql = "SELECT copydir FROM copyfile WHERE ip ='"+ip+"' AND originaldir = '"+originaldir+"'";
            //3.ResultSet类，用来存放获取的结果集！！
            ResultSet rs = mysql_statement.executeQuery(sql);

            while(rs.next()){
                //masterdir
                copydir = rs.getString("copydir");
                System.out.println("copydir:::"+copydir);
            }
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return copydir;
    }

    public void addRegMachineAndHeart(String regIp, String regPort, String data) {
        PreparedStatement psql = null;
        try {
            psql = mysql_con.prepareStatement("insert into regmachine (regIp,regPort,data) values(?,?,?)");
            psql.setString(1, regIp);
            psql.setString(2, regPort);
            psql.setString(3, data);
            psql.executeUpdate();
        } catch (Exception e) {
            try {
                psql = mysql_con.prepareStatement("\n" +
                        "update regmachine set regPort=?,data=? where regIp=?");
                psql.setString(3, regIp);
                psql.setString(1, regPort);
                psql.setString(2, data);
                psql.executeUpdate();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        try {
            psql.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        Mysql mysql = new Mysql();
        //mysql.addEvent("localhost","add","a.b","12.12");
       // String s = mysql.query("192.168.33.134","/home/quanfei/app/zookeeper-3.4.5/bin/README.txt");
        mysql.addCheckoutData("192.168.33.134","/home/quanfei/app/zookeeper-3.4.5/bin/README.txt","");
        //System.out.println(s);
    }




}
