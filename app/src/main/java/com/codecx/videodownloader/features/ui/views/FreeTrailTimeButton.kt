package com.codecx.videodownloader.features.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.core.view.marginEnd
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.toDp
import com.codecx.videodownloader.databinding.FreeTrailTileButtonBinding
import com.google.android.material.card.MaterialCardView

class FreeTrailTimeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: FreeTrailTileButtonBinding = FreeTrailTileButtonBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        cardElevation = 0f
        setCardBackgroundColor(Color.TRANSPARENT)
        strokeWidth = 1.toDp(context)
        radius = 24.toDp(context).toFloat()

        context.withStyledAttributes(
            attrs,
            R.styleable.FreeTrailTimeButton,
            defStyleAttr,
            R.style.Theme_VideoDownloader
        ) {
            binding.title.text = getString(R.styleable.FreeTrailTimeButton_title) ?: ""
            binding.des.text = getString(R.styleable.FreeTrailTimeButton_des) ?: ""
            binding.icon.setImageResource(
                getResourceId(
                    R.styleable.FreeTrailTimeButton_icon,
                    R.drawable.new_download_icon
                )
            )

            val sColor = when {
                hasValue(R.styleable.FreeTrailTimeButton_tile_strokeColor) -> {
                    getColor(R.styleable.FreeTrailTimeButton_tile_strokeColor, "#1AF51F70".toColorInt())
                }
                hasValue(R.styleable.FreeTrailTimeButton_strokeColor) -> {
                    getColor(R.styleable.FreeTrailTimeButton_strokeColor, "#1AF51F70".toColorInt())
                }
                else -> "#1AF51F70".toColorInt()
            }
            setStrokeColor(ColorStateList.valueOf(sColor))

            val viewColorInt = getColor(R.styleable.FreeTrailTimeButton_view_color, "#1AF51F70".toColorInt())
            binding.viewColor.backgroundTintList = ColorStateList.valueOf(viewColorInt)

            val startColor = getColor(R.styleable.FreeTrailTimeButton_tile_backgroundStartColor, "#FFF0F5".toColorInt())
            val endColor = getColor(R.styleable.FreeTrailTimeButton_tile_backgroundEndColor, "#F51F70".toColorInt())

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(startColor, endColor)
            ).apply {
                cornerRadius = 24.toDp(context).toFloat()
            }
            binding.root.background = gradientDrawable

            val iconStartColor = getColor(R.styleable.FreeTrailTimeButton_tile_imageStartColor, "#FF337E".toColorInt())
            val iconEndColor = getColor(R.styleable.FreeTrailTimeButton_tile_imageEndColor, "#E91E63".toColorInt())

            val iconGradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(iconStartColor, iconEndColor)
            ).apply {
                shape = GradientDrawable.OVAL
            }
            binding.imageCard.background = iconGradient
        }
    }
}
