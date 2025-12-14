# Contributing to SaveTheGovernment

Thank you for your interest in contributing to **SaveTheGovernment**.  
This document describes the rules, workflow, and quality standards that must be followed by all contributors to ensure clean, maintainable, and consistent code.

---

## 1. General principles

- Do **not** commit directly to the `main` branch.
- All changes must go through **Pull Requests (PRs)**.
- Every change must be **reviewed and verified**.
- Code should remain **clean, readable, and consistent**.

---

## 2. Branching strategy

### Protected `main` branch
- `main` is protected.
- Only **merged Pull Requests** should land on `main`.

### Feature / bugfix branches
Create a new branch for each change:

```bash
git checkout -b feature/feature-name
git checkout -b bugfix/bug-description
```

Examples:
```bash
feature/ui-sidebar
feature/budget-summary
bugfix/ui-overlap
bugfix/null-validation
```

---

## 3. Sync with `main`

Before starting work:

```bash
git switch main
git pull origin main
git checkout -b feature/your-feature
git rebase main
```

Alternative (if you prefer merge instead of rebase):

```bash
git merge main
```

---

## 4. Commit rules

### Small & atomic commits
- One commit should represent one logical change.
- Avoid large “everything at once” commits.

### Commit message convention
Use **Conventional Commits**:

```bash
git commit -m "feat: add budget summary table"
```

Common prefixes:
- **feat** – new feature
- **fix** – bug fix
- **refactor** – code improvement without behavior change
- **docs** – documentation changes
- **test** – add or improve tests
- **chore** – maintenance tasks (config/build/tooling)

---

## 5. Code quality requirements

Before opening a PR, your branch must pass:

```bash
mvn clean verify
```

This includes:
- Compile
- Unit tests
- Checkstyle
- SpotBugs
- Packaging

PRs with failing builds should **not** be approved.

---

## 6. Static analysis

### SpotBugs
```bash
mvn spotbugs:check
mvn spotbugs:gui
```

SpotBugs output is generated at:
```
target/spotbugsXml.xml
```

### Checkstyle
```bash
mvn checkstyle:check
```

All code must comply with the existing Checkstyle configuration.

---

## 7. Testing rules

- Any business-critical change should include corresponding tests.
- Tests live under `src/test/java`.
- Test classes should follow the naming convention: `Test<ClassName>`.
- Tests should be **independent** (avoid shared mutable state).

---

## 8. Pull requests

Every Pull Request:
- Must be approved by **at least one team member**.
- Will be checked for:
  - Correctness / functionality
  - Conflicts
  - Readability and cleanliness
  - Performance (where relevant)
  - Compliance with project standards

Recommended PR description contents:
- What changed
- Why it changed
- How it was tested

---

## 9. Documentation

- Public classes and methods should include **Javadoc**.
- Any changes to workflow, tools, or usage should be reflected in:
  - `README.md`, or
  - the relevant documentation files

---

## 10. Files that must not be committed

The following should not be pushed to the repository:

```text
target/
*.class
.DS_Store
.vscode/
.idea/
```

Make sure these are covered by `.gitignore`.

---

## 11. Development environment

- Java **21**
- Maven **3.9+**
- Shared `pom.xml` for all dependencies and plugins
- Maven Wrapper (`./mvnw`) is recommended for consistent builds

---

## 12. Final note

Following these rules:
- improves project quality
- reduces merge conflicts
- makes code reviews easier
- reflects professional software engineering practices

Thank you for contributing.
