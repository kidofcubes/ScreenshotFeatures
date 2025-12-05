package io.github.kidofcubes.screenshotfeatures;

import org.joml.Matrix4f;

public class CameraChanges {
    public void test(){
//        new Matrix4f().setOrtho(
//                -width, width,
//                -height, height,
//                min, max
//        );

        new Matrix4f().setOrtho(
                -8, 8,
                -4.5f, 4.5f,
                -10, 10
        );

    }
}
