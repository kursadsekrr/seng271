# Software Design Document (SDD)

---

## Cover

| Field | Value |
| :--- | :--- |
| **Project** | Department Course Timetable Management System |
| **Document** | Software Design Document (SDD) |
| **Version** | v1.0 |
| **Related SRS version** | v1.0 (Dated 24.10.2026) |
| **Date** | 10.11.2026 |
| **Team number** | Team 01 |
| **Member 1** | Orhan Yalın Öztürk [GitHub: yalinozturk] |
| **Member 2** | Kürşad Şeker [GitHub: kursadsekrr] |
| **Repository** | https://github.com/cankaya-seng/seng271-timetable-management |

---

## Revision History

| Version | Date | Author(s) | Summary of changes |
| :--- | :--- | :--- | :--- |
| **v1.0** | 10.11.2026 | Orhan Yalın Öztürk, Kürşad Şeker | Initial baseline design document covering complete architectural breakdown, domain class specifications, constraint checker mappings, flowchart descriptions, and traceability tables based on SRS v1.0. |

---

## 1. Introduction
This Software Design Document (SDD) provides the comprehensive architectural, structural, and procedural design specification for the standalone Department Course Timetable Management System implemented in Java SE 17+. It formally realizes and maps every functional and non-functional requirement established in the Software Requirements Specification (SRS v1.0).

---

## 2. System Overview

The system follows a clean multi-tier modular architecture adhering strictly to Object-Oriented Design (OOD) and SOLID principles, completely decoupling the interactive console presentation, external I/O parsing, business core domain models, and the rule-based scheduling engine.

| Part | Classes | Responsibility |
| :--- | :--- | :--- |
| **User Interface (menu and screens)** | `ConsoleUIController`, `MenuNavigator`, `SchedulePrinter` | Presents the interactive CLI menu, captures user input selections, coordinates operational flows, prints formatted ASCII weekly calendar grids, and displays conflict/warning dialogs. |
| **Input (reading and validating files)** | `DataLoader`, `CourseCSVParser`, `InstructorTextParser`, `RoomCSVParser`, `ConstraintTextParser`, `ValidationResult` | Reads heterogeneous external data sources (`courses.csv`, `instructors.txt`, `rooms.csv`, `constraints.txt`), performs syntactical and data-type validation, verifies referential keys, and instantiates domain models in memory. |
| **Domain Model (courses, instructors, rooms, sessions, time slots)** | `Course`, `Session`, `TheorySession`, `LabSession`, `Instructor`, `Room`, `Classroom`, `Laboratory`, `TimeSlot`, `DayOfWeek`, `Constraint`, `PriorityType` | Encapsulates all primary real-world departmental entities, structural relationships, state invariants, capacities, and availability time matrices. |
| **Scheduling (algorithm and constraint checks)** | `Scheduler`, `ConstraintChecker`, `HardConstraintEvaluator`, `SoftConstraintEvaluator`, `Timetable`, `Assignment` | Implements the deterministic rule-based two-phase heuristic algorithm, executes real-time validation for HC-01 through HC-09, evaluates SC-01 through SC-04, maintains active assignments, and safely isolates unscheduled sessions. |
| **Output (timetables, reports, email draft)** | `ExportManager`, `ClassroomRequestGenerator`, `SummaryReportWriter` | Serializes scheduled data into tabular `.txt` and structured `.csv` files, builds diagnostic scheduling audit logs, and formats official classroom request letters for the Student Affairs Office. |

---

## 3. UML Class Diagram

### 3.1 Class Diagram Overview
Below is the structural class model representing the complete object graph.

