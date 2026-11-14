package com.example.minidesk

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.ui.components.FloatingTaskbar
import com.example.minidesk.ui.components.HeroSection
import com.example.minidesk.ui.components.MainTopHeader
import com.example.minidesk.ui.components.PermissionsModalSheet
import com.example.minidesk.ui.pages.AppearancePage
import com.example.minidesk.ui.pages.DeckEditorPage
import com.example.minidesk.ui.pages.SettingsPage
import com.example.minidesk.ui.theme.MiniDeskTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepo: MiniDeskSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepo = MiniDeskSettingsRepository.get(this)

        if (Settings.canDrawOverlays(this)) {
            startOverlayService()
        }

        setContent {
            MiniDeskTheme {
                val prefs by settingsRepo.preferences.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(prefs.colorGraph.bgSolid)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        MainHubScreen(
                            settingsRepo = settingsRepo,
                            onStartService = { startOverlayService() },
                            onStopService = { stopOverlayService() }
                        )
                    }
                }
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, MiniDeskOverlayService::class.java)
        startForegroundService(intent)
        settingsRepo.updatePreferences { it.copy(isServiceEnabled = true) }
    }

    private fun stopOverlayService() {
        val intent = Intent(this, MiniDeskOverlayService::class.java)
        stopService(intent)
        settingsRepo.updatePreferences { it.copy(isServiceEnabled = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainHubScreen(
    settingsRepo: MiniDeskSettingsRepository,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val prefs by settingsRepo.preferences.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(prefs.colorGraph.bgSolid))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sticky Top Header
            MainTopHeader(
                prefs = prefs
            )

            // Main Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero Action Cards (Sidebar & Permissions)
                HeroSection(
                    prefs = prefs,
                    onToggleService = { enabled ->
                        if (enabled) onStartService() else onStopService()
                    },
                    onOpenPermissionsSheet = { showPermissionsSheet = true }
                )

                // Page Destination Content
                when (selectedTab) {
                    0 -> AppearancePage(settingsRepo = settingsRepo, prefs = prefs)
                    1 -> DeckEditorPage(settingsRepo = settingsRepo, prefs = prefs)
                    2 -> SettingsPage()
                }
            }
        }

        // Floating Bottom Taskbar Dock
        FloatingTaskbar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            prefs = prefs,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )

        // In-App Permissions Sheet
        if (showPermissionsSheet) {
            PermissionsModalSheet(
                sheetState = sheetState,
                onDismiss = { showPermissionsSheet = false }
            )
        }
    }
}
