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
    private int height;
    private int width;

    public WaveView(Context context, @Nullable WaveFile waveFile) {
        super(context);

        this.holder = getHolder();
        this.wavefile = waveFile;
        getHolder().addCallback(this);
        setWillNotDraw(false);
        paint = setPaintParams();
        path = new Path();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startDrawingWavFile(wavefile);
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        holder.removeCallback(this);
    }

    void startDrawingWavFile(WaveFile wavefile) {
        if (wavefile != null) {
            final Canvas canvas = holder.lockCanvas(null);
            disposable = wavefile.getAmplitudes(width).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNext, this::onError, () -> onComplete(canvas));
        }
    }

    private void onNext(Float val) {
        final float amplitude = normaliseAmplitude(val, height);
        addLinePoint(path, amplitude, height, xOffset);
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

    private Paint setPaintParams() {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2.0f);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));
        paint.setAntiAlias(false);
        return paint;
    }

    private void addLinePoint(Path path, float amplitude, int height, int xOffset) {
        final float yOffset = (height / 2);
        if(xOffset == 0){
            path.moveTo((float) xOffset, yOffset);
        }
        path.lineTo((float) xOffset, yOffset - amplitude);
    }
}
