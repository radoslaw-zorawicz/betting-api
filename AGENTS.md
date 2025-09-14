# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/radoslawzorawicz/battingapi` — Spring Boot source (e.g., `BettingApplication`, `EventRetriever`, `RaceEvent`).
- `src/main/resources` — configuration and assets (`application.properties`, `static/`, `templates/`).
- `src/test/java` — JUnit 5 tests mirroring the main package structure.
- `pom.xml` — Maven configuration. Use the Maven Wrapper (`./mvnw`). Build outputs to `target/`.

## Build, Test, and Development Commands
- `./mvnw clean verify` — compile, run tests, and package the app.
- `./mvnw spring-boot:run` — run locally at `http://localhost:8080`.
- `./mvnw test` — run all tests. Example single test: `./mvnw -Dtest=BettingApplicationTests test`.
- Java version: ensure `JAVA_HOME` targets Java 24 (as set in `pom.xml`).

## Coding Style & Naming Conventions
- Java style: 4 spaces, no tabs; one class per file; avoid wildcard imports.
- Packages: `com.radoslawzorawicz.bettingapi`.
- Naming: Classes `PascalCase` (e.g., `BettingApplication`), methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- Spring: prefer constructor injection (as in `EventRetriever`). Keep components cohesive and small.

### Method Ordering
- Order by visibility: public → package‑private (no modifier) → private.
- Within each visibility group, list static methods first, then instance methods.
- Keep related overloads adjacent within the same group for readability.
- Apply consistently across classes, components, and controllers.

### Static Methods
- Prefer instance methods; do not mark methods `static` unless they are pure, stateless utilities in a dedicated utility class or an intentional factory.
- Avoid static mutable state.
- When in doubt, choose instance methods for easier testability and extension.

Method ordering clarification: within each visibility group, list static methods first (when present). This does not encourage adding new static methods; prefer instance methods per “Static Methods”.

### Local Variables and Types
- Immutability: declare local variables as `final` when they are not reassigned.
- Use `final var` for direct construction or obvious factory calls where the type is clear from the initializer.
  - Examples: `final var user = new User(id, name);`, `final var client = RestClient.create();`
- Avoid `var` for lambdas/method references; use explicit functional interface types.
  - Example: `final Supplier<Integer> supplier = () -> 1;`
- Prefer explicit type when assigning from method results to aid readability and navigation.
  - Example: `final Foo foo = bar.generateFoo();`
- Favor readability over brevity: if `var` obscures intent (e.g., complex generics), choose an explicit type. Keep usage consistent within a scope.
 - Lambdas: use meaningful parameter names; avoid single-letter names. If a parameter is unused, name it `ignored`.

## Testing Guidelines
- Frameworks: JUnit 5 with Spring Boot Test.
- Location: mirror production packages under `src/test/java`.
- Naming: `*Tests.java` (e.g., `BettingApplicationTests`).
- Scope: use `@SpringBootTest` only when needed; favor lighter tests for components without full context.
- Run before PR: `./mvnw clean verify`.

### Additional Testing Rules

- Single responsibility per test
  - Each test verifies one behavior or scenario.
  - Prefer one logical assert block; group multiple assertions only if tightly related.

- Parameterized tests
  - Use `@ParameterizedTest` for the same behavior across multiple inputs.
  - `@CsvSource` — prefer when inputs are small combinations of scalars/strings/enums:
    - Good for enums + a few extra scalars (JUnit auto‑converts enum names), booleans, ints, and simple strings.
    - Keep rows short and readable; for large or dynamic inputs, switch to `@MethodSource`.
    - Use quoted strings for values that include spaces or delimiters; adjust `delimiter` when needed (e.g., `delimiter = ';'`).
    - Represent special cases with `nullValues` and `emptyValue` parameters.
    - Pass `BigDecimal` as strings to preserve scale (e.g., `"10.00"`).
    - For boundary matrices, prefer tabular `@CsvSource` rather than many similar tests.
  - `@EnumSource` — use for pure enum cases; include/exclude subsets with `names` + `mode`. Avoid `@MethodSource` when data is enum‑only.
  - `@MethodSource` — reserve for complex objects, dynamic data, or when inputs are not conveniently expressible via `@CsvSource`/`@EnumSource`.