```
+----------------------------------------------------------------------------------------------------+
|                                         ConsoleUIController                                        |
+----------------------------------------------------------------------------------------------------+
| - scheduler: Scheduler                                                                             |
| - dataLoader: DataLoader                                                                           |
| - exportManager: ExportManager                                                                     |
+----------------------------------------------------------------------------------------------------+
| + start(): void                                                                                    |
| + handleMenuSelection(choice: int): void                                                           |
| + renderYearTimetable(year: int): void                                                             |
| + promptManualModification(): void                                                                 |
| + promptClassroomRequest(): void                                                                   |
+----------------------------------------------------------------------------------------------------+
                                |
                                v
+----------------------------------------------------------------------------------------------------+
|                                              Scheduler                                             |
+----------------------------------------------------------------------------------------------------+
| - timetable: Timetable                                                                             |
| - constraintChecker: ConstraintChecker                                                             |
| - requestGenerator: ClassroomRequestGenerator                                                      |
| - courses: List<Course>                                                                            |
| - instructors: Map<String, Instructor>                                                             |
| - rooms: List<Room>                                                                                |
| - specialConstraints: List<Constraint>                                                            |
+----------------------------------------------------------------------------------------------------+
| + loadData(dataDir: String): void                                                                  |
| + generateTimetable(): ScheduleResult                                                              |
| + manuallyReassign(sessionId: String, newSlot: TimeSlot, newRoom: Room): ValidationStatus          |
| + getUnscheduledSessions(): List<Session>                                                          |
| + generateClassroomRequest(courseCode: String): String                                             |
+----------------------------------------------------------------------------------------------------+
        |                                       |                                    |
        | uses                                  | delegates                          | manages
        v                                       v                                    v
+-----------------------------+   +------------------------------------+   +-------------------------+
|      ConstraintChecker      |   |             Timetable              |   |          Course         |
+-----------------------------+   +------------------------------------+   +-------------------------+
| - hardChecker: HCEvaluator  |   | - assignments: List<Assignment>    |   | - courseCode: String    |
| - softChecker: SCEvaluator  |   | - unscheduledSessions: List<Sess.> |   | - courseTitle: String   |
+-----------------------------+   | - grid: Map<TimeSlot, Assignment>  |   | - yearGroup: int        |
| + validateHardConstraints(  |   +------------------------------------+   | - theoryHours: int      |
|     session: Session,       |   | + addAssignment(a: Assign.): bool  |   | - labHours: int         |
|     slot: TimeSlot,         |   | + removeAssignment(s: Sess.): bool |   | - studentCount: int     |
|     room: Room): CheckResult|   | + getAssignmentsForYear(y: int): L |   | - instructor: Instructor|
| + checkSoftConstraints(     |   | + getAssignmentsForRoom(r: Room): L|   +-------------------------+
|     session: Session,       |   | + getAssignmentsForInst(i: Inst): L|   | + getSessions(): List   |
|     slot: TimeSlot): List   |   | + getWeeklyGrid(year: int): String |   +-------------------------+
+-----------------------------+   +------------------------------------+                |
                                                    |                                   | creates
                                                    v                                   v
                                      +---------------------------+        +--------------------------+
                                      |         Assignment        |        |          Session         |
                                      +---------------------------+        +--------------------------+
                                      | - session: Session        |<>----->| - sessionId: String      |
                                      | - slot: TimeSlot          |        | - course: Course         |
                                      | - room: Room              |        | - sessionType: String    |
                                      +---------------------------+        | - durationHours: int     |
                                      | + getSession(): Session   |        | - requiredRoomType: Type |
                                      | + getSlot(): TimeSlot     |        +--------------------------+
                                      | + getRoom(): Room         |                     ^
                                      +---------------------------+                     |
                                                    |                         +---------+---------+
                                                    |                         |                   |
                                                    v                         v                   v
+-------------------------------+     +---------------------------+    +-------------+     +-------------+
|          Instructor           |     |         TimeSlot          |    |TheorySession|     | LabSession  |
+-------------------------------+     +---------------------------+    +-------------+     +-------------+
| - instructorId: String        |     | - day: DayOfWeek          |    
| - fullName: String            |     | - startHour: int          |    +-----------------------------+
| - unavailableSlots: List<Slot>|     | - duration: int           |    |            Room             |
| - preferredTimes: String      |     +---------------------------+    +-----------------------------+
+-------------------------------+     | + overlapsWith(s: Slot): b|    | - roomId: String            |
| + isAvailable(s: Slot): bool  |     | + getEndHour(): int       |    | - building: String          |
| + hasPreferenceMatch(s): bool |     | + getDay(): DayOfWeek     |    | - capacity: int             |
+-------------------------------+     +---------------------------+    | - roomType: RoomType        |
                                                                       | - maintenanceSlots: List    |
                                                                       +-----------------------------+
                                                                       | + isAvailable(s): boolean   |
                                                                       | + canAccommodate(s): boolean|
                                                                       +-----------------------------+
```

Tool used: Draw.io / PlantUML. Source file: `docs/design/timetable_class_diagram.puml`

- [x] Every main class is shown, named exactly as in the Java code.
- [x] Important attributes have a type and visibility (e.g., `capacity: int`).
- [x] Main methods have parameters and a return type (e.g., `+ isAvailable(slot: TimeSlot): boolean`).
- [x] Relationships between classes are drawn with multiplicities.
- [x] The class that runs the scheduling algorithm is identifiable (`Scheduler`).
- [x] The diagram is readable when printed on one A4 page.

---

## 4. Class Descriptions

