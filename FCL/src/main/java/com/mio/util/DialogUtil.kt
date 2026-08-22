package com.mio.util

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.mio.dialog.ItemSelectionDialog
import com.tungsten.fcl.R
import com.tungsten.fcl.databinding.DialogRuleErrorBinding
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog
import com.tungsten.fcllibrary.component.dialog.FCLBaseAppCompatDialog
import com.tungsten.fcllibrary.component.dialog.FCLDialog
import kotlin.system.exitProcess

fun showErrorDialog(context: Context, message: Int, vararg args: String?) {
    showErrorDialog(context, context.getString(message, *args))
}

fun showErrorDialog(context: Context, message: String) {
    FCLAlertDialog.Builder(context)
        .setAlertLevel(FCLAlertDialog.AlertLevel.ALERT)
        .setMessage(message)
        .setNegativeButton(context.getString(R.string.dialog_positive)) { }
        .setCancelable(false)
        .create()
        .show()
}

@JvmOverloads
fun showItemSelectionDialog(
    context: Context,
    title: String = "",
    items: List<String>,
    small: Boolean = true,
    callback: (Int, String) -> Unit
) {
    ItemSelectionDialog(context, title, items,small) { position, item ->
        callback(position, item)
    }.show()
}

fun showWarningDialog(context: Context, message: String, onConfirm: () -> Unit) {
    FCLAlertDialog.Builder(context)
        .setAlertLevel(FCLAlertDialog.AlertLevel.INFO)
        .setMessage(message)
        .setPositiveButton {
            onConfirm()
        }
        .setNegativeButton {

        }
        .create()
        .show()
}

fun Context.showErrorTips(message: String = "") {
    FCLBaseAppCompatDialog.Builder(this, DialogRuleErrorBinding::inflate)
        .setCancelOnBackPressed(false)
        .setCancelOnTouchOutside(false)
        .setHeightPercent(0.7F)
        .setWidthPercent(0.6F)
        .onInitView { binding ->
            binding.tips.text = message
            binding.cancel.text = getString(R.string.dialog_positive)
            binding.cancel.setOnClickListener { exitProcess(0) }
            binding.confirm.visibility = View.GONE

            val params = binding.cancel.layoutParams as ConstraintLayout.LayoutParams
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToStart = ConstraintLayout.LayoutParams.UNSET
            binding.cancel.layoutParams = params
        }
        .show()
}