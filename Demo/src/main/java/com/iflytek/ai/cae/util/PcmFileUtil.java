package com.iflytek.ai.cae.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PcmFileUtil {
	private String WRITE_PCM_DIR = "/data/PCM/";
	
	private final static String PCM_SURFFIX = ".pcm";
	
	private FileOutputStream mFos;
	
	private FileInputStream mFis;
	
	public PcmFileUtil() {
		
	}
	
	public PcmFileUtil(String writeDir) {
		WRITE_PCM_DIR = writeDir;
	}
	
	public boolean openPcmFile(String filePath) {
		File file = new File(filePath);
		try {
			mFis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mFis = null;
			return false;
		}
		
		return true;
	}
	
	public int read(byte[] buffer) {
		if (null != mFis) {
			try {
				return mFis.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
				closeReadFile();
				return 0;
			}
		}
		
		return -1;
	}
	
	public void closeReadFile() {
		if (null != mFis) {
			try {
				mFis.close();
				mFis = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void createPcmFile() {
		File dir = new File(WRITE_PCM_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (null != mFos) {
			return;
		}
		
		DateFormat df = new SimpleDateFormat("MM-dd-hh-mm-ss", Locale.CHINA);
		String filename = df.format(new Date());
		String pcmPath = WRITE_PCM_DIR + filename + PCM_SURFFIX;
		
		File pcm = new File(pcmPath);
		try {
			if(pcm.createNewFile()) {
				mFos = new FileOutputStream(pcm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(byte[] data) {
		synchronized (PcmFileUtil.this) {
			if (null != mFos) {
				try {
					mFos.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void write(byte[] data, int offset, int len) {
		synchronized (PcmFileUtil.this) {
			if (null != mFos) {
				try {
					mFos.write(data, offset, len);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void closeWriteFile() {
		synchronized (PcmFileUtil.this) {
			if (null != mFos) {
				try {
					mFos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFos = null;
			}
		}
	}


	public void WavToPcm(String src,String target) {
		try {
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(target);
			byte[] buf = new byte[1024 * 4];
			int size = fis.read(buf);
			fos.write(buf, 44, size - 44);
			size = fis.read(buf);
			while (size != -1) {
				fos.write(buf, 0, size);
				size = fis.read(buf);
			}
			fis.close();
			fos.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File tranPcmToWavFile(String pcmfilepath, String wavfilepath, int BitsPerSample, int SamplesPerSec, int Channels) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			File pcmFile = new File(pcmfilepath);
			fis = new FileInputStream(pcmFile);
			File file = new File(wavfilepath);
			if (!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(wavfilepath);


			int PCMSize = 0;
			byte[] buf = new byte[1024 * 4];
			int size = fis.read(buf);

			while (size != -1) {
				PCMSize += size;
				size = fis.read(buf);
			}
			fis.close();

			WaveHeader header = new WaveHeader();
			//长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
			header.fileLength = PCMSize + (44 - 8);
			header.FmtHdrLeth = 16;
			header.BitsPerSample = (short) BitsPerSample;  //采样精度
			header.Channels = (short) Channels;            //通道数
			header.FormatTag = 0x0001;
			header.SamplesPerSec = SamplesPerSec;          //采样频率
			header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
			header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
			header.DataHdrLeth = PCMSize;

			byte[] h = header.getHeader();

			assert h.length == 44; //WAV标准，头部应该是44字节
			//write header
			fos.write(h, 0, h.length);
			//write data stream
			fis = new FileInputStream(pcmFile);
			size = fis.read(buf);
			while (size != -1) {
				fos.write(buf, 0, size);
				size = fis.read(buf);
			}
			fis.close();
			fos.close();
			return new File(wavfilepath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
}
