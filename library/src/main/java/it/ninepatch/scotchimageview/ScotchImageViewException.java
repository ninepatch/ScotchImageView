package it.ninepatch.scotchimageview;

/**
 * Created by luca on 7/12/16.
 */
public class ScotchImageViewException extends RuntimeException {

    public ScotchImageViewException() {
    }

    public ScotchImageViewException(String detailMessage) {
        super(detailMessage);
    }

    public ScotchImageViewException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ScotchImageViewException(Throwable throwable) {
        super(throwable);
    }
}
