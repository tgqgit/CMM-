package test.action;
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
    public int decSExtendIF = 0; //1则发生中断
    public boolean isDefT;//变量是否被声明
    //CHEN
    public int count;
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
        {
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
                    result = ifStatement();
                    nextSyn();
                }
                else if(syn.kind == 3){  //while语句
                    //System.out.println("<语句>\t\t\t->\t\t<while语句>");
                    entrance.parserResult = entrance.parserResult + "<语句>\t\t\t->\t\t<while语句>\n";
                    //System.out.println("<while语句>\t->\t\twhile(<条件语句>){语句块}");
                    entrance.parserResult = entrance.parserResult + "<while语句>\t->\t\twhile(<条件语句>){语句块}\n";
                    result = whileStatement();
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

    public String expression() {
        String tp,eplace2,eplace,temp = null;
        eplace = expressionMD();
        if(eplace == null){
            return null;
        }
        while(syn.kind == 8 || syn.kind == 9){   //'+' '-'
            //System.out.println("<表达式拓展>\t->\t\t<运算符号><表达式>");
            entrance.parserResult = entrance.parserResult + "<表达式拓展>\t->\t\t<运算符号><表达式>\n";
            //System.out.println("<运算符号>\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "<运算符号>\t\t->\t\t" + syn.content +"\n";
            if(sign < wordList.size())
                temp = syn.content; //获取当前运算符
            nextSyn();//读取下一个单词符号;
            if((eplace2 = expressionMD()) == null){
                return null;
            }
            //tp = newtemp();
            //emit(temp,eplace,eplace2,tp);
            eplace = "T";
        }
        //System.out.println("<表达式拓展>\t->\t\t∂");
        entrance.parserResult = entrance.parserResult + "<表达式拓展>\t->\t\t∂\n";
        return eplace;
    }
    //优先处理*，/
    public String expressionMD() {
        String tp,eplace3,eplace,tt = null;
        eplace = expressionB();
        if(eplace == null){
            return null;
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
            if (syn.kind == 11)
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
                            entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                            entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，除数为0. \n";
                        }
                    }
                    else if(syn.kind == 25)
                    {
                        if (region.IfDivZero(Integer.parseInt(syn.content))) {
                            //System.out.printf("错误出现在第%d行，除数为0. \n", syn.line);
                            entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                            entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，除数为0. \n";
                        }
                    }

                    lastSyn();
                }else {
                    if (region.IfDivZero(Integer.parseInt(syn.content))) {
                        //System.out.printf("错误出现在第%d行，除数为0. \n", syn.line);
                        entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，除数为0. \n";
                        entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，除数为0. \n";
                    }
                }
                lastSyn();
            }
            //YUYI
            nextSyn();
            if((eplace3 = expressionB()) == null){
                return null;
            }
            //tp = newtemp();  //生成新的变量名
            //emit(tt,eplace,eplace3,tp);
            //eplace = tp;
        }
        //System.out.println("<表达式拓展>\t->\t\t∂");
        return eplace;
    }
    //优先处理（）
    public String expressionB(){
        String fplace;
        fplace = " ";
        if(syn.kind == 24){     //变量名
            //YUYI
            //判断变量是否已经定义
            //变量作用域
            if(!region.IsExist(syn)){
                System.out.printf("错误出现在第%d行，变量%s未定义\n", syn.line,syn.content);
                entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t <表达式因子><表达式拓展>\n";
            }
            else{
                if(isDefT == true)
                {
                if(region.getType(syn)!=region.getType(temptoken)) {
                    System.out.printf("错误出现在第%d行，变量%s类型错误\n", syn.line, syn.content);
                    entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误\n";
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，变量"+syn.content+"类型错误\n";
                }
                if(!region.getIfValued(syn)) {
                    System.out.printf("错误出现在第%d行，使用了未赋值变量%s \n", syn.line, syn.content);
                    entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，使用了未赋值变量"+syn.content+"\n";
                }
                }
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
            if(wordList.get(sign + 1).kind == 22){   //如果下一个是 [
                nextSyn();//现在是[
                //System.out.println("<声明类型拓展>  ->\t\t[int值]");
                //YUYI二期
                //开始往前看
                if(wordList.get(sign+1).kind != 25)
                {
                    lastSyn();
                    entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标必须是非负整数 \n";
                    entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标必须是非负整数 \n";
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
                            entrance.parserError = entrance.parserError +"错误出现在第"+syn.line+"行，"+syn.content+"不是数组 \n";
                        }
                        else {
                            if (region.getArraySize(syn) < tempLocation + 1) {
                                //System.out.printf("错误出现在第%d行，数组%s下标越界 \n", wordList.get(sign + 1).line, temptoken.content);
                                //entrance.parserResult = entrance.parserResult + "错误出现在第"+wordList.get(sign + 1).line+"行，数组"+temptoken.content+"下标越界 \n";
                                //entrance.parserError = entrance.parserError +"错误出现在第"+wordList.get(sign + 1).line+"行，数组"+temptoken.content+"下标越界 \n";
                                entrance.parserResult = entrance.parserResult + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标越界 \n";
                                entrance.parserError = entrance.parserError + "错误出现在第" + syn.line + "行，数组" + syn.content + "下标越界 \n";
                            }
                        }
                    }
                }
                nextSyn();
                //YUYI二期
                entrance.parserResult = entrance.parserResult + "<声明类型拓展>  ->\t\t[int值]\n";
                decStatementExtend();
            }
            else{
            //if(sign < wordList.size()) {
                //System.out.println("<声明类型拓展>  ->\t\t∂");
                boolean isNotArr = region.getIsArray(syn);
                if(isNotArr != false)//不是变量
                {
                    entrance.parserResult = entrance.parserResult +"第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                    entrance.parserError = entrance.parserError+ "第"+syn.line+"行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                }
                else
                {
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>  ->\t\t∂\n";
                }
                fplace = syn.content;
            }
            nextSyn();
        }
        else if(syn.kind == 25 || syn.kind == 26 || syn.kind == 9)
        {   //数字（含负数）
            //System.out.println("<表达式>        ->\t\t <表达式因子><表达式拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t <表达式因子><表达式拓展>\n";
            if(syn.kind == 9){  //负数
                nextSyn();
                if(syn.kind == 25 || syn.kind == 26){
                    //在这里处理负数的情况
                    //System.out.println("<表达式因子>\t->\t\t<负数>");
                    entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t<负数>\n";
                    //System.out.println("<负数>        ->     -" + syn.content);
                    entrance.parserResult = entrance.parserResult + "<负数>        ->     -" + syn.content +"\n";
                    //YUYI
                    if(isDefT == true) {
                        if (!region.Num1(region.getType(temptoken), syn)) {
                            //System.out.printf("错误出现在第%d行，类型不匹配 \n", temptoken.line);
                            entrance.parserError = entrance.parserError + "错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                        }
                    }
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
                //在这里处理负数的情况
                //System.out.println("<表达式因子>\t->\t\t<正数>");
                entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t<正数>\n";
                //System.out.println("<正数>        ->     " + syn.content);
                entrance.parserResult = entrance.parserResult + "<正数>        ->     " + syn.content +"\n";
                //YUYI TGQYUYI
                if(isDefT == true) {
                    if (!region.Num1(region.getType(temptoken), syn)) {
                    /*原来的
                    //TGQ YUYI 一期改BUG ???
                    if(temptoken.line!=0){
                    //System.out.printf("错误出现在第%d行，类型不匹配 \n", temptoken.line);
                    entrance.parserError = entrance.parserError + "错误出现在第"+temptoken.line+"行，类型不匹配 \n";
                    }
                    */
                        entrance.parserError = entrance.parserError + "错误出现在第" + temptoken.line + "行，类型不匹配 \n";
                    }
                }
                nextSyn();
            }
            else{
                if(sign < wordList.size()){
                    //System.out.println("<表达式因子>\t->\t\t" + syn.content);
                    entrance.parserResult = entrance.parserResult + "<表达式因子>\t->\t\t" + syn.content + "\n";
                    fplace = syn.content;
                }
                nextSyn();//读取下一个单词符号；
            }
        }
        else if(syn.kind == 17){     //'('
            //System.out.println("<表达式>        ->\t\t（<表达式因子><表达式拓展>）<表达式拓展>");
            entrance.parserResult = entrance.parserResult + "<表达式>        ->\t\t（<表达式因子><表达式拓展>）<表达式拓展>\n";
            nextSyn();//读取下一个单词符号；
            if((fplace = expression()) == null){
                return null;
            }
            if(syn.kind == 18){      //')'
                nextSyn();//读取下一个单词符号；
            }
            else {
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行缺少')'");
                entrance.parserResult = entrance.parserResult + syn.line + "行缺少')'\n";
                entrance.parserError = entrance.parserError + syn.line + "行缺少')'\n";
                return null;
            }
        }
        else {
            elseError = 0;  //TGQ
            //System.out.println(syn.line + "行表达式出现语法错误");
            entrance.parserResult = entrance.parserResult + syn.line + "行表达式出现语法错误\n";
            entrance.parserError = entrance.parserError + syn.line + "行表达式出现语法错误\n";
            return null;
        }
        return fplace;
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
                //System.out.println("变量名ID\t\t->\t\t" + syn.content);
                entrance.parserResult = entrance.parserResult + "变量名ID\t\t->\t\t" + syn.content +"\n";
                //YUYI
                //检查变量是否重复申明，是-》报错    否-》加入RECORD
                if(region.IfRep(syn))
                {
                    //System.out.printf("错误产生在第%d行，变量%s重复定义。\n", syn.line, syn.content);
                    entrance.parserError = entrance.parserError + "错误产生在第"+syn.line+"行，变量"+syn.content+"重复定义。\n";
                }else
                {
                    region.addToReco(syn,kindFlag);
                    isDefT = true;
                }
                temptoken = new token(syn);
                //YUYI
                nextSyn();
                if(syn.kind == 22){  //  [
                    //System.out.println("<声明类型拓展>\t->\t\t[int值]");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>\t->\t\t[int值]\n";
                    if(wordList.get(sign+1).kind == 9||wordList.get(sign+1).content.equals("0"))
                    {
                        lastSyn();
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
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
                    /*
                    if(decSExtendIF == 1)//不中断
                    {
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
                    else if(decSExtendIF == 0){
                        int tLD1 = syn.line;//跳过当前的行
                        int tLD2 = syn.line;
                        while(tLD2 == tLD1)
                        {
                            nextSyn();
                            tLD2 = syn.line;
                            entrance.parserError = entrance.parserError + tLD1 +tLD2+"\n";
                        }
                        lastSyn();
                    }*/
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
                    region.setArraySize(syn, tempInt);//数组标识位已经是数组了
                }
                nextSyn();
                nextSyn();
                //YUYI二期
                //System.out.println("int值\t\t\t->\t\t" + syn.content);
                int temp =  Integer.parseInt(syn.content);//YUYI
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
                    //System.out.println("数组声明错误！出现real值");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！下标不能是real值\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！下标不能是real值\n";
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
                        lastSyn();
                    }
                }else if(syn.kind == 23||syn.kind == 12) //] =
                {
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！-缺少“int值”\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！-缺少“int值”\n";
                    if(syn.kind == 12)  //=
                    {
                        elseError = 0;
                        //缺少 ]
                        //System.out.println("数组声明错误！-缺少“]”");
                        entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误！-缺少“]”\n";
                        entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误！-缺少“]”\n";
                        //lastSyn();
                    }
                }else if(syn.kind == 9){//???
                    decSExtendIF = 1;//非法中断
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    if(wordList.get(sign-4).kind != 4){
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误!\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误!\n";}
                }else {
                    decSExtendIF = 1;//非法中断
                    elseError = 0;
                    //System.out.println("数组声明错误！-缺少“int值”");
                    entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组表示错误!\n";
                    entrance.parserError = entrance.parserError + "第"+syn.line+"行数组表示错误!\n";
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
        return result;
    }

    public int decStatementContent(){
        int result = 1;
        //现在是=
        //TGQYUYI
        lastSyn();//是否是]
        int typeCom = syn.kind;//是否是23 ]
        int lineCom = syn.line;
        nextSyn();
        //TGQYUYI
        nextSyn();
        if(syn.kind == 20){  //  {
            //System.out.println("<声明内容拓展>  ->\t\t{<数组元素集>}");
            if(typeCom != 23)
            {
                entrance.parserResult = entrance.parserResult +"第"+lineCom +"行数据集合不能赋值给非数组变量!\n";
                entrance.parserError = entrance.parserError +"第"+lineCom+"行数据集合不能赋值给非数组变量!\n";
            }
            entrance.parserResult = entrance.parserResult + "<声明内容拓展>  ->\t\t{<数组元素集>}\n";
            //处理数组元素集
            nextSyn();
            result = arrayElement();
            if(syn.kind == 21){  //  }
                result = 1;
                //YUYI
                if(region.IfArrayOver(temptoken, count))
                {
                    //System.out.printf("误出现在第错%d行，数组变量%s越界。 \n", temptoken.line,temptoken.content);
                    entrance.parserError = entrance.parserError + "错误出现在第"+temptoken.line+"行，数组变量"+temptoken.content+"越界。 \n";
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
                entrance.parserResult = entrance.parserResult +"第"+lineCom +"行表达式不能赋值给数组!\n";
                entrance.parserError = entrance.parserError +"第"+lineCom+"行表达式不能赋值给数组!\n";
            }
            entrance.parserResult = entrance.parserResult + "<声明内容拓展>  ->\t\t<表达式>\n";
            expression();
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
                //System.out.printf("错误出现在第%d行，使用了未定义变量%s. \n", syn.line, syn.content);
                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，使用了未定义变量"+syn.content+". \n";
                isDefT = false;
            }
            else {
                temptoken = new token(syn);
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
        if(syn.kind == 12){  //  =
            region.setIfValued(true,temptoken);//YUYI
            //TGQYUYI
            lastSyn();
            boolean isAssArr = region.getIsArray(syn);
            if(isAssArr == true)
            {
                //应该是数组
                entrance.parserResult = entrance.parserResult +"第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
                entrance.parserError = entrance.parserError+ "第"+syn.line+ "行引用数组变量"+syn.content+"的时候缺少下标'[非负数]'\n";
            }
            nextSyn();
            //System.out.println("<赋值语句拓展>->\t\t=<表达式>");
            entrance.parserResult = entrance.parserResult + "<赋值语句拓展>->\t\t=<表达式>\n";
            nextSyn();
            expression();
            result = 1;
        }
        else if(syn.kind == 22){   //  [
            //System.out.println("<赋值语句拓展>->\t\t[int值]=<表达式>");
            entrance.parserResult = entrance.parserResult + "<赋值语句拓展>->\t\t[int值]=<表达式>\n";
            nextSyn();
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
                    entrance.parserResult = entrance.parserResult +"错误出现在第"+syn.line+"行，"+syn.content+"不是数组\n";
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，"+syn.content+"不是数组\n";
                }
                else if(region.getArraySize(syn)<tempLocation2 + 1)
                {
                    //System.out.printf("错误出现在第%d行，数组%s下标越界 \n", syn.line, temptoken.content);
//                    entrance.parserResult = entrance.parserResult +"错误出现在第"+syn.line+"行，数组"+temptoken.content+"下标越界 \n";
//                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组"+temptoken.content+"下标越界 \n";
                    entrance.parserResult = entrance.parserResult +"错误出现在第"+syn.line+"行，数组"+syn.content+"下标越界 \n";
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组"+syn.content+"下标越界 \n";
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
                        if(expression() != null){
                            result = 1;
                        }
                    }
                    else{
                        elseError = 0;  //TGQ
                        //System.out.println(syn.line + "行缺少“=”");
                        entrance.parserResult = entrance.parserResult +syn.line + "行缺少“=”\n";
                        entrance.parserError = entrance.parserError + syn.line + "行缺少“=”\n";
                    }
                }
                else{
                    elseError = 0;  //TGQ
                    //System.out.println(syn.line + "行缺少“]”");
                    entrance.parserResult = entrance.parserResult + syn.line +"行缺少“]”\n";
                    entrance.parserError = entrance.parserError + syn.line +"行缺少“]”\n";
                }
            }
            else{
                result = 1;
                elseError = 0;  //TGQ
                //System.out.println(syn.line + "行缺少int值");
                entrance.parserResult = entrance.parserResult + syn.line +"行缺少int值\n";
                entrance.parserError = entrance.parserError + syn.line +"行缺少int值\n";
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
        if(syn.kind == 17){    //  (
            nextSyn();
            expression();
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
        return result;
    }

    /**
     * 输入语句
     */
    public int readStatement(){
        //TGQ加的
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
                    entrance.parserResult = entrance.parserResult +"第"+readLine+ "行变量"+syn.content+"未定义！\n";
                    entrance.parserError = entrance.parserError + "第"+readLine+"行变量"+syn.content+"未定义！\n";
                }else {
                    temptoken = new token(syn);
                }
                nextSyn();
                if(syn.kind == 22){  //   [
                    //System.out.println("<声明类型拓展>->\t\t[int值]");
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>->\t\t[int值]\n";
                    if(isReDefine == true)
                    {
                        if(readIsArr == false)
                        {
                            entrance.parserResult = entrance.parserResult + "错误出现在第"+readLine+"行，"+readCon+"不是数组 \n";
                            entrance.parserError = entrance.parserError + "错误出现在第"+readLine+"行，"+readCon+"不是数组 \n";
                        }else {
                            //是否越界以及其他的错误 待修改
                            //read问题
//                            lastSyn();
//                            temptoken = new token(syn);
//                            nextSyn();
                            //上面都有问题
                            if(wordList.get(sign+1).kind==9||wordList.get(sign+1).content.equals("0"))
                            {
                                lastSyn();
                                fuhao = 1;
                                entrance.parserResult = entrance.parserResult + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
                                entrance.parserError = entrance.parserError + "第"+syn.line+"行数组"+syn.content+"下标必须是正整数\n";
                                nextSyn();
                            }else if(wordList.get(sign+1).kind == 25)
                            {
                                int tempL = Integer.parseInt(wordList.get(sign+1).content);
                                if(region.getArraySize(temptoken)< tempL + 1)
                                {
                                    entrance.parserResult = entrance.parserResult + "错误出现在第"+readLine+"行，数组"+temptoken.content+"下标越界 \n";
                                    entrance.parserError = entrance.parserError + "错误出现在第"+readLine+"行，数组"+temptoken.content+"下标越界 \n";
                                }
                            }
                        }
                    }
                    //现在是[
                    decStatementExtend();//末尾在]
                    if(fuhao == 1)
                    {
                        lastSyn();
                    }
                    nextSyn();
                }
                else{
                    //System.out.println("<声明类型拓展>->\t\t∂");
                    if(isReDefine == true)
                    {
                        if(readIsArr == true)
                        {
                            entrance.parserResult = entrance.parserResult + "第"+readLine+"行引用数组变量"+readCon+"的时候缺少下标'[非负数]'\n";
                            entrance.parserError = entrance.parserError+ "第"+readLine+"行引用数组变量"+readCon+"的时候缺少下标'[非负数]'\n";
                        }
                    }
                    entrance.parserResult = entrance.parserResult + "<声明类型拓展>->\t\t∂\n";
                }

            }
            if(syn.kind == 18){   //    )
                nextSyn();
                if(syn.kind == 19){   //   ;
                    result = 1;
                    return result;
                }
                else{
                    elseError = 0;  //TGQYU
                    //System.out.println((syn.line -1) + "行缺少“;”");
                    entrance.parserResult = entrance.parserResult + (syn.line -1) +"行缺少“;”\n";   //TGQYUFA
                    entrance.parserError = entrance.parserError + (syn.line -1) +"行缺少“;”\n";
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
            //TGQYUFA
        }
        return result;
    }

    /**
     * if语句
     */
    public int ifStatement(){
        int result = 0;
        if(syn.kind == 1){  // if
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
                }
                else {
                    lastSyn();
                    result = condition();
                    if(result == 0){
                        return result;
                    }
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
                            //变量作用域
                            depth++;
                            SemanticAnalysis temp = new SemanticAnalysis();
                            temp.setFront(region);
                            region.next.add(temp);
                            region = temp;
                            //变量作用域
                            result = statements();
                            if(syn.kind == 21){  // }
                                //变量作用域
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
            //System.out.println("<if扩展>\t\t->\t\telse{语句块}");
            entrance.parserResult = entrance.parserResult + "<if扩展>\t\t->\t\telse{语句块}\n";
            nextSyn();
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
                    //变量作用域
                    depth++;
                    SemanticAnalysis temp = new SemanticAnalysis();
                    temp.setFront(region);
                    region.next.add(temp);
                    region = temp;
                    //变量作用域
                    result = statements();
                    if(syn.kind == 21){  // }
                        result = 1;
                        //System.out.println("else语句处理结束");
                        entrance.parserResult = entrance.parserResult + "else语句处理结束\n";
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
            //System.out.println("<if扩展>\t\t->\t\t∂");
            entrance.parserResult = entrance.parserResult + "<if扩展>\t\t->\t\t∂\n";
            //TGQYUFA 无else时回溯
            lastSyn();
        }
        return result;
    }

    /**
     * while语句
     */
    public int whileStatement(){
        int result = 1;
        if(syn.kind == 3){  // while
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
                    result = condition();
                    if(result == 0){
                        return result;  //return可以吗
                    }
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
                            depth++;
                            SemanticAnalysis temp = new SemanticAnalysis();
                            temp.setFront(region);
                            region.next.add(temp);
                            region = temp;
                            //变量作用域
                            result = statements();
                            if(syn.kind == 21){  // }
                                //变量作用域
                                depth--;
                                region = region.getFront();
                                //变量作用域
                                result = 1;
                                //System.out.println("while语句处理结束");
                                entrance.parserResult = entrance.parserResult + "while语句处理结束\n";
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
                        statements();
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

    public int condition() {
        //System.out.println("处理条件语句");
        entrance.parserResult = entrance.parserResult + "处理条件语句\n";
        //System.out.println("<条件语句>\t\t-> \t\t<表达式><比较符号><表达式>");
        entrance.parserResult = entrance.parserResult + "<条件语句>\t\t-> \t\t<表达式><比较符号><表达式>\n";
        int result = 1;
        nextSyn();
        isDefT = false;
        expression();
        if(syn.kind == 13 || syn.kind == 14 || syn.kind == 15 || syn.kind == 16){
            //         <                 >                 ==                <>
            //System.out.println("<比较符号>\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "<比较符号>\t\t->\t\t" + syn.content + "\n";
            nextSyn();
            expression();
            result = 1;
        }
        else{
            result = 1;
            elseError = 0;  //TGQ
            //System.out.println(syn.line + "行条件语句缺少比较运算符！");
            entrance.parserResult = entrance.parserResult + syn.line + "行条件语句缺少比较运算符！\n";
            entrance.parserError = entrance.parserError + syn.line + "行条件语句缺少比较运算符！\n";
        }
        return result;
    }


    public int arrayElement(){
        int result = 1;
        if(syn.kind == 25){   // int值
            //System.out.println("<数组元素集>\t->\t\t<int数组元素集>");
            entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t<int数组元素集>\n";
            result = iArrayElement();
        }
        else if(syn.kind == 26){  // real值
            //System.out.println("<数组元素集>\t->\t\t<real数组元素集>");
            entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t<real数组元素集>\n";
            result = rArrayElement();
        }
        else if(syn.kind == 9){  //  -
            nextSyn();
            if(syn.kind == 25){   // int值
                //System.out.println("<数组元素集>\t->\t\t<int数组元素集>");
                entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t-<int数组元素集>\n";
                result = iArrayElement();
            }
            else if(syn.kind == 26){  // real值
                //System.out.println("<数组元素集>\t->\t\t<real数组元素集>");
                entrance.parserResult = entrance.parserResult + "<数组元素集>\t->\t\t-<real数组元素集>\n";
                result = rArrayElement();
            }
        }
        return result;
    }
    //数组重定位 TGQYUFA
    public int LocateArray()
    {
        int count = 0;
        while(syn.kind != 19&&syn.kind != 21)//; }
        {
            nextSyn();
            ++count;
        }
        //lastSyn();
        return count;
    }
    //int集合
    public int iArrayElement(){
        //YUYI
        if(region.getType(temptoken) == 26) {
            //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);
            entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
        }//YUYI
        int result = 1;
        count = 0;//YUYI
        if(syn.kind == 25){   //  int值
            //System.out.println("<int数组元素集>\t->\t\tint值<int数组元素拓展>");
            entrance.parserResult = entrance.parserResult + "<int数组元素集>\t->\t\tint值<int数组元素拓展>\n";
            //System.out.println("int值\t\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content + "\n";
            count++;//YUYI
            nextSyn();//TGQYUFA 正常的时候这里是;
            //YUYI 修改块 BUG
            while(syn.kind == 19){   //  ; 不能是 }
                nextSyn();
                if(syn.kind == 25 || syn.kind == 9){   // int值或 -
                    if(syn.kind == 9){
                        count++;
                        nextSyn();
                        if(syn.kind == 25)
                        {
                            //System.out.println("<int数组元素拓展>->\t\t;-int值<int数组元素拓展>");
                            entrance.parserResult = entrance.parserResult + "<int数组元素拓展>->\t\t;-int值<int数组元素拓展>\n";
                            //nexySyn();
                            //System.out.println("int值\t\t\t->\t\t" + syn.content);
                            entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content + "\n";
                            nextSyn();
                        }
                        else
                        {
                            if(syn.kind == 26) {
                                elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组类型错误  \n", syn.line);//YUYI
                                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误  \n";
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
                    else{
                        //System.out.println("<int数组元素拓展>->\t\t;int值<int数组元素拓展>");
                        entrance.parserResult = entrance.parserResult + "<int数组元素拓展>->\t\t;int值<int数组元素拓展>\n";
                        //System.out.println("int值\t\t\t->\t\t" + syn.content);
                        entrance.parserResult = entrance.parserResult + "int值\t\t\t->\t\t" + syn.content +"\n";
                        count++;
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
                    elseError = 0;
                    //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);//YUYI
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
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
        if(region.getType(temptoken) == 25) {
            //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);
            entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
        }
        //YUYI
        int result = 1;
        count = 0;//YUYI
        if(syn.kind == 26){   //  real值
            //System.out.println("<real数组元素集>\t->\t\treal值<real数组元素拓展>");
            entrance.parserResult = entrance.parserResult + "<real数组元素集>\t->\t\treal值<real数组元素拓展>\n";
            //System.out.println("real值\t\t\t->\t\t" + syn.content);
            entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content +"\n";
            count++;//YUYI
            nextSyn();
            //YUYI 修改块 BUG
            while(syn.kind == 19){   //  ;
                nextSyn();
                if(syn.kind == 26 || syn.kind == 9){   // real值或 -
                    if(syn.kind == 9){
                        count++;
                        nextSyn();
                        //System.out.println("<real数组元素拓展>->\t\t;-real值<real数组元素拓展>");
                        //不是小数 语法错误
                        if(syn.kind != 26)
                        {
                            if(syn.kind == 25)
                            {
                                elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组类型错误 \n", syn.line);//新加的语法
                                //entrance.parserResult = entrance.parserResult + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误 \n";
                            }
                            else {//其他的错误 找到 ; }
                                elseError = 0;
                                //System.out.printf("错误出现在第%d行，数组表示错误 \n", syn.line);//新加的语法
                                entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组表示错误 \n";
                            }
                            nextSyn();
                            LocateArray();//YUYI
                        }else {
                            //System.out.println("real值\t\t\t->\t\t" + syn.content);
                            entrance.parserResult = entrance.parserResult + "<real数组元素拓展>->\t\t;-real值<real数组元素拓展>\n";
                            entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content + "\n";
                            nextSyn();
                        }
                    }
                    else{
                        //System.out.println("<real数组元素拓展>->\t\t;real值<real数组元素拓展>");
                        entrance.parserResult = entrance.parserResult + "<real数组元素拓展>->\t\t;real值<real数组元素拓展>\n";
                        //System.out.println("real值\t\t\t->\t\t" + syn.content);
                        entrance.parserResult = entrance.parserResult + "real值\t\t\t->\t\t" + syn.content +"\n";
                        count++;
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
                    elseError = 0;
                    //System.out.printf("错误出现在第%d行，数组类型错误  \n", syn.line);
                    entrance.parserError = entrance.parserError + "错误出现在第"+syn.line+"行，数组类型错误  \n";
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
