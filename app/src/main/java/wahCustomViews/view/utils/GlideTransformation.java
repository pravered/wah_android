package wahCustomViews.view.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * Created by wah on 26/8/15.
 */
public class GlideTransformation implements Transformation<Bitmap> {
    private BitmapPool mBitmapPool;
    private int borderWidth;
    private Paint paintBorder;
    private int cornerRadius;

    public GlideTransformation(BitmapPool pool, int borderWidth, int borderColor, int cornerRadius) {
        this.mBitmapPool = pool;
        this.borderWidth = borderWidth;
        this.cornerRadius = cornerRadius;

        paintBorder =new Paint();
        paintBorder.setAntiAlias(true);
        paintBorder.setColor(borderColor);
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();
        Bitmap bitmap = null;
        if(cornerRadius >0){
            int width = source.getWidth();
            int height = source.getHeight();

            bitmap = mBitmapPool.get(width, height, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(0, 0, width, height),
                    cornerRadius, cornerRadius, paint);
        }
        else {
            int canvasSize = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - canvasSize) / 2;
            int height = (source.getHeight() - canvasSize) / 2;

            bitmap = mBitmapPool.get(canvasSize, canvasSize, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(source, BitmapShader.TileMode.CLAMP,
                    BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - 4.0f, paintBorder);
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) - 4.0f, paint);
        }

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    @Override
    public String getId() {
        return "GlideTransformation()";
    }
}
