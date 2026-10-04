package com.example.fileconverter.util

import android.content.Context

object AppPolicyManager {
    private const val PREFS_NAME = "filebodlai_policy"
    private const val KEY_AGREEMENT_ACCEPTED = "user_agreement_accepted_v1"

    fun isAgreementAccepted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AGREEMENT_ACCEPTED, false)
    }

    fun setAgreementAccepted(context: Context, accepted: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AGREEMENT_ACCEPTED, accepted).apply()
    }
}
