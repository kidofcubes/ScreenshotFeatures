package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.joml.*;

import java.lang.Math;
import java.util.List;
import java.util.Map;

import static io.github.kidofcubes.screenshotfeatures.screens.ConfigsGui.createTabButtons;
import static java.lang.Math.*;

public class CameraMatrixEditorGui extends GuiBase {
    public static Matrix4f matrix = new Matrix4f().setOrtho(
            -10.0f, 10.0f,
            -10.0f, 10.0f,
            -10.0f, 10.0f
    );
    @Override
    public void init(){
        super.init();
        ConfigsGui.tab = ConfigsGui.ConfigGuiTab.CAMERA_MATRIX_EDITOR;

        title = StringUtils.translate("screenshotfeatures.gui.title.cameramatrixeditor");

        this.clearWidgets();
        this.clearButtons();
        createTabButtons(this,10,26);
        this.addWidget(new CameraMatrixEditorWidget(10,100,this.width-20,this.height-(10+100)));
    }
    public static class CameraMatrixEditorWidget extends WidgetBase {
        public Vector3f cameraPosition;
        public Quaternionf cameraRotation;
        public Vector3f cameraRotationEuler;
        public Matrix4f personalProjectionMatrix;
        public CameraMatrixEditorWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
//            this.matrix = new Matrix4f().setOrtho(
//                   -10.0f, 10.0f,
//                    -10.0f, 10.0f,
//                    -10.0f, 10.0f
//            );
//            this.matrix = new Matrix4f().setPerspective(
//                    (float)(TAU/4.0f),
//                1.0f,
//                0.005F,
//                1000.0f
//            );
            float fovDegrees = 90.0f;
            this.personalProjectionMatrix = new Matrix4f();
            this.personalProjectionMatrix.setPerspective(
                    fovDegrees * (float) (Math.PI / 180.0),
                    (float)width / height,
                    0.01F,
                   1000.0f
            );

            cameraPosition = new Vector3f(0.0f,0.0f,-(float)Math.pow(1.1f,44f));
            cameraRotation = new Quaternionf();
            cameraRotationEuler = new Vector3f();
        }

        @Override
        protected boolean onKeyTypedImpl(KeyInput input){
            if(input.getKeycode() == KeyCodes.KEY_TAB){
                if(!isShiftDown()){
                    this.index++;
                }else{
                    this.index+=points.size()-1;
                }
                return true;
            }

            return super.onKeyTypedImpl(input);
        }

        @Override
        public boolean onMouseScrolledImpl(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){
            if(verticalAmount != 0.0){
                cameraPosition.z *= (float)Math.pow(1.1,-verticalAmount);
                cameraPosition.z = -(float)clamp(-cameraPosition.z,Math.pow(1.1,-100), Math.pow(1.1,256));
                return true;
            }
            return super.onMouseScrolledImpl(mouseX,mouseY,horizontalAmount,verticalAmount);
        }
        private boolean dragging = false;
        private int prevMouseX = -1;
        private int prevMouseY = -1;
        private int index = 0;

        @Override
        protected boolean onMouseClickedImpl(Click click,boolean doubleClick){
            if(click.button() == 0){
                dragging = true;
                prevMouseX = -1;
                prevMouseY = -1;
                return true;
            }
            return super.onMouseClickedImpl(click,doubleClick);
        }

        @Override
        public void onMouseReleasedImpl(Click click){
            if(click.button() == 0){
                dragging = false;
            }
            super.onMouseReleasedImpl(click);
        }

