/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.healthconnect.codelab.presentation.screen.exercisesession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.example.healthconnect.codelab.R
import com.example.healthconnect.codelab.presentation.component.ExerciseSessionRow
import com.example.healthconnect.codelab.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Shows a list of [ExerciseSessionRecord]s from today.
 */
@Composable
fun ExerciseSessionScreen(
  permissions: Set<String>,
  permissionsGranted: Boolean,
  backgroundReadPermissions: Set<String>,
  backgroundReadAvailable: Boolean,
  backgroundReadGranted: Boolean,
  historyReadPermissions: Set<String>,
  historyReadAvailable: Boolean,
  historyReadGranted: Boolean,
  onBackgroundReadClick: () -> Unit = {},
  sessionsList: List<ExerciseSessionRecord>,
  uiState: ExerciseSessionViewModel.UiState,
  onInsertClick: () -> Unit = {},
  onDetailsClick: (String) -> Unit = {},
  onError: (Throwable?) -> Unit = {},
  onPermissionsResult: () -> Unit = {},
  onPermissionsLaunch: (Set<String>) -> Unit = {},
) {

  // Remember the last error ID, such that it is possible to avoid re-launching the error
  // notification for the same error when the screen is recomposed, or configuration changes etc.
  val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

  LaunchedEffect(uiState) {
    // If the initial data load has not taken place, attempt to load the data.
    if (uiState is ExerciseSessionViewModel.UiState.Uninitialized) {
      onPermissionsResult()
    }

    // The [ExerciseSessionViewModel.UiState] provides details of whether the last action was a
    // success or resulted in an error. Where an error occurred, for example in reading and
    // writing to Health Connect, the user is notified, and where the error is one that can be
    // recovered from, an attempt to do so is made.
    if (uiState is ExerciseSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
      onError(uiState.exception)
      errorId.value = uiState.uuid
    }
  }

  if (uiState != ExerciseSessionViewModel.UiState.Uninitialized) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (!permissionsGranted) {
        item {
          Button(
            onClick = {
              onPermissionsLaunch(permissions)
            }
          ) {
            Text(text = stringResource(R.string.permissions_button_label))
          }
        }
      } else {
        item {
          Button(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .padding(4.dp),
            onClick = {
              onInsertClick()
            }
          ) {
            Text(stringResource(id = R.string.insert_exercise_session))
          }
        }
        if (!backgroundReadGranted) {
          item {
            Button(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(4.dp),
              onClick = {
                onPermissionsLaunch(backgroundReadPermissions)
              },
              enabled = backgroundReadAvailable,
            ) {
              if (backgroundReadAvailable){
                Text(stringResource(R.string.request_background_read))
              } else {
                Text(stringResource(R.string.background_read_is_not_available))
              }
            }
          }
        } else {
          item {
            Button(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(4.dp),
              onClick = {
                onBackgroundReadClick()
              },
            ) {
              Text(stringResource(R.string.read_steps_in_background))
            }
          }
        }

        if (!historyReadGranted) {
          item {
            Button(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(4.dp),
              onClick = {
                onPermissionsLaunch(historyReadPermissions)
              },
              enabled = historyReadAvailable,
            ) {
              if (historyReadAvailable){
                Text(stringResource(R.string.request_history_read))
              } else {
                Text(stringResource(R.string.history_read_is_not_available))
              }
            }
          }
        }

        items(sessionsList) { session ->
          ExerciseSessionRow(
            ZonedDateTime.ofInstant(session.startTime, session.startZoneOffset),
            ZonedDateTime.ofInstant(session.endTime, session.endZoneOffset),
            session.metadata.id,
            session.metadata.dataOrigin.packageName,
            session.title ?: stringResource(R.string.no_title),
            onDetailsClick = { uid ->
              onDetailsClick(uid)
            }
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun ExerciseSessionScreenPreview() {
  HealthConnectTheme {
    val runningStartTime = ZonedDateTime.now()
    val runningEndTime = runningStartTime.plusMinutes(30)
    val walkingStartTime = ZonedDateTime.now().minusMinutes(120)
    val walkingEndTime = walkingStartTime.plusMinutes(30)
    ExerciseSessionScreen(
      permissions = setOf(),
      permissionsGranted = true,
      backgroundReadPermissions = setOf(),
      backgroundReadAvailable = false,
      backgroundReadGranted = false,
      historyReadPermissions = setOf(),
      historyReadAvailable = true,
      historyReadGranted = false,
      sessionsList = listOf(
        ExerciseSessionRecord(
          metadata = Metadata.manualEntryWithId(UUID.randomUUID().toString()),
          exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
          title = "Running",
          startTime = runningStartTime.toInstant(),
          startZoneOffset = runningStartTime.offset,
          endTime = runningEndTime.toInstant(),
          endZoneOffset = runningEndTime.offset,
        ),
        ExerciseSessionRecord(
          metadata = Metadata.manualEntryWithId(UUID.randomUUID().toString()),
          exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_WALKING,
          title = "Walking",
          startTime = walkingStartTime.toInstant(),
          startZoneOffset = walkingStartTime.offset,
          endTime = walkingEndTime.toInstant(),
          endZoneOffset = walkingEndTime.offset,
        )
      ),
      uiState = ExerciseSessionViewModel.UiState.Done
    )
  }
}
