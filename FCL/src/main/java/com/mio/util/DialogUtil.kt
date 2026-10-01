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
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CancellationException
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
    selectedIndex: Int = -1,
    callback: (Int, String) -> Unit
) {
    ItemSelectionDialog(context, title, items, small, selectedIndex) { position, item ->
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
        .setHeightPercent(0.644F)
        .setWidthPercent(0.555F)
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

fun errRuleDialog(context: Context, msg: String?, url: URL?, future: CompletableFuture<*>) {
    FCLBaseAppCompatDialog.Builder(context, DialogRuleErrorBinding::inflate)
        .setCancelOnBackPressed(false)
        .setCancelOnTouchOutside(false)
        .setHeightPercent(0.7F)
        .setWidthPercent(0.7F)
        .onInitView { binding ->

            binding.tips.text = msg ?: "当前设置规则不满足该版本要求，请根据提示修改！"
            if(url != null) {
                binding.confirm.text = "下载"
                binding.confirm.setOnClickListener {
                    openLink(context, url.toString())
                    future.completeExceptionally(CancellationException("由于用户设置不满足规则，取消本次启动"))
                    dismiss()
                }
            }else {
                binding.confirm.visibility = View.GONE

                val params = binding.cancel.layoutParams as ConstraintLayout.LayoutParams
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToStart = ConstraintLayout.LayoutParams.UNSET
                binding.cancel.layoutParams = params
            }

            binding.cancel.setOnClickListener {
                future.completeExceptionally(CancellationException("用户强行终止了启动"))
                dismiss()
            }
        }
        .show()
}