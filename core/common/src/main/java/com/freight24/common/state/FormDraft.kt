package com.freight24.common.state

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A form draft that survives the process being killed while the app sits in the
 * background.
 *
 * A ViewModel on its own is not enough for a long form. Android can kill a
 * backgrounded process at any time to reclaim memory, and when the user returns
 * the Activity, the navigation graph and the ViewModel scoped to a back stack
 * entry are both rebuilt from scratch. Anything the operator typed lives only in
 * that ViewModel's `StateFlow`, so it is gone — the form comes back blank and
 * the work is lost. That is exactly what `SavedStateHandle` exists for: it is
 * written into the instance-state bundle on every `onSaveInstanceState`, long
 * before the process is at risk.
 *
 * ## Why JSON rather than putting the state object in the handle directly
 *
 * `SavedStateHandle` only accepts primitives, arrays and `Parcelable` /
 * `Serializable`. This codebase's API models are kotlinx-serializable data
 * classes, which are neither, so a draft is encoded to a single JSON string and
 * decoded back on restore. That also keeps the bundle small, which matters:
 * saved instance state shares a budget with the rest of the bundle and is
 * written on every state change.
 *
 * So keep drafts narrow — the fields a person actually typed and the few objects
 * they picked. Do not put whole lookup tables or lists of search results in a
 * draft; they are cheap to refetch and expensive to carry.
 *
 * ## Usage
 *
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel(private val handle: SavedStateHandle) : ViewModel() {
 *     private val draft = FormDraft(handle, "my_form", MyDraft.serializer())
 *
 *     private val _state = MutableStateFlow(draft.restore()?.toState() ?: MyState())
 *     val state: StateFlow<MyState> = _state.asStateFlow()
 *
 *     init {
 *         // Persist after the initial restore has been applied, or the empty
 *         // starting state would immediately overwrite the saved draft.
 *         _state.drop(1).onEach { draft.save(MyDraft.from(it)) }
 *             .launchIn(viewModelScope)
 *     }
 * }
 * ```
 */
class FormDraft<T : Any>(
    private val handle: SavedStateHandle,
    private val key: String,
    private val serializer: KSerializer<T>,
) {
    /**
     * The saved draft, or null when this is a genuinely fresh form.
     *
     * Returns null rather than throwing when the saved value cannot be read —
     * a payload written by an older build of the app, or a model that changed
     * shape. A blank form is a recoverable annoyance; a crash on return from the
     * background is not. The read sits inside the guard too: `SavedStateHandle.get`
     * is an unchecked cast, so a key holding an unexpected type would throw at
     * the call site rather than inside a surrounding `runCatching`.
     */
    fun restore(): T? =
        runCatching {
            handle.get<String>(key)?.let { raw -> json.decodeFromString(serializer, raw) }
        }.getOrNull()

    /** Writes [draft] so it is available after the process is killed. */
    fun save(draft: T) {
        // Never let a serialization failure take down the form: the operator
        // would lose the text they are looking at rather than the backup of it.
        val encoded = runCatching { json.encodeToString(serializer, draft) }.getOrNull()
        if (encoded == null) {
            handle.remove<String>(key)
        } else {
            handle[key] = encoded
        }
    }

    /** Forgets the draft. Call once the form's work is safely submitted. */
    fun clear() {
        handle.remove<String>(key)
    }

    private companion object {
        /**
         * `ignoreUnknownKeys` so a draft written before a field was removed (or
         * renamed) still restores the fields that do still exist, instead of
         * failing the whole decode and blanking the form.
         */
        val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
    }
}
