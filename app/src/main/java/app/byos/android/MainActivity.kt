package app.byos.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.byos.android.ui.ByosApp
import app.byos.android.ui.LibraryViewModel
import app.byos.android.ui.theme.ByosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            val vm: LibraryViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val playback by vm.playback.state.collectAsStateWithLifecycle()
            val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
            var hadPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                hadPermission = granted
                if (granted) vm.scan() else vm.permissionDenied()
            }
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                if (granted && !hadPermission) vm.scan()
                if (!granted && hadPermission) vm.permissionDenied()
                hadPermission = granted
            }
            LaunchedEffect(Unit) {
                if (BuildConfig.DEBUG && intent.getBooleanExtra("preview", false)) vm.preview()
                else if (hadPermission) vm.scan()
            }
            ByosTheme {
                ByosApp(state, playback, vm, onScan = {
                    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) vm.scan()
                    else launcher.launch(permission)
                }, onSettings = { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) })
            }
        }
    }
}
