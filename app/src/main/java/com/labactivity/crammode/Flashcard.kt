package com.labactivity.crammode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Flashcard(
    val question: String = "",
    val answer: String = ""
) : Parcelable

