package CMM;
import CMM.cFrame;
import CMM.token;
import java.io.*;
import java.util.ArrayList;

import CMM.lexer;

public class entrance {
    public static ArrayList<token> tokenStream = new ArrayList<token>();
    public static void main(String[] args){

        //cFrame f = new cFrame();
        //展示界面
        //f.display();

        //词法分析开始
        lexer le = new lexer();
        String pathname = "src/cmmtest.txt";
        //源文件的txt
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
        //System.out.println(afterfile);
        //开始分析
        tokenStream = le.analyze(afterfile);
        for(int i = 0;i < tokenStream.size();i++)
        {
            System.out.print(tokenStream.get(i).kind+"\t");
            System.out.println(tokenStream.get(i).content);
        }
    }

}