| Class | Responsibility (one sentence) | Key attributes | Key methods | Related classes |
| :--- | :--- | :--- | :--- | :--- |
| **`ConsoleUIController`** | Orchestrates the top-level CLI workflow, displaying interactive screens and directing coordinator commands to business components. | `scanner: Scanner`, `scheduler: Scheduler`, `exportManager: ExportManager` | `start()`, `displayMenu()`, `handleInput()`, `renderGrid()` | `Scheduler`, `ExportManager`, `Timetable` |
| **`DataLoader`** | Coordinates the file parsing pipeline across external data formats, enforcing type checks and referential integrity. | `dataDirectory: Path`, `parsingErrors: List<String>` | `loadAll()`, `validateForeignKeys()`, `getWarnings()` | `Course`, `Instructor`, `Room`, `Constraint` |
| **`Course`** | Represents an undergraduate departmental course specification and splits itself into concrete teaching blocks. | `courseCode: String`, `courseTitle: String`, `yearGroup: int`, `theoryHours: int`, `labHours: int`, `studentCount: int`, `instructor: Instructor` | `getSessions()`, `getTheoryHours()`, `getLabHours()`, `getYearGroup()` | `Instructor`, `Session`, `TheorySession`, `LabSession` |
| **`Session`** | Serves as an abstract base model for an indivisible, continuous teaching unit (lecture block or laboratory session). | `sessionId: String`, `course: Course`, `durationHours: int`, `requiredType: RoomType` | `getSessionId()`, `getDurationHours()`, `getRequiredType()`, `getCourse()` | `Course`, `RoomType`, `Assignment` |
| **`TheorySession`** | Specializes `Session` for theoretical classroom lectures (-T). | *(Inherited from `Session`)* | `isLaboratory(): boolean (returns false)` | `Session`, `Course` |
| **`LabSession`** | Specializes `Session` for practical hands-on computer laboratory sessions (-L). | *(Inherited from `Session`)* | `isLaboratory(): boolean (returns true)` | `Session`, `Course` |
| **`Instructor`** | Holds faculty identity, declared blackout unavailable times, and time-of-day teaching preferences. | `instructorId: String`, `fullName: String`, `unavailableSlots: List<TimeSlot>`, `preferredTimes: String` | `isAvailable(slot: TimeSlot): boolean`, `satisfiesPreference(slot: TimeSlot): boolean` | `TimeSlot`, `Course` |
| **`Room`** | Represents a departmental physical educational facility (Classroom or Laboratory) with fixed seating and maintenance windows. | `roomId: String`, `building: String`, `capacity: int`, `roomType: RoomType`, `maintenanceSlots: List<TimeSlot>` | `isAvailable(slot: TimeSlot): boolean`, `canHost(session: Session): boolean` | `RoomType`, `TimeSlot`, `Session` |
| **`TimeSlot`** | Defines an immutable atomic or multi-hour consecutive temporal period within the academic 5-day week. | `day: DayOfWeek`, `startHour: int`, `durationHours: int` | `overlapsWith(other: TimeSlot): boolean`, `getEndHour(): int`, `isWithinWorkingHours(): boolean` | `DayOfWeek`, `Assignment` |
| **`Constraint`** | Represents special scheduling conditions read from `constraints.txt` (fixed time, specific room, lab before theory). | `courseCode: String`, `type: ConstraintType`, `value: String`, `priority: PriorityType` | `isMandatory(): boolean`, `getType(): ConstraintType`, `getValue(): String` | `Course`, `PriorityType` |
| **`Scheduler`** | Executes the two-phase rule-based scheduling algorithm, controls manual updates, and manages unscheduled sessions. | `timetable: Timetable`, `checker: ConstraintChecker`, `courses: List<Course>`, `rooms: List<Room>` | `generateTimetable(): ScheduleResult`, `manuallyReassign(...)`, `getCandidateSlots(...)` | `Timetable`, `ConstraintChecker`, `Course`, `Room` |
| **`ConstraintChecker`** | Aggregates all hard constraint (HC-01 - HC-09) and soft constraint (SC-01 - SC-04) rule evaluation routines. | `timetable: Timetable` | `validateAllHardConstraints(...)`, `evaluateSoftConstraints(...)` | `Timetable`, `Session`, `TimeSlot`, `Room` |
| **`Timetable`** | Maintains active session assignments, monitors room/instructor/cohort bookings, and formats year-group timetable views. | `assignments: List<Assignment>`, `unscheduledSessions: List<Session>` | `addAssignment(...)`, `removeAssignment(...)`, `isInstructorBusy(...)`, `isRoomOccupied(...)` | `Assignment`, `Session`, `TimeSlot` |
| **`Assignment`** | Immutable tuple binding a scheduled `Session` to a specific `TimeSlot` and physical `Room`. | `session: Session`, `slot: TimeSlot`, `room: Room` | `getSession()`, `getSlot()`, `getRoom()` | `Session`, `TimeSlot`, `Room` |
| **`ClassroomRequestGenerator`** | Constructs formatted formal requisition letters for the Student Affairs Office for sessions lacking room capacity. | `scheduler: Scheduler` | `buildRequestDraft(session: Session): String`, `findSuggestedSlot(session: Session): TimeSlot` | `Session`, `TimeSlot`, `Course` |
| **`ExportManager`** | Serializes schedules and audit summaries to disk in structured CSV and text format. | `outputDirectory: Path` | `exportYearGrids()`, `exportGlobalCSV()`, `saveSummaryReport()` | `Timetable`, `Assignment` |