- Enum inputs
  - Prefer `@EnumSource` for testing multiple enum values; use `names` with `mode = INCLUDE/EXCLUDE` as needed.
  - Generic pattern: when exercising a subset of any enum, use `@EnumSource(MyEnum.class, names = {"VAL1", "VAL2"}, mode = EnumSource.Mode.INCLUDE)` to drive the test over the desired cases without a custom provider. In the test method, accept `MyEnum` as a parameter and derive any needed values from it.
  - Avoid `@MethodSource` when parameters are exclusively enum-based; `@EnumSource` is simpler, clearer, and self-documenting.
  - If you need a small extra scalar alongside an enum (e.g., expected int code), prefer `@CsvSource` with enum names and the scalar. JUnit will convert the enum name to the enum type automatically.
  - Reserve `@MethodSource` for cases where inputs require building complex objects or dynamic data not expressible via `@EnumSource`/`@CsvSource`.

- Determinism
  - Avoid non‑determinism: stub randomness (e.g., odds), fix time sources where needed.

### Testing Conventions
- Method names: prefix with `should` and describe behavior, e.g., `shouldReturnDriversWithOdds()`.
- Structure: separate phases with comments `// given`, `// when`, `// then`.
- Assertions: use AssertJ fluent API (`assertThat(...)`). For Vavr types, use AssertJ‑Vavr (`containsOnLeft`, `hasRightValueSatisfying`, `contains`, `isEmpty`).
- Collections: prefer collection assertions (`containsExactly`, `containsExactlyInAnyOrder`, `containsOnly`, `hasSize`, `allMatch`) over index-based checks like `list.get(0)`.
- Object equality: when asserting all fields, construct an expected instance and compare (`isEqualTo` or `usingRecursiveComparison`) instead of field-by-field assertions.
  - For `Option`/`Either`, assert the container holds the expected whole object (e.g., `Option.contains(expected)`, `Either.containsOnRight(expected)`), rather than asserting individual fields; use `hasValueSatisfying`/`hasRightValueSatisfying` only when additional checks are necessary.
- Controller tests (RestAssured/MockMvc):
  - Use Hamcrest matchers for fluent HTTP/JSON assertions. Always prefer `statusCode(...)` and `body(jsonPath, matcher)` with `hasSize`, `contains`, `equalTo`, etc.
  - Do not post-process JSON into maps/lists for AssertJ when a Hamcrest matcher would express the intent directly.
  - Keep AssertJ (and AssertJ‑Vavr) for non-HTTP unit tests (domain/services) and any value/object comparisons outside HTTP response shape.
  - Example: `given().accept(JSON).when().get("/events").then().statusCode(200).body("driver.driverNumber", contains(1, 16));`
 - Formatting: break fluent assertions across lines for readability (one call per line):
   - RestAssured/Hamcrest:
     `given()
        .when()
        .get("/events/{session_id}/drivers_market", "abc")
        .then()
        .statusCode(422);`
   - AssertJ:
     `assertThat(list)
        .hasSize(2)
        .containsExactlyElementsOf(expected);`

#### String/Text Blocks in Tests
- Prefer Java text blocks (`"""..."""`) for multiline JSON or payloads instead of string concatenation.
- For dynamic values inside text blocks, use placeholders with `.formatted(...)` to inject values, rather than concatenation.
- Keep indentation readable; align JSON fields and arrays to reflect the intended structure.

## Commit & Pull Request Guidelines
- Commits: imperative mood and concise subject (≤72 chars). Example: `feat(retriever): add OpenF1 client using RestClient`.
- Reference issues with `#<id>` when applicable.
- PRs: include a clear description, linked issues, test coverage for changes, and any local run steps. Ensure `./mvnw clean verify` passes.

