package CMM;
import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;

//import CMM.token;
public class lexer {
    //存储源程序的字符串 存储结构

    //读取的结构
    //private BufferedReader reader;
    //token解析

    //static int num;	//字码标号
    //存储token信息的结构
    //public static ArrayList<token> tokenStream = new ArrayList<token>();
    //token的放回

    //定义关键字
    /*
     * 关键字 if else while read write int real
     * 特殊符号 + - *  / =  < > ==  <> ( ) ; { } [ ]
     * 以及以下的特殊注释符号
     */
    /**/
    private String []keyword = {"if","else","while","read","write","int","real"};
    private String []operator = {"+","-","*","/","=","<",">","==","<>"};
    private String []divide = {"(",")",";","{","}","[","]"};
    /*
    public static final int IF 		= 1;	//if
    public static final int ELSE 	= 2;	//else
    public static final int WHILE 	= 3;	//while
    public static final int READ 	= 4;	//read
    public static final int WRITE	= 5;	//write
    public static final int INT 	= 6;	//int
    public static final int REAL 	= 7;	//real

    public static final int PLUS 	= 8;	//+
    public static final int MINUS 	= 9;	//-
    public static final int MULTI 	= 10;	//*
    public static final int DIVI 	= 11;	// /（除号)
    public static final int ASSIGN 	= 12;	// =
    public static final int LT 		= 13;	// <
    public static final int GT 		= 14;	// >
    public static final int EQUAL 	= 15;	// ==
    public static final int NEQUAL 	= 16;	// <>（不等于）

    public static final int LROUND 	= 17;	//(
    public static final int RROUND 	= 18;	// )
    public static final int SEMI	= 19;	// ;
    public static final int LBRACE 	= 20;	// {
    public static final int RBRACE 	= 21;	// }
     */
    public String[] description = {"if","else","while","read","write","int","real","+","-","*","/","=","<",">","==",
            "<>","(",")",";","{","}","[","]","变量标识符","整数(int)","实数(real)"};
//    public static final int LCOM 	= 22;	// /* 评论符号
//    public static final int RCOM 	= 23;	// */
    public static final int LBRACKET= 22;	// [
    public static final int RBRACKET= 23;	// ]

    public static final int ID 		= 24;	//Identifier
    //标识符 小数 整数值 的存储
    public static final int VAL_INT = 25;	//int值
    public static final int VAL_REAL= 26;	//real值

