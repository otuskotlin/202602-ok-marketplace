import kotlin.time.Clock

actual fun currentDate(): DateString {
    return DateString(Clock.System.now().toString())
}
