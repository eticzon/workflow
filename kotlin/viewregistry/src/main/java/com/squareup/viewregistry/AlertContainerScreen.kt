package com.squareup.viewregistry

data class AlertContainerScreen<B : Any>(
  override val baseScreen: B,
  override val modals: List<AlertScreen> = emptyList()
) : ModalContainerScreen<B, AlertScreen> {
  constructor(
    baseScreen: B,
      modal: AlertScreen
  ) : this(baseScreen, listOf(modal))
}

fun <M : Any> BackStackScreen<M>.toAlertContainerScreen():
    AlertContainerScreen<BackStackScreen<M>> = AlertContainerScreen(this)
