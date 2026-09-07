package com.example.baovesogui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baovesogui.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(this)

        setContent {
            MaterialTheme {
                MainAppScreen(db.xeDao())
            }
        }
    }
}

@Composable
fun MainAppScreen(xeDao: XeDao) {
    var currentTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = currentTab) {
            Tab(selected = currentTab == 0, onClick = { currentTab = 0 }) {
                Text("1. Nhập Xe", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = currentTab == 1, onClick = { currentTab = 1 }) {
                Text("2. Lấy Xe (Check-out)", modifier = Modifier.padding(16.dp))
            }
        }

        when (currentTab) {
            0 -> EntryScreen(xeDao)
            1 -> CheckoutScreen(xeDao)
        }
    }
}

@Composable
fun EntryScreen(xeDao: XeDao) {
    var bienSoInput by remember { mutableStateOf("") }
    var roomInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VehicleType.MOTORBIKE) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("NHẬP XE VÀO BÃI", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bienSoInput,
            onValueChange = { bienSoInput = it },
            label = { Text("Biển số xe (Ví dụ: 29A12345)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedType == VehicleType.MOTORBIKE,
                onClick = { selectedType = VehicleType.MOTORBIKE }
            )
            Text("🏍️ Xe Máy")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = selectedType == VehicleType.CAR,
                onClick = { selectedType = VehicleType.CAR }
            )
            Text("🚗 Ô Tô")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = roomInput,
            onValueChange = { roomInput = it },
            label = { Text("Số phòng (Bắt buộc nhập mới)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (bienSoInput.isNotBlank() && roomInput.isNotBlank()) {
                    scope.launch {
                        xeDao.insertXe(
                            XeInPark(
                                bienSo = bienSoInput.uppercase().trim(),
                                soPhong = roomInput.trim(),
                                loaiXe = selectedType
                            )
                        )
                        bienSoInput = ""
                        roomInput = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("LƯU XE VÀO BÃI")
        }
    }
}

@Composable
fun CheckoutScreen(xeDao: XeDao) {
    var searchRoom by remember { mutableStateOf("") }
    var xeList by remember { mutableStateOf(listOf<XeInPark>()) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("TÌM XE THEO PHÒNG", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchRoom,
                onValueChange = { searchRoom = it },
                label = { Text("Nhập số phòng") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        xeList = xeDao.getActiveByRoom(searchRoom.trim())
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Tìm")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(xeList) { xe ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (xe.loaiXe == VehicleType.MOTORBIKE) "🏍️ Xe Máy" else "🚗 Ô Tô",
                                fontSize = 14.sp
                            )
                            Text(text = "Biển: ${xe.bienSo}", fontSize = 18.sp)
                            Text(text = "Phòng: ${xe.soPhong}", fontSize = 14.sp)
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                scope.launch {
                                    xeDao.checkoutXe(xe.id)
                                    xeList = xeDao.getActiveByRoom(searchRoom.trim())
                                }
                            }
                        ) {
                            Text("CHO XE RA")
                        }
                    }
                }
            }
        }
    }
}
