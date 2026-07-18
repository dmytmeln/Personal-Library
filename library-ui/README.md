# Library UI

Angular web application for Personal Library.

## Prerequisites

* Node.js
* npm
* Library API running on `http://localhost:8080`

## Install

From `library-ui`:

```bash
npm install
```

## Run Locally

```bash
npm start
```

The application starts at `http://localhost:4200`. The development server proxies `/api` requests to `http://localhost:8080` using `proxy.conf.json`.

## Build

Development build:

```bash
npm run build
```

Production build:

```bash
npm run prod
```

Build artifacts are written to `dist/`.

## Run Tests

Unit tests:

```bash
npm test
```

Playwright end-to-end tests:

```bash
npx playwright install
npx playwright test
```

Playwright starts the frontend development server automatically. Start the backend and PostgreSQL separately before tests requiring API access.

## Source Structure

Application code lives under `src/app` and is organized by feature. Shared UI and infrastructure code belongs in focused folders such as `common`, `services`, `guards`, and `interfaces`.
