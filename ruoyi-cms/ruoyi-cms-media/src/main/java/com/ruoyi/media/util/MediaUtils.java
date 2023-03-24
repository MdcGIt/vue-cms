package com.ruoyi.media.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.ruoyi.common.utils.ServletUtils;

import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;

public class MediaUtils {

	/**
	 * 获取音视频文件信息
	 * 
	 * @param file
	 * @return
	 */
	public static MultimediaInfo getMultimediaInfo(String url) {
		try {
			MultimediaObject multimediaObject;
			if (ServletUtils.isHttpUrl(url)) {
				multimediaObject = new MultimediaObject(new URL(url));
			} else {
				multimediaObject = new MultimediaObject(new File(url));
			}
			return multimediaObject.getInfo();
		} catch (EncoderException | MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
//		MultimediaInfo multimediaInfo = getMultimediaInfo("C:/Users/liwy/Videos/911Mothers_2010W-480p.mp4");
//		MultimediaInfo multimediaInfo = getMultimediaInfo("http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4");
//		// 封装格式
//		System.out.println(multimediaInfo.getFormat());
//		// 时长
//		System.out.println(multimediaInfo.getDuration());
//		// 元数据
//		System.out.println(multimediaInfo.getMetadata());
//		VideoInfo video = multimediaInfo.getVideo();
//		// 比特率：bps
//		System.out.println("BitRate: " + video.getBitRate());
//		// 视频编码方式
//		System.out.println("Decoder: " + video.getDecoder());
//		// 帧率：Hz
//		System.out.println("FrameRate: " + video.getFrameRate());
//		// 视频尺寸
//		System.out.println("width: " + video.getSize().getWidth());
//		System.out.println("height: " + video.getSize().getHeight());
//		System.out.println("EncoderArgument: " + video.getSize().asEncoderArgument());
//		System.out.println("aspectRatioExpression: " + video.getSize().aspectRatioExpression());
//		// VideoInfo元数据
//		System.out.println(video.getMetadata());
		System.out.println("cost: " + (System.currentTimeMillis() - s));
		
		s = System.currentTimeMillis();
		MultimediaInfo multimediaInfo2 = getMultimediaInfo("C:/Users/liwy/Music/11582.mp3");
		// 格式
		System.out.println(multimediaInfo2.getFormat());
		// 时长
		System.out.println(multimediaInfo2.getDuration());
		// 元数据
		System.out.println(multimediaInfo2.getMetadata());
		AudioInfo audio = multimediaInfo2.getAudio();
		// 比特率：bps
		System.out.println("BitRate: " + audio.getBitRate());
		// 视频编码方式
		System.out.println("Decoder: " + audio.getDecoder());
		// 声道数
		System.out.println("Channels: " + audio.getChannels());
		// 取样率 Hz
		System.out.println("SamplingRate: " + audio.getSamplingRate());
		// AudioInfo元数据
		System.out.println(audio.getMetadata());
		System.out.println("cost: " + (System.currentTimeMillis() - s));

		s = System.currentTimeMillis();
		
		
		System.out.println("cost: " + (System.currentTimeMillis() - s));
	}
	
	/**
	 * 视频转码成mp4
	 */
	public void videoToMp4(String filePath) {
		
	}
}
