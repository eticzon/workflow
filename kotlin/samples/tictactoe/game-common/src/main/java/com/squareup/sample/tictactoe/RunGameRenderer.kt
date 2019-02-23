/*
 * Copyright 2017 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sample.tictactoe

import com.squareup.sample.tictactoe.RunGameEvent.ConfirmQuit
import com.squareup.sample.tictactoe.RunGameEvent.ContinuePlaying
import com.squareup.sample.tictactoe.RunGameState.MaybeQuitting
import com.squareup.sample.tictactoe.RunGameState.MaybeQuittingForSure
import com.squareup.sample.tictactoe.RunGameState.Playing
import com.squareup.viewregistry.AlertContainerScreen
import com.squareup.viewregistry.AlertScreen
import com.squareup.viewregistry.AlertScreen.Button.NEGATIVE
import com.squareup.viewregistry.AlertScreen.Button.NEUTRAL
import com.squareup.viewregistry.AlertScreen.Button.POSITIVE
import com.squareup.viewregistry.AlertScreen.Event
import com.squareup.viewregistry.AlertScreen.Event.ButtonClicked
import com.squareup.viewregistry.AlertScreen.Event.Canceled
import com.squareup.viewregistry.BackStackScreen
import com.squareup.viewregistry.EventHandlingScreen.Companion.ignoreEvents
import com.squareup.workflow.Renderer
import com.squareup.workflow.WorkflowInput
import com.squareup.workflow.WorkflowPool
import com.squareup.workflow.adaptEvents
import com.squareup.workflow.render

object RunGameRenderer :
    Renderer<RunGameState, RunGameEvent, AlertContainerScreen<BackStackScreen<*>>> {

  override fun render(
    state: RunGameState,
    workflow: WorkflowInput<RunGameEvent>,
    workflows: WorkflowPool
  ): AlertContainerScreen<BackStackScreen<*>> {
    return when (state) {

      is Playing -> {
        return TakeTurnsRenderer
            .render(state.takingTurns, workflows)
            .let { AlertContainerScreen(it) }
      }

      is RunGameState.NewGame -> AlertContainerScreen(
          BackStackScreen(NewGameScreen(workflow::sendEvent))
      )

      is MaybeQuitting -> AlertContainerScreen(
          BackStackScreen(GamePlayScreen(state.completedGame.lastTurn, ignoreEvents())),
          maybeQuitScreen(workflow)
      )

      is MaybeQuittingForSure -> AlertContainerScreen(
          BackStackScreen(GamePlayScreen(state.completedGame.lastTurn, ignoreEvents())),
          maybeQuitScreen(workflow),
          maybeQuitScreen(workflow, "Really?", "Yes God damn it!", "Sigh, no")
      )

      is RunGameState.GameOver -> AlertContainerScreen(
          BackStackScreen(GameOverScreen(state, workflow::sendEvent))
      )
    }
  }

  private fun maybeQuitScreen(
    workflow: WorkflowInput<RunGameEvent>,
    message: String = "Do you really want to concede the game?",
    positive: String = "I Quit",
    negative: String = "No"
  ): AlertScreen {
    return AlertScreen(
        workflow.adaptEvents<Event, RunGameEvent> { alertEvent ->
          when (alertEvent) {
            is ButtonClicked -> when (alertEvent.button) {
              POSITIVE -> ConfirmQuit
              NEGATIVE -> ContinuePlaying
              NEUTRAL -> throw IllegalArgumentException()
            }
            Canceled -> ContinuePlaying
          }
        }::sendEvent,
        buttons = mapOf(
            POSITIVE to positive,
            NEGATIVE to negative
        ),
        message = message
    )
  }
}