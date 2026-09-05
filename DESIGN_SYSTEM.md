<android_compose_aesthetics>
You tend to converge toward generic, "on distribution" outputs. In Android UI, this creates the
"default Material" aesthetic: stock Roboto, the default purple seed color, cards-in-a-Column
layouts, and a FAB in the bottom-right because that's what every tutorial does. Avoid this: make
creative, distinctive screens that feel designed, not scaffolded. Focus on:

Typography: Choose type that has character. Material 3 Expressive is built around a wider,
more expressive type scale — use it. Pull in a distinctive Google Font via
`androidx.compose.ui.text.googlefonts` (downloadable fonts) or bundle a variable font instead of
shipping the default Roboto with no customization. Vary weight and optical size deliberately
(Roboto Flex, or a display face with real personality for headlines) rather than one weight
scaled up and down. Define the full scale in `Type.kt` as named `TextStyle` tokens exposed through
`MaterialTheme.typography.*` — never inline `fontSize`/`fontWeight` on a `Text` composable.

Color & Theme: Commit to a cohesive palette, expressed entirely through
`MaterialTheme.colorScheme.*` — no hardcoded hex, ever, only semantic tokens defined in
`core:ui`. M3 Expressive gives you a much larger role set than base M3
(`surfaceContainerLowest` → `surfaceContainerHighest`, `primaryFixed`/`primaryFixedDim`,
tertiary as a real accent, not an afterthought) — use the expanded roles instead of leaning on
primary/secondary for everything. Generate your seed color deliberately (brand color, not the
Material Theme Builder default purple), and build distinct, intentional light AND dark
`ColorScheme` objects — dark mode should feel authored, not auto-inverted. Dominant color with a
sharp accent beats a timid, evenly-distributed palette here too.

Motion: M3 Expressive's whole premise is spring-based, physical motion — use
`MaterialTheme.motionScheme` (`fastSpatialSpec`, `slowEffectsSpec`, etc.) instead of default
tween/ease curves. Prioritize one well-orchestrated screen-entry choreography — staggered
`AnimatedVisibility` reveals across list items via increasing delay, shared-element/container
transforms on navigation (`SharedTransitionLayout`), `Modifier.animateBounds` for layout shifts —
over scattering isolated micro-interactions everywhere. Let shapes move: M3 Expressive's
`MaterialShapes` morphing (pill ↔ cookie ↔ star) on FABs, buttons, and loading indicators is a
signature move — use it somewhere it earns its keep, not everywhere.

Backgrounds & Surfaces: Build depth instead of defaulting to a flat `surface` color. Layer
tonal `surfaceContainer` steps to establish elevation without relying on shadow alone; use
`Brush.linearGradient`/`radialGradient` sparingly and intentionally (built from your token colors,
not raw hex) for hero or header regions; consider `Modifier.blur` / haze effects for
glassmorphic surfaces if that fits the brand; use the new expressive shape system to break up
rectangular monotony on cards and containers instead of uniform `RoundedCornerShape(12.dp)`
everywhere.

Avoid generic AI-generated Android aesthetics:
- Stock Roboto at default weight with no type-scale personality
- The default Material purple/violet seed color and its auto-generated tonal palette
- Predictable Card-in-LazyColumn-with-8dp-padding layouts
- FAB bottom-right + 3-item BottomNavigationBar as an unexamined default
- Flat `surface` everywhere with elevation conveyed only through shadow
- Uniform 12dp rounded corners on every single component

Interpret creatively and make choices that feel specifically designed for THIS product's context
— not a template. Vary theme, type pairing, and motion character across screens where the product
context calls for it. You still tend to converge on the same "safe" expressive choices
(same seed hue, same shape morph, same spring spec) across generations — actively resist that.
Think outside the box, but every color must still resolve through `MaterialTheme.colorScheme.*`
and compile cleanly across both `app-trucker` and `app-connect`.
</android_compose_aesthetics