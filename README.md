# Striver's 45-Day Challenge — 180 SDE Problems

A structured repository for solving Striver's 180-problem SDE sheet over 45 days,
testing each solution from a root runner, recording daily progress in Markdown,
and pushing one verified commit per day.

## Repository structure

Solutions mirror the sheet's topic and subtopic hierarchy. A Java solution lives at
`Topic/Subtopic/Solution.java`.

```text
.
├── main.java                # Java test runner and problem registry
├── main.py                  # Python test runner
├── main.cpp                 # C++ test runner
├── Arrays/
│   ├── LinearScan/
│   ├── TwoPointers/
│   └── DivideAndConquer/
├── BinarySearch/
│   ├── BinarySearch/
│   ├── SearchOnAnswer/
│   └── PartitionSearch/
├── Hashing/
│   └── HashingAndPrefixSums/
├── SlidingWindowAndTwoPointers/
│   ├── SlidingWindow/
│   └── CountingWindows/
├── RecursionAndBacktracking/
│   ├── SubsetsAndCombinations/
│   └── Backtracking/
├── LinkedList/
│   └── FastAndSlowPointers/
└── logs/
    └── daily_log.md         # one Markdown logbook for all 45 days
```

The subtopic folders are currently empty so the first solution can establish the
naming pattern. Each problem uses a short, unique slug such as `two-sum` or
`set-matrix-zeroes`.

## Rules

- Solve each problem in **one language only**: Java, Python, or C++.
- Keep each solution file limited to the LeetCode `Solution` class.
- Keep all test inputs and expected outputs in the corresponding root runner.
- Add every problem to exactly one runner.
- Record every completed problem in `logs/daily_log.md`.
- Test before committing; a daily commit should never contain a knowingly failing
  solution.

## Daily workflow

1. Create the solution in the matching `Topic/Subtopic/` folder.
2. Register the problem and at least one test case in the selected runner.
3. Run that problem and then run the complete runner.
4. Add a structured problem entry to today's section in `logs/daily_log.md`.
5. Review the changes, commit them, and push them to GitHub.

For Java, `Topic/Subtopic/Solution.java` must contain a public class named
`Solution`, and the file must declare its package:

```java
package Arrays.LinearScan;

import java.util.List;

public class Solution {
    public List<Integer> subarraySum(int[] nums, int k) {
        // solution
    }
}
```

The package name must exactly match the solution's folders. For example,
`Arrays/LinearScan/Solution.java` uses `package Arrays.LinearScan;`.

## Java runner

Register each Java problem in the static block in `MajorityElementI.java`. The registration
contains its slug, fully qualified `Solution` class, LeetCode method name, and one
or more test cases:

```java
register(
    "subarray-sum-equals-k",
    "Arrays.LinearScan.Solution",
    "subarraySum",
    new TestCase(
        "example 1",
        () -> new Object[] {new int[] {1, 1, 1}, 2},
        2
    )
);
```

A test input is a supplier, so every run receives fresh, mutable input. For a Java
method returning `void`, the runner automatically compares the mutated first
argument with the expected result. Methods that return a value are compared by
their return value. Primitive arrays, nested arrays, and lists are supported.

Compile **all** Java files together. Do not compile only `MajorityElementI.java`, because Java
loads registered solution classes by their package-qualified names.

PowerShell:

```powershell
$sources = Get-ChildItem -Path . -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac $sources
java main
java main subarray-sum-equals-k
```

Git Bash or macOS/Linux:

```sh
find . -name "*.java" -print0 | xargs -0 javac
java main
java main subarray-sum-equals-k
```

Running without a slug executes all registered problems. Running with an unknown
slug prints the available problem slugs. Generated `*.class` files are ignored by
Git.

## Python runner

Register the problem in `KNOWN_PROBLEMS` and its cases in `TEST_CASES` in
`main.py`:

```python
KNOWN_PROBLEMS = {
    "set-matrix-zeroes": ("Arrays.LinearScan", "SetMatrixZeroes", "setZeroes"),
}

TEST_CASES = {
    "set-matrix-zeroes": [
        (
            ([[1, 1, 1], [1, 0, 1], [1, 1, 1]],),
            [[1, 0, 1], [0, 0, 0], [1, 0, 1]],
        ),
    ],
}
```

The corresponding solution is `Arrays/LinearScan/SetMatrixZeroes.py` and contains
only:

```python
class Solution:
    def setZeroes(self, matrix: list[list[int]]) -> None:
        ...
```

Run all or one problem with:

```sh
python main.py
python main.py set-matrix-zeroes
```

## C++ runner

For C++, include each solution in `main.cpp`, add a `run_<slug>()` function with
its test cases, and register that function in `PROBLEMS`. The solution file itself
contains only the namespaced `Solution` class.

```sh
g++ -std=c++17 main.cpp -o main
./main
./main set-matrix-zeroes
```

## Daily Markdown log

`logs/daily_log.md` is the source of truth for the challenge. It records:

- overall days and problems completed;
- the date, topic, subtopic, problems solved, and total time;
- a short summary and next-day plan;
- one section per problem containing its LeetCode link, solution link, status,
  approach, complexity, mistakes, learning, and next step;
- test status, so the repository records evidence that the solution was run.

For each new day, append one block at the end of the file and update the counters
at the top. Keep solved and attempted problems distinct: use **Solved** for a
passing solution, **Unsolved** for an incomplete attempt, and **Need Review** for
a passing solution that should be revisited.

## Manual daily GitHub commit

Automatic pushes are intentionally disabled. After the Java/Python/C++ tests pass
and the log is updated:

```powershell
git status
git add main.java logs/daily_log.md
git add "Topic/Subtopic/Solution.java"
git commit -m "feat(Arrays): solve set-matrix-zeroes"
git push origin main
```

Replace the runner, solution path, and commit message with the files and problem
actually worked on. If the problem is solved in Python or C++, stage that runner
instead of `MajorityElementI.java`.
