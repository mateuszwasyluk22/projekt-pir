# Projekt Semestralny — Programowanie Równoległe i Rozproszone

## Calkowanie numeryczne: metoda prostokątów, trapezów, kwadratura Gaussa-Legendre

### Wersja sekwencyjna (baseline) + równoległa (ForkJoinPool)

---

## Opis problemu

Projekt realizuje problem **całkowania numerycznego** funkcji f(x) na przedziale [a, b] za pomocą trzech metod:

1. **Metoda prostokątów** (midpoint rule) — rząd dokładności O(h²)
2. **Metoda trapezów** (composite trapezoidal rule) — rząd dokładności O(h²)
3. **Kwadratura Gaussa-Legendre** (3-punktowa, composite) — rząd dokładności O(h⁶)

Dla każdej metody zaimplementowano:
- **Wersję sekwencyjną** jako punkt odniesienia (baseline)
- **Wersję równoległą** z użyciem ForkJoinPool (divide-and-conquer)

Funkcja podcałkowa jest podawana jako wyrażenie tekstowe (np. `sin(x^2)`, `x^3+2*x`) i parsowana przez bibliotekę exp4j.

---

## Architektura

### Technologie
- Java 17, Spring Boot 4.0.5, Maven
- exp4j 0.4.8 (parsowanie wyrażeń matematycznych)

### Mechanizmy współbieżności
| Mechanizm | Klasa | Rola |
|-----------|-------|-----|
| `ConcurrentHashMap` | `TaskService` | Thread-safe przechowywanie zadań |
| `LinkedBlockingQueue` | `TaskService` | Bufor producent-konsument (POST → dispatcher) |
| `ExecutorService` | `TaskService` | Pula dispatcher threads (FixedThreadPool) |
| `ForkJoinPool` | `IntegrationService` | Równoległe obliczenia (1/2/4/8 workerów) |
| `RecursiveTask<Double>` | `*ForkJoinTask` | Divide-and-conquer (podział na połowy) |
| `Thread` (daemon) | `TaskService.init()` | Customowe wątki dispatcherów |

### Struktura klas
```
pl.edu.prir.integral/
├── IntegralApplication.java          # Punkt wejścia Spring Boot
├── config/
│   └── CorsConfig.java               # Konfiguracja CORS
├── controller/
│   └── TaskController.java           # REST API endpointy
├── model/
│   ├── IntegrationMethod.java        # Enum: RECTANGLE, TRAPEZOID, GAUSS
│   ├── Task.java                     # Model zadania
│   ├── TaskRequest.java              # DTO zapytania POST
│   ├── TaskStatus.java               # Enum: QUEUED, RUNNING, DONE, FAILED
│   └── BenchmarkResult.java          # Wyniki benchmarku + S(N), E(N)
├── service/
│   ├── IntegrationService.java       # Logika obliczeniowa (seq + par)
│   └── TaskService.java              # Zarządzanie cyklem życia zadań
├── sequential/
│   ├── SequentialIntegrator.java     # Baseline sekwencyjny (3 metody)
│   └── GaussWeights.java             # Węzły i wagi Gaussa-Legendre
└── concurrent/
    ├── RectangleForkJoinTask.java    # ForkJoin — metoda prostokątów
    ├── TrapezoidForkJoinTask.java    # ForkJoin — metoda trapezów
    └── GaussForkJoinTask.java        # ForkJoin — kwadratura Gaussa
```

---

## Kontrakt API

### POST /tasks
Tworzy nowe zadanie obliczeniowe (asynchronicznie).

**Request:**
```json
{
  "method": "GAUSS",
  "function": "sin(x^2)",
  "a": 0,
  "b": 1,
  "intervals": 1000000,
  "workers": 4
}
```

**Response (202 Accepted):**
```json
{
  "taskId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "status": "QUEUED",
  "method": "GAUSS",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

### GET /tasks/{id}
Zwraca szczegóły zadania (status, wynik lub błąd).

**Response (200 OK):**
```json
{
  "taskId": "3fa85f64-...",
  "status": "DONE",
  "method": "GAUSS",
  "function": "sin(x^2)",
  "a": 0, "b": 1,
  "intervals": 1000000,
  "workers": 4,
  "result": 0.3102683017233811,
  "executionTimeMs": 42
}
```

### GET /tasks/{id}/benchmark
Uruchamia benchmark: 2 warmup + 5 pomiarów dla 1, 2, 4, 8 workerów.

**Response (200 OK):**
```json
{
  "taskId": "...",
  "method": "GAUSS",
  "results": [
    { "workers": 1, "avgTimeMs": 120, "minTimeMs": 115, "maxTimeMs": 130 },
    { "workers": 2, "avgTimeMs": 65, "minTimeMs": 60, "maxTimeMs": 72 },
    { "workers": 4, "avgTimeMs": 35, "minTimeMs": 32, "maxTimeMs": 40 },
    { "workers": 8, "avgTimeMs": 22, "minTimeMs": 20, "maxTimeMs": 28 }
  ],
  "speedup": { "1": 1.0, "2": 1.85, "4": 3.43, "8": 5.45 },
  "efficiency": { "1": 1.0, "2": 0.92, "4": 0.86, "8": 0.68 }
}
```

---

## Uruchomienie lokalnie

### Wymagania
- Java 17+
- Maven 3.8+

### Kroki
```bash
# 1. Klonuj repozytorium
git clone <URL>
cd async

# 2. Zbuduj projekt
mvn clean package -DskipTests

# 3. Uruchom aplikację
java -jar target/async-0.0.1-SNAPSHOT.jar

# Lub alternatywnie:
mvn spring-boot:run
```

Aplikacja startuje na `http://localhost:8080`.

### Testowanie curl
```bash
# Utwórz zadanie
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"method":"TRAPEZOID","function":"sin(x^2)","a":0,"b":1,"intervals":1000000,"workers":4}'

# Sprawdź status (podmień TASK_ID)
curl http://localhost:8080/tasks/TASK_ID

# Uruchom benchmark
curl http://localhost:8080/tasks/TASK_ID/benchmark
```

### Funkcje testowe
| Funkcja | Przedział | Wartość dokładna |
|---------|-----------|-----------------|
| `x^2` | [0, 1] | 1/3 = 0.333333 |
| `sin(x)` | [0, 3.14159265] | 2.0 |
| `exp(-x^2)` | [0, 1] | = 0.746824 |
| `sin(x^2)` | [0, 1] | = 0.310268 |
| `x^3 + 2*x` | [0, 2] | 8.0 |

---

## Uruchomienie w Dockerze

```bash
# 1. Zbuduj obraz
docker build -t async-integral ./backend/

# 2. Uruchom kontener
docker run -p 8080:8080 async-integral

# 3. Testuj (tak samo jak lokalnie)
curl http://localhost:8080/tasks
```

---

## Benchmark i analiza wydajności

### Metodologia
- Workerów: 1, 2, 4, 8
- Warmup: 2 rundy (JIT compilation)
- Pomiary: 5 powtórzeń
- Metryki: S(N) = T1/TN, E(N) = S(N)/N

---

## Autorzy
Projekt semestralny — Programowanie Równoległe i Rozproszone
