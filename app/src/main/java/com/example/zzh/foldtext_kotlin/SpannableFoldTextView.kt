package com.example.zzh.foldtext

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.example.zzh.foldtext_kotlin.MyLinkMovementMethod
import com.example.zzh.foldtext_kotlin.R


/**
 * Created by zhangzhihao on 2018/6/28 10:26.
 */

class SpannableFoldTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr), View.OnClickListener {


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
    /**
     * 提示的span
     */
    private var mSpan: ExpandSpan? = null

    var flag = false

    /**
     * 展开后是否显示文字提示
     */
    var isShowTipAfterExpand = false

    /**
     * 是否是Span的点击事件
     */
    var isExpandSpanClick = false

    /**
     * 父View是否设置了点击事件
     */
    var isParentClick = false
    var listener: OnClickListener? = null

    companion object {
        val ELLIPSIZE_END = "..."
        val MAX_LINE = 4
        val EXPAND_TIP_TEXT = "收起全文"
        val FOLD_TIP_TEXT = "全文"
        val TIP_COLOR = -0x1
        val END = 0
    }

    init {
        mShowMaxLine = MAX_LINE
        mSpan = ExpandSpan()
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.FoldTextView)
            mShowMaxLine = arr.getInt(R.styleable.FoldTextView_showMaxLine, MAX_LINE)
            mTipGravity = arr.getInt(R.styleable.FoldTextView_tipGravity, END)
            mTipColor = arr.getColor(R.styleable.FoldTextView_tipColor, TIP_COLOR)
            mTipClickable = arr.getBoolean(R.styleable.FoldTextView_tipClickable, false)
            mFoldText = arr.getString(R.styleable.FoldTextView_foldText)
            mExpandText = arr.getString(R.styleable.FoldTextView_expandText)
            isShowTipAfterExpand = arr.getBoolean(R.styleable.FoldTextView_showTipAfterExpand, false)
            isParentClick = arr.getBoolean(R.styleable.FoldTextView_isSetParentClick, false)
            arr.recycle()
        }
        if (TextUtils.isEmpty(mExpandText)) {
            mExpandText = EXPAND_TIP_TEXT
        }
        if (TextUtils.isEmpty(mFoldText)) {
            mFoldText = FOLD_TIP_TEXT
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if (TextUtils.isEmpty(text) || mShowMaxLine == 0) {
            super.setText(text, type)
        } else if (isExpand) {
            //文字展开
            val spannable = SpannableStringBuilder(mOriginalText)
            addTip(spannable, type)
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

    fun addTip(span: SpannableStringBuilder, type: BufferType?) {
        if (!(isExpand && !isShowTipAfterExpand)) {
            //折叠或者展开并且展开后显示提示
            if (mTipGravity == END) {
                span.append("  ")
            } else {
                span.append("\n")
            }
            var length = 0
            if (isExpand) {
                span.append(mExpandText)
                length = mExpandText!!.length
            } else {
                span.append(mFoldText)
                length = mFoldText!!.length
            }
            if (mTipClickable) {
                span.setSpan(mSpan, span.length - length, span.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                if (isParentClick) {
                    movementMethod = MyLinkMovementMethod.getInstance()
                    isClickable = false
                    isFocusable = false
                    isLongClickable = false
                } else {
                    movementMethod = LinkMovementMethod.getInstance()
                }
                span.setSpan(ForegroundColorSpan(mTipColor), span.length - length, span.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        super.setText(span, type)
    }

    fun formatText(text: CharSequence?, type: BufferType?) {
        mOriginalText = text.toString()
        var layout = getLayout()
        if (layout == null || !layout.text.equals(mOriginalText)) {
            super.setText(mOriginalText, type)
            layout = getLayout()
        }
        if (layout == null) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    translateText(getLayout(), type)
                }
            })
        } else {
            translateText(layout, type)
        }
    }

    fun translateText(layout: Layout, type: BufferType?) {
        if (layout.lineCount > mShowMaxLine) {
            val span = SpannableStringBuilder()
            val start = layout.getLineStart(mShowMaxLine - 1)
            var end = layout.getLineVisibleEnd(mShowMaxLine - 1)
            val paint = paint
            val builder = StringBuilder()
            if (mTipGravity == END) {
                builder.append("   ").append(mFoldText)
            }
            end -= paint.breakText(mOriginalText, start, end, false, paint.measureText(builder.toString()), null)
            val ellipsize = mOriginalText.subSequence(0, end)
            span.append(ellipsize)
            span.append(ELLIPSIZE_END)
            addTip(span, type)
        }
    }

    private inner class ExpandSpan : ClickableSpan() {

        override fun onClick(widget: View) {
            if (mTipClickable) {
                isExpand = !isExpand
                isExpandSpanClick = true
                Log.d("emmm", "onClick: span click")
                text = mOriginalText
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.setColor(mTipColor)
            ds.isUnderlineText = false
        }
    }

    override fun onClick(v: View?) {
        if (isExpandSpanClick) {
            isExpandSpanClick = false
        } else {
            listener?.onClick(v)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        listener = l
        super.setOnClickListener(this)
    }
}
