package com.yourname.allowancetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yourname.allowancetracker.data.RecurringAllowance
import com.yourname.allowancetracker.ui.AllowanceViewModel
import com.yourname.allowancetracker.utils.formatCurrency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringAllowanceManagementScreen(
    viewModel: AllowanceViewModel,
    childId: Int,
    childName: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyCode = uiState.currencyCode
    var allowances by remember { mutableStateOf<List<RecurringAllowance>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAllowance by remember { mutableStateOf<RecurringAllowance?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<RecurringAllowance?>(null) }
    val scope = rememberCoroutineScope()

    // Load allowances
    LaunchedEffect(childId) {
        viewModel.getRecurringAllowance(childId).collect { newAllowances ->
            allowances = newAllowances
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Recurring Allowances",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "$childName • ${allowances.size} schedules",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6C63FF),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Recurring")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (allowances.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔄", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Recurring Allowances",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            "Set up automatic allowances for your child",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the + button to create one",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allowances) { allowance ->
                        RecurringAllowanceCard(
                            allowance = allowance,
                            currencyCode = currencyCode,
                            onEdit = {
                                selectedAllowance = allowance
                                showEditDialog = true
                            },
                            onToggle = {
                                scope.launch {
                                    viewModel.toggleRecurringAllowance(
                                        allowance.id,
                                        !allowance.isActive
                                    )
                                }
                            },
                            onDelete = {
                                showDeleteDialog = allowance
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddRecurringAllowanceDialog(
            currencyCode = currencyCode,
            onDismiss = { showAddDialog = false },
            onSave = { amount, frequency, day ->
                scope.launch {
                    viewModel.addRecurringAllowance(childId, amount, frequency, day)
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedAllowance != null) {
        EditRecurringAllowanceDialog(
            allowance = selectedAllowance!!,
            onDismiss = {
                showEditDialog = false
                selectedAllowance = null
            },
            onUpdate = { amount, frequency, day ->
                scope.launch {
                    viewModel.updateRecurringAllowance(
                        selectedAllowance!!.copy(
                            amount = amount,
                            frequency = frequency,
                            day = day
                        )
                    )
                    showEditDialog = false
                    selectedAllowance = null
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Recurring Allowance?") },
            text = {
                Text(
                    "This will permanently remove the recurring allowance of ${formatCurrency(showDeleteDialog!!.amount, currencyCode)}."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteRecurringAllowance(showDeleteDialog!!.id)
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ============================================
// RECURRING ALLOWANCE CARD
// ============================================
@Composable
fun RecurringAllowanceCard(
    allowance: RecurringAllowance,
    currencyCode: String,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allowance.isActive) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        formatCurrency(allowance.amount, currencyCode),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allowance.isActive) Color(0xFF333333) else Color.Gray
                    )
                    if (!allowance.isActive) {
                        Text(
                            "Paused",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    allowance.getDisplayText(),
                    fontSize = 13.sp,
                    color = if (allowance.isActive) Color(0xFF6C63FF) else Color.Gray
                )
                Text(
                    if (allowance.isActive) "Active" else "Inactive",
                    fontSize = 11.sp,
                    color = if (allowance.isActive) Color(0xFF4CAF50) else Color.Gray
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Toggle button
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (allowance.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (allowance.isActive) "Pause" else "Resume",
                        tint = if (allowance.isActive) Color(0xFFFF9800) else Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================
// ADD RECURRING ALLOWANCE DIALOG
// ============================================
@Composable
fun AddRecurringAllowanceDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (Double, String, Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Weekly") }
    var selectedDay by remember { mutableStateOf(1) }
    var dayExpanded by remember { mutableStateOf(false) }

    val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val monthDays = (1..28).map { it.toString() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔄", fontSize = 28.sp)
                    Text(
                        "Recurring Allowance",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Automatically add allowance on a schedule",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("10.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Frequency Selection
                Text(
                    "Frequency",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = frequency == "Weekly",
                            onClick = {
                                frequency = "Weekly"
                                selectedDay = 1
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF6C63FF)
                            )
                        )
                        Text("Weekly", fontSize = 14.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = frequency == "Monthly",
                            onClick = {
                                frequency = "Monthly"
                                selectedDay = 1
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF6C63FF)
                            )
                        )
                        Text("Monthly", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Selection - Using Dropdown
                Text(
                    if (frequency == "Weekly") "Day of Week" else "Day of Month",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { dayExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (frequency == "Weekly") {
                                weekDays[selectedDay - 1]
                            } else {
                                val suffix = when (selectedDay) {
                                    1 -> "st"
                                    2 -> "nd"
                                    3 -> "rd"
                                    else -> "th"
                                }
                                "${selectedDay}$suffix"
                            },
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Day",
                            tint = Color(0xFF6C63FF)
                        )
                    }

                    DropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        if (frequency == "Weekly") {
                            weekDays.forEachIndexed { index, day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        selectedDay = index + 1
                                        dayExpanded = false
                                    }
                                )
                            }
                        } else {
                            monthDays.forEach { day ->
                                val dayNum = day.toInt()
                                DropdownMenuItem(
                                    text = {
                                        val suffix = when (dayNum) {
                                            1 -> "st"
                                            2 -> "nd"
                                            3 -> "rd"
                                            else -> "th"
                                        }
                                        Text("$day$suffix")
                                    },
                                    onClick = {
                                        selectedDay = dayNum
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Preview
                if (amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F3FF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📅", fontSize = 20.sp)
                            Column {
                                Text(
                                    "Schedule Preview",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C63FF)
                                )
                                Text(
                                    "${formatCurrency(amount.toDoubleOrNull()!!, currencyCode)} every ${
                                        if (frequency == "Weekly") {
                                            weekDays[selectedDay - 1]
                                        } else {
                                            val suffix = when (selectedDay) {
                                                1 -> "st"
                                                2 -> "nd"
                                                3 -> "rd"
                                                else -> "th"
                                            }
                                            "${selectedDay}$suffix"
                                        }
                                    }",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0) {
                                onSave(amountValue, frequency, selectedDay)
                                onDismiss()
                            }
                        },
                        enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Recurring", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// EDIT RECURRING ALLOWANCE DIALOG
// ============================================
@Composable
fun EditRecurringAllowanceDialog(
    allowance: RecurringAllowance,
    onDismiss: () -> Unit,
    onUpdate: (Double, String, Int) -> Unit
) {
    var amount by remember { mutableStateOf(String.format("%.2f", allowance.amount)) }
    var frequency by remember { mutableStateOf(allowance.frequency) }
    var selectedDay by remember { mutableStateOf(allowance.day) }
    var dayExpanded by remember { mutableStateOf(false) }

    val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val monthDays = (1..28).map { it.toString() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✏️", fontSize = 28.sp)
                    Text(
                        "Edit Recurring Allowance",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Update the recurring allowance schedule",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("10.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Frequency Selection
                Text(
                    "Frequency",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = frequency == "Weekly",
                            onClick = {
                                frequency = "Weekly"
                                selectedDay = 1
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF6C63FF)
                            )
                        )
                        Text("Weekly", fontSize = 14.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(
                            selected = frequency == "Monthly",
                            onClick = {
                                frequency = "Monthly"
                                selectedDay = 1
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF6C63FF)
                            )
                        )
                        Text("Monthly", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Selection
                Text(
                    if (frequency == "Weekly") "Day of Week" else "Day of Month",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { dayExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (frequency == "Weekly") {
                                weekDays[selectedDay - 1]
                            } else {
                                val suffix = when (selectedDay) {
                                    1 -> "st"
                                    2 -> "nd"
                                    3 -> "rd"
                                    else -> "th"
                                }
                                "${selectedDay}$suffix"
                            },
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Day",
                            tint = Color(0xFF6C63FF)
                        )
                    }

                    DropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        if (frequency == "Weekly") {
                            weekDays.forEachIndexed { index, day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        selectedDay = index + 1
                                        dayExpanded = false
                                    }
                                )
                            }
                        } else {
                            monthDays.forEach { day ->
                                val dayNum = day.toInt()
                                DropdownMenuItem(
                                    text = {
                                        val suffix = when (dayNum) {
                                            1 -> "st"
                                            2 -> "nd"
                                            3 -> "rd"
                                            else -> "th"
                                        }
                                        Text("$day$suffix")
                                    },
                                    onClick = {
                                        selectedDay = dayNum
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0) {
                                onUpdate(amountValue, frequency, selectedDay)
                                onDismiss()
                            }
                        },
                        enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}