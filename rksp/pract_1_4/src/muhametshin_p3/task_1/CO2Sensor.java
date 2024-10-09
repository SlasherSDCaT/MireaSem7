package muhametshin_p3.task_1;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

import java.util.Random;

public class CO2Sensor extends Observable<SensorInfo> {
    private final PublishSubject<SensorInfo> subject = PublishSubject.create();

    @Override
    protected void subscribeActual(Observer<? super SensorInfo> observer) {
        subject.subscribe(observer);
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                int co = new Random().nextInt(30, 101);
                subject.onNext(new SensorInfo(SensorType.CO2, co));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
