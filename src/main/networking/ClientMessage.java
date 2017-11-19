package main.networking;

/**
 * Created by rkossowski on 18.11.2017.
 */
public enum  ClientMessage {
    LOG_IN,
    REGISTER,
    GET_BACKUP_FILES_LIST,
    GET_ALL_FILE_VERSIONS,
    REMOVE_FILE,
    REMOVE_FILE_VERSION,

    EXIT;
}
