# Issue tracker: Local Markdown

Issues and specs for this repo live as markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`
- The spec is `.scratch/<feature-slug>/spec-<feature-slug>.md`
- Implementation issues are one file per ticket at `.scratch/<feature-slug>/issues/<NN>-<slug>.md`, numbered from `01`, never a single combined tickets file
- Triage state is recorded as a `**Status:**` line near the top of each issue file (see "Status")
- Comments and conversation history append to the bottom of the file under a `## Comments` heading
- Specs and issues are written in Spanish: titles, body, section headings and comments

## Status

Using the status is mandatory. Every issue file has a `**Status:**` line near the top, and the agent keeps it up to date:

- `open`: set when the ticket is created.
- `claimed`: the agent sets it **before** starting any work on the ticket, and saves the file.
- `done`: the agent sets it when it finishes the ticket. It also marks with `[x]` every check that was met; a check that was not met stays `[ ]` and the agent tells the user.

If the file uses frontmatter (like `spec-<feature-slug>.md`), the status goes in its `estado:` field, and the closing value is `cerrada`.

Wayfinding tickets follow the same rule, but close with `resolved` instead of `done` (see "Wayfinding operations").

## When a skill says "publish to the issue tracker"

Create a new file under `.scratch/<feature-slug>/` (creating the directory if needed).

## When a skill says "fetch the relevant ticket"

Read the file at the referenced path. The user will normally pass the path or the issue number directly.

## Wayfinding operations

Used by `/wayfinder`. The **map** is a file with one **child** file per ticket.

- **Map**: `.scratch/<effort>/map.md` (the Notes / Decisions-so-far / Fog body).
- **Child ticket**: `.scratch/<effort>/issues/NN-<slug>.md`, numbered from `01`, with the question in the body. A `Type:` line records the ticket type (`research`/`prototype`/`grilling`/`task`); a `Status:` line records `claimed`/`resolved`.
- **Blocking**: a `Blocked by: NN, NN` line near the top. A ticket is unblocked when every file it lists is `resolved`.
- **Frontier**: scan `.scratch/<effort>/issues/` for files that are open, unblocked, and unclaimed; first by number wins.
- **Claim**: set `Status: claimed` and save before any work.
- **Resolve**: append the answer under an `## Answer` heading, set `Status: resolved`, then append a context pointer (gist + link) to the map's Decisions-so-far in `map.md`.
