package org.mcxa.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet


/**
 * A simple [Fragment] subclass.
 * Use the [CycleOverview.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleOverview : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_overview, container, false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CycleOverview.
         */
        fun newInstance(): CycleOverview {
            val fragment = CycleOverview()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}

// this class draws the custom graphic for showing the cycle
// it is not designed to be used outside of this fragment
class CycleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val paintRed = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintGrey = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paintRed.color = Color.parseColor("#E53935")
        paintRed.style = Paint.Style.FILL
        paintGrey.color = Color.parseColor("#E0E0E0")
        paintGrey.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)

        if (mode == MeasureSpec.EXACTLY)
            return size
        if (mode == MeasureSpec.AT_MOST)
            return size
        else
            return dpOrSpToPx(context, 52.toFloat()).toInt()
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)

        if (mode == MeasureSpec.EXACTLY)
            return size
        else
            return dpOrSpToPx(context, 24.toFloat()).toInt()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(0.toFloat(),0.toFloat(),width.toFloat(),height.toFloat(), paintGrey)
        canvas?.drawRect(0.toFloat(),0.toFloat(),width * 5 / 28.toFloat(),height.toFloat(), paintRed)
    }

    // convert a dp or sp value to px
    private fun dpOrSpToPx(context: Context, dpOrSpValue: Float): Float {
        return dpOrSpValue * context.resources.displayMetrics.density
    }
}
