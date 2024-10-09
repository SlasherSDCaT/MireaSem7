package muhametshin_p3.task_1;

public class SensorInfo {
    private SensorType type;
    private Integer value;

    public SensorInfo(SensorType type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public SensorType getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
