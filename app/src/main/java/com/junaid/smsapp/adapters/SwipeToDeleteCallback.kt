package com.junaid.smsapp.adapters

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.junaid.smsapp.R
import android.graphics.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.RectF
import android.graphics.BitmapFactory










class SwipeToDeleteCallback(private val mAdapter: ConversationAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var iconDelete: Bitmap? = null
    private var iconArchive: Bitmap? = null
    private var backgroundDelete: ColorDrawable? = null
    private var backgroundArchive: ColorDrawable? = null
    private  var p = Paint()


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    init {
        iconDelete = drawableToBitmap(ContextCompat.getDrawable(mAdapter.context, R.drawable.ic_delete_white_24dp)!!)
        iconArchive = drawableToBitmap(ContextCompat.getDrawable(mAdapter.context, R.drawable.ic_archive_white_24dp)!!)
        backgroundDelete = ColorDrawable(Color.RED)
        backgroundArchive = ColorDrawable(Color.BLUE)

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            mAdapter.deleteItem(position)
            Log.d("TAG", "onSwiped: left ${mAdapter.data.size}  $position")
        }else{
            mAdapter.archiveItem(position)
        }

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20


        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
        val width = height / 3
        if (dX > 0) { // Swiping to the right
            val p = Paint()
            p.color = Color.parseColor("#4c4cff")
            val background = RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                dX,
                itemView.bottom.toFloat()
            )
            c.drawRect(background, p)
            val icon_dest = RectF(
                itemView.left.toFloat() + width,
                itemView.top.toFloat() + width,
                itemView.left.toFloat() + 2 * width,
                itemView.bottom.toFloat() - width
            )
            c.drawBitmap(iconArchive!!, null, icon_dest, p)

//            val iconLeft = itemView.left + iconMargin + iconArchive!!.intrinsicWidth
//            val iconRight = itemView.left + iconMargin
//            iconArchive?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
//
//            backgroundArchive?.setBounds(
//                itemView.left, itemView.top,
//                itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
//            )
//            backgroundArchive?.draw(c)
//            iconArchive?.draw(c)

        } else if (dX < 0) { // Swiping to the left

            p.setColor(Color.parseColor("#D32F2F"))
            val background = RectF(
                itemView.right.toFloat() + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            c.drawRect(background, p)
            val icon_dest = RectF(
                itemView.right.toFloat() - 2 * width,
                itemView.top.toFloat() + width,
                itemView.right.toFloat() - width,
                itemView.bottom.toFloat() - width
            )
            c.drawBitmap(iconDelete!!, null, icon_dest, p)
        }
//        backgroundDelete?.draw(c)
//        iconDelete?.draw(c)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


   private fun drawableToBitmap(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

}