        private List<Vector3f> points = List.of(
                new Vector3f(-1.0f,-1.0f,1.0f),
                new Vector3f(-1.0f,1.0f,1.0f),
                new Vector3f(1.0f,1.0f,1.0f),
                new Vector3f(1.0f,-1.0f,1.0f),
                new Vector3f(-1.0f,-1.0f,-1.0f),
                new Vector3f(-1.0f,1.0f,-1.0f),
                new Vector3f(1.0f,1.0f,-1.0f),
                new Vector3f(1.0f,-1.0f,-1.0f)
        );
        @Override
        public void render(DrawContext drawContext,int mouseX,int mouseY,boolean selected){
            super.render(drawContext,mouseX,mouseY,selected);
            this.drawContext.drawStrokedRectangle(x,y,width,height,Colors.WHITE);

            if(dragging){
                if(this.prevMouseX==-1){
                    this.prevMouseX = mouseX;
                    this.prevMouseY = mouseY;
                }
                this.cameraRotationEuler.x += (float)Math.toRadians(this.prevMouseY-mouseY);
                this.cameraRotationEuler.y += (float)Math.toRadians(mouseX-this.prevMouseX);
                this.cameraRotation = new Quaternionf().rotateXYZ(this.cameraRotationEuler.x,this.cameraRotationEuler.y,0.0f);

                this.prevMouseX = mouseX;
                this.prevMouseY = mouseY;
            }


            drawLine(drawContext,projectedToScreen(-1.0f,-1.0f,-1.0f),projectedToScreen(1.0f,-1.0f,-1.0f),0xFFFF0000);

            drawLine(drawContext,projectedToScreen(-1.0f,-1.0f,1.0f),projectedToScreen(1.0f,-1.0f,1.0f),0xFFCC0099);
            drawLine(drawContext,projectedToScreen(-1.0f,1.0f,-1.0f),projectedToScreen(1.0f,1.0f,-1.0f),0xFFCC9900);
            drawLine(drawContext,projectedToScreen(-1.0f,1.0f,1.0f),projectedToScreen(1.0f,1.0f,1.0f),0xFFCC9999);

            drawLine(drawContext,projectedToScreen(-1.0f,-1.0f,-1.0f),projectedToScreen(-1.0f,1.0f,-1.0f),0xFF00FF00);

            drawLine(drawContext,projectedToScreen(1.0f,-1.0f,-1.0f),projectedToScreen(1.0f,1.0f,-1.0f),0xFF99CC00);
            drawLine(drawContext,projectedToScreen(-1.0f,-1.0f,1.0f),projectedToScreen(-1.0f,1.0f,1.0f),0xFF00CC99);
            drawLine(drawContext,projectedToScreen(1.0f,-1.0f,1.0f),projectedToScreen(1.0f,1.0f,1.0f),0xFF99CC99);


            drawLine(drawContext,projectedToScreen(-1.0f,-1.0f,-1.0f),projectedToScreen(-1.0f,-1.0f,1.0f),0xFF0000FF);

            drawLine(drawContext,projectedToScreen(1.0f,-1.0f,-1.0f),projectedToScreen(1.0f,-1.0f,1.0f),0xFF9900CC);
            drawLine(drawContext,projectedToScreen(-1.0f,1.0f,-1.0f),projectedToScreen(-1.0f,1.0f,1.0f),0xFF0099CC);
            drawLine(drawContext,projectedToScreen(1.0f,1.0f,-1.0f),projectedToScreen(1.0f,1.0f,1.0f),0xFF9999CC);


            index = index % points.size();
            Vector3f point = points.get(index);
            Vector3f point0 = clamped(projectedToScreen(new Vector3f(point)));
            Vector3f viewSpacePoint = matrix.invert(new Matrix4f()).transformProject(new Vector3f(point));
            String name = switch(index){
                case 0 -> "Bottom Left Far";
                case 1 -> "Top Left Far";
                case 2 -> "Top Right Far";
                case 3 -> "Bottom Right Far";

                case 4 -> "Bottom Left Near";
                case 5 -> "Top Left Near";
                case 6 -> "Top Right Near";
                case 7 -> "Bottom Right Near";
                default -> "???";
            };
            drawContext.drawTooltip(textRenderer,
                    List.of(
//                            Text.of(String.format("%s x:%.2f y: %.2f z: %.2f (appr.)",name,viewSpacePoint.x,viewSpacePoint.y,viewSpacePoint.z))
                            Text.of(String.format("%s (pos appx.)",name)),
                            Text.of(String.format("x: %.10f",viewSpacePoint.x)),
                            Text.of(String.format("y: %.10f",viewSpacePoint.y)),
                            Text.of(String.format("z: %.10f",viewSpacePoint.z))
                    ),
                    sx(point0),sy(point0));
            drawContext.drawStrokedRectangle(sx(point0)-5,sy(point0)-5,10,10,Colors.WHITE);
        }

