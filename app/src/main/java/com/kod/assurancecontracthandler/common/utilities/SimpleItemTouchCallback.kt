package com.kod.assurancecontracthandler.common.utilities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.views.main.fragmentListContracts.ContractListAdapter

class SimpleItemTouchCallback(
    private val context: Context,
    private val rvAdapter: ContractListAdapter,
    private val onSwipeCallback: (Int) -> Unit
) :
    ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT
    ) {
    private var icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_export)
    private var initiated: Boolean = false

    private fun initSwipeView() {
        initiated = true
    }

    override fun onChildDraw(
        canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        if (!initiated) {
            initSwipeView()
        }

        if (dX <= 0.0f && dX != 0.0f) {
            val intrinsicHeight = icon?.intrinsicWidth ?: 0
            val xMarkTop = itemView.top + ((itemView.bottom - itemView.top) - intrinsicHeight) / 2
            val xMarkBottom = xMarkTop + intrinsicHeight
            val backGroundColor: Int =
                MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, Color.GREEN)

            colorCanvas(
                canvas,
                backGroundColor,
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )

            drawIconOnCanVas(
                canvas, icon,
                itemView.right - 2 * (icon?.intrinsicWidth ?: 0),
                xMarkTop,
                itemView.right - (icon?.intrinsicWidth ?: 0),
                xMarkBottom
            )
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun colorCanvas(canvas: Canvas, canvasColor: Int, left: Int, top: Int, right: Int, bottom: Int) {

        val paint = Paint()
        paint.color = canvasColor
        paint.isAntiAlias = true

        val rect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        canvas.drawRoundRect(rect, 80f, 80f, paint)
    }

    private fun drawIconOnCanVas(
        canvas: Canvas, icon: Drawable?, left: Int, top: Int, right: Int, bottom: Int
    ) {
        icon?.setBounds(left, top, right, bottom)
        icon?.draw(canvas)
    }

    private fun vibrate() {
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator!!.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = true

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int
    ) {
        vibrate()
        onSwipeCallback(viewHolder.adapterPosition)
        rvAdapter.notifyItemChanged(viewHolder.adapterPosition)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.6f
    }
}