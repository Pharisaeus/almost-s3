package net.forprogrammers.almosts3.interfaces.error;

public class RestRequestFailure {
    private final Integer code;
    private final String msg;

    public RestRequestFailure(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
