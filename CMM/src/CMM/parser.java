package CMM;
import java.util.ArrayList;
public class parser {
    public ArrayList<token> wordList = entrance.tokenStream;
    public token syn;  //当前的token
    public int sign = 0;  //记录当前token在wordList中的位置

    //读取下一个token
    public void nextSyn() {
        if (sign + 1 < wordList.size()) {
            sign++;
            syn = wordList.get(sign);
        }
    }

    /**
     * 程序
     * 返回值0表示分析出错，1表示分析成功
     */
    public int program() {
        int result = 0;
        if (sign <= wordList.size()) {
            syn = wordList.get(sign);
            //System.out.println(syn.kind);
        }
        if (syn.kind == 20) {  //“{”，处理语句块
            System.out.println("处理语句块");
            result = statements();
        } else {  //报错，缺少“{”
            result = 0;
        }
        return result;
    }

    /**
     * 语句块
     */
    public int statements() {
        int result = 0;
        nextSyn();
        System.out.println(syn.content);
        while (syn.kind == 6 || syn.kind == 7 || syn.kind == 24 || syn.kind == 5 || syn.kind == 4 || syn.kind == 1 || syn.kind == 3) {
            if (syn.kind == 6 || syn.kind == 7) {  //int或real，声明语句
                System.out.println("声明语句处理");
                decStatement();
                nextSyn();
            } else if (syn.kind == 24) {  //变量名ID，赋值语句
                System.out.println("赋值语句");
                assStatement();
                nextSyn();
            } else if (syn.kind == 5) {  //write，输出语句
                System.out.println("输出语句");
                writeStatement();
                nextSyn();
            } else if (syn.kind == 4) {  //read，输入语句
                System.out.println("输入语句");
                readStatement();
                nextSyn();
            } else if (syn.kind == 1) {  //if语句
                System.out.println("处理if语句");
                result = ifStatement();
                nextSyn();
            } else if (syn.kind == 3) {  //while语句
                System.out.println("while语句");
                result = whileStatement();
                nextSyn();
            }
        }
        return result;
    }

    /**
     * 表达式
     */

    public String expression() {
        String tp, eplace2, eplace, temp = null;
        eplace = expressionMD();
        if (eplace == null) {
            return null;
        }
        while (syn.kind == 8 || syn.kind == 9) {   //'+' '-'
            if (sign < wordList.size())
                temp = syn.content; //获取当前运算符
            nextSyn();//读取下一个单词符号;
            if ((eplace2 = expressionMD()) == null) {
                return null;
            }
            //tp = newtemp();
            //emit(temp,eplace,eplace2,tp);
            eplace = "T";
        }
        return eplace;
    }

    //优先处理*，/
    public String expressionMD() {
        String tp, eplace3, eplace, tt = null;
        eplace = expressionB();
        if (eplace == null) {
            return null;
        }
        while (syn.kind == 10 || syn.kind == 11) {    //'*' '/'
            if (sign < wordList.size())
                tt = syn.content;
            nextSyn();//读取下一个单词符号;
            if ((eplace3 = expressionB()) == null) {
                return null;
            }
            //tp = newtemp();  //生成新的变量名
            //emit(tt,eplace,eplace3,tp);
            //eplace = tp;
        }
        return eplace;
    }

    //优先处理（）
    public String expressionB() {
        String fplace;
        fplace = " ";
        if (syn.kind == 24) {     //变量名
            if (wordList.get(sign + 1).kind == 22) {   //如果下一个是 [
                System.out.println("处理声明类型拓展");
                nextSyn();
                decStatementExtend();
            }
            if (sign < wordList.size()) {
                fplace = syn.content;
            }
            nextSyn();
        } else if (syn.kind == 25 || syn.kind == 26 || syn.kind == 9) {   //数字（含负数）
            if (syn.kind == 9) {  //负数
                nextSyn();
                if (syn.kind == 25 || syn.kind == 26) {
                    //在这里处理负数的情况
                    System.out.println("-" + syn.content);
                    nextSyn();
                } else {
                    System.out.println("负数表达错误！");
                }
            } else {
                if (sign < wordList.size()) {
                    fplace = syn.content;
                }
                nextSyn();//读取下一个单词符号；
            }
        } else if (syn.kind == 17) {     //'('
            nextSyn();//读取下一个单词符号；
            if ((fplace = expression()) == null) {
                return null;
            }
            if (syn.kind == 18) {      //')'
                nextSyn();//读取下一个单词符号；
            } else {
                System.out.println("输出')'错误");
                return null;
            }
        } else {
            System.out.println(syn.content + "输出'('错误");
            return null;
        }
        return fplace;
    }


