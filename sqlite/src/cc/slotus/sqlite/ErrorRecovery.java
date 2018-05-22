package cc.slotus.sqlite;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by mengkeer on 2015/12/10.
 */
public class ErrorRecovery {


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
            rs = stmt.executeQuery("select * from mao1_copy where id!=0 ");
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
            int t = temp.getType();
            int id = temp.getId();
            String type = "";
            String ck = "";
            switch (t){
                case 1:
                    type = "单选";
                    break;
                case 2:
                    type = "多选";
                    break;
                case 3:
                    type = "判断";
                    break;
                default:
                    type = "未知类型";
                    break;
            }
            if((t==1||t==3)&&temp.getAnswer().length()>1) ck = "不符合常理";
            if(t==2&&temp.getAnswer().length()>5)  ck = "不符合常理";

            if(ck.equals(""))   continue;

            System.out.print("第"+id+"题\t"+"类型为:"+type+"\t"+"答案长度为："+temp.getAnswer().length()+'\t'+ck);
            if(!ck.equals("")) System.out.print("\t开始修正\t");

            String ans1 = temp.getAnswer();
            System.out.print("原答案为："+ans1+"\t");
            String ans2 = work(temp.getId(),temp.getAnswer());
            System.out.print("修正后为："+ans2+"\t");
            if(!ans1.equals(ans2)) System.out.println("\t\t已修正");
            else{
                System.out.println();
            }
        }

        close();

    }

    private static String work(int id,String answer) {
        String ans = "";

        for(int i=0;i<answer.length();i++){
            char ch = answer.charAt(i);
            if((ch>='a'&&ch<='z')||(ch>='A'&&ch<='Z'))  ans += ""+ch;
        }

        try {
            stmt.execute("UPDATE mao1_copy SET answer = '"+ans+"' WHERE id = "+id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(ans.equals(""))     ans = "没修改";

        return ans;
    }

    public static void main(String[] args) {
        init();
        start();
    }



}
