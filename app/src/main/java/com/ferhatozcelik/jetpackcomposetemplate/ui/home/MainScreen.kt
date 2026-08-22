package com.ferhatozcelik.jetpackcomposetemplate.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "CRYPTO PRECISION PRO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BTC / USD Signal Engine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Action: STRONG BUY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Entry Zone: $64,200 - $64,500", fontSize = 14.sp)
                Text(text = "Target 1 (TP1): $65,800", fontSize = 14.sp)
                Text(text = "Target 2 (TP2): $67,200", fontSize = 14.sp)
                Text(text = "Stop Loss (SL): $63,100", fontSize = 14.sp, color = Color(0xFFE53935))
            }
        }
    }
}
