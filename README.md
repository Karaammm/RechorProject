# ReCHor

ReCHor is a JavaFX public-transport journey planner. It loads indexed
timetable data from a local `timetable-*` directory, computes journeys, and
displays them in a desktop GUI.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer
- Optional local timetable dataset (see [Timetable data](#timetable-data))

JavaFX and JUnit are declared in `ReCHor/pom.xml` and are downloaded from
Maven Central. No local JavaFX SDK installation or IntelliJ-specific library
path is required.

## Build and test

From the repository root:

```sh
cd ReCHor
mvn test
```

The build compiles the sources in `src/`, tests in `test/`, and packages the
files in `resources/` as application resources.

The dataset-dependent `RouterTest` is excluded from the default test run
because timetable binaries are not part of the repository. Once the required
local snapshots are present, it can be run explicitly with
`mvn -Dtest=RouterTest test`.

## Run

The application starts with a small in-memory demo timetable, so it can be
tried immediately after cloning:

```sh
cd ReCHor
mvn exec:java
```

To use a real external snapshot instead, place it next to `pom.xml` and pass
its path:

```sh
mvn exec:java -Dexec.args="--timetable timetable-18"
```

Run commands from the `ReCHor` directory so relative paths resolve correctly.
The demo timetable contains the synthetic stations EPFL, Renens, Lausanne, and
Genève and is intended only to verify the UI and routing flow.

## Timetable data

Timetable files are large binary runtime data and are intentionally not
committed to this Git repository. They are not pulled or refreshed regularly.
Only the following timetable snapshots are available locally:

| Dataset | Available dates |
| --- | --- |
| `timetable-12` | 2025-03-17 to 2025-03-23 |
| `timetable-13` | 2025-03-24 to 2025-03-30 |
| `timetable-14` | 2025-03-31 to 2025-04-06 |
| `timetable-15` | 2025-04-07 to 2025-04-13 |
| `timetable-16` | 2025-04-14 to 2025-04-20 |
| `timetable-17` | 2025-04-21 to 2025-04-27 |
| `timetable-18` | 2025-04-28 to 2025-05-04 |
| `timetable-19` | 2025-05-05 to 2025-05-11 |

The application requires the selected snapshot to contain the requested
date's `connections.bin`, `connections-succ.bin`, and `trips.bin` files.
Consequently, with the currently available data, use the snapshot containing
the requested date; the default `timetable-18` path supports dates from
2025-04-28 through 2025-05-04.

## Project layout

- `ReCHor/src/` — application and domain source code
- `ReCHor/test/` — JUnit tests
- `ReCHor/resources/` — JavaFX stylesheets and transport icons
- `ReCHor/timetable-*` — optional local timetable snapshots, ignored by Git
- `ReCHor/src/ch/epfl/rechor/timetable/demo/` — small built-in timetable for a
  zero-download first run
- `ReCHor/pom.xml` — reproducible Maven build configuration

`ReCHor/` is the only project directory in this repository. The repository
itself is `RechorProject`; there is no second nested Git repository. Maven is
the single source of truth for the project, so IDE module files and copied JARs
are intentionally not tracked.

## IDE support

The project can be opened in IntelliJ IDEA or VS Code. IDE metadata is not
required: Maven is the source of truth for dependencies, compilation, and
tests.
