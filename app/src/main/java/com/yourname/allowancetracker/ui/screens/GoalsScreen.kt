package com.yourname.allowancetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yourname.allowancetracker.data.SavingsGoal
import com.yourname.allowancetracker.ui.AllowanceViewModel
import com.yourname.allowancetracker.utils.formatCurrency
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GoalsScreen(
    viewModel: AllowanceViewModel,
    childId: Int,
    childName: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyCode = uiState.currencyCode
    var goals by remember { mutableStateOf<List<SavingsGoal>>(emptyList()) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAllocateDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var showEditGoalDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var showDeleteGoalDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    val scope = rememberCoroutineScope()

    // Load goals
    LaunchedEffect(childId) {
        viewModel.getGoalsForChild(childId).collect { newGoals ->
            goals = newGoals
        }
    }

    // Calculate stats
    val totalGoals = goals.size
    val completedGoals = goals.count { it.isCompleted }
    val activeGoals = goals.count { !it.isCompleted }
    val totalSaved = goals.sumOf { it.savedAmount }
    val totalTarget = goals.sumOf { it.targetAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🎯 Savings Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "$childName • $activeGoals active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
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
                onClick = { showAddGoalDialog = true },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                modifier = Modifier.size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add Goal",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F3FF),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        ) {
            // Stats Cards
            if (goals.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Active",
                        value = activeGoals.toString(),
                        icon = Icons.Default.PlayArrow,
                        color = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Completed",
                        value = completedGoals.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Progress",
                        value = if (totalTarget > 0) "${((totalSaved / totalTarget) * 100).toInt()}%" else "0%",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Goals
                val activeGoalList = goals.filter { !it.isCompleted }
                val completedGoalList = goals.filter { it.isCompleted }

                if (activeGoalList.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🎯 Active Goals",
                            count = activeGoalList.size
                        )
                    }
                    items(activeGoalList) { goal ->
                        AnimatedContent(
                            targetState = goal,
                            transitionSpec = {
                                fadeIn() + slideInVertically() togetherWith
                                        fadeOut() + slideOutVertically()
                            }
                        ) {
                            EnhancedGoalCard(
                                goal = goal,
                                currencyCode = currencyCode,
                                onAllocate = { showAllocateDialog = goal },
                                onEdit = { showEditGoalDialog = goal },
                                onDelete = { showDeleteGoalDialog = goal }
                            )
                        }
                    }
                }

                if (completedGoalList.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🎉 Completed Goals",
                            count = completedGoalList.size
                        )
                    }
                    items(completedGoalList) { goal ->
                        EnhancedGoalCard(
                            goal = goal,
                            currencyCode = currencyCode,
                            onAllocate = null,
                            onEdit = { showEditGoalDialog = goal },
                            onDelete = { showDeleteGoalDialog = goal }
                        )
                    }
                }

                if (goals.isEmpty()) {
                    item {
                        EmptyGoalsState()
                    }
                }
            }
        }
    }

    // ============================================
    // DIALOGS
    // ============================================

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onCreate = { name, amount, icon ->
                scope.launch {
                    viewModel.createGoal(childId, name, amount, icon)
                    showAddGoalDialog = false
                }
            }
        )
    }

    // Allocate Dialog
    if (showAllocateDialog != null) {
        val goal = showAllocateDialog!!
        AllocateFundsDialog(
            goal = goal,
            availableBalance = uiState.selectedChild?.balance ?: 0.0,
            currencyCode = currencyCode,
            onDismiss = { showAllocateDialog = null },
            onAllocate = { amount, note ->
                scope.launch {
                    viewModel.allocateToGoal(childId, goal.id, amount, note)
                    showAllocateDialog = null
                }
            }
        )
    }

    // Edit Goal Dialog
    if (showEditGoalDialog != null) {
        EditGoalDialog(
            goal = showEditGoalDialog!!,
            onDismiss = { showEditGoalDialog = null },
            onUpdate = { name, targetAmount, icon ->
                scope.launch {
                    viewModel.updateGoal(
                        showEditGoalDialog!!.copy(
                            name = name,
                            targetAmount = targetAmount,
                            icon = icon
                        )
                    )
                    showEditGoalDialog = null
                }
            }
        )
    }

    // Delete Goal Dialog
    if (showDeleteGoalDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteGoalDialog = null },
            title = {
                Text(
                    "Delete Goal?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete '${showDeleteGoalDialog?.name}'? This will remove all allocated funds and cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            showDeleteGoalDialog?.let { viewModel.deleteGoal(it) }
                            showDeleteGoalDialog = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGoalDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ============================================
// STATS CARD
// ============================================
@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================
// SECTION HEADER
// ============================================
@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Text(
            "$count items",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// ============================================
// ENHANCED GOAL CARD
// ============================================
@Composable
fun EnhancedGoalCard(
    goal: SavingsGoal,
    currencyCode: String,
    onAllocate: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = goal.getProgress()
    val isCompleted = goal.isCompleted
    val remaining = goal.getRemainingAmount()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon, Name, and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF6C63FF), Color(0xFF9B8FFF))
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            goal.icon,
                            fontSize = 28.sp
                        )
                        if (isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = (-4).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅", fontSize = 12.sp)
                            }
                        }
                    }

                    Column {
                        Text(
                            goal.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${formatCurrency(goal.savedAmount, currencyCode)} / ${formatCurrency(goal.targetAmount, currencyCode)}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            if (isCompleted) {
                                Text(
                                    "✅ Completed",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (!isCompleted) {
                        IconButton(
                            onClick = { onEdit() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit",
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete",
                            tint = Color(0xFFE53935).copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isCompleted) {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6C63FF), Color(0xFF9B8FFF))
                                )
                            }
                        )
                )

                // Progress percentage label
                if (progress > 0.1f) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Remaining and Allocate button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCompleted) {
                    Column {
                        Text(
                            "Remaining",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            formatCurrency(remaining, currencyCode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Goal Achieved! 🎉",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                if (!isCompleted && onAllocate != null) {
                    Button(
                        onClick = onAllocate,
                        modifier = Modifier
                            .height(40.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Allocate",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Allocate",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// EMPTY STATE
// ============================================
@Composable
fun EmptyGoalsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6C63FF).copy(alpha = 0.2f),
                                Color(0xFF6C63FF).copy(alpha = 0.05f)
                            ),
                            radius = 100f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🎯",
                    fontSize = 56.sp
                )
            }

            Text(
                "No Goals Yet!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                "Start saving for something special!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                "🎮 Video games  📱 Phone  🎁 Gifts  ✈️ Travel",
                fontSize = 13.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================
// EDIT GOAL DIALOG
// ============================================
@Composable
fun EditGoalDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onUpdate: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(goal.name) }
    var amount by remember { mutableStateOf(String.format("%.2f", goal.targetAmount)) }
    var selectedIcon by remember { mutableStateOf(goal.icon) }

    val icons = listOf("🎯", "🎮", "💰", "🎁", "📱", "✈️", "🏠", "🚗", "🐶", "📚", "👟", "🎸")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "✏️ Edit Goal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g., Video Game Console") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Target Amount") },
                    placeholder = { Text("100.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Choose an Icon",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Icon grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { icon ->
                                FilterChip(
                                    selected = selectedIcon == icon,
                                    onClick = { selectedIcon = icon },
                                    label = { Text(icon, fontSize = 20.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6C63FF).copy(alpha = 0.2f),
                                        selectedLabelColor = Color(0xFF6C63FF)
                                    )
                                )
                            }
                            // Fill empty spaces
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
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
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (name.isNotBlank() && amountValue != null && amountValue > 0) {
                                onUpdate(name, amountValue, selectedIcon)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Goal")
                    }
                }
            }
        }
    }
}

