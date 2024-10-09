package muhametshin_p3.task_4;

public class File {

    private final String fileType;
    private final int fileSize;

    public File(String fileType, int fileSize) {
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
    public String getFileType() {
        return fileType;
    }
    public int getFileSize() {
        return fileSize;
    }


}
