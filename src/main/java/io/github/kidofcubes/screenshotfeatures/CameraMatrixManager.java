package io.github.kidofcubes.screenshotfeatures;

import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes;
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
            if(!Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()){
                updateMatrix();
            }
        });
        Configs.CameraMatrix.MATRIX_WIDTH.setValueChangeCallback(config -> {
            if(!Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()){
                updateMatrix();
            }
        });
        Configs.CameraMatrix.MATRIX_HEIGHT.setValueChangeCallback(config -> {
            if(!Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()){
                updateMatrix();
            }
        });
        Configs.CameraMatrix.MATRIX_FAR.setValueChangeCallback(config -> {
            if(!Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()){
                updateMatrix();
            }
        });
        Configs.CameraMatrix.MATRIX_NEAR.setValueChangeCallback(config -> {
            if(!Configs.CameraMatrix.ALWAYS_APPLY_MATRIX.getBooleanValue()){
                updateMatrix();
            }
        });
    }

    public static double orthogonalWidth=10;
    public static double orthogonalHeight=10;
    public static double orthogonalNear=0.1;
    public static double orthogonalFar=10.0;

    private static void updateMatrix(){

        if((CameraMatrixManager.matrix.properties() | PROPERTY_PERSPECTIVE) > 0){
            if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.BOTH){
                CameraMatrixManager.setPerspectiveDimensions(
                        Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue(),
                        Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue(),
                        Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue()
                );
            }else if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.WIDTH){
                CameraMatrixManager.setPerspectiveDimensions(
                        Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue(),
                        Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue(),
                        -1
                );
            }else if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.HEIGHT){
                CameraMatrixManager.setPerspectiveDimensions(
                        Configs.CameraMatrix.MATRIX_PERSPECTIVE_SETTINGS_DISTANCE.getDoubleValue(),
                        -1,
                        Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue()
                );
            }
        }else if((CameraMatrixManager.matrix.properties() | PROPERTY_AFFINE) > 0){ //assuming its orthogonal
            if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.BOTH){
                orthogonalWidth = Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue();
                orthogonalHeight = Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue();
            }else if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.WIDTH){
                double origWidth = orthogonalWidth;
                orthogonalWidth = Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue();
                orthogonalHeight *= orthogonalWidth/origWidth;
            }else if(Configs.CameraMatrix.MATRIX_SETTINGS_APPLY.getOptionListValue()==ConfigTypes.MatrixSettingsApplyType.HEIGHT){
                double origHeight = orthogonalHeight;
                orthogonalHeight = Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue();
                orthogonalWidth *= orthogonalHeight/origHeight;
            }
            CameraMatrixManager.matrix.setOrtho(
                    -orthogonalWidth/2, orthogonalWidth/2,
                    -orthogonalHeight/2, orthogonalHeight/2,
                    orthogonalNear, orthogonalFar
            );
        }
    }
    public static void initPerspective(){
        CameraMatrixManager.matrix.setPerspective(Math.PI/2, Configs.CameraMatrix.MATRIX_WIDTH.getDoubleValue()/Configs.CameraMatrix.MATRIX_HEIGHT.getDoubleValue(), 0.1, 10);
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

    /// set width or height to -1 to scale same amount, set to -2 to skip
    public static void setPerspectiveDimensions(double dist, double desiredWidth, double desiredHeight){
        if((CameraMatrixManager.matrix.properties() | PROPERTY_PERSPECTIVE) > 0){
            Vector3d corner = CameraMatrixManager.matrixToView(new Vector3d(-1,-1,CameraMatrixManager.matrix.transformProject(new Vector3d(0,0,-dist)).z));
            double origWidth = (corner.x)*-2f;
            double widthMultiplier = (origWidth/desiredWidth); //suprisingly accurate???

            double origHeight = (corner.y)*-2f;
            double heightMultiplier = (origHeight/desiredHeight); //suprisingly accurate???
            if(desiredWidth>0 && desiredHeight>0){
                CameraMatrixManager.matrix.set(0,0,(widthMultiplier*CameraMatrixManager.matrix.get(0,0)));
                CameraMatrixManager.matrix.set(1,1,(heightMultiplier*CameraMatrixManager.matrix.get(1,1)));
            }else if(desiredWidth>0){
                CameraMatrixManager.matrix.set(0,0,(widthMultiplier*CameraMatrixManager.matrix.get(0,0)));
                if(desiredHeight==-1){
                    CameraMatrixManager.matrix.set(1,1,(widthMultiplier*CameraMatrixManager.matrix.get(1,1)));
                }
            }else if(desiredHeight>0){
                CameraMatrixManager.matrix.set(1,1,(heightMultiplier*CameraMatrixManager.matrix.get(1,1)));
                if(desiredWidth==-1){
                    CameraMatrixManager.matrix.set(0,0,(heightMultiplier*CameraMatrixManager.matrix.get(0,0)));
                }
            }
            CameraMatrixManager.matrix.determineProperties();
        }
    }



    public static Vector3d clamped(Vector3d vec){
        vec.x = Math.clamp(vec.x,-1.0f,1.0f);
        vec.y = Math.clamp(vec.y,-1.0f,1.0f);
        return vec;
    }

    public static Vector3d matrixToView(Vector3d vector3f){
        return CameraMatrixManager.matrix.invert(new Matrix4d()).transformProject(vector3f).mul(1.0f,1.0f,1.0f);
    }
    public void test(){
//        new Matrix4f().setOrtho(
//                -width, width,
//                -height, height,
//                min, max
//        );

    }
}
