package com.squareup.viewregistry

/**
 * May show a stack of [AlertScreen] over a [baseScreen].
 *
 * @param B the type of [baseScreen]
 */
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

fun <B : Any> BackStackScreen<B>.toAlertContainerScreen():
    AlertContainerScreen<BackStackScreen<B>> = AlertContainerScreen(this)