## Security & Configuration Tips
- Do not commit secrets. Use environment variables or profile-specific files (e.g., `application-local.properties`).
- Activate profiles with `--spring.profiles.active=local` when running.
- External calls: `EventRetriever` targets OpenF1; handle timeouts/retries where appropriate.

## Agent-Specific Implementation Rules
- Prefer declarative Stream API for data pipelines (arrays/collections): use `map`/`filter`/`flatMap`/`sorted`/`distinct` with `collect`/`toList()`/`toSet()`, reductions (`reduce`, `max`, `min`, `sum`), and grouping (`groupingBy`, `partitioningBy`). Favor method references and `IntStream/LongStream` for primitives. Avoid external mutation in pipelines.
- Return empty collections, not `null`; use `Optional` for optional scalars.
- Keep methods pure where practical; avoid shared mutable state; favor constructor injection and `final` fields.
- Example pattern: `Arrays.stream(sessions).filter(Objects::nonNull).map(this::toRaceEvent).toList()`; extract `private RaceEvent toRaceEvent(SessionDto s)` for clarity and reuse.

### Functional Error Handling (Either/Optional)
- Avoid throwing for expected flows; keep methods total and side‑effect free where practical.
- Never return `null`. Use `Either` for domain errors and `Optional` for missing scalars; return empty collections for lists/sets/maps.
- Keep pipelines declarative; transform with `map`/`flatMap` and branch with `fold`.

#### Either
- Semantics: `Right` = success, `Left` = error. Name `Left` types explicitly (e.g., `RaceRetrievalError`).
- Transform with `map` (success) and `mapLeft` (error). Chain with `flatMap` to propagate failures.
- Branch with `fold(leftFn, rightFn)`; do not call `getLeft()`/`getRight()`; avoid `isLeft()`/`isRight()` when `fold` suffices.
- Convert exceptions at boundaries: catch and return `Either.left(domainError)`; avoid throwing inside functional flows.
- Keep `Either` within service/app layers; convert to framework types (HTTP, DB) at the edges.

#### Optional
- Use Java `Optional` in public APIs; avoid mixing with Vavr `Option`.
- Create with `Optional.ofNullable(...)`; transform via `map`/`flatMap`; unwrap only via `orElse*`/`orElseThrow` at boundaries.
- Do not wrap collections in `Optional`; return empty collections instead. Reserve `Optional<T>` for scalars.
- Elevate to `Either` when needed: `optional.map(Either::right).orElseGet(() -> Either.left(error))`.
- Avoid `isPresent()`/`isEmpty()` + `get()`; prefer fluent `map`/`flatMap` + `orElse*`/`orElseGet` for branching, and `orElseThrow` only at boundaries.
- Example: `repo.find(id).map(this::toDto).orElseGet(() -> defaultValue);`

### Either/Optional Transformation
- Compose in small steps: prefer a chain of `map` (Right) / `mapLeft` (Left) and `flatMap` for dependent calls; avoid large lambdas.
- Adapt early, fold late: convert types close to the source with `map`/`mapLeft`; call `fold` only at boundaries (e.g., controllers, persistence gateways).
- Keep it single‑expression: return the composed chain; introduce variables only when reused or for clarity.
- Method references first: `ResponseEntity::ok`, `Controller::toHttpStatus`, `this::toDto`; use tiny helpers for readable chains.
- Don’t probe `Either`: avoid `getLeft()/getRight()` and `isLeft()/isRight()`; branch via `fold`.
- Error evolution across layers: keep domain error types explicit; translate using `mapLeft` instead of throwing.
- Optional usage: use Java `Optional` for scalars; avoid `Optional<Collection>`—return empty collections.
- Use Vavr interop for fluent composition, e.g., `Option.toEither(error)`, `Try.toEither()`.
- Prefer `Either.filterOrElse(predicate, leftSupplier)` over inline `if` checks inside `map`/`flatMap` to encode validation within the chain.
 - Map vs. flatMap: use `map` when transforming only the Right value; avoid `flatMap(... -> Either.right(...))`. Reserve `flatMap` for chaining another `Either`-producing call.

