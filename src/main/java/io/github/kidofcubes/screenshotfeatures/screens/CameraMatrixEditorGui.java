package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.joml.*;

import java.lang.Math;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static io.github.kidofcubes.screenshotfeatures.CameraMatrixManager.*;
import static io.github.kidofcubes.screenshotfeatures.screens.ConfigsGui.createTabButtons;
import static java.lang.Math.*;
import static org.joml.Matrix4dc.PROPERTY_PERSPECTIVE;

public class CameraMatrixEditorGui extends GuiConfigsBase {

    public CameraMatrixEditorGui(){
        super(10,50,ScreenshotFeatures.MOD_ID,null,ScreenshotFeatures.MOD_ID+".gui.title.configs", String.format("%s", "version"));
    };

    private CameraMatrixEditorWidget widget;
    @Override
    public void init(){
        super.init();

        //disable searchbar taking keybinds
        try{
            Field field = WidgetListBase.class.getDeclaredField("widgetSearchBar");
            field.trySetAccessible();
            field.set(this.getListWidget(),  null);
        }catch (NoSuchFieldException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ConfigsGui.tab = ConfigsGui.ConfigGuiTab.CAMERA_MATRIX_EDITOR;

        title = StringUtils.translate("screenshotfeatures.gui.title.cameramatrixeditor");

        this.clearWidgets();
        this.clearButtons();
        createTabButtons(this,10,26);
        widget = new CameraMatrixEditorWidget(375,75,this.width-405,this.height-(10+75));
        this.addWidget(widget);


        this.addButton(new ButtonGeneric(375,75,100, false, "screenshotfeatures.gui.cameramatrix.applyWidth"), (button,mouseButton) -> {
            updateMatrix(true,false,true);
        });

        this.addButton(new ButtonGeneric(500,75,100, false, "screenshotfeatures.gui.cameramatrix.applyHeight"), (button,mouseButton) -> {
            updateMatrix(false,true,true);
        });


        this.addButton(new ButtonGeneric(375,100,100, false, "screenshotfeatures.gui.cameramatrix.applyOnlyWidth"), (button,mouseButton) -> {
            updateMatrix(true,false,false);
        });

        this.addButton(new ButtonGeneric(500,100,100, false, "screenshotfeatures.gui.cameramatrix.applyOnlyHeight"), (button,mouseButton) -> {
            updateMatrix(false,true,false);
        });


        this.addButton(new ButtonGeneric(375,125,100, false, "screenshotfeatures.gui.cameramatrix.setOrthogonal"), (button,mouseButton) -> {
            CameraMatrixManager.initOrthogonal();
        });

        this.addButton(new ButtonGeneric(500,125,100, false, "screenshotfeatures.gui.cameramatrix.setPerspective"), (button,mouseButton) -> {
            CameraMatrixManager.initPerspective();
        });


        this.addButton(new ButtonGeneric(375,150,100, false, "screenshotfeatures.gui.cameramatrix.applyAspectRatioWidth"), (button,mouseButton) -> {
            System.out.println("RATIO IS "+((double)this.client.getWindow().getFramebufferHeight() / (double)this.client.getWindow().getFramebufferWidth()));
            skipResponse = true;
            Configs.CameraMatrix.MATRIX_HEIGHT.setDoubleValue(
                    Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue() *
                            ((double)this.client.getWindow().getFramebufferHeight() / this.client.getWindow().getFramebufferWidth())
            );
            skipResponse = false;
            dirtyConfig = true;
        });

        this.addButton(new ButtonGeneric(500,150,100, false, "screenshotfeatures.gui.cameramatrix.applyAspectRatioHeight"), (button,mouseButton) -> {
            System.out.println("RATIO IS "+((double)this.client.getWindow().getFramebufferWidth() / (double)this.client.getWindow().getFramebufferHeight()));
            skipResponse = true;
            Configs.CameraMatrix.MATRIX_WIDTH.setDoubleValue(
                    Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue() *
                            ((double)this.client.getWindow().getFramebufferWidth() / (double)this.client.getWindow().getFramebufferHeight())
            );
            skipResponse = false;
            dirtyConfig = true;
        });
    }

    int mouseX=0;
    int mouseY=0;
    @Override
    public void mouseMoved(double mouseX,double mouseY){
        super.mouseMoved(mouseX,mouseY);
        this.mouseX = (int)mouseX;
        this.mouseY = (int)mouseY;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs(){
        return ConfigOptionWrapper.createFor(Configs.CameraMatrix.OPTIONS);
    }

    @Override
    public void render(DrawContext drawContext,int mouseX,int mouseY,float partialTicks){
        if(dirtyConfig){
            this.reCreateListWidget();
            this.getListWidget().resize(width, height);
            dirtyConfig = false;
        }
        super.render(drawContext,mouseX,mouseY,partialTicks);
    }

    //todo editor for projection matrices and orth matrices

    public class CameraMatrixEditorWidget extends WidgetBase {
        public Vector3d cameraPosition;
        public Quaterniond cameraRotation;
        public Vector3d cameraRotationEuler;
        public Matrix4d personalProjectionMatrix;
        public CameraMatrixEditorWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
            double fovDegrees = 90.0f;
            this.personalProjectionMatrix = new Matrix4d();
            this.personalProjectionMatrix.setPerspective(
                    fovDegrees * (double) (Math.PI / 180.0),
                    (double)width / height,
                    0.00001F,
                   100000.0f
            );

            cameraPosition = new Vector3d(0.0f,0.0f,-(double)Math.pow(1.1f,44f));
            cameraRotation = new Quaterniond();
            cameraRotationEuler = new Vector3d();
        }


        @Override
        protected boolean onKeyTypedImpl(KeyInput input){
            if(input.getKeycode() == KeyCodes.KEY_TAB){
                if(!isShiftDown()){
                    this.index++;
                }else{
                    this.index--;
                }
                return true;
            }

            if(input.getKeycode() == KeyCodes.KEY_SPACE){
                this.drawTooltip = !this.drawTooltip;
                return true;
            }

            if(input.getKeycode() == KeyCodes.KEY_S){
                CameraMatrixManager.matrix = new Matrix4d().setPerspective((double)(PI*0.5),1,0.1f,8192.0f);
                double far = CameraMatrixManager.matrix.get(3,2)/(CameraMatrixManager.matrix.get(2,2)+1.0);
                System.out.println("FAR IS "+far);
                System.out.println("OTHER FAR IS "+matrixToView(new Vector3d(-1,-1,1)));
                return true;
            }

            if(input.getKeycode() == KeyCodes.KEY_A){
                if((CameraMatrixManager.matrix.properties() | PROPERTY_PERSPECTIVE) > 0){
//                    double far = (double)(matrix.get(3,2)/(matrix.get(2,2)+1.0));
//                    far = -matrixToView(new Vector3d(-1,-1,1)).z;
//                    double near = -matrixToView(new Vector3d(-1,-1,-1)).z;



//                    double origWidth = (matrixToView(new Vector3d(-1,-1,1)).x)*-2f;
                    double origWidth = (matrixToView(new Vector3d(-1,-1,CameraMatrixManager.matrix.transformProject(new Vector3d(0,0,-(double)Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue())).z)).x)*-2f;

                    double multiplier = (double)((double)origWidth/(Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue())); //suprisingly accurate???
                    double origNum = CameraMatrixManager.matrix.get(0,0);
                    CameraMatrixManager.matrix.set(0,0,(multiplier*origNum));
//                    System.out.println("ORIG NUM: "+origNum+" NEW NUM: "+matrix.get(0,0));
//                    System.out.println("MATRIX: \n"+matrix);

                    CameraMatrixManager.matrix.determineProperties();
                }
//                matrix.set(2,2,matrix.get(2,2)*(isShiftDown()?0.5f:2.0f));
                return true;
            }

            if(input.getKeycode() == KeyCodes.KEY_D){
//                matrix.set(3,2,matrix.get(3,2)*(isShiftDown()?0.5f:2.0f));
                return true;
            }

            if(input.getKeycode() == KeyCodes.KEY_W){
                return true;
            }

            return super.onKeyTypedImpl(input);
        }
//
//        @Override
//        protected boolean onCharTypedImpl(CharInput input){
//            return super.onCharTypedImpl(input);
//        }

        @Override
        public boolean onMouseScrolledImpl(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){
            if(verticalAmount != 0.0){
                cameraPosition.z *= (double)Math.pow(1.1,-verticalAmount);
                cameraPosition.z = -(double)clamp(-cameraPosition.z,Math.pow(1.1,-100), Math.pow(1.1,256));
                return true;
            }
            return super.onMouseScrolledImpl(mouseX,mouseY,horizontalAmount,verticalAmount);
        }
        private boolean dragging = false;
        private boolean drawTooltip = true;
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

        private List<Vector3d> points = List.of(
                //far
                new Vector3d(-1.0f,-1.0f,1.0f),
                new Vector3d(-1.0f,1.0f,1.0f),
                new Vector3d(1.0f,1.0f,1.0f),
                new Vector3d(1.0f,-1.0f,1.0f),

                //near
                new Vector3d(-1.0f,-1.0f,-1.0f),
                new Vector3d(-1.0f,1.0f,-1.0f),
                new Vector3d(1.0f,1.0f,-1.0f),
                new Vector3d(1.0f,-1.0f,-1.0f)
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
                this.cameraRotationEuler.x += Math.toRadians(this.prevMouseY-mouseY);
                this.cameraRotationEuler.y += Math.toRadians(mouseX-this.prevMouseX);
                this.cameraRotation = new Quaterniond().rotateXYZ(this.cameraRotationEuler.x,this.cameraRotationEuler.y,0.0f);

                this.prevMouseX = mouseX;
                this.prevMouseY = mouseY;
            }


            drawLine(drawContext,matrixToClip(-1.0f,-1.0f,-1.0f),matrixToClip(1.0f,-1.0f,-1.0f),0xFFFF0000);

            drawLine(drawContext,matrixToClip(-1.0f,-1.0f,1.0f),matrixToClip(1.0f,-1.0f,1.0f),0xFFCC0099);
            drawLine(drawContext,matrixToClip(-1.0f,1.0f,-1.0f),matrixToClip(1.0f,1.0f,-1.0f),0xFFCC9900);
            drawLine(drawContext,matrixToClip(-1.0f,1.0f,1.0f),matrixToClip(1.0f,1.0f,1.0f),0xFFCC9999);

            drawLine(drawContext,matrixToClip(-1.0f,-1.0f,-1.0f),matrixToClip(-1.0f,1.0f,-1.0f),0xFF00FF00);

            drawLine(drawContext,matrixToClip(1.0f,-1.0f,-1.0f),matrixToClip(1.0f,1.0f,-1.0f),0xFF99CC00);
            drawLine(drawContext,matrixToClip(-1.0f,-1.0f,1.0f),matrixToClip(-1.0f,1.0f,1.0f),0xFF00CC99);
            drawLine(drawContext,matrixToClip(1.0f,-1.0f,1.0f),matrixToClip(1.0f,1.0f,1.0f),0xFF99CC99);


            drawLine(drawContext,matrixToClip(-1.0f,-1.0f,-1.0f),matrixToClip(-1.0f,-1.0f,1.0f),0xFF0000FF);

            drawLine(drawContext,matrixToClip(1.0f,-1.0f,-1.0f),matrixToClip(1.0f,-1.0f,1.0f),0xFF9900CC);
            drawLine(drawContext,matrixToClip(-1.0f,1.0f,-1.0f),matrixToClip(-1.0f,1.0f,1.0f),0xFF0099CC);
            drawLine(drawContext,matrixToClip(1.0f,1.0f,-1.0f),matrixToClip(1.0f,1.0f,1.0f),0xFF9999CC);


            List<Vector3d> allPoints = new ArrayList<>(points);
            double dist = CameraMatrixManager.matrix.transformProject(new Vector3d(0,0,-(double)Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue())).z;
            allPoints.add(new Vector3d(-1.0f,-1.0f,dist));
            allPoints.add(new Vector3d(-1.0f,1.0f,dist));
            allPoints.add(new Vector3d(1.0f,1.0f,dist));
            allPoints.add(new Vector3d(1.0f,-1.0f,dist));

            while(index<0){
                index+=allPoints.size();
            }
            index = index % allPoints.size();
            Vector3d point = allPoints.get(index);


            Vector3d point0 = matrixToClip(new Vector3d(point));
            point0.x = clamp(point0.x,-1.0f,1.0f);
            point0.y = clamp(point0.y,-1.0f,1.0f);
            Vector3d viewSpacePoint = matrixToView(new Vector3d(point));
            String name = switch(index){
                case 0  -> "Bottom Left Far";
                case 1  -> "Top Left Far";
                case 2  -> "Top Right Far";
                case 3  -> "Bottom Right Far";

                case 4  -> "Bottom Left Near";
                case 5  -> "Top Left Near";
                case 6  -> "Top Right Near";
                case 7  -> "Bottom Right Near";

                case 8  -> "Bottom Left Configured";
                case 9  -> "Top Left Configured";
                case 10 -> "Top Right Configured";
                case 11 -> "Bottom Right Configured";
                default -> "???";
            };
            if(drawTooltip){
                drawContext.drawTooltip(textRenderer,
                        List.of(
//                            Text.of(String.format("%s x:%.2f y: %.2f z: %.2f (appr.)",name,viewSpacePoint.x,viewSpacePoint.y,viewSpacePoint.z))
                                Text.of(String.format("%s (pos appx.)",name)),
                                Text.of(String.format("x: %.10f",viewSpacePoint.x)),
                                Text.of(String.format("y: %.10f",viewSpacePoint.y)),
                                Text.of(String.format("z: %.10f",viewSpacePoint.z))
                        ),
                        sx(point0),sy(point0));
            }
            drawContext.drawStrokedRectangle(sx(point0)-5,sy(point0)-5,10,10,Colors.WHITE);
            Vector3d sidePoint;

            double scale = cameraPosition.distance(viewSpacePoint)/5.0f;
            //not perfect but good enough

            sidePoint = viewToClip(new Vector3d(viewSpacePoint).add(scale,0.0f,0.0f));
            drawLine(drawContext,matrixToClip(new Vector3d(point)),sidePoint,0xFFFF0000, 2);
            drawContext.fill(sx(sidePoint)-8,sy(sidePoint)-1,sx(sidePoint)+8,sy(sidePoint)+9,0xAA000000);
            drawContext.drawCenteredTextWithShadow(textRenderer, "+X", sx(sidePoint),sy(sidePoint),0xFFFFFFFF);

            sidePoint = viewToClip(new Vector3d(viewSpacePoint).add(0.0f,scale,0.0f));
            drawLine(drawContext,matrixToClip(new Vector3d(point)),sidePoint,0xFF00FF00, 2);
            drawContext.fill(sx(sidePoint)-8,sy(sidePoint)-1,sx(sidePoint)+8,sy(sidePoint)+9,0xAA000000);
            drawContext.drawCenteredTextWithShadow(textRenderer, "+Y", sx(sidePoint),sy(sidePoint),0xFFFFFFFF);

            sidePoint = viewToClip(new Vector3d(viewSpacePoint).add(0.0f,0.0f,scale));
            drawLine(drawContext,matrixToClip(new Vector3d(point)),sidePoint,0xFF0000FF, 2);
            drawContext.fill(sx(sidePoint)-8,sy(sidePoint)-1,sx(sidePoint)+8,sy(sidePoint)+9,0xAA000000);
            drawContext.drawCenteredTextWithShadow(textRenderer, "+Z", sx(sidePoint),sy(sidePoint),0xFFFFFFFF);



        }



        private Vector3d matrixToClip(double x,double y,double z){
            return matrixToClip(new Vector3d(x,y,z));
        }
        private Vector3d matrixToClip(Vector3d vector3f){
            return viewToClip(matrixToView(vector3f));
        }
        private Vector3d viewToClip(double x,double y,double z){
            return viewToClip(new Vector3d(x,y,z));
        }
        private Vector3d viewToClip(Vector3d vector3f){
            vector3f.rotate(cameraRotation);
            vector3f.add(cameraPosition.negate(new Vector3d()));

            if(vector3f.z<=0.01f){
                vector3f.z = 0.01f;
            }
            personalProjectionMatrix.transformProject(vector3f);
            return vector3f;
        }


        private Vector2d clipToScreen(Vector3d vec){
            return new Vector2d(vec.x*(width/2.0f) + (width/2.0f) + (double)x,vec.y*(height/2.0f) + (height/2.0f) + (double)y);
        }

        private int sx(Vector3d vector3f){
            return (int)(Math.round(vector3f.x*(width/2.0f)) + ((width/2) + x));
        }

        private int sy(Vector3d vector3f){
            return (int)(Math.round(vector3f.y*(height/2.0f)) + ((height/2) + y));
        }

        private void drawLine(DrawContext drawContext, Vector3d point0, Vector3d point1, int color){
            drawLine(drawContext, point0, point1, color, 0.5f);
        }
        private void drawLine(DrawContext drawContext, Vector3d point0, Vector3d point1, int color, double radius){
            drawLine(drawContext,sx(point0),sy(point0),sx(point1),sy(point1),color,radius);
        }
        private double clampMult(Vector2d v, Vector2d d){
            double num = 0.0f;
            if(d.x!=0.0f){
                if((x-v.x)>0.0f&&d.x>0.0f){
                    num+=(x-v.x)/d.x;
                }
                if(((width+x)-v.x)<0.0f&&d.x<0.0f){
                    num+=((width+x)-v.x)/d.x;
                }
            }
            v = d.mul(num, new Vector2d()).add(v);
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
        private void drawLine(DrawContext drawContext, int x0, int y0, int x1, int y1, int color, double radius){
//            x0 = Math.clamp(x0,0,width);
//            x1 = Math.clamp(x1,0,width);
//            y0 = Math.clamp(y0,0,height);
//            y1 = Math.clamp(y1,0,height);
            Vector2d direction = new Vector2d(x1-x0,y1-y0);
            Vector2d normalized = direction.normalize(new Vector2d());
            double minn = clampMult(new Vector2d(x0,y0),normalized);
            double maxx = direction.length()-clampMult(new Vector2d(x1,y1),normalized.negate(new Vector2d()));

            for(double mult=0;mult<=(maxx-minn);mult+=1.0f){
                Vector2d xy0 = normalized.mul(mult+minn, new Vector2d()).add(x0,y0);

                if(xy0.x < x || xy0.x > width+x || xy0.y < y || xy0.y > height+y){
                    break;
                }

                drawContext.fill(
                        (int)Math.round(xy0.x-radius),
                        (int)Math.round(xy0.y-radius),
                        (int)Math.round(xy0.x+radius),
                        (int)Math.round(xy0.y+radius),
                        color);
            }
        }
    }
}