---

## 5. Design Decisions

| Question | Our decision | Why |
| :--- | :--- | :--- |
| **How is a course split into sessions (theory and laboratory blocks)?** | Courses are decomposed into two distinct child objects: a `TheorySession` (having duration equal to `theoryHours` and room requirement `Classroom`) and a `LabSession` (duration `labHours` and room requirement `Laboratory`). If `labHours == 0`, only a `TheorySession` is generated. | This enforces SRS Section 2.4.4 and Section 3.5: multi-hour teaching units are treated as atomic, indivisible continuous blocks rather than independent fragmented 1-hour slots. |
| **How is a time slot represented (day, start hour, length)?** | As an immutable value object `TimeSlot` containing a `DayOfWeek` enum (MONDAY through FRIDAY), a `startHour` integer ($9 \le h \le 16$), and a `durationHours` integer ($1 \le d \le 4$). | Encapsulating time as a composite object with built-in boundary validation ($startHour + duration \le 17$) makes overlap detection (`overlapsWith`) straightforward and prevents off-by-one scheduling errors. |
| **How is the timetable stored (per year group, per room, per instructor)?** | As a unified collection of `Assignment` objects inside `Timetable`, backed by indexed secondary lookup hash maps: `Map<DayOfWeek, Map<Integer, Assignment>>` per room, per instructor, and per student cohort (Year 1 to 4). | A unified master state guarantees consistency (avoiding out-of-sync multi-table states), while $O(1)$ indexed maps allow immediate collision checks across instructors, rooms, and student cohorts. |
| **How are instructor and room availability stored?** | Stored within each `Instructor` and `Room` instance as a `List<TimeSlot>` representing blackout periods. For quick checks, a two-dimensional boolean matrix `boolean[5][8]` (5 days, 8 daily slots) is maintained. | Direct boolean indexing allows the constraint engine to verify whether a room or professor is free during all hours of a candidate block in $O(1)$ time without iterative scanning. |
| **How are special requests represented and marked as mandatory or preferred?** | A dedicated `Constraint` model encapsulates `(courseCode, type, value, priority)`. The `priority` field is an enum with values `MANDATORY` or `PREFERRED`. | Allows the scheduler to partition constraints during Phase 1: `MANDATORY` items (HC-08 fixed times, HC-09 fixed rooms) are enforced as non-negotiable prerequisites, while `PREFERRED` items trigger warnings or score penalties. |
| **How are hard constraints checked (one method per constraint, one checker class, ...)?** | Centralized inside a dedicated `ConstraintChecker` class that organizes evaluation into individual, private modular check methods (`checkHC01_InstructorConflict`, `checkHC02_RoomConflict`, etc.), called by a public coordinator method `validateAllHardConstraints()`. | Follows the Single Responsibility Principle (SRP). Keeping each constraint in an isolated method simplifies unit testing, pinpoint error diagnostics, and enables precise reporting for why a session was rejected. |
| **How are input errors detected and reported?** | The `DataLoader` scans input files line-by-line inside defensive `try-catch` blocks. Syntactical errors (e.g., non-numeric hours) or unreferenced foreign keys log clear line-level warnings and skip the faulty row without crashing. | Fulfills FR-02, FR-09, and NFR-02 (fault-tolerant execution). The coordinator receives complete diagnostics in the console, while all valid data rows are safely loaded into memory. |
| **Which collections (arrays, ArrayList, HashMap, ...) are used, and for what?** | `ArrayList` is used for ordered course and session lists; `HashMap` is used for entity lookups by string key (`Map<String, Course>`, `Map<String, Instructor>`); `EnumMap` and `HashSet` are used for fast time slot occupancy checks. | `ArrayList` preserves deterministic sequential processing order; `HashMap` guarantees instantaneous $O(1)$ relational lookups during foreign key validation and collision checking. |

