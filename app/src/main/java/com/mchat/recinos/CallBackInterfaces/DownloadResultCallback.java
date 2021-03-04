package com.mchat.recinos.CallBackInterfaces;

/**
 * Callback method called after an image download task has concluded and the data obtained is provided.
 */
public interface DownloadResultCallback {
    void onImageDownloadResult(int id, byte[] result);
}