### Controller/Edge Adapters
- Accept optional query params as boxed types or `Optional<T>`; avoid sentinel values.
- Centralize error mapping: extract `private static HttpStatus toHttpStatus(DomainError e)`; avoid inline `switch` blocks.
- Pre‑map errors, then fold: `return service.call(...).mapLeft(Controller::toHttpStatus).fold(status -> ResponseEntity.status(status).build(), ResponseEntity::ok);`
- Avoid duplication: success via `ResponseEntity::ok`, error via a single `status -> ResponseEntity.status(status).build()` lambda.
- Keep controllers thin: parse params, delegate to services, adapt `Either`/`Optional` to HTTP; keep business logic and error construction in services.

### Spring Data Repositories
- When adding a Spring Data repository method that returns an optional value:
  - If Vavr is present in the project, ask whether to return Java `Optional` or Vavr `Option` before implementing.
  - If Vavr is not present (or no answer), default to Java `Optional`.

### Spring Boot

#### HTTP URI Building (RestClient)
- Prefer fluent `UriBuilder` with `queryParamIfPresent` over manual `if` checks for optional parameters.
- Lift optionals with `Optional.ofNullable(value)`; for strings, add `.filter(s -> !s.isBlank())` to skip blanks.
- Compose as a single expression and call `.build()` at the end; avoid temporary variables unless reused.
- Let `UriBuilder` handle encoding; do not concatenate query strings manually.
- Omit null/blank parameters to rely on server defaults instead of sending empty values.
- Example: `uri(b -> b.path("/sessions").queryParamIfPresent("year", Optional.ofNullable(year)).queryParamIfPresent("meeting_key", Optional.ofNullable(meetingKey)).queryParamIfPresent("session_type", Optional.ofNullable(sessionType).filter(s -> !s.isBlank())).build())`

#### Mockito / BDDMockito
- If `org.mockito:mockito-core` (or BDDMockito API) is present on the test classpath, always use BDDMockito in tests instead of vanilla Mockito stubbing.
  - Stubbing: use `given(...).willReturn(...)`, `given(...).willThrow(...)`, `given(...).willAnswer(...)` instead of `when(...).thenReturn/thenThrow/thenAnswer`.
  - Verification: prefer `then(mock).should(...).method(...)` over `verify(mock).method(...)` when following BDD style.
  - Use static imports from `org.mockito.BDDMockito` for `given` (and `then` when applicable) for readability and consistency.

### Granular Tests (Agent Rule)
- Single behavior per test: if a test verifies more than one independent behavior, split it into separate tests. Example: greater/less/equal comparisons are three tests.
- One logical assert block: group only tightly related expectations; avoid mixing unrelated assertions in one test.
- Parameterize variations: when exercising the same behavior across inputs, use `@ParameterizedTest` with `@CsvSource`/`@EnumSource` before adding more assertions.
- Naming: start with `should...` and describe the single behavior under test clearly.
- Boundaries as separate tests: treat null, zero, negative, and equality as distinct cases rather than bundling them together.

### Imports
- Prefer imports over fully qualified names (FQNs) throughout code and tests.
- Avoid wildcard imports for both types and static members; keep imports explicit.
- Use static imports when they improve readability (e.g., assertions, matchers, BDD helpers, common constants). Avoid broad static imports that introduce ambiguity.
- Resolve name clashes by importing one type and qualifying the other at the usage site. Do not mix imported and FQN forms for the same type within a file.
- Keep imports minimal and consistent: import only what is used; remove unused and duplicate imports.
- Organize imports in groups with a blank line between:
  - Java/Jakarta (`java.*`, `javax.*`, `jakarta.*`)
  - Third‑party libraries
  - Project (`com.radoslawzorawicz.bettingapi.*`)
  Sort alphabetically within each group.
- Production code: be conservative with static imports (prefer for constants). Tests: freely use static imports for fluent DSLs to enhance readability.
