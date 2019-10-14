package CMM;
import CMM.cFrame;
import CMM.token;
import java.io.*;
import java.util.ArrayList;

import CMM.lexer;

public class entrance {
    public static ArrayList<token> tokenStream = new ArrayList<token>();
    public static ArrayList<token> tokenError = new ArrayList<>();  //错误的词素
    public static void main(String[] args){

        //1.词法分析开始

        lexer le = new lexer();

        //源文件的txt的处理
        String pathname = "src/cmmtest(2).txt";
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
        System.out.println(afterfile);
//        System.out.println("!!!");
//        System.out.println(afterfile[4]);
//        System.out.println("!!!");
        //开始词法分析
        tokenStream = le.analyze(afterfile);
        int tempError = 0;
        for(int i = 0;i < tokenStream.size();i++)
        {
            int w = tokenStream.get(i).kind;
            if(w<0)
                System.out.print("Error("+w+")"+"\t");
            else
                System.out.print(w+"\t\t\t");
            System.out.print(tokenStream.get(i).content+"\t\t\t");
            System.out.print(tokenStream.get(i).line);
            if(w<0)
            {
                System.out.println(tokenError.get(tempError).content);
                ++tempError;
            }
            else
                System.out.println();
        }
    }

}