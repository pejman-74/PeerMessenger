package com.peer_messanger.util

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.TriangleEdgeTreatment
import com.google.android.material.snackbar.Snackbar
import com.peer_messanger.R
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "peerMassage"

const val selfUserDatabaseId = "0"
const val PERMISSION_REQUEST_CODE = 110
const val REQUEST_ENABLE_BT = 100

fun Context.singleButtonAlertDialog(
    message: String,
    positiveButtonText: String,
    action: (() -> Unit)? = null
): AlertDialog {
    return MaterialAlertDialogBuilder(
        this,
        R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
    ).setTitle(R.string.app_name)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            action?.let {
                it()
            }
            dialog.dismiss()
        }.setCancelable(false)
        .show()
}

fun Context.doubleButtonAlertDialog(
    message: String,
    positiveButtonText: String,
    negativeButtonText: String,
    positiveButtonAction: (() -> Unit)? = null,
    negativeButtonAction: (() -> Unit)? = null
): AlertDialog {
    return MaterialAlertDialogBuilder(
        this,
        R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
    ).setTitle(R.string.app_name)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            positiveButtonAction?.let {
                it()
            }
            dialog.dismiss()
        }.setNegativeButton(negativeButtonText) { dialog, _ ->
            negativeButtonAction?.let {
                it()
            }
            dialog.dismiss()
        }
        .setCancelable(true)
        .show()
}

fun MaterialCardView.setTailLength(location: String, length: Float) {
    val edgeTreatment = TriangleEdgeTreatment(length, false)
    doOnLayout {
        when (location) {
            "left" -> {
                val offsetEdgeTreatment =
                    OffsetEdgeTreatment(edgeTreatment, (height / 2).toFloat())
                shapeAppearanceModel =
                    shapeAppearanceModel.toBuilder().setLeftEdge(offsetEdgeTreatment)
                        .build()
            }
            "right" -> {
                val offsetEdgeTreatment =
                    OffsetEdgeTreatment(edgeTreatment, (height / 2).inv().toFloat())
                shapeAppearanceModel =
                    shapeAppearanceModel.toBuilder().setRightEdge(offsetEdgeTreatment).build()

            }
        }
    }
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun View.snackBar(message: String) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    snackBar.setLocation(Gravity.BOTTOM)
    snackBar.show()
}

private fun Snackbar.setLocation(gravity: Int) {
    val view = this.view
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.gravity = gravity
    view.layoutParams = params
}

fun String.toLocalTime(): String {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        .parse(this.replace("Z", "+00:00"))
    date?.let {
        val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getDefault()
        return dateFormatter.format(date)
    }
    return this
}

fun getCurrentUTCDateTime() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date()) + "Z"

