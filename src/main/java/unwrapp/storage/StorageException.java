package unwrapp.storage;

/**
 * Created by Shubham Mittal on 6/07/17.
 */


public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