---

## 6. Automatic Scheduling Algorithm

### 6.1 Strategy
1. **In what order are sessions processed?**
   Processing executes in a strict two-phase priority sequence:
   - **Phase 1 (Fixed-Time Requests - HC-08):** All sessions constrained by mandatory fixed day/time requests are scheduled first.
   - **Phase 2 (Flexible Sessions):** The remaining sessions are sorted using a heuristic: highest duration blocks first (3-hour blocks before 2-hour blocks), followed by larger enrollment sizes (Most Constrained First). Theory sessions are placed before laboratory sessions of the same course.
2. **How are candidate time slots selected?**
   The algorithm iterates deterministically through the academic week: Days: Monday (0) to Friday (4); Start Hours: 09:00 to $(17 - \text{durationHours})$. For each candidate starting slot, the system confirms that the continuous block fits entirely within working hours without crossing the 17:00 threshold (HC-04).
3. **How are suitable rooms selected?**
   Departmental rooms are filtered by `RoomType` matching the session classification (Classroom for `-T`, Laboratory for `-L`, HC-06) and capacity criteria ($\text{capacity} \ge \text{studentCount}$, HC-05). Eligible rooms are sorted in ascending order of capacity to preserve larger rooms for bigger classes (Best-Fit heuristic).
4. **How are scheduling constraints checked?**
   For every candidate `(Session, TimeSlot, Room)` tuple:
   - `ConstraintChecker.validateAllHardConstraints()` verifies that:
     1. The instructor is available and has no overlapping assignment (HC-01, HC-07).
     2. The room is not occupied and not under maintenance (HC-02, HC-09).
     3. The course's year group has no overlapping sessions (HC-03).
     4. Room capacity and type are strictly satisfied (HC-05, HC-06).
   - If all pass, `ConstraintChecker.evaluateSoftConstraints()` checks for instructor morning/afternoon preference matches (SC-02) and student gap minimization (SC-03).
5. **How are conflicts handled, and which alternatives are tried in what order?**
   When a collision occurs, the algorithm rejects that specific combination and systematically tries:
   1. The next suitable room in the sorted room pool for the *same* time slot.
   2. If all rooms are exhausted, the next consecutive time slot within the *same* day.
   3. If the day is exhausted, moves to the next operational day.
   Previous valid timetable assignments remain completely unaffected (no destructive backtracking).
6. **When does the algorithm decide that a session cannot be assigned?**
   A session is deemed unassignable only after the algorithm tests all permutations of operational days (Mon-Fri), valid time slots (09:00-17:00), and compatible departmental rooms without finding a single conflict-free tuple. It is moved to `unscheduledSessions` with a root-cause diagnostic:
   - Tagged **`NO ROOM`** if viable time slots were open for instructor and cohort, but no departmental room satisfied capacity or type.
   - Tagged **`CONSTRAINT CONFLICT`** if instructor availability, fixed-time rules, or cohort overlaps prevented assignment.

---

### 6.2 Flowchart
The structural flowchart below visualizes the entire algorithm, matching the logic in SRS Section 4.3.2.

```
       [Start: Automated Scheduling]
                    |
                    v
       [1. Load & Validate Input Datasets]
                    |
                    v
       [2. Identify Fixed-Time Requests (Phase 1)]
                    |
                    v
    +->[3. Select Next Session]
    |               |
    |         (All processed?) ----> YES ----> [10. Compile Metrics & Diagnostic Summary]
    |               |                                              |
    |              NO                                              v
    |               v                               [11. Timetable Generation Complete]
    |  [4. Search Available Candidate Slot]
    |  (Mon-Fri, 09:00 - (17-duration))
    |               |
    |               v
    |  [5. Search Suitable Room]
    |  (Type Match & Capacity >= Enrollment)
    |               |
    |               v
    |  <Valid Slot & Room Permutation Found?>
    |         |                           |
    |        YES                          NO (Exhausted all slots/rooms)
    |         |                           |
    |         v                           v
    |  <6. Hard Constraints Pass?>       [Record in Unscheduled Registry]
    |  (HC-01 through HC-09)              [Categorize: NO ROOM / CONFLICT]
    |         |              |                     |
    |        YES             NO                    |
    |         |              |                     |
    |         |        [7. Detect Conflict]        |
    |         |        [8. Try Alternative Room]--+
    |         |        [or Next Slot / Next Day]   |
    |         v                                    |
    |  <Soft Constraints Satisfied?>               |
    |  (SC-01 through SC-04)                       |
    |         |               |                    |
    |        YES              NO                   |
    |         |               |                    |
    |         |         [Log SC Warning]           |
    |         |               |                    |
    |         +-------+-------+                    |
    |                 |                            |
    |                 v                            |
    |  [9. Record Successful Assignment]           |
    |  [Commit to Master Timetable Grid]           |
    |                 |                            |
    +-----------------+----------------------------+
```

