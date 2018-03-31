package com.xxx.reader.download;

import com.xxx.reader.prepare.Cancelable;

/**
 * Created by davidleen29 on 2018/3/23.
 */

public interface DownloadListener {

      void startDownload(String url, String filePath, Cancelable cancelable, NotifyListener notifyListener);


    interface NotifyListener {
        void onComplete(String url, String filePath);

        void onProgress(String url, String filePath, long  bytesLoaded,long maxSize,float process);

        void onError(String url, String filePath);

        void onCancel(String url, String filePath);
    }
}
