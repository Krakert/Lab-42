package com.hva.hboict.lab42.ui

import android.annotation.SuppressLint
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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hva.hboict.lab42.R
import com.hva.hboict.lab42.databinding.FragmentMainBinding
import com.hva.hboict.lab42.model.Bubble
import com.hva.hboict.lab42.model.Direction
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ThreadLocalRandom

private const val MAX_SIZE = 70F
private const val MIN_SIZE = 5F
private const val MAX_SPEED = 0.9F
private const val AMOUNT_BUBBLES = 15

@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    var bgImage: ImageView? = null
    private var bubbles = arrayListOf<Bubble>()
    private var _binding: FragmentMainBinding? = null
    private val displayMetrics = DisplayMetrics()
    private var spacer = displayMetrics.widthPixels / AMOUNT_BUBBLES * (0..AMOUNT_BUBBLES).random()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bgImage = binding.bgBubble
        animateBackground()
        animateWelcomeMessage()

        view.setOnTouchListener { _, event ->
            var indexToPop = -1
            bubbles.forEachIndexed { index, bubble ->
                val xMin = bubble.x - bubble.size
                val yMin = bubble.y - bubble.size
                if (event.x > xMin && event.x < bubble.x + bubble.size && event.y > yMin && event.y < bubble.y + bubble.size) {
                   indexToPop = index
                }
            }
            if (indexToPop != -1) {
                bubbles.removeAt(indexToPop)
                if (bubbles.size < AMOUNT_BUBBLES) {
                    bubbles.add(
                        generateBubble(
                            ThreadLocalRandom.current().nextInt(spacer - 50, spacer + 150).toFloat()
                        )
                    )
                }
            }
            false
        }


    }

    private fun animateWelcomeMessage() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {

                    val animation = AlphaAnimation(1.0f, 0.0f)
                    animation.duration = 400
                    animation.repeatCount = 1
                    animation.repeatMode = Animation.REVERSE

                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) { }
                        override fun onAnimationStart(animation: Animation?) { }
                        override fun onAnimationRepeat(animation: Animation?) {
                            val arrayWelcomeTxt = resources.getStringArray(R.array.Welcome_array)
                            binding.txtWelcome.text = arrayWelcomeTxt[Random().nextInt(arrayWelcomeTxt.size)].toString()
                        }
                    })

                    binding.txtWelcome.startAnimation(animation)
                }
            }
        }, 0, 1000 * 5)
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

                val paint = Paint()
                paint.color = Color.rgb(253, 182, 91)
                paint.style = Paint.Style.FILL
                paint.isAntiAlias = true
                paint.isDither = true
                activity?.runOnUiThread {
                    for ((x, y, _, _, size) in bubbles) {
                        canvas.drawCircle(x, y, size, paint)
                    }
                    bgImage!!.background = BitmapDrawable(resources, bitmap)
                }
                MainScope().launch {
                    updateBubblePositions()
                }
            }
        }, 0, 20)
    }

    private fun generateBubbles() {
        for (i in 0 until AMOUNT_BUBBLES) {
            // Get the display size
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
            spacer = displayMetrics.widthPixels / AMOUNT_BUBBLES * i
            val offset = ThreadLocalRandom.current().nextInt(spacer - 50, spacer + 150).toFloat()
            bubbles.add(generateBubble(offset))
        }
    }

    private fun generateBubble(offsetLeft: Float): Bubble {
        activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        val size = (MIN_SIZE.toInt()..MAX_SIZE.toInt()).random().toFloat()
        val speedY = (size - MAX_SIZE) / (MIN_SIZE - MAX_SIZE) * MAX_SPEED
        val direction = Direction.values()[Random().nextInt(Direction.values().size)]
        return Bubble(
            x = offsetLeft,
            y = (displayMetrics.heightPixels + ThreadLocalRandom.current()
                .nextInt(size.toInt(), size.toInt() + 50)).toFloat(),
            speedY = speedY,
            speedX = Random().nextFloat(),
            size = size,
            direction = direction
        )
    }


    private fun updateBubblePositions() {
        for (i in bubbles.indices) {
            val bubble: Bubble = bubbles[i]
            bubble.y -= bubble.speedY

            when (bubble.direction) {
                Direction.LEFT -> bubble.x -= bubble.speedX
                Direction.RIGHT -> bubble.x += bubble.speedX
            }

            when {
                bubble.x < 0 + bubble.size -> bubble.direction = Direction.RIGHT
                bubble.x > displayMetrics.widthPixels - bubble.size -> bubble.direction = Direction.LEFT
            }

            if (bubble.y < -50) bubbles[i] = generateBubble(bubble.x)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}