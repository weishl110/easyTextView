package com.android.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.android.shape.ShapeImpl
import com.android.shape.ShapeStyle
import com.android.spannable.SpannableImpl

/**
 * Create by weishl
 * 2022/11/3
 */
class EasyTextView : AppCompatTextView {

    private val TAG = "debug_EasyTextView"

    private val mCornerArray = FloatArray(8)
    private var mSolidColor: Int = 0
    private var mStrokeWidth: Float = 0f
    private var mStrokeColor: Int = 0
    private var mShapeBgType: ShapeBgType = ShapeBgType.CIRCLE

    private var mTextStrokeEnable: Boolean = false
    private var mTextStrokeWidth: Float = 0f
    private var mTextStrokeColor: Int = 0

    private var mOriginTextColor: Int = 0

    private var mDrawableLeftMode: DrawableMode = DrawableMode.NORMAL
    private var mDrawableTopMode: DrawableMode = DrawableMode.NORMAL
    private var mDrawableRightMode: DrawableMode = DrawableMode.NORMAL
    private var mDrawableBottomMode: DrawableMode = DrawableMode.NORMAL
    private var mDrawableLeftWidth = 0
    private var mDrawableLeftHeight = 0
    private var mDrawableTopWidth = 0
    private var mDrawableTopHeight = 0
    private var mDrawableRightWidth = 0
    private var mDrawableRightHeight = 0
    private var mDrawableBottomWidth = 0
    private var mDrawableBottomHeight = 0
    private var mDrawableChanged = false

    private var mShaderEnable: Boolean = false
    private var mShaderAngle = ShaderMode.LEFT_TO_RIGHT
    private var mShaderColorArrays = IntArray(3)

    // 文本渐变
    private var mTextShaderEnable = false
    private var mTextShaderStartColor = 0
    private var mTextShaderEndColor = 0
    private var mTextShaderMode: ShaderMode = ShaderMode.TOP_TO_BOTTOM
    private var mTextShader: LinearGradient? = null

    private var mTextBoldWidth: Float = 0f // 字体加粗

    // 状态背景颜色
    private var mBgSelectedColor: Int = 0
    private var mBgEnabledColor: Int = 0
    private var mBgPressedColor: Int = 0

    // 状态文本颜色
    private var mTextSelectedColor: Int = 0
    private var mTextEnabledColor: Int = 0
    private var mTextPressedColor: Int = 0

    private val DEFAULT_CORNER = 0f

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mSpanText: Array<String>? = null
    private var mSpanTextColor: Int = 0
    private var mSpanStyle: Int = Typeface.NORMAL
    private var mSpanTextSize: Int = 0
    private var isSpanText: Boolean = false

    private var mLineMode = LineMode.NORMAL

