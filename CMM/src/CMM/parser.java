package CMM;
import jdk.jshell.Snippet;

import java.util.ArrayList;

public class parser {
    public ArrayList<token> wordList = entrance.tokenStream;
    public token syn;  //当前的token
    public int sign = 0;  //记录当前token在wordList中的位置

    //变量作用域
    public int depth = 0;//当前token所在的深度
    public SemanticAnalysis firstDepth = new SemanticAnalysis();   //存储全局变量
    public SemanticAnalysis region;       //当前token所在的区域
    //变量作用域

    //田国庆加的
    public boolean last = true;//TGQADD 前面的程序都没有问题的时候最后属否缺少} 等
    public static int elseError = 1;   //语法是否有误，除去以上的错误
    public static int yuyiErrorExi = 1;//0则无法执行
    public int decSExtendIF = 0; //1则发生中断
    public boolean isDefT;//变量是否被声明
    public boolean inSentence = true;//在非条件语句中
    //public boolean decArrCanAss;//声明时候数组可以被赋值吗
    //CHEN
    public int count;

    //执行4
    //声明数组
    public boolean arrayR;  //可以赋值数组 true
    public boolean arrayLose;//数据损失 默认false
    public boolean notArrayR;   //可以赋值变量 默认true
    //if-else
    //public int ifR = 1;     //if语句可以执行 为true
    //public int elseR = 1;   //else语句可以执行 为true
//    public int ifStart;
//    public int ifEnd;
//    public boolean hasElse;
//    public int elseStart;
//    public int elseEnd;
    //while
    //public boolean whileR;  //可执行 为true
    //public int whileStart = -1;
    //public int whileEnd = -1;
    public boolean ifJump = false;  //总的
    //public boolean firstWExc;
    //第一次遇到块
    //public boolean firstMetBlock;//第一次遇到块，是不执行的 while
    //条件语句正确
    //public boolean conR;
    //执行4

    public token temptoken = new token();
    //CHEN
    //读取下一个token
    public void nextSyn(){

        if(sign + 1 < wordList.size()){
            sign++;
            syn = wordList.get(sign);
        }
        //TGQADD
        else
        {
            //TGQ 前面的程序都没有问题的时候最后属否缺少} 等
            last = false;
        }
    }
    //TGQYUFA
    public void lastSyn()
    {
        if(sign != 0)
        {
            --sign;
            syn = wordList.get(sign);
        }
    }
    //忽略出现错误的单词的一整行
    public void jump()//结束在当前的行，不换行
    {
        int end = 0;//  期望最后一个是;
        int tempLine = syn.line;
        while (end == 0)
        {
            nextSyn();
            if (syn.line != tempLine) {
                end = 1;
                lastSyn();//取回去最后一个
            }
        }
    }
    //找到 } 和else 21 2
    public void jumpForIF()
    {
        int end = 0;
        while (end == 0)
        {
            nextSyn();
            if (syn.kind == 2||syn.kind == 21) {
                end = 1;
                if(syn.kind ==2)
                    lastSyn();//取回去最后一个
            }
        }
    }
    //TGQYUFA
    /**
     * 程序
     * 返回值0表示分析出错，1表示分析成功
     */
    public int program(){
        int result = 0;
        if(sign <= wordList.size()){    //??? 没问题
            syn = wordList.get(sign);
            //System.out.println(syn.kind);
        }
        if(syn.kind == 20){  //“{”，处理语句块
            //TGQYUFA
            if(wordList.size()==1)
            {
                //只有开头？？？TGQYUFA
                elseError = 0;
                //System.out.println(syn.line+"只有'{' ，没有匹配的 '}'");
                entrance.parserResult = entrance.parserResult + syn.line+"只有'{' ，没有匹配的 '}'\n" ;
                entrance.parserError = entrance.parserError + syn.line+"只有'{' ，没有匹配的 '}'\n" ;
            }
            else
            {
                nextSyn();
                if(syn.kind == 21)
                {
                    //TGQYUFA 主程序体内没有东西
                    elseError = 0;
                    //System.out.println(syn.line+"主程序体内没有代码！");
                    entrance.parserResult = entrance.parserResult + syn.line+"主程序体内没有代码！\n" ;
                    entrance.parserError = entrance.parserError + syn.line+"主程序体内没有代码！\n";
                }
                else {
                    //正常的情况
                    lastSyn();
                    //lastSyn();
                    //normal代码
                    //System.out.println("<程序> -> {<语句块>}");
                    //变量作用域
                    depth++;
                    region = firstDepth;
                    //4是否执行
                    region.sExcute = true;//执行
                    region.sfirstMetBlock = false;//不影响正常的执行
                    region.sifJump = false;
                    region.swhileStart = -1;
                    region.swhileEnd = -1;
                    region.sifStart = -1;
                    region.selseStart = -1;
                    //4是否执行
                    //变量作用域
                    entrance.parserResult = entrance.parserResult + "<程序> -> {<语句块>}\n";
                    result = statements();
                    //TGQADD 前面的程序都没有问题的时候最后属否缺少} 等
                    if(elseError != 0)
                    {
                        if (!last) {
                            elseError = 0;
                            //System.out.println(++syn.line+"行建议加入与程序开头的 { 匹配的 }");
                            entrance.parserResult = entrance.parserResult + (++syn.line)+"行建议加入与程序开头的 { 匹配的 }\n" ;
                            entrance.parserError = entrance.parserError + (++syn.line)+"行建议加入与程序开头的 { 匹配的 }\n" ;
                        }
                        else
                        {
                            if(syn.kind != 21)
                            {
                                elseError = 0;
                                //System.out.println(syn.line+"行的"+syn.content+"应该替换为 } 以与程序开头的 { 匹配！");
                                entrance.parserResult = entrance.parserResult + syn.line+"行的"+syn.content+"应该替换为 } 以与程序开头的 { 匹配！\n";
                                entrance.parserError = entrance.parserError + syn.line+"行的"+syn.content+"应该替换为 } 以与程序开头的 { 匹配！\n";
                            }
                            if(sign != (wordList.size()-1))
                            {
                                elseError = 0;
                                //System.out.println("程序{}体外存在无用的信息");
                                entrance.parserResult = entrance.parserResult + "程序{}体外存在无用的信息\n";
                                entrance.parserError = entrance.parserError + "程序{}体外存在无用的信息\n";
                            }
                        }
                    }
                    //正常的情况
                }
            }

        }
        else{  //报错，缺少“{”
            result = 0;
            elseError = 0;
        }
        return result;
    }

    /**
     * 语句块
     */
    public int statements(){
        int result = 1;
        int i = 0;
        //System.out.println("<语句块>\t\t->\t\t<语句><语句拓展>");
        entrance.parserResult = entrance.parserResult + "<语句块>\t\t->\t\t<语句><语句拓展>\n";
        nextSyn();

        while(last == true &&syn.kind != 21)//最后是 } 待修改 不能是最后一个单词？
        //while(last == true)
        {
            if(ifJump)
            {
                //entrance.wholeExcute+="跳转le！\n";
                //System.out.println("跳转了！");
                lastSyn();
                ifJump = false;
                //System.out.println(syn.kind);
            }
            if(syn.kind == 6 || syn.kind == 7 || syn.kind == 24 || syn.kind == 5 || syn.kind == 4 || syn.kind == 1 || syn.kind == 3) {
                if(i != 0){
                    //System.out.println("<语句拓展>\t\t->\t\t<语句><语句拓展>");
                    entrance.parserResult = entrance.parserResult + "<语句拓展>\t\t->\t\t<语句><语句拓展>\n";
                }
                if(syn.kind == 6 || syn.kind == 7){  //int或real，声明语句
                    //System.out.println("<语句>\t\t\t->\t\t<声明语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<声明语句>\n";
                    //System.out.println("<声明语句>\t\t->\t\t<数据类型>变量名ID<声明类型拓展><声明内容>;");
                    entrance.parserResult = entrance.parserResult + "<声明语句>\t\t->\t\t<数据类型>变量名ID<声明类型拓展><声明内容>;\n";
                    decStatement();
                    nextSyn();
                }
                else if(syn.kind == 24){  //变量名ID，赋值语句
                    //System.out.println("<语句>\t\t\t->\t\t<赋值语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<赋值语句>\n";
                    //System.out.println("<赋值语句>\t\t->\t\t变量名ID<赋值语句拓展>;");
                    entrance.parserResult = entrance.parserResult + "<赋值语句>\t\t->\t\t变量名ID<赋值语句拓展>;\n";
                    assStatement();
                    nextSyn();
                }
                else if(syn.kind == 5){  //write，输出语句
                    //System.out.println("<语句>\t\t\t->\t\t<输出语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<输出语句>\n";
                    //System.out.println("<输出语句>\t\t->\t\twrite(<表达式>);");
                    entrance.parserResult = entrance.parserResult + "<输出语句>\t\t->\t\twrite(<表达式>);\n";
                    writeStatement();
                    nextSyn();
                }
                else if(syn.kind == 4){  //read，输入语句
                    //System.out.println("<语句>\t\t\t->\t\t<输入语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<输入语句>\n";
                    //System.out.println("<输入语句>\t\t->\t\tread(变量名ID<声明类型拓展>);");
                    entrance.parserResult = entrance.parserResult + "<输入语句>\t\t->\t\tread(变量名ID<声明类型拓展>);\n";
                    readStatement();
                    nextSyn();
                }
                else if(syn.kind == 1){  //if语句
                    //System.out.println("<语句>\t\t\t->\t\t<if语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<if语句>\n";
                    //System.out.println("<if语句>\t\t->\t\tif(<条件语句>){<语句块>}<if扩展>");
                    entrance.parserResult = entrance.parserResult + "<if语句>\t\t->\t\tif(<条件语句>){<语句块>}<if扩展>\n";
                    //firstMetBlock = true;//4
                    result = ifStatement();
                    nextSyn();
                }
                else if(syn.kind == 3){  //while语句
                    //System.out.println("<语句>\t\t\t->\t\t<while语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<while语句>\n";
                    //System.out.println("<while语句>\t->\t\twhile(<条件语句>){语句块}");
                    entrance.parserResult = entrance.parserResult + "<while语句>\t->\t\twhile(<条件语句>){语句块}\n";
                    //firstMetBlock = true;//4
                    result = whileStatement();
                    //System.out.println("出while了！");
                    nextSyn();
                }
                i++;
            }
            else if(syn.kind != 2){ //  else 待修改
                //找到最终的; 或者开头正确的语句
                //TGQYUFA
                //lastSyn();
                elseError = 0;
                int tempLine = syn.line;
                //System.out.print("第"+tempLine+"行语句: '");
                int end = 0;//  期望最后一个是;
                int left = 0;// { 没有出现
                String err = "";
                while(end == 0) //换行完就OK
                {
                    err = err + syn.content;
                    nextSyn();
                    if(syn.line != tempLine)
                    {
                        end = 1;
                    }

                }
                //System.out.println(err +"' 有误!");
                entrance.parserError = entrance.parserError + "第"+tempLine+"行语句: '" + err +"' 有误!\n";
            }
            else {//TGQYUFA
                lastSyn();//TGQYUFA
                return result;//TGQYUFA
                //if后面缺少 }的特殊情况 待修改
            }
            //nextSyn();
        }
        //System.out.println("<语句拓展>\t\t->\t\t∂");
        entrance.parserResult = entrance.parserResult + "<语句拓展>\t\t->\t\t∂\n";
        return result;
    }

    /**
     * 表达式
     */

