package com.freight24.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===========================================================================
// Docket primitives — the shared "clearing desk" visual language.
//
// The screens speak freight paperwork rather than generic Material cards:
//   • the LANE SPINE  — a 4dp colour bar down the left edge of every card,
//                       keyed to status. Reads like a file tab in a physical
//                       docket tray.
//   • the DOG-EAR     — a cut top-right corner with the folded half painted a
//                       shade darker, so the card reads as a document.
//   • the PERFORATION — a dashed tear line separating a docket body from its
//                       footer.
//   • the META TAG    — a small tracked-caps stamp for counts and markers,
//                       optionally hatch-marked for onboarding content.
//
// First authored on VerificationRequestsScreen; extracted here so the More
// desk and the Leads tray use the exact same primitives instead of forking.
// ===========================================================================

/** Size of the dog-ear cut on the top-right of every docket card. */
val DocketCut = 16.dp

/** Width of the status spine on the left edge of every docket card. */
val SpineWidth: Dp = 4.dp

/** Card silhouette with the dog-ear cut. */
val DocketShape = CutCornerShape(topEnd = DocketCut)

/** Small panels: filter chips, ledger rows, queue rail cells. */
val PanelShape = RoundedCornerShape(10.dp)

/** Tiny stamps and tally badges. */
val TagShape = RoundedCornerShape(4.dp)

/**
 * A docket card: status spine down the left edge, dog-eared top-right corner,
 * hairline border. All content sits in the padded column to the right of the
 * spine.
 *
 * Pass a non-null [onClick] to make the whole card one tap target (list
 * dockets); pass null for a static shell (section dockets with their own
 * per-row handling).
 *
 * The spine is painted by an overlay sized with `matchParentSize`, deliberately
 * **not** by a `Row(Modifier.height(IntrinsicSize.Min))` with a `fillMaxHeight`
 * bar. Intrinsic measurement is a hard `IllegalStateException` for anything built
 * on `SubcomposeLayout` — lazy lists, `BoxWithConstraints`, `TabRow` — so a city
 * picker's suggestion list crashed the screen it was opened in. It also silently
 * collapsed multi-line text fields, because `BasicTextField`'s minimum intrinsic
 * height ignores `minLines`. Measuring content once, normally, avoids both.
 */
@Composable
fun DocketCard(
    accent: Color,
    testTag: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 14.dp, end = 14.dp, top = 13.dp, bottom = 12.dp,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    val foldTone = MaterialTheme.colorScheme.surfaceContainerHighest
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    val layoutDirection = LocalLayoutDirection.current

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .semantics { testTagsAsResourceId = true }
                .then(clickableModifier),
            shape = DocketShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(
                        start = contentPadding.calculateStartPadding(layoutDirection) + SpineWidth,
                        end = contentPadding.calculateEndPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                    content = content,
                )

                // Lane spine — status colour, matched to the card's measured height.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            drawRect(
                                color = accent,
                                size = Size(SpineWidth.toPx(), size.height),
                            )
                        },
                )
            }
        }

        // Folded corner: the visible half of the dog-ear notch, painted a shade
        // darker so the cut reads as paper folded back rather than a clipped box.
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(DocketCut),
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = foldTone)
        }
    }
}

/** Dashed tear line between a docket's body and its footer. */
@Composable
fun PerforationRule(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 7f), 0f),
        )
    }
}

/** Diagonal hazard hatch used to mark new-onboarding content. */
@Composable
fun HatchMark(
    modifier: Modifier = Modifier,
    color: Color,
    step: Float = 5f,
) {
    Canvas(modifier = modifier) {
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = color,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 1.6f,
            )
            x += step
        }
    }
}

/** Small tracked-caps tag. Used for meta counts and NEW / REMOVED markers. */
@Composable
fun MetaTag(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    hatch: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = TagShape,
        color = container,
    ) {
        Row(
            modifier = Modifier.height(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hatch) {
                HatchMark(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight(),
                    color = content.copy(alpha = 0.55f),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Bold,
                color = content,
                modifier = Modifier.padding(
                    start = if (hatch) 0.dp else 7.dp,
                    end = 7.dp,
                ),
            )
        }
    }
}
