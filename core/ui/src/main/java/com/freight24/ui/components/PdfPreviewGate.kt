package com.freight24.ui.components

import android.content.Context
import android.os.SystemClock

/**
 * Process-wide guard that collapses repeated taps on a document into a single
 * PDF viewer.
 *
 * [getPdfLaunchIntent] builds an intent for `PdfViewerActivity`, which the
 * library declares with no `launchMode` and therefore with the default
 * `standard`. Every `startActivity` for a `standard` activity creates a new
 * instance, so a double tap on a document opened two viewers: two activities,
 * two PDF downloads of the same file, and a Back press that appears to do
 * nothing because it only closes the copy on top.
 *
 * Screening at the launch site is what actually fixes it. Blocking inside the
 * ViewModels instead would only suppress the second *request* while leaving the
 * second `startActivity` free to run once its own fetch landed, and six of the
 * eleven signed-URL fetches have no gate at all.
 *
 * The window is deliberately wider than the platform double-tap timeout
 * ([android.view.ViewConfiguration.getDoubleTapTimeout], 300 ms). This is not
 * gesture recognition: it has to stay closed until the viewer has actually
 * covered the calling screen, otherwise a slightly slow third tap still lands.
 * Once the viewer is up the button behind it is unreachable, so a longer window
 * costs nothing — the only case it can suppress is a deliberate second open
 * within the window, which after backing out of a viewer is not a real flow.
 */
private object PdfPreviewGate {
    private val lock = Any()

    /** Uptime of the last accepted launch; `0` means the gate is open. */
    private var lastLaunchAt = 0L

    /**
     * Warns that a launch must not run before this many milliseconds have
     * passed since the last accepted one.
     */
    const val GUARD_MS = 800L

    /** Claims the gate. Returns false when a launch is already under way. */
    fun acquire(): Boolean =
        synchronized(lock) {
            val now = SystemClock.uptimeMillis()
            if (now - lastLaunchAt < GUARD_MS) return false
            lastLaunchAt = now
            true
        }

    /**
     * Hands the gate back early. Called when a launch fails, so a device with
     * no PDF handler — or a transient failure — cannot lock the user out of
     * every document for the rest of the window.
     */
    fun release() =
        synchronized(lock) {
            lastLaunchAt = 0L
        }
}

/**
 * Opens [url] in the PDF viewer, ignoring taps that arrive while another viewer
 * is still being launched.
 *
 * Prefer this over `context.startActivity(getPdfLaunchIntent(url, context))` at
 * every document call site — the intent builder alone has no idea how many times
 * it is about to be used.
 *
 * @return true when the viewer was started, false when the tap was suppressed as
 *   a duplicate. Suppressed taps are not an error: the user already has the
 *   document they asked for on screen.
 */
fun launchPdfPreview(
    url: String,
    context: Context,
): Boolean {
    if (!PdfPreviewGate.acquire()) return false

    return runCatching { context.startActivity(getPdfLaunchIntent(url, context)) }
        .fold(
            onSuccess = { true },
            onFailure = {
                // No PDF handler on the device, or the activity is blocked.
                // Give the gate straight back so the next tap can try again.
                PdfPreviewGate.release()
                false
            },
        )
}
