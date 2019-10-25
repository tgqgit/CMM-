package test.action;

public class token {
    //种类（单词种类号）
    public int kind;
    //行
    public int line;
    //存储的内容
    public String content;
    public token(int k, String con,int line)
    {
        this.kind = k;
        this.content = con;
        this.line = line;
    }
}
