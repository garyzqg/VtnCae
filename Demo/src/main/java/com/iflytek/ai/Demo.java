package com.iflytek.ai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.ai.recorder.RawAudioRecorder;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.ai.cae.CaeOperator;
import com.iflytek.ai.cae.OnCaeOperatorlistener;
import com.iflytek.cae.R;
import com.iflytek.ai.cae.CaeCoreHelper;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import android.media.AudioManager;

/**
 * 语义理解demo。
 */
public class Demo extends Activity implements OnClickListener {
	private static String TAG = Demo.class.getSimpleName();
    public static Context mainContext;
	private Toast mToast;
	private TextView txt_status;
	private EditText mNlpText;
	private Button btnSave;
	private Button btnBLS;
	private AudioManager mAudioManager;
	private int maxVolume = 9;
	private  int minVolume = 3;
	private  int curtVol = 0;


	private AIUIAgent mAIUIAgent = null;
	private int mAIUIState = AIUIConstant.STATE_IDLE;


	private String mSyncSid = "";

	// 多麦克算法库
	private CaeOperator caeOperator;
	//语义结果处理
	private boolean isAsring=true;
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.nlpdemo);
		initLayout();
		mainContext = this;
		mToast = Toast.makeText(this, "  ", Toast.LENGTH_SHORT);
		requestPermission();

		if(!CaeCoreHelper.hasModeFile()) {
			CaeCoreHelper.portingFile(this);
		}
		//创建AIUI 代理
        createAgent();
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		maxVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		curtVol = maxVolume;

		//Log.i("shengjie", "start mAudioManager.setParameters");
		//mAudioManager.setParameters("dsp_loop" + "=" + "0");
		//mAudioManager.setParameters("test_out_stream_route" + "=" + "0x2");
		//mAudioManager.setParameters("test_in_stream_route" + "=" + "0x80000080");
		//mAudioManager.setParameters("dsploop_type" + "=" + "1");
		//mAudioManager.setParameters("dsp_loop" + "=" + "1");
		//Log.i("shengjie", "mAudioManager.setParameters done");

		initCaeEngine();
	}




	private void initLayout() {
		findViewById(R.id.text_nlp_start).setOnClickListener(Demo.this);
		findViewById(R.id.btnRec).setOnClickListener(Demo.this);
		findViewById(R.id.btnStop).setOnClickListener(Demo.this);
		findViewById(R.id.btnNlp).setOnClickListener(Demo.this);
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(Demo.this);

		btnBLS = (Button) findViewById(R.id.btnBls);
		btnBLS.setOnClickListener(Demo.this);

		findViewById(R.id.btnStopBls).setOnClickListener(Demo.this);



		txt_status = (TextView) findViewById(R.id.txt_status);
		mNlpText = (EditText) findViewById(R.id.nlp_text);
	}

	Handler mMainHandler = new Handler();
	private String RETURN_DESKTOP = " 再按一次返回桌面";
	private int PERMISSION_CODE = 0;    //权限申请码
	/**
	 * 动态申请权限
	 */
	public void requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
			//申请权限
			ActivityCompat.requestPermissions(Demo.this, permissions, PERMISSION_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults.length == 2
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
			} else {
				//没有取得权限
				Uri packageURI = Uri.parse("package:" + "com.iflytek.morfeicore");
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onClick(View view) {
		int[] pos = new int[2];
		view.getLocationOnScreen(pos);

		int x = pos[0] + view.getWidth() / 2;
		int y = pos[1] + view.getHeight() / 2;

		Log.d("button_pos", "x:" + x + ", y:" + y);

		switch (view.getId()) {
			case R.id.btnBls:


				break;
			case R.id.btnStopBls:

				break;

			case R.id.btnRec:
                Log.i("shengjie", "start recording -mAudioManager.setParameters dsp_loop = 1");
				mAudioManager.setParameters("dsp_loop" + "=" + "1");
				start();
				break;
			case R.id.btnStop:
				Log.i("shengjie","mAudioManager.setParameters dsp_loop = 0");
				mAudioManager.setParameters("dsp_loop"+"="+"0");
				btnSave.setText("开始保存");
				stopAiuiService();
				if(caeOperator!=null) {
					caeOperator.restCaeEngine();
					caeOperator.stopRecord();
				}
				break;

			case R.id.btnSave:
				if(caeOperator!=null) {
					if(!caeOperator.isAudioSaving()){//默认为false
						caeOperator.startSaveAudio();
						btnSave.setText("停止保存");
					}else{
						caeOperator.stopSaveAudio();
						btnSave.setText("开始保存");
					}
				}

				break;

			default:
				break;
		}
	}

	void start(){

		startAiuiService();
		initCaeEngine();

		if(caeOperator!=null) {
			//txt_status.setText("当前阵列通道号配置："+caeOperator.getChannelNum());
			caeOperator.startRecord(new RawAudioRecorder.RecordListener(){
				@Override
				public void onRecordStart() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setText("-->开启录音成功！");
						}
					});
				}

				@Override
				public void onPcmData(byte[] data) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setText("-->接收数据成功！");
						}
					});
				}

				@Override
				public void onError(int error, String errorMessage) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setText("开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！");
							distoryCaeEngine();
						}
					});
				}
			});
		}
	}


	private String getAIUIParams() {
		String params = "";

		AssetManager assetManager = getResources().getAssets();
		try {
			InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
			byte[] buffer = new byte[ins.available()];

			ins.read(buffer);
			ins.close();

			params = new String(buffer);
			Log.d(TAG,"params="+params);
			JSONObject paramsJson = new JSONObject(params);

			params = paramsJson.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return params;
	}

	private void createAgent() {
		if (null == mAIUIAgent) {
			Log.i(TAG, "create aiui agent");

			//获取assert 目录下的aiui_phone.cfg 配置文件
			mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
//			AIUIMessage resetWakeupMsg = new AIUIMessage(
//					AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
//			mAIUIAgent.sendMessage(resetWakeupMsg);

		}

		if (null == mAIUIAgent) {
			final String strErrorTip = "创建AIUIAgent失败！";
			showTip(strErrorTip);

			mNlpText.setText(strErrorTip);
		} else {
			showTip("AIUIAgent已创建");
		}
	}

	private  void startAiuiService(){

		if(mAIUIAgent!=null) {
			Log.d(TAG,"start aiui service!");
			AIUIMessage startMsg = new AIUIMessage(
					AIUIConstant.CMD_START, 0, 0, "", null);
			mAIUIAgent.sendMessage(startMsg);
		}
	}

	private  void stopAiuiService(){
        if(mAIUIAgent!=null) {
			Log.d(TAG,"stop aiui  service!");
			AIUIMessage startMsg = new AIUIMessage(
					AIUIConstant.CMD_STOP, 0, 0, "", null);
			mAIUIAgent.sendMessage(startMsg);
		}
	}

	private void destroyAgent() {
		if (null != mAIUIAgent) {
			Log.i(TAG, "destroy aiui agent");

			mAIUIAgent.destroy();
			mAIUIAgent = null;

			showTip("AIUIAgent已销毁");
		} else {
			showTip("AIUIAgent为空");
		}
	}

	private AIUIListener mAIUIListener = new AIUIListener() {

		@Override
		public void onEvent(AIUIEvent event) {
			//Log.i( TAG,  "on event: " + event.eventType );

			switch (event.eventType) {
				case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
					setText("已连接服务器");
					break;

				case AIUIConstant.EVENT_SERVER_DISCONNECTED:
					setText("与服务器断连");
					break;

				case AIUIConstant.EVENT_WAKEUP:
					setText( "EVENT_WAKEUP: 进入识别状态" );
					break;

				case AIUIConstant.EVENT_TTS:
					//showTip( "云端TTS" );
					//Log.d(TAG,"aiui EVENT_TTS get!");

					break;
				case AIUIConstant.EVENT_RESULT: {
					Log.d(TAG,"aiui Result get!");
					long posRsltOnArrival = System.currentTimeMillis();
					try {
						JSONObject bizParamJson = new JSONObject(event.info);
						JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
						JSONObject params = data.getJSONObject("params");
						JSONObject content = data.getJSONArray("content").getJSONObject(0);

						if (content.has("cnt_id")) {
							String cnt_id = content.getString("cnt_id");
							String cntStr = new String(event.data.getByteArray(cnt_id), "utf-8");

							// 获取该路会话的id，将其提供给支持人员，有助于问题排查
							// 也可以从Json结果中看到
							String sid = event.data.getString("sid");
							String tag = event.data.getString("tag");

							//showTip("tag=" + tag);

							// 获取从数据发送完到获取结果的耗时，单位：ms
							// 也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
							long eosRsltTime = event.data.getLong("eos_rslt", -1);
						//	txt_status.setText(eosRsltTime + "ms");

							if (TextUtils.isEmpty(cntStr)) {
								return;
							}

							JSONObject cntJson = new JSONObject(cntStr);

							if (mNlpText.getLineCount() > 1000) {
								mNlpText.setText("");
							}

							//mNlpText.append( "\n" );
							//mNlpText.append(cntJson.toString());
							//mNlpText.setSelection(mNlpText.getText().length());
							
							String sub = params.optString("sub");
							Log.d(TAG,"sub = :"+sub);
							if ("nlp".equals(sub)) {
								// 解析得到语义结果
								String resultStr = cntJson.optString("intent");
								JSONObject result = cntJson.getJSONObject("intent");
								long posRsltParseFinish = System.currentTimeMillis();
								//String res=new String(resultStr.getBytes(),"GBK");
								mNlpText.append( "\n" );
								mNlpText.append("NLP: "+resultStr);
								mNlpText.setSelection(mNlpText.getText().length());
								//语义解析
								Log.d( TAG, resultStr );
							}else if ("tts".equals(sub)) {
									/*
								    if (SenceManager.isMusicSence())
									{
										Log.e("Event Result","当前为音乐场景，不进行tts播报！");
										return;
									}else {
										Log.e("Event Result","当前不是音乐场景，开始tts播报！");
									}

									 sid = event.data.getString("sid");
									 cnt_id = content.getString("cnt_id");
									byte[] audio = event.data.getByteArray(cnt_id); //合成音频数据
									int dts = content.getInt("dts");
									int frameId = content.getInt("frame_id");// 音频段id，取值：1,2,3,...

									int percent = event.data.getInt("percent"); //合成进度

									boolean isCancel = "1".equals(content.getString("cancel"));  //合成过程中是否被取消
								    if(isCancel){
										myPcmPlayer.stopPlay();

									}else{
								    	setSystemVol(maxVolume);
										//Log.d(TAG,"get cloud TTS Data! frameId: "+frameId +"   percnet: "+percent+"%   isCnanel: "+isCancel);
										myPcmPlayer.play(audio);
									}
									*/
							}

						}
					} catch (Throwable e) {
						e.printStackTrace();
						//mNlpText.append( "\n" );
						//mNlpText.append( e.getLocalizedMessage());
					}
					
					//mNlpText.append( "\n" );
				} break;
	
				case AIUIConstant.EVENT_ERROR: {
					mNlpText.append( "\n" );
					mNlpText.append( "错误: " + event.arg1+"\n" + event.info );
				} break;

				case AIUIConstant.EVENT_SLEEP:{
					Log.d(TAG,"EVENT_SLEEP");
					setText("EVENT_SLEEP：  aiui 进入睡眠状态 ");
					isAsring=false;
					//caeOperator.restCaeEngine();
				}break;
	
				case AIUIConstant.EVENT_VAD: {
					if (AIUIConstant.VAD_BOS == event.arg1) {
						setText("找到vad_bos");
					} else if (AIUIConstant.VAD_EOS == event.arg1) {
						setText("找到vad_eos");
					} else {//录音音量
						//showTip("" + event.arg2);
					}
				} break;
				
				case AIUIConstant.EVENT_START_RECORD: {
					setText("已开始录音");
				} break;
				
				case AIUIConstant.EVENT_STOP_RECORD: {
					setText("已停止录音");
				} break;
	
				case AIUIConstant.EVENT_STATE: {	// 状态事件
					mAIUIState = event.arg1;
					
					if (AIUIConstant.STATE_IDLE == mAIUIState) {
						// 闲置状态，AIUI未开启
					//	showTip("STATE_IDLE");
						setText("STATE_IDLE "+mAIUIState );

					} else if (AIUIConstant.STATE_READY == mAIUIState) {
						// AIUI已就绪，等待唤醒
						//showTip("STATE_READY");
						setText("STATE_READY "+mAIUIState );
					} else if (AIUIConstant.STATE_WORKING == mAIUIState) {
						// AIUI工作中，可进行交互
						//showTip("STATE_WORKING");
						setText("STATE_WORKING "+mAIUIState );
					}
				} break;
				
				case AIUIConstant.EVENT_CMD_RETURN: {
					if (AIUIConstant.CMD_SYNC == event.arg1) {	// 数据同步的返回
						int dtype = event.data.getInt("sync_dtype", -1);
						int retCode = event.arg2;

						switch (dtype) {
							case AIUIConstant.SYNC_DATA_SCHEMA: {
								if (AIUIConstant.SUCCESS == retCode) {
									// 上传成功，记录上传会话的sid，以用于查询数据打包状态
									// 注：上传成功并不表示数据打包成功，打包成功与否应以同步状态查询结果为准，数据只有打包成功后才能正常使用
									mSyncSid = event.data.getString("sid");

									// 获取上传调用时设置的自定义tag
									String tag = event.data.getString("tag");

									// 获取上传调用耗时，单位：ms
									long timeSpent = event.data.getLong("time_spent", -1);
									if (-1 != timeSpent) {
								//		txt_status.setText(timeSpent + "ms");
									}

									showTip("上传成功，sid=" + mSyncSid + "，tag=" + tag + "，你可以试着说“打电话给刘德华”");
								} else {
									mSyncSid = "";
									showTip("上传失败，错误码：" + retCode);
								}
							} break;
						}
					} else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == event.arg1) {	// 数据同步状态查询的返回
						// 获取同步类型
						int syncType = event.data.getInt("sync_dtype", -1);
						if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
							// 若是同步数据查询，则获取查询结果，结果中error字段为0则表示上传数据打包成功，否则为错误码
							String result = event.data.getString("result");

							showTip(result);
						}
					}
				} break;
				
				default:
					break;
			}
		}

	};
 
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        if(caeOperator!=null){
        	caeOperator.stopRecord();
			caeOperator.releaseCae();
		}
    	if( null != mAIUIAgent ){
    		mAIUIAgent.destroy();
    		mAIUIAgent = null;
    	}
    }
	
	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	private void setText(final String str) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				/*if(caeOperator!=null) {
					mNlpText.append(" Cae Channel Num:  " + caeOperator.getChannelNum() + " \n" );
				}
				*/
				mNlpText.append(str + " \n");

			}
		});
	}
	private void ClearText(){

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mNlpText.setText("");
			}
		});
	}

	private OnCaeOperatorlistener onCaeOperatorlistener=new OnCaeOperatorlistener() {
		@Override
		public void onAudio(byte[] audioData, int dataLen) {
			if(isAsring&&mAIUIState==AIUIConstant.STATE_WORKING) {
				Log.d(TAG, "write audio " + audioData.length);
				mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, "data_type=audio,sample_rate=16000", audioData));
			}else{
				//Log.e(TAG,"未送入音频： mAIUIState ="+mAIUIState+"  isAsring： "+isAsring);
			}
		}

		@Override
		public void onWakeup(int angle ,int beam){
			isAsring = false;
			ClearText();
			AIUIMessage cancelTts = new AIUIMessage(AIUIConstant.CMD_TTS,AIUIConstant.CANCEL, 0, "", null);
			mAIUIAgent.sendMessage(cancelTts);
			setText("stop TTS!   ");

			//阵列唤醒事件回调，唤醒后需要发命令CMD_WAKEUP，通知AIUI 进入到Working 状态；
			AIUIMessage resetWakeupMsg = new AIUIMessage(
					AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
			mAIUIAgent.sendMessage(resetWakeupMsg);
			setText("角度："+angle+"   波束："+beam);
			//Log.e(TAG,mAIUIState+"触发唤醒事件");
			mMainHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isAsring = true;
				}
			},100);

		}
	};



	private void initCaeEngine(){
		Log.d(TAG, "initCaeEngine");
		if (null == caeOperator) {
			caeOperator = new CaeOperator();
			caeOperator.initCAEInstance(this, onCaeOperatorlistener);
			Log.d(TAG, "initCaeEngine");
			setText("------------ Ent：初始化阵列cae引擎成功！");
		}else{
			Log.d(TAG, "initCaeEngine is Init Done!");

		}

	}
	
	private void distoryCaeEngine(){
		if (null != caeOperator) {
			caeOperator.stopRecord();
			caeOperator.releaseCae();
			setText("------------  Exit：退出，释放阵列cae引擎  ------------ ");
		}else{
			Log.d(TAG, "distoryCaeEngine is Done!");

		}
		caeOperator = null;
	}

}
