package cc.slotus.sqlite;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import cc.slotus.sqlite.Model;

public class Main {
    static String fileName = "new.db";
    static Connection conn;
    static Statement stmt;
    static ResultSet rs;
    static ArrayList<Model> list = new ArrayList<>();
    static int id = 0;     //排序id
    static int pid = 0;     //章节id
    static int qid = 0;     //题目id
    static int type = 1;      //题型分类id 1为单选 2为多选 3为判断
    static Boolean isJudge = false;        //是否是判断题

    /*
          去除前后空格与末尾可能出现的PAGE之后的内容（该bug不清楚，并不影响解析，如无产生请忽略）
     */
    public static String init(String text) {
        String str = text.trim();
        int step = str.lastIndexOf("PAGE");
        step = step < 0 ? str.length() : step;
        str = str.substring(0, step).trim();
        return str;
    }

    public static void fitlerHyperlink(String[] args) {

    }

    //获取题目中的题干
    public static String getTitleByRow(String row) {
        String title = "";
        Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
        Matcher matcher = pattern.matcher(row);
        if(isJudge==true){
            while (matcher.find()) {
                String temp = matcher.group();
                if(matcher.group().trim().contains("对")||matcher.group().trim().contains("错")){
                    title = row.replace(temp, " ");
                    return title;
                }

            }

        }
        while (matcher.find()) {
            String temp = matcher.group();
            if (temp.matches(".*[A-F]+.*")) {
                title = row.replace(temp, " ");
            }

        }
        if (title.equals(""))   return  row;

        return title;
    }

    //获取题目中的答案
    public static String getAnswerByRow(String row) {
        String answer = "";
        Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
        Matcher matcher = pattern.matcher(row);

        if(isJudge==true){
            while (matcher.find()) {
                if(matcher.group().trim().contains("对")){
                    return answer="对";
                }
                if(matcher.group().trim().contains("错")){
                    return answer="错";
                }
            }

        }


        while (matcher.find()) {
            String temp = matcher.group();
            if(temp.matches(".*[A-F]+.*")&&!temp.matches(".*[G-Z]+.*")){
                answer += temp;
            }

        }
        answer = answer.trim();
//        System.out.println("answer:"+answer);
        //去除答案中的空格与特殊字符         会存在一些看似空格却不是空格的特殊字符
        String ans = "";
        for (int i = 0; i < answer.length(); i++) {
            char ch = answer.charAt(i);
            if (ch >= 'A' && ch <= 'F') {
                ans += ch;
            }
        }

        return ans;
    }

    public static String clearAnswerError(String row) {
        row = row.replace((char) 12288, ' ').trim();
        row = row.replace((char) 65313, 'A').trim();
        row = row.replace((char) 65314, 'B').trim();
        row = row.replace((char) 65315, 'C').trim();
        row = row.replace((char) 65316, 'D').trim();
        row = row.replace((char) 160, ' ').trim();
        return row;
    }

    //解析word纯文本内容
    public static void startParse(String str) {
        String[] rows = str.split("\n");
        String answer = "";
        String title = "";
        String option = "";
        boolean isQuestion = false;

        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            row = clearAnswerError(row);
            if(row.equals(""))      continue;
            if (row.contains("第") && row.contains("章")) {
                pid += 1;
                qid = 0;
                continue;
            }
            if (row.contains("选择题") || row.contains("单选题") || row.contains("多选题")||row.contains("项选择")  || row.contains("判断题")) {
                qid = 0;
                isJudge = false;
            }
            if (row.contains("单项选择题") || row.contains("单选题")||row.contains("单项选择")) {
                type = 1;
                continue;
            }
            if (row.contains("多项选择题") || row.contains("多选题")||row.contains("多项选择")) {
                type = 2;
                continue;
            }
            if (row.contains("判断题") || row.contains("辨析题")) {
                type = 3;
                qid = 0;
                isJudge = true;
                continue;
            }
//            System.out.println(row);
//            if(row.contains("http")){
//                System.out.println("存在超链接");
//                System.out.println(row);
//            }

            //判断是否以数字开头  是的话则为题目
            if (row.matches("^[0-9]+.*")) {
                isQuestion = true;
                option = "";
                title = "";
                answer = "";
                answer = getAnswerByRow(row);
                title = getTitleByRow(row);
                if(isJudge==false)  continue;
            }

            boolean flag = false;
            if (isJudge == true) {
                option = "";
            } else {
                row = clearAnswerError(row);
                while (!row.equals("") && row.charAt(0) >= 'A' && row.charAt(0) <= 'Z') {
                    flag = true;
                    option += row + "";
                    ++i;
                    if (i + 1 > rows.length) break;
                    row = rows[i].trim();
                    row = clearAnswerError(row);
                }
            }


            if (flag == true)    --i;
            if((flag==true||isJudge==true)&&isQuestion==true){
                isQuestion = false;
                Model one = new Model();
                one.setId(++id);
                one.setPid(pid);
                one.setQid(++qid);
                one.setType(type);
                one.setTitle(title);
                one.setOption(option);
                one.setAnswer(answer);
                one.setFlag(0);
                list.add(one);
            }

        }
        System.out.println("总题数:"+list.size());
//        for (Model temp : list) {
//            System.out.println("总id:"+temp.getId()+"\tpid:"+temp.getPid()+"\tqid:"+temp.getQid()+"\t类型:"+temp.getType());
//            System.out.println("题干:"+temp.getTitle());
//            System.out.println("选项:"+temp.getOption());
//            System.out.println("答案:"+temp.getAnswer());
//        }
    }

    public static void importToSqlite() {

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + fileName);
            stmt = conn.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("开始导入");
        for (Model temp : list) {
            try {
                System.out.println("开始导入第" + temp.getId() + "条");
                stmt.execute("INSERT INTO mao2 VALUES ('" + temp.getId() + "','" + temp.getPid() + "','" + temp.getQid() + "'" +
                        ",'" + temp.getType() + "','" + temp.getTitle() + "','" + temp.getOption() + "','" + temp.getAnswer() + "'" +
                        ",'" + temp.getFlag() + "')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            //解析doc文件
            InputStream is = new FileInputStream(new File("2018上半年毛概2.doc"));
            WordExtractor ex = new WordExtractor(is);
            String text = ex.getText();
            text = init(text);      //初始化
            startParse(text);            //开始解析
            importToSqlite();
//            System.out.println(text);
            /*解析docx文件*/
//            OPCPackage opcPackage = POIXMLDocument.openPackage("ez.docx");
//            POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
//            String text2007 = extractor.getText();
//            System.out.println(text2007);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}