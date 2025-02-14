package com.muren.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.muren.habittracker.ui.theme.HabitTrackerTheme
import java.text.SimpleDateFormat
import java.util.*

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
    val completedDates = remember { mutableStateListOf<String>() }
    val completedPerDay = remember { mutableStateMapOf<String, Int>() }

    val completedCount = habitList.count { it.isCompleted }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Hábitos Completados: $completedCount / ${habitList.size}",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6200EE),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = habitName.value,
                onValueChange = { habitName.value = it },
                singleLine = true,
                keyboardActions = KeyboardActions(onDone = {
                    if (habitName.value.isNotEmpty()) {
                        habitList.add(Habit(name = habitName.value, isCompleted = false))
                        habitName.value = ""
                    }
                }),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Agregar Hábito",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        CalendarView(completedDates, completedPerDay)

        habitList.forEachIndexed { index, habit ->
            HabitItem(
                habit = habit,
                onCheckedChange = { isChecked ->
                    habitList[index] = habit.copy(isCompleted = isChecked)
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (isChecked) {
                        completedDates.add(date)
                        completedPerDay[date] = completedPerDay.getOrDefault(date, 0) + 1
                    } else {
                        completedDates.remove(date)
                        completedPerDay[date] = completedPerDay.getOrDefault(date, 0) - 1
                        if (completedPerDay[date] == 0) {
                            completedPerDay.remove(date)
                        }
                    }
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

        WeeklyProgress(completedPerDay)
    }
}

@Composable
fun CalendarView(completedDates: List<String>, completedPerDay: Map<String, Int>) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = (1..daysInMonth).map { day ->
        val dayFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            calendar.apply { set(Calendar.DAY_OF_MONTH, day) }.time
        )
        day to dayFormatted
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days) { (day, dayFormatted) ->
            val isCompleted = completedDates.contains(dayFormatted)
            val habitCount = completedPerDay[dayFormatted] ?: 0
            DayBox(day = day, isCompleted = isCompleted, habitCount = habitCount)
        }
    }
}

@Composable
fun DayBox(day: Int, isCompleted: Boolean, habitCount: Int) {
    val backgroundColor = if (isCompleted) Color(0xFF6200EE) else Color.LightGray
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(4.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .wrapContentSize(align = Alignment.Center)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            if (habitCount > 0) {
                Text(
                    text = "$habitCount",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun WeeklyProgress(completedPerDay: Map<String, Int>) {
    val calendar = Calendar.getInstance()
    val weeks = mutableListOf<Pair<String, Int>>()

    var currentWeek = getWeekOfMonth(calendar)
    var habitsInWeek = 0

    for ((date, habitCount) in completedPerDay) {
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
        val week = getWeekOfMonth(calendar)
        if (week != currentWeek) {
            if (habitsInWeek > 0) {
                weeks.add(Pair(currentWeek, habitsInWeek))
            }
            currentWeek = week
            habitsInWeek = habitCount
        } else {
            habitsInWeek += habitCount
        }
    }

    if (habitsInWeek > 0) {
        weeks.add(Pair(currentWeek, habitsInWeek))
    }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Progreso Semanal",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        weeks.forEach { (week, habitsCompleted) ->
            Text("Semana $week: $habitsCompleted hábitos completados")
        }
    }
}

fun getWeekOfMonth(calendar: Calendar): String {
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    return when {
        dayOfMonth in 1..7 -> "Semana 1"
        dayOfMonth in 8..14 -> "Semana 2"
        dayOfMonth in 15..21 -> "Semana 3"
        else -> "Semana 4"
    }
}

@Composable
fun HabitItem(habit: Habit, onCheckedChange: (Boolean) -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val textColor = if (habit.isCompleted) Color(0xFF388E3C) else Color.Black
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
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Button(onClick = onEditClick) {
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
