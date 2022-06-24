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
import java.util.*

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
            if (it == null) {
                return@Consumer
            }
            // Create a phrase.
            val estemAge = it.estimatedAge.toString()
            val result = estemAge.filter { it.isDigit() }
            val age = it.estimatedAge.years // Integer.parseInt(result)


            val lingo = "\\vct=20\\\\rspd=50\\G-R-A-T-I-S   \\pau=750\\    \\rspd=70\\gratis"

            val arrayTxtYoung = resources.getStringArray(R.array.phrases_young_array)
            val arrayTxtOld = resources.getStringArray(R.array.phrases_old_array)

            if (age < 0) {
                return@Consumer
            }

            if (ageSpeach(age)) {
                speach(arrayTxtOld[Random().nextInt(arrayTxtOld.size)].toString(), 100, 80)
            } else {
                speach(arrayTxtYoung[Random().nextInt(arrayTxtYoung.size)].toString(), 100, 80)
            }
        }
        humanEngager?.start()
    }

    override fun onRobotFocusLost() {}

    override fun onRobotFocusRefused(reason: String?) {}

}

