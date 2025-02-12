package com.muren.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.muren.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    val habitList = remember { mutableStateListOf<Habit>() }
    var habitName = remember { mutableStateOf("") }
    var editingIndex = remember { mutableStateOf<Int?>(null) }

    val completedCount = habitList.count { it.isCompleted }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Hábitos Completados: $completedCount / ${habitList.size}",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            TextField(
                value = habitName.value,
                onValueChange = { habitName.value = it },
                label = { Text("Nuevo Hábito") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            onClick = {
                if (habitName.value.isNotEmpty()) {
                    editingIndex.value?.let { index ->
                        habitList[index] = habitList[index].copy(name = habitName.value)
                        editingIndex.value = null
                    } ?: run {
                        habitList.add(Habit(name = habitName.value, isCompleted = false))
                    }
                    habitName.value = ""
                }
            },
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text(if (editingIndex.value != null) "Guardar Hábito" else "Agregar Hábito", color = Color.White)
        }

        habitList.forEachIndexed { index, habit ->
            HabitItem(
                habit = habit,
                onCheckedChange = { isChecked ->
                    habitList[index] = habit.copy(isCompleted = isChecked)
                },
                onEditClick = {
                    habitName.value = ""
                    editingIndex.value = index
                },
                onDeleteClick = {
                    habitList.removeAt(index)
                }
            )
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onCheckedChange: (Boolean) -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val textColor = if (habit.isCompleted) Color.Green else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Checkbox(
            checked = habit.isCompleted,
            onCheckedChange = { isChecked -> onCheckedChange(isChecked) },
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                color = textColor,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Button(
                onClick = onEditClick,
            ) {
                Text("Editar")
            }

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HabitTrackerTheme {
        HabitTrackerScreen()
    }
}