    /**
     * over == 0 表示表达式处理结束 ，1 表示未结束
     * @return
     */
    /*
    public int expression(){
        System.out.println("处理表达式");
        int over = 0;
        System.out.println(syn.content);
        if(syn.kind == 17){  // (
            expressionMD();
            nextSyn();
            System.out.println(syn.content);
            expressionB();
            nextSyn();
            System.out.println(syn.content);
            if(syn.kind == 18){
                over = 0;
            }
            else{
                System.out.println("表达式缺少“）”");
            }
        }
        else{
            expressionMD();
        }
        return over;
    }

    public int expressionMD(){
        int over = 0;
        //nextSyn();
        if(syn.kind == 24 || syn.kind == 25 || syn.kind == 26 || syn.kind == 9){
            if(syn.kind == 24){
                System.out.println("调用声明类型拓展");
            }
            else if(syn.kind == 9){
                System.out.println("处理负数");
            }
            over = 0;
        }
        return over;
    }

    public int expressionB(){
        int over = 0;
        //nextSyn();
        System.out.println(syn.content);
        if(syn.kind == 8 || syn.kind == 9 || syn.kind == 10 || syn.kind == 11){
            //加减乘除
            System.out.println(syn.content);
            over = expression();
        }
        else{
            over = 0;
        }
        return over;
    }

*/

    /**
     * 声明语句
     */
    public int decStatement() {
        int result = 0;
        if (syn.kind == 6 || syn.kind == 7) {   //数据类型
            nextSyn();
            if (syn.kind == 24) {    //变量名
                nextSyn();
                if (syn.kind == 22) {  //  [
                    result = decStatementExtend();
                    nextSyn();
                    if (syn.kind == 12) {  //  =
                        result = decStatementContent();
                    } else {
                        result = 1;
                    }
                } else if (syn.kind == 12) {  // =
                    result = decStatementContent();
                }
                if (syn.kind == 19) {  //  ;
                    result = 1;
                    System.out.println("声明语句处理结束");
                } else {
                    result = 0;
                    System.out.println("声明语句缺少“;”");
                }
            }
        }

        return result;
    }

    public int decStatementExtend() {
        int result = 0;
        if (syn.kind == 22) {  //数组声明 [int值]  [
            nextSyn();
            if (syn.kind == 25) {  //int值
                nextSyn();
                if (syn.kind == 23) {  // ]
                    result = 1;
                } else {
                    System.out.println("数组声明错误！-缺少“]”");
                    result = 0;
                }
            } else {
                System.out.println("数组声明错误！-缺少“int值”");
                result = 0;
            }
        }
        return result;
    }

    public int decStatementContent() {
        int result = 0;
        nextSyn();
        if (syn.kind == 20) {  //  {
            //处理数组元素集
            nextSyn();
            result = arrayElement();
            System.out.println(syn.content);
            if (syn.kind == 21) {  //  }
                result = 1;
                nextSyn();
            } else {
                System.out.println(syn.line + "行缺少“}”");
            }
        } else {
            expression();
        }
        return result;
    }

    /**
     * 赋值语句
     */
    public int assStatement() {
        int result = 0;
        if (syn.kind == 24) {   //变量名
            nextSyn();
            result = assStatementExtend();
            if (syn.kind == 19 && result == 1) {
                System.out.println("赋值语句分析正确");
            } else {
                System.out.println(syn.line + "行赋值语句出现语法错误");
            }
        }
        return result;
    }

    public int assStatementExtend() {
        int result = 0;
        if (syn.kind == 12) {  //  =
            nextSyn();
            expression();
            result = 1;
        } else if (syn.kind == 22) {   //  [
            nextSyn();
            if (syn.kind == 25) {   // int值
                nextSyn();
                if (syn.kind == 23) {   //   ]
                    nextSyn();
                    if (syn.kind == 12) {  //   =
                        nextSyn();
                        if (expression() != null) {
                            result = 1;
                        }
                    } else {
                        System.out.println(syn.line + "行缺少“=”");
                    }
                } else {
                    System.out.println(syn.line + "行缺少“]”");
                }
            } else {
                result = 0;
                System.out.println(syn.line + "行缺少int值");
            }
        }
        return result;
    }

    /**
     * 输出语句
     */
    public int writeStatement() {
        int result = 0;
        nextSyn();
        if (syn.kind == 17) {    //  (
            nextSyn();
            expression();
            if (syn.kind == 18) {   //    )
                nextSyn();
                if (syn.kind == 19) {   //   ;
                    result = 1;
                } else {
                    System.out.println(syn.line + "行缺少“;”");
                }
            } else {
                System.out.println(syn.kind + "行缺少“)”");
            }
        } else {
            System.out.println(syn.kind + "行缺少“(”");
        }
        return result;
    }

    /**
     * 输入语句
     */
    public int readStatement() {
        int result = 0;
        nextSyn();
        if (syn.kind == 17) {    //  (
            nextSyn();
            if (syn.kind == 24) {    //变量名
                nextSyn();
                decStatementExtend();
                nextSyn();
            }
            if (syn.kind == 18) {   //    )
                nextSyn();
                if (syn.kind == 19) {   //   ;
                    result = 1;
                    return result;
                } else {
                    System.out.println(syn.line + "行缺少“;”");
                }
            } else {
                System.out.println(syn.kind + "行缺少“)”");
            }
        } else {
            System.out.println(syn.kind + "行缺少“(”");
        }
        return result;
    }

