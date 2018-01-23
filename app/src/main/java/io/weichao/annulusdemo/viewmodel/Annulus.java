package io.weichao.annulusdemo.viewmodel;

import android.content.Context;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import io.weichao.annulusdemo.util.GLES30Util;
import io.weichao.annulusdemo.util.MatrixStateUtil;

/**
 * Created by WeiChao on 2016/8/5.
 */
public class Annulus {
    private FloatBuffer mPositionBuffer;//顶点坐标数据缓冲
    private int mVertexCount;

    private int mProgram;//自定义渲染管线程序id
    private int muMVPMatrixHandle;//总变换矩阵引用
    private int muMMatrixHandle;//位置、旋转变换矩阵
    private int maCameraHandle; //摄像机位置属性引用
    private int maPositionHandle; //顶点位置属性引用
    private int maColorHandle;
    private int maNormalHandle; //顶点法向量属性引用
    private int maSunLightLocationHandle;//光源位置属性引用

    public Annulus(Context context, float rx, float ry, float rz, float ri, int splitCount) {
        //初始化顶点数据
        initVertexData(rx, ry, rz, ri, splitCount);
        //初始化着色器
        initScript(context);
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData(float rx, float ry, float rz, float ri, int splitCount) {
        int span = 10;
        try {
            span = 180 / splitCount;
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Float> alVertix = new ArrayList<>();
        //将球进行单位切分的角度，纵向、横向angleSpan度一份
        for (float fi = -180; fi < 180; fi = fi + span) {
            for (float theta = -180; theta < 180; theta = theta + span) {
                //纵向、横向各到一个角度后，计算对应的此点在球面上的坐标
                float x1 = (float) (rx * (ri + Math.cos(Math.toRadians(fi))) * Math.cos(Math.toRadians(theta)));
                float y1 = (float) (ry * (ri + Math.cos(Math.toRadians(fi))) * Math.sin(Math.toRadians(theta)));
                float z1 = (float) (rz * Math.sin(Math.toRadians(fi)));

                float x2 = (float) (rx * (ri + Math.cos(Math.toRadians(fi))) * Math.cos(Math.toRadians(theta + span)));
                float y2 = (float) (ry * (ri + Math.cos(Math.toRadians(fi))) * Math.sin(Math.toRadians(theta + span)));
                float z2 = (float) (rz * Math.sin(Math.toRadians(fi)));

                float x3 = (float) (rx * (ri + Math.cos(Math.toRadians(fi + span))) * Math.cos(Math.toRadians(theta)));
                float y3 = (float) (ry * (ri + Math.cos(Math.toRadians(fi + span))) * Math.sin(Math.toRadians(theta)));
                float z3 = (float) (rz * Math.sin(Math.toRadians(fi + span)));

                float x4 = (float) (rx * (ri + Math.cos(Math.toRadians(fi + span))) * Math.cos(Math.toRadians(theta + span)));
                float y4 = (float) (ry * (ri + Math.cos(Math.toRadians(fi + span))) * Math.sin(Math.toRadians(theta + span)));
                float z4 = (float) (rz * Math.sin(Math.toRadians(fi + span)));

                //构建第1个三角形
                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                //构建第2个三角形
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x4);
                alVertix.add(y4);
                alVertix.add(z4);
            }
        }
        //顶点的数量为坐标值数量的1/3，因为一个顶点有3个坐标
        mVertexCount = alVertix.size() / 3;

        float[] positionArray = new float[alVertix.size()];
        for (int i = 0; i < alVertix.size(); i++) {
            positionArray[i] = alVertix.get(i);
        }
        mPositionBuffer = ByteBuffer.allocateDirect(positionArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPositionBuffer.put(positionArray).position(0);
    }

    /**
     * 初始化着色器
     *
     * @param context
     */
    public void initScript(Context context) {
        mProgram = GLES30Util.loadProgram(context, "model/annulus/script/vertex_shader.sh", "model/annulus/script/fragment_shader.sh");
        //获取程序中顶点位置属性引用
        maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        //
        maColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor");
        //获取程序中顶点法向量属性引用
        maNormalHandle = GLES30.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取程序中摄像机位置引用
        maCameraHandle = GLES30.glGetUniformLocation(mProgram, "uCamera");
        //获取程序中光源位置引用
        maSunLightLocationHandle = GLES30.glGetUniformLocation(mProgram, "uLightLocationSun");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMMatrix");
    }

    public void draw() {
        //指定使用某套着色器程序（必须每次都指定）
        GLES30.glUseProgram(mProgram);

        //将最终变换矩阵传入渲染管线
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixStateUtil.getFinalMatrix(), 0);
        //将位置、旋转变换矩阵传入渲染管线
        GLES30.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixStateUtil.getMMatrix(), 0);
        // TODO 下面这句有时报错：java.lang.IllegalArgumentException: remaining() < count*3 < needed
        //将摄像机位置传入渲染管线
//        GLES30.glUniform3fv(maCameraHandle, 1, MatrixStateUtil.cameraFB);
        //将光源位置传入渲染管线
        GLES30.glUniform3fv(maSunLightLocationHandle, 1, MatrixStateUtil.lightPositionFBSun);

        //将顶点位置数据送入渲染管线（必须每次都指定）
        GLES30.glVertexAttribPointer(maPositionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, mPositionBuffer);
        //
        GLES30.glVertexAttrib4f(maColorHandle, 0.0f, 1.0f, 0.0f, 1.0f);
        //将顶点法向量数据送入渲染管线（必须每次都指定）
        GLES30.glVertexAttribPointer(maNormalHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, mPositionBuffer);
        //启用顶点位置数据数组
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        //启用顶点法向量数据数组
        GLES30.glEnableVertexAttribArray(maNormalHandle);

        //绘制三角形线
//        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, mVertexCount);
        //绘制图形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVertexCount);
    }
}