- [x] 1. Loading input data
- [x] 2. Identifying fixed-time sessions
- [x] 3. Selecting an unscheduled session
- [x] 4. Searching for an available time slot
- [x] 5. Searching for a suitable room
- [x] 6. Checking scheduling constraints
- [x] 7. Detecting conflicts
- [x] 8. Trying alternatives
- [x] 9. Recording successful assignments
- [x] 10. Reporting unscheduled sessions
- [x] 11. Completing the timetable generation process

---

### 6.3 Pseudocode

```java
public ScheduleResult generateTimetable() {
    timetable.clear();
    List<Session> allSessions = extractSessionsFromCurriculum(this.courses);
    
    // Phase 1: Fixed-Time Allocations (HC-08)
    List<Session> fixedSessions = filterFixedTimeSessions(allSessions);
    for (Session session : fixedSessions) {
        TimeSlot targetSlot = getFixedSlotRequest(session);
        Room targetRoom = getFixedRoomRequest(session); // Optional fixed room
        
        if (targetRoom == null) {
            targetRoom = findBestFitRoom(session, targetSlot);
        }
        
        CheckResult result = constraintChecker.validateAllHardConstraints(session, targetSlot, targetRoom);
        if (result.isPassed()) {
            timetable.addAssignment(new Assignment(session, targetSlot, targetRoom));
        } else {
            timetable.markUnscheduled(session, ProblemType.CONSTRAINT_CONFLICT, result.getReason());
        }
    }

    // Phase 2: Flexible Sessions (Heuristic: Longest & Largest First)
    List<Session> flexibleSessions = getUnscheduledRemaining(allSessions);
    flexibleSessions.sort(Comparator.comparingInt(Session::getDurationHours)
                                    .thenComparingInt(s -> s.getCourse().getStudentCount())
                                    .reversed());

    for (Session session : flexibleSessions) {
        boolean assigned = false;
        
        dayLoop:
        for (DayOfWeek day : DayOfWeek.values()) {
            for (int startHour = 9; startHour <= (17 - session.getDurationHours()); startHour++) {
                TimeSlot candidateSlot = new TimeSlot(day, startHour, session.getDurationHours());
                
                // Get rooms matching room type and capacity >= students
                List<Room> candidateRooms = getSuitableRoomsSortedByCapacity(session);
                for (Room room : candidateRooms) {
                    CheckResult hcCheck = constraintChecker.validateAllHardConstraints(session, candidateSlot, room);
                    if (hcCheck.isPassed()) {
                        List<String> warnings = constraintChecker.evaluateSoftConstraints(session, candidateSlot);
                        timetable.addAssignment(new Assignment(session, candidateSlot, room));
                        timetable.logWarnings(session, warnings);
                        assigned = true;
                        break dayLoop; // Successfully scheduled; move to next session
                    }
                }
            }
        }
        
        if (!assigned) {
            ProblemType cause = determineRootCause(session);
            timetable.markUnscheduled(session, cause.getType(), cause.getMessage());
        }
    }
    
    return timetable.compileResults();
}
```

---

### 6.4 Where Each Hard Constraint Is Checked

| ID | Constraint | Checked in (Class.method) | Code version |
| :--- | :--- | :--- | :--- |
| **HC-01** | Instructor conflict | `ConstraintChecker.checkHC01_InstructorConflict(Session, TimeSlot)` | Done in v1.0 |
| **HC-02** | Room conflict | `ConstraintChecker.checkHC02_RoomConflict(Room, TimeSlot)` | Done in v1.0 |
| **HC-03** | Year-group conflict | `ConstraintChecker.checkHC03_YearGroupConflict(Session, TimeSlot)` | Done in v1.0 |
| **HC-04** | Working hours | `TimeSlot.isWithinWorkingHours()` | Done in v1.0 |
| **HC-05** | Room capacity | `Room.canAccommodate(int studentCount)` | Done in v1.0 |
| **HC-06** | Room type | `Room.matchesType(RoomType requiredType)` | Done in v1.0 |
| **HC-07** | Instructor availability | `Instructor.isAvailable(TimeSlot)` | Done in v1.0 |
| **HC-08** | Fixed-time requirements | `Scheduler.processFixedTimeSessions()` | Done in v1.0 |
| **HC-09** | Room availability | `Room.isAvailable(TimeSlot)` | Done in v1.0 |

