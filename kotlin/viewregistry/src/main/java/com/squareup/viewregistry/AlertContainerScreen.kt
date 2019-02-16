package com.squareup.viewregistry

data class AlertContainerScreen<B : Any>(
  override val baseScreen: B,
  override val modals: List<AlertScreen> = emptyList()
) : IsModalContainerScreen<B, AlertScreen> {
  constructor(
    baseScreen: B,
    modal: AlertScreen
  ) : this(baseScreen, listOf(modal))

  constructor(
    baseScreen: B,
    vararg modals: AlertScreen
  ) : this(baseScreen, modals.toList())
}

fun <M : Any> BackStackScreen<M>.toAlertContainerScreen():
    AlertContainerScreen<BackStackScreen<M>> = AlertContainerScreen(this)
