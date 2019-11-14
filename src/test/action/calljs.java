package test.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.directwebremoting.Browser;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;

import java.io.*;
import java.io.Console;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

public class calljs {
    volatile static String message = "";
    static Thread t1;
    static ExecutorService executorService = Executors.newCachedThreadPool();

    public void waitjs() {
        inputareafunc();
        mylock();
    }

    public void inputareafunc() {
        Runnable run = new Runnable() {
            private ScriptBuffer scriptBuffer = new ScriptBuffer();

            @Override
            public void run() {
                scriptBuffer.appendCall("inputarea");
                Collection<ScriptSession> sessions = Browser.getTargetSessions();
                for (ScriptSession scriptSession : sessions) {
                    scriptSession.addScript(scriptBuffer);
                }
            }
        };
        Browser.withAllSessions(run);
    }

    public void mylock() {
        executorService.submit(() -> {
            t1 = Thread.currentThread();
            System.out.println("接收就绪:");
            if (message == "") {
                System.out.println("locked");
                LockSupport.park();
            }
            System.out.println("我收到了" + message);
            message = "";
            System.out.println("最后这个说明阻塞成功!");
            executorService.shutdown();
        });
    }

    public void myunlock(String mymessage) {
        executorService.submit(() -> {
            message = mymessage;
            LockSupport.unpark(t1);
            System.out.println("unlocked");
        });
    }

}