// ============================================
// ADD GOAL DIALOG
// ============================================
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🎯") }

    val icons = listOf("🎯", "🎮", "💰", "🎁", "📱", "✈️", "🏠", "🚗", "🐶", "📚", "👟", "🎸")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "🎯 New Savings Goal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "What are you saving for?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g., Video Game Console") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Target Amount") },
                    placeholder = { Text("100.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Choose an Icon",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Icon grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { icon ->
                                FilterChip(
                                    selected = selectedIcon == icon,
                                    onClick = { selectedIcon = icon },
                                    label = { Text(icon, fontSize = 20.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6C63FF).copy(alpha = 0.2f),
                                        selectedLabelColor = Color(0xFF6C63FF)
                                    )
                                )
                            }
                            // Fill empty spaces
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
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
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (name.isNotBlank() && amountValue != null && amountValue > 0) {
                                onCreate(name, amountValue, selectedIcon)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Goal")
                    }
                }
            }
        }
    }
}

// ============================================
// ALLOCATE FUNDS DIALOG
// ============================================
@Composable
fun AllocateFundsDialog(
    goal: SavingsGoal,
    availableBalance: Double,
    currencyCode: String,
    onDismiss: () -> Unit,
    onAllocate: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val remaining = goal.getRemainingAmount()
    val maxAllocate = minOf(availableBalance, remaining)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "💰",
                        fontSize = 28.sp
                    )
                    Text(
                        "Allocate Funds",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F3FF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "${goal.icon} ${goal.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Progress: ${formatCurrency(goal.savedAmount, currencyCode)} / ${formatCurrency(goal.targetAmount, currencyCode)}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                "${(goal.getProgress() * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                        Text(
                            "Remaining: ${formatCurrency(remaining, currencyCode)}",
                            fontSize = 13.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Available Balance: ${formatCurrency(availableBalance, currencyCode)}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to Allocate") },
                    placeholder = { Text("10.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    ),
                    trailingIcon = {
                        if (amount.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    val maxAmount = if (maxAllocate > 0) maxAllocate else 0.0
                                    amount = String.format("%.2f", maxAmount)
                                },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("MAX", fontSize = 11.sp)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("Chores this week") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        focusedLabelColor = Color(0xFF6C63FF)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0 && amountValue <= maxAllocate) {
                                onAllocate(amountValue, note.ifBlank { "Savings allocation" })
                            }
                        },
                        enabled = amount.toDoubleOrNull() != null &&
                                amount.toDoubleOrNull()!! > 0 &&
                                amount.toDoubleOrNull()!! <= maxAllocate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Allocate",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Allocate")
                    }
                }

                if (maxAllocate <= 0 && availableBalance > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ This goal is fully funded!",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800)
                    )
                } else if (availableBalance <= 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ No funds available to allocate",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}