package com.codeholic.cornernavigation;

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
import java.util.Collections;
import java.util.List;

public class CornerNavBar extends View {

    private static boolean STATUS_SUBMENU_PRESSED = false;

    private static final int STATUS_MENU_OPENED = 1 << 1;

    private static final int STATUS_MENU_CLOSED = 1 << 4;

    private static final int MAX_SUBMENU_NUM = 7;

    private static boolean IS_SUBMENU_ANIM_RUNNING = false;

    private int pressedIndex =0;

    private int newFocusedIndex;

    private boolean isIconFocused = false;

    private  ValueAnimator animator;

    private Canvas canvas;

    private int partSize;

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

    private float miniArcSize;

    private OnMenuSelectedListener onMenuSelectedListener;

    private OnMenuStatusChangedListener onMenuStatusChangeListener;

    public CornerNavBar(Context context) {
        this(context, null);
    }

    public CornerNavBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerNavBar(Context context, AttributeSet attrs, int defStyleAttr) {
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

        partSize =(int) (minSize / 2.5);
        iconSize = partSize / 6 ;

        resetMainDrawableBounds();

        RectF mainMenuRectF = new RectF(getMeasuredWidth() - partSize, getMeasuredHeight() - partSize, getMeasuredWidth() + partSize, getMeasuredHeight() + partSize);
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
        if (STATUS_SUBMENU_PRESSED){
            drawSubMenu(canvas);
            drawMainMenu(canvas);
        }
    }


    private void drawSubMenu(Canvas canvas) {

        int centerX=0, centerY=0, sweepAngle = 30,startAngle=285,angle=0;
        RectF menuRectF;

        RectF arc = new RectF(getMeasuredWidth() - partSize*2, getMeasuredHeight() - partSize*2, getMeasuredWidth() + partSize*2, getMeasuredHeight() + partSize*2);
        drawArcSectors(180,30,arc) ;
        for (int i = 0; i < itemNum; i++) {


            if (pressed && (newFocusedIndex-1 == 1 || newFocusedIndex-1 == 2))
            {
                angle = 285 + (30 * i);
                centerX = (int) (getMeasuredWidth() + Math.sin(Math.toRadians(angle - fraction)) * (partSize * 1.6));
                centerY = (int) (getMeasuredHeight() - Math.cos(Math.toRadians(angle - fraction)) * (partSize * 1.6));

                drawSubMenuIcon(canvas, centerX, centerY, i);
                angle -= sweepAngle;

            }
            else {
                centerX = (int) (getMeasuredWidth() + Math.sin(Math.toRadians(startAngle)) * (partSize * 1.6));
                centerY = (int) (getMeasuredHeight() - Math.cos(Math.toRadians(startAngle)) * (partSize * 1.6));

                drawSubMenuIcon(canvas, centerX, centerY, i);
                startAngle += sweepAngle;

            }
            menuRectF = new RectF((int)(centerX-partSize/1.6), (int)(centerY-partSize/1.6), (int)(centerX+partSize/1.6), (int)(centerY+partSize/1.6));
            if (menuRectFList.size() - 1 > i) {
                menuRectFList.remove(i + 1);
            }
            menuRectFList.add(i + 1, menuRectF);
        }
        //if (STATUS_SUBMENU_PRESSED)
            //STATUS_SUBMENU_PRESSED = false;
     //   pressed = false;
    }

    private void drawArcSectors(int startAngle,int sweepAngle,RectF arc)
    {
        sPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (int i = 0; i < itemNum; i++) {
            switch (i){
                case 0 : sPaint.setColor(getResources().getColor(R.color.subMenuStart));
                    break;
                case 1 : sPaint.setColor(getResources().getColor(R.color.subMenuCenter));
                    break;
                case 2 : sPaint.setColor(getResources().getColor(R.color.subMenuEnd));
                    break;
                default:
                    sPaint.setColor(getResources().getColor(R.color.subMenuEnd));
                    break;
            }

            sPaint.setAlpha((int) arcAlphaValue);
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
            miniArcSize = partSize - 10 ;
        }


        oPaint.setColor(getResources().getColor(R.color.menuMain));
        RectF arc = new RectF(getMeasuredWidth() - miniArcSize, getMeasuredHeight() - miniArcSize, getMeasuredWidth() + miniArcSize, getMeasuredHeight() + miniArcSize);
        canvas.drawArc(arc,180f,90f,true,oPaint);
        drawMainMenuIcon(canvas);
    }