    /**
     * if语句
     */
    public int ifStatement() {
        int result = 0;
        if (syn.kind == 1) {  // if
            nextSyn();
            System.out.println(syn.content);
            if (syn.kind == 17) {  // (
                result = condition();
                if (result == 0) {
                    return result;
                }
                if (syn.kind == 18) {  // )
                    nextSyn();
                    if (syn.kind == 20) {  // {
                        result = statements();
                        if (syn.kind == 21) {  // }
                            System.out.println("if语句处理结束");
                            result = 1;
                        } else {
                            result = 0;
                            System.out.println(syn.line + "行if语句缺少“}”");
                        }
                    } else {
                        result = 0;
                        System.out.println(syn.line + "行if语句缺少“{”");
                    }
                } else {
                    System.out.println(syn.line + "行if语句缺少“）”");
                }
            } else {
                result = 0;
                System.out.println(syn.line + "行if语句缺少“)”");
            }
        }
        nextSyn();
        System.out.println(syn.content);
        if (syn.kind == 2) {  // else
            nextSyn();
            if (syn.kind == 20) { // {
                result = statements();
                if (syn.kind == 21) {  // }
                    result = 1;
                    System.out.println("else语句处理结束");
                } else {
                    result = 0;
                    System.out.println(syn.line + "行else语句缺少“}”");
                }
            } else {
                result = 0;
                System.out.println(syn.line + "行else语句缺少“{”");
            }
        }
        return result;
    }

    /**
     * while语句
     */
    public int whileStatement() {
        int result = 0;
        if (syn.kind == 3) {  // while
            nextSyn();
            if (syn.kind == 17) {  // (
                result = condition();
                if (result == 0) {
                    return result;
                }
                if (syn.kind == 18) {  // )
                    nextSyn();
                    if (syn.kind == 20) {  // {
                        result = statements();
                        if (syn.kind == 21) {  // }
                            result = 1;
                            System.out.println("while语句处理结束");
                        } else {
                            result = 0;
                            System.out.println(syn.line + "行while语句缺少“}”");
                        }
                    } else {
                        result = 0;
                        System.out.println(syn.line + "行while语句缺少“{”");
                    }
                } else {
                    System.out.println(syn.line + "行while语句缺少“）”");
                }
            } else {
                result = 0;
                System.out.println(syn.line + "行while语句缺少“)”");
            }
        }
        return result;
    }

    public int condition() {
        System.out.println("处理条件语句");
        int result = 0;
        nextSyn();
        System.out.println(syn.content);
        expression();
        if (syn.kind == 13 || syn.kind == 14 || syn.kind == 15 || syn.kind == 16) {
            //         <                 >                 ==                <>
            nextSyn();
            expression();
            result = 1;
        } else {
            result = 0;
            System.out.println(syn.line + "行条件语句缺少比较运算符！");
        }
        return result;
    }


    public int arrayElement() {
        int result = 0;
        if (syn.kind == 25) {   // int值
            result = iArrayElement();
        } else if (syn.kind == 26) {  // real值
            result = rArrayElement();
        } else if (syn.kind == 9) {  //  -
            nextSyn();
            if (syn.kind == 25) {   // int值
                result = iArrayElement();
            } else if (syn.kind == 26) {  // real值
                result = rArrayElement();
            }
        }
        return result;
    }

    public int iArrayElement() {
        int result = 0;
        if (syn.kind == 25) {   //  int值
            nextSyn();
            while (syn.kind == 19) {   //  ;
                nextSyn();
                if (syn.kind == 25 || syn.kind == 9) {   // int值或 -
                    if (syn.kind == 9) {
                        nextSyn();
                    }
                    nextSyn();
                } else {
                    result = 0;
                    System.out.println(syn.line + "行“;”后缺少数字");
                    return result;
                }
            }
            if (syn.kind != 21) {   //  }
                System.out.println(syn.line + "行出现语法错误");
            } else {
                result = 1;
            }
        }
        return result;
    }

    public int rArrayElement() {
        int result = 0;
        if (syn.kind == 26) {   //  real值
            nextSyn();
            while (syn.kind == 19) {   //  ;
                nextSyn();
                if (syn.kind == 26 || syn.kind == 9) {   // real值或 -
                    if (syn.kind == 9) {
                        nextSyn();
                    }
                    nextSyn();
                } else {
                    result = 0;
                    System.out.println(syn.line + "行“;”后缺少数字");
                    return result;
                }
            }
            if (syn.kind != 21) {
                System.out.println(syn.line + "行出现语法错误");
            } else {
                result = 1;
            }
        }
        return result;
    }
}