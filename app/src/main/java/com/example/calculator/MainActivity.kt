package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

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
    
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape),
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 24.sp
        )
    }
}

private fun formatResult(number: Double): String {
    return if (number % 1.0 == 0.0) {
        number.toLong().toString()
    } else {
        "%.8f".format(number).trimEnd('0').trimEnd('.')
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
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
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
                    modifier = Modifier.animateContentSize()
                )
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
                text = "±",
                onClick = { 
                    if (!showError) {
                        display = if (display.startsWith("-")) display.drop(1) else "-$display"
                    }
                },
                modifier = Modifier.weight(1f),
                colors = operatorColors
            )
            CalculatorButton(
                text = "%",
                onClick = { 
                    if (!showError && display != "0") {
                        try {
                            val number = display.toDouble()
                            display = formatResult(number / 100)
                        } catch (e: Exception) {
                            display = "Error"
                            showError = true
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = operatorColors
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