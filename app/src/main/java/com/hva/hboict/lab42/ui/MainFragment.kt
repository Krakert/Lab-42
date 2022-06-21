package com.hva.hboict.lab42.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hva.hboict.lab42.databinding.FragmentMainBinding
import com.hva.hboict.lab42.model.Bubble
import java.util.*
import java.util.concurrent.ThreadLocalRandom

private const val MAX_SIZE = 70F
private const val MIN_SIZE = 5F
private const val MAX_SPEED = 0.5F
private const val AMOUNT_BUBBLES = 15

@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    var bgImage: ImageView? = null
    private var bubbles = arrayListOf<Bubble>()
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bgImage = binding.bgBubble
        animateBackground()

    }

    private fun animateBackground() {
        generateBubbles()
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {

                // Get the display size
                val displayMetrics = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)

                // Create bitmap with a canvas size of the screen
                val bitmap = Bitmap.createBitmap(
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    Bitmap.Config.ARGB_4444
                )
                val canvas = Canvas(bitmap)

                // Paint????
                val paint = Paint()
                paint.color = Color.rgb(253, 182, 91)
                paint.style = Paint.Style.FILL
                paint.isAntiAlias = true
                paint.isDither = true
                for ((x, y, _, size) in bubbles) {
                    canvas.drawCircle(x, y, size, paint)
                }
                activity?.runOnUiThread {
                    bgImage!!.background = BitmapDrawable(
                        resources,
                        bitmap
                    )
                }
                updateBubblePositions()
            }
        }, 0, 10)
    }


    private fun generateBubbles() {
        for (i in 0 until AMOUNT_BUBBLES) {
            // Get the display size
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
            val spacer = displayMetrics.widthPixels / AMOUNT_BUBBLES * i
            val offset = ThreadLocalRandom.current().nextInt(spacer - 50, spacer + 150).toFloat()
            bubbles.add(generateBubble(offset))
        }
    }

    private fun generateBubble(offsetLeft: Float): Bubble {
        // Get the display size
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        val size = (MIN_SIZE.toInt()..MAX_SIZE.toInt()).random().toFloat()
        val speed = (size - MAX_SIZE) / (MIN_SIZE - MAX_SIZE) * MAX_SPEED
        return Bubble(
            x = offsetLeft,
            y = (displayMetrics.heightPixels + ThreadLocalRandom.current().nextInt(100, 500)).toFloat(),
            speed = speed,
            size = size
        )
    }


    private fun updateBubblePositions() {
        for (i in bubbles.indices) {
            val bubble: Bubble = bubbles[i]
            bubble.y -= bubble.speed
            if (bubble.y < -50) bubbles[i] = generateBubble(bubble.x)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}