    public token expression() {
        String tp,eplace2,eplace,temp = null;
        //4
        token re = new token(0,null,0);
        token ep,ep2;
        int result1 = 0;
        float result2 = 0;
        int a = 1;
        //4
        ep = expressionMD();
        if(ep.content == null){
            return re;
        }
        while(syn.kind == 8 || syn.kind == 9){   //'+' '-'
            //System.out.println("<表达式拓展>\t->\t\t<运算符号><表达式>");
            entrance.parserResult = entrance.parserResult + "<表达式拓展>\t->\t\t<运算符号><表达式>\n";
            //System.out.println("<运算符号>\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "<运算符号>\t\t->\t\t" + syn.content +"\n";
            if(sign < wordList.size())
                temp = syn.content; //获取当前运算符
            nextSyn();//读取下一个单词符号;
            if((ep2 = expressionMD()).content == null){
                return re;
            }
            //tp = newtemp();
            //emit(temp,eplace,eplace2,tp);
            //eplace = "T";
            //ZEN 4
            //表达式执行，处理+和-
            int x1 = 0, x2 = 0;     //对应于ep和ep2为int值的情况
            float y1 = 0, y2 = 0;   //对应于ep和ep2为real值的情况
            String s1,s2;           //记录ep和ep2是哪一种类型
            //根据ep和ep2获取到对应的值
            if(ep.kind == 24){
                if(region.getType(ep) == 25){
                    //4region.setvalue(ep,3);
                    x1 = region.getIntV(ep);
                    s1 = "x";
                }
                else{
                    //4region.setvalue(ep,(float)4.0);
                    y1 = region.getFloatV(ep);
                    s1 = "y";
                }
            }
            else if(ep.kind == 25){
                x1 = Integer.parseInt(ep.content);
                s1 = "x";
            }
            else{
                y1 = Float.parseFloat(ep.content);
                s1 = "y";
            }
            if(ep2.kind == 24){
                if(region.getType(ep2) == 25){
                    //4region.setvalue(ep2,3);
                    x2 = region.getIntV(ep2);
                    s2 = "x";
                }
                else{
                    //4region.setvalue(ep2,(float) 2.0);
                    y2 = region.getFloatV(ep2);
                    s2 = "y";
                }
            }
            else if(ep2.kind == 25){
                x2 = Integer.parseInt(ep2.content);
                s2 = "x";
            }
            else{
                y2 = Float.parseFloat(ep2.content);
                s2 = "y";
            }
            //计算结果
            if(temp.equals("+")){
                if(s1.equals("x")){
                    if(s2.equals("x")){
                        result1 = x1 + x2;
                        a = 1;
                    }
                    else{
                        result2 = x1 + y2;
                        a = 2;
                    }
                }
                else{
                    if(s2.equals("x")){
                        result2 = y1 + x2;
                        a = 2;
                    }
                    else{
                        result2 = y1 + y2;
                        a = 2;
                    }
                }
            }
            else{
                if(s1.equals("x")){
                    if(s2.equals("x")){
                        result1 = x1 - x2;
                        a = 1;
                    }
                    else{
                        result2 = x1 - y2;
                        a = 2;
                    }
                }
                else{
                    if(s2.equals("x")){
                        result2 = y1 - x2;
                        a = 2;
                    }
                    else{
                        result2 = y1 - y2;
                        a = 2;
                    }
                }
            }
            //将结果储存到ep中
            if(a == 1){
                ep = new token(25,String.valueOf(result1),syn.line);
            }
            else{
                ep = new token(26,String.valueOf(result2),syn.line);
            }
            //ZEN 4
        }
        //System.out.println("<表达式拓展>\t->\t\t∂");
        entrance.parserResult = entrance.parserResult + "<表达式拓展>\t->\t\t∂\n";
        return ep;
    }
    //优先处理*，/
    public token expressionMD() {
        String tp,eplace3,eplace,tt = null;
        //ZEN4
        token re = new token(0,null,0);    //返回值
        token ep,ep3;
        int result1 = 0;    //结果为int
        float result2 = 0;  //结果为float
        int b = 1;          //记录结果的类型
        //ZEN4
        ep = expressionB();
        if(ep.content == null){
            return ep;
        }
        while(syn.kind == 10 || syn.kind == 11) {    //'*' '/'
            //System.out.println("<表达式拓展>\t->\t\t<运算符号><表达式>");
            entrance.parserResult = entrance.parserResult + "<表达式拓展>\t->\t\t<运算符号><表达式>\n";
            //System.out.println("<运算符号>\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "<运算符号>\t\t->\t\t" + syn.content + "\n";
            if (sign < wordList.size())
                tt = syn.content;
            //nextSyn();//读取下一个单词符号;
            //YUYI
            //判断运算符是否为/
            if (syn.kind == 11) // /
            {
                nextSyn();
                if(syn.kind == 9)//-号
                {
                    nextSyn();
                    if(syn.kind == 26)
                    {
                        double tF = Double.parseDouble(syn.content);
                        double tF1 = Math.ceil(tF);
                        Double tf2 = new Double(tF1);
                        int tF3 = tf2.intValue();
                        if (region.IfDivZero(tF3)) {
                            //System.out.printf("错误出现在第%d行，除数为0. \n", syn.line);
                            yuyiErrorExi = 0;
                            entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                            entrance.yuyiError = entrance.yuyiError + "错误出现在第" + syn.line + "行，除数为0. \n";
                        }
                    }
                    else if(syn.kind == 25)
                    {
                        if (region.IfDivZero(Integer.parseInt(syn.content))) {
                            //System.out.printf("错误出现在第%d行，除数为0. \n", syn.line);
                            yuyiErrorExi = 0;
                            entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                            entrance.yuyiError = entrance.yuyiError + "错误出现在第" + syn.line + "行，除数为0. \n";
                        }
                    }
                    lastSyn();
                }else {
                    if(syn.kind == 25||syn.kind == 26)
                    {
                        int tF3;
                        if(syn.kind == 26)
                        {
                            double tF = Double.parseDouble(syn.content);
                            double tF1 = Math.ceil(tF);
                            Double tf2 = new Double(tF1);
                            tF3 = tf2.intValue();
                        }
                        else{
                            tF3 = Integer.parseInt(syn.content);
                        }
                        if (region.IfDivZero(tF3)) {
                            //System.out.printf("错误出现在第%d行，除数为0. \n", syn.line);
                            yuyiErrorExi = 0;
                            entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                            entrance.yuyiError = entrance.yuyiError + "错误出现在第" + syn.line + "行，除数为0. \n";
                        }
                    }
                }
                lastSyn();
            }
            //YUYI
            nextSyn();
            if((ep3 = expressionB()).content == null){
                return re;
            }
            //tp = newtemp();  //生成新的变量名
            //emit(tt,eplace,eplace3,tp);
            //eplace = tp;
            //ZEN4
            //表达式执行，处理*和/，得出结果
            int x1 = 0, x2 = 0;
            float y1 = 0, y2 = 0;
            String s1,s2;

            if(ep.kind == 24){
                if(region.getType(ep) == 25){
                    //4region.setvalue(ep,3);
                    x1 = region.getIntV(ep);
                    s1 = "x";
                }
                else{
                    //4region.setvalue(ep,(float)4.0);
                    y1 = region.getFloatV(ep);
                    s1 = "y";
                }
            }
            else if(ep.kind == 25){
                x1 = Integer.parseInt(ep.content);
                s1 = "x";
            }
            else{
                y1 = Float.parseFloat(ep.content);
                s1 = "y";
            }
            if(ep3.kind == 24){
                if(region.getType(ep3) == 25){
                    //4region.setvalue(ep3,3);
                    x2 = region.getIntV(ep3);
                    s2 = "x";
                }
                else{
                    //4region.setvalue(ep3,(float) 2.0);
                    y2 = region.getFloatV(ep3);
                    s2 = "y";
                }
            }
            else if(ep3.kind == 25){
                x2 = Integer.parseInt(ep3.content);
                s2 = "x";
            }
            else{
                y2 = Float.parseFloat(ep3.content);
                s2 = "y";
            }
            if(tt.equals("*")){
                if(s1.equals("x")){
                    if(s2.equals("x")){
                        result1 = x1 * x2;
                        b = 1;
                    }
                    else{
                        result2 = x1 * y2;
                        b = 2;
                    }
                }
                else{
                    if(s2.equals("x")){
                        result2 = y1 * x2;
                        b = 2;
                    }
                    else{
                        result2 = y1 * y2;
                        b = 2;
                    }
                }
            }
            else{
                if(s1.equals("x")){
                    if(s2.equals("x")){
                        if(x2 == 0)
                        {
                            //System.out.println("1");
                            while(!((syn.kind == 18&&(wordList.get(sign+1).kind==19))||(syn.kind == 19)||syn.kind ==13||syn.kind == 14||syn.kind == 15||syn.kind ==16||(syn.kind==18&&wordList.get(sign+1).kind==20)))
                            {
                                nextSyn();
                            }
                            return re;
                            //重定位
                        }
                        result1 = x1 / x2;
                        b = 1;
                    }
                    else{
                        if(y2 == 0)
                        {
                            //System.out.println("2");
                            while(!((syn.kind == 18&&(wordList.get(sign+1).kind==19))||(syn.kind == 19)||syn.kind ==13||syn.kind == 14||syn.kind == 15||syn.kind ==16||(syn.kind==18&&wordList.get(sign+1).kind==20)))
                            {
                                nextSyn();
                            }
                            return re;
                        }
                        result2 = x1 / y2;
                        b = 2;
                    }
                }
                else{
                    if(s2.equals("x")){
                        if(x2 == 0)
                        {
                            //System.out.println("3");
                            while(!((syn.kind == 18&&(wordList.get(sign+1).kind==19))||(syn.kind == 19)||syn.kind ==13||syn.kind == 14||syn.kind == 15||syn.kind ==16||(syn.kind==18&&wordList.get(sign+1).kind==20)))
                            {
                                nextSyn();
                            }
                            return re;
                        }
                        result2 = y1 / x2;
                        b = 2;
                    }
                    else{
                        if(y2 == 0)
                        {
                            //System.out.println("4");
                            while(!((syn.kind == 18&&(wordList.get(sign+1).kind==19))||(syn.kind == 19)||syn.kind ==13||syn.kind == 14||syn.kind == 15||syn.kind ==16||(syn.kind==18&&wordList.get(sign+1).kind==20)))
                            {
                                nextSyn();
                            }
                            return re;
                        }
                        result2 = y1 / y2;
                        b = 2;
                    }
                }
            }

            if(b == 1){
                ep = new token(25,String.valueOf(result1),syn.line);
            }
            else{
                ep = new token(26,String.valueOf(result2),syn.line);
            }
            //ZEN4
        }
        //System.out.println("<表达式拓展>\t->\t\t∂");
        return ep;
    }
    //优先处理（）
    public token expressionB(){
//        String fplace;
//        fplace = " ";
        //ZEN4
        //返回类型换成了token
        token fp = new token(0,null,0);
        token re = new token(0,null,0);    //返回值为空值时返回re
        //ZNE4
        if(syn.kind == 24)
        {     //变量名
            //YUYI
            //判断变量是否已经定义
            //变量作用域
            if(!region.IsExist(syn))
            {
                //System.out.printf("错误出现在第%d行，变量%s未定义\n", syn.line,syn.content);
                entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t <表达式因子><表达式拓展>\n";
                yuyiErrorExi = 0;
                entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，变量"+syn.content+"未声明\n";
            }
            else{
//                if(isDefT == true)
//                {
                if(inSentence)
                {
                    if(region.getType(syn)!=region.getType(temptoken)) {
                        //System.out.printf("错误出现在第%d行，变量%s类型错误\n", syn.line, syn.content);
                        entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误\n";
                        //entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误\n";
                        entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误(但可执行)\n";
                    }
                    if(!region.getIfValued(syn)) {
                        //System.out.printf("错误出现在第%d行，使用了未赋值变量%s \n", syn.line, syn.content);
                        entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
                        //entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
                        yuyiErrorExi = 0;
                        entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
                    }
                }

                //}
            }
            //变量作用域
            /*是要的
            if (!SemanticAnalysis.IfRep(syn)) {
                //System.out.printf("错误出现在第%d行，变量%s未定义\n", syn.line, syn.content);
                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，变量"+syn.content+"未定义\n";
            }
            if (SemanticAnalysis.getType(syn) != SemanticAnalysis.getType(temptoken)) {
                //System.out.printf("错误出现在第%d行，变量%s类型错误\n", syn.line, syn.content);
                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误\n";
            }
            if (!SemanticAnalysis.getIfValued(syn)){
                //System.out.printf("错误出现在第%d行，使用了未赋值变量%s \n", syn.line, syn.content);
                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
            }
            */
            //System.out.println("<表达式>        ->\t\t <表达式因子><表达式拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t <表达式因子><表达式拓展>\n";
            //System.out.println("<表达式因子>\t->\t\t变量名ID<声明类型拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t变量名ID<声明类型拓展>\n";
            //System.out.println("变量名ID\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "变量名ID\t\t->\t\t" + syn.content +"\n";
            if(wordList.get(sign + 1).kind == 22)
            {
                //ZNE4
                token temp = syn;  //记录数组变量
                //ZEN4
                //如果下一个是 [
                nextSyn();//现在是[
                //System.out.println("<声明类型拓展>  ->\t\t[int值]");
                //YUYI二期
                //开始往前看
                if(wordList.get(sign+1).kind != 25)
                {
                    lastSyn();
                    elseError = 0;
                    //yuyiErrorExi = 0;
                    entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标必须是非负整数 \n";
                    entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标必须是非负整数 \n";
                    //entrance.yuyiError = entrance.yuyiError + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标必须是非负整数 \n";
                    nextSyn();
                }else{
                    int tempLocation = Integer.parseInt(wordList.get(sign+1).content);
                    lastSyn();//取名字
                    //if(SemanticAnalysis.getArraySize(temptoken)<Integer.parseInt(wordList.get(sign+1).content)+1)
                    if(region.IsExist(syn))
                    {
                        boolean isAr = region.getIsArray(syn);
                        if(isAr == false)
                        {
                            entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，"+syn.content+"不是数组 \n";
                            yuyiErrorExi = 0;
                            entrance.yuyiError = entrance.yuyiError +"错误出现在第"+syn.line+"行，"+syn.content+"不是数组 \n";
                        }
                        else {
                            if (region.getArraySize(syn) < tempLocation + 1) {
                                //System.out.printf("错误出现在第%d行，数组%s下标越界 \n", wordList.get(sign + 1).line, temptoken.content);
                                //entrance.parserResult = entrance.parserResult + "错误出现在第"+wordList.get(sign + 1).line+"行，数组"+temptoken.content+"下标越界 \n";
                                //entrance.parserError = entrance.parserError +"错误出现在第"+wordList.get(sign + 1).line+"行，数组"+temptoken.content+"下标越界 \n";
                                yuyiErrorExi = 0;
                                entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标越界 \n";
                                entrance.yuyiError = entrance.yuyiError + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标越界 \n";
                            }
                        }
                    }
                }
                nextSyn();
                //YUYI二期
                entrance.parserResult = entrance.parserResult + "<声明类型拓展>  ->\t\t[int值]\n";
                //表达式执行 获取所需数组元素的值
                int s = decStatementExtend();//4
                //ZEN4
                if(region.getType(temp) == 25){
                    int arrayV = region.getIntArrayV(temp,s);
                    fp = new token(25,String.valueOf(arrayV),temp.line);
                    nextSyn();
                    return fp;
                }
                else{
                    float arrayV = region.getFloatArrayV(syn,s);
                    fp = new token(26,String.valueOf(arrayV),syn.line);
                    nextSyn();
                    return fp;
                }
                //ZEN4
            }
            else
            {
            //if(sign < wordList.size()) {
                //System.out.println("<声明类型拓展>  ->\t\t∂");
                boolean isNotArr = region.getIsArray(syn);
                if(isNotArr != false)//不是变量
                {
                    yuyiErrorExi = 0;
                    entrance.parserResult = entrance.parserResult +"第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                    entrance.yuyiError = entrance.yuyiError+ "第"+syn.line+"行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                }
                else
                {
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>  ->\t\t∂\n";
                }
                //4
                if(region.getType(syn) == 25){
                    int iv = region.getIntV(syn);
                    fp = new token(25,String.valueOf(iv),syn.line);
                    nextSyn();
                    return fp;
                }
                else{
                    float fv = region.getFloatV(syn);
                    fp = new token(26,String.valueOf(fv),syn.line);
                    nextSyn();
                    return fp;
                }
                //fp = syn;
            }
            //nextSyn();
        }
        else if(syn.kind == 25 || syn.kind == 26 || syn.kind == 9)
        {   //数字（含负数）
            //System.out.println("<表达式>        ->\t\t <表达式因子><表达式拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t <表达式因子><表达式拓展>\n";
            if(syn.kind == 9){  //负数
                nextSyn();
                if(syn.kind == 25 || syn.kind == 26){
//                	if(syn.kind==25)
//                	{
//                		region.setvalue(temptoken, -Integer.parseInt(syn.content));
//                	}
//                	else
//                	{
//                		region.setvalue(temptoken, -Float.parseFloat(syn.content));
//                	}
                    //在这里处理负数的情况
                    //System.out.println("<表达式因子>\t->\t\t<负数>");
                    entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t<负数>\n";
                    //System.out.println("<负数>        ->     -" + syn.content);
                    entrance.parserResult = entrance.parserResult + "<负数>        ->     -" + syn.content +"\n";
                    //YUYI

                    if(isDefT == true) {
                        //11.18
                        if(inSentence)
                        {
                            if (!region.Num1(region.getType(temptoken), syn)) {
                                //System.out.printf("错误出现在第%d行，类型不匹配 \n", temptoken.line);
                                entrance.parserError = entrance.parserError + "错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                                entrance.yuyiError = entrance.yuyiError+"错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                            }
                        }
                    }
                    //ZEN4
                    //负数要将“-”加上
                    fp = new token(syn.kind,"-" + syn.content, syn.line);
                    //ZEN4
                    //YUYI
                    nextSyn();
                }
                else{
                    elseError = 0;//TGQ
                    //System.out.println(syn.line + "行负数表达错误！");
                    //entrance.parserResult = entrance.parserResult + syn.line +"行负数表达错误！\n";
                    entrance.parserError = entrance.parserError + syn.line +"行负数表达错误！\n";
                }
            }
            //这个体是语义分析
            //YUYI 正数测试代码
            else if(syn.kind == 25 || syn.kind == 26){
//            	if(syn.kind==25)
//            	{
//            		region.setvalue(temptoken, Integer.parseInt(syn.content));
//            	}
//            	else
//            	{
//            		region.setvalue(temptoken, Float.parseFloat(syn.content));
//            	}
                //在这里处理负数的情况
                //System.out.println("<表达式因子>\t->\t\t<正数>");
                entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t<正数>\n";
                //System.out.println("<正数>        ->     " + syn.content);
                entrance.parserResult = entrance.parserResult + "<正数>        ->     " + syn.content +"\n";
                //YUYI TGQYUYI
                if(isDefT == true) {
                    //11.18
                    if(inSentence)
                    {
                        if (!region.Num1(region.getType(temptoken), syn)) {
                    /*原来的
                    //TGQ YUYI 一期改BUG ???
                    if(temptoken.line!=0){
                    //System.out.printf("错误出现在第%d行，类型不匹配 \n", temptoken.line);
                    entrance.parserError = entrance.parserError + "错误出现在第"+temptoken.line+"行，类型不匹配 \n";
                    }
                    */
                            entrance.parserError = entrance.parserError + "错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                            entrance.yuyiError = entrance.yuyiError + "错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                        }
                    }

                }
                //ZEN4
                //将当前token赋值给fp
                fp = syn;
                //ZEN4
                nextSyn();
            }
            else{
                if(sign < wordList.size()){
                    //System.out.println("<表达式因子>\t->\t\t" + syn.content);
                    entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t" + syn.content + "\n";
                    fp = syn;//4
                }
                nextSyn();//读取下一个单词符号；
            }
        }
        else if(syn.kind == 17){     //'('
            //System.out.println("<表达式>        ->\t\t（<表达式因子><表达式拓展>）<表达式拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t（<表达式因子><表达式拓展>）<表达式拓展>\n";
            nextSyn();//读取下一个单词符号；
            if((fp = expression()).content == null){//4
                return re;
            }
            if(syn.kind == 18){      //')'
                nextSyn();//读取下一个单词符号；
            }
            else {
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行缺少')'");
                entrance.parserResult = entrance.parserResult + syn.line + "行缺少')'\n";
                entrance.parserError = entrance.parserError + syn.line + "行缺少')'\n";
                return re;
            }
        }
        else {
            elseError = 0;  //TGQ
            //System.out.println(syn.line + "行表达式出现语法错误");
            entrance.parserResult = entrance.parserResult + syn.line + "行表达式出现语法错误\n";
            entrance.parserError = entrance.parserError + syn.line + "行表达式出现语法错误\n";
            return re;
        }
        return fp;
    }

