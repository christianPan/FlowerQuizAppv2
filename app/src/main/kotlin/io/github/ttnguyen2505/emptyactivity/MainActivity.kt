package io.github.ttnguyen2505.emptyactivity

package com.example.flowerquiz // Replace with your actual package name

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// --- ROOM DATABASE MODEL ---
@Entity(tableName = "flowers")
data class Flower(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val german: String,
    val latin: String,
    val family: String,
    val imageUri: String? = null // New image field
)

@Dao
interface FlowerDao {
    @Query("SELECT * FROM flowers")
    suspend fun getAllFlowers(): List<Flower>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlower(flower: Flower)
}

@Database(entities = [Flower::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flowerDao(): FlowerDao
    
    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "flower_db")
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}

// --- VIEWMODEL ---
class FlowerViewModel(private val dao: FlowerDao) : ViewModel() {
    private val _flowerList = MutableStateFlow<List<Flower>>(emptyList())
    val flowerList: StateFlow<List<Flower>> = _flowerList

    // Quiz State
    var shuffledQuizList by mutableStateOf(listOf<Flower>())
    var currentQuizIndex by mutableStateOf(0)
    var isQuizActive by mutableStateOf(false)

    init {
        loadDatabase()
    }

    private fun loadDatabase() {
        viewModelScope.launch {
            _flowerList.value = dao.getAllFlowers()
        }
    }

    fun addFlower(german: String, latin: String, family: String, imageUri: String?) {
        viewModelScope.launch {
            val newFlower = Flower(german = german.trim(), latin = latin.trim(), family = family.trim(), imageUri = imageUri)
            dao.insertFlower(newFlower)
            loadDatabase() // Refresh list
        }
    }

    fun startQuiz() {
        if (_flowerList.value.isNotEmpty()) {
            shuffledQuizList = _flowerList.value.shuffled()
            currentQuizIndex = 0
            isQuizActive = true
        }
    }

    fun stopQuiz() { isQuizActive = false }

    fun checkAnswer(latinInput: String, familyInput: String): Boolean {
        val currentFlower = shuffledQuizList[currentQuizIndex]
        return latinInput.trim().equals(currentFlower.latin, ignoreCase = true) &&
               familyInput.trim().equals(currentFlower.family, ignoreCase = true)
    }

    fun nextFlower() {
        if (currentQuizIndex < shuffledQuizList.size - 1) {
            currentQuizIndex++
        } else {
            isQuizActive = false
        }
    }
}

// ViewModel Factory needed to pass the Room Dao
class FlowerViewModelFactory(private val dao: FlowerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FlowerViewModel(dao) as T
    }
}

// --- MAIN UI ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(applicationContext)
        val factory = FlowerViewModelFactory(db.flowerDao())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: FlowerViewModel = viewModel(factory = factory)
                    if (viewModel.isQuizActive) {
                        QuizScreen(viewModel)
                    } else {
                        AddFlowerScreen(viewModel)
                    }
                }
            }
        }
    }
}

// --- ADD FLOWER SCREEN ---
@Composable
fun AddFlowerScreen(viewModel: FlowerViewModel) {
    val flowerList by viewModel.flowerList.collectAsState()
    val context = LocalContext.current
    
    var germanInput by remember { mutableStateOf("") }
    var latinInput by remember { mutableStateOf("") }
    var familyInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable permission so the app can access it after restart
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedImageUri = it
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Flower Database (${flowerList.size}/150)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = germanInput, onValueChange = { germanInput = it }, label = { Text("German Name") })
        OutlinedTextField(value = latinInput, onValueChange = { latinInput = it }, label = { Text("Latin Name") })
        OutlinedTextField(value = familyInput, onValueChange = { familyInput = it }, label = { Text("Family Name") })
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }) {
                Text(if (selectedImageUri == null) "Attach Picture" else "Picture Attached!")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                if (germanInput.isNotBlank() && latinInput.isNotBlank() && familyInput.isNotBlank()) {
                    viewModel.addFlower(germanInput, latinInput, familyInput, selectedImageUri?.toString())
                    germanInput = ""; latinInput = ""; familyInput = ""; selectedImageUri = null
                }
            }) { Text("Save Flower") }

            Button(onClick = { viewModel.startQuiz() }, enabled = flowerList.isNotEmpty()) { Text("Start Quiz") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(flowerList) { flower ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    flower.imageUri?.let { uriString ->
                        AsyncImage(
                            model = Uri.parse(uriString),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).padding(end = 8.dp)
                        )
                    }
                    Text("${flower.german} | ${flower.latin}")
                }
            }
        }
    }
}

// --- QUIZ SCREEN ---
@Composable
fun QuizScreen(viewModel: FlowerViewModel) {
    val currentFlower = viewModel.shuffledQuizList[viewModel.currentQuizIndex]
    
    var latinInput by remember { mutableStateOf("") }
    var familyInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var showAnswer by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Question ${viewModel.currentQuizIndex + 1} of ${viewModel.shuffledQuizList.size}")
        Spacer(modifier = Modifier.height(16.dp))
        
        currentFlower.imageUri?.let { uriString ->
            AsyncImage(
                model = Uri.parse(uriString),
                contentDescription = "Flower Image",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = currentFlower.german, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = latinInput, onValueChange = { latinInput = it }, label = { Text("Latin Name") })
        OutlinedTextField(value = familyInput, onValueChange = { familyInput = it }, label = { Text("Family Name") })

        Spacer(modifier = Modifier.height(16.dp))

        if (feedbackMessage.isNotEmpty()) {
            Text(feedbackMessage, color = if (isCorrect) Color(0xFF4CAF50) else Color.Red, fontWeight = FontWeight.Bold)
            if (!isCorrect && showAnswer) {
                Text("Correct Latin: ${currentFlower.latin}\nCorrect Family: ${currentFlower.family}", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.stopQuiz() }) { Text("End Quiz") }
            Button(onClick = {
                if (showAnswer || isCorrect) {
                    viewModel.nextFlower(); latinInput = ""; familyInput = ""; feedbackMessage = ""; showAnswer = false; isCorrect = false
                } else {
                    isCorrect = viewModel.checkAnswer(latinInput, familyInput)
                    if (isCorrect) feedbackMessage = "Correct!" else { feedbackMessage = "Incorrect!"; showAnswer = true }
                }
            }) { Text(if (showAnswer || isCorrect) "Next" else "Check") }
        }
    }
}
