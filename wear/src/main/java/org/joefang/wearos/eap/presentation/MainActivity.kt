package org.joefang.wearos.eap.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Text
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.TimeText
import org.joefang.wearos.eap.R
import org.joefang.wearos.eap.ui.WiFiConfigScreen
import org.joefang.wearos.eap.theme.WiFiSuggestTheme

class MainActivity : ComponentActivity() {
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Log.e("EAPConnect", "Location permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            WiFiSuggestTheme {
                val context = LocalContext.current
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimeText()
                    Spacer(Modifier.height(8.dp))

                    if (!hasPermission) {
                        Button(onClick = {
                            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text(text = stringResource(R.string.grant_location))
                        }
                    } else {
                        WiFiConfigScreen { ssid, identity, password, domain ->
                            suggestEnterpriseNetwork(context, ssid, identity, password, domain)
                        }
                    }
                }
            }
        }
    }

    private fun suggestEnterpriseNetwork(
        context: Context,
        ssid: String,
        identity: String,
        password: String,
        domain: String
    ) {
        val enterpriseConfig = WifiEnterpriseConfig().apply {
            setIdentity(identity)
            setPassword(password)
            setEapMethod(WifiEnterpriseConfig.Eap.PEAP)
            setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2)
            setDomainSuffixMatch(domain)
        }

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2EnterpriseConfig(enterpriseConfig)
            .setPriority(1)
            .build()

        val status = (context.getSystemService(WIFI_SERVICE) as WifiManager)
            .addNetworkSuggestions(listOf(suggestion))

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Log.i("EAPConnect", "Suggestion added; approve via notification")
        } else {
            Log.e("EAPConnect", "Failed to add suggestion: $status")
        }
    }
}