    /**
     * 声明语句
     */
    public int decStatement(){
        int result = 1;
        if(syn.kind == 6 || syn.kind == 7){   //数据类型
            if(syn.kind == 6){
                //System.out.println("<数据类型>\t\t->\t\tint");
                entrance.parserResult = entrance.parserResult + "<数据类型>\t\t->\t\tint\n";
            }
            else{
                //System.out.println("<数据类型>\t\t->\t\treal");
                entrance.parserResult = entrance.parserResult + "<数据类型>\t\t->\t\treal\n";
            }
            int kindFlag = syn.kind+19;//YUYI
            nextSyn();
            if(syn.kind == 24){    //变量名

                arrayR = true;//可以赋值
                notArrayR = true;//可以赋值
                //System.out.println("变量名ID\t\t->\t\t" + syn.content);
                entrance.parserResult = entrance.parserResult + "变量名ID\t\t->\t\t" + syn.content +"\n";
                //YUYI
                //检查变量是否重复申明，是-》报错    否-》加入RECORD
                if(region.IfRep(syn))
                {
                    yuyiErrorExi = 0;
                    //System.out.printf("错误产生在第%d行，变量%s重复定义。\n", syn.line, syn.content);
                    entrance.parserResult = entrance.parserResult + "错误产生在第"+syn.line+"行，变量"+syn.content+"重复定义。\n";
                    entrance.yuyiError = entrance.yuyiError +"错误产生在第"+syn.line+"行，变量"+syn.content+"重复定义。\n";
                    arrayR = false;//无法赋值
                    notArrayR = false;//无法赋值
                }else
                {
                    region.addToReco(syn,kindFlag);
                    //region.clean(syn);
                    isDefT = true;
                }
                temptoken = new token(syn);//加变量
                //YUYI
                nextSyn();
                if(syn.kind == 22){  //  [
                    //System.out.println("<声明类型拓展>\t->\t\t[int值]");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>\t->\t\t[int值]\n";
                    if(wordList.get(sign+1).kind == 9||wordList.get(sign+1).content.equals("0")||wordList.get(sign+1).kind == 26)
                    {
                        lastSyn();
                        yuyiErrorExi = 0;
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
                        entrance.yuyiError = entrance.yuyiError + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
                        arrayR = false;//4
                        nextSyn();
                    }
                    result = decStatementExtend();
                    nextSyn();
                    //System.out.println(syn.content); //???一期疑惑
                    if(syn.kind == 12){  //  =
                        region.setIfValued(true,temptoken);//YUYI
                        //System.out.println("<声明内容>\t\t->\t\t=<声明内容拓展>");
                        entrance.parserResult = entrance.parserResult + "<声明内容>\t\t->\t\t=<声明内容拓展>\n";
                        result = decStatementContent();
                    }
                    else{
                        result = 1; //待修改
                    }

                }
                else if(syn.kind == 12){  // =
                    region.setIfValued(true,temptoken);//YUYI
                    //System.out.println("<声明类型拓展>\t->\t\t∂");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>\t->\t\t∂\n";
                    //System.out.println("<声明内容>\t\t->\t\t=<声明内容拓展>");
                    entrance.parserResult = entrance.parserResult + "<声明内容>\t\t->\t\t=<声明内容拓展>\n";
                    result = decStatementContent();
                }
                else{
                    //System.out.println("<声明类型拓展>\t->\t\t∂");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>\t->\t\t∂\n";
                    //System.out.println("<声明内容>\t\t->\t\t∂");
                    entrance.parserResult = entrance.parserResult + "<声明内容>\t\t->\t\t∂\n";
                }
                if(syn.kind == 19){  //  ;
                    result = 1;
                    //System.out.println("声明语句处理结束");
                    entrance.parserResult = entrance.parserResult + "声明语句处理结束\n";
                }
                else{
                    result = 1;
                    elseError = 0;  //TGQ
                    //System.out.println((syn.line) + "行声明语句缺少“;”");    //TGQYU
                    entrance.parserResult = entrance.parserResult + (syn.line) +"行声明语句缺少“;”\n";
                    entrance.parserError = entrance.parserError + (syn.line) +"行声明语句缺少“;”\n";
                    lastSyn();//TGQYUFA 加入本行恢复
                }
            }
            else{//缺少变量标识符号
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行变量名定义错误");
                entrance.parserResult = entrance.parserResult + syn.line + "行变量名定义错误\n";        //TGQYUFA找到下一个真正的;分隔符号 或者换行
                entrance.parserError = entrance.parserError + syn.line + "行变量名定义错误\n";
                //TGQYUFA
                int end = 0;//  期望最后一个是;
                //int left = 0;// { 没有出现
                int tempLineDec = syn.line;
                while(end == 0)
                {
                    nextSyn();
                    if(syn.line != tempLineDec)
                    {
                        end =1;
                        lastSyn();//取最后一行一个
                    }
                }
                //TGQYUFA
            }
        }
        return result;
    }

