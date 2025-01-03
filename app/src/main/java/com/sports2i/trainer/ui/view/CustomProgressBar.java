package com.sports2i.trainer.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.sports2i.trainer.R;

public class CustomProgressBar extends ProgressBar {
    private Paint paint;

    public CustomProgressBar(Context context) {
        super(context);
        init();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.border_color)); // 선의 색상을 설정하세요.
        paint.setStrokeWidth(2); // 선의 두께를 설정하세요.
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // ProgressBar의 현재 상태
        int progress = getProgress();
        // ProgressBar의 최대 값
        int max = getMax();
        // ProgressBar의 너비
        int width = getWidth();
        // ProgressBar의 높이
        int height = getHeight();

        int paddingTop = 40;
        // 하단 padding
        int paddingBottom = 40;

        // 선의 간격 계산
        float interval = (float) width / 10;

        // 간격을 띄운 선 그리기
        for (int i = 1; i <= 9; i++) {
            float linePosition = interval * i;
            canvas.drawLine(linePosition, paddingTop, linePosition, height - paddingBottom, paint);
        }

        // 현재 프로그레스 위치 강조
//        float progressPosition = (float) progress / max * width;
//        paint.setColor(getResources().getColor(R.color.primary)); // 강조할 선의 색상을 설정하세요.
//        canvas.drawLine(progressPosition, 0, progressPosition, height, paint);
    }
}
