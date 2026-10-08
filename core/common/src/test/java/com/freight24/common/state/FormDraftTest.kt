package com.freight24.common.state

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@Serializable
private data class Nested(val id: String = "", val label: String? = null)

@Serializable
private data class Envelope(
    val text: String = "",
    val count: Int = 0,
    val flag: Boolean = false,
    val nested: Nested? = null,
    val items: List<Nested> = emptyList(),
)

/**
 * `SavedStateHandle` is rebuilt empty after process death and repopulated from
 * the instance-state bundle, so a second handle over the same keys is what the
 * ViewModel actually sees on return. These tests drive that round trip rather
 * than reusing one handle, which would pass even if nothing was written.
 */
class FormDraftTest {
    private val key = "test_draft"

    private fun handoff(from: SavedStateHandle): SavedStateHandle = SavedStateHandle(mapOf(key to from.get<String>(key)))

    @Test
    fun `restores what was saved`() {
        val original = Envelope(text = "₹45,000", count = 3, flag = true, nested = Nested("a", "b"), items = listOf(Nested("x")))

        val store = SavedStateHandle()
        val draft = FormDraft(store, key, Envelope.serializer())
        draft.save(original)

        val restored = FormDraft(handoff(store), key, Envelope.serializer()).restore()

        assertEquals(original, restored)
    }

    @Test
    fun `returns null when nothing was ever saved`() {
        assertNull(FormDraft(SavedStateHandle(), key, Envelope.serializer()).restore())
    }

    @Test
    fun `defaults survive the round trip so a partial form is not blanked`() {
        val store = SavedStateHandle()
        val draft = FormDraft(store, key, Envelope.serializer())
        draft.save(Envelope(text = "only this"))

        val restored = FormDraft(handoff(store), key, Envelope.serializer()).restore()

        assertEquals(Envelope(text = "only this"), restored)
    }

    @Test
    fun `corrupt payload yields null instead of throwing`() {
        val store = SavedStateHandle()
        store[key] = "{not json at all"

        // A blank form is recoverable; a crash on return from the background is not.
        assertNull(FormDraft(store, key, Envelope.serializer()).restore())
    }

    @Test
    fun `unknown keys from an older build are ignored`() {
        val store = SavedStateHandle()
        store[key] = """{"text":"kept","count":9,"flag":true,"nested":null,"items":[],"retiredField":"gone"}"""

        val restored = FormDraft(store, key, Envelope.serializer()).restore()

        assertEquals(Envelope(text = "kept", count = 9, flag = true), restored)
    }

    @Test
    fun `clear removes the draft`() {
        val store = SavedStateHandle()
        val draft = FormDraft(store, key, Envelope.serializer())
        draft.save(Envelope(text = "typed"))
        draft.clear()

        assertNull(draft.restore())
    }

    @Test
    fun `clear inside save is not resurrected by a later save`() {
        val store = SavedStateHandle()
        val draft = FormDraft(store, key, Envelope.serializer())

        draft.save(Envelope(text = "first"))
        draft.clear()
        // Guards the submit path: the state update that follows a successful
        // submit must not write the spent draft back out.
        assertNull(FormDraft(handoff(store), key, Envelope.serializer()).restore())
    }

    @Test
    fun `a second save replaces the first rather than accumulating`() {
        val store = SavedStateHandle()
        val draft = FormDraft(store, key, Envelope.serializer())

        draft.save(Envelope(text = "one"))
        draft.save(Envelope(text = "two"))

        assertEquals(Envelope(text = "two"), draft.restore())
    }
}