    private void drawMainMenuIcon(Canvas canvas) {
        isIconFocused = true;
        canvas.save();
        switch (status) {
            case STATUS_MENU_CLOSED:

                resetBoundsAndDrawIcon(canvas,openMenuIcon, (int)(getMeasuredWidth() - (partSize/2.5)), (int)(getMeasuredHeight() - (partSize/2.5)), iconSize,isIconFocused,true);
                break;

            case STATUS_MENU_OPENED:
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, (int)(getMeasuredWidth() - (partSize/2.5)), (int)(getMeasuredHeight() - (partSize/2.5)), iconSize,isIconFocused,true);
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
                        openMenuIcon = convertDrawable(menuResList.get(newFocusedIndex-1));
                        openMenuIcon.setTint(getResources().getColor(R.color.white));
                        newFocusedIndex = index;
                        calculateAngleDifference();
                        if (onMenuSelectedListener != null)
                            onMenuSelectedListener.onMenuSelected(newFocusedIndex-1);
                        invalidate();
                    }
                }

                break;
        }
        if (IS_SUBMENU_ANIM_RUNNING)
            return false;
        else
            return true;
    }

    private void startSubMenuAnim(int difference){

        if (pressedIndex < itemNum) {
            pressedIndex += newFocusedIndex - 1;
            if (pressedIndex >= itemNum)
                pressedIndex -= itemNum;
        }
        IS_SUBMENU_ANIM_RUNNING = true;
        if (animator != null)
            if (animator.isRunning())
                animator.cancel();

        animator = ValueAnimator.ofFloat(0f,difference);
        animator.setDuration(1000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fraction = (float)valueAnimator.getAnimatedValue();
                /*if (pressedIndex < itemNum) {
                    pressedIndex += newFocusedIndex - 1;
                    if (pressedIndex >= itemNum)
                        pressedIndex -= itemNum;
                }*/
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int shiftDistance = newFocusedIndex -1;
                Collections.rotate(subMenuDrawableList,-shiftDistance);
                STATUS_SUBMENU_PRESSED = true;
                pressed = false;
                IS_SUBMENU_ANIM_RUNNING = false;
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
                miniArcSize = partSize + (float)valueAnimator.getAnimatedValue();
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
                miniArcSize =  partSize - (float)valueAnimator.getAnimatedValue();
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

    private void calculateAngleDifference()
    {
        switch (newFocusedIndex-1)
        {
            case 1:
                startSubMenuAnim(30);
                break;
            case 2:
                startSubMenuAnim(60);
                break;
        }
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


    public CornerNavBar setMainMenu(int closeMenuRes) {


        closeMenuIcon = convertDrawable(closeMenuRes);

        return this;
    }

    public CornerNavBar setMainMenu(Bitmap closeMenuBitmap) {
        closeMenuIcon = convertBitmap(closeMenuBitmap);
        return this;
    }

    public CornerNavBar setMainMenu(Drawable closeMenuDrawable) {
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CornerNavBar addSubMenu(int menuRes)
    {
        this.addSubMenu(menuRes,false);
        return  this;
    }


    public CornerNavBar addSubMenu(int menuRes, boolean isItemFocused) {
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

    public CornerNavBar addSubMenu(Bitmap menuBitmap) {
        if (subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuDrawableList.add(convertBitmap(menuBitmap));
            itemNum = subMenuDrawableList.size();
        }
        return this;
    }

    public CornerNavBar addSubMenu(Drawable menuDrawable) {
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

    public CornerNavBar setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    public CornerNavBar setOnMenuStatusChangeListener(OnMenuStatusChangedListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

