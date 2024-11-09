import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.OutputStream

@Composable
@Preview
fun App() {
    var symptomsInput by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var precautions by remember { mutableStateOf(listOf<String>()) }
    var medications by remember { mutableStateOf(listOf<String>()) }
    var diets by remember { mutableStateOf(listOf<String>()) }
    var workouts by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF2196F3),
            primaryVariant = Color(0xFF1976D2),
            secondary = Color(0xFF03DAC5)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    "Medical Prediction System",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Input Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = symptomsInput,
                            onValueChange = { symptomsInput = it },
                            label = { Text("Enter symptoms") },
                            placeholder = { Text("e.g., fever, headache, nausea") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    fetchDiagnosis(symptomsInput)?.let {
                                        diagnosis = it.getString("disease")
                                        description = it.getString("description")
                                        precautions = it.getJSONArray("precautions").map { precaution -> precaution.toString() }
                                        medications = it.getJSONArray("medications").map { medication -> medication.toString() }
                                        diets = it.getJSONArray("diets").map { diet -> diet.toString() }
                                        workouts = it.getJSONArray("workouts").map { workout -> workout.toString() }
                                    } ?: run {
                                        diagnosis = "Error retrieving diagnosis"
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Get Diagnosis")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Results Section
                if (diagnosis.isNotEmpty()) {
                    ResultSection(
                        title = "Diagnosis",
                        content = diagnosis,
                        color = Color(0xFF1976D2)
                    )

                    ResultSection(
                        title = "Description",
                        content = description,
                        color = Color(0xFF2196F3)
                    )

                    if (precautions.isNotEmpty()) {
                        ListSection(
                            title = "Precautions",
                            items = precautions,
                            color = Color(0xFF03DAC5)
                        )
                    }

                    if (medications.isNotEmpty()) {
                        ListSection(
                            title = "Medications",
                            items = medications,
                            color = Color(0xFF00BCD4)
                        )
                    }

                    if (diets.isNotEmpty()) {
                        ListSection(
                            title = "Recommended Diet",
                            items = diets,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    if (workouts.isNotEmpty()) {
                        ListSection(
                            title = "Recommended Workouts",
                            items = workouts,
                            color = Color(0xFF8BC34A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultSection(title: String, content: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ListSection(title: String, items: List<String>, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Keeping the existing fetchDiagnosis function unchanged
suspend fun fetchDiagnosis(symptoms: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("http://127.0.0.1:4000/predict")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonInputString = JSONObject().put("symptoms", symptoms).toString()
            connection.outputStream.use { os: OutputStream ->
                os.write(jsonInputString.toByteArray())
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                JSONObject(response)
            } else {
                println("Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Medical Prediction System"
    ) {
        App()
    }
}