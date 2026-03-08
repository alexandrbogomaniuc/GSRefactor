# NanoBanana Phase 1 Review Pack

This folder is a partial runtime-oriented QA pack assembled from the committed NanoBanana source branch for game `7000`.

It is intentionally narrower than the OpenAI pack:

- committed backgrounds are present for desktop, landscape, and portrait,
- committed symbol and UI contact sheets are mirrored into `runtime/`,
- no committed runtime-ready VFX atlas was present on the source branch,
- QA should expect provider validation to report the missing VFX payload and fall back safely.

The source branch delivered review/source art under `raw-assets/providers/nanobanana/` rather than a runtime-ready `assets/providers/nanobanana/` folder, so this pack is an engineering assembly for QA beta only.
