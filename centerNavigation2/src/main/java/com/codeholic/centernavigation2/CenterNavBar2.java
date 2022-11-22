package com.codeholic.centernavigation2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CenterNavBar2 extends View {

    private static final int STATUS_MENU_OPENED = 1 << 1;

    private static final int STATUS_MENU_CLOSED = 1 << 4;

    private static final int MAX_SUBMENU_NUM = 7;

    private int pressedIndex =0;

    private int newFocusedIndex;

    private boolean isIconFocused = false;

    private  ValueAnimator animator;

    private Canvas canvas;

    private int mainMenuPartSize;

    private int subMenuPartSize;

    private int iconSize;

    private int itemNum;

    private float fraction;

    private int mainMenuColor;

    private Drawable openMenuIcon, closeMenuIcon;

    private List<Drawable> subMenuDrawableList;

    private List<Integer> menuResList;

    private List<RectF> menuRectFList;

    private int status;

    private boolean pressed;

    private Paint oPaint, sPaint;

    private float arcAlphaValue;

    private float focusedAlphaValue=255;

    private float miniArcSize;

    private float centerX;

    private OnMenuSelectedListener onMenuSelectedListener;

    private OnMenuStatusChangedListener onMenuStatusChangeListener;

    public CenterNavBar2(Context context) {
        this(context, null);
    }

    public CenterNavBar2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CenterNavBar2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        status = STATUS_MENU_CLOSED;
        init();
    }

    private void init() {
        initTool();

        mainMenuColor = Color.parseColor("#613eea");

        openMenuIcon = new GradientDrawable();
        closeMenuIcon = new GradientDrawable();

        subMenuDrawableList = new ArrayList<>();
        menuRectFList = new ArrayList<>();

        menuResList = new ArrayList<>();

    }

    private void initTool() {
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int measureWidthSize = width, measureHeightSize = height;

        if (widthMode == MeasureSpec.AT_MOST) {
            measureWidthSize = dip2px(20) * 14 ;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            measureHeightSize = dip2px(20) * 10;
        }
        setMeasuredDimension(measureWidthSize, measureHeightSize);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(getMeasuredWidth(), getMeasuredHeight());

        mainMenuPartSize =(int) (minSize / 4);
        iconSize =(int) (mainMenuPartSize / 3.5) ;

        subMenuPartSize = (int) (minSize / 1.5);

        centerX = getMeasuredWidth() / 2;

        resetMainDrawableBounds();

        RectF mainMenuRectF = new RectF(centerX - mainMenuPartSize, getMeasuredHeight() - mainMenuPartSize, centerX + mainMenuPartSize, getMeasuredHeight() +mainMenuPartSize);
        menuRectFList.add(mainMenuRectF);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        this.canvas = canvas;

        switch (status) {
            case STATUS_MENU_CLOSED:
                drawMainMenu(canvas);
                break;
            case STATUS_MENU_OPENED:
                drawSubMenu(canvas);
                drawMainMenu(canvas);
                break;
        }
    }


    private void drawSubMenu(Canvas canvas) {

        int centerX=0, centerY=0, sweepAngle = 185/itemNum,angle=0;
        int startAngle = 270 +(180/itemNum) / 2;
        RectF menuRectF;


        RectF arc = new RectF(this.centerX - subMenuPartSize, getMeasuredHeight() - subMenuPartSize, this.centerX + subMenuPartSize, getMeasuredHeight() + subMenuPartSize);
        drawArcSectors(180,sweepAngle,arc) ;
        for (int i = 0; i < itemNum; i++) {

            centerX = (int) (this.centerX + Math.sin(Math.toRadians(startAngle)) * (subMenuPartSize * 0.8));
            centerY = (int) (getMeasuredHeight() - Math.cos(Math.toRadians(startAngle)) * (subMenuPartSize * 0.8));


            drawSubMenuIcon(canvas, centerX, centerY, i);
            startAngle += sweepAngle;

            menuRectF = new RectF((int)(centerX-mainMenuPartSize *0.7 ), (int)(centerY-mainMenuPartSize * 0.7), (int)(centerX+mainMenuPartSize * 0.7), (int)(centerY+mainMenuPartSize * 0.7));
            if (menuRectFList.size() - 1 > i) {
                menuRectFList.remove(i + 1);
            }
            menuRectFList.add(i + 1, menuRectF);
        }
    }

    private void drawArcSectors(int startAngle,int sweepAngle,RectF arc)
    {
        sPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (int i = 0; i < itemNum; i++) {

            if (i%2 == 0) {
                sPaint.setColor(getResources().getColor(R.color.subMenu1));
                sPaint.setAlpha((int) arcAlphaValue);
            }
            else {
                sPaint.setColor(getResources().getColor(R.color.subMenu2));
                sPaint.setAlpha((int) arcAlphaValue);

            }


            if (i == newFocusedIndex-1) {
                sPaint.setColor(getResources().getColor(R.color.focusedItem));
                sPaint.setAlpha((int) focusedAlphaValue);
            }


            canvas.drawArc(arc,startAngle,sweepAngle,true,sPaint);

            startAngle += sweepAngle;
        }
    }

    private void drawSubMenuIcon(Canvas canvas, int centerX, int centerY, int index) {

        if (index == newFocusedIndex-1)
            isIconFocused = true;
        else
            isIconFocused = false;

        resetBoundsAndDrawIcon(canvas, subMenuDrawableList.get(index),centerX,centerY,iconSize,isIconFocused,false);
    }

    private void drawMainMenu(Canvas canvas) {

        if (status == STATUS_MENU_CLOSED ) {
            miniArcSize = mainMenuPartSize - 10 ;
        }


        oPaint.setColor(getResources().getColor(R.color.subMenu1));
        RectF arc = new RectF(centerX - miniArcSize, getMeasuredHeight() - miniArcSize, centerX + miniArcSize, getMeasuredHeight() + miniArcSize);
        canvas.drawArc(arc,180f,180f,true,oPaint);
        drawMainMenuIcon(canvas);
    }

    private void drawMainMenuIcon(Canvas canvas) {
        isIconFocused = true;
        canvas.save();
        switch (status) {
            case STATUS_MENU_CLOSED:

                resetBoundsAndDrawIcon(canvas,openMenuIcon, (int)(centerX), (int)(getMeasuredHeight() - (mainMenuPartSize/2.5)), iconSize,isIconFocused,true);
                break;

            case STATUS_MENU_OPENED:
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, (int)(centerX), (int)(getMeasuredHeight() - (mainMenuPartSize/2.5)), iconSize,isIconFocused,true);
                break;
        }
        canvas.restore();
    }

    private void resetBoundsAndDrawIcon(Canvas canvas, Drawable drawable, int centerX, int centerY, int diff,boolean isIconFocused,boolean isMainMenuIcon) {

        if (drawable == null) return;

        drawable.setBounds(centerX - diff, centerY - diff, centerX + diff, centerY + diff);
        if (!isMainMenuIcon)
            drawable.setAlpha((int)arcAlphaValue);
        drawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = clickWhichRectF(event.getX(), event.getY());
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                pressed = true;
                if (index != -1) {
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (index == -1) {
                    pressed = false;
                }
                break;


            case MotionEvent.ACTION_UP:
                if (index == -1) {
                    pressed = false;
                }
                if (index == 0) {
                    if (status == STATUS_MENU_CLOSED) {
                        openDrawerAnimation();
                    } else if (status == STATUS_MENU_OPENED) {
                        closeDrawerAnimation();
                    }
                    pressed = false;
                }
                    else {
                    if (status == STATUS_MENU_OPENED && index != -1) {
                        newFocusedIndex = index;
                        openMenuIcon = convertDrawable(menuResList.get(newFocusedIndex-1));
                        openMenuIcon.setTint(getResources().getColor(R.color.white));
                        if (onMenuSelectedListener != null)
                            onMenuSelectedListener.onMenuSelected(newFocusedIndex-1);
                        startSubMenuAnim();
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    private void startSubMenuAnim(){

        if (animator != null)
            if (animator.isRunning())
                animator.cancel();

        animator = ValueAnimator.ofFloat(130f,255f);
        animator.setDuration(1400);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                focusedAlphaValue = (float)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    private void openDrawerAnimation()
    {

        if (animator != null)
            if (animator.isRunning())
                animator.cancel();


            //     Mini Arc Anim
        animator = ValueAnimator.ofFloat(0f,20f);
        animator.setDuration(200);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                miniArcSize = mainMenuPartSize + (float)valueAnimator.getAnimatedValue();
                invalidate();
                status = STATUS_MENU_OPENED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuOpened();

            }
        });

            // Big Arc Anim

        ValueAnimator animator2 = ValueAnimator.ofFloat(0f,255f);
        animator2.setDuration(400);

        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                arcAlphaValue = (float) valueAnimator.getAnimatedValue();
                focusedAlphaValue = (float) valueAnimator.getAnimatedValue();
                invalidate();

            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(600);
        animatorSet.playTogether(animator,animator2);
        animatorSet.start();

    }


    private void closeDrawerAnimation()
    {

        if (animator != null)
            if (animator.isRunning())
                animator.cancel();


        //     Mini Arc Anim
        animator = ValueAnimator.ofFloat(0f,10f);
        animator.setDuration(200);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                miniArcSize =  mainMenuPartSize - (float)valueAnimator.getAnimatedValue();
                invalidate();
                status = STATUS_MENU_OPENED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuOpened();

            }
        });

        // Big Arc Anim

        ValueAnimator animator2 = ValueAnimator.ofFloat(255f,0f);
        animator2.setDuration(400);

        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                arcAlphaValue = (float) valueAnimator.getAnimatedValue();
                focusedAlphaValue = (float) valueAnimator.getAnimatedValue();
                invalidate();

            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(600);
        animatorSet.playTogether(animator,animator2);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                status = STATUS_MENU_CLOSED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuClosed();
            }
        });
        animatorSet.start();
    }

    private int clickWhichRectF(float x, float y) {
        int index = -1;

        for (RectF rectF : menuRectFList) {
            if (rectF.contains(x, y)) {
                index = menuRectFList.indexOf(rectF);
                break;
            }
        }
        return index;
    }

    private Drawable convertDrawable(int iconRes) {
        return getResources().getDrawable(iconRes);
    }

    private Drawable convertBitmap(Bitmap bitmap) {
        return new BitmapDrawable(getResources(), bitmap);
    }

    private void resetMainDrawableBounds() {
        openMenuIcon.setBounds(getMeasuredWidth() - iconSize / 2, getMeasuredHeight() - iconSize / 2,
                getMeasuredWidth() + iconSize / 2, getMeasuredHeight() + iconSize / 2);
        closeMenuIcon.setBounds(getMeasuredWidth() - iconSize / 2, getMeasuredHeight() - iconSize / 2,
                getMeasuredWidth() + iconSize / 2, getMeasuredHeight() + iconSize / 2);
    }


    public CenterNavBar2 setMainMenu(int closeMenuRes) {


        closeMenuIcon = convertDrawable(closeMenuRes);

        return this;
    }

    public CenterNavBar2 setMainMenu(Bitmap closeMenuBitmap) {
        closeMenuIcon = convertBitmap(closeMenuBitmap);
        return this;
    }

    public CenterNavBar2 setMainMenu(Drawable closeMenuDrawable) {
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CenterNavBar2 addSubMenu(int menuRes)
    {
        this.addSubMenu(menuRes,false);
        return  this;
    }


    public CenterNavBar2 addSubMenu(int menuRes, boolean isItemFocused) {
        if (subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuDrawableList.add(convertDrawable(menuRes));
            itemNum = subMenuDrawableList.size();
            menuResList.add(menuRes);

            if(isItemFocused)
            {
                newFocusedIndex = itemNum;
                openMenuIcon = convertDrawable(menuRes);
                openMenuIcon.setTint(getResources().getColor(R.color.white));
            }
        }
        return this;
    }

    public CenterNavBar2 addSubMenu(Bitmap menuBitmap) {
        if (subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuDrawableList.add(convertBitmap(menuBitmap));
            itemNum = subMenuDrawableList.size();
        }
        return this;
    }

    public CenterNavBar2 addSubMenu(Drawable menuDrawable) {
        if ( subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuDrawableList.add(menuDrawable);
            itemNum =  subMenuDrawableList.size();
        }
        return this;
    }


    public void openMenu() {
        if (status == STATUS_MENU_CLOSED) {
            openDrawerAnimation();
        }
    }


    public void closeMenu() {
        if (status == STATUS_MENU_OPENED) {
            closeDrawerAnimation();
        }
    }

    public boolean isOpened() {
        return status == STATUS_MENU_OPENED;
    }

    public CenterNavBar2 setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    public CenterNavBar2 setOnMenuStatusChangeListener(OnMenuStatusChangedListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