---

### 6.5 Known Limitations
- **Greedy Placement Horizon:** Because the algorithm uses a deterministic rule-based greedy approach without backtracking (as bounded by SRS 2.4.3), an assignment made early in Phase 2 may consume a room or slot that causes a later session to fail, even if a globally optimal schedule exists.
- **Independent Cohort Assumption:** Multi-year enrollments (e.g., irregular students repeating courses from different years) are outside the model scope; conflict checks are strictly partitioned by designated student year group (1 to 4).

---

## 7. Conflict Detection and Handling

| Problem | Detected in (Class.method) | What the algorithm does | What the user sees |
| :--- | :--- | :--- | :--- |
| **Instructor or room already busy in the slot** | `ConstraintChecker.checkHC01_InstructorConflict`<br>`ConstraintChecker.checkHC02_RoomConflict` | Rejects candidate tuple; iterates to the next room or advances starting hour. | During generation: internal loop iteration. During manual edit: displays `[X] HC-01/HC-02 Conflict: <Resource> is already used by <Session>` and keeps original assignment. |
| **Year-group overlap** | `ConstraintChecker.checkHC03_YearGroupConflict` | Rejects time slot; advances slot to avoid cohort collision. | During manual edit: displays `[X] HC-03 Year-group conflict: Cohort Year <N> already has <Session> scheduled` and keeps original state. |
| **No room with enough capacity or the right type** | `ConstraintChecker.checkHC05_RoomCapacity`<br>`ConstraintChecker.checkHC06_RoomType` | Bypasses incompatible rooms. If no departmental facility fits, marks session as unassigned. | Unscheduled summary lists: `Problem Type: NO ROOM`, `Reason: No laboratory with capacity >= <N>`. |
| **Fixed-time request violates a hard constraint** | `Scheduler.processFixedTimeSessions` | Flags failure immediately; leaves session unscheduled. | Prints `[FAIL] <Session> Fixed time <Slot> violates <HC-ID>: <Reason>` (e.g., instructor unavailable). |
| **Soft-constraint violation** | `ConstraintChecker.evaluateSoftConstraints` | Accepts the slot assignment; records a descriptive warning entry in the audit log. | Prints `[WARN] <Session> SC-02: Instructor prefers morning hours; placed at 13:00`. On manual edit: displays confirmation prompt `Apply change anyway? (y/n)`. |

---

## 8. User Interface Design

| Menu option | What it does | Requirement IDs | Class.method called |
| :--- | :--- | :--- | :--- |
| **`1. Load input data`** | Ingests CSV and TXT files from `./data/`, parses courses, rooms, instructors, and constraints, reporting parsing warnings. | `FR-01`, `FR-02`, `NFR-02` | `DataLoader.loadAll()`, `ConsoleUIController.displayLoadSummary()` |
| **`2. Generate timetable automatically`** | Triggers the two-phase scheduling algorithm, evaluates constraints, assigns blocks, and prints summary metrics. | `FR-03`, `HC-01` to `HC-09`, `SC-01` to `SC-04` | `Scheduler.generateTimetable()`, `ConsoleUIController.printGenerationMetrics()` |
| **`3. View timetable by year group`** | Prompts for year group (1-4) and renders an 8x5 ASCII schedule grid with assigned rooms and course labels. | `FR-04`, `NFR-03` | `Timetable.getWeeklyGrid(year)`, `ConsoleUIController.renderGrid()` |
| **`4. View all scheduled sessions`** | Outputs department-wide tabular list of every committed session with time, room, instructor, and cohort details. | `FR-05` | `Timetable.getAllAssignments()`, `ConsoleUIController.printTable()` |
| **`5. View unscheduled sessions and warnings`** | Displays detailed diagnostics for failed sessions (categorized by NO ROOM vs CONSTRAINT) and soft constraint warnings. | `FR-05`, `NFR-03` | `Timetable.getUnscheduledReport()`, `ConsoleUIController.printDiagnostics()` |
| **`6. Modify / remove an assignment`** | Interactively reassigns a session to a new slot/room with real-time hard-constraint checks and soft-constraint prompts. | `FR-06`, `HC-01` to `HC-09` | `Scheduler.manuallyReassign(...)`, `ConsoleUIController.promptManualEdit()` |
| **`7. Generate classroom request email`** | Selects an unassigned session tagged `NO ROOM`, computes a conflict-free recommended time slot, and formats an official requisition draft. | `FR-07` | `ClassroomRequestGenerator.buildRequestDraft(...)`, `ConsoleUIController.saveEmailPrompt()` |
| **`8. Save timetable to file`** | Exports full schedule grids to text files and complete records to CSV spreadsheets in `./output/`. | `FR-08` | `ExportManager.exportAll(...)`, `ConsoleUIController.printSaveConfirmation()` |
| **`0. Exit`** | Gracefully closes streams, displays farewell message, and terminates JVM process. | `FR-09` | `ConsoleUIController.terminate()` |

