package test.action;
import test.action.cFrame;
import test.action.token;
import java.io.*;
import java.util.ArrayList;

import test.action.parser;

public class entrance {
    //使用的基础数据结构
    public static ArrayList<token> tokenStream = new ArrayList<token>();
    public static ArrayList<token> tokenError = new ArrayList<>();  //错误的词素
    //前后端交互的接口
    public static String tokenResult = "\n 词法分析开始：\n";
    public static String parserResult = "\n语法分析开始：\n\n";
    public String compile(){

        //1.词法分析开始

        lexer le = new lexer();

        //源文件的txt的处理
        String pathname = "D:\\Program Data\\Java_workbench\\idea\\CMM\\compiler\\src\\test\\action\\语法测试(正确).txt";
        File file = new File(pathname);
        //txt 转换为 字符数组
        String source = "";
        try {
            source = le.txt2String(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(source);
        char sourcefile[] = source.toCharArray();
        //预处理 去掉注释等
        char afterfile[] = le.preLexer(sourcefile);
        //输出预处理后的程序文件
        System.out.println(afterfile);
        //开始词法分析
        tokenStream = le.analyze(afterfile);
        int tempError = 0;
        for(int i = 0;i < tokenStream.size();i++)
        {
            int w = tokenStream.get(i).kind;
            if(w<0)
                //System.out.print("Error("+w+")"+"\t");
                tokenResult = tokenResult + "Error("+w+")"+"\t";
            else
                //System.out.print(w+"\t\t\t");
                tokenResult = tokenResult + w+"\t\t\t";
            //System.out.print(tokenStream.get(i).content+"\t\t\t");
            tokenResult = tokenResult + tokenStream.get(i).content+"\t\t\t";
            //System.out.print(tokenStream.get(i).line);
            tokenResult = tokenResult + tokenStream.get(i).line;
            if(w<0)
            {
                //System.out.println(tokenError.get(tempError).content);
                tokenResult = tokenResult + tokenError.get(tempError).content + "\n";
                ++tempError;
            }
            else
                //System.out.println();
                tokenResult = tokenResult + "\n";
        }
        System.out.println(tokenResult);
        //语法分析开始
        System.out.println("\n语法分析开始：\n");
        parser par = new parser();
        int res = par.program();
        if(parser.elseError == 1) {
            System.out.println("程序语法无误！");
            entrance.parserResult = entrance.parserResult + "程序语法无误！\n";
        }
        else {
            System.out.println("程序语法有误");
            entrance.parserResult = entrance.parserResult + "程序语法有误！\n";
        }
//        System.out.println("\n"+parserResult);
           return tokenResult+parserResult;
    }
}