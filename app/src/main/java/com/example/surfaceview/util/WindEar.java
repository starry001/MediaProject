package com.example.surfaceview.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WindEar {
    private static final String TAG = "rustApp";
    private static final String TMP_FOLDER_NAME = "AnWindEar";
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;
    private static final int PLAY_AUDIO_BUFFER_TIMES = 1;
    private static final int AUDIO_FREQUENCY = 44100;

    private static final int RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private static final int PLAY_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecordThread aRecordThread;           // 录制线程
    private volatile WindState state = WindState.IDLE; // 当前状态
    private File tmpPCMFile = null;
    private File tmpWavFile = null;
    private OnState onStateListener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * PCM缓存目录
     */
    private static String cachePCMFolder;

    /**
     * wav缓存目录
     */
    private static String wavFolderPath;

    private static WindEar instance = new WindEar();

    private WindEar() {

    }

    public static WindEar getInstance() {
        if (null == instance) {
            instance = new WindEar();
        }
        return instance;
    }

    public void setOnStateListener(OnState onStateListener) {
        this.onStateListener = onStateListener;
    }

    /**
     * 初始化目录
     */
    public static void init() {
        // 存储在App内  也可以存在SD卡上
        cachePCMFolder = Environment.getExternalStorageDirectory() + File.separator + TMP_FOLDER_NAME;

        File folder = new File(cachePCMFolder);
        if (!folder.exists()) {
            boolean f = folder.mkdirs();
            Log.e(TAG, String.format(Locale.CHINA, "PCM目录:%s -> %b", cachePCMFolder, f));
        } else {
            for (File f : folder.listFiles()) {
                boolean d = f.delete();
                Log.e(TAG, String.format(Locale.CHINA, "删除PCM文件:%s %b", f.getName(), d));
            }
            Log.e(TAG, String.format(Locale.CHINA, "PCM目录:%s", cachePCMFolder));
        }

        wavFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + TMP_FOLDER_NAME;
        File wavDir = new File(wavFolderPath);
        if (!wavDir.exists()) {
            boolean w = wavDir.mkdirs();
            Log.e(TAG, String.format(Locale.CHINA, "wav目录:%s -> %b", wavFolderPath, w));
        } else {
            Log.e(TAG, String.format(Locale.CHINA, "wav目录:%s", wavFolderPath));
        }
    }

    /**
     * 开始录制音频
     */
    public synchronized void startRecord(boolean createWav) {
        if (!state.equals(WindState.IDLE)) {
            Log.e(TAG, "无法开始录制，当前状态为 " + state);
            return;
        }
        try {
            tmpPCMFile = File.createTempFile("recording", ".pcm", new File(cachePCMFolder));
            if (createWav) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss", Locale.CHINA);
                tmpWavFile = new File(wavFolderPath + File.separator + "r" + sdf.format(new Date()) + ".wav");
            }
            Log.e(TAG, "tmp file " + tmpPCMFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != aRecordThread) {
            aRecordThread.interrupt();
            aRecordThread = null;
        }
        aRecordThread = new AudioRecordThread(createWav);
        aRecordThread.start();
    }

    public synchronized void stopRecord() {
        if (!state.equals(WindState.RECORDING)) {
            return;
        }
        state = WindState.STOP_RECORD;
        notifyState(state);
    }

    /**
     * 播放录制好的PCM文件
     */
    public synchronized void startPlayPCM() {
        if (!isIdle()) {
            return;
        }
        new AudioTrackPlayThread(tmpPCMFile).start();
    }

    /**
     * 播放录制好的wav文件
     */
    public synchronized void startPlayWav() {
        if (!isIdle()) {
            return;
        }
        new AudioTrackPlayThread(tmpWavFile).start();
    }

    public synchronized void stopPlay() {
        if (!state.equals(WindState.PLAYING)) {
            return;
        }
        state = WindState.STOP_PLAY;
    }

    private synchronized boolean isIdle() {
        return WindState.IDLE.equals(state);
    }

    /**
     * 音频录制线程
     * 使用FileOutputStream来写文件
     */
    private class AudioRecordThread extends Thread {
        AudioRecord aRecord;
        int bufferSize;
        boolean createWav;

        AudioRecordThread(boolean createWav) {
            this.createWav = createWav;
            bufferSize = AudioRecord.getMinBufferSize(AUDIO_FREQUENCY,
                    RECORD_CHANNEL_CONFIG, AUDIO_ENCODING) * RECORD_AUDIO_BUFFER_TIMES;
            Log.e(TAG, "record buffer size = " + bufferSize);
            aRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_FREQUENCY,
                    RECORD_CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize);
        }

        @Override
        public void run() {
            state = WindState.RECORDING;
            notifyState(state);
            Log.e(TAG, "录制开始");
            try {
                // 这里选择FileOutputStream而不是DataOutputStream
                FileOutputStream pcmFos = new FileOutputStream(tmpPCMFile);

                FileOutputStream wavFos = new FileOutputStream(tmpWavFile);
                if (createWav) {
                    writeWavFileHeader(wavFos, bufferSize, AUDIO_FREQUENCY, aRecord.getChannelCount());
                }
                aRecord.startRecording();
                byte[] byteBuffer = new byte[bufferSize];
                while (state.equals(WindState.RECORDING) && !isInterrupted()) {
                    int end = aRecord.read(byteBuffer, 0, byteBuffer.length);
                    pcmFos.write(byteBuffer, 0, end);
                    pcmFos.flush();
                    if (createWav) {
                        wavFos.write(byteBuffer, 0, end);
                        wavFos.flush();
                    }
                }
                aRecord.stop(); // 录制结束
                pcmFos.close();
                wavFos.close();
                if (createWav) {
                    // 修改header
                    FileInputStream pcmFis = new FileInputStream(tmpWavFile);
                    RandomAccessFile wavRaf = new RandomAccessFile(tmpWavFile, "rw");
                    byte[] header = generateWavFileHeader(pcmFis.getChannel().size(), AUDIO_FREQUENCY, aRecord.getChannelCount());
                    wavRaf.seek(0);
                    wavRaf.write(header);
                    wavRaf.close();
                    pcmFis.close();
                }
                Log.i(TAG, "audio tmp file len: " + tmpPCMFile.length());
            } catch (Exception e) {
                Log.e(TAG, "AudioRecordThread:", e);
                notifyState(WindState.ERROR);
            }
            notifyState(state);
            state = WindState.IDLE;
            notifyState(state);
            Log.e(TAG, "录制结束");
        }

    }

    /**
     * AudioTrack播放音频线程
     * 使用FileInputStream读取文件
     */
    private class AudioTrackPlayThread extends Thread {
        AudioTrack track;
        int bufferSize = 10240;
        File audioFile;

        AudioTrackPlayThread(File aFile) {
            setPriority(Thread.MAX_PRIORITY);
            audioFile = aFile;
            int bufferSize = AudioTrack.getMinBufferSize(AUDIO_FREQUENCY,
                    PLAY_CHANNEL_CONFIG, AUDIO_ENCODING) * PLAY_AUDIO_BUFFER_TIMES;

            track = new AudioTrack(AudioManager.STREAM_MUSIC,
                    AUDIO_FREQUENCY,
                    PLAY_CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize,
                    AudioTrack.MODE_STREAM);
        }

        @Override
        public void run() {
            super.run();
            state = WindState.PLAYING;
            notifyState(state);
            try {
                FileInputStream fis = new FileInputStream(audioFile);
                track.play();
                byte[] aByteBuffer = new byte[bufferSize];
                while (state.equals(WindState.PLAYING) &&
                        fis.read(aByteBuffer) >= 0) {
                    track.write(aByteBuffer, 0, aByteBuffer.length);
                }
                track.stop();
                track.release();
            } catch (Exception e) {
                Log.e(TAG, "AudioTrackPlayThread:", e);
                notifyState(WindState.ERROR);
            }
            state = WindState.STOP_PLAY;
            notifyState(state);
            state = WindState.IDLE;
            notifyState(state);
        }

    }

    private synchronized void notifyState(final WindState currentState) {
        if (null != onStateListener) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onStateListener.onStateChanged(currentState);
                }
            });
        }
    }

    public interface OnState {
        void onStateChanged(WindState currentState);
    }

    /**
     * 表示当前状态
     */
    public enum WindState {
        ERROR,
        IDLE,
        RECORDING,
        STOP_RECORD,
        PLAYING,
        STOP_PLAY
    }

    /**
     * @param out            wav音频文件流
     * @param totalAudioLen  不包括header的音频数据总长度
     * @param longSampleRate 采样率,也就是录制时使用的频率
     * @param channels       audioRecord的频道数量
     * @throws IOException 写文件错误
     */
    private void writeWavFileHeader(FileOutputStream out, long totalAudioLen, long longSampleRate,
                                    int channels) throws IOException {
        byte[] header = generateWavFileHeader(totalAudioLen, longSampleRate, channels);
        out.write(header, 0, header.length);
    }

    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param totalAudioLen  不包括header的音频数据总长度
     * @param longSampleRate 采样率,也就是录制时使用的频率
     * @param channels       audioRecord的频道数量
     */
    private byte[] generateWavFileHeader(long totalAudioLen, long longSampleRate, int channels) {
        long totalDataLen = totalAudioLen + 36;
        long byteRate = longSampleRate * 2 * channels;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (2 * channels);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }
}