---

## 9. Requirements Traceability

| Requirement | Implemented in (Class.method) | Test case(s) | Status |
| :--- | :--- | :--- | :--- |
| **FR-01** (File Parsing) | `CourseCSVParser.parse()`, `InstructorTextParser.parse()`, `RoomCSVParser.parse()` | `TC-01: Verify parsing of valid CSV/TXT files` | Implemented |
| **FR-02** (Validation) | `DataLoader.validateDataTypes()`, `DataLoader.validateForeignKeys()` | `TC-02: Corrupt row and missing foreign key skipping` | Implemented |
| **FR-03** (Auto Scheduling) | `Scheduler.generateTimetable()` | `TC-03: Full curriculum conflict-free assignment run` | Implemented |
| **FR-04** (Year Grid View) | `Timetable.getWeeklyGrid(int year)` | `TC-04: Validate 5-day by 8-hour ASCII grid output` | Implemented |
| **FR-05** (Global & Diagnostics)| `Timetable.getAllAssignments()`, `Timetable.getUnscheduledReport()` | `TC-05: Check summary metrics and warning outputs` | Implemented |
| **FR-06** (Manual Modify) | `Scheduler.manuallyReassign(...)`, `ConstraintChecker.validateAllHardConstraints(...)` | `TC-06: Attempt colliding manual move and verify rejection` | Implemented |
| **FR-07** (Email Generator) | `ClassroomRequestGenerator.buildRequestDraft(...)` | `TC-07: Format email text with conflict-free slot` | Implemented |
| **FR-08** (Persistence) | `ExportManager.exportYearGrids()`, `ExportManager.exportGlobalCSV()` | `TC-08: Check generated files in output/ directory` | Implemented |
| **FR-09** (Error Interception)| `ConsoleUIController.handleInput()`, `DataLoader.safeRead()` | `TC-09: Provide non-numeric input to menu options` | Implemented |
| **HC-01** (Instructor Overlap)| `ConstraintChecker.checkHC01_InstructorConflict()` | `TC-HC01: Assign 2 courses to same instructor at 10:00` | Tested |
| **HC-02** (Room Collision) | `ConstraintChecker.checkHC02_RoomConflict()` | `TC-HC02: Assign 2 courses to Room A101 at same time` | Tested |
| **HC-03** (Cohort Overlap) | `ConstraintChecker.checkHC03_YearGroupConflict()` | `TC-HC03: Assign 2 Year 2 courses concurrently` | Tested |
| **HC-04** (Working Hours) | `TimeSlot.isWithinWorkingHours()` | `TC-HC04: Block spanning beyond 17:00 boundary` | Tested |
| **HC-05** (Room Capacity) | `Room.canAccommodate(int count)` | `TC-HC05: Place 70 students into 60-capacity room` | Tested |
| **HC-06** (Room Type Match) | `Room.matchesType(RoomType type)` | `TC-HC06: Schedule Lab session into Lecture Classroom` | Tested |
| **HC-07** (Inst. Available) | `Instructor.isAvailable(TimeSlot slot)` | `TC-HC07: Schedule session during instructor day-off` | Tested |
| **HC-08** (Fixed Requests) | `Scheduler.processFixedTimeSessions()` | `TC-HC08: Mandatory request assigned before general pool`| Tested |
| **HC-09** (Room Available) | `Room.isAvailable(TimeSlot slot)` | `TC-HC09: Assign room during declared maintenance hour` | Tested |

---

## 10. Changes from the Previous Version

| Element | Change | Reason |
| :--- | :--- | :--- |
| **All sections** | Initial baseline architecture defined (v1.0). | Derived and formalized directly from SRS v1.0 specifications. |

---

## AI-Use Declaration

| Tool | Used for | How we checked the result |
| :--- | :--- | :--- |
| **Gemini / LLM Assistant** | Structuring the design document to align strictly with the SDD template and synthesizing class method specifications from the SRS models. | Verified all class attributes, methods, constraint checker mappings, and flowchart steps against the SRS v1.0 document; confirmed every hard and soft constraint ID matches across both documents. |
