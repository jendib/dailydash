# DailyDash

DailyDash is a dashboard displayed in browser kiosk mode on a screen. It provides an at-a-glance view of the shopping list, upcoming calendar events, weather, Home Assistant sensors, and recent photos.

The main usage is passive viewing from a distance on an always-on screen. Keep changes readable, unobtrusive, and suitable for unattended operation. The repository contains the web application and API; launching the browser in kiosk mode is handled outside this project.

## Architecture and code map

| Location | Purpose |
| --- | --- |
| `web/src/App.vue` | Dashboard layout, data loading, and refresh timer; currently the entire dashboard lives here |
| `web/src/main.js` | Vue initialization and Axios API base URL |
| `web/src/assets/main.css` | Tailwind CSS entry point |
| `web/public/config.js` | Browser API URL for local development |
| `web/nginx.conf` | Production OpenResty server and runtime `/config.js` generation |
| `api/src/main/java/org/bgamard/dailydash/resource/` | HTTP endpoints and integration orchestration |
| `api/src/main/java/org/bgamard/dailydash/client/` | MicroProfile REST clients for external services |
| `api/src/main/java/org/bgamard/dailydash/config/` | SmallRye configuration mappings |
| `api/src/main/java/org/bgamard/dailydash/model/` | Integration payloads and dashboard response models |
| `api/src/main/resources/application.properties` | API configuration, CORS, logging, and native build arguments |
| `api/src/main/resources/reflection-config.json` | Reflection metadata used by native builds |
| `docker-compose.yml` | Deployment using published API and web images |
| `.github/workflows/build.yml` | Builds and publishes both images on pushes to `main` |

The backend uses Java 25, Quarkus 3.40.1.2, Maven, Quarkus REST/Jackson, and REST clients. The frontend uses Vue 3, Vite 6, Tailwind CSS 3, Axios, and Heroicons. There is no application database or persistence layer.

The browser calls the API for private integrations and calls Open-Meteo directly for weather. Integration credentials stay on the API side. Photo previews pass through the API so the browser does not need the Immich key.

## Features and endpoints

All API paths below are relative to the API root.

| Feature | Source and current behavior |
| --- | --- |
| Shopping list | `GET /shopping-list`: authenticates through `gpsoauth`, fetches Google Keep changes, and returns unchecked `LIST_ITEM` text for the configured parent node |
| Calendar | `GET /calendar`: downloads the configured iCalendar feed, parses it with ical4j, filters events by their end date (or start date if no end exists), and sorts by start date |
| Household sensors | `GET /home-assistant`: returns indoor temperature, CO2, balcony temperature, and today's/tomorrow's Tempo colors from Home Assistant |
| Photos | `GET /photos/latest`: searches Immich for images with descending order and a size of 10; the UI displays the first nine IDs |
| Photo previews | `GET /photos/{id}/preview`: proxies the Immich thumbnail endpoint with `size=preview` |
| Weather | The browser requests Open-Meteo directly; `GET /weather` is only a placeholder returning `OK` |

## Local development

Use JDK 25 and the checked-in Maven wrapper for the API. The wrapper downloads Maven 3.9.11. For frontend development, install Node.js/npm compatible with the versions in `web/package.json`; the existing web Docker build uses Node 18.

Configure the integration environment variables below before starting the API. From `api/`:

```powershell
# Windows PowerShell
./mvnw.cmd quarkus:dev
```

```sh
# Linux/macOS
bash ./mvnw quarkus:dev
```

The API normally listens on `http://localhost:8080`. In another terminal, from `web/`:

```sh
npm ci
npm run dev
```

Open the address printed by Vite. `web/public/config.js` defaults to `http://localhost:8080`. There is no Vite API proxy: the browser connects to that URL directly. `web/index.html` loads `/config.js` before the app, and `main.js` uses `window.API_URL` for both Axios requests and photo URLs. This is runtime configuration, not a `VITE_*` variable.

## Configuration

