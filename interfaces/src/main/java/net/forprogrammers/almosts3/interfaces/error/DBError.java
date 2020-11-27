package net.forprogrammers.almosts3.interfaces.error;

public class DBError {
    private final String msg;

    public DBError(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
