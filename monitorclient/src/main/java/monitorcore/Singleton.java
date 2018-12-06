package monitorcore;

import java.util.LinkedList;
import java.util.List;

public class Singleton {
    private static Singleton instance;
    private static List<String> list = new LinkedList<String>();
    private Singleton (){

    }
    public static synchronized Singleton getInstance(){    //对获取实例的方法进行同步
        if (instance == null)
            instance = new Singleton();
        return instance;
    }


    public static List<String> getList() {
        return list;
    }

    public static void setList(String s) {
        list.add(s);
    }
}