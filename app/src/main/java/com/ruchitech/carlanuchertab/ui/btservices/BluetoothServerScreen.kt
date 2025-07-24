package com.ruchitech.carlanuchertab.ui.btservices

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BluetoothServerScreen(viewModel: BluetoothServerViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.startServer()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Bluetooth Server Running...", fontSize = 20.sp)
    }
}
