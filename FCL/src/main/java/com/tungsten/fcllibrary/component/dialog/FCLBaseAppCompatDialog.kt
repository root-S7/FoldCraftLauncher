package com.tungsten.fcllibrary.component.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.tungsten.fcl.R

/**
 * 对AppCompatDialog的一个封装，默认采用非全屏的主题
 * 注意，如果cancelOnTouchOutside和cancelOnBackPressed均设置的是1
 * 那么在style中务必要设置android:windowIsFloating为false，否则会造成状态栏和导航栏视觉上奇怪问题
 */
abstract class FCLBaseAppCompatDialog<VB : ViewBinding> protected constructor(
    context: Context,
    themeResId: Int = R.style.Dialog_Default,
    private val inflate: (LayoutInflater) -> VB,
    private val builder: Builder<VB>
) : FCLDialog(context, themeResId), DefaultLifecycleObserver {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding ?: throw IllegalStateException("DialogBinding已销毁或未初始化")

    val fullMode = (builder.widthPercent == 1f) && (builder.heightPercent == 1f)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super<FCLDialog>.onCreate(savedInstanceState)
        //if(fullMode) window?.enableEdgeToEdgeCompat()

        setCancelable(builder.cancelOnBackPressed)
        setCanceledOnTouchOutside(builder.cancelOnTouchOutside)
        _binding = inflate(layoutInflater)
        if(fullMode) adaptPadding()

        setContentView(binding.root)
        ((context as? LifecycleOwner) ?: (context as? ContextWrapper)?.baseContext as? LifecycleOwner)?.lifecycle?.addObserver(this)
        initView()
    }

    protected abstract fun initView()

    protected fun adaptPadding() {
        val initialPadding = Rect(
            binding.root.paddingLeft,
            binding.root.paddingTop,
            binding.root.paddingRight,
            binding.root.paddingBottom
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                initialPadding.left + bars.left,
                initialPadding.top + bars.top,
                initialPadding.right + bars.right,
                initialPadding.bottom + bars.bottom
            )

            insets
        }
    }

    override fun onStart() {
        super<FCLDialog>.onStart()

        val window = window ?: return
        if (!fullMode) {
            val metrics = context.resources.displayMetrics
            val params = window.attributes

            builder.widthPercent?.let {
                require(it in 0f..1f) { "dialogWidthPercent 必须在 0~1 之间" }
                params.width = (metrics.widthPixels * it).toInt()
            }
            builder.heightPercent?.let {
                require(it in 0f..1f) { "dialogHeightPercent 必须在 0~1 之间" }
                params.height = (metrics.heightPixels * it).toInt()
            }
            window.attributes = params
        }
        //else window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
        if(isShowing) dismiss()
    }

    @CallSuper
    override fun show() {
        val activity = context as? Activity
        if(activity != null && (activity.isFinishing || activity.isDestroyed)) return
        if(!isShowing) super.show()
    }

    @CallSuper
    override fun dismiss() {
        if(!isShowing) return

        _binding = null
        builder.onInitView(null)
        window?.decorView?.setOnApplyWindowInsetsListener(null)
        super.dismiss()
    }

    class Builder<VB : ViewBinding>(private val context: Context, private val inflate: (LayoutInflater) -> VB) {
        @StyleRes internal var themeResId: Int = 0
            private set
        internal var cancelOnTouchOutside: Boolean = false
            private set
        internal var cancelOnBackPressed: Boolean = false
            private set
        internal var widthPercent: Float? = null
            private set
        internal var heightPercent: Float? = null
            private set
        internal var initBlock: (FCLBaseAppCompatDialog<VB>.(VB) -> Unit)? = null
            private set

        fun setTheme(@StyleRes themeResId: Int) = apply { this.themeResId = themeResId }

        fun setCancelOnTouchOutside(cancel: Boolean) = apply { this.cancelOnTouchOutside = cancel }

        fun setCancelOnBackPressed(cancel: Boolean) = apply { this.cancelOnBackPressed = cancel }

        fun setWidthPercent(percent: Float) = apply { this.widthPercent = percent }

        fun setHeightPercent(percent: Float) = apply { this.heightPercent = percent }

        fun onInitView(block: (FCLBaseAppCompatDialog<VB>.(binding: VB) -> Unit)? = null) = apply { this.initBlock = block }

        fun build(): FCLBaseAppCompatDialog<VB> = object : FCLBaseAppCompatDialog<VB>(context, resolveThemeResId(), inflate, this) {
            override fun initView() {
                initBlock?.invoke(this, binding)
            }
        }

        fun show(): FCLBaseAppCompatDialog<VB> = build().apply { show() }

        @StyleRes
        private fun resolveThemeResId(): Int {
            if(themeResId != 0) return themeResId

            val isFullScreen = widthPercent == 1f && heightPercent == 1f
            return if (isFullScreen) R.style.Dialog_Full_Default else R.style.Dialog_Default
        }
    }
}