    //关键字的判断
    int isKey(String str){
        int index = -1;
        for(int i = 0;i < keyword.length;i++)
        {
            if(keyword[i].equals(str))
            {
                index = i;
                break;
            }
        }
        return index;
    }
    //字母的判断
    boolean isLetter(char letter)
    {
        if((letter >= 'a' && letter <= 'z')||(letter >= 'A' && letter <= 'Z'))
            return true;
        else
            return false;
    }
    //判断是否是数字
    boolean isDigit(char digit)
    {
        if(digit >= '0' && digit <= '9')
            return true;
        else
            return false;
    }
    //判断是否为操作符号
    int isOperator(String str){
        int index = -1;
        for(int i=0;i<operator.length;i++)
        {
            if(str.equals(operator[i]))
            {
                index=i;
                break;
            }
        }
        return index;

    }
    //判断是否为界限符号
    public int isDivide(String str){
        int index = -1;
        for(int i = 0;i < divide.length;i++)
        {
            if(str.equals(divide[i])) {
                index = i;
                break;
            }
        }
        return index;
    }
    //分析过程
    //把txt转换为数组
    public String txt2String(File file) throws IOException
    {
        StringBuilder result =new StringBuilder();
        try {
            BufferedReader br= new BufferedReader(new FileReader(file));
            String s=null;
            while((s=br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result.toString();
    }
    //对文本的预处理
    //对文本进行预处理
    public  char[] preLexer(char[] sourcefile)
    {
        char []afterfile = new char[10000];
        int index=0;
        if(sourcefile.length!=0)
        {
            for(int i=0;i<sourcefile.length;i++)
            {
                //去掉//注释后的一整行
                if(sourcefile[i]=='/'&&sourcefile[i+1]=='/')
                {
                    while(sourcefile[i]!='\n')
                        i++;
                }
                //去掉/**/型注释中间的字符，若只检测到/*，未检测到*/，则提示注释有误
                if(sourcefile[i]=='/'&&sourcefile[i+1]=='*')
                {
                    i=i+2;
                    while(sourcefile[i]!='*'||sourcefile[i+1]!='/')
                    {
                        i++;
                        if(i==(sourcefile.length-1))
                        {
                            System.out.println("注释有误，未找到注释尾");
                            return afterfile;
                        }
                    }
                    i=i+2;
                }
                if(sourcefile[i]!='\n'&&sourcefile[i]!='\r'&&sourcefile[i]!='\t')
                {
                    afterfile[index]=sourcefile[i];
                    index++;
                }
            }
            index++;
            afterfile[index]='\0';
        }
        return afterfile;
    }

    ArrayList<token> analyze(char[] afterfile){
        ArrayList<token> tokenStream = new ArrayList<token>();
        //扫描器的位置下标
        int index = 0;
        //存储一个临时的字符串
        String temp = "";
        //给token赋值
        int nt;
        //扫描器
        while (afterfile[index] != '\0'){
            //开头是_或字母可能是保留字或者是标识符
            if((isLetter(afterfile[index]))||(afterfile[index]=='_'))
            {
                temp+=afterfile[index];
                //当下一个字符不为字母或数字，则停止扫描，并将扫描结果存入temp
                while(isLetter(afterfile[index+1])||isDigit(afterfile[index+1]))
                {
                    index++;
                    temp+=afterfile[index];
                }
                //将temp与保留字数组匹配，匹配成功即为保留字，否则为标识符
                if(isKey(temp)!=-1)
                {
                   //是关键字
                    nt = isKey(temp);
                    tokenStream.add(new token(nt+1,keyword[nt]));
                }
                else {
                    //是标识符
                    tokenStream.add(new token(24,temp));
                }
            }
            //开头是数字时候，小数或者整数
            else if(isDigit(afterfile[index]))
            {
                temp += afterfile[index];
                while(isDigit(afterfile[index+1]))
                {
                    index++;
                    temp+=afterfile[index];
                }
                //存在.时候，继续判断
                if(afterfile[index+1]=='.')
                {
                    index++;
                    //小数点后无数字，出错
                    if(!isDigit(afterfile[index+1])){
                        //小数点后无数字
                        temp = "";
                        break;
                    }
                    //合法的小数
                    else{
                        temp += afterfile[index];
                        while(isDigit(afterfile[index+1]))
                        {
                            index++;
                            temp+=afterfile[index];
                        }
                        //检测为小数
                        tokenStream.add(new token(26,temp));
                    }
                }
                else//为整数
                {
                    tokenStream.add(new token(25,temp));
                }
            }
            //界限符号和运算符号(跳过空格)
            else if(afterfile[index]!=' ')
            {
                temp += afterfile[index];
                if(isDivide(temp)!=-1)
                {
                    nt = isDivide(temp);
                    //是界限符号
                    temp ="";
                    index++;

                    tokenStream.add(new token(nt+8,divide[nt]));
                    continue;
                }
                //运算符号
                else{
                    //双目运算符号
                    if(afterfile[index+1]=='>'||afterfile[index+1]=='=')
                    {
                        index++;
                        temp+=afterfile[index];
                    }
                    if(isOperator(temp)!=-1)
                    {
                        //是运算符号
                        nt = isOperator(temp);
                        tokenStream.add(new token(nt+17,operator[nt]));
                    }
                    else{
                        //报错

                    }
                }
            }
            temp="";
            index++;
        }
        return tokenStream;
    }
}
