# AnimeBox 🎬

AnimeBox is an early-stage anime discovery and dubbing-community project.

## Project structure

- `index.html` — project launch page with links to the prototypes
- `apps/user/index.html` — user panel prototype
- `apps/admin/index.html` — admin panel prototype
- `website/index.html` — public landing page prototype
- `shared/style.css` — shared responsive dark theme

## Try it

Open any HTML file in a modern browser, or enable GitHub Pages in repository settings and choose the `main` branch / root folder to preview the root page online.

## Current status

This repository currently contains front-end prototypes only. Authentication, database, real video streaming, actual file downloads, payments, email delivery, and Discord integration are not connected. Premium actions in the admin page are demonstration-only and do not grant real entitlements.

## Planned architecture

- User app and admin app with separate access controls
- Shared backend API and database
- Silver and Gold membership durations managed server-side
- UID-based manual membership management with audit history
- Support via email, in-app tickets, and Discord
- Download concurrency policies: Free 1, Silver up to 3, Gold up to 5
- Private media storage and short-lived authorized media access

**Security:** Never commit passwords, API keys, database URLs with credentials, or Backblaze B2 secrets to this repository.