package cc.slotus.sqlite;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by mengkeer on 2015/12/10.
 */
public class Differ {

    static ArrayList<Model> list = new ArrayList<>();
    static String fileName = "tiku.db";
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
            rs = stmt.executeQuery("select * from mao2 where id != 0");
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

        for(Model temp:list){
            work(temp.getId(),temp.getQid());
        }
        close();
    }

    private static void work(int id,int qid) {

        try {
            int pid = 0;
            if(id<=100){
                pid = 1;
            }else if(id<=200){
                pid = 2;
                qid = id - 100;
            }else if(id<=300){
                pid = 3;
                qid = id - 200;
            }else if(id<=450){
                pid = 4;
                qid = id - 300;
            }else if(id<=500){
                pid = 1;
            }else if(id<=550){
                pid = 2;
                qid = id - 500;
            }else if(id<=600){
                pid = 3;
                qid = id - 550;
            }else if(id<=628){
                pid = 4;
                qid = id - 600;
            }
            stmt.execute("UPDATE mao2 SET pid = '"+pid+"' , qid = '"+qid+"' WHERE id = '"+id+"' ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        init();
        start();
    }

}
