package CMM;
import java.io.*;
import java.util.ArrayList;

public class entrance {
    //使用的基础数据结构
    public static ArrayList<token> tokenStream = new ArrayList<token>();
    public static ArrayList<token> tokenError = new ArrayList<>();  //错误的词素
    //前后端交互的接口
    public static String tokenResult = "";
    public static String parserResult = "语法分析开始：\n\n";
    public static String parserError = "";
    public static String lexerError = "";
    public static String yuyiError = "";
    public static String wholeExcute = "";
    public static String writeCon = "";

    public static String presentCon = "";
    //分析标识位置 lexer
    public static int lexerPoint = 0;
    //节点结束
    public static void main(String[] args)
    {

        //1.词法分析开始

        lexer le = new lexer();

        //源文件的txt的处理
        String pathname = "src/语义测试.txt";
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
            if(w<0) {
                //System.out.print("Error("+w+")"+"\t");
                tokenResult = tokenResult + "Error(" + w + ")" + "\t";
                lexerError = lexerError + "Error(" + w + ")" + "\t";
                lexerError = lexerError + tokenStream.get(i).content+"\t\t\t";
                lexerError = lexerError + tokenStream.get(i).line;
            }
            else
                //System.out.print(w+"\t\t\t");
                tokenResult = tokenResult + w+"\t\t\t";
            //System.out.print(tokenStream.get(i).content+"\t\t\t");
            tokenResult = tokenResult + tokenStream.get(i).content+"\t\t\t";
            //System.out.print(tokenStream.get(i).line);
            tokenResult = tokenResult + tokenStream.get(i).line;
            if(w<0)
            {
                ++lexerPoint;
                //System.out.println(tokenError.get(tempError).content);
                tokenResult = tokenResult + tokenError.get(tempError).content + "\n";
                lexerError = lexerError + tokenError.get(tempError).content + "\n";
                ++tempError;
            }
            else
                //System.out.println();
                tokenResult = tokenResult + "\n";
        }
        System.out.println(tokenResult);

        if(lexerPoint == 0)
        {
            lexerError = "词法无误!\n";

            //语法分析开始
            //System.out.println(lexerError);
            //System.out.println("\n语法分析开始：\n");
            parser par = new parser();
            par.program();
            //System.out.println(parserResult);
            if(parser.elseError == 1) {
                //System.out.println("语法无误！\n");
                //entrance.parserResult = entrance.parserResult + "程序语法无误！\n";
                presentCon = lexerError + "语法无误！\n";
                if(parser.yuyiErrorExi == 1)//无误
                {
                    presentCon += "语义无误！\n" +"执行结果如下：\n";
                    if(writeCon == "")
                    {
                        writeCon = "\n无任何write()语句的输出！\n";
                    }
                    presentCon += writeCon;
                }
                else
                {
                    presentCon += "语义有误！\n" + yuyiError;
                }
//                if(parserError != "")
//                {
//                    System.out.println(parserError);
//                    System.out.println("语义有误！\n");
//                    presentCon = presentCon + parserError + "语义有误！\n";
//                }else
//                {
//                    presentCon = presentCon + "语义无误！\n";
//                }
            }
            else {
                //System.out.println("语法有误!\n");
                //entrance.parserResult = entrance.parserResult + "程序语法有误！\n";
                //System.out.println(parserError);
                presentCon = lexerError + "语法有误!\n" + parserError;

            }
            //System.out.println("\n"+parserResult);
        }else
        {
            //无法进行语法的分析
            lexerError = "存在词法错误：\n"+ lexerError;
            presentCon = lexerError;
            //System.out.println(lexerError);
        }
        System.out.println(presentCon);
        //System.out.println("完整分析过程：\n"+wholeExcute);
//        System.out.println("执行结果：\n");
//        System.out.println(wholeExcute);
//        System.out.println("\n\n\n"+parserResult);
    }
}