# UI / Copy refinement — 2026-08-30

## Copy action
- Recognition result keeps one primary copy action only: **复制tag** in Chinese.
- The action copies the app's logically filtered/limited recognition tags (`limitedTags`), not raw unfiltered output.
- Removed visible duplicate copy actions from the recognition prompt card, negative prompt card, floating copy button, model comparison result, prompt record dialog, and batch result dialog.
- The underlying clipboard helper remains app-controlled and uses the Android clipboard service; there is no selectable/native-copy UI added to the tag result.

## First launch
- Replaced the three stacked first-run dialogs with one full-screen modal flow.
- Flow pages: Welcome → Privacy → Language.
- Privacy requires an explicit **Agree & continue** action.
- Language selection is presented as a custom large-rounded selection surface, not a copy of the reference.
- Uses large 30–38dp corner radii, layered decorative shapes, spring indicators, and animated page transitions.
- Legacy dialogs remain available from Settings and are suppressed during first launch so dialogs cannot stack.
- After completion the activity recreates once to apply the selected locale cleanly.