    public int decStatementExtend(){
        int result = 1;
        int temp = 0;
        if(syn.kind == 22){  //数组声明 [int值]  [
            nextSyn();
            if(syn.kind == 25){  //int值
                //YUYI二期
                int tempInt = Integer.parseInt(syn.content);
                lastSyn();
                lastSyn();  //变量名字的时候
                lastSyn();
                int tempType = syn.kind;
                nextSyn();
                if(tempType == 6||tempType == 7)
                {
                    //SemanticAnalysis.setArraySize(temptoken, Integer.parseInt(syn.content));
                    region.setArraySize(syn, tempInt);//数组标识位已经是数组了 数组大小
                }
                nextSyn();
                nextSyn();
                //YUYI二期
                //System.out.println("int值\t\t\t->\t\t" + syn.content);
                temp =  Integer.parseInt(syn.content);//YUYI
                entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content+"\n";
                nextSyn();
                if(syn.kind == 23){  // ]
                    result = 1;
                    region.setArray(temptoken, temp);//YUYI ?????
                }
                else{
                    elseError = 0;  //TGQ
                    //System.out.println("数组声明错误！-缺少“]”");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组声明错误！-缺少“]”\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组声明错误！-缺少“]”\n";
                    arrayR = false;
                    result = 1;
                    //TGQYUFA
                    if(syn.kind == 12)//=
                    {
                        lastSyn();
                    }
                    else {
                        //语法中断
                        //TGQYUFA
                        int end = 0;//  期望最后一个是;
                        int tempLineDec = syn.line;
                        while(end == 0)
                        {
                            nextSyn();
                            if(syn.line != tempLineDec)
                            {
                                end =1;
                                lastSyn();//取回去最后一个
                            }
                        }
                        //TGQYUFA
                    }
                    //lastSyn();
                }
            }
            else{
                elseError = 0;  //TGQ
                //TGQYUFA
                if(syn.kind == 26)
                {
                    elseError = 0;
                    //yuyiErrorExi = 0;
                    //System.out.println("数组声明错误！出现real值");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！下标不能是real值\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！下标不能是real值\n";
                    //entrance.yuyiError = entrance.yuyiError + "第"+syn.line+"行数组表示错误！下标不能是real值\n";
                    arrayR = false;
                    nextSyn();
                    if(syn.kind == 23)  //]
                    {
                        //System.out.println("】！！！！！！！");
                        //entrance.parserResult = entrance.parserResult + "】！！！！！！！\n";
                        //TGQYUFA 没问题
                    }
                    else if(syn.kind == 12) //=
                    {
                        elseError = 0;
                        //缺少]
                        //System.out.println("数组声明错误！缺少']'");
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！缺少']'\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！缺少']'\n";
                        arrayR = false;
                        lastSyn();
                    }
                }else if(syn.kind == 23||syn.kind == 12) //] =
                {
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组声明错误！-缺少“int值”\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组声明错误！-缺少“int值”\n";
                    arrayR = false;
                    if(syn.kind == 12)  //=
                    {
                        elseError = 0;
                        //缺少 ]
                        //System.out.println("数组声明错误！-缺少“]”");
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！-缺少“]”\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！-缺少“]”\n";
                        arrayR = false;
                        //lastSyn();
                    }
                }else if(syn.kind == 9){//???
                    decSExtendIF = 1;//非法中断
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    if(wordList.get(sign-4).kind != 4){
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误!\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误!\n";
                    arrayR = false;
                    }
                }else {
                    decSExtendIF = 1;//非法中断
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误!\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误!\n";
                    arrayR = false;
                }

                //TGQYUFA 持续执行
                nextSyn();
                if(syn.kind == 12)  //=
                {
                    lastSyn();
                }else
                {
                    decSExtendIF = 1;//中断
                }
                if(decSExtendIF == 1)
                {
                    int tDL1 = syn.line;
                    int tDL2 = syn.line;
                    while(tDL1 == tDL2)
                    {
                        nextSyn();
                        tDL2 = syn.line;
                    }
                    lastSyn();
                    lastSyn();
                }
                //其他的中断
                //原来的
                //System.out.println("数组声明错误！-缺少“int值”");
                //entrance.parserResult = entrance.parserResult + "数组声明错误！-缺少“int值”\n";
                //原来的
                result = 1;
            }
        }
        return temp;
    }

    public int decStatementContent(){
        int result = 1;
        //4
        token decRe;
        //4
        //现在是=
        //TGQYUYI
        lastSyn();//是否是]
        int typeCom = syn.kind;//是否是23 ]
        int lineCom = syn.line;
        nextSyn();
        //TGQYUYI
        nextSyn();
        if(syn.kind == 20){  //  {;
            if(typeCom != 23)
            {
                yuyiErrorExi = 0;
                entrance.parserResult = entrance.parserResult +"第"+lineCom +"行数据集合不能赋值给非数组变量!\n";
                entrance.yuyiError = entrance.yuyiError +"第"+lineCom+"行数据集合不能赋值给非数组变量!\n";
                //4
//                entrance.parserResult = entrance.parserResult + "第"+lineCom +"行数据集合不能赋值给非数组变量!无法执行！\n";
//                entrance.wholeExcute = entrance.wholeExcute + "第"+lineCom +"行数据集合不能赋值给非数组变量!无法执行！\n";
                arrayR = false;
                notArrayR = false;//无实际意义
            }
            entrance.parserResult = entrance.parserResult + "<声明内容拓展>  ->\t\t{<数组元素集>}\n";
            //处理数组元素集
            nextSyn();
            result = arrayElement();
            //if((firstMetBlock == true&&firstWExc == false)||ifR==-1||ifR == 0||elseR == -1||elseR==0)//只是判断
            if(region.sExcute == false)
            {
                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
                entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行只是检查语法语义，不执行！\n";
            }
            //else if(firstMetBlock == false||(firstMetBlock == true&&firstWExc == true)||ifR == 1||elseR==1)//开始执行
            else
            {
                if(arrayR == true)
                {
                    if(arrayLose == true)
                    {
                        entrance.parserResult = entrance.parserResult + "第"+wordList.get(sign-1).line+"行数组数据有效性缺失";
                        entrance.wholeExcute = entrance.wholeExcute + "第"+wordList.get(sign-1).line+"行数组数据有效性缺失\n";
                    }
                    int ShuzuNowT = region.getType(temptoken);
                    String SNT = "";
                    if(ShuzuNowT == 25)
                    {
                        SNT = "int";
                    }else if(ShuzuNowT == 26)
                    {
                        SNT = "real";
                    }
                    String ShuzuNow = "第"+syn.line+"行处理结果如下一行所示："+SNT+" "+temptoken.content+"["+region.getArraySize(temptoken)+"] = {";
                    int outType = region.getType(temptoken);
                    //entrance.wholeExcute = entrance.wholeExcute+ outType;//4
                    int outSize = region.getArraySize(temptoken);
                    //entrance.wholeExcute = entrance.wholeExcute+ outSize;//4
                    //4数组输出
                    for(int i = 0;i < outSize;i++)
                    {
                        if(outType== 25)
                        {
                            ShuzuNow = ShuzuNow + region.getIntArrayV(temptoken,i);
                        }else if(outType == 26)
                        {
                            ShuzuNow = ShuzuNow + region.getFloatArrayV(temptoken,i);
                        }
                        if(i != outSize-1)
                        {
                            ShuzuNow = ShuzuNow +";";
                        }else {
                            ShuzuNow = ShuzuNow +"};\n";
                        }
                    }
                    entrance.parserResult = entrance.parserResult + ShuzuNow;//4
                    entrance.wholeExcute = entrance.wholeExcute+ ShuzuNow;//4
                }else {//4
                    yuyiErrorExi = 0;
                    entrance.parserResult = entrance.parserResult + "第"+wordList.get(sign-1).line+"行数组不是有效的,无法执行\n";
                    entrance.yuyiError = entrance.yuyiError + "第"+wordList.get(sign-1).line+"行数组不是有效的，无法执行\n";
                }
            }
            if(syn.kind == 21){  //  }
                result = 1;
                //YUYI
                //if(region.IfArrayOver(temptoken, count))
                if(region.getArraySize(temptoken)<=count)//？
                {
                    yuyiErrorExi = 0;
                    //System.out.printf("误出现在第错%d行，数组变量%s越界。 \n", temptoken.line,temptoken.content);
                    entrance.parserResult = entrance.parserResult + "错误出现在第"+temptoken.line+"行，数组变量"+temptoken.content+"越界。 \n";
                    entrance.yuyiError = entrance.yuyiError +"错误出现在第"+temptoken.line+"行，数组变量"+temptoken.content+"越界。 \n";

                }else
                {
                    if(region.sExcute == false)//只是判断
                    {
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
                        entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行只是检查语法语义，不执行！\n";
                    }
                    else
                    {
                        if(arrayR == true)
                        {
                            int realType = region.getType(temptoken);
                            while(count<region.getArraySize(temptoken)-1)
                            {
                                ++count;
                                if(realType == 25)
                                {
                                    region.setvalue(temptoken,0,count);//4
                                }else if(realType == 26)
                                {
                                    region.setvalue(temptoken,(float)0.0,count);//4
                                }
                            }
                        }
                    }
                }
                //YUYI
                nextSyn();
            }
            else{
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行缺少“}”");
                entrance.parserResult = entrance.parserResult + syn.line + "行缺少“}”\n";
                entrance.parserError = entrance.parserError + syn.line + "行缺少“}”\n";
            }
        }
        else{
            //System.out.println("<声明内容拓展>  ->\t\t<表达式>");
            if(typeCom == 23)
            {
                yuyiErrorExi = 0;
                entrance.parserResult = entrance.parserResult +"第"+lineCom +"行表达式不能在声明时赋值给数组!\n";
                entrance.yuyiError = entrance.yuyiError +"第"+lineCom+"行表达式不能在声明时赋值给数组!\n";
                notArrayR = false;
            }
            entrance.parserResult = entrance.parserResult + "<声明内容拓展>  ->\t\t<表达式>\n";

            //4执行
            decRe = expression();
            if(region.sExcute == false)//只是判断
            {
                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
                entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行只是检查语法语义，不执行！\n";
            }
            else//开始执行
            {
                if (decRe.kind == 0) {
                    entrance.parserResult = entrance.parserResult + "第" + syn.line + "行声明语句表达式有误，无法给出结果！\n";
                    entrance.parserError = entrance.parserError + "第" + syn.line + "行运行时错误，无法给出结果！\n";
                    entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行声明语句表达式有误，无法给出结果！\n";
                    entrance.writeCon = entrance.writeCon + "第" + syn.line + "行声明语句表达式有误，无法给出结果！\n";
                } else {
                    int realType = region.getType(temptoken);
                    if (decRe.kind != realType) {
                        entrance.parserResult = entrance.parserResult + "第" + syn.line + "行声明语句执行后数据有损！\n";
                        entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行声明语句执行后数据有损！\n";
                    }
                    if (realType == 25) {
                        //int tempV;
                        if (decRe.kind != 25) {
                            //转换
                            region.setvalue(temptoken, (int) Float.parseFloat(decRe.content));
                        } else {
                            region.setvalue(temptoken, Integer.parseInt(decRe.content));
                        }
                        entrance.parserResult = entrance.parserResult + "第" + syn.line + "行声明语句执行后：" + temptoken.content + " = " + region.getIntV(temptoken) + "\n";
                        entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行声明语句执行后：" + temptoken.content + " = " + region.getIntV(temptoken) + "\n";
                    } else if (realType == 26) {
                        if (decRe.kind != 26) {
                            //转换
                            region.setvalue(temptoken, (float) Integer.parseInt(decRe.content));
                        } else {
                            region.setvalue(temptoken, Float.parseFloat(decRe.content));
                        }
                        entrance.parserResult = entrance.parserResult + "第" + syn.line + "行声明语句执行后：" + temptoken.content + " = " + region.getFloatV(temptoken) + "\n";
                        entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行声明语句执行后：" + temptoken.content + " = " + region.getFloatV(temptoken) + "\n";
                    }
                }
            }
        }
        return result;
    }

    /**
     * 赋值语句
     */
    public int assStatement(){
        int result = 1;
        if(syn.kind == 24){   //变量名
            //System.out.println("变量名ID\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "变量名ID\t\t->\t\t" + syn.content +"\n";
            //YUYI
            //在Reco里判断有无
            if(!region.IsExist(syn)) {
                yuyiErrorExi = 0;
                //System.out.printf("错误出现在第%d行，使用了未定义变量%s. \n", syn.line, syn.content);
                entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，使用了未定义变量"+syn.content+". \n";
                entrance.yuyiError = entrance.yuyiError+ "错误出现在第"+syn.line+"行，使用了未定义变量"+syn.content+". \n";
                isDefT = false;
            }
            else {
                temptoken = new token(syn);
                //region.clean(syn);
                isDefT = true;
            }
            //YUYI
            nextSyn();
            result = assStatementExtend();
            //if(syn.kind == 19 && result == 1){//；
            if(syn.kind == 19){//；
                //System.out.println("赋值语句分析完成");
                entrance.parserResult = entrance.parserResult + "赋值语句分析完成\n";
            }
            else{
                elseError = 0;  //TGQ
                //TGQYUFA
                lastSyn();//回到上一个单词
                //System.out.println(syn.line + "行赋值语句出现语法错误，表达式末端期待';'");
                elseError = 0;
                entrance.parserResult = entrance.parserResult + syn.line + "行赋值语句出现语法错误\n";
                entrance.parserError = entrance.parserError + syn.line + "行赋值语句出现语法错误\n";
                nextSyn();//错误的单词
                //if(syn.kind == 6 || syn.kind == 7 || syn.kind == 24 || syn.kind == 5 || syn.kind == 4 || syn.kind == 1 || syn.kind == 3)
                while(!(syn.kind == 6 || syn.kind == 7 || syn.kind == 24 || syn.kind == 5 || syn.kind == 4 || syn.kind == 1 || syn.kind == 3||syn.kind ==21||syn.kind ==2))//21 2是if while中特殊的情况
                {
                    nextSyn();
                }
                lastSyn();
                //TGQYUFA
            }
        }
        return result;
    }

    public int assStatementExtend(){
        int result = 0;
        token assRe = new token(0,null,0);
        if(syn.kind == 12){  //  =
            region.setIfValued(true,temptoken);//YUYI
            //TGQYUYI
            lastSyn();
            boolean isAssArr = region.getIsArray(syn);
            if(isAssArr == true)
            {
                //应该是数组
                yuyiErrorExi = 0;
                entrance.parserResult = entrance.parserResult +"第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                entrance.yuyiError = entrance.yuyiError + "第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
            }
            nextSyn();
            //System.out.println("<赋值语句拓展>->\t\t=<表达式>");
            entrance.parserResult = entrance.parserResult + "<赋值语句拓展>->\t\t=<表达式>\n";
            nextSyn();
            //4
            assRe = expression();//无需执行有时候
                if(isAssArr == true)
                {
                    yuyiErrorExi = 0;
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+ "行赋值语句引用数组变量"+syn.content+"的时候缺少下标'[非负数]'，无法执行\n";
                    entrance.yuyiError = entrance.yuyiError + "第"+syn.line+ "行赋值语句引用数组变量"+syn.content+"的时候缺少下标'[非负数]'，无法执行\n";
                    //entrance.wholeExcute += "第"+syn.line+ "行赋值语句引用数组变量"+syn.content+"的时候缺少下标'[非负数]'，无法执行\n";
                }
                else {
                    int realType = region.getType(temptoken);
                    if(realType == 0)
                    {
                        yuyiErrorExi = 0;
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+ "行赋值语句变量有误，无法执行\n";
                        entrance.yuyiError = entrance.yuyiError + "第"+syn.line+ "行赋值语句变量，无法执行\n";
                        //entrance.wholeExcute += "第"+syn.line+ "行赋值语句的表达式有误，无法执行\n";
                    }else {
                        if(region.sExcute == false)//只是判断
                        {
                            entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
                            entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行只是检查语法语义，不执行！\n";
                        }
                        else//开始执行
                        {
                            if(assRe.kind == 0)
                            {
                                entrance.parserResult = entrance.parserResult + "第" + syn.line + "行赋值语句表达式发生运行时错误，无法给出结果！\n";
                                entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行赋值语句表达式发生运行时错误，无法给出结果！\n";
                                entrance.writeCon = entrance.writeCon +"第" + syn.line + "行赋值语句表达式发生运行时错误，无法给出结果！\n";
                            }
                            else {
                                if (assRe.kind != realType) {
                                    entrance.parserResult = entrance.parserResult + "第" + syn.line + "行赋值语句执行后数据有损！（可以执行）\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行赋值语句执行后数据有损！(可以执行)\n";
                                }
                                if (realType == 25) {
                                    //int tempV;

                                    if (assRe.kind != 25) {
                                        //转换
                                        region.setvalue(temptoken, (int) Float.parseFloat(assRe.content));
                                    } else {
                                        region.setvalue(temptoken, Integer.parseInt(assRe.content));
                                    }
                                    entrance.parserResult = entrance.parserResult + "第" + syn.line + "行赋值语句执行后：" + temptoken.content + " = " + region.getIntV(temptoken) + "\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行赋值语句执行后：" + temptoken.content + " = " + region.getIntV(temptoken) + "\n";
                                    //System.out.println(temptoken.content+" = "+region.getIntV(temptoken));

                                } else if (realType == 26) {
                                    if (assRe.kind != 26) {
                                        //转换
                                        region.setvalue(temptoken, (float) Integer.parseInt(assRe.content));
                                    } else {
                                        region.setvalue(temptoken, Float.parseFloat(assRe.content));
                                    }
                                    entrance.parserResult = entrance.parserResult + "第" + syn.line + "行赋值语句执行后：" + temptoken.content + " = " + region.getFloatV(temptoken) + "\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第" + syn.line + "行赋值语句执行后：" + temptoken.content + " = " + region.getFloatV(temptoken) + "\n";
                                    //System.out.println(temptoken.content+" = "+region.getIntV(temptoken));
                                }
                            }

                        }
                    }

                }
            //}

            result = 1;
        }
        else if(syn.kind == 22){   //  [
            //System.out.println("<赋值语句拓展>->\t\t[int值]=<表达式>");
            entrance.parserResult = entrance.parserResult + "<赋值语句拓展>->\t\t[int值]=<表达式>\n";
            nextSyn();

            boolean canExc = true;//可以执行 4

            if(syn.kind == 25){   // int值
                //YUYI二期

                int tempLocation2 = Integer.parseInt(syn.content);//取下标
                lastSyn();
                lastSyn();
                //if(SemanticAnalysis.getArraySize(temptoken)<Integer.parseInt(syn.content)+1)
                //是否是数组
                boolean isArr = region.getIsArray(syn);
                    if(isArr == false)
                    {
                        yuyiErrorExi = 0;
                        entrance.parserResult = entrance.parserResult +"错误出现在第"+syn.line+"行，"+syn.content+"不是数组，无法执行！\n";
                        entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，"+syn.content+"不是数组，无法执行！\n";
                        canExc = false;
                    }
                    else if(region.getArraySize(syn)<tempLocation2 + 1)
                    {
                        yuyiErrorExi = 0;
                        entrance.parserResult = entrance.parserResult +"错误出现在第"+syn.line+"行，数组"+syn.content+"下标越界，无法执行！ \n";
                        entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组"+syn.content+"下标越界，无法执行！\n";
                        canExc = false;
                    }

                nextSyn();
                nextSyn();
                //YUYI二期
                //System.out.println("int值\t\t\t->\t\t" + syn.content);
                entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content +"\n";
                nextSyn();
                if(syn.kind == 23){   //   ]
                    nextSyn();
                    if(syn.kind == 12){  //   =
                        region.setIfValued(true,temptoken);//YUYI
                        nextSyn();
                        //4执行
                        assRe = expression();
//                        if(assRe != null){
//                            result = 1;
//                        }
                        if(region.sExcute == false)//只是判断
                        {
                            entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
                            entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行只是检查语法语义，不执行！\n";
                        }
                        else//开始执行
                        {
                            if(assRe.kind == 0)
                            {
                                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行赋值语句表达式运行时错误，无法给出结果！\n";
                                //entrance.parserError = entrance.parserError + "第"+syn.line+"行运行时错误，无法给出结果！\n";
                                entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行赋值语句表达式有误，无法给出结果！\n";
                                entrance.writeCon = entrance.writeCon + "第"+syn.line+"行赋值语句表达式发生运行时错误，无法给出结果！\n";
                            }else {
                                int realType = region.getType(temptoken);
                                if(assRe.kind != realType)
                                {
                                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行赋值语句执行后数据有损！\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行赋值语句执行后数据有损！\n";
                                }
                                if(realType == 25)
                                {
                                    //int tempV;
                                    if(assRe.kind!=25)
                                    {
                                        //转换
                                        region.setvalue(temptoken,(int)Float.parseFloat(assRe.content),tempLocation2);
                                    }else {
                                        region.setvalue(temptoken,Integer.parseInt(assRe.content),tempLocation2);
                                    }
                                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行赋值语句执行后："+temptoken.content+"["+tempLocation2+"] = "+region.getIntArrayV(temptoken,tempLocation2)+"\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行赋值语句执行后："+temptoken.content+"["+tempLocation2+"] = "+region.getIntArrayV(temptoken,tempLocation2)+"\n";
                                    //System.out.println(temptoken.content+" = "+region.getIntV(temptoken));
                                    //System.out.println("!!!!!!!!!!!!hahah");
                                }else if(realType == 26)
                                {
                                    if(assRe.kind != 26)
                                    {
                                        //转换
                                        region.setvalue(temptoken,(float)Integer.parseInt(assRe.content),tempLocation2);
                                    }else {
                                        region.setvalue(temptoken,Float.parseFloat(assRe.content),tempLocation2);
                                    }
                                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行赋值语句执行后："+temptoken.content+"["+tempLocation2+"] = "+region.getFloatArrayV(temptoken,tempLocation2)+"\n";
                                    entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line+"行赋值语句执行后："+temptoken.content+"["+tempLocation2+"] = "+region.getFloatArrayV(temptoken,tempLocation2)+"\n";
                                    //System.out.println(temptoken.content+" = "+region.getFloatV(temptoken));
                                }
                            }
                        }

                        //执行
                    }
                    else{
                        elseError = 0;  //TGQ
                        //System.out.println(syn.line + "行缺少“=”");
                        entrance.parserResult = entrance.parserResult +syn.line + "行缺少“=”\n";
                        entrance.parserError = entrance.parserError + syn.line + "行缺少“=”\n";
                        canExc = false;
                    }
                }
                else{
                    elseError = 0;  //TGQ
                    //System.out.println(syn.line + "行缺少“]”");
                    entrance.parserResult = entrance.parserResult + syn.line +"行缺少“]”\n";
                    entrance.parserError = entrance.parserError + syn.line +"行缺少“]”\n";
                    canExc = false;
                }
            }
            else{
                result = 1;
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行缺少int值");
                entrance.parserResult = entrance.parserResult + syn.line +"行缺少int值(非负)\n";
                entrance.parserError = entrance.parserError + syn.line +"行缺少int值\n";
                entrance.parserResult = entrance.parserResult  + "第"+syn.line +"行引用数组变量"+temptoken.content+"缺少int值(非负)\n";
                canExc = false;//4
                if(canExc == false)
                {
                    entrance.wholeExcute = entrance.wholeExcute + "第"+syn.line +"行引用数组变量"+temptoken.content+"缺少int值(非负)\n";
                }
                //TGQYUFA
                jump();// 待修改 检查
                //TGQYUFA
            }
        }
        return result;
    }
    /**
     * 输出语句
     */
    public int writeStatement(){
        int result = 0;
        nextSyn();
        token writeRe = new token(0,null,0);
        if(syn.kind == 17){    //  (
            nextSyn();
            inSentence = false;
            writeRe = expression();//4
            inSentence = true;
            //System.out.println(writeRe.kind+"!!!!!!");
            if(syn.kind == 18){   //    )
                nextSyn();
                if(syn.kind == 19){   //   ;
                    result = 1;
                }
                else{
                    elseError = 0;  //TGQYU
                    //System.out.println((syn.line - 1)+ "行缺少“;”");
                    entrance.parserResult = entrance.parserResult +(syn.line - 1) + "行缺少“;”\n";
                    entrance.parserError = entrance.parserError + (syn.line - 1) + "行缺少“;”\n";
                    //TGQYUFA
                    lastSyn();
                    jump();
                    //TGQYUFA
                }
            }
            else{
                elseError = 0;  //TGQYU
                //System.out.println((syn.line) + "行缺少“)”");
                entrance.parserResult = entrance.parserResult +(syn.line) + "行缺少“)”\n";
                entrance.parserError = entrance.parserError + (syn.line) + "行缺少“)”\n";
                //TGQYUFA
                lastSyn();
                jump();
                //TGQYUFA
            }
        }
        else{
            elseError = 0;  //TGQYU
            //System.out.println((syn.line) + "行缺少“(”");
            entrance.parserResult = entrance.parserResult +(syn.line) + "行缺少“(”\n";
            entrance.parserError = entrance.parserError + (syn.line) + "行缺少“(”\n";
            //TGQYUFA
            lastSyn();
            jump();
            //TGQYUFA
        }
        //4 结果的输出
        if(region.sExcute == false)//只是判断
        {
            entrance.parserResult = entrance.parserResult + "第"+syn.line+"行只是检查语法语义，不执行！\n";//实际定位
            entrance.wholeExcute = entrance.wholeExcute+ "第"+syn.line+"行只是检查语法语义，不执行！\n";
            entrance.writeCon = entrance.writeCon + "第"+syn.line+"行只是检查语法语义，不执行！\n";
            //System.out.println("只是判断");
        }
        else//开始执行
        {
            if(writeRe.kind == 0)
            {
                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行表达式有误，write()无法给出结果！\n";
                entrance.parserError = entrance.parserError + "第"+syn.line+"行表达式有误，write()无法给出结果！\n";
                entrance.wholeExcute = entrance.wholeExcute+ "第"+syn.line+"行表达式有误，write()无法给出结果！\n";
                entrance.writeCon = entrance.writeCon + "第"+syn.line+"行表达式有误，write()无法给出结果！\n";
            }else
            {
                if(writeRe.kind == 25)
                {
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行结果："+writeRe.content +"(int)\n";
                    entrance.wholeExcute = entrance.wholeExcute+"第"+syn.line+"行结果："+writeRe.content +"(int)\n";
                    entrance.writeCon = entrance.writeCon + "第"+syn.line+"行结果："+writeRe.content +"(int)\n";
                }
                else if(writeRe.kind == 26)
                {
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行结果："+writeRe.content +"(real)\n";
                    entrance.wholeExcute = entrance.wholeExcute+"第"+syn.line+"行结果："+writeRe.content +"(real)\n";
                    entrance.writeCon = entrance.writeCon + "第"+syn.line+"行结果："+writeRe.content +"(real)\n";
                }
                //System.out.println(writeRe.content+"!");
            }
        }
        return result;
    }

    /**
     * 输入语句
     */
    public int readStatement(){
        //TGQ加的
        //TGQ执行
        boolean readBlock = true;//true要调用阻塞函数
        int readBlockArrLoc = -1;//数组下标
        //TGQ执行
        int fuhao = 0;
        int result = 0;
        nextSyn();
        if(syn.kind == 17){    //  (
            nextSyn();
            if(syn.kind == 24){    //变量名
                //System.out.println("变量名ID\t\t->\t\t" + syn.content);
                entrance.parserResult = entrance.parserResult + "变量名ID\t\t->\t\t" + syn.content +"\n";
                //是否声明
                boolean isReDefine = region.IfRep(syn);//false是没有声明
                boolean readIsArr = region.getIsArray(syn);
                int readLine = syn.line;
                String readCon = syn.content;
                if(isReDefine == false)//未定义
                {
                    yuyiErrorExi = 0;
                    entrance.parserResult = entrance.parserResult +"第"+readLine+ "行变量"+syn.content+"未定义！\n";
                    entrance.yuyiError = entrance.yuyiError + "第"+readLine+"行变量"+syn.content+"未定义！\n";
                    readBlock = false;//田国庆
                }else {
                    temptoken = new token(syn);//阻塞用
                }
                nextSyn();
                if(syn.kind == 22){  //   [
                    //System.out.println("<声明类型拓展>->\t\t[int值]");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>->\t\t[int值]\n";
                    if(isReDefine == true)
                    {
                        if(readIsArr == false)
                        {
                            yuyiErrorExi = 0;
                            entrance.parserResult = entrance.parserResult + "错误出现在第"+readLine+"行，"+readCon+"不是数组 \n";
                            entrance.yuyiError = entrance.yuyiError + "错误出现在第"+readLine+"行，"+readCon+"不是数组 \n";
                            readBlock = false;
                        }else {
                            //是否越界以及其他的错误 待修改
                            //read问题
//                            lastSyn();
//                            temptoken = new token(syn);
//                            nextSyn();
                            //上面都有问题
                            if(wordList.get(sign+1).kind==9||wordList.get(sign+1).kind == 26)
                            {
                                lastSyn();
                                fuhao = 1;
                                elseError = 0;
                                //yuyiErrorExi = 0;
                                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组"+syn.content+"下标必须是非负整数\n";
                                entrance.parserError = entrance.parserError + "第"+syn.line+"行数组"+syn.content+"下标必须是非负整数\n";
                                readBlock = false;
                                //entrance.yuyiError = entrance.yuyiError + "第"+syn.line+"行数组"+syn.content+"下标必须是非负整数\n";
                                nextSyn();
                            }else if(wordList.get(sign+1).kind == 25)
                            {
                                int tempL = Integer.parseInt(wordList.get(sign+1).content);
                                readBlockArrLoc = tempL;
                                //System.out.println(readBlockArrLoc);
                                if(region.getArraySize(temptoken)< tempL + 1)
                                {
                                    yuyiErrorExi = 0;
                                    entrance.parserResult = entrance.parserResult + "错误出现在第"+readLine+"行，数组"+temptoken.content+"下标越界 \n";
                                    entrance.yuyiError = entrance.yuyiError + "错误出现在第"+readLine+"行，数组"+temptoken.content+"下标越界 \n";
                                    readBlock = false;
                                }else {
                                    readBlock = true;
                                }
                            }
                        }
                    }
                    //现在是[
                    decStatementExtend();//末尾在]
//                    if(fuhao == 1)
//                    {
//                        lastSyn();
//                    }
                    nextSyn();
                }
                else{
                    //System.out.println("<声明类型拓展>->\t\t∂");
                    if(isReDefine == true)
                    {
                        if(readIsArr == true)
                        {
                            yuyiErrorExi = 0;
                            entrance.parserResult = entrance.parserResult + "第"+readLine+"行引用数组变量"+readCon+"的时候缺少下标'[非负数]'\n";
                            entrance.yuyiError = entrance.yuyiError+ "第"+readLine+"行引用数组变量"+readCon+"的时候缺少下标'[非负数]'\n";
                            readBlock = false;
                        }
                        readBlock = true;
                    }
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>->\t\t∂\n";
                }

            }
            if(syn.kind == 18){   //    )
                nextSyn();
                if(syn.kind == 19){   //   ;
                    result = 1;
                    //return result;
                }
                else{
                    elseError = 0;  //TGQYU
                    //System.out.println((syn.line -1) + "行缺少“;”");
                    entrance.parserResult = entrance.parserResult + (syn.line -1) +"行缺少“;”\n";   //TGQYUFA
                    entrance.parserError = entrance.parserError + (syn.line -1) +"行缺少“;”\n";
                    readBlock = false;
                    //TGQYUFA
                    lastSyn();
                    jump();
                    //TGQYUFA
                }
            }
            else{
                elseError = 0;  //TGQYU
                //System.out.println((syn.line) + "行缺少“)”");
                entrance.parserResult = entrance.parserResult + (syn.line) + "行缺少“)”\n";
                entrance.parserError = entrance.parserError + (syn.line) + "行缺少“)”\n";
                //TGQYUFA
                lastSyn();
                jump();
                readBlock = false;
                //TGQYUFA
            }
        }
        else{
            elseError = 0;  //TGQYU
            //System.out.println((syn.line) + "行缺少“(”");
            entrance.parserResult = entrance.parserResult + (syn.line) + "行缺少“(”\n";
            entrance.parserError = entrance.parserError + (syn.line) + "行缺少“(”\n";
            //TGQYUFA
            lastSyn();
            jump();
            readBlock = false;
            //TGQYUFA
        }
        //阻塞
        //System.out.println(readBlock+"!!!!!!!1");
        if(readBlock)
        {
            //函数调用
            //赋值 是否是数组 类型
            //System.out.println(region.getIsArray(temptoken));
        }
        return result;
    }

    /**
     * if语句
     */
    public int ifStatement(){
        int result = 0;
        int ifR = 1;
        int elseR = 1;
        int ifStart = -1;
        int elseStart = -1;
        //int tempelseR = -1;
        if(syn.kind == 1){  // if
            ifStart = sign;
            nextSyn();
            if(syn.kind == 17){  // (
                //TGQYUFA 空判断体
                nextSyn();//前看一步
                if(syn.kind == 18||syn.kind == 20)//) {
                {
                    elseError = 0;
                    //System.out.println(syn.line+"行if主程序体内没有代码！");
                    entrance.parserResult = entrance.parserResult + syn.line+"行if判断体内没有代码！\n" ;
                    entrance.parserError = entrance.parserError + syn.line+"行if判断体内没有代码！\n";
                    if(syn.kind == 20)
                    {
                        entrance.parserResult = entrance.parserResult + syn.line+"if语句缺少'{'！\n" ;
                        entrance.parserError = entrance.parserError + syn.line+"if语句缺少'{'！\n";
                    }
                    //4
                    //ifR = -1;
                    //tempelseR = -1;
                    //4
                }
                else {
                    lastSyn();
                    result = condition();
                    ifR = result;//4 hahahaha
                    if(ifR == -1)
                    {
                        elseR = -1;
                    }else if(ifR == 0)
                    {
                        elseR = 1;
                    }else if(ifR == 1){
                        elseR = 0;
                    }
//                    if(result == 0){
//                        return result;
//                    }
                }
                //TGQYUFA
                if(syn.kind == 18){  // )
                    nextSyn();
                    if(syn.kind == 20){  // {
                        //TGQYUFA
                        nextSyn();
                        if(syn.kind == 21||syn.kind == 2)
                        {
                            //TGQYUFA 主程序体内没有东西
                            elseError = 0;
                            //System.out.println(syn.line+"行if主程序体内没有代码！");
                            entrance.parserResult = entrance.parserResult + syn.line+"行if主程序体内没有代码！\n" ;
                            entrance.parserError = entrance.parserError + syn.line+"行if主程序体内没有代码！\n";
                            if(syn.kind == 2)
                            {
                                elseError = 0;
                                //System.out.println(syn.line+"行if主程序体缺少'}'！");
                                entrance.parserResult = entrance.parserResult + syn.line+"行if主程序体缺少'}'！\n" ;
                                entrance.parserError = entrance.parserError + syn.line+"行if主程序体缺少'}'！\n";
                                //往前1位TGQYUFA
                                lastSyn();
                            }
                        }
                        else {
                            //TGQYUFA
                            lastSyn();
                            //是否第二次进去的判断
                            boolean firstTime = true;
                            int loc = -1;
                            for(int i = 0;i < region.next.size();++i)
                            {
                                if(region.next.get(i).sifStart == ifStart)
                                {
                                    firstTime = false;
                                    loc = i;
                                }
                            }
                            if(firstTime == true)
                            {
                                //变量作用域
                                depth++;
                                SemanticAnalysis temp = new SemanticAnalysis();
                                temp.setFront(region);
                                region.next.add(temp);
                                region = temp;
                                region.sifStart = ifStart;
                                region.selseStart = -1;
                                //变量作用域
                            }else {
                                region = region.next.get(loc);
                            }
//                            //变量作用域
//                            depth++;
//                            SemanticAnalysis temp = new SemanticAnalysis();
//                            temp.setFront(region);
//                            region.next.add(temp);
//                            region = temp;
//                            //变量作用域
                            if(ifR == 1)
                            {
                                region.sExcute = true;
                                int bef = region.IFE();
                                if(bef != 0)
                                {
                                    region.sExcute = false;
                                }
                            }else if(ifR == 0||ifR == -1)
                            {
                                region.sExcute = false;
                            }
                            result = statements();
                            //4
                            //ifR = 1;
                            //elseR = tempelseR;
                            //4
                            if(syn.kind == 21){  // }
                                //ifEnd = sign;
                                //变量作用域
                                //4变量清空
                                region.Reco.removeAllElements();
                                //4
                                --depth;
                                region = region.getFront();
                                //变量作用域
                                //System.out.println("if语句处理结束");
                                entrance.parserResult = entrance.parserResult + "if语句处理结束\n";
                                result = 1;
                            }
                            else{
                                result = 1;
                                elseError = 0;  //TGQ
                                //System.out.println(syn.line + "行if语句缺少“}”");
                                entrance.parserResult = entrance.parserResult + syn.line + "行if语句缺少“}”\n";
                                entrance.parserError = entrance.parserError + syn.line + "行if语句缺少“}”\n";
                                //TGQYUFA
                                lastSyn();
                            }
                        }
                    }
                    else{
                        result = 1;
                        elseError = 0;  //TGQ
                        //System.out.println(syn.line + "行if语句缺少“{”");
                        entrance.parserResult = entrance.parserResult + syn.line + "行if语句缺少“{”\n";
                        entrance.parserError = entrance.parserError + syn.line + "行if语句缺少“{”\n";
                        //TGQYUFA
                        lastSyn();
                        statements();
                        //4
                        //ifR = 1;
                        //elseR = tempelseR;
                        //4
                    }
                }
                else{
                    elseError = 0;  //TGQYU
                    //System.out.println((syn.line-1) + "行if语句缺少“)”");
                    entrance.parserResult = entrance.parserResult + (syn.line-1) + "行if语句缺少“)”\n";
                    entrance.parserError = entrance.parserError + (syn.line-1) + "行if语句缺少“)”\n";
                    //IF忽略
                    jumpForIF();
                }
            }
            else{
                result = 1;
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行if语句缺少“(”");
                entrance.parserResult = entrance.parserResult + syn.line + "行if语句缺少“(”\n";
                entrance.parserError = entrance.parserError + syn.line + "行if语句缺少“(”\n";
                //IF忽略
                jumpForIF();//大中断
            }
        }
        nextSyn();
        if(syn.kind == 2){  // else
            //hasElse = true;//有的
            elseStart = sign;
            //System.out.println("<if扩展>\t\t->\t\telse{语句块}");
            entrance.parserResult = entrance.parserResult + "<if扩展>\t\t->\t\telse{语句块}\n";
            nextSyn();
            //4
            //4
            if(syn.kind == 20){ // {
                //TGQYUFA
                nextSyn();
                if(syn.kind == 21)   //}
                {
                    //TGQYUFA 主程序体内没有东西
                    elseError = 0;
                    //System.out.println(syn.line+"行else主程序体内没有代码！");
                    entrance.parserResult = entrance.parserResult + syn.line+"行else主程序体内没有代码！\n" ;
                    entrance.parserError = entrance.parserError + syn.line+"行else主程序体内没有代码！\n" ;
                }
                else {//TGQYUFA
                    lastSyn();
                    //4是否第一次
                    boolean firsttime = true;
                    int locala = -1;
                    for(int i = 0;i < region.next.size();++i)
                    {
                        if(region.next.get(i).selseStart == elseStart)
                        {
                            firsttime = false;
                            locala = i;
                        }
                    }
                    if(firsttime == true)
                    {
                        //变量作用域
                        depth++;
                        SemanticAnalysis temp = new SemanticAnalysis();
                        temp.setFront(region);
                        region.next.add(temp);
                        region = temp;
                        region.selseStart = elseStart;
                        //变量作用域
                    }
                    else
                    {
                        region = region.next.get(locala);
                    }
                    //4
//                    //变量作用域
//                    depth++;
//                    SemanticAnalysis temp = new SemanticAnalysis();
//                    temp.setFront(region);
//                    region.next.add(temp);
//                    region = temp;
//                    //变量作用域
                    //4是否执行
                    if(elseR == 1)
                    {
                        region.sExcute = true;
                        int bef = region.IFE();
                        if(bef != 0)
                        {
                            region.sExcute = false;
                        }
                    }else if(elseR == 0||elseR == -1)
                    {
                        region.sExcute = false;
                    }
                    //4是否执行
                    result = statements();
                    if(syn.kind == 21){  // }
                        result = 1;
                        //System.out.println("else语句处理结束");
                        //变量作用域
                        //清空
                        region.Reco.removeAllElements();
                        --depth;
                        region = region.getFront();
                        entrance.parserResult = entrance.parserResult + "else语句处理结束\n";
                        //elseEnd = sign;
                    }
                    else{
                        result = 1;
                        elseError = 0;  //TGQ
                        //System.out.println(syn.line + "行else语句缺少“}”");
                        entrance.parserResult = entrance.parserResult + syn.line + "行else语句缺少“}”\n";
                        entrance.parserError = entrance.parserError + syn.line + "行else语句缺少“}”\n";
                        //待修改
                        //往前1位TGQYUFA
                        //lastSyn();
                    }
                }
            }
            else{
                result = 1;
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行else语句缺少“{”");
                entrance.parserResult = entrance.parserResult + syn.line + "行else语句缺少“{”\n";
                entrance.parserError = entrance.parserError + syn.line + "行else语句缺少“{”\n";
                //往前1位TGQYUFA
                lastSyn();
                statements();
            }
        }
        else{
            //hasElse = false;//没有
            //System.out.println("<if扩展>\t\t->\t\t∂");
            entrance.parserResult = entrance.parserResult + "<if扩展>\t\t->\t\t∂\n";
            //TGQYUFA 无else时回溯
            lastSyn();
        }
        //firstMetBlock = false;//首次完成
        ifR = 1;
        elseR = 1;
        return result;
    }

    /**
     * while语句
     */
    public int whileStatement(){
        //第一次默认该执行
        //firstWExc = true;
        int result = 1;
        int conRe = -2;//???4逻辑
        ifJump = false;//没有跳转
//        firstMetBlock = true;
//        if(whileStart == sign)
//        {
//            firstMetBlock = false;
//            //entrance.wholeExcute+="while开始正式执行：\n";
//        }else
//        {
//            //无操作
//            entrance.wholeExcute+="while第一次只是检查:\n";
//            conRe = 1;
//        }
        if(syn.kind == 3){  // while
            int tempStart = sign;
            //whileStart = sign;
            nextSyn();
            if(syn.kind == 17){  // (
                //TGQYUFA 空判断体
                nextSyn();//前看一步
                if(syn.kind == 18||syn.kind == 20)//) {
                {
                    elseError = 0;
                    //System.out.println(syn.line+"行if主程序体内没有代码！");
                    entrance.parserResult = entrance.parserResult + syn.line+"行while判断体内没有代码！\n" ;
                    entrance.parserError = entrance.parserError + syn.line+"行while判断体内没有代码！\n";
                    if(syn.kind == 20)
                    {
                        entrance.parserResult = entrance.parserResult + syn.line+"while语句缺少'{'！\n" ;
                        entrance.parserError = entrance.parserError + syn.line+"while语句缺少'{'！\n";
                    }
                }
                else {
                    lastSyn();
                    //firstMetBlock = true;//首次执行
                    result = condition();
                    conRe = result;
                }

                if(syn.kind == 18){  // )
                    nextSyn();
                    if(syn.kind == 20){  // {
                        //TGQYUFA
                        nextSyn();
                        if(syn.kind == 21)   // }
                        {
                            //TGQYUFA 主程序体内没有东西
                            elseError = 0;
                            //System.out.println(syn.line+"行while主程序体内没有代码！");
                            entrance.parserResult = entrance.parserResult + syn.line+"行while主程序体内没有代码！\n" ;
                            entrance.parserError = entrance.parserError + syn.line+"行while主程序体内没有代码！\n" ;
                        }else {
                            //TGQYUFA
                            lastSyn();//正常的情况
                            //变量作用域
                            //tempstart 第二次以后就不会新建作用域了 是否等于tempstart 第二次直接进去作用域
                            //第一次
                            boolean firstTime = true;
                            int loca = -1;
                            for(int i = 0;i < region.next.size();++i)
                            {
                                if(region.next.get(i).swhileStart == tempStart)
                                {
                                    firstTime = false;
                                    loca = i;
                                }
                            }
                            if(firstTime == true)//新建
                            {
                                depth++;
                                SemanticAnalysis temp = new SemanticAnalysis();
                                temp.setFront(region);
                                region.next.add(temp);
                                region = temp;
                                region.swhileStart = tempStart;
                                region.sfirstMetBlock = true;
                                region.sifJump = false;
                                region.sifStart = -1;
                                region.selseStart = -1;
                            }else
                            {
                                region = region.next.get(loca);//进去了
                                region.sfirstMetBlock = false;
                                region.sifJump = false;
                            }
//                            depth++;
//                            SemanticAnalysis temp = new SemanticAnalysis();
//                            temp.setFront(region);
//                            region.next.add(temp);
//                            region = temp;
//                            if(region.swhileStart == tempStart)
//                            {
//                                region.sfirstMetBlock = false;
//                                System.out.println("不是第一次进去");
//                            }else
//                            {
//                                region.swhileStart = tempStart;
//                                region.sfirstMetBlock = true;
//                                entrance.wholeExcute+="while第一次只是检查:\n";
//                                System.out.println("是第一次进去");
//                            }
                            //变量作用域
                            bloJum1:{
                                    if((conRe == 0||conRe ==-1))
                                    {
                                        //System.out.println("daoda");
                                        if(region.sfirstMetBlock == false)
                                        {
                                            //System.out.println("!!!");
                                            sign = region.swhileEnd;
                                            syn = wordList.get(sign);
                                            region.sifJump = false;
                                            //entrance.wholeExcute+="1\n";
                                            break bloJum1;
                                        }
                                        else if(region.sfirstMetBlock == true)
                                        {
                                            //System.out.println("!!!!!");
                                            region.sifJump = true;
                                            region.sExcute = false;//不该执行
                                            statements();
                                        }
                                    }else if(conRe == 1)
                                    {
                                        region.sifJump = true;
                                        region.sExcute = true;
                                        //entrance.wholeExcute+="2\n";
                                        int bef = region.IFE();//以前所有的是否执行
                                        if(region.sfirstMetBlock == true)
                                        {
                                            region.sExcute = false;
                                            //System.out.println("首次阻塞");
                                            if(bef != 0)
                                            {
                                                region.sifJump = false;
                                                //System.out.println(region.sExcute+"hererere");
                                            }
                                            //region.sifJump = true;
                                        }
                                        statements();
                                    }
                            }

                            //System.out.println("fuck5");
                            if(syn.kind == 21){  // }
                                region.swhileEnd = sign;//结束
                                //ifJump = false;//默认不跳

                                if(region.sifJump == true)
                                {
                                    //System.out.println("跳");
                                    ifJump = true;
                                    sign = region.swhileStart;
                                    syn = wordList.get(sign);
                                }
                                region.Reco.removeAllElements();
                                depth--;
                                region = region.getFront();
                                //变量作用域
                                result = 1;
                                //System.out.println("while语句处理结束");
                                entrance.parserResult = entrance.parserResult + "while语句处理结束\n";
                                //System.out.println("while结束！");
                                //4???跳转
                                //System.out.println("fuck4");

                            }
                            else{
                                result = 1;
                                elseError = 0;  //TGQ
                                //System.out.println(syn.line + "行while语句缺少“}”");
                                entrance.parserResult = entrance.parserResult + syn.line + "行while语句缺少“}”\n";
                                entrance.parserError = entrance.parserError + syn.line + "行while语句缺少“}”\n";
                            }

                        }

                    }
                    else{
                        result = 1;
                        elseError = 0;  //TGQ
                        //System.out.println(syn.line + "行while语句缺少“{”");
                        entrance.parserResult = entrance.parserResult + syn.line + "行while语句缺少“{”\n";
                        entrance.parserError = entrance.parserError + syn.line + "行while语句缺少“{”\n";
                        //TGQYUFA
                        lastSyn();
                        bloJum1:{
                            if((conRe == 0||conRe ==-1))
                            {
                                if(region.sfirstMetBlock == false)
                                {
                                    sign = region.swhileEnd;
                                    syn = wordList.get(sign);
                                    //entrance.wholeExcute+="1\n";
                                    break bloJum1;
                                }
                                else if(region.sfirstMetBlock == true)
                                {
                                    region.sExcute = false;//不该执行
                                    statements();
                                }
                            }else if(conRe == 1)
                            {
                                region.sifJump = true;
                                region.sExcute = true;
                                //entrance.wholeExcute+="2\n";
                                int bef = region.IFE();
                                if(region.sfirstMetBlock == true)
                                {
                                    region.sExcute = false;
                                    //region.sifJump = true;
                                    if(bef!=0)
                                    {
                                        region.sifJump = false;
                                    }
                                }
                                //region.sExcute = region.IFE();//以前所有的是否执行
                                statements();

                            }
                        }
                        //statements();原来的
                        //firstMetBlock = false;//4
                    }
                }
                else{
                    elseError = 0;  //TGQYU
                    //System.out.println((syn.line-1) + "行while语句缺少“）”");
                    entrance.parserResult = entrance.parserResult + (syn.line-1) + "行while语句缺少“）”\n";
                    entrance.parserError = entrance.parserError + (syn.line-1) + "行while语句缺少“）”\n";
                    //TGQYUFA直接中断
                    //IF忽略
                    jumpForIF();
                }
            }
            else{
                result = 1;
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行while语句缺少“(”");
                entrance.parserResult = entrance.parserResult + syn.line + "行while语句缺少“(”\n";
                entrance.parserError = entrance.parserError + syn.line + "行while语句缺少“(”\n";
                //IF忽略 TGQYUFA
                jumpForIF();//大中断
            }
        }
        return result;
    }


    public int condition() {//-1 无法执行 0错 1是对
        //删除注释conR = true;
        token conTL = new token(0,null,0);
        token conTR = new token(0,null,0);
        int LType;
        int RType;
        int iL;
        int iR;
        float rL;
        float rR;
        //System.out.println("处理条件语句");
        entrance.parserResult = entrance.parserResult + "处理条件语句\n";
        //System.out.println("<条件语句>\t\t-> \t\t<表达式><比较符号><表达式>");
        entrance.parserResult = entrance.parserResult + "<条件语句>\t\t-> \t\t<表达式><比较符号><表达式>\n";
        int result = 1;
        nextSyn();
        //isDefT = false;
        inSentence = false;//2019.11.18 4
        conTL = expression();
        LType = conTL.kind;
        if(syn.kind == 13 || syn.kind == 14 || syn.kind == 15 || syn.kind == 16){
            //         <                 >                 ==                <>
            //System.out.println("<比较符号>\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "<比较符号>\t\t->\t\t" + syn.content + "\n";
            int compST = syn.kind;
            nextSyn();
            conTR = expression();
            inSentence = true;//2019.11.18 4
            RType = conTR.kind;
            if(LType==0||RType==0)
            {
                entrance.parserResult +="第"+syn.line+"条件语句发生了运行时错误，无法给出结果！\n";
                entrance.wholeExcute +="第"+syn.line+"条件语句发生了运行时错误，无法给出结果！\n";
                entrance.writeCon +="第"+syn.line+"条件语句发生了运行时错误，无法给出结果！\n";
                return -1;
            }else
            {
                if(LType == 25)//左int
                {
                    iL = Integer.parseInt(conTL.content);
                    if(RType == 25)//右int
                    {
                        iR = Integer.parseInt(conTR.content);
                        if(compST == 13)//<
                        {
                            if((iL<iR)== true)
                            {
                                return 1;
                            }else if((iL<iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 14)//>
                        {
                            if((iL>iR)== true)
                            {
                                return 1;
                            }else if((iL>iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 15)//==
                        {
                            if((iL==iR)== true)
                            {
                                return 1;
                            }else if((iL==iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 16)//<>
                        {
                            if((iL!=iR)== true)
                            {
                                return 1;
                            }else if((iL!=iR)== false)
                            {
                                return 0;
                            }
                        }
                    }else if(RType == 26)//右real
                    {
                        rR = Float.parseFloat(conTR.content);
                        if(compST == 13)//<
                        {
                            if((iL<rR)== true)
                            {
                                return 1;
                            }else if((iL<rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 14)//>
                        {
                            if((iL>rR)== true)
                            {
                                return 1;
                            }else if((iL>rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 15)//==
                        {
                            if((iL==rR)== true)
                            {
                                return 1;
                            }else if((iL==rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 16)//<>
                        {
                            if((iL!=rR)== true)
                            {
                                return 1;
                            }else if((iL!=rR)== false)
                            {
                                return 0;
                            }
                        }
                    }
                }
                if(LType == 26)//左real
                {
                    rL = Float.parseFloat(conTL.content);
                    if(RType == 25)//右int
                    {
                        iR = Integer.parseInt(conTR.content);
                        if(compST == 13)//<
                        {
                            if((rL<iR)== true)
                            {
                                return 1;
                            }else if((rL<iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 14)//>
                        {
                            if((rL>iR)== true)
                            {
                                return 1;
                            }else if((rL>iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 15)//==
                        {
                            if((rL==iR)== true)
                            {
                                return 1;
                            }else if((rL==iR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 16)//<>
                        {
                            if((rL!=iR)== true)
                            {
                                return 1;
                            }else if((rL!=iR)== false)
                            {
                                return 0;
                            }
                        }
                    }else if(RType == 26)//右real
                    {
                        rR = Float.parseFloat(conTR.content);
                        if(compST == 13)//<
                        {
                            if((rL<rR)== true)
                            {
                                return 1;
                            }else if((rL<rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 14)//>
                        {
                            if((rL>rR)== true)
                            {
                                return 1;
                            }else if((rL>rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 15)//==
                        {
                            if((rL==rR)== true)
                            {
                                return 1;
                            }else if((rL==rR)== false)
                            {
                                return 0;
                            }
                        }else if(compST == 16)//<>
                        {
                            if((rL!=rR)== true)
                            {
                                return 1;
                            }else if((rL!=rR)== false)
                            {
                                return 0;
                            }
                        }
                    }
                }
            }
            result = 1;
            //return ;
        }
        else{
            result = 1;
            elseError = 0;  //TGQ
            //System.out.println(syn.line + "行条件语句缺少比较运算符！");
            entrance.parserResult = entrance.parserResult + syn.line + "行条件语句缺少比较运算符！\n";
            entrance.parserError = entrance.parserError + syn.line + "行条件语句缺少比较运算符！\n";
            inSentence = true;
            //删除注释conR = false;
            return -1;//条件语句错误
        }
        //return result;
        return -1;//??? 4
    }


    public int arrayElement(){
        count = 0;
        int firstMacth = region.getType(temptoken);
        int result = 1;
        if(syn.kind == 25){   // int值
            //System.out.println("<数组元素集>\t->\t\t<int数组元素集>");
            entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t<int数组元素集>\n";
            //执行
            if(arrayR == true)
            {
                if(firstMacth == 25) {
                    region.setvalue(temptoken, Integer.parseInt(syn.content), count);
                }else if(firstMacth == 26)
                {
                    region.setvalue(temptoken, (float) Integer.parseInt(syn.content), count);
                }
            }
            result = iArrayElement();
        }
        else if(syn.kind == 26){  // real值
            //System.out.println("<数组元素集>\t->\t\t<real数组元素集>");
            entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t<real数组元素集>\n";
            //执行
            if(arrayR == true)
            {
                if(firstMacth == 25) {
                    region.setvalue(temptoken, (int)Float.parseFloat(syn.content), count);
                }else if(firstMacth == 26)
                {
                    region.setvalue(temptoken, Float.parseFloat(syn.content), count);
                }
            }

            result = rArrayElement();
        }
        else if(syn.kind == 9){  //  -
            nextSyn();
            if(syn.kind == 25){   // int值
                //System.out.println("<数组元素集>\t->\t\t<int数组元素集>");
                entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t-<int数组元素集>\n";
                //执行
                if(arrayR == true)
                {
                    if(firstMacth == 25) {
                        region.setvalue(temptoken, -Integer.parseInt(syn.content), count);
                    }else if(firstMacth == 26)
                    {
                        region.setvalue(temptoken, -(float) Integer.parseInt(syn.content), count);
                    }
                }
                result = iArrayElement();
            }
            else if(syn.kind == 26){  // real值
                //System.out.println("<数组元素集>\t->\t\t<real数组元素集>");
                entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t-<real数组元素集>\n";
                //执行
                if(arrayR == true)
                {
                    if(firstMacth == 25) {
                        region.setvalue(temptoken, -(int)Float.parseFloat(syn.content), count);
                    }else if(firstMacth == 26)
                    {
                        region.setvalue(temptoken, -Float.parseFloat(syn.content), count);
                    }
                }
                result = rArrayElement();
            }
        }
        return result;
    }
    //数组重定位 TGQYUFA
    public int LocateArray()
    {
        int countL = 0;
        while(syn.kind != 19&&syn.kind != 21)//; }
        {
            nextSyn();
            ++countL;
        }
        //lastSyn();
        return countL;
    }
    //int集合
    public int iArrayElement(){
        //int w = region.getArraySize(temptoken);
        arrayLose = false;//无损失 理想
        //YUYI
        //本来应该的数据类型 大小
        int realType = 0;
        int realSize = 0;
        if(arrayR == true) {
            realType = region.getType(temptoken);
            realSize = region.getArraySize(temptoken);
        }
        if(region.getType(temptoken) == 26) {   //real数组
            //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);
            entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误 \n";
            entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
            //entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
            arrayLose = true;
        }//YUYI
        int result = 1;
        //count = 1;//YUYI
//        ++count;
        if(syn.kind == 25){   //  int值
            //System.out.println("<int数组元素集>\t->\t\tint值<int数组元素拓展>");
            entrance.parserResult = entrance.parserResult + "<int数组元素集>\t->\t\tint值<int数组元素拓展>\n";
            //System.out.println("int值\t\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content + "\n";
            //执行
//            if(arrayR == true)
//            {
//                if(count<=realSize-1)
//                {
//                    if(realType == 25)
//                    {
//                        region.setvalue(temptoken,Integer.parseInt(syn.content),count);//4
//                    }
//                    if(realType == 26)
//                    {
//                        region.setvalue(temptoken,(float)(Integer.parseInt(syn.content)),count);//4
//                    }
//                }
//            }
            //执行
            //count++;//YUYI 为了执行而修改
            nextSyn();//TGQYUFA 正常的时候这里是;
            //YUYI 修改块 BUG
            while(syn.kind == 19){   //  ; 不能是 }
                nextSyn();
                //++count;
                if(syn.kind == 25 || syn.kind == 9){   // int值或 -
                    if(syn.kind == 9)
                    {
                        //count++;
                        nextSyn();
                        if(syn.kind == 25)
                        {
                            //System.out.println("<int数组元素拓展>->\t\t;-int值<int数组元素拓展>");
                            entrance.parserResult = entrance.parserResult + "<int数组元素拓展>->\t\t;-int值<int数组元素拓展>\n";
                            //nexySyn();
                            //System.out.println("int值\t\t\t->\t\t" + syn.content);
                            entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content + "\n";
                            count++;
                            //执行
                            if(arrayR == true)
                            {
                                if(count<=realSize-1)
                                {
                                    if(realType == 25)
                                    {
                                        region.setvalue(temptoken,-Integer.parseInt(syn.content),count);//4
                                    }
                                    if(realType == 26)
                                    {
                                        region.setvalue(temptoken,-(float)(Integer.parseInt(syn.content)),count);//4
                                    }
                                }
                            }
                            //执行
                            //region.setvalue(temptoken,-Integer.parseInt(syn.content),count-1);//4
                            nextSyn();
                        }
                        else
                        {
                            if(syn.kind == 26) {
                                //elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组类型错误  \n", syn.line);//YUYI
                                entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误  \n";
                                entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组类型错误  \n";
                                //执行
                                arrayLose = true;
                                count++;
                                if(arrayR == true)
                                {
                                    if(count<=realSize-1)
                                    {
                                        if(realType == 25)
                                        {
                                            region.setvalue(temptoken,-(int)(Float.parseFloat(syn.content)),count);//4
                                        }
                                        if(realType == 26)
                                        {
                                            region.setvalue(temptoken,-Float.parseFloat(syn.content),count);//4
                                        }
                                    }
                                }
                                //执行
                            }else {
                                elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组表示错误  \n", syn.line);
                                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组表示错误  \n";
                            }
                            nextSyn();
                            //TGQYUFA不要中断
                            LocateArray();//现在是 ; }
                        }
                    }
                    else
                    {
                        //System.out.println("<int数组元素拓展>->\t\t;int值<int数组元素拓展>");
                        entrance.parserResult = entrance.parserResult + "<int数组元素拓展>->\t\t;int值<int数组元素拓展>\n";
                        //System.out.println("int值\t\t\t->\t\t" + syn.content);
                        entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content +"\n";
                        //count++;
                        if(arrayR == true)
                        {
                            count++;
                            if(count<=realSize-1)
                            {
                                if(realType == 25)
                                {
                                    region.setvalue(temptoken,Integer.parseInt(syn.content),count);//4
                                }
                                if(realType == 26)
                                {
                                    region.setvalue(temptoken,(int)(Float.parseFloat(syn.content)),count);//4
                                }
                            }
                        }
                        //region.setvalue(temptoken,Integer.parseInt(syn.content),count-1);//4
                        
                        
                        
                        //测试
                        //System.out.println(region.getIntArrayV(temptoken, count-1));
                        nextSyn();
                    }
                    int reality = LocateArray();//是否因为错误重定位 YUYI 语法中断
                    if(reality != 0)
                    {
                        elseError = 0;
                        //System.out.println("第"+syn.line+"行数组的错误表示");
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！\n";
                    }//YUFATGQ
                }
                //YUYI新加的块
                else if (syn.kind ==26 )
                {
                    yuyiErrorExi = 0;
                    count++;
                    //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);//YUYI
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                    arrayLose = true;
                    if(arrayR == true)
                    {
                        count++;
                        if(count<=realSize-1)
                        {
                            if(realType == 25)
                            {
                                region.setvalue(temptoken,(int)(Float.parseFloat(syn.content)),count);//4
                            }
                            if(realType == 26)
                            {
                                region.setvalue(temptoken,Float.parseFloat(syn.content),count);//4
                            }
                        }
                    }
                    nextSyn();
                    LocateArray();//YUFATGQ
                }
                else{
                    result = 1;
                    elseError = 0;  //TGQ
                    //System.out.println(syn.line + "行“;”后缺少数字");
                    entrance.parserResult = entrance.parserResult + syn.line + "行“;”后缺少数字\n";
                    entrance.parserError = entrance.parserError + syn.line + "行“;”后缺少数字\n";
                    LocateArray();//YUFATGQ 不要中断
                    //return result;
                }
            }
            //YUYI 修改块
            //System.out.println("<int数组元素拓展>->\t\t∂");
            entrance.parserResult = entrance.parserResult + "<int数组元素拓展>->\t\t∂\n";
            if(syn.kind != 21){   //  }
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行出现语法错误");
                //entrance.parserResult = entrance.parserResult + syn.line + "行出现语法错误\n";
                //System.out.println(syn.line + "行数组缺少'}'");
                entrance.parserResult = entrance.parserResult + syn.line + "行数组缺少'}'\n";
                entrance.parserError = entrance.parserError + syn.line + "行数组缺少'}'\n";
                //YUFATGQ
                int tempLineA = syn.line;
                int tLA = tempLineA;//重定位到下一行
                while(tempLineA == tLA)
                {
                    nextSyn();
                    tLA = syn.line;
                }
                lastSyn();//假设最后还是; 待修改
                //YUFATGQ
            }
            else{
                result = 1;
            }
        }
        return result;
    }
    //real集合
    public int rArrayElement(){
        //YUYI
        arrayLose = false;//无损失 理想 4
        //本来应该的数据类型 大小
        int realType = 0;
        int realSize = 0;
        if(arrayR == true) {
            realType = region.getType(temptoken);
            realSize = region.getArraySize(temptoken);
        }
        if(region.getType(temptoken) == 25) {
            //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);
            entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误 \n";
            entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
            arrayLose = true;
        }
        //YUYI
        int result = 1;
        count = 0;//YUYI
        if(syn.kind == 26){   //  real值
            //System.out.println("<real数组元素集>\t->\t\treal值<real数组元素拓展>");
            entrance.parserResult = entrance.parserResult + "<real数组元素集>\t->\t\treal值<real数组元素拓展>\n";
            //System.out.println("real值\t\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content +"\n";
            //执行
            if(arrayR == true)
            {
                if(count<=realSize-1)
                {
                    if(realType == 25)
                    {
                        region.setvalue(temptoken,(int)(Float.parseFloat(syn.content)),count);//4
                    }
                    if(realType == 26)
                    {
                        region.setvalue(temptoken,Float.parseFloat(syn.content),count);//4
                    }
                }
            }
            //执行
            //count++;//YUYI
            nextSyn();
            //YUYI 修改块 BUG
            while(syn.kind == 19){   //  ;
                nextSyn();
                if(syn.kind == 26 || syn.kind == 9){   // real值或 -
                    if(syn.kind == 9)
                    {
                        count++;
                        nextSyn();
                        //System.out.println("<real数组元素拓展>->\t\t;-real值<real数组元素拓展>");
                        //不是小数 语法错误
                        if(syn.kind != 26)
                        {
                            if(syn.kind == 25)
                            {
                                //elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);//新加的语法
                                //entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                                entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                                entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                                //执行
                                arrayLose = true;
                                if(arrayR == true)
                                {
                                    if(count<=realSize-1)
                                    {
                                        if(realType == 25)
                                        {
                                            region.setvalue(temptoken,-Integer.parseInt(syn.content),count);//4
                                        }
                                        if(realType == 26)
                                        {
                                            region.setvalue(temptoken,-(float)(Integer.parseInt(syn.content)),count);//4
                                        }
                                    }
                                }
                                //执行
                            }
                            else {//其他的错误 找到 ; }
                                elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组表示错误 \n", syn.line);//新加的语法
                                entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组表示错误 \n";
                                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组表示错误 \n";
                            }
                            nextSyn();
                            LocateArray();//YUYI
                        }
                        else 
                        {
                            //System.out.println("real值\t\t\t->\t\t" + syn.content);
                            entrance.parserResult = entrance.parserResult + "<real数组元素拓展>->\t\t;-real值<real数组元素拓展>\n";
                            entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content + "\n";
                            //执行
                            if(arrayR == true)
                            {
                                if(count<=realSize-1)
                                {
                                    if(realType == 25)
                                    {
                                        region.setvalue(temptoken,-(int)(Float.parseFloat(syn.content)),count);//4
                                    }
                                    if(realType == 26)
                                    {
                                        region.setvalue(temptoken,-Float.parseFloat(syn.content),count);//4
                                    }
                                }
                            }
                            //执行
                            //region.setvalue(temptoken,Float.parseFloat(syn.content),count-1);
                            nextSyn();
                        }
                    }
                    else
                    {
                        //System.out.println("<real数组元素拓展>->\t\t;real值<real数组元素拓展>");
                        entrance.parserResult = entrance.parserResult + "<real数组元素拓展>->\t\t;real值<real数组元素拓展>\n";
                        //System.out.println("real值\t\t\t->\t\t" + syn.content);
                        entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content +"\n";
                        count++;
                        //执行
                        if(arrayR == true)
                        {
                            if(count<=realSize-1)
                            {
                                if(realType == 25)
                                {
                                    region.setvalue(temptoken,(int)(Float.parseFloat(syn.content)),count);//4
                                }
                                if(realType == 26)
                                {
                                    region.setvalue(temptoken,Float.parseFloat(syn.content),count);//4
                                }
                            }
                        }
                        //执行
                        //region.setvalue(temptoken,Float.parseFloat(syn.content),count-1);

                        //测试
                        //System.out.println(region.getFloatArrayV(temptoken, count-1));
                        
                        nextSyn();
                    }
                    int reality = LocateArray();//是否因为错误重定位 YUYI
                    if(reality != 0)
                    {
                        elseError = 0;
                        //System.out.println("第"+syn.line+"行数组的错误表示");
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！\n";
                    }//YUFATGQ
                }
                //新的块
                else if(syn.kind == 25)
                {
                    //elseError = 0;
                    count++;
                    //System.out.printf("错误出现在第%d行，数组类型错误  \n", syn.line);

                    entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误  \n";
                    entrance.yuyiError = entrance.yuyiError + "错误出现在第"+syn.line+"行，数组类型错误  \n";
                    //执行
                    arrayLose = true;
                    if(arrayR == true)
                    {
                        if(count<=realSize-1)
                        {
                            if(realType == 25)
                            {
                                region.setvalue(temptoken,Integer.parseInt(syn.content),count);//4
                            }
                            if(realType == 26)
                            {
                                region.setvalue(temptoken,(float)(Integer.parseInt(syn.content)),count);//4
                            }
                        }
                    }
                    //执行
                    nextSyn();
                    LocateArray();//YUFATGQ
                }
                else{
                    result = 1;
                    elseError = 0;  //TGQ
                    //System.out.println(syn.line + "行“;”后缺少数字");
                    entrance.parserResult = entrance.parserResult + syn.line + "行“;”后缺少数字\n";
                    entrance.parserError = entrance.parserError + syn.line + "行“;”后缺少数字\n";
                    LocateArray();//YUFATGQ 不要中断
                    //return result;
                }
            }
            //YUYI 修改块 向上
            //System.out.println("<real数组元素拓展>->\t\t∂");
            entrance.parserResult = entrance.parserResult + "<real数组元素拓展>->\t\t∂\n";
            if(syn.kind != 21){
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行数组缺少'}");
                entrance.parserResult = entrance.parserResult + syn.line + "行数组缺少'}'\n";
                entrance.parserError = entrance.parserError + syn.line + "行数组缺少'}'\n";
                //YUFATGQ
                int tempLineA = syn.line;
                int tLA = tempLineA;//重定位到下一行
                while(tempLineA == tLA)
                {
                    nextSyn();
                    tLA = syn.line;
                }
                lastSyn();//假设最后还是; 待修改
                //YUFATGQ
            }
            else{
                result = 1;
            }
        }
        return result;
    }
}
