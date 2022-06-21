package com.hva.hboict.lab42

import android.content.ContentValues.TAG
import android.util.Log
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.human.*
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.builder.ApproachHumanBuilder
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder
import java.util.*

class HumanEngager(
    private val qiContext: QiContext, // Inner state, from which state is calculated
    private val unengageTimeMs: Int
) {
    private val awareness: HumanAwareness = qiContext.humanAwareness

    // Inner working of engaging system
    private var engaging = false
    private var queuedRecommendedHuman: Human? = null
    private var disengageTimerTask: TimerTask? = null
    var onInteracting: Consumer<Human>? = null

    /* Internal; notify listener of "isInteracting" state.
	 */
    private fun setIsInteracting(human: Human?) {
        if (onInteracting != null) {
            try {
                onInteracting!!.consume(human)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    /* Internal; processes a recommended candidate for engagement by creating an Engage action.
	 */
    private fun tryToEngageHuman(human: Human?) {
        if (human != null) {
            engaging = true

            // Get the characteristics, and log them.
            Log.i(TAG, "Age: ${human.estimatedAge.years} year(s)")
            Log.i(TAG, "Gender: ${human.estimatedGender}")
            Log.i(TAG, "Pleasure state: ${human.emotion.pleasure}")
            Log.i(TAG, "Excitement state: ${human.emotion.excitement}")
            Log.i(TAG, "Engagement state: ${human.engagementIntention}")
            Log.i(TAG, "Smile state: ${human.facialExpressions.smile}")
            Log.i(TAG, "Attention state: ${human.attention}")

            val engage = EngageHumanBuilder.with(qiContext).withHuman(human).build()
            engage.addOnHumanIsEngagedListener {
                setIsInteracting(
                    human
                )
            }
            engage.async().run().thenConsume {
                engaging = false
                // Try again with a new human
                tryToEngageHuman(queuedRecommendedHuman)
                queuedRecommendedHuman = null
                // This listener could never be called any more, but leaving it risks a memory leak
                engage.removeAllOnHumanIsEngagedListeners()
            }
        } else {
            // No human to engage - BUT we give a timeout
            disengageTimerTask = object : TimerTask() {
                override fun run() {
                    setIsInteracting(null)
                }
            }
            Timer("disengage").schedule(disengageTimerTask, unengageTimeMs.toLong())
        }
    }

    /* Start tracking and engaging humans.
	 */
    fun start() {
        awareness.async().addOnRecommendedHumanToEngageChangedListener { recommendedHuman: Human? ->
            if (!engaging) {
                // Engage with human
                tryToEngageHuman(recommendedHuman)
            } else {
                queuedRecommendedHuman = recommendedHuman
            }
        }
        awareness.async().recommendedHumanToEngage.andThenConsume { human: Human? ->
            tryToEngageHuman(human)
        }

        awareness.async().addOnRecommendedHumanToApproachChangedListener { human: Human ->
            // Build the action.
            // Robot will approach human (ride to him)
            val approachHuman = ApproachHumanBuilder.with(qiContext)
                .withHuman(human)
                .build()

            // Run the action asynchronously.
            approachHuman.async().run()
        }
    }

    /* Start tracking and engaging humans.
	 */
    fun stop() {
        awareness.removeAllOnRecommendedHumanToEngageChangedListeners()
        if (disengageTimerTask != null) {
            disengageTimerTask!!.cancel()
        }
    } // Internal API

}

