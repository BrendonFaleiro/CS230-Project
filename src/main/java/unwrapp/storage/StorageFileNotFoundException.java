package unwrapp.storage;

/**
 * Created by Shubham Mittal on 6/07/17.
 */


public class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String msg) {
        super(msg);
    }
    public StorageFileNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}