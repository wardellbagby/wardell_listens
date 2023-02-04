import com.wardellbagby.listens.Target.Micropub
import com.wardellbagby.listens.httpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

/**
 * Create a new port for the Micropub site specified by [Micropub.endpoint] with the text contained
 * in [message].
 */
suspend fun Micropub.post(message: String) {
  httpClient.submitForm<Unit>(
    url = endpoint,
    formParameters = Parameters.build {
      append("h", "entry")
      append("content", message)
      append("access_token", accessToken)
    }
  )
}