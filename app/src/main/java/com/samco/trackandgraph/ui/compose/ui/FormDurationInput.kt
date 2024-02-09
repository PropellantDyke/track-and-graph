/*
 *  This file is part of Track & Graph
 *
 *  Track & Graph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Track & Graph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Track & Graph.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.samco.trackandgraph.ui.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.samco.trackandgraph.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormDurationInputPart(
    modifier: Modifier = Modifier,
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    description: String,
    focusManager: FocusManager? = LocalFocusManager.current,
    focusRequester: FocusRequester? = null,
    keyboardController: SoftwareKeyboardController? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        FormTextInput(
            modifier = Modifier,
            textFieldValue = textFieldValue,
            onValueChange = onValueChange,
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            textAlign = TextAlign.Center,
            isNumeric = true
        )
        Text(
            text = description,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = MaterialTheme.typography.body2.fontWeight,
            color = colorResource(id = R.color.form_text_note)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormDurationInput(
    hoursFieldValue: TextFieldValue,
    minutesFieldValue: TextFieldValue,
    secondsFieldValue: TextFieldValue,
    onHoursValueChange: (TextFieldValue) -> Unit,
    onMinutesValueChange: (TextFieldValue) -> Unit,
    onSecondsValueChange: (TextFieldValue) -> Unit,
    focusManager: FocusManager? = null,
    focusRequester: FocusRequester? = null,
    keyboardController: SoftwareKeyboardController? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.form_section_padding)))
    {
        FormDurationInputPart(
            modifier = Modifier.weight(1.0F),
            textFieldValue = hoursFieldValue,
            onValueChange = onHoursValueChange,
            stringResource(id = R.string.hours),
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
        )
        FormDurationInputPart(
            modifier = Modifier.weight(1.0F),
            textFieldValue = minutesFieldValue,
            onValueChange = onMinutesValueChange,
            stringResource(id = R.string.minutes),
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
        )
        FormDurationInputPart(
            modifier = Modifier.weight(1.0F),
            textFieldValue = secondsFieldValue,
            onValueChange = onSecondsValueChange,
            stringResource(id = R.string.seconds),
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
        )
    }
}
