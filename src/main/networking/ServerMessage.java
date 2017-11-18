package main.networking;

/**
 * Created by rkossowski on 18.11.2017.
 */
public enum ServerMessage {
    SEND_USERNAME,
    SEND_PASSWORD,
    USER_CREATED,
    USER_EXISTS,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    SENDING_BACKUP_FILES_LIST,
    SENDING_BACKUP_FILES_LIST_FINISHED;
}
