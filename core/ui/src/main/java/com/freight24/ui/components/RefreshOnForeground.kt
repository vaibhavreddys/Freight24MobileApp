package com.freight24.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Re-runs [onRefresh] every time the screen comes back to the foreground — the
 * user switched apps and came back, or navigated here from another screen.
 *
 * Data shown on a docket goes stale the moment it leaves the screen, so the
 * refresh is a correctness fix rather than a nicety.
 *
 * [onRefresh] must be **silent**: it has to refresh while the old content stays
 * on screen, and only replace it once a genuinely different payload arrives. A
 * refresh that raises a full-screen loading flag will blank the screen on every
 * app switch, which is worse than showing data that is a few seconds old. Most
 * ViewModels here already expose a separate `onRefresh()` that does this —
 * prefer it over the first-load entry point.
 *
 * This fires for the *screen*, so it also covers returning from a detail page.
 * Screens that navigate back through a `savedStateHandle` signal do not need it,
 * and screens holding unsaved input (forms, search, editors) must not use it —
 * a refresh there discards what the user typed.
 */
@Composable
fun RefreshOnForeground(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // The observer outlives a single composition, so it would otherwise call the
    // callback captured on the first pass — a stale ViewModel or state reader.
    val refresh by rememberUpdatedState(onRefresh)

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) refresh()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
