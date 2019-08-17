package scyuan.irsdp.jobhandler;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

import java.util.concurrent.TimeUnit;

public class ParentBufferingJobTransformation extends ComponentDefinition {
    Component jh = create(SynchronousJobHandler.class, Init.NONE);
    Component timer = create(JavaTimer.class, Init.NONE);
    Component th = create(BufferingJobTransformation.class, new BufferingJobTransformation.Init(3));
    Component js = create(JobSubmitter.class, Init.NONE);


    {
        connect(js.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(js.getNegative(TransformationHandler.class), th.getPositive(TransformationHandler.class));
        connect(th.getNegative(JobHandler.class), jh.getPositive(JobHandler.class));
        connect(th.getNegative(Timer.class), timer.getPositive(Timer.class));
    }


    public static void main(String[] args) {
        Kompics.createAndStart(ParentBufferingJobTransformation.class, 1);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Kompics.shutdown();
    }
}
