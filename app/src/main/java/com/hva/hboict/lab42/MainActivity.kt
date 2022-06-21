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
import com.aldebaran.qi.sdk.builder.ApproachHumanBuilder
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

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
        humanEngager = HumanEngager(this.qiContext!!, 100)
        humanEngager?.onInteracting = Consumer {

            if (it != null) {
                // Create a phrase.
                val locale: Locale = Locale(Language.DUTCH, Region.NETHERLANDS)
                val phrase: Phrase = Phrase("Je ziet eruit als een ${it.estimatedGender}")

                // Build the action.
                val say: Say = SayBuilder.with(qiContext)
                    .withPhrase(phrase)
                    .withLocale(locale)
                    .build()

                // Run the action synchronously.
                say.run()

                val approachHuman = ApproachHumanBuilder.with(qiContext)
                    .withHuman(it)
                    .build()

                // Run the action asynchronously.
                approachHuman.async().run()
            }

        }
        humanEngager?.start()
    }

    override fun onRobotFocusLost() {}

    override fun onRobotFocusRefused(reason: String?) {}

}