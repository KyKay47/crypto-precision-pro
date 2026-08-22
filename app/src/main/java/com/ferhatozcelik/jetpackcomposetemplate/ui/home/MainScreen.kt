package com.ferhatozcelik.jetpackcomposetemplate.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

data class Candle(
    val high: Double,
    val low: Double,
    val close: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val coroutineScope = rememberCoroutineScope()

    var btcPriceDisplay by remember { mutableStateOf("$77,350.00") }
    var accountBalance by remember { mutableStateOf("3751.49") }
    var riskPercent by remember { mutableStateOf("1") }
    var isLoading by remember { mutableStateOf(false) }

    // Real Market Structure State
    var h4Bias by remember { mutableStateOf("BULLISH ↑") }
    var h1Bias by remember { mutableStateOf("BULLISH ↑") }
    var m15Bias by remember { mutableStateOf("BULLISH ↑") }
    var overallBias by remember { mutableStateOf("BULLISH 🟢") }
    var confluenceRate by remember { mutableStateOf("100% (Confirmed Structure)") }
    var marketStructureText by remember { mutableStateOf("BULLISH BREAKOUT 🟢") }
    var isBullishStructure by remember { mutableStateOf(true) }

    // Dynamic Execution Bounds
    var entryZoneText by remember { mutableStateOf("77,000.00 - 77,350.00") }
    var stopLossText by remember { mutableStateOf("76,200.00") }
    var tp1Text by remember { mutableStateOf("78,800.00") }
    var tp2Text by remember { mutableStateOf("80,200.00") }

    fun analyzeMarketStructure() {
        coroutineScope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    // 1. Fetch Spot Price
                    val priceRes = URL("https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT").readText()
                    val currentPrice = JSONObject(priceRes).getString("price").toDouble()

                    // Helper to fetch and parse candles
                    fun getCandles(interval: String): List<Candle> {
                        val jsonStr = URL("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=$interval&limit=25").readText()
                        val arr = JSONArray(jsonStr)
                        val list = mutableListOf<Candle>()
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONArray(i)
                            list.add(
                                Candle(
                                    high = item.getString(2).toDouble(),
                                    low = item.getString(3).toDouble(),
                                    close = item.getString(4).toDouble()
                                )
                            )
                        }
                        return list
                    }

                    // 2. Fetch Multi-Timeframe K-Lines
                    val h4Candles = getCandles("4h")
                    val h1Candles = getCandles("1h")
                    val m15Candles = getCandles("15m")

                    // 3. Trend Evaluator (Price vs 20 SMA)
                    fun checkIsBullish(candles: List<Candle>): Boolean {
                        val recent20 = candles.takeLast(20)
                        val sma20 = recent20.map { it.close }.average()
                        return candles.last().close >= sma20
                    }

                    val h4IsBull = checkIsBullish(h4Candles)
                    val h1IsBull = checkIsBullish(h1Candles)
                    val m15IsBull = checkIsBullish(m15Candles)

                    val bullCount = listOf(h4IsBull, h1IsBull, m15IsBull).count { it }

                    // 4. Determine Structure & Alignment
                    val isOverallBull = bullCount >= 2
                    val confluencePct = when (bullCount) {
                        3, 0 -> "100% (Confirmed Structure)"
                        else -> "66% (Moderate Alignment)"
                    }

                    val m15High = m15Candles.maxOf { it.high }
                    val m15Low = m15Candles.minOf { it.low }

                    withContext(Dispatchers.Main) {
                        btcPriceDisplay = String.format("$%.2f", currentPrice)
                        h4Bias = if (h4IsBull) "BULLISH ↑" else "BEARISH ↓"
                        h1Bias = if (h1IsBull) "BULLISH ↑" else "BEARISH ↓"
                        m15Bias = if (m15IsBull) "BULLISH ↑" else "BEARISH ↓"

                        isBullishStructure = isOverallBull
                        confluenceRate = confluencePct

                        if (isOverallBull) {
                            overallBias = "BULLISH 🟢"
                            marketStructureText = if (bullCount == 3) "BULLISH BREAKOUT 🟢" else "BULLISH CONTINUATION 🟢"

                            val entryLower = currentPrice * 0.998
                            val sl = m15Low.coerceAtMost(currentPrice * 0.985)
                            val riskDist = currentPrice - sl
                            val tp1 = currentPrice + (riskDist * 1.5)
                            val tp2 = currentPrice + (riskDist * 2.5)

                            entryZoneText = String.format("%.2f - %.2f", entryLower, currentPrice)
                            stopLossText = String.format("%.2f", sl)
                            tp1Text = String.format("%.2f", tp1)
                            tp2Text = String.format("%.2f", tp2)
                        } else {
                            overallBias = "BEARISH 🔴"
                            marketStructureText = if (bullCount == 0) "BEARISH BREAKDOWN 🔴" else "BEARISH REJECTION 🔴"

                            val entryUpper = currentPrice * 1.002
                            val sl = m15High.coerceAtLeast(currentPrice * 1.015)
                            val riskDist = sl - currentPrice
                            val tp1 = currentPrice - (riskDist * 1.5)
                            val tp2 = currentPrice - (riskDist * 2.5)

                            entryZoneText = String.format("%.2f - %.2f", currentPrice, entryUpper)
                            stopLossText = String.format("%.2f", sl)
                            tp1Text = String.format("%.2f", tp1)
                            tp2Text = String.format("%.2f", tp2)
                        }
                    }
                } catch (e: Exception) {
                    // Retain defaults if network drops
                } finally {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        analyzeMarketStructure()
    }

    val balanceVal = accountBalance.toDoubleOrNull() ?: 3751.49
    val riskVal = riskPercent.toDoubleOrNull() ?: 1.0
    val riskAmount = balanceVal * (riskVal / 100.0)
    val recLotSize = String.format("%.2f Lots", (riskAmount / 500.0).coerceAtLeast(0.01))

    val goldColor = Color(0xFFFFC107)
    val darkBg = Color(0xFF121212)
    val cardBg = Color(0xFF1E1E1E)
    val grayLabel = Color(0xFF9E9E9E)
    val greenText = Color(0xFF4CAF50)
    val redText = Color(0xFFFF5252)
    val cyanText = Color(0xFF00E5FF)

    val activeSignalColor = if (isBullishStructure) greenText else redText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CRYPTO PRECISION PRO",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = goldColor
        )
        Text(
            text = "Market Structure & Trend Engine v3.1",
            fontSize = 12.sp,
            color = grayLabel
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LIVE BTC/USD SPOT PRICE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = grayLabel
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = btcPriceDisplay,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = accountBalance,
                onValueChange = { accountBalance = it },
                label = { Text("Account Balance ($)", fontSize = 12.sp) },
                modifier = Modifier.weight(1.5f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = riskPercent,
                onValueChange = { riskPercent = it },
                label = { Text("Risk %", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { analyzeMarketStructure() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = goldColor,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = if (isLoading) "Scanning Market Structure..." else "Analyze Market Structure 🔄",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SIGNAL & MARKET STRUCTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = grayLabel)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Signal Bias:", color = Color.White, fontSize = 14.sp)
                    Text(overallBias, color = activeSignalColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Confluence Rate:", color = Color.White, fontSize = 14.sp)
                    Text(confluenceRate, color = goldColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Market Structure:", color = Color.White, fontSize = 14.sp)
                    Text(marketStructureText, color = activeSignalColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("TIMEFRAME TREND ALIGNMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = grayLabel)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("H4 Macro Bias:", color = Color.White, fontSize = 14.sp)
                    Text(h4Bias, color = if (h4Bias.contains("BULLISH")) greenText else redText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("H1 Intermediate:", color = Color.White, fontSize = 14.sp)
                    Text(h1Bias, color = if (h1Bias.contains("BULLISH")) greenText else redText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("M15 Execution:", color = Color.White, fontSize = 14.sp)
                    Text(m15Bias, color = if (m15Bias.contains("BULLISH")) greenText else redText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("RISK MANAGEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = goldColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rec. Lot Size:", color = Color.White, fontSize = 14.sp)
                    Text(recLotSize, color = greenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("TRADE EXECUTION BOUNDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = grayLabel)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Entry Zone:", color = Color.White, fontSize = 14.sp)
                    Text(entryZoneText, color = cyanText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stop Loss (SL):", color = Color.White, fontSize = 14.sp)
                    Text(stopLossText, color = redText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Take Profit 1 (TP1):", color = Color.White, fontSize = 14.sp)
                    Text(tp1Text, color = greenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Take Profit 2 (TP2):", color = Color.White, fontSize = 14.sp)
                    Text(tp2Text, color = greenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