        private Vector3f clamped(Vector3f vec){
            vec.x = Math.clamp(vec.x,-1.0f,1.0f);
            vec.y = Math.clamp(vec.y,-1.0f,1.0f);
            return vec;
        }

        private Vector3f projectedToScreen(float x, float y, float z){
            return projectedToScreen(new Vector3f(x,y,z));
        }
        private Vector3f projectedToScreen(Vector3f vector3f){
            matrix.invert(new Matrix4f()).transformProject(vector3f);
            vector3f.z *= -1.0f;
            vector3f.x *= -1.0f;
            vector3f.rotate(cameraRotation);
            vector3f.add(cameraPosition.negate(new Vector3f()));

            if(vector3f.z<=0.01f){
                vector3f.z = 0.01f;
            }
            personalProjectionMatrix.transformProject(vector3f);
            return vector3f;
        }

        private int sx(Vector3f vector3f){
            return Math.round(vector3f.x*(width/2.0f)) + ((width/2) + x);
        }

        private int sy(Vector3f vector3f){
            return Math.round(vector3f.y*(height/2.0f)) + ((height/2) + y);
        }

        private void drawLine(DrawContext drawContext, Vector3f point0, Vector3f point1, int color){
            drawLine(drawContext,sx(point0),sy(point0),sx(point1),sy(point1),color);
        }
        private float clampMult(Vector2f v, Vector2f d){
            float num = 0.0f;
            if(d.x!=0.0f){
                if((x-v.x)>0.0f&&d.x>0.0f){
                    num+=(x-v.x)/d.x;
                }
                if(((width+x)-v.x)<0.0f&&d.x<0.0f){
                    num+=((width+x)-v.x)/d.x;
                }
            }
            v = d.mul(num, new Vector2f()).add(v);
            if(d.y!=0.0f){
                if((y-v.y)>0.0f&&d.y>0.0f){
                    num+=(y-v.y)/d.y;
                }
                if(((height+y)-v.y)<0.0f&&d.y<0.0f){
                    num+=((height+y)-v.y)/d.y;
                }
            }
            assert num>=0.0f;
            return num;
        }
        private void drawLine(DrawContext drawContext, int x0, int y0, int x1, int y1, int color){
//            x0 = Math.clamp(x0,0,width);
//            x1 = Math.clamp(x1,0,width);
//            y0 = Math.clamp(y0,0,height);
//            y1 = Math.clamp(y1,0,height);
            Vector2f direction = new Vector2f(x1-x0,y1-y0);
            Vector2f normalized = direction.normalize(new Vector2f());
            float minn = clampMult(new Vector2f(x0,y0),normalized);
            float maxx = direction.length()-clampMult(new Vector2f(x1,y1),normalized.negate(new Vector2f()));

            for(float mult=0;mult<=(maxx-minn);mult+=1.0f){
                Vector2f xy0 = normalized.mul(mult+minn, new Vector2f()).add(x0,y0);

                if(xy0.x < x || xy0.x > width+x || xy0.y < y || xy0.y > height+y){
                    break;
                }

                drawContext.fill((int)xy0.x,(int)xy0.y,(int)xy0.x+1,(int)xy0.y+1,color);
            }
        }
    }
}
