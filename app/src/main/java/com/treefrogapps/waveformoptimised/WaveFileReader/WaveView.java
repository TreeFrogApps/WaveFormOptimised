package com.treefrogapps.waveformoptimised.WaveFileReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


@SuppressLint("ViewConstructor")
public class WaveView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private WaveFile wavefile;
    private Disposable disposable;
    private int xOffset;
    private Paint paint;
    private Path path;

    public WaveView(Context context, @Nullable WaveFile waveFile) {
        super(context);

        this.holder = getHolder();
        this.wavefile = waveFile;
        getHolder().addCallback(this);
        setWillNotDraw(false);
        paint = new Paint();
        path = new Path();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startDrawingWavFile(this.wavefile);
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    void startDrawingWavFile(WaveFile wavefile) {
        if (wavefile != null) {
            final Canvas canvas = holder.lockCanvas(null);
            setPaintParams(paint);
            Log.e(getClass().getSimpleName(), " " + canvas.getWidth() + " , " + canvas.getHeight());
            disposable = wavefile.getAmplitudes(canvas.getWidth()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(val -> onNext(val, canvas), this::onError, () -> onComplete(canvas));
        }
    }

    private void onNext(Float val, Canvas canvas) {
        final float amplitude = normaliseAmplitude(val, canvas.getHeight());
        addLinePoint(path, amplitude, canvas.getHeight(), xOffset);
        xOffset++;
    }

    private void onError(Throwable e) {
        e.printStackTrace();
    }

    private void onComplete(Canvas canvas) {
        Log.i(getClass().getSimpleName(), "Drawing complete");
        canvas.drawPath(path, paint);
        holder.unlockCanvasAndPost(canvas);
        xOffset = 0;
    }

    private float normaliseAmplitude(Float val, int y) {
        return val * (y / 2);
    }

    private void setPaintParams(Paint paint) {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2.0f);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));
        paint.setAntiAlias(true);
    }

    private void addLinePoint(Path path, float amplitude, int height, int xOffset) {
        final float yOffset = (height / 2);
        path.lineTo((float) xOffset, yOffset - amplitude);
        path.moveTo((float) xOffset, yOffset - amplitude);
    }

}
