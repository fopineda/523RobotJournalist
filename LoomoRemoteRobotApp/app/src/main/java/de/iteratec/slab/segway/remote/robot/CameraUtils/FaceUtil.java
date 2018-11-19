package de.iteratec.slab.segway.remote.robot.CameraUtils;

import android.graphics.Rect;
import android.graphics.RectF;



// Moves and transforms the green recrangle. Might not need it, but if we do then it must not show. (NOT
public class FaceUtil {
    private static final String TAG = "FaceUtil";
    private static float FACE_Y_SCALE_FACTOR = 0.159f;
    private static float FACE_X_SCALE_FACTOR = 0.1f;
    private static float TRANSFORM_Y_FACTOR = 0.5f;
    private static float TRANSFORM_X_FACTOR = 0.1f;

    public static RectF transformRect(RectF rectF, final float viewWidth, final float viewHeight) {
        RectF nRect = new RectF();
        nRect.left = rectF.left - rectF.width() * FACE_X_SCALE_FACTOR;
        nRect.right = rectF.right + rectF.width() * FACE_X_SCALE_FACTOR;

        nRect.top = rectF.top - rectF.height() * FACE_Y_SCALE_FACTOR;
        nRect.bottom = rectF.bottom + rectF.height() * FACE_Y_SCALE_FACTOR;
        nRect.top = nRect.top - (viewHeight - (rectF.top + rectF.bottom)) / viewHeight * TRANSFORM_Y_FACTOR;
        nRect.bottom = nRect.bottom - (viewHeight - (rectF.top + rectF.bottom)) / viewHeight * TRANSFORM_Y_FACTOR;
        return nRect;
    }

    public static RectF calculateFaceRect(final Rect face, final int viewWidth, final int
            viewHeight, final float width, final float height) {
        RectF rectF = new RectF();
        rectF.left = viewWidth - viewWidth * (face.right / width);
        rectF.top = viewHeight * (face.top / height);
        rectF.right = viewWidth - viewWidth * (face.left / width);
        rectF.bottom = viewHeight * (face.bottom / height);
        return rectF;
    }

    public static RectF calculateFaceToFace(final Rect face, final int viewWidth, final int
            viewHeight, final float width, final float height) {
        float left = viewWidth - viewWidth * (face.right / width);
        float top = viewHeight * (face.top / height);
        float right = viewWidth - viewWidth * (face.left / width);
        float bottom = viewHeight * (face.bottom / height);
        return new RectF(left,top,right,bottom);
    }
}
