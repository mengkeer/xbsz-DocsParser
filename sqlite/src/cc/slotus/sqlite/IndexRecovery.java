package cc.slotus.sqlite;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by mengkeer on 2015/12/10.
 */
public class IndexRecovery {

    static ArrayList<Model> list = new ArrayList<>();
    static String fileName = "new.db";
    static Connection conn;
    static Statement stmt;
    static ResultSet rs;


    private static void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
            stmt = conn.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void close() {
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static void start() {
        try {
            rs = stmt.executeQuery("select * from mao2");
            while (rs.next()){
                Model temp = new Model();
                temp.setId(rs.getInt(rs.findColumn("id")));
                temp.setPid(rs.getInt(rs.findColumn("pid")));
                temp.setQid(rs.getInt(rs.findColumn("qid")));
                temp.setType(rs.getInt(rs.findColumn("type")));
                temp.setTitle(rs.getString(rs.findColumn("title")));
                temp.setOption(rs.getString(rs.findColumn("option")));
                temp.setAnswer(rs.getString(rs.findColumn("answer")));
                temp.setFlag(rs.getInt(rs.findColumn("flag")));
                list.add(temp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int ff = 0;
        for(Model temp:list){
            work(ff++,temp.getId(),temp.getPid(),temp.getQid(),temp.getTitle());
            System.out.println("2017.12.31 正在更新第"+ff+"行");
        }
        close();
    }

    private static void work(int ff, int id, int pid, int qid,String title) {

        try {
            stmt.execute("UPDATE mao2 SET id = '"+ff+"' WHERE id = '"+id+"' and pid = '"+pid+"' and qid = '"+qid+"' and title = '"+title+"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        init();
        start();
    }

}
