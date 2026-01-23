package org.vinaygopinath.launchchat.unittest.screens.main.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import org.vinaygopinath.launchchat.helpers.UsernameHelper
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.screens.main.domain.LaunchChatUseCase
import org.vinaygopinath.launchchat.screens.main.domain.LaunchChatUseCase.Companion.LaunchChatResult
import org.vinaygopinath.launchchat.screens.main.domain.PrefixCountryCodeUseCase
import java.time.Instant

@RunWith(JUnit4::class)
class LaunchChatUseCaseTest {

    private val phoneNumberHelper = mock<PhoneNumberHelper>()
    private val usernameHelper = mock<UsernameHelper>()
    private val prefixCountryCodeUseCase = mock<PrefixCountryCodeUseCase>()

    private val useCase = LaunchChatUseCase(
        phoneNumberHelper = phoneNumberHelper,
        usernameHelper = usernameHelper,
        prefixCountryCodeUseCase = prefixCountryCodeUseCase
    )

    private fun buildChatApp(
        identifierType: ChatApp.IdentifierType = ChatApp.IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME,
        launchType: ChatApp.LaunchType = ChatApp.LaunchType.BOTH_URL_AND_INTENT
    ): ChatApp {
        return ChatApp(
            id = 1,
            name = "TestApp",
            identifierType = identifierType,
            launchType = launchType,
            intentPackageSelection = "com.test.app",
            phoneNumberLaunchIntent = "test://phone/[phone-number]",
            phoneNumberLaunchUrl = "https://test.com/[phone-number]",
            usernameLaunchIntent = "test://user/[username]",
            usernameLaunchUrl = "https://test.com/[username]",
            createdAt = Instant.now(),
            deletedAt = null,
            isPredefined = true,
            isEnabled = true
        )
    }

    @Test
    fun `returns NoIdentifierFoundError when input contains neither phone number nor username`() = runTest {
        whenever(phoneNumberHelper.containsPhoneNumbers("random text")).thenReturn(false)
        whenever(usernameHelper.containsUsernames("random text")).thenReturn(false)

        val result = useCase.execute(
            inputString = "random text",
            chatApp = buildChatApp(),
            messageInputString = null
        )

        assertThat(result).isEqualTo(LaunchChatResult.NoIdentifierFoundError)
    }

    @Test
    fun `returns PhoneNumberLaunch when input contains single phone number and chat app supports phone numbers`() = runTest {
        val phoneNumber = "+1234567890"
        whenever(phoneNumberHelper.containsPhoneNumbers(phoneNumber)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(phoneNumber)).thenReturn(listOf(phoneNumber))
        whenever(prefixCountryCodeUseCase.execute(phoneNumber)).thenReturn(phoneNumber)

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.PHONE_NUMBER_ONLY)

        val result = useCase.execute(
            inputString = phoneNumber,
            chatApp = chatApp,
            messageInputString = null
        )

