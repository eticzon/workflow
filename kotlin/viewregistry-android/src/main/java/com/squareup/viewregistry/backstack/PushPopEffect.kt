package com.squareup.viewregistry.backstack

import android.support.transition.Fade
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.squareup.viewregistry.BackStackScreen
import com.squareup.viewregistry.ViewRegistry
import com.squareup.viewregistry.backstack.ViewStateStack.Direction
import com.squareup.viewregistry.backstack.ViewStateStack.Direction.PUSH
import com.squareup.viewregistry.viewOrNull
import io.reactivex.Observable

/**
 * Performs a pretty lame push or pop animation, just to prove that we can.
 */
object PushPopEffect : BackStackEffect {
  /**
   * Does not rely on [from] to have a [View.backStackKey].
   */
  override fun execute(
    from: View,
    to: BackStackScreen<*>,
    screens: Observable<out BackStackScreen<*>>,
    viewRegistry: ViewRegistry,
    container: ViewGroup,
    setUpNewView: (View) -> Unit,
    direction: Direction
  ) {
    val newScene = to.sceneForWrappedScreen(screens, viewRegistry, container) { scene ->
      scene.viewOrNull()
          ?.let { setUpNewView(it) }
    }

    val outEdge = if (direction == PUSH) Gravity.START else Gravity.END
    val inEdge = if (direction == PUSH) Gravity.END else Gravity.START

    val outSet = TransitionSet()
        .addTransition(Slide(outEdge).addTarget(from))
        .addTransition(Fade(Fade.OUT))

    val fullSet = TransitionSet()
        .addTransition(outSet)
        .addTransition(Slide(inEdge).excludeTarget(from, true))

    TransitionManager.go(newScene, fullSet)
  }
}
