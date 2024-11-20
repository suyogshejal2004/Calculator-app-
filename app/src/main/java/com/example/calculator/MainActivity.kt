package com.example.calculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import kotlin.math.abs
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer

data class CalculationHistoryItem(
    val expression: String,
    val result: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Calculator()
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )
    
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 8.dp
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

private fun formatResult(number: Double): String {
    return when {
        number == 0.0 -> "0"
        abs(number) < 0.00000001 || abs(number) > 99999999 -> {
            "%.2E".format(number)
        }
        number % 1.0 == 0.0 -> number.toLong().toString()
        else -> "%.8f".format(number).trimEnd('0').trimEnd('.')
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Calculator() {
    var display by remember { mutableStateOf("0") }
    var operation by remember { mutableStateOf("") }
    var firstNumber by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(listOf<CalculationHistoryItem>()) }
    var showHistory by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(0.0) }
    var showMemory by remember { mutableStateOf(false) }

    fun calculateResult() {
        if (firstNumber.isNotEmpty() && display.isNotEmpty()) {
            try {
                val num1 = firstNumber.toDouble()
                val num2 = display.toDouble()
                val result = when (operation) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×" -> num1 * num2
                    "÷" -> if (num2 != 0.0) num1 / num2 else throw ArithmeticException("Division by zero")
                    else -> display.toDouble()
                }
                val formattedResult = formatResult(result)
                history = history + CalculationHistoryItem(
                    "$firstNumber $operation $display",
                    formattedResult
                )
                display = formattedResult
                operation = ""
                firstNumber = ""
                newNumber = true
                showError = false
            } catch (e: Exception) {
                display = "Error"
                showError = true
            }
        }
    }

    fun backspace() {
        if (!showError && display != "0") {
            display = if (display.length == 1 || (display.length == 2 && display.startsWith("-"))) {
                "0"
            } else {
                display.dropLast(1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(vertical = 16.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Memory indicator
            if (memory != 0.0) {
                Text(
                    text = "M",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Operation display
                if (firstNumber.isNotEmpty() && operation.isNotEmpty()) {
                    Text(
                        text = "$firstNumber $operation",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Main display
                AnimatedContent(
                    targetState = display,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) with 
                        (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "display"
                ) { targetDisplay ->
                    Text(
                        text = targetDisplay,
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.End,
                        color = if (showError) 
                            MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface,
                        fontSize = 48.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Buttons grid
        val operatorColors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
        
        val numberColors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )

        // First row
        Row(modifier = Modifier.weight(1f)) {
            CalculatorButton(
                text = "C",
                onClick = { 
                    display = "0"
                    operation = ""
                    firstNumber = ""
                    newNumber = true
                    showError = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            )
            CalculatorButton(
                text = "⌫",  // Backspace
                onClick = { backspace() },
                modifier = Modifier.weight(1f),
                colors = operatorColors
            )
            CalculatorButton(
                text = "M",  // Memory
                onClick = { showMemory = !showMemory },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            CalculatorButton(
                text = "÷",
                onClick = {
                    if (!showError && display != "0") {
                        operation = "÷"
                        firstNumber = display
                        newNumber = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = operatorColors
            )
        }

        // History Dialog
        if (showHistory) {
            AlertDialog(
                onDismissRequest = { showHistory = false },
                title = { Text("Calculation History") },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        if (history.isEmpty()) {
                            Text(
                                text = "No calculations yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            history.asReversed().forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.expression,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "= ${item.result}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            history = emptyList()
                            showHistory = false 
                        }
                    ) {
                        Text("Clear History")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHistory = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Memory Dialog
        if (showMemory) {
            AlertDialog(
                onDismissRequest = { showMemory = false },
                title = { Text("Memory Operations") },
                text = {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Current Memory: ${formatResult(memory)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        memory += display.toDouble()
                                        showMemory = false
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            ) {
                                Text("M+")
                            }
                            Button(
                                onClick = {
                                    try {
                                        memory -= display.toDouble()
                                        showMemory = false
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            ) {
                                Text("M-")
                            }
                            Button(
                                onClick = {
                                    display = formatResult(memory)
                                    newNumber = true
                                    showMemory = false
                                }
                            ) {
                                Text("MR")
                            }
                            Button(
                                onClick = {
                                    memory = 0.0
                                    showMemory = false
                                }
                            ) {
                                Text("MC")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMemory = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Number rows
        val numbers = listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3")
        )

        numbers.forEach { row ->
            Row(modifier = Modifier.weight(1f)) {
                row.forEach { number ->
                    CalculatorButton(
                        text = number,
                        onClick = {
                            if (!showError) {
                                if (newNumber) {
                                    display = number
                                    newNumber = false
                                } else {
                                    display = if (display == "0") number else display + number
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = numberColors
                    )
                }
                val operator = when(row[0]) {
                    "7" -> "×"
                    "4" -> "-"
                    else -> "+"
                }
                CalculatorButton(
                    text = operator,
                    onClick = {
                        if (!showError && display != "0") {
                            operation = operator
                            firstNumber = display
                            newNumber = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = operatorColors
                )
            }
        }

        // Last row
        Row(modifier = Modifier.weight(1f)) {
            CalculatorButton(
                text = "0",
                onClick = {
                    if (!showError) {
                        if (newNumber) {
                            display = "0"
                            newNumber = false
                        } else if (display != "0") {
                            display += "0"
                        }
                    }
                },
                modifier = Modifier.weight(2f),
                colors = numberColors
            )
            CalculatorButton(
                text = ".",
                onClick = { 
                    if (!showError && !display.contains(".")) {
                        display = if (display == "0") "0." else "$display."
                        newNumber = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = numberColors
            )
            CalculatorButton(
                text = "=",
                onClick = { if (!showError) calculateResult() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}