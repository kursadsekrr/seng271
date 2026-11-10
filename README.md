# Department Course Timetable Management System

An automated, standalone Java decision-support application designed to generate conflict-free weekly schedules for an undergraduate curriculum across four academic year groups while strictly enforcing operational hard constraints and accommodating academic soft preferences.

## Team
| Name | GitHub |
| --- | --- |
| Kürşad Şeker | [@kursadsekrr](https://github.com/kursadsekrr) |
| Yalın Öztürk | [@yalinn](https://github.com/yalinn) |

## Requirements
- Java SE 17 or later (JDK 17, 19, or 21 LTS)
- External libraries: None (Pure Java Standard Library: `java.lang`, `java.util`, `java.io`)

## How to Compile and Run
The project uses package structures (`model`, `scheduler`, `io`, `service`, `ui`). Ensure you compile and run from the repository root directory:

### Command Line / Terminal
```bash
# 1. Compile source files into out directory
javac -d out -sourcepath src src/Main.java

# 2. Run the application
java -cp out Main
```

### Windows PowerShell (If encountering `UnsupportedClassVersionError` / Java 8 conflict)
If your terminal defaults to an older Java version (e.g., class file version 52.0):
```powershell
# Run directly with installed modern JDK (e.g., JDK 19 or JDK 17):
& "C:\Program Files\Java\jdk-19\bin\java.exe" -cp out Main

# Or temporarily adjust the PATH variable for your session:
$env:Path = "C:\Program Files\Java\jdk-19\bin;" + $env:Path
java -cp out Main
```

### Using an IDE (IntelliJ IDEA / Eclipse / VS Code)
1. Open the repository root folder in your IDE.
2. Mark the `src` directory as **Sources Root**.
3. Set the Project SDK / JDK to **Java 17** or higher in Project Structure settings.
4. Run `src/Main.java`. Ensure the working directory is set to the project root so the `./data` and `./output` paths resolve correctly.

## Input Files
| File | Format | Content |
| --- | --- | --- |
| `data/courses.csv` | CSV | Course code, title, year group (1-4), instructor ID, theory hours, lab hours, enrollment, and required room type. |
| `data/instructors.txt` | TXT | Instructor ID, full name, blackout unavailable hours, and preferred time slots (Morning/Afternoon). |
| `data/rooms.csv` | CSV | Room identifier, building name, student capacity, and room classification (Classroom/Laboratory). |
| `data/constraints.txt` | TXT | Pre-assigned fixed time slots, specific room mandates, and priority level (Mandatory/Preferred). |

The detailed schemas and data validation rules are specified in Section 3.8 of the SRS.

## Using the Application
| Menu option | What it does |
| --- | --- |
| **1. Load input data** | Ingests CSV and TXT files, validates records and referential keys, flags malformed lines, and loads domain models into memory (`FR-01`, `FR-02`). |
| **2. Generate timetable automatically** | Executes the two-stage heuristic scheduler: places fixed-time requests (`HC-08`), schedules remaining theory/lab blocks (`HC-01`-`HC-09`), logs soft-constraint warnings (`SC-01`-`SC-04`), and isolates bottlenecks (`FR-03`). |
| **3. View timetable by year group** | Displays a formatted 5-day by 8-hour weekly calendar grid for the selected student cohort (Year 1, 2, 3, or 4) (`FR-04`). |
| **4. View all scheduled sessions** | Lists all committed sessions department-wide with assigned day, time, room, and instructor details (`FR-05`). |
| **5. View unscheduled sessions and warnings** | Summarizes unassigned sessions categorized by root cause (`NO ROOM` vs `CONSTRAINT CONFLICT`) and lists all logged soft-constraint warnings (`FR-05`). |
| **6. Modify / remove an assignment** | Interactively reassigns a session to another day, time, or room with real-time hard-constraint validation guards (`FR-06`). |
| **7. Generate classroom request email** | Formats an official room request letter addressed to the Student Affairs Office for sessions lacking room capacity, suggesting conflict-free time slots (`FR-07`). |
| **8. Save timetable to file** | Serializes schedules into cohort ASCII grids (`timetable_year_<N>.txt`), flat CSV records (`complete_timetable.csv`), and summary diagnostic logs (`timetable_summary.txt`) (`FR-08`). |
| **0. Exit** | Safely halts the interactive execution and terminates the program (`FR-09`). |

## Outputs
Generated artifacts are persisted automatically in the `output/` directory:
- `output/timetable_year_1.txt` ... `output/timetable_year_4.txt`: Formatted weekly ASCII schedule grids per cohort.
- `output/complete_timetable.csv` & `output/complete_timetable.txt`: Master department-wide schedule.
- `output/timetable_summary.txt`: Schedule metrics, diagnostics, unscheduled reasons, and soft-constraint warnings.
- `output/classroom_request_<CourseCode>.txt`: Official requisition letters drafted for the Student Affairs Office.

## Repository Structure
```text
docs/requirements/      SRS versions and documentation
docs/design/            SDD versions and diagram source files (.puml, .drawio)
docs/testing/           Test results and verification logs
docs/checkpoints/       Checkpoint presentation and review reports
data/                   Input files (courses.csv, instructors.txt, rooms.csv, constraints.txt)
src/                    Java source code
out/                    Compiled Java bytecodes (.class)
output/                 Generated timetables, diagnostic logs, and draft letters
```

## Versions
| Version | Tag | Date | Summary |
| --- | --- | --- | --- |
| Code v1 | code-v1 | 08.11.2026 | Initial project architecture, CLI menu system, file parsers, and domain entity structures. |
| Code v2 | code-v2 | 10.11.2026 | Full rule-based two-stage scheduler, HC-01–HC-09 validation, SC warnings, and manual edit guards. |
| Code v3 | code-v3 | [ ] | Complete persistence export engine, formatted ASCII grids, and automated email draft generation. |
| Code v4 | code-v4 | [ ] | Edge-case validation, comprehensive unit tests, and performance benchmarks. |
| Code v5 | code-v5 | [ ] | Code review refactoring, code quality audits, and test coverage optimization. |
| Code v6 | code-v6 | [ ] | Pre-release stabilization and packaging. |
| Final   | final   | [ ] | Verified final release matching SRS and SDD specifications. |

## Known Limitations
- **Greedy Algorithmic Placement:** Because the scheduler uses a deterministic heuristic without extensive backtracking (as constrained by SRS Section 2.4.3), decisions made early during Phase 2 may leave a session unscheduled even if an optimal permutation exists.
- **Single Cohort Model:** Student cohort conflict resolution (`HC-03`) assumes students only take courses within their designated year group; irregular students taking cross-year courses are not modeled.
- **Single-User CLI:** Designed for synchronous, single-operator interaction via console; concurrent multi-user execution is not supported.

## Documents
- SRS: `docs/requirements/`
- SDD: `docs/design/`