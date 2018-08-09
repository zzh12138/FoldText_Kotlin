package com.example.zzh.foldtext_kotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.AppCompatTextView
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewTreeObserver

/**
 * Created by zhangzhihao on 2018/7/20 17:41.
 * describe 该类主要完成以下功能
 *  1.显示评论列表
 */
class FoldTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {
    companion object {
        val ELLIPSIZE_END = "..."
        val MAX_LINE = 4
        val EXPAND_TIP_TEXT = "  收起全文"
        val FOLD_TIP_TEXT = "全文"
        val TIP_COLOR = -0x1
        val END = 0
    }

    /**
     * 显示最大行数
     */
    var mShowMaxLine: Int = 0
    /**
     * 折叠文本
     */
    var mFoldText: String? = ""
    /**
     * 展开文本
     */
    var mExpandText: String? = ""
    /**
     * 原始文本
     */
    var mOriginalText: String = ""
    /**
     * 是否展开
     */
    var isExpand = false
    /**
     * 全文显示的位置 0末尾 1下一行
     */
    var mTipGravity = 0
    /**
     * 提示文字颜色
     */
    var mTipColor: Int = 0

    /**
     * 提示是否可点击
     */
    var mTipClickable = false
    var flag = false
    var mPaint: Paint = Paint()

    /**
     * 展开后是否显示文字提示
     */
    var isShowTipAfterExpand = false

    /**
     * 提示文字坐标范围
     */
    var minX: Float = 0f
    var maxX: Float = 0f
    var minY: Float = 0f
    var maxY: Float = 0f

    /**
     * 收起全文不在同一行时，增加一个变量记录坐标
     */
    var middleY: Float = 0f
    /**
     * 原始文本行数
     */
    var originalLineCount = 0
    /**
     * 是否超过最大行数
     */
    var isOverMaxLine = false

    /**
     * 点击时间
     */
    var clickTime = 0L

    init {
        mShowMaxLine = FoldTextView.MAX_LINE
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.FoldTextView)
            mShowMaxLine = arr.getInt(R.styleable.FoldTextView_showMaxLine, FoldTextView.MAX_LINE)
            mTipGravity = arr.getInt(R.styleable.FoldTextView_tipGravity, FoldTextView.END)
            mTipColor = arr.getColor(R.styleable.FoldTextView_tipColor, FoldTextView.TIP_COLOR)
            mTipClickable = arr.getBoolean(R.styleable.FoldTextView_tipClickable, false)
            mFoldText = arr.getString(R.styleable.FoldTextView_foldText)
            mExpandText = arr.getString(R.styleable.FoldTextView_expandText)
            isShowTipAfterExpand = arr.getBoolean(R.styleable.FoldTextView_showTipAfterExpand, false)
            arr.recycle()
        }
        if (TextUtils.isEmpty(mExpandText)) {
            mExpandText = FoldTextView.EXPAND_TIP_TEXT
        }
        if (TextUtils.isEmpty(mFoldText)) {
            mFoldText = FoldTextView.FOLD_TIP_TEXT
        }
        if (mTipGravity == END) {
            mFoldText = "  " + mFoldText
        }
        mPaint.textSize = textSize
        mPaint.color = mTipColor
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (TextUtils.isEmpty(text) || mShowMaxLine == 0) {
            super.setText(text, type)
        } else if (isExpand) {
            //文字展开
            val spannable = SpannableStringBuilder(mOriginalText)
            if (isShowTipAfterExpand) {
                spannable.append(mExpandText)
                spannable.setSpan(ForegroundColorSpan(mTipColor), spannable.length - mExpandText.length, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            super.setText(spannable, type)
            val mLieCount = lineCount
            val layout = layout
            minX = paddingLeft + layout.getPrimaryHorizontal(spannable.lastIndexOf(mExpandText!![0]) - 1)
            maxX = paddingLeft + layout.getPrimaryHorizontal(spannable.lastIndexOf(mExpandText!![mExpandText!!.length - 1]) + 1)
            val bound = Rect()
            layout.getLineBounds(originalLineCount - 1, bound)
            if (mLieCount > originalLineCount) {
                //不在同一行
                minY = (paddingTop + bound.top).toFloat()
                middleY = minY + paint.fontMetrics.descent - paint.fontMetrics.ascent
                maxY = middleY + paint.fontMetrics.descent - paint.fontMetrics.ascent
            } else {
                //同一行
                minY = (paddingTop + bound.top).toFloat()
                maxY = minY + paint.fontMetrics.descent - paint.fontMetrics.ascent
            }

        } else {
            if (!flag) {
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        viewTreeObserver.removeOnPreDrawListener(this)
                        flag = true
                        formatText(text, type)
                        return true
                    }
                })
            } else {
                formatText(text, type)
            }
        }
    }

    fun formatText(text: CharSequence?, type: BufferType?) {
        mOriginalText = text.toString()
        var l = layout
        if (l == null || !l.text.equals(mOriginalText)) {
            super.setText(mOriginalText, type)
            l = layout
        }
        if (l == null) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    translateText(l, type)
                }
            })
        } else {
            translateText(l, type)
        }
    }

    fun translateText(l: Layout, type: BufferType?) {
        //记录原始行数
        originalLineCount = l.lineCount
        if (l.lineCount > mShowMaxLine) {
            isOverMaxLine = true
            val span = SpannableStringBuilder()
            val start = l.getLineStart(mShowMaxLine - 1)
            var end = l.getLineVisibleEnd(mShowMaxLine - 1)
            if (mTipGravity == END) {
                val builder = StringBuilder(ELLIPSIZE_END).append("  ").append(mFoldText)
                end -= paint.breakText(mOriginalText, start, end, false, paint.measureText(builder.toString()), null)
            } else {
                end--;
            }
            val ellipsize = mOriginalText.subSequence(0, end)
            span.append(ellipsize).append(ELLIPSIZE_END)
            if (mTipGravity != END) {
                span.append("\n")
            }
            super.setText(span, type)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isOverMaxLine && !isExpand) {
            //折叠
            if (mTipGravity == END) {
                minX = width - paddingLeft - paddingRight - paint.measureText(mFoldText)
                maxX = (width - paddingLeft - paddingRight).toFloat()
            } else {
                minX = paddingLeft.toFloat()
                maxX = minX + paint.measureText(mFoldText)
            }
            minY = height - (paint.fontMetrics.descent - paint.fontMetrics.ascent) - paddingBottom
            maxY = (height - paddingBottom).toFloat()
            canvas?.drawText(mFoldText, minX, height - paint.fontMetrics.descent - paddingBottom, mPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mTipClickable) {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    clickTime = System.currentTimeMillis()
                    if (!isClickable && isInRange(event.x, event.y)) {
                        return true
                    }
                }
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    val delTime = System.currentTimeMillis() - clickTime
                    clickTime = 0L
                    if (delTime < ViewConfiguration.getTapTimeout() && isInRange(event.x, event.y)) {
                        isExpand = !isExpand
                        text = mOriginalText
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun isInRange(x: Float, y: Float): Boolean {
        if (minX < maxX) {
            //同一行
            return x in minX..maxX && y in minY..maxY
        } else {
            //两行
            return x <= maxX && y in middleY..maxY || x >= minX && y in minY..middleY
        }
    }
}