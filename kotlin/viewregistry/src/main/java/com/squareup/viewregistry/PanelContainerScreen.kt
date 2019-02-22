package com.squareup.viewregistry

data class PanelContainerScreen<B : Any>(
  override val baseScreen: B,
  override val modals: List<BackStackScreen<*>> = emptyList()
) : IsModalContainerScreen<B, BackStackScreen<*>> {
  constructor(
    baseScreen: B,
      modal: BackStackScreen<*>
  ) : this(baseScreen, listOf(modal))

  constructor(
    baseScreen: B,
      vararg modals: BackStackScreen<*>
  ) : this(baseScreen, modals.toList())
}

fun <B: Any> BackStackScreen<B>.toPanelContainerScreen():
    PanelContainerScreen<BackStackScreen<B>> = PanelContainerScreen(this)
