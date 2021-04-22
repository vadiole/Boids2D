package vadiole.boids2d.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import kotlin.random.Random;
import vadiole.boids2d.Config;
import vadiole.boids2d.global.extensions.IntExtensionsKt;

public class FireworksOverlay extends View {

    private static final Paint[] paint;
    private final RectF rect = new RectF();
    private long lastUpdateTime;
    private boolean started;
    private boolean startedFall;
    private float speedCoef = 1.0f;
    private int fallingDownCount;
    private static final int particlesCount = Config.INSTANCE.getDevicePerformance().getFireworksParticlesCount();
    private static final int fallParticlesCount = Config.INSTANCE.getDevicePerformance().getFireworksFallParticlesCount();

    private static final int[] colors = new int[]{
            0xffee1111,
            0xffffc40d,
            0xff2d89ef,
            0xffff0097,
            0xff603cba,
            0xff00a300
    };


    static {
        paint = new Paint[colors.length];
        for (int a = 0; a < paint.length; a++) {
            paint[a] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint[a].setColor(colors[a]);
        }
    }

    private class Particle {
        byte type;
        byte colorType;
        byte side;
        byte typeSize;
        byte xFinished;
        byte finishedStart;

        float x;
        float y;
        short rotation;
        float moveX;
        float moveY;

        private void draw(Canvas canvas) {
            if (type == 0) {
                canvas.drawCircle(x, y, IntExtensionsKt.getToPx(typeSize), paint[colorType]);
            } else if (type == 1) {
                rect.set(x - IntExtensionsKt.getToPx(typeSize), y - IntExtensionsKt.getToPx(2), x + IntExtensionsKt.getToPx(typeSize), y + IntExtensionsKt.getToPx(2));
                canvas.save();
                canvas.rotate(rotation, rect.centerX(), rect.centerY());
                canvas.drawRoundRect(rect, IntExtensionsKt.getToPx(2), IntExtensionsKt.getToPx(2), paint[colorType]);
                canvas.restore();
            }
        }

        private boolean update(int dt) {
            float moveCoef = dt / 16.0f;
            x += moveX * moveCoef;
            y += moveY * moveCoef;
            if (xFinished != 0) {
                float dp = IntExtensionsKt.getToPx(1) * 0.5f;
                if (xFinished == 1) {
                    moveX += dp * moveCoef * 0.05f;
                    if (moveX >= dp) {
                        xFinished = 2;
                    }
                } else {
                    moveX -= dp * moveCoef * 0.05f;
                    if (moveX <= -dp) {
                        xFinished = 1;
                    }
                }
            } else {
                if (side == 0) {
                    if (moveX > 0) {
                        moveX -= moveCoef * 0.05f;
                        if (moveX <= 0) {
                            moveX = 0;
                            xFinished = finishedStart;
                        }
                    }
                } else {
                    if (moveX < 0) {
                        moveX += moveCoef * 0.05f;
                        if (moveX >= 0) {
                            moveX = 0;
                            xFinished = finishedStart;
                        }
                    }
                }
            }
            float yEdge = -IntExtensionsKt.getToPx(1) / 2.0f;
            boolean wasNegative = moveY < yEdge;
            if (moveY > yEdge) {
                moveY += IntExtensionsKt.getToPx(1) / 3.0f * moveCoef * speedCoef;
            } else {
                moveY += IntExtensionsKt.getToPx(1) / 3.0f * moveCoef;
            }
            if (wasNegative && moveY > yEdge) {
                fallingDownCount++;
            }
            if (type == 1 || type == 2) {
                rotation += moveCoef * 10;
                if (rotation > 360) {
                    rotation -= 360;
                }
            }
            return y >= getMeasuredHeight();
        }
    }

    private final ArrayList<Particle> particles = new ArrayList<>(particlesCount + fallParticlesCount);

    public FireworksOverlay(Context context) {
        super(context);
    }

    public FireworksOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FireworksOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FireworksOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Particle createParticle(boolean fall) {
        Particle particle = new Particle();
        particle.type = (byte) Random.Default.nextInt(2);
        particle.colorType = (byte) Random.Default.nextInt(colors.length);
        particle.side = (byte) Random.Default.nextInt(2);
        particle.finishedStart = (byte) (1 + Random.Default.nextInt(2));
        if (particle.type == 0 || particle.type == 2) {
            particle.typeSize = (byte) (4 + Random.Default.nextFloat() * 2);
        } else {
            particle.typeSize = (byte) (4 + Random.Default.nextFloat() * 4);
        }
        if (fall) {
            particle.y = -Random.Default.nextFloat() * getMeasuredHeight() * 1.2f;
            particle.x = IntExtensionsKt.getToPx(5) + Random.Default.nextInt(getMeasuredWidth() - IntExtensionsKt.getToPx(10));
            particle.xFinished = particle.finishedStart;
        } else {
            int xOffset = IntExtensionsKt.getToPx(4 + Random.Default.nextInt(10));
            int yOffset = getMeasuredHeight() / 4;
            if (particle.side == 0) {
                particle.x = -xOffset;
            } else {
                particle.x = getMeasuredWidth() + xOffset;
            }
            particle.moveX = (particle.side == 0 ? 1 : -1) * (IntExtensionsKt.getToPx(12) / 10f + Random.Default.nextFloat() * IntExtensionsKt.getToPx(4));
            particle.moveY = -(IntExtensionsKt.getToPx(4) + Random.Default.nextFloat() * IntExtensionsKt.getToPx(4));
            particle.y = yOffset / 2f + Random.Default.nextInt(yOffset * 2);
        }
        return particle;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        particles.clear();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        started = true;
        startedFall = false;
        fallingDownCount = 0;
        speedCoef = 1.0f;
        for (int a = 0; a < particlesCount; a++) {
            particles.add(createParticle(false));
        }
        invalidate();
    }

    private void startFall() {
        if (startedFall) {
            return;
        }
        startedFall = true;
        for (int a = 0; a < fallParticlesCount; a++) {
            particles.add(createParticle(true));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!started) {
           return;
        }
        long newTime = SystemClock.elapsedRealtime();
        int dt = (int) (newTime - lastUpdateTime);
        lastUpdateTime = newTime;
        if (dt > 18) {
            dt = 16;
        }
        for (int a = 0, N = particles.size(); a < N; a++) {
            Particle p = particles.get(a);
            p.draw(canvas);
            if (p.update(dt)) {
                particles.remove(a);
                a--;
                N--;
            }
        }
        if (fallingDownCount >= particlesCount / 2 && speedCoef > 0.2f) {
            startFall();
            speedCoef -= dt / 16.0f * 0.15f;
            if (speedCoef < 0.2f) {
                speedCoef = 0.2f;
            }
        }
        if (!particles.isEmpty()) {
            invalidate();
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            started = false;
        }
    }
}
