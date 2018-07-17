package com.example.surfaceview.model

import android.media.*
import android.util.Log
import android.view.Surface
import com.example.surfaceview.`interface`.IPlayerCallBack
import java.io.IOException
import java.nio.ByteBuffer


@Suppress("DEPRECATION")
class VideoPlayer {
    private val TAG = "TAG"
    private val TIMEOUT_US: Long = 10000
    private var callBack: IPlayerCallBack? = null
    private var videoThread: VideoThread? = null
    private var audioThread: AudioThread? = null
    private var isPlaying: Boolean = false
    private var filePath: String? = null
    private var surface: Surface? = null

    constructor(surface: Surface, filePath: String) {
        this.surface = surface
        this.filePath = filePath
    }

    constructor(surface: Surface) {
        this.surface = surface
    }

    fun setFilePath(filePath: String) {
        this.filePath = filePath
    }

    fun setCallBack(callBack: IPlayerCallBack) {
        this.callBack = callBack
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun play() {
        isPlaying = true
        if (videoThread == null) {
            videoThread = VideoThread()
            videoThread!!.start()
        }
        if (audioThread == null) {
            audioThread = AudioThread()
            audioThread!!.start()
        }
    }

    fun stop() {
        isPlaying = false
        audioThread?.run {
            interrupt()
        }
        videoThread?.run {
            interrupt()
        }
    }

    /*将缓冲区传递至解码器
     * 如果到了文件末尾，返回true;否则返回false
     */
    private fun putBufferToCoder(extractor: MediaExtractor, decoder: MediaCodec, inputBuffers: Array<ByteBuffer>): Boolean {
        var isMediaEOS = false
        // 取得一个空闲的输入缓存区的索引，因为输入缓存区是以队列的方式被重复数据，出队的是空闲的缓存区
        val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
        if (inputBufferIndex >= 0) {
            // 根据索引得到输入缓存区
            val inputBuffer = inputBuffers[inputBufferIndex]
            // 读取数据
            val sampleSize = extractor.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                isMediaEOS = true
                Log.e(TAG, "decord media end of stream")
            } else {
                // 数据入队
                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                // 移动到下一帧
                extractor.advance()
            }
        }
        return isMediaEOS
    }

