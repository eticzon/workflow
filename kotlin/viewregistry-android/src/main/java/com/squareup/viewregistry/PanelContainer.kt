package com.squareup.viewregistry

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import io.reactivex.Observable
import kotlin.reflect.jvm.jvmName

/**
 * Container view for [PanelContainerScreen].
 */
class PanelContainer
@JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null
) : AbstractModalContainer<BackStackScreen<*>>(context, attributeSet) {

  /**
   * Always returns `true`, because a view created to display one [BackStackScreen]
   * can display any [BackStackScreen].
   */
  override fun BackStackScreen<*>.matches(modalScreen: BackStackScreen<*>) = true

  override fun showDialog(
    modalScreen: BackStackScreen<*>,
    screens: Observable<out BackStackScreen<*>>,
    viewRegistry: ViewRegistry
  ): Dialog {
    val binding = viewRegistry.getBinding<BackStackScreen<*>>(modalScreen::class.jvmName)
    val view = binding.buildView(screens, viewRegistry, this)

    val dialog = AlertDialog.Builder(context)
        .apply {
          setView(view)
          setCancelable(false)
        }
        .show()

    val displayMetrics = context.displayMetrics
    val layoutParams = WindowManager.LayoutParams()
        .apply { copyFrom(dialog.window.attributes) }

    if (context.isTablet) {
      if (context.isPortrait) {
        layoutParams.width = displayMetrics.widthPixels
        layoutParams.height = (displayMetrics.heightPixels * 0.5f).toInt()
      } else {
        layoutParams.width = (displayMetrics.widthPixels * 0.5f).toInt()
        layoutParams.height = displayMetrics.heightPixels
      }
    } else {
      layoutParams.width = displayMetrics.widthPixels
      layoutParams.height = displayMetrics.heightPixels
    }

    dialog.window.attributes = layoutParams
    return dialog
  }

  companion object : ViewBinding<PanelContainerScreen<*>>
  by BuilderBinding(
      type = PanelContainerScreen::class.java,
      builder = { screens, viewRegistry, context, _ ->
        PanelContainer(context).apply {
          layoutParams = (ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
          takeScreens(screens, viewRegistry)
        }
      }
  )
}
