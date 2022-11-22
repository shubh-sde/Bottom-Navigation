package com.codeholic.centernavigation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CenterNavBar extends View {

    private static final int STATUS_MENU_OPENED = 1 << 1;

    private static final int STATUS_MENU_CLOSED = 1 << 4;

    private static final int MAX_SUBMENU_NUM = 7;

    private final int shadowRadius = 5;

    private int newFocusedIndex,focusedIndex;

    private boolean isIconFocused = false;

    private  ValueAnimator animator;

    private Canvas canvas;

    private int partSize;

    private int iconSize;

    private float CircularMenuRadius;

    private int itemNum;

    private float itemMenuRadius;

    private float fraction, rFraction;

    private float pathLength;

    private int mainMenuColor;

    private Drawable openMenuIcon, closeMenuIcon;

    //private List<Integer> subMenuColorList;

    private List<Drawable> subMenuDrawableList;

    private List<Integer> menuResList;

    private List<RectF> menuRectFList;

    private int centerX, centerY;

    private int clickIndex;

    private int rotateAngle;

    private float CANVAS_ALPHA_VALUE,FOCUSED_ITEM_ALPHA_VALUE;

    private static final int MIN_LONG_PRESSED_DURATION = 500; // Set to 1000 milli seconds to handle long press events

    private long buttonPressedDuration;

    private boolean longPressedActive,isOptionDragActive,isAlreadyVibrated;

    private int itemIconSize;

    private int status;

    private boolean pressed;

    private Paint oPaint, focusedPaint, sPaint;

    private int fadeInAlphaValue=0;

    private PathMeasure pathMeasure;

    private Path path, dstPath;

    private OnMenuSelectedListener onMenuSelectedListener;

    private OnMenuStatusChangedListener onMenuStatusChangeListener;

    public CenterNavBar(Context context) {
        this(context, null);
    }

    public CenterNavBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CenterNavBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        status = STATUS_MENU_CLOSED;
        init();
    }

    private void init() {
        initTool();

        mainMenuColor = Color.parseColor("#613eea");

        openMenuIcon = new GradientDrawable();
        closeMenuIcon = new GradientDrawable();

      //  subMenuColorList = new ArrayList<>();
        subMenuDrawableList = new ArrayList<>();
        menuRectFList = new ArrayList<>();

        menuResList = new ArrayList<>();


    }

    private void initTool() {
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        focusedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusedPaint.setStyle(Paint.Style.STROKE);
        focusedPaint.setStrokeCap(Paint.Cap.ROUND);

        sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sPaint.setStyle(Paint.Style.FILL);

        path = new Path();
        dstPath = new Path();
        pathMeasure = new PathMeasure();
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

        partSize = minSize / 10;
        iconSize =(int) partSize ;


        /*                              Dynamically calculate center radius of Circle
        if(itemNum > 7)
        {
            float constantA =(float) (3.5 + (itemNum / 5));
            CircularMenuRadius = (float) (partSize * constantA);
        }
        else {
            CircularMenuRadius = (float) (partSize * 3.5);
        }*/

        CircularMenuRadius = (float) (partSize * 3.5);


        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        resetMainDrawableBounds();

        RectF mainMenuRectF = new RectF(centerX - partSize, centerY - partSize, centerX + partSize, centerY + partSize);
        menuRectFList.add(mainMenuRectF);

        itemIconSize = iconSize;
        itemMenuRadius = partSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        this.canvas = canvas;
        switch (status) {
            case STATUS_MENU_CLOSED:
                drawMainMenu(canvas);
                break;
            case STATUS_MENU_OPENED:
                drawMainMenu(canvas);
                drawSubMenu(canvas);
                break;
        }
    }


    private void drawSubMenu(Canvas canvas) {

        int itemX, itemY, angle;
        final float offsetRadius = 1.5f;
        RectF menuRectF;

        for (int i = 0; i < itemNum; i++) {
            angle = i * (360 / itemNum);
            if(i == newFocusedIndex-1)
            {
                itemX = (int) (centerX + Math.sin(Math.toRadians(angle)) * CircularMenuRadius);
                itemY = (int) (centerY - Math.cos(Math.toRadians(angle)) * CircularMenuRadius);

                oPaint.setColor(getResources().getColor(R.color.SubMenu));
                /*if (focusedIndex == newFocusedIndex){
                    oPaint.setColor(getResources().getColor(R.color.SubMenu));
                    oPaint.setAlpha((int)CANVAS_ALPHA_VALUE);
                }
                else
                {
                    oPaint.setAlpha((int)CANVAS_ALPHA_VALUE);
                }*/

            }
            else {
                itemX = (int) (centerX + Math.sin(Math.toRadians(angle)) * CircularMenuRadius);
                itemY = (int) (centerY - Math.cos(Math.toRadians(angle)) * CircularMenuRadius);

                oPaint.setColor(getResources().getColor(R.color.white));
                //oPaint.setAlpha((int)CANVAS_ALPHA_VALUE);
            }
            oPaint.setAlpha((int)CANVAS_ALPHA_VALUE);


            drawMenuShadow(canvas, itemX, itemY, itemMenuRadius);
            canvas.drawCircle(itemX, itemY, itemMenuRadius+15, oPaint);
            drawSubMenuIcon(canvas, itemX, itemY, i);

            menuRectF = new RectF(itemX - partSize, itemY - partSize, itemX + partSize, itemY + partSize);
            if (menuRectFList.size() - 1 > i) {
                menuRectFList.remove(i + 1);
            }
            menuRectFList.add(i + 1, menuRectF);

        }
    }


    private void drawSubMenuIcon(Canvas canvas, int centerX, int centerY, int index) {
        int diff;

            diff = iconSize / 2;


        if (index == newFocusedIndex-1)
            isIconFocused = true;
        else
            isIconFocused = false;

        resetBoundsAndDrawIcon(canvas, subMenuDrawableList.get(index), centerX, centerY, diff,isIconFocused,false);
    }

    private void resetBoundsAndDrawIcon(Canvas canvas, Drawable drawable, int centerX, int centerY, int diff,boolean isIconFocused,boolean isMainMenuIcon) {

        if (drawable == null) return;
        if (isIconFocused)
        {

            DrawableCompat.setTint(
                    DrawableCompat.wrap(drawable),
                    getResources().getColor(R.color.white)
            );
        }
        else
        {

            DrawableCompat.setTint(
                    DrawableCompat.wrap(drawable),
                    getResources().getColor(R.color.black)
            );
        }
        drawable.setBounds(centerX - diff, centerY - diff, centerX + diff, centerY + diff);
        if (!isMainMenuIcon)
            drawable.setAlpha((int)CANVAS_ALPHA_VALUE);
        drawable.draw(canvas);
    }

    private void drawMainMenu(Canvas canvas) {
        float centerMenuRadius, realFraction;
        if (status == STATUS_MENU_CLOSED ) {
            centerMenuRadius = partSize + 20 ;
        } else {
            centerMenuRadius = partSize + 20;
        }


        oPaint.setColor(getResources().getColor(R.color.MenuMain));

        canvas.drawCircle(centerX, centerY, centerMenuRadius, oPaint);
        drawMainMenuIcon(canvas);
    }

    private void drawMainMenuIcon(Canvas canvas) {
        isIconFocused = true;
        canvas.save();
        switch (status) {
            case STATUS_MENU_CLOSED:

                resetBoundsAndDrawIcon(canvas,openMenuIcon, centerX, centerY, iconSize / 2,isIconFocused,true);
                //openMenuIcon.draw(canvas);
                break;

            case STATUS_MENU_OPENED:
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, centerX, centerY, iconSize / 2,isIconFocused,true);
                break;
        }
        canvas.restore();
    }

    private void drawMenuShadow(Canvas canvas, int centerX, int centerY, float radius) {
        if (radius + shadowRadius > 0) {
            //            sPaint.setShader(new RadialGradient(centerX, centerY, radius + shadowRadius,                    Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP));
            sPaint.setAlpha((int) CANVAS_ALPHA_VALUE);
            sPaint.setColor(getResources().getColor(R.color.transparent));
            canvas.drawCircle(centerX, centerY, radius + shadowRadius, sPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = clickWhichRectF(event.getX(), event.getY());
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (!longPressedActive) {
                    longPressedActive = true;
                    buttonPressedDuration = Calendar.getInstance().getTimeInMillis();
                }
                pressed = true;
                if (index != -1) {
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (longPressedActive) {
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - buttonPressedDuration;
                    if (clickDuration >= MIN_LONG_PRESSED_DURATION  && index == 0 && status == STATUS_MENU_CLOSED) {
                        if (!isAlreadyVibrated) {
                            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(50);
                            isAlreadyVibrated = true;
                        }
                        Log.d("Index at move ----", String.valueOf(index));
                        //  Start the Object Drag ---------------
                        centerX = (int) event.getX();
                        centerY = (int) event.getY();

                        RectF mainMenuRectF = new RectF(centerX - partSize, centerY - partSize, centerX + partSize, centerY + partSize);
                        menuRectFList.set(0,mainMenuRectF);

                        isOptionDragActive = true;
                        invalidate();
                    }
                }
                if (index == -1) {
                    pressed = false;
                }
                break;


            case MotionEvent.ACTION_UP:
                pressed = false;
                longPressedActive = false;
                isAlreadyVibrated = false;
                if (index != -1) {
                }
                if (index == 0 && !isOptionDragActive) {

                    if (status == STATUS_MENU_CLOSED) {
                        openDrawerAnimation();
                    } else if (status == STATUS_MENU_OPENED) {
                        longPressedActive = false;
                        closeDrawerAnimation();
                    }
                } else {
                    if (status == STATUS_MENU_OPENED && index != -1) {
                        if (onMenuSelectedListener != null)
                            onMenuSelectedListener.onMenuSelected(index - 1);
                        rotateAngle = clickIndex * (360 / itemNum);
                        newFocusedIndex = index;
                        openMenuIcon = convertDrawable(menuResList.get(newFocusedIndex-1));
                        openMenuIcon.setTint(getResources().getColor(R.color.white));

                        invalidate();
                        //startCloseMeunAnima();
                    }
                    longPressedActive = false;
                }
                isOptionDragActive = false;
                break;
        }
        return true;
    }


    private void startSubMenuAnim(){

        if (animator != null)
            if (animator.isRunning())
                animator.cancel();

        animator = ValueAnimator.ofFloat(0f,255f);
        animator.setDuration(1400);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fraction = (float)valueAnimator.getAnimatedValue();
                FOCUSED_ITEM_ALPHA_VALUE = (float) valueAnimator.getAnimatedValue();
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


        animator = ValueAnimator.ofFloat(0f,255f);
        animator.setDuration(1400);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fraction = (float)valueAnimator.getAnimatedValue();
                CANVAS_ALPHA_VALUE = (float) valueAnimator.getAnimatedValue();
                invalidate();
                status = STATUS_MENU_OPENED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuOpened();
            }
        });

        animator.start();
    }


    private void closeDrawerAnimation()
    {

        if (animator != null)
            if (animator.isRunning())
                animator.cancel();

        animator = ValueAnimator.ofFloat(255f,0f);
        animator.setDuration(1400);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fraction = (float) valueAnimator.getAnimatedValue();
                CANVAS_ALPHA_VALUE = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                status = STATUS_MENU_CLOSED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuClosed();
            }
        });
        animator.start();
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
        openMenuIcon.setBounds(centerX - iconSize / 2, centerY - iconSize / 2,
                centerX + iconSize / 2, centerY + iconSize / 2);
        closeMenuIcon.setBounds(centerX - iconSize / 2, centerY - iconSize / 2,
                centerX + iconSize / 2, centerY + iconSize / 2);
    }


    public CenterNavBar setMainMenu(int mainMenuColor, int closeMenuRes) {

        //openMenuIcon = convertDrawable(openMenuRes);
        closeMenuIcon = convertDrawable(closeMenuRes);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CenterNavBar setMainMenu(int mainMenuColor, Bitmap closeMenuBitmap) {
        //openMenuIcon = convertBitmap(openMenuBitmap);
        closeMenuIcon = convertBitmap(closeMenuBitmap);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CenterNavBar setMainMenu(int mainMenuColor, Drawable closeMenuDrawable) {
        //openMenuIcon = openMenuDrawable;
        closeMenuIcon = closeMenuDrawable;
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CenterNavBar addSubMenu(int menuRes)
    {
        this.addSubMenu(menuRes,false);
        return  this;
    }


    public CenterNavBar addSubMenu(int menuRes, boolean isItemFocused) {
        //if (subMenuColorList.size() < MAX_SUBMENU_NUM && subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            if (subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            //subMenuColorList.add(menuColor);
            subMenuDrawableList.add(convertDrawable(menuRes));
            itemNum = subMenuDrawableList.size();
            menuResList.add(menuRes);

            if(isItemFocused)
            {
                newFocusedIndex = focusedIndex = itemNum;
                openMenuIcon = convertDrawable(menuRes);
                openMenuIcon.setTint(getResources().getColor(R.color.white));
            }
        }
        return this;
    }

    public CenterNavBar addSubMenu(int menuColor, Bitmap menuBitmap) {
        if (subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuDrawableList.add(convertBitmap(menuBitmap));
            itemNum =  subMenuDrawableList.size();
        }
        return this;
    }

    public CenterNavBar addSubMenu(int menuColor, Drawable menuDrawable) {
        if ( subMenuDrawableList.size() < MAX_SUBMENU_NUM) {

            subMenuDrawableList.add(menuDrawable);
            itemNum = subMenuDrawableList.size();
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

    public CenterNavBar setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    public CenterNavBar setOnMenuStatusChangeListener(OnMenuStatusChangedListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        Log.d("Density Value ----",String.valueOf(scale));
        return (int) (dpValue * scale + 0.5f);
    }
}