    //获取指定类型媒体文件所在轨道
    private fun getMediaTrackIndex(videoExtractor: MediaExtractor, MEDIA_TYPE: String): Int {
        var trackIndex = -1
        for (i in 0 until videoExtractor.trackCount) {
            val mediaFormat = videoExtractor.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(MEDIA_TYPE)) {
                trackIndex = i
                break
            }
        }
        return trackIndex
    }

    //延迟渲染
    private fun sleepRender(audioBufferInfo: MediaCodec.BufferInfo, startMs: Long) {
        while (audioBufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                break
            }
        }
    }

    private inner class VideoThread : Thread() {
        override fun run() {
            if (surface == null || !surface!!.isValid()) {
                Log.e("TAG", "surface invalid!")
                return
            }
            val videoExtractor = MediaExtractor()
            var videoCodec: MediaCodec? = null
            try {
                videoExtractor.setDataSource(filePath!!)
            } catch (e: IOException) {
                Log.e("VideoPlayer", "VideoThread : ${e.message}")
            }

            val videoTrackIndex: Int
            //获取视频所在轨道
            videoTrackIndex = getMediaTrackIndex(videoExtractor, "video/")
            if (videoTrackIndex >= 0) {
                val mediaFormat = videoExtractor.getTrackFormat(videoTrackIndex)
                val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
                val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
                val time = (mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000000).toFloat()
                callBack!!.videoAspect(width, height, time)
                videoExtractor.selectTrack(videoTrackIndex)
                try {
                    // 创建解码器
                    videoCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME))
                    // 设置解码数据显示的地方
                    videoCodec!!.configure(mediaFormat, surface, null, 0)
                } catch (e: IOException) {
                    Log.e("VideoPlayer", "VideoThread : ${e.message}")
                }
            }

            if (videoCodec == null) {
                Log.e(TAG, "MediaCodec null")
                return
            }
            videoCodec.start()

            val videoBufferInfo = MediaCodec.BufferInfo()
            // 输入缓存区数组，就是放置未解码数据的地方
            val inputBuffers = videoCodec.inputBuffers
            // 输出缓存区数组，放置已经解码的数据的地方
            // val outputBuffers = videoCodec.inputBuffers
            var isVideoEOS = false

            val startMs = System.currentTimeMillis()
            while (!Thread.interrupted()) {
                if (!isPlaying) {
                    continue
                }
                //将资源传递到解码器
                if (!isVideoEOS) {
                    isVideoEOS = putBufferToCoder(videoExtractor, videoCodec, inputBuffers)
                }
                // 读取解码的输出
                val outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US)
                when (outputBufferIndex) {
                // 格式已经更改
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.e(TAG, "format changed")
                // 请重试
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Log.e(TAG, "超时")
                // 输出缓冲区已经改变
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED ->
                        //outputBuffers = videoCodec.getOutputBuffers();
                        Log.e(TAG, "output buffers changed")
                    else -> {
                        //直接渲染到Surface时使用不到outputBuffer
                        //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        sleepRender(videoBufferInfo, startMs)
                        //渲染
                        // 输出数据已经使用完毕，那么可以释放它，这样解码器就可以重复使用它了
                        videoCodec.releaseOutputBuffer(outputBufferIndex, true)
                    }
                }

                if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.e(TAG, "buffer stream end")
                    break
                }
            }//end while
            videoCodec.stop()
            videoCodec.release()
            videoExtractor.release()
        }
    }

    private inner class AudioThread : Thread() {
        private var audioInputBufferSize: Int = 0

        private var audioTrack: AudioTrack? = null

        override fun run() {
            val audioExtractor = MediaExtractor()
            var audioCodec: MediaCodec? = null
            try {
                audioExtractor.setDataSource(filePath!!)
            } catch (e: IOException) {
                Log.e("VideoPlayer", "AudioThread : ${e.message}")
            }

            for (i in 0 until audioExtractor.trackCount) {
                val mediaFormat = audioExtractor.getTrackFormat(i)
                val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("audio/")) {
                    audioExtractor.selectTrack(i)
                    val audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    val audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    val minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                            if (audioChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT)
                    val maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    audioInputBufferSize = if (minBufferSize > 0) minBufferSize * 4 else maxInputSize
                    val frameSizeInBytes = audioChannels * 2
                    audioInputBufferSize = audioInputBufferSize / frameSizeInBytes * frameSizeInBytes
                    audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                            audioSampleRate,
                            if (audioChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            audioInputBufferSize,
                            AudioTrack.MODE_STREAM)
                    audioTrack!!.play()
                    Log.e(TAG, "audio play")
                    try {
                        audioCodec = MediaCodec.createDecoderByType(mime)
                        audioCodec!!.configure(mediaFormat, null, null, 0)
                    } catch (e: IOException) {
                        Log.e("VideoPlayer", "AudioThread : ${e.message}")
                    }
                    break
                }
            }
            if (audioCodec == null) {
                Log.e(TAG, "audio decoder null")
                return
            }
            audioCodec.start()
            //
            val buffers = audioCodec.outputBuffers
            var sz = buffers[0].capacity()
            if (sz <= 0)
                sz = audioInputBufferSize
            var mAudioOutTempBuf = ByteArray(sz)

            val audioBufferInfo = MediaCodec.BufferInfo()
            val inputBuffers = audioCodec.inputBuffers
            var outputBuffers = audioCodec.outputBuffers
            var isAudioEOS = false
            val startMs = System.currentTimeMillis()

            while (!Thread.interrupted()) {
                if (!isPlaying) {
                    continue
                }
                if (!isAudioEOS) {
                    isAudioEOS = putBufferToCoder(audioExtractor, audioCodec, inputBuffers)
                }
                //
                val outputBufferIndex = audioCodec.dequeueOutputBuffer(audioBufferInfo, TIMEOUT_US)
                when (outputBufferIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.e(TAG, "format changed")
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Log.e(TAG, "超时")
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        outputBuffers = audioCodec.outputBuffers
                        Log.e(TAG, "output buffers changed")
                    }
                    else -> {
                        val outputBuffer = outputBuffers[outputBufferIndex]
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        sleepRender(audioBufferInfo, startMs)
                        if (audioBufferInfo.size > 0) {
                            if (mAudioOutTempBuf.size < audioBufferInfo.size) {
                                mAudioOutTempBuf = ByteArray(audioBufferInfo.size)
                            }
                            outputBuffer.position(0)
                            outputBuffer.get(mAudioOutTempBuf, 0, audioBufferInfo.size)
                            outputBuffer.clear()
                            if (audioTrack != null)
                                audioTrack!!.write(mAudioOutTempBuf, 0, audioBufferInfo.size)
                        }
                        //
                        audioCodec.releaseOutputBuffer(outputBufferIndex, false)
                    }
                }

                if (audioBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.e(TAG, "buffer stream end")
                    break
                }
            }//end while
            with(audioCodec) {
                stop()
                release()
            }
            audioExtractor.release()
            audioTrack?.run {
                stop()
                release()
            }
        }
    }
}