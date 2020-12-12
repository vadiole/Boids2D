package vadiole.boids2d.global.extensions

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import vadiole.boids2d.BuildConfig


fun Fragment.launchOrDownloadApp(appId: String, errorMessage: Int) {
    context?.let {
        val intent = it.packageManager.getLaunchIntentForPackage(appId)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent != null) startActivity(intent) else openGooglePlay(appId, errorMessage)
    }
}

fun Fragment.openGooglePlay(appId: String, errorMessage: Int) {
    val appUrl = "https://play.google.com/store/apps/details?id=$appId"
    val gplayIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(appUrl)
        `package` = ("com.android.vending")
    }

    activity?.let {
        if (gplayIntent.resolveActivity(it.packageManager) != null) startActivity(gplayIntent)
        else openUrl(appUrl, errorMessage)
    }
}

fun Fragment.share(text: String) = startActivity(Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, text)
    type = "text/plain"
})

fun Fragment.toClipboard(label: String, text: String) {
    val systemService =
        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val myClip = ClipData.newPlainText(label, text)
    systemService.setPrimaryClip(myClip)
}

@SuppressLint("DefaultLocale")
fun Fragment.openUrl(url: String, errorMessage: Int) {
    activity?.let {
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url.trim().replace(" ", "+").toLowerCase())
        }
        if (webIntent.resolveActivity(it.packageManager) != null) startActivity(webIntent)
        else Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
}

fun Fragment.setLightStatusBar() {
    activity?.window?.decorView?.let {
        var flags = it.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        it.systemUiVisibility = flags
    }
}

fun Fragment.clearLightStatusBar() {
    activity?.window?.decorView?.let {
        var flags = it.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        it.systemUiVisibility = flags
    }
}


/**
 * Метод для получения размеров экрана
 *
 * @return Point: x - width, y - height
 */
fun Fragment.getScreenSize(): Point {
    requireActivity().windowManager.defaultDisplay.run {
        val size = Point()
        getSize(size)
        return size
    }
}

fun Fragment.getScreenDensity(): Float {
    return resources.displayMetrics.density
}


@SuppressLint("UseCompatLoadingForDrawables")
fun Fragment.getDrawable(id: Int): Drawable? {
    return requireContext().getDrawable(id)
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Fragment.getAnimatedDrawable(id: Int): AnimatedVectorDrawable? {
    return (requireContext().getDrawable(id) as? AnimatedVectorDrawable)
}

fun Fragment.getColor(id: Int): Int {
    return ContextCompat.getColor(requireContext(), id)
}

fun Fragment.getDimen(id: Int): Float {
    return requireContext().resources.getDimension(id)
}

fun Fragment.getDimenInt(id: Int): Int {
    return requireContext().resources.getDimension(id).toInt()
}

fun Fragment.getAnimation(id: Int, onAnimationEnd: (Animation?) -> Unit = {}): Animation {
    val animation = AnimationUtils.loadAnimation(requireContext(), id)
    if (onAnimationEnd != {}) animation.setAnimationListener(end = onAnimationEnd)
    return animation
}

fun Fragment.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.openAppSystemSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    })
}

fun Fragment.canRequestPermissionAgain(permission: String): Boolean {
    return (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission))
}

fun Fragment.requireAppContext(): Context {
    return requireContext().applicationContext
}
