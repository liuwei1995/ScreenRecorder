package com.liuwei1995.screenrecorder.util;

import android.text.TextUtils;

import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by liuwei on 2017/8/11 16:11
 * @hide
 */

public class Mp4ParserUtils {

//    public static void join(List<String> filePaths, String resultPath){
//
//    }
    public static boolean join(List<File> filePaths, String resultPath){
        boolean result = false;

        if (filePaths == null || filePaths.size() <= 0 || TextUtils.isEmpty(resultPath)) {
            throw new IllegalArgumentException();
        }
        if (filePaths.size() == 1) { // 只有一个视频片段，不需要合并
            return true;
        }
        for (File filePath : filePaths) {
            try {
                URL u = new URL(filePath.toString());
                URLConnection uc = u.openConnection();
                String type = uc.getContentType();


                H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(filePath));
                AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
                Movie movie = new Movie();
                movie.addTrack(h264Track);
                movie.addTrack(aacTrack);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    enum FileType{

        MP4_AUDIO("audio/mp4"), MP4_VIDEO("video/mp4"),_3GPP_AUDIO("audio/3gpp"), _3GPP_VIDEO("video/3gpp");

        public String s;

        public String value(){
            return s;
        }

        FileType(String s) {
            this.s = s;
        }
    }


}
