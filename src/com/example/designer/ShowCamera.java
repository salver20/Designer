package com.example.designer;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holdMe;
	private Camera theCamera;

	public ShowCamera(Context context, Camera camera) {
		super(context);
		theCamera = camera;
		holdMe = getHolder();
		holdMe.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			theCamera.setPreviewDisplay(holder);
			Camera.Parameters parameters = theCamera.getParameters();
			parameters.set("orientation", "portrait");
			theCamera.setDisplayOrientation(90);
			parameters.setRotation(90);

			int bestWidth = 0;
			int bestHeight = 0;
			List<Size> previewSizes = parameters.getSupportedPictureSizes();
			DisplayMetrics dm = getResources().getDisplayMetrics();
			int LW = (int) dm.xdpi;
			int LH = (int) dm.ydpi;
			for (Size s : previewSizes) {
				if (s.width > bestWidth && s.width <= LW
						&& s.height > bestHeight && s.height <= LH) {
					bestWidth = s.width;
					bestHeight = s.height;
				}
			}
			if (bestWidth != 0 && bestHeight != 0) {
				parameters.setPreviewSize(bestWidth, bestHeight);
				theCamera.setParameters(parameters);
			}
			theCamera.startPreview();
		} catch (Exception e) {
		}
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (theCamera != null) {
			theCamera.stopPreview();
			theCamera.release();
			theCamera = null;
		}
	}
}
