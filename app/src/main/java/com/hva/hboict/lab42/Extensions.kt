package com.hva.hboict.lab42

import android.content.res.Resources
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.human.*
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.builder.ApproachHumanBuilder
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import java.util.*

class HumanEngager(private val qiContext: QiContext, private val unengageTimeMs: Int) {

    private val awareness: HumanAwareness = qiContext.humanAwareness
    // Inner working of engaging system
    private var engaging = false
    private var queuedRecommendedHuman: Human? = null
    private var disengageTimerTask: TimerTask? = null
    var onInteracting: Consumer<Human>? = null
    private var currentTimestamp = System.currentTimeMillis()
    private var trigger = true



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

        awareness.async().addOnRecommendedHumanToApproachChangedListener { human: Human? ->
            // Build the action.
            // Robot will approach human (ride to him)

            val arrayTxtGreeting = if (isDutch) {
                Resources.getSystem().getStringArray(R.array.phrases_greeting_array)
            } else {
                Resources.getSystem().getStringArray(R.array.phrases_greeting_array_english)
            }

            val randomElement = arrayTxtGreeting[Random().nextInt(arrayTxtGreeting.size)].toString()
            val phrase = Phrase(randomElement)

            val locale = if (isDutch) {
                Locale(Language.DUTCH, Region.NETHERLANDS)
            } else {
                Locale(Language.ENGLISH, Region.UNITED_KINGDOM)
            }
            val say: Say = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .withLocale(locale)
                .build()

            if (human != null) {
                val approachHuman = ApproachHumanBuilder.with(qiContext)
                    .withHuman(human)
                    .build()
                // Run the action asynchronously.
                approachHuman.async().run()
            }

            if (trigger) {
                say.async().run()
                trigger = false
                currentTimestamp = System.currentTimeMillis()
            }
            if (System.currentTimeMillis() - currentTimestamp > 20000) {
                trigger = true
            }
        }
    }
}

