package com.squareup.viewregistry

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.squareup.coordinators.Coordinator
import com.squareup.coordinators.Coordinators
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.jvm.jvmName

/**
 * Base class for containers that show [ModalContainerScreen.modals] in [Dialog]s
 */
abstract class AbstractModalContainer<M : Any>
@JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet), HandlesBack {
  private val base: View? get() = getChildAt(0)
  private var dialogs: List<DialogRef<M>> = emptyList()
  private val takeScreensSubs = CompositeDisposable()

  private val attached = BehaviorSubject.create<Boolean>()

  // Kind of a hack: use a Coordinator to create observable attached property.
  // TODO should probably just drop Coordinators for RxBinding.
  init {
    // TODO fix compiler warning by moving this to onAttachedToWindow()
    Coordinators.bind(this) {
      object : Coordinator() {
        override fun attach(view: View) {
          attached.onNext(true)
        }

        override fun detach(view: View) {
          attached.onNext(false)

          // TODO(https://github.com/square/workflow/issues/51)
          // Not good enough, the stupid Activity cleans it up and shames us about "leaks" in logcat
          // before this point. Try to use a lifecycle observer to clean that up.
          dialogs.forEach { it.dialog.hide() }
          dialogs = emptyList()
        }
      }
    }
  }

  override fun onBackPressed(): Boolean {
    // This should only be hit if there are no dialogs showing, so we only
    // need to consider the body.
    return (base as? HandlesBack)?.onBackPressed() == true
  }

  fun takeScreens(
    screens: Observable<out ModalContainerScreen<*, M>>,
    viewRegistry: ViewRegistry
  ) {
    takeScreensSubs.clear()

    // This looks like we're leaking subscriptions to screens, since we only unsubscribe
    // if takeScreens() is called again. It's fine, though:  the switchMap in whileAttached()
    // ensures that we're only subscribed to it while this view is attached to a window.

    // Create a new body view each time the type of [AlertContainerScreen.base] changes.
    takeScreensSubs.add(screens
        .whileAttached()
        .distinctUntilChanged { containerScreen -> containerScreen.baseScreen::class }
        .subscribe {
          removeAllViews()
          addView(it.viewForBase(screens, viewRegistry, this))
        }
    )

    // Compare the new modals list to the set of dialogs already on display. Show
    // any new ones, throw away any stale ones.
    takeScreensSubs.add(screens
        .whileAttached()
        .subscribe { containerScreen ->
          val newDialogs = mutableListOf<DialogRef<M>>()
          for ((i, modal) in containerScreen.modals.withIndex()) {
            newDialogs += if (dialogs.size < i && dialogs[i].matches(modal)) {
              dialogs[i]
            } else {
              DialogRef<M>(modal, containerScreen.showDialog(i))
            }
          }

          (dialogs - newDialogs).forEach { it.dialog.hide() }
          dialogs = newDialogs
        }
    )
  }

  protected open fun DialogRef<M>.matches(modalScreen: M): Boolean = true

  protected abstract fun showDialog(modalScreen: M): Dialog

  private fun ModalContainerScreen<*, M>.showDialog(index: Int): Dialog = showDialog(modals[index])

  private fun <T> Observable<T>.whileAttached(): Observable<T> =
    attached.switchMap { isAttached -> if (isAttached) this else Observable.never<T>() }

  protected class DialogRef<M>(
    val screen: M,
    val dialog: Dialog
  )

  private fun <T : Any> ModalContainerScreen<T, *>.viewForBase(
    containerScreens: Observable<out ModalContainerScreen<*, *>>,
    viewRegistry: ViewRegistry,
    container: ViewGroup
  ): View {
    val baseScreens: Observable<out T> = containerScreens.mapToBaseMatching(this)
    val binding: ViewBinding<T> = viewRegistry.getBinding(baseScreen::class.jvmName)
    return binding.buildView(baseScreens, viewRegistry, container)
  }

  private fun <T : Any> Observable<out ModalContainerScreen<*, *>>.mapToBaseMatching(
    screen: ModalContainerScreen<T, *>
  ): Observable<out T> {
    return map { it.baseScreen }.ofType(screen.baseScreen::class.java)
  }
}
