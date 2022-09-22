/*
 * Copyright (c) 2022 New Vector Ltd
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

package im.vector.app.features.settings.devices.v2.overview

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.test.MvRxTestRule
import im.vector.app.features.settings.devices.v2.DeviceFullInfo
import im.vector.app.features.settings.devices.v2.IsCurrentSessionUseCase
import im.vector.app.features.settings.devices.v2.verification.CheckIfCurrentSessionCanBeVerifiedUseCase
import im.vector.app.test.test
import im.vector.app.test.testDispatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

private const val A_SESSION_ID = "session-id"

class SessionOverviewViewModelTest {

    @get:Rule
    val mvRxTestRule = MvRxTestRule(testDispatcher = testDispatcher)

    private val args = SessionOverviewArgs(
            deviceId = A_SESSION_ID
    )
    private val isCurrentSessionUseCase = mockk<IsCurrentSessionUseCase>()
    private val getDeviceFullInfoUseCase = mockk<GetDeviceFullInfoUseCase>()
    private val checkIfCurrentSessionCanBeVerifiedUseCase = mockk<CheckIfCurrentSessionCanBeVerifiedUseCase>()

    private fun createViewModel() = SessionOverviewViewModel(
            initialState = SessionOverviewViewState(args),
            isCurrentSessionUseCase = isCurrentSessionUseCase,
            getDeviceFullInfoUseCase = getDeviceFullInfoUseCase,
            checkIfCurrentSessionCanBeVerifiedUseCase = checkIfCurrentSessionCanBeVerifiedUseCase,
    )

    @Test
    fun `given the viewModel has been initialized then viewState is updated with session info`() {
        // Given
        val deviceFullInfo = mockk<DeviceFullInfo>()
        every { getDeviceFullInfoUseCase.execute(A_SESSION_ID) } returns flowOf(deviceFullInfo)
        val isCurrentSession = true
        every { isCurrentSessionUseCase.execute(any()) } returns isCurrentSession
        val expectedState = SessionOverviewViewState(
                deviceId = A_SESSION_ID,
                isCurrentSession = isCurrentSession,
                deviceInfo = Success(deviceFullInfo)
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.test()
                .assertLatestState { state -> state == expectedState }
                .finish()
        verify {
            isCurrentSessionUseCase.execute(A_SESSION_ID)
            getDeviceFullInfoUseCase.execute(A_SESSION_ID)
        }
    }

    @Test
    fun `given current session can be verified when handling verify current session action then self verification event is posted`() {
        // Given
        val deviceFullInfo = mockk<DeviceFullInfo>()
        every { getDeviceFullInfoUseCase.execute(A_SESSION_ID) } returns flowOf(deviceFullInfo)
        every { isCurrentSessionUseCase.execute(any()) } returns true
        val verifySessionAction = SessionOverviewAction.VerifySession
        coEvery { checkIfCurrentSessionCanBeVerifiedUseCase.execute() } returns true

        // When
        val viewModel = createViewModel()
        val viewModelTest = viewModel.test()
        viewModel.handle(verifySessionAction)

        // Then
        viewModelTest
                .assertEvent { it is SessionOverviewViewEvent.SelfVerification }
                .finish()
        coVerify {
            checkIfCurrentSessionCanBeVerifiedUseCase.execute()
        }
    }

    @Test
    fun `given current session cannot be verified when handling verify current session action then reset secrets event is posted`() {
        // Given
        val deviceFullInfo = mockk<DeviceFullInfo>()
        every { getDeviceFullInfoUseCase.execute(A_SESSION_ID) } returns flowOf(deviceFullInfo)
        every { isCurrentSessionUseCase.execute(any()) } returns true
        val verifySessionAction = SessionOverviewAction.VerifySession
        coEvery { checkIfCurrentSessionCanBeVerifiedUseCase.execute() } returns false

        // When
        val viewModel = createViewModel()
        val viewModelTest = viewModel.test()
        viewModel.handle(verifySessionAction)

        // Then
        viewModelTest
                .assertEvent { it is SessionOverviewViewEvent.PromptResetSecrets }
                .finish()
        coVerify {
            checkIfCurrentSessionCanBeVerifiedUseCase.execute()
        }
    }
}
