package com.hva.hboict.lab42

import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.hva.hboict.lab42.databinding.ActivityMainBinding

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null
    private var humanEngager: HumanEngager? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)

    }

    private fun speach(phrase: String, pitch: Int, speed: Int) {
        val locale = Locale(Language.DUTCH, Region.NETHERLANDS)
        val pitchS = "\\vct=$pitch\\"
        val speedS = "\\rspd=$speed\\"
        val phraseBuild = Phrase("$speedS  $pitchS  $phrase")
        println(phraseBuild)
        val say: Say = SayBuilder.with(qiContext)
            .withPhrase(phraseBuild)
            .withLocale(locale)
            .build()

        return say.run()
    }

    private fun ageSpeach(age: Int): Boolean {
        return age > 18
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
        humanEngager = HumanEngager(this.qiContext!!, 100)
        humanEngager?.onInteracting = Consumer {


            if (it != null) {
                // Create a phrase.
                val estemAge = it.estimatedAge.toString()
                val result = estemAge.filter { it.isDigit() }
                val age = Integer.parseInt(result)

                println(ageSpeach(age))
                val lingo = "\\vct=20\\\\rspd=50\\G-R-A-T-I-S   \\pau=750\\    \\rspd=70\\gratis"


                val phrasesOld = listOf(
                    "U ziet er goed uit vandaag",
                    "Welkom in dit mooie pand, leuk dat u er bent",
                    "Ik heet u welkom op de U-FAA",
                    "De U-FAA blijft zich uitbreiden",
                    "De techniek hier staat nooit stil",
                    "Dit gebouw is echt mooi modern",
                    "Wist u dat het getal 42 veelvoorkomend is in de wetenschap, het zou het ultieme antwoord op het leven zijn."
                )
                val phrasesYoung = listOf(
                    "Er zitten hier veel pokemons",
                    "Hey jij ziet er mooi uit!", "Welkom op de U-FAA", "Wat leuk dat je hier bent",
                    "Hoe noem je een oude sneeuwpop? ........... Water!"
                )

                if (age > 0) {
                    if (ageSpeach(age)) {
                        val randomElement = phrasesOld.random()
                        speach(randomElement, 100, 80)
                    } else {
                        val randomElement = phrasesYoung.random()
                        speach((randomElement), 100, 80)
                    }

                }
            }

        }
        humanEngager?.start()
    }

    override fun onRobotFocusLost() {}

    override fun onRobotFocusRefused(reason: String?) {}

}