    private var mBorderText: AppCompatTextView? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
        createBorderView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
        createBorderView(context, attrs)
    }

    private fun createBorderView(context: Context, attrs: AttributeSet?) {
        if (mTextStrokeEnable && mTextStrokeWidth > 0) {
            mBorderText = object : AppCompatTextView(context, attrs) {

                private var mLeftDrawableWidth = 0f
                private var mTopDrawableWidth = 0f

                override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                    super.onSizeChanged(w, h, oldw, oldh)
                    getCompoundDrawableWidth()
                    setCompoundDrawables(null, null, null, null)
                }

                override fun onDraw(canvas: Canvas?) {
                    canvas?.save()
                    canvas?.translate(mLeftDrawableWidth, mTopDrawableWidth)
                    super.onDraw(canvas)
                    canvas?.restore()
                }


                fun getCompoundDrawableWidth() {
                    val drawables = compoundDrawables
                    if (drawables.isNotEmpty()) {
                        drawables[0]?.let {
                            mLeftDrawableWidth = it.intrinsicWidth + compoundDrawablePadding * 1f
                        }
                        drawables[1]?.let {
                            mTopDrawableWidth = it.intrinsicWidth + compoundDrawablePadding * 1f
                        }
                    }
                }
            }
            mBorderText?.let {
                val paint = it.paint
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.isAntiAlias = true
                paint.textSize = textSize
                paint.typeface = typeface
                paint.strokeWidth = mTextStrokeWidth
                paint.color = mTextStrokeColor
                it.setTextColor(mTextStrokeColor)
                it.gravity = gravity
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mBorderText?.let {
            val btText = it.text.toString()
            if (btText.isNullOrEmpty() || btText != text) {
                it.text = text
                postInvalidate()
            }
            it.measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mBorderText?.layout(left, top, right, bottom)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        mBorderText?.layoutParams = params
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs ?: return
        context.obtainStyledAttributes(attrs, R.styleable.SuperTextView).also {
            val count = it.indexCount
            for (index in 0 until count) {
                val attr = it.getIndex(index)
                when (attr) {
                    R.styleable.SuperTextView_etv_corner -> {
                        val corner = it.getDimension(attr, DEFAULT_CORNER)
                        if (corner != 0f) {
                            setStrokeCorner(corner)
                        }
                    }
                    R.styleable.SuperTextView_etv_left_top_corner -> {
                        if (mCornerArray[0] == 0f) {
                            val leftTop = it.getDimension(attr, 0f)
                            setLeftTopCorner(leftTop)
                        }
                    }
                    R.styleable.SuperTextView_etv_right_top_corner -> {
                        if (mCornerArray[2] == 0f) {
                            val rightTop = it.getDimension(attr, 0f)
                            setRightTopCorner(rightTop)
                        }
                    }
                    R.styleable.SuperTextView_etv_right_bottom_corner -> {
                        if (mCornerArray[4] == 0f) {
                            val rightBottom = it.getDimension(attr, 0f)
                            setRightBottomCorner(rightBottom)
                        }
                    }
                    R.styleable.SuperTextView_etv_left_bottom_corner -> {
                        if (mCornerArray[6] == 0f) {
                            val leftBottom = it.getDimension(attr, 0f)
                            setLeftBottomCorner(leftBottom)
                        }
                    }
                    R.styleable.SuperTextView_etv_solid -> {
                        mSolidColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_stroke_width -> {
                        mStrokeWidth = it.getDimension(attr, 0f)
                    }
                    R.styleable.SuperTextView_etv_stroke_color -> {
                        mStrokeColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_corner_type -> {
                        mShapeBgType = ShapeBgType.valueOf(it.getInt(attr, ShapeBgType.CIRCLE.code))
                    }
                    R.styleable.SuperTextView_etv_text_stroke_width -> {
                        mTextStrokeWidth = it.getDimension(attr, 0f)
                    }
                    R.styleable.SuperTextView_etv_text_stroke_color -> {
                        mTextStrokeColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_stroke -> {
                        mTextStrokeEnable = it.getBoolean(attr, false)
                    }
                    R.styleable.SuperTextView_etv_left_drawable_mode -> {
                        mDrawableLeftMode =
                            DrawableMode.valueOf(it.getInt(attr, DrawableMode.NORMAL.code))
                    }
                    R.styleable.SuperTextView_etv_top_drawable_mode -> {
                        mDrawableTopMode =
                            DrawableMode.valueOf(it.getInt(attr, DrawableMode.NORMAL.code))
                    }
                    R.styleable.SuperTextView_etv_right_drawable_mode -> {
                        mDrawableRightMode =
                            DrawableMode.valueOf(it.getInt(attr, DrawableMode.NORMAL.code))
                    }
                    R.styleable.SuperTextView_etv_bottom_drawable_mode -> {
                        mDrawableBottomMode =
                            DrawableMode.valueOf(it.getInt(attr, DrawableMode.NORMAL.code))
                    }
                    R.styleable.SuperTextView_etv_left_drawable_width -> {
                        mDrawableLeftWidth = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_left_drawable_height -> {
                        mDrawableLeftHeight = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_top_drawable_width -> {
                        mDrawableTopWidth = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_top_drawable_height -> {
                        mDrawableTopHeight = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_right_drawable_width -> {
                        mDrawableRightWidth = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_right_drawable_height -> {
                        mDrawableRightHeight = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_bottom_drawable_width -> {
                        mDrawableBottomWidth = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_bottom_drawable_height -> {
                        mDrawableBottomHeight = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_shader_enable -> {
                        mShaderEnable = it.getBoolean(attr, false)
                    }
                    R.styleable.SuperTextView_etv_shader_start_color -> {
                        mShaderColorArrays[0] = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_shader_center_color -> {
                        mShaderColorArrays[1] = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_shader_end_color -> {
                        mShaderColorArrays[2] = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_shader_mode -> {
                        mShaderAngle =
                            ShaderMode.valueOf(it.getInt(attr, ShaderMode.LEFT_TO_RIGHT.code))
                    }
                    R.styleable.SuperTextView_etv_text_shader_enable -> {
                        mTextShaderEnable = it.getBoolean(attr, false)
                    }
                    R.styleable.SuperTextView_etv_text_shader_start_color -> {
                        mTextShaderStartColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_shader_end_color -> {
                        mTextShaderEndColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_shader_mode -> {
                        mTextShaderMode =
                            ShaderMode.valueOf(it.getInt(attr, ShaderMode.TOP_TO_BOTTOM.code))
                    }
                    R.styleable.SuperTextView_etv_bold_width -> {
                        mTextBoldWidth = it.getDimension(attr, 0f)
                    }
                    R.styleable.SuperTextView_etv_bg_enabled_color -> {
                        mBgEnabledColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_bg_selected_color -> {
                        mBgSelectedColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_bg_pressed_color -> {
                        mBgPressedColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_enabled_color -> {
                        mTextEnabledColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_selected_color -> {
                        mTextSelectedColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_text_pressed_color -> {
                        mTextPressedColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_span_style -> {
                        mSpanStyle = it.getInt(attr, Typeface.NORMAL)
                    }
                    R.styleable.SuperTextView_etv_span_text -> {
                        val spanText = it.getString(attr).orEmpty()
                        if (spanText.isNotEmpty()) {
                            mSpanText = spanText.split(",").toTypedArray()
                        }
                    }
                    R.styleable.SuperTextView_etv_span_text_size -> {
                        mSpanTextSize = it.getDimensionPixelOffset(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_span_color -> {
                        mSpanTextColor = it.getColor(attr, 0)
                    }
                    R.styleable.SuperTextView_etv_line_style -> {
                        mLineMode = LineMode.valueOf(it.getInt(attr, LineMode.NORMAL.code))
                    }
                }
            }
            it.recycle()
        }
        mOriginTextColor = currentTextColor
        setSpanText()
    }

    private fun setSpanText() {
        val str = text.toString()
        val spanText = mSpanText
        if (str.isNullOrEmpty() || spanText.isNullOrEmpty()) return

        val span = SpannableImpl.get().init(str).color(mSpanTextColor, *spanText)
        if (mSpanTextSize > 0) {
            span.size(mSpanTextSize, *spanText)
        }
        if (mSpanStyle == Typeface.BOLD) {
            span.bold(*spanText)
        } else if (mSpanStyle == Typeface.ITALIC) {
            span.italic(*spanText)
        }
        isSpanText = true
        text = span.getSpan()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        calculBgCornerRadius()
        changeDrawableLocation()
    }

    private fun changeDrawableSize() {
        if (mDrawableLeftWidth != 0 || mDrawableLeftHeight != 0 || mDrawableTopWidth != 0 || mDrawableTopHeight != 0
            || mDrawableRightWidth != 0 || mDrawableRightHeight != 0 || mDrawableBottomWidth != 0 || mDrawableBottomHeight != 0
        ) {

            rebuild()
        }
    }

    private fun calculBgCornerRadius() {
        if (mShapeBgType == ShapeBgType.CIRCLE) {
            val halfWidth = mWidth / 1.8f
            mCornerArray.fill(halfWidth)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val needScroll = scrollX != 0 || scrollY != 0
        if (needScroll) {
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
        }
        drawBg(canvas)
        drawLine()

        if (isSpanText) {
            sdkOnDraw(canvas)
            return
        }

        if (mTextStrokeEnable) {
            mBorderText?.draw(canvas)
        } else if (mTextBoldWidth > 0f) {
            paint.strokeWidth = mTextBoldWidth
            paint.style = Paint.Style.FILL_AND_STROKE
            sdkOnDraw(canvas)
        }

        if (mTextShaderEnable) {
            drawShaderText(canvas)
        } else {
            setTextColor(getStatusTextColor())
            sdkOnDraw(canvas)
        }
    }

    private fun drawLine() {
        paint.isAntiAlias = true
        when (mLineMode) {
            LineMode.CENTER -> {
                paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            }
            LineMode.BOTTOM -> {
                paint.flags = Paint.UNDERLINE_TEXT_FLAG
            }
            else -> paint.flags = Paint.ANTI_ALIAS_FLAG
        }
    }

    private fun drawShaderText(canvas: Canvas) {
        val tempShader = paint.shader
        val lineCount = layout.lineCount
        if (layout != null && lineCount > 0) {
            var x0 = layout.getLineLeft(0)
            val y0 = layout.getLineTop(0).toFloat()
            var x1 = x0 + layout.getLineWidth(0)
            val y1 = y0 + layout.height
            if (lineCount > 1) {
                for (index in 0 until lineCount) {
                    if (x0 > layout.getLineLeft(index)) {
                        x0 = layout.getLineLeft(index)
                    }
                    if (x1 < x0 + layout.getLineWidth(index)) {
                        x1 = x0 + layout.getLineWidth(index)
                    }
                }
            }
            if (mTextShader == null) {
                mTextShader = createShader(
                    mTextShaderStartColor,
                    mTextShaderEndColor,
                    mTextShaderMode,
                    x0,
                    y0,
                    x1,
                    y1
                )
            }

            paint.shader = mTextShader
            sdkOnDraw(canvas)
        }
        paint.shader = tempShader
    }

    @SuppressLint("WrongCall")
    private fun sdkOnDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    private fun createShader(
        startColor: Int,
        endColor: Int,
        shaderMode: ShaderMode,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float
    ): LinearGradient? {
        if (startColor == 0 || endColor == 0) return null
        var tempX1 = x1
        var tempY1 = y1
        var tempStart = startColor
        var tempEnd = endColor
        when (shaderMode.code) {
            ShaderMode.TOP_TO_BOTTOM.code -> {
                tempX1 = x0
            }
            ShaderMode.BOTTOM_TO_TOP.code -> {
                tempX1 = x0
                tempStart = endColor
                tempEnd = startColor
            }
            ShaderMode.LEFT_TO_RIGHT.code -> tempY1 = y0
            ShaderMode.RIGHT_TO_LEFT.code -> {
                tempY1 = y0
                tempStart = endColor
                tempEnd = startColor

            }
            else -> tempX1 = x0
        }
        return LinearGradient(x0, y0, tempX1, tempY1, tempStart, tempEnd, Shader.TileMode.CLAMP)
    }

    private fun drawBg(canvas: Canvas) {
        var shapeImpl =
            ShapeImpl(context).setStyle(ShapeStyle.RECTANGLE).shapeSolid().color(getStatusBgColor())
                .then().shapeStroke().setStroke(mStrokeWidth.toInt(), mStrokeColor)
                .then().shapeCorners().radius(mCornerArray)
                .then()
        if (mShaderEnable && mShaderColorArrays[0] != 0 && mShaderColorArrays[1] != 0) {
            shapeImpl = shapeImpl.shapeGradient().setColors(mShaderColorArrays)
                .angle(ShaderMode.toGradientDrawableOrientation(mShaderAngle)).then()
        }
        shapeImpl.getShape().apply {
            setBounds(0, 0, mWidth, mHeight)
            draw(canvas)
        }
    }

    private fun getStatusBgColor(): Int {
        return if (mBgEnabledColor != 0 && !isEnabled) {
            mBgEnabledColor
        } else if (mBgSelectedColor != 0 && isSelected) {
            mBgSelectedColor
        } else if (mBgPressedColor != 0 && isPressed) {
            mBgPressedColor
        } else mSolidColor
    }

    private fun getStatusTextColor(): Int {
        return if (mTextEnabledColor != 0 && !isEnabled) {
            mTextEnabledColor
        } else if (mTextSelectedColor != 0 && isSelected) {
            mTextSelectedColor
        } else if (mTextPressedColor != 0 && isPressed) {
            mTextPressedColor
        } else mOriginTextColor
    }


    /** 背景圆角 */
    fun setStrokeCorner(radius: Float): EasyTextView {
        mCornerArray.fill(radius)
        return this
    }

    /** 背景左上角圆角度 */
    fun setLeftTopCorner(radius: Float): EasyTextView {
        mCornerArray[0] = radius
        mCornerArray[1] = radius
        return this
    }

    fun getLeftTopCorner() = mCornerArray[0]

    /** 背景右上角圆角度 */
    fun setRightTopCorner(radius: Float): EasyTextView {
        mCornerArray[2] = radius
        mCornerArray[3] = radius
        return this
    }

    fun getRightTopCorner() = mCornerArray[2]

    /** 背景左下角圆角度 */
    fun setRightBottomCorner(radius: Float): EasyTextView {
        mCornerArray[4] = radius
        mCornerArray[5] = radius
        return this
    }

    fun getRightBottomCorner() = mCornerArray[4]

    /** 背景左下角圆角度 */
    fun setLeftBottomCorner(radius: Float): EasyTextView {
        mCornerArray[6] = radius
        mCornerArray[7] = radius
        return this
    }

    fun getLeftBottomCorner() = mCornerArray[6]

    /** 背景描边宽度 */
    fun setStrokeWidth(strokeWidth: Float): EasyTextView {
        mStrokeWidth = strokeWidth
        return this
    }

    fun getStrokeWidth() = mStrokeWidth

    /** 背景描边颜色 */
    fun setStrokeColor(@ColorInt color: Int): EasyTextView {
        mStrokeColor = color
        return this
    }

    fun getStrokeColor() = mStrokeColor

    /** 背景描边颜色 */
    fun setStrokeColorRes(@ColorRes color: Int): EasyTextView {
        mStrokeColor = ContextCompat.getColor(context, color)
        return this
    }

    /** 背景类型 ** 半圆 or 矩形圆角 ** */
    fun setShapeBgType(shapeBgType: ShapeBgType): EasyTextView {
        mShapeBgType = shapeBgType
        return this
    }

    fun getShapeBgType() = mShapeBgType

    /** 背景填充颜色 */
    fun setSolidColor(@ColorInt color: Int): EasyTextView {
        mSolidColor = color
        return this
    }

    fun getEtvSolidColor() = mSolidColor

    /** 背景填充颜色 */
    fun setSolidColorRes(@ColorRes color: Int): EasyTextView {
        mSolidColor = ContextCompat.getColor(context, color)
        return this
    }

    /** 文本设置是否开启描边 */
    fun setTextStrokeEnable(enable: Boolean): EasyTextView {
        mTextStrokeEnable = enable
        return this
    }

    fun isTextStrokeEnabled() = mTextStrokeEnable

    /** 文本描边宽度 */
    fun setTextStrokeWidth(strokeWidth: Float): EasyTextView {
        mTextStrokeWidth = strokeWidth
        return this
    }

    fun getTextStrokeWidth() = mTextStrokeWidth

    /** 文本描边颜色 */
    fun setTextStrokeColor(@ColorInt color: Int): EasyTextView {
        mTextStrokeColor = color
        return this
    }

    /** 文本描边颜色 */
    fun setTextStrokeColorRes(@ColorRes color: Int): EasyTextView {
        mTextStrokeColor = getColor(color)
        return this
    }

    fun getTextStrokeColor() = mTextStrokeColor

    /** drawable高度位置 */
    fun setDrawableLeftMode(drawableMode: DrawableMode): EasyTextView {
        mDrawableLeftMode = drawableMode
        mDrawableChanged = true
        return this
    }

    /** drawable高度位置 */
    fun setDrawableRightMode(drawableMode: DrawableMode): EasyTextView {
        mDrawableRightMode = drawableMode
        mDrawableChanged = true
        return this
    }

    /** drawable高度位置 */
    fun setDrawableTopMode(drawableMode: DrawableMode): EasyTextView {
        mDrawableChanged = true
        mDrawableTopMode = drawableMode
        return this
    }

    /** drawable高度位置 */
    fun setDrawableBottomMode(drawableMode: DrawableMode): EasyTextView {
        mDrawableRightMode = drawableMode
        mDrawableChanged = true
        return this
    }

    /** drawable高度宽度 */
    fun setDrawableLeftSize(width: Int, height: Int): EasyTextView {
        mDrawableLeftWidth = width
        mDrawableLeftHeight = height
        mDrawableChanged = true
        return this
    }

    /** drawable高度宽度 */
    fun setDrawableTopSize(width: Int, height: Int): EasyTextView {
        mDrawableTopWidth = width
        mDrawableTopHeight = height
        mDrawableChanged = true
        return this
    }

    /** drawable高度宽度 */
    fun setDrawableRightSize(width: Int, height: Int): EasyTextView {
        mDrawableRightWidth = width
        mDrawableRightHeight = height
        mDrawableChanged = true
        return this
    }

    /** drawable高度宽度 */
    fun setDrawableBottomSize(width: Int, height: Int): EasyTextView {
        mDrawableBottomWidth = width
        mDrawableBottomHeight = height
        mDrawableChanged = true
        return this
    }


    /** 背景渐变是否开启，需要设置setShaderColor */
    fun setShaderEnable(enabled: Boolean): EasyTextView {
        mShaderEnable = enabled
        return this
    }

    fun isBgShaderEnable() = mShaderEnable

    /** 背景渐变模式 ShaderMode */
    fun setShaderMode(mode: ShaderMode): EasyTextView {
        mShaderAngle = mode
        return this
    }

    fun getShaderMode() = mShaderAngle

    /** 背景渐变颜色 */
    fun setShaderColor(
        @ColorInt startColor: Int,
        @ColorInt centerColor: Int,
        @ColorInt endColor: Int
    ): EasyTextView {
        mShaderColorArrays[0] = startColor
        mShaderColorArrays[1] = centerColor
        mShaderColorArrays[2] = endColor
        return this
    }

    /** 背景渐变颜色 */
    fun setShaderColorRes(
        @ColorRes startColor: Int,
        @ColorRes centerColor: Int,
        @ColorRes endColor: Int
    ): EasyTextView {
        mShaderColorArrays[0] = getColor(startColor)
        mShaderColorArrays[1] = getColor(centerColor)
        mShaderColorArrays[2] = getColor(endColor)
        return this
    }

    /** 文案加粗，setTextStrokeEnable需要设置false */
    fun setTextBoldWidth(boldWidth: Float): EasyTextView {
        mTextBoldWidth = boldWidth
        return this
    }

    fun getTextBoldWidth() = mTextBoldWidth

    /** 文案渐变色是否开启 */
    fun setTextShaderEnabled(textShaderEnabled: Boolean): EasyTextView {
        mTextShaderEnable = textShaderEnabled
        return this
    }

    fun isTextShaderEnabled() = mTextShaderEnable

    /** 文案渐变颜色 */
    fun setTextShaderColor(@ColorInt startColor: Int, @ColorInt endColor: Int): EasyTextView {
        mTextShaderStartColor = startColor
        mTextShaderEndColor = endColor
        return this
    }

    /** 文案渐变颜色 */
    fun setTextShaderColorRes(@ColorRes startColor: Int, @ColorRes endColor: Int): EasyTextView {
        mTextShaderStartColor = getColor(startColor)
        mTextShaderEndColor = getColor(endColor)
        return this
    }

    /** 文案渐变模式 ShaderMode */
    fun setTextShaderMode(mode: ShaderMode): EasyTextView {
        mTextShaderMode = mode
        return this
    }

    fun getTextShaderMode() = mTextShaderMode

    private fun changeDrawableLocation() {
        try {
            val drawables = compoundDrawables
            if (drawables.size != 4) return
            val drawableLeft = drawables[0]
            if (drawableLeft != null) {
                setDrawable(
                    drawableLeft,
                    0,
                    mDrawableLeftWidth,
                    mDrawableLeftHeight,
                    mDrawableLeftMode
                )
                if (mDrawableLeftWidth == 0) {
                    mDrawableLeftWidth = drawableLeft.intrinsicWidth + compoundDrawablePadding
                }
            }
            val drawableTop = drawables[1]
            if (drawableTop != null) {
                setDrawable(drawableTop, 1, mDrawableTopWidth, mDrawableTopHeight, mDrawableTopMode)
            }
            val drawableRight = drawables[2]
            if (drawableRight != null) {
                setDrawable(
                    drawableRight,
                    2,
                    mDrawableRightWidth,
                    mDrawableRightHeight,
                    mDrawableRightMode
                )
            }
            val drawableBottom = drawables[3]
            if (drawableBottom != null) {
                setDrawable(
                    drawableBottom,
                    3,
                    mDrawableBottomWidth,
                    mDrawableBottomHeight,
                    mDrawableBottomMode
                )
            }
            setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom)
        } catch (e: Exception) {
        }
    }

    private fun setDrawable(
        drawable: Drawable,
        tag: Int,
        drawableWidth: Int,
        drawableHeight: Int,
        mode: DrawableMode
    ) {
        val bounds = DrawableBoundsHelper.getDrawableBounds(
            this, drawable, mode, tag, drawableWidth, drawableHeight
        )
        drawable.setBounds(bounds[0], bounds[1], bounds[2], bounds[3])
    }

    /** 代码设置属性后需要调用此方法 */
    fun rebuild() {
        calculBgCornerRadius()
//        if (mDrawableChanged) {
        changeDrawableLocation()
//        }
        invalidate()
    }

    private fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    /**
     * 状态图的显示模式。SuperTextView定义了10中显示模式。它们控制着状态图的相对位置。
     * 默认为居中，即[DrawableMode.NORMAL]。
     */
    enum class DrawableMode(var code: Int) {
        /** 正左*/
        LEFT(0),

        /** 正上 */
        NORMAL(1),

        /** 正右 */
        RIGHT(2),

        TOP(3),

        BOTTOM(4);

        companion object {
            fun valueOf(code: Int): DrawableMode {
                for (mode in values()) {
                    if (mode.code == code) {
                        return mode
                    }
                }
                return NORMAL
            }
        }
    }

    /**
     * SuperTextView的渐变模式。
     * 可以通过 setDrawableLeftMode or setDrawableTopMode
     */
    enum class ShaderMode(var code: Int) {
        /** 从上到下 */
        TOP_TO_BOTTOM(0),

        /** 从下到上 */
        BOTTOM_TO_TOP(1),

        /**  从左到右  */
        LEFT_TO_RIGHT(2),

        /** 从右到左  */
        RIGHT_TO_LEFT(3);

        companion object {
            fun valueOf(code: Int): ShaderMode {
                for (mode in values()) {
                    if (mode.code == code) {
                        return mode
                    }
                }
                return TOP_TO_BOTTOM
            }

            fun toGradientDrawableOrientation(mode: ShaderMode): GradientDrawable.Orientation {
                return when (mode.code) {
                    TOP_TO_BOTTOM.code -> {
                        GradientDrawable.Orientation.TOP_BOTTOM
                    }
                    BOTTOM_TO_TOP.code -> {
                        GradientDrawable.Orientation.BOTTOM_TOP
                    }
                    LEFT_TO_RIGHT.code -> {
                        GradientDrawable.Orientation.LEFT_RIGHT
                    }
                    RIGHT_TO_LEFT.code -> {
                        GradientDrawable.Orientation.RIGHT_LEFT
                    }
                    else -> GradientDrawable.Orientation.TOP_BOTTOM
                }
            }
        }
    }

    enum class ShapeBgType(var code: Int) {
        CIRCLE(1),
        NORMAL(2);

        companion object {
            fun valueOf(code: Int): ShapeBgType {
                for (mode in values()) {
                    if (mode.code == code) {
                        return mode
                    }
                }
                return CIRCLE
            }
        }
    }

    enum class LineMode(var code: Int) {

        NORMAL(0),
        CENTER(1),
        BOTTOM(2);

        companion object {
            fun valueOf(code: Int): LineMode {
                for (mode in values()) {
                    if (mode.code == code) {
                        return mode
                    }
                }
                return NORMAL
            }
        }
    }
}