        assertThat(result).isInstanceOf(LaunchChatResult.PhoneNumberLaunch::class.java)
        val launch = result as LaunchChatResult.PhoneNumberLaunch
        assertThat(launch.phoneNumber).isEqualTo(phoneNumber)
        assertThat(launch.message).isNull()
        assertThat(launch.chatApp).isEqualTo(chatApp)
    }

    @Test
    fun `returns PhoneNumberLaunch with message when message is provided`() = runTest {
        val phoneNumber = "+1234567890"
        val message = "Hello there!"
        whenever(phoneNumberHelper.containsPhoneNumbers(phoneNumber)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(phoneNumber)).thenReturn(listOf(phoneNumber))
        whenever(prefixCountryCodeUseCase.execute(phoneNumber)).thenReturn(phoneNumber)

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.PHONE_NUMBER_ONLY)

        val result = useCase.execute(
            inputString = phoneNumber,
            chatApp = chatApp,
            messageInputString = message
        )

        assertThat(result).isInstanceOf(LaunchChatResult.PhoneNumberLaunch::class.java)
        val launch = result as LaunchChatResult.PhoneNumberLaunch
        assertThat(launch.message).isEqualTo(message)
    }

    @Test
    fun `returns PhoneNumberLaunch with null message when message is blank`() = runTest {
        val phoneNumber = "+1234567890"
        whenever(phoneNumberHelper.containsPhoneNumbers(phoneNumber)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(phoneNumber)).thenReturn(listOf(phoneNumber))
        whenever(prefixCountryCodeUseCase.execute(phoneNumber)).thenReturn(phoneNumber)

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.PHONE_NUMBER_ONLY)

        val result = useCase.execute(
            inputString = phoneNumber,
            chatApp = chatApp,
            messageInputString = "   "
        )

        assertThat(result).isInstanceOf(LaunchChatResult.PhoneNumberLaunch::class.java)
        val launch = result as LaunchChatResult.PhoneNumberLaunch
        assertThat(launch.message).isNull()
    }

    @Test
    fun `returns MultiplePhoneNumbersDetected when input contains multiple phone numbers`() = runTest {
        val inputText = "Call +1234567890 or +0987654321"
        val phoneNumbers = listOf("+1234567890", "+0987654321")
        whenever(phoneNumberHelper.containsPhoneNumbers(inputText)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(inputText)).thenReturn(phoneNumbers)

        val result = useCase.execute(
            inputString = inputText,
            chatApp = buildChatApp(),
            messageInputString = null
        )

        assertThat(result).isInstanceOf(LaunchChatResult.MultiplePhoneNumbersDetected::class.java)
        val detected = result as LaunchChatResult.MultiplePhoneNumbersDetected
        assertThat(detected.phoneNumbers).isEqualTo(phoneNumbers)
    }

    @Test
    fun `returns ChatAppDoesNotSupportPhoneNumbersError when chat app only supports usernames`() = runTest {
        val phoneNumber = "+1234567890"
        whenever(phoneNumberHelper.containsPhoneNumbers(phoneNumber)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(phoneNumber)).thenReturn(listOf(phoneNumber))

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.USERNAME_ONLY)

        val result = useCase.execute(
            inputString = phoneNumber,
            chatApp = chatApp,
            messageInputString = null
        )

        assertThat(result).isEqualTo(LaunchChatResult.ChatAppDoesNotSupportPhoneNumbersError)
    }

    @Test
    fun `returns UsernameLaunch when input contains single username and chat app supports usernames`() = runTest {
        val username = "johndoe"
        whenever(phoneNumberHelper.containsPhoneNumbers(username)).thenReturn(false)
        whenever(usernameHelper.containsUsernames(username)).thenReturn(true)
        whenever(usernameHelper.extractUsernames(username)).thenReturn(listOf(username))

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.USERNAME_ONLY)

        val result = useCase.execute(
            inputString = username,
            chatApp = chatApp,
            messageInputString = null
        )

        assertThat(result).isInstanceOf(LaunchChatResult.UsernameLaunch::class.java)
        val launch = result as LaunchChatResult.UsernameLaunch
        assertThat(launch.username).isEqualTo(username)
        assertThat(launch.message).isNull()
        assertThat(launch.chatApp).isEqualTo(chatApp)
    }

    @Test
    fun `returns UsernameLaunch with message when message is provided`() = runTest {
        val username = "johndoe"
        val message = "Hello!"
        whenever(phoneNumberHelper.containsPhoneNumbers(username)).thenReturn(false)
        whenever(usernameHelper.containsUsernames(username)).thenReturn(true)
        whenever(usernameHelper.extractUsernames(username)).thenReturn(listOf(username))

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.USERNAME_ONLY)

        val result = useCase.execute(
            inputString = username,
            chatApp = chatApp,
            messageInputString = message
        )

        assertThat(result).isInstanceOf(LaunchChatResult.UsernameLaunch::class.java)
        val launch = result as LaunchChatResult.UsernameLaunch
        assertThat(launch.message).isEqualTo(message)
    }

    @Test
    fun `returns MultipleUsernamesDetected when input contains multiple usernames`() = runTest {
        val inputText = "contact johndoe or janedoe"
        val usernames = listOf("johndoe", "janedoe")
        whenever(phoneNumberHelper.containsPhoneNumbers(inputText)).thenReturn(false)
        whenever(usernameHelper.containsUsernames(inputText)).thenReturn(true)
        whenever(usernameHelper.extractUsernames(inputText)).thenReturn(usernames)

        val result = useCase.execute(
            inputString = inputText,
            chatApp = buildChatApp(),
            messageInputString = null
        )

        assertThat(result).isInstanceOf(LaunchChatResult.MultipleUsernamesDetected::class.java)
        val detected = result as LaunchChatResult.MultipleUsernamesDetected
        assertThat(detected.usernames).isEqualTo(usernames)
    }

    @Test
    fun `returns ChatAppDoesNotSupportUsernamesError when chat app only supports phone numbers`() = runTest {
        val username = "johndoe"
        whenever(phoneNumberHelper.containsPhoneNumbers(username)).thenReturn(false)
        whenever(usernameHelper.containsUsernames(username)).thenReturn(true)
        whenever(usernameHelper.extractUsernames(username)).thenReturn(listOf(username))

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.PHONE_NUMBER_ONLY)

        val result = useCase.execute(
            inputString = username,
            chatApp = chatApp,
            messageInputString = null
        )

        assertThat(result).isEqualTo(LaunchChatResult.ChatAppDoesNotSupportUsernamesError)
    }

    @Test
    fun `phone numbers take precedence over usernames when input contains both`() = runTest {
        val inputText = "+1234567890"
        whenever(phoneNumberHelper.containsPhoneNumbers(inputText)).thenReturn(true)
        whenever(phoneNumberHelper.extractPhoneNumbers(inputText)).thenReturn(listOf(inputText))
        whenever(prefixCountryCodeUseCase.execute(inputText)).thenReturn(inputText)
        whenever(usernameHelper.containsUsernames(inputText)).thenReturn(true)

        val chatApp = buildChatApp(identifierType = ChatApp.IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME)

        val result = useCase.execute(
            inputString = inputText,
            chatApp = chatApp,
            messageInputString = null
        )

        assertThat(result).isInstanceOf(LaunchChatResult.PhoneNumberLaunch::class.java)
    }

    @Test
    fun `executeAfterSinglePhoneNumberSelection returns PhoneNumberLaunch with prefixed phone number`() = runTest {
        val rawPhoneNumber = "1234567890"
        val prefixedPhoneNumber = "+11234567890"
        whenever(prefixCountryCodeUseCase.execute(rawPhoneNumber)).thenReturn(prefixedPhoneNumber)

        val chatApp = buildChatApp()
        val message = "Hello!"

        val result = useCase.executeAfterSinglePhoneNumberSelection(
            selectedPhoneNumber = rawPhoneNumber,
            chatApp = chatApp,
            messageInputString = message
        )

        assertThat(result).isInstanceOf(LaunchChatResult.PhoneNumberLaunch::class.java)
        val launch = result as LaunchChatResult.PhoneNumberLaunch
        assertThat(launch.phoneNumber).isEqualTo(prefixedPhoneNumber)
        assertThat(launch.message).isEqualTo(message)
        assertThat(launch.chatApp).isEqualTo(chatApp)
    }
}
