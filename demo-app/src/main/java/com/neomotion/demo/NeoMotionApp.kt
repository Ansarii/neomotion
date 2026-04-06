package com.neoninnovationlab.neomotion.demo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that triggers Hilt's code generation.
 * No logic here — Hilt handles everything via @AndroidEntryPoint on Activities/Fragments.
 */
@HiltAndroidApp
class NeoMotionApp : Application()
