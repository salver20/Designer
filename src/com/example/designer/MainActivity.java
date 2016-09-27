package com.example.designer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String SAVE_PATH = Environment
			.getExternalStorageDirectory() + "/";
	private Camera cameraObject;
	private ShowCamera showcamera;
	private FrameLayout preview;
	// private Button snap;
	private ImageView girl;
	private ImageView pic;
	private File current = null;

	public static final int GET = 1;
	public static final int NOT_GET = 0;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET:
				Toast.makeText(getApplicationContext(), "GET",
						Toast.LENGTH_SHORT).show();
				break;
			case NOT_GET:
				Toast.makeText(getApplicationContext(), "NOT GET",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}
	};

	private PictureCallback captureIt = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());

			Bitmap bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.girl);

			Matrix matrix = new Matrix();
			matrix.setRotate(90, bmp.getWidth(), bmp.getHeight());
			Bitmap tempbmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);

			Matrix matrix2 = new Matrix();
			matrix2.postScale(((float) bm.getWidth()) / (tempbmp1.getWidth()),
					((float) bm.getHeight()) / (tempbmp1.getHeight()));
			Bitmap tempbmp2 = Bitmap.createBitmap(tempbmp1, 0, 0,
					tempbmp1.getWidth(), tempbmp1.getHeight(), matrix2, true);

			Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(),
					bmp.getConfig());
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(tempbmp2, new Matrix(), null);
			canvas.drawBitmap(bm, new Matrix(), null);
			bmp.recycle();
			bm.recycle();
			tempbmp1.recycle();
			tempbmp2.recycle();

			if (bitmap != null) {
				File appDir = new File(SAVE_PATH, "PIC");
				if (!appDir.exists()) {
					appDir.mkdir();
				}
				String name = "IMAGE_" + time + ".jpq";
				File file = new File(appDir, name);
				try {
					FileOutputStream fos = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
					fos.flush();
					fos.close();
					Message message = new Message();
					message.what = GET;
					handler.sendMessage(message);
					current = file;
				} catch (FileNotFoundException e) {
					Message message = new Message();
					message.what = NOT_GET;
					handler.sendMessage(message);
				} catch (IOException e) {
					Message message = new Message();
					message.what = NOT_GET;
					handler.sendMessage(message);
				}
				try {
					MediaStore.Images.Media.insertImage(
							MainActivity.this.getContentResolver(),
							file.getAbsolutePath(), name, null);
					Matrix matrix3 = new Matrix();
					matrix3.postScale(0.05f, 0.05f);
					Bitmap temp = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), matrix3,
							true);
					pic.setImageBitmap(temp);
				} catch (FileNotFoundException e) {
				}
				MainActivity.this.sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
								.parse("file://" + SAVE_PATH)));
				bitmap.recycle();
			}
			cameraObject.startPreview();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		cameraObject = isCameraAvailiable();
		showcamera = new ShowCamera(this, cameraObject);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(showcamera);
		// snap = (Button) findViewById(R.id.button_capture);
		girl = (ImageView) findViewById(R.id.girl);
		pic = (ImageView) findViewById(R.id.pic);

		girl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cameraObject.takePicture(null, null, captureIt);
			}
		});
		pic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);

				if (current != null) {
					intent.setDataAndType(Uri.parse("file://" + current),
							"image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivity(intent);
				}
			}
		});
	}

	public static Camera isCameraAvailiable() {
		Camera object = null;
		try {
			object = Camera.open();
		} catch (Exception e) {
		}
		return object;
	}

	protected void onStart() {
		super.onStart();
		Log.d("ddddddd", "start");
	}

	protected void onResume() {
		super.onResume();
		Log.d("ddddddd", "resume");
	}

	protected void onPause() {
		super.onPause();
		Log.d("ddddddd", "pause");
	}

	protected void onStop() {
		super.onStop();
		if (cameraObject != null) {
			cameraObject.release();
		}
		Log.d("ddddddd", "stop");
	}

	protected void onRestart() {
		cameraObject = isCameraAvailiable();
		showcamera = new ShowCamera(this, cameraObject);
		preview.addView(showcamera);
		super.onRestart();

		Log.d("ddddddd", "restart");
	}

	protected void onDestroy() {
		super.onDestroy();
		if (cameraObject != null) {
			cameraObject.release();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
