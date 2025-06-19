package com.mraihanfauzii.restrokotlin.utils

import com.mraihanfauzii.restrokotlin.model.ExercisePlan
import com.squareup.moshi.Types.newParameterizedType

object Types {
    val EXERCISE_LIST = newParameterizedType(
        List::class.java,
        ExercisePlan::class.java
    )
}