# Contribution Guidelines

Everybody is warmly welcome to contribute to `OwnRide`! 
To maintain high code quality and consistency, please follow these guidelines.

## Commit Message Guidelines

We follow the standard Git commit message conventions, primarily based on [The Seven Rules of a Good Git Commit Message](https://cbea.ms/git-commit/).

### 1. Separate subject from body with a blank line
The first line should be the summary of the changes. 
If you need to provide more context, add a blank line followed by a more detailed description.

### 2. Limit the subject line to 50 characters
Keep the subject concise and to the point. 
Aim for 50 characters or fewer.

### 3. Capitalize the subject line
Start the subject line with an uppercase letter.

### 4. Do not end the subject line with a period
The subject line is a title, not a sentence.

### 5. Use the imperative mood in the subject line
Write the subject as if you are giving an order: `Add feature`, `Fix bug`, `Update documentation`, etc.
*   **Correct:** `Fix user authentication`
*   **Incorrect:** `Fixed user authentication`, `Fixes user authentication`, `Fixing user authentication`

### 6. Wrap the body at 72 characters
If you have a body, ensure it doesn't run too long per line for better readability in terminal tools.

### 7. Use the body to explain *why* vs. *what*
Focus on the motivation behind the change, not a literal description of the code changes (the diff shows "what"). 
Explain what problem this solves and why this approach was taken.

### NO Prefixes
We do **not** use prefixes like `feat:`, `fix:`, `refactor:`, or `bug:` in the subject line. 
Just a clean, imperative subject.

### Language
All commit messages must be in **English**.

---

## Development Workflow

1.  **Fork** the repository.
2.  Create a **Feature Branch** (`git checkout -b amazingFeature`).
3.  Commit your changes using the guidelines above.
4.  Run local verification: `mvn spotless:apply sortpom:sort verify`.
5.  Push the branch (`git push origin amazingFeature`).
6.  Open a **Pull Request**.
