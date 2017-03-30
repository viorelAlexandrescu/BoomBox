package n1njagangsta.boombox.Exceptions;

/**
 * Created by viorel on 17.03.2017.
 */

public class NoSongsException extends Exception {
    public NoSongsException() {
        super("Failed to get first item. No songs on device");
    }
}
