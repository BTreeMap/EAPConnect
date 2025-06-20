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
import java.security.KeyStore
import java.security.cert.X509Certificate

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
//                    TimeText()
//                    Spacer(Modifier.height(8.dp))

                    if (!hasPermission) {
                        Button(onClick = {
                            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text(text = stringResource(R.string.grant_location))
                        }
                    } else {
//                        Text(text = "Permission is working")
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
        try {
            // Load the system trusted certificates from AndroidCAStore
            val keyStore = KeyStore.getInstance("AndroidCAStore")
            keyStore.load(null)
            val certList = keyStore.aliases().toList().mapNotNull { alias ->
                keyStore.getCertificate(alias) as? X509Certificate
            }
            val certArray = certList.toTypedArray()

            // Configure WifiEnterpriseConfig with the system trusted certificates
            val enterpriseConfig = WifiEnterpriseConfig().apply {
                setIdentity(identity)
                setPassword(password)
                eapMethod = WifiEnterpriseConfig.Eap.PEAP
                phase2Method = WifiEnterpriseConfig.Phase2.MSCHAPV2
                domainSuffixMatch = domain
                caCertificates = certArray
            }

            // Build the WifiNetworkSuggestion
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setPriority(1)
                .build()

            // Add the network suggestion
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))

            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                Log.i("EAPConnect", "Suggestion added; approve via notification")
            } else {
                Log.e("EAPConnect", "Failed to add suggestion: $status")
            }
        } catch (e: Exception) {
            Log.e("EAPConnect", "Error configuring network suggestion", e)
            // Optionally, handle the error in the UI if needed
        }
    }
}
