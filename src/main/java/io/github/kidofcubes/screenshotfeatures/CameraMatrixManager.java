package io.github.kidofcubes.screenshotfeatures;

import io.github.kidofcubes.screenshotfeatures.config.Configs;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import static org.joml.Matrix4dc.*;

public class CameraMatrixManager {
    public static Matrix4d matrix = new Matrix4d().setOrtho(
            -10.0f, 10.0f,
            -10.0f, 10.0f,
            -10.0f, 10.0f
    );

    public static void register(){
        Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.setValueChangeCallback(config -> {
            if(Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()&&!skipResponse){
                updateMatrix(false, false, Configs.CameraMatrix.KEEP_ASPECT_RATIO.getBooleanValue());
            }
        });
        Configs.CameraMatrix.MATRIX_WIDTH.setValueChangeCallback(config -> {
            if(Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()&&!skipResponse){
                updateMatrix(true, false, Configs.CameraMatrix.KEEP_ASPECT_RATIO.getBooleanValue());
            }
        });
        Configs.CameraMatrix.MATRIX_HEIGHT.setValueChangeCallback(config -> {
            if(Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()&&!skipResponse){
                updateMatrix(false, true, Configs.CameraMatrix.KEEP_ASPECT_RATIO.getBooleanValue());
            }
        });
        Configs.CameraMatrix.MATRIX_FAR.setValueChangeCallback(config -> {
            if(Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()&&!skipResponse){
                updateMatrix(false, false, Configs.CameraMatrix.KEEP_ASPECT_RATIO.getBooleanValue());
            }
        });
        Configs.CameraMatrix.MATRIX_NEAR.setValueChangeCallback(config -> {
            if(Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()&&!skipResponse){
                updateMatrix(false, false, Configs.CameraMatrix.KEEP_ASPECT_RATIO.getBooleanValue());
            }
        });

        // because who wants this on when they open the game???
        Configs.CameraMatrix.OVERRIDE_MATRIX.setBooleanValue(false);
        initOrthogonal();
        updateMatrix(true,true,false);
    }

    public static double orthogonalWidth=10;
    public static double orthogonalHeight=10;
    public static double orthogonalNear=0.1;
    public static double orthogonalFar=10.0;
    public static boolean dirtyConfig = false;
    public static boolean skipResponse = false;

    public static void updateMatrix(boolean width,boolean height,boolean keepAspectRatio){

        if((CameraMatrixManager.matrix.properties() & PROPERTY_PERSPECTIVE) > 0){
            CameraMatrixManager.setPerspectiveDimensions(width,height,keepAspectRatio);
        }else if((CameraMatrixManager.matrix.properties() & PROPERTY_AFFINE) > 0){ //assuming its orthogonal
            orthogonalNear = Configs.CameraMatrix.MATRIX_NEAR.getDoubleValue();
            orthogonalFar = Configs.CameraMatrix.MATRIX_FAR.getDoubleValue();
            if(width&&height){
                orthogonalWidth = Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue();
                orthogonalHeight = Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue();
            }else if(width){
                double origWidth = orthogonalWidth;
                orthogonalWidth = Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue();
                if(keepAspectRatio){
                    orthogonalHeight *= orthogonalWidth/origWidth;
                    skipResponse=true;
                    Configs.CameraMatrix.MATRIX_HEIGHT.setDoubleValue(orthogonalHeight);
                    skipResponse=false;
                    dirtyConfig = true;
                }

            }else if(height){
                double origHeight = orthogonalHeight;
                orthogonalHeight = Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue();
                if(keepAspectRatio){
                    orthogonalWidth *= orthogonalHeight/origHeight;
                    skipResponse=true;
                    Configs.CameraMatrix.MATRIX_WIDTH.setDoubleValue(orthogonalWidth);
                    skipResponse=false;
                    dirtyConfig = true;
                }
            }
            CameraMatrixManager.matrix.setOrtho(
                    -orthogonalWidth/2, orthogonalWidth/2,
                    -orthogonalHeight/2, orthogonalHeight/2,
                    orthogonalNear, orthogonalFar
            );

        }
    }
    public static void initPerspective(){
//        CameraMatrixManager.matrix.setPerspective(Math.PI/2, Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue()/Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue(), 0.1, 10);
        double multiplier = (Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue()/Configs.CameraMatrix.MATRIX_NEAR.getDoubleValue());
        CameraMatrixManager.matrix.setPerspectiveRect(
                Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue()/(multiplier),
                Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue()/(multiplier),
                Configs.CameraMatrix.MATRIX_NEAR.getDoubleValue(),
                Configs.CameraMatrix.MATRIX_FAR.getDoubleValue()
        );
    }

    public static void initOrthogonal(){
        orthogonalWidth = Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue();
        orthogonalHeight = Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue();
        orthogonalNear = Configs.CameraMatrix.MATRIX_NEAR.getDoubleValue();
        orthogonalFar = Configs.CameraMatrix.MATRIX_FAR.getDoubleValue();
        CameraMatrixManager.matrix.setOrtho(
                -orthogonalWidth/2, orthogonalWidth/2,
                -orthogonalHeight/2, orthogonalHeight/2,
                orthogonalNear, orthogonalFar
        );
    }

    public static void setPerspectiveDimensions(boolean updateWidth, boolean updateHeight, boolean keepAspectRatio){
        if((CameraMatrixManager.matrix.properties() & PROPERTY_PERSPECTIVE) > 0){
            Vector3d corner = CameraMatrixManager.matrixToView(new Vector3d(-1,-1,CameraMatrixManager.matrix.transformProject(new Vector3d(0,0,-Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue())).z));
            double origWidth = (corner.x)*-2f;
            double widthMultiplier = (origWidth/Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue()); //suprisingly accurate???

            double origHeight = (corner.y)*-2f;
            double heightMultiplier = (origHeight/Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue());
            if(updateWidth&&updateHeight){
                CameraMatrixManager.matrix.set(0,0,(widthMultiplier*CameraMatrixManager.matrix.get(0,0)));
                CameraMatrixManager.matrix.set(1,1,(heightMultiplier*CameraMatrixManager.matrix.get(1,1)));
            }else if(updateWidth){
                CameraMatrixManager.matrix.set(0,0,(widthMultiplier*CameraMatrixManager.matrix.get(0,0)));
                if(keepAspectRatio){
                    CameraMatrixManager.matrix.set(1,1,(widthMultiplier*CameraMatrixManager.matrix.get(1,1)));
                    skipResponse=true;
                    Configs.CameraMatrix.MATRIX_HEIGHT.setDoubleValue(widthMultiplier*Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue());
                    skipResponse=false;
                    dirtyConfig=true;
                }
            }else if(updateHeight){
                CameraMatrixManager.matrix.set(1,1,(heightMultiplier*CameraMatrixManager.matrix.get(1,1)));
                if(keepAspectRatio){
                    CameraMatrixManager.matrix.set(0,0,(heightMultiplier*CameraMatrixManager.matrix.get(0,0)));
                    skipResponse=true;
                    Configs.CameraMatrix.MATRIX_WIDTH.setDoubleValue(heightMultiplier*Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue());
                    skipResponse=false;
                    dirtyConfig=true;
                }
            }
            CameraMatrixManager.matrix.determineProperties();
        }
    }


    public static Vector3d matrixToView(Vector3d vector3f){
        return CameraMatrixManager.matrix.invert(new Matrix4d()).transformProject(vector3f).mul(1.0f,1.0f,1.0f);
    }
}
