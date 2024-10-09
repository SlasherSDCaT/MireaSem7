package muhametshin_p3.task_1;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class Alarmer implements Observer<SensorInfo> {

    private final int CO2_LIMIT = 70;
    private final int TEMP_LIMIT = 25;
    private int temperature = 0;
    private int co2 = 0;


    @Override
    public void onSubscribe(@NonNull Disposable disposable) {
        System.out.println(disposable.hashCode() + " has subscribed");
    }

    @Override
    public void onNext(@NonNull SensorInfo sensorInfo) {
        if (sensorInfo.getType() == SensorType.TEMPERATURE) temperature = sensorInfo.getValue();
        else co2 = sensorInfo.getValue();

        systemCheck(sensorInfo);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Completed");

    }

    public void systemCheck(SensorInfo info) {
        if (temperature >= TEMP_LIMIT && co2 >= CO2_LIMIT)
            System.out.println("ALARM!!! Temperature/CO2: " + temperature + "/" + co2);
        else if (temperature >= TEMP_LIMIT)
            System.out.println("Temperature warning: " + temperature);
        else if (co2 >= CO2_LIMIT)
            System.out.println("CO2 warning: " + co2);
    }

    public static void main(String[] args) {
        Alarmer alarmer = new Alarmer();

        CO2Sensor co2Sensor = new CO2Sensor();
        TemperatureSensor temperatureSensor = new TemperatureSensor();

        temperatureSensor.subscribe(alarmer);
        co2Sensor.subscribe(alarmer);

        temperatureSensor.start();
        co2Sensor.start();

        try {
            Thread.sleep(20000); // Запуск системы на 10 секунд
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
