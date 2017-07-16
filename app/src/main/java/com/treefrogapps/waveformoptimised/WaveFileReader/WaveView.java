package com.treefrogapps.waveformoptimised.WaveFileReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.graphics.Color.BLUE;
import static android.graphics.Paint.Cap.ROUND;


@SuppressLint("ViewConstructor")
public class WaveView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private WaveFile wavefile;
    private Disposable disposable;
    private int xOffset;
    private Paint paint;
    private List<Float> wavePoints = new ArrayList<>();

    public WaveView(Context context, @Nullable WaveFile waveFile) {
        super(context);

        this.holder = getHolder();
        this.wavefile = waveFile;
        getHolder().addCallback(this);
        setWillNotDraw(false);
        paint = new Paint();
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
            disposable = wavefile.getAmplitudes(canvas.getWidth()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(val -> onNext(val, canvas), this::onError, () -> onComplete(canvas));
        }
    }

    private void onNext(Float val, Canvas canvas) {
        final float amplitude = normaliseAmplitude(val, canvas.getHeight());
        wavePoints.addAll(addLinePoint(wavePoints, amplitude, canvas, xOffset));
        xOffset++;
    }

    private void onError(Throwable e) {
        e.printStackTrace();
    }

    private void onComplete(Canvas canvas) {
        Log.i(getClass().getSimpleName(), "Drawing complete");
        canvas.drawLines(getFloatPoints(wavePoints), paint);
        holder.unlockCanvasAndPost(canvas);
        xOffset = 0;
        wavePoints.clear();
    }

    private float[] getFloatPoints(List<Float> wavePoints) {
        final float[] points = new float[wavePoints.size()];
        for (int i = 0; i < wavePoints.size(); i++) {
            points[i] = wavePoints.get(i);
        }
        return points;
    }

    private float normaliseAmplitude(Float val, int y) {
        return val * (y / 2);
    }

    private void setPaintParams(Paint paint) {
        paint.setColor(BLUE);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(2.0F);
        paint.setStrokeCap(ROUND);
    }

    private List<Float> addLinePoint(List<Float> wavePoints, float amplitude, Canvas canvas, int xOffset) {
        final ArrayList<Float> newLine = new ArrayList<>();
        if (wavePoints.size() > 2) {
            newLine.add(wavePoints.get(wavePoints.size() - 2));
            newLine.add(wavePoints.get(wavePoints.size() - 1));
        }
        final float yOffset = (canvas.getHeight() / 2);
        newLine.add((float) xOffset);
        newLine.add(yOffset - amplitude);
        return newLine;
    }

}
