package io.mateu;

import io.mateu.mdd.core.util.Helper;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.time.LocalDateTime;

/**
 * Created by miguel on 28/4/17.
 */
@WebListener
public class ContextListener implements javax.servlet.ServletContextListener {
    private Thread hiloTaskRunner;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("****CONTEXTLISTENER****");
        Helper.loadProperties();

        if ("yes".equalsIgnoreCase(System.getProperty("taskrunner")) || "true".equalsIgnoreCase(System.getProperty("taskrunner"))) {
            if (hiloTaskRunner == null) {
                System.out.println("****ARRANCANDO TASKRUNNER****");
                hiloTaskRunner = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean dentro = true;
                        while (dentro) {
                            try {
                                logarEstado();
                                System.out.println("..." + LocalDateTime.now());
                                Thread.sleep(60000l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                dentro = false;
                            }
                        }
                        System.out.println("****TASKRUNNER DETENIDO****");
                    }
                });
                hiloTaskRunner.start();
                System.out.println("****TASKRUNNER ARRANCADO****");
            }
        }
    }

    private void logarEstado() {
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB] ##### " + LocalDateTime.now());

        //Print used memory
        System.out.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        //Print free memory
        System.out.println("Free Memory:"
                + runtime.freeMemory() / mb);

        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);

        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (hiloTaskRunner != null) hiloTaskRunner.interrupt();
    }
}
