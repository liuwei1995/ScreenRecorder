package com.liuwei1995.screenrecorder;

import org.junit.Test;

import static com.liuwei1995.screenrecorder.ExampleUnitTest.FileType.MP4_AUDIO;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String name = MP4_AUDIO.value();
        System.out.println(name);
    }

    enum FileType{

        MP4_AUDIO("audio/mp4"), MP4_VIDEO("video/mp4");

        public String s;

        public String value(){
           return s;
        }

        FileType(String s) {
            this.s = s;
        }
    }
}