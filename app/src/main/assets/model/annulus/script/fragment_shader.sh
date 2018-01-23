#version 300 es
precision mediump float;//给出浮点默认精度
in vec4 vAmbient;
in vec4 vDiffuse;
in vec4 vSpecular;
in vec4 vColor;
out vec4 fragColor;
void main()
{  //月球着色器的main方法
  vec4 finalColor = vColor;
  //给此片元颜色值
  fragColor = finalColor*vAmbient+finalColor*vSpecular+finalColor*vDiffuse;
}