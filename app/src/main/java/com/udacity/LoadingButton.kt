package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(2000)

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    private var buttonColor = 0
    private var progressBarColor = 0
    private var textColor = 0
    private var diskColor = 0

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColor = getColor(R.styleable.LoadingButton_buttonColor, 0)
            progressBarColor = getColor(R.styleable.LoadingButton_progressBarColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            diskColor = getColor(R.styleable.LoadingButton_diskColor, 0)
        }
        valueAnimator.addUpdateListener {
            currentWidth = (it.animatedValue as Float) * widthSize
            currentDegree = (it.animatedValue as Float) * 360
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                if (buttonState == ButtonState.Clicked) {
                    buttonState = ButtonState.Completed
                }
                invalidate()
            }
        })
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = resources.getDimension(R.dimen.default_text_size)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var currentWidth = 0f
    private var currentDegree = 0f

    override fun performClick(): Boolean {
        buttonState = ButtonState.Clicked
        valueAnimator.start()

        super.performClick()
        return true
    }

    fun downloadStarted() {
        buttonState = ButtonState.Loading
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.start()
    }

    fun downloadComplete() {
        buttonState = ButtonState.Completed
        valueAnimator.end()
        invalidate()
    }

    val r = Rect()
    val rectF = RectF()
    val circleIndicatorWidth = 40f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            canvas.drawColor(buttonColor)
            if (buttonState == ButtonState.Completed) {
                paint.color = textColor
                canvas.drawText(
                    context.getString(R.string.button_name),
                    (widthSize / 2).toFloat(),
                    (heightSize / 2).toFloat() + 20,
                    paint
                )
            }
            if (buttonState == ButtonState.Loading || buttonState == ButtonState.Clicked) {
                paint.color = progressBarColor
                canvas.drawRect(0f, 0f, currentWidth, heightSize.toFloat(), paint)

                val text = context.getString(R.string.button_loading)
                paint.color = textColor
                canvas.drawText(
                    text,
                    (widthSize / 2).toFloat(),
                    (heightSize / 2).toFloat() + 20,
                    paint
                )

                paint.getTextBounds(text, 0, text.length, r)
                rectF.set(
                    (widthSize / 2f + r.width() / 2) + 5,
                    (heightSize / 2) - circleIndicatorWidth,
                    (widthSize / 2f + r.width() / 2) + 2 * circleIndicatorWidth,
                    (heightSize / 2) + circleIndicatorWidth
                )
                paint.color = diskColor
                canvas.drawArc(rectF, 0f, currentDegree, true, paint)

            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}