package io.weichao.annulusdemo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.weichao.annulusdemo.util.MatrixStateUtil;
import io.weichao.annulusdemo.viewmodel.Annulus;

/**
 * Created by admin on 2016/11/14.
 */
public class GLES30AnnulusSV extends GLSurfaceView {
    public final static int FRAME = 30;
    private final static long DELAY = (long) (1000.0f / FRAME);

    public final static int TIME_ROUND_ONE_CIRCLE = 5;//s
    private final static float RADIANS_PLUS = 360 / FRAME / TIME_ROUND_ONE_CIRCLE;

    private final static float X_TOUCH_SCALE_FACTOR = 0.5f;
    private final static float Y_TOUCH_SCALE_FACTOR = 0.5f;

    private Activity mActivity;

    private boolean isThreadRun;//旋转线程的工作标志位

    private float mXozAngle;//可改变光源照射位置
    private float mYozAngle;//摄像机绕X轴旋转的角度
    private float mRotAngle;//自转角度

    private float mPreviousX;//上次的触控位置X坐标
    private float mPreviousY;//上次的触控位置Y坐标

    public GLES30AnnulusSV(Activity activity) {
        super(activity);

        mActivity = activity;

        setEGLContextClientVersion(3);//设置使用OPENGL ES3.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        SceneRenderer mRenderer = new SceneRenderer(activity);//创建场景渲染器
        setRenderer(mRenderer);//设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为持续渲染
    }

    //触摸事件回调方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mDownX = (int) event.getX();
//                mDownY = (int) event.getY();
//                mOffsetX = 0;
//                mOffsetY = 0;
//                mDownTime = SystemClock.elapsedRealtime();
//                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
//                int offset = Math.abs(currentX - mDownX);
//                if (offset > mOffsetX) {
//                    mOffsetX = offset;
//                }
//                offset = Math.abs(currentY - mDownY);
//                if (offset > mOffsetY) {
//                    mOffsetY = offset;
//                }

                float dx = currentX - mPreviousX;//计算触控X位移
                float dy = currentY - mPreviousY;//计算触控Y位移

                //将X位移折算成绕Y轴旋转的角度
                mRotAngle += dx * X_TOUCH_SCALE_FACTOR;

                //将Y位移折算成绕X轴旋转的角度，触控纵向位移摄像机绕x轴旋转 -90～+90
                mYozAngle += dy * Y_TOUCH_SCALE_FACTOR;
                if (mYozAngle > 90) {
                    mYozAngle = 90;
                } else if (mYozAngle < -90) {
                    mYozAngle = -90;
                }
                float cy = (float) (7.2 * Math.sin(Math.toRadians(mYozAngle)));
                float cz = (float) (7.2 * Math.cos(Math.toRadians(mYozAngle)));
                float upy = (float) Math.cos(Math.toRadians(mYozAngle));
                float upz = -(float) Math.sin(Math.toRadians(mYozAngle));
                //改变绕X轴旋转角度
                MatrixStateUtil.setCamera(0, cy, cz, 0, 0, 0, 0, upy, upz);
//            case MotionEvent.ACTION_UP:
//                // TODO 持续渲染自动调用 ACTION_UP?
//                if (SystemClock.elapsedRealtime() - mDownTime < ConstantUtil.SINGLE_TAP_TIMEOUT && mOffsetX < (mScrollDistanceWidthLimit >> 4) && mOffsetY < (mScrollDistanceHeightLimit >> 4)) {
//                    mActivity.onSingleTap();
//                }
//                break;
        }
        mPreviousX = (int) event.getX();
        mPreviousY = (int) event.getY();
        return true;
    }

    public synchronized boolean isThreadRun() {
        return isThreadRun;
    }

    public synchronized void setThreadRun(boolean b) {
        isThreadRun = b;
    }

    private class SceneRenderer implements Renderer {
        private Context mContext;
        private Annulus mAnnulus;

        public SceneRenderer(Context context) {
            mContext = context;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置屏幕背景颜色RGBA
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            //打开深度检测
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            //设置剔除三角形的面
            GLES30.glCullFace(GLES30.GL_BACK);
            //启用剔除
            GLES30.glEnable(GLES30.GL_CULL_FACE);

            //创建对象
            mAnnulus = new Annulus(mContext, 0.2f, 0.2f, 0.2f, 2f, 20);

            //初始化变换矩阵
            MatrixStateUtil.setInitStack();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置
            GLES30.glViewport(0, 0, width, height);
            //计算GLSurfaeVIew的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            MatrixStateUtil.setProjectFrustum(-ratio, ratio, -1, 1, 4f, 100);

            //设置相机9参数
            float cy = (float) (7.2 * Math.sin(Math.toRadians(mYozAngle)));
            float cz = (float) (7.2 * Math.cos(Math.toRadians(mYozAngle)));
            float upy = (float) Math.cos(Math.toRadians(mYozAngle));
            float upz = -(float) Math.sin(Math.toRadians(mYozAngle));
            MatrixStateUtil.setCamera(0, cy, cz, 0, 0, 0, 0, upy, upz);

            //启动一个线程定时旋转
            new Thread() {
                public void run() {
                    while (isThreadRun()) {
                        mRotAngle += RADIANS_PLUS;
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            //清除屏幕深度缓冲与颜色缓冲
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

            //保护现场
            MatrixStateUtil.pushMatrix();

            //设置太阳灯光的位置
            float sunX = (float) (Math.sin(Math.toRadians(mRotAngle)) * 100);
            float sunZ = (float) (Math.cos(Math.toRadians(mRotAngle)) * 100);
            MatrixStateUtil.setLightLocationSun(sunX, 50, sunZ);

            //自转
            MatrixStateUtil.rotate(mRotAngle, 0, 1, 0);

            //绘制
            mAnnulus.draw();

            //恢复现场
            MatrixStateUtil.popMatrix();
        }
    }
}