`docker-compose.yml` lists the deployment environment variables. Supply real values locally or through the deployment environment; never commit them.

| Variable | Meaning |
| --- | --- |
| `DAILYDASH_CALENDAR_URL` | iCalendar feed URL; treat private feed URLs as credentials |
| `DAILYDASH_GOOGLEKEEP_MASTER_TOKEN` | Google Keep authentication master token |
| `DAILYDASH_GOOGLEKEEP_NODE_ID` | Parent node ID of the shopping list |
| `DAILYDASH_GOOGLEKEEP_SESSION_ID` | Session ID included in Google Keep change requests |
| `DAILYDASH_HOMEASSISTANT_TOKEN` | Home Assistant bearer token |
| `DAILYDASH_IMMICH_KEY` | Immich API key |
| `QUARKUS_REST_CLIENT_HOMEASSISTANT_URL` | Home Assistant server base URL; the client adds `/api` |
| `QUARKUS_REST_CLIENT_GOOGLEKEEP_URL` | Google Keep service base URL; the client adds `/notes` |
| `QUARKUS_REST_CLIENT_IMMICH_URL` | Immich server base URL; the client adds `/api` |
| `API_URL` | API base URL reachable by the kiosk browser, used by the production web container |

A root `.env` file can supply Compose variable interpolation and is ignored by Git. It is not automatically exported into a shell running Maven from `api/`; set the API process environment separately for local development.

In production, OpenResty generates `/config.js` from its `API_URL` environment variable. Use an address the kiosk browser can reach, not a Compose-only service hostname. Jackson and the container launch commands use `Europe/Paris`; the current Open-Meteo URL separately requests `Europe/Berlin`.

## Build, validation, and deployment

Run these commands from their respective module directories:

```powershell
# api/: JVM compilation, packaging, and available checks
./mvnw.cmd -B clean verify
```

```sh
# web/: production frontend bundle
npm ci
npm run build

# api/: native executable with a local GraalVM 25 toolchain
bash ./mvnw -B clean verify -Pnative

# api/: native executable using a container toolchain instead
bash ./mvnw -B clean verify -Pnative -Dquarkus.native.container-build=true
```

The JVM package runs with `java -jar target/quarkus-app/quarkus-run.jar` from `api/`. See [the API README](api/README.md) for additional packaging commands.

There are currently no API test sources or frontend test/lint scripts. A successful JVM build checks compilation and Quarkus augmentation, but does not verify live integrations, the kiosk layout, or native execution. For UI work, inspect the dashboard at the kiosk's actual screen size. For integration work, verify the relevant endpoint with configured services. Changes involving reflection or integration models should also be checked with a native build.

On pushes to `main`, GitHub Actions builds the API as a Linux native executable with GraalVM 25 using the runner's installed Maven (`mvn -B clean package -Pnative`) and packages it using `api/src/main/docker/Dockerfile.native`. CI uses installed Maven to avoid depending on the wrapper JAR bootstrap download; local development can use the wrapper. The web image uses a Vite build served by OpenResty. Both are published to GHCR with the `latest` tag:

- `ghcr.io/jendib/dailydash/api:latest`
- `ghcr.io/jendib/dailydash/web:latest`

The separate `Dockerfile.jvm` supports JVM deployments but is not used by the current publishing workflow. Compose consumes published images rather than building the local source:

```sh
docker compose up -d
```

Compose uses `pull_policy: always`, restarts both services automatically, and exposes the API on host port `10301` and the web UI on `10302`.

## Notes for future agents

- There is no application authentication or authorization, and API CORS allows all origins. The dashboard exposes data and should be deployed on a trusted network or behind appropriate access controls.
- This is a public repository. Keep tokens, private calendar URLs, personal photos, and local HTTP request histories out of commits. `.idea/` and `.env` are ignored; ignored IDE files can still contain credentials and are not project documentation.
- Keep changes scoped to the relevant module. Check both frontend consumers and backend response models when changing an API contract, and state which build/runtime checks were actually performed.
