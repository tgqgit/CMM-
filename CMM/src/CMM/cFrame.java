package CMM;
import java.awt.Button;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class cFrame extends Frame{
    public void display() {
        this.setTitle("CMM解释器");
        this.setSize(480,200);
        this.setLocation (200,400);
        Button b1 = new Button("exit");
        b1.setSize(10,20);
        this.add(b1);
        //设置可见
        this.setVisible(true);
    }
}
