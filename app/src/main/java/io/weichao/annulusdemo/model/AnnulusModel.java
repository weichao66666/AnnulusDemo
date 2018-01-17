package io.weichao.annulusdemo.model;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.weichao.annulusdemo.util.GLES30Util;
import io.weichao.annulusdemo.view.GLES30AnnulusSV;

public class AnnulusModel implements IModel {
    private static final String TAG = "AnnulusModel";

    public RelativeLayout view;

    private GLES30AnnulusSV mGLES3AnnulusSV;

    public AnnulusModel(Activity activity) {
        view = new RelativeLayout(activity);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (GLES30Util.detectOpenGLES30(activity)) {
            mGLES3AnnulusSV = new GLES30AnnulusSV(activity);
        } else {
            Log.e(TAG, "OpenGL ES 3.0 not supported on device.  Exiting...");
        }
        view.addView(mGLES3AnnulusSV);
    }

    @Override
    public void onResume() {
        if (mGLES3AnnulusSV != null) {
            mGLES3AnnulusSV.setThreadRun(true);
            mGLES3AnnulusSV.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mGLES3AnnulusSV != null) {
            mGLES3AnnulusSV.setThreadRun(false);
            mGLES3AnnulusSV.onPause();
        }
    }

    @Override
    public void onDestroy() {
    }
}
