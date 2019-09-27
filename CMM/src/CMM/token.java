package CMM;

public class token {
    //种类
    public int kind;
//    //行列
//    public int line;
//    public int col;
    //存储的内容
    public String content;
    public token(int k, String con)
    {
        this.kind = k;
        this.content = con;
    }
}
