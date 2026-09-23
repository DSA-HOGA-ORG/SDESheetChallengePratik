import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Test runner for Striver's 45-Day Challenge Java solutions.
 *
 * Usage after compiling every Java source file:
 *   java main                 Run every registered problem
 *   java main <problem-slug>  Run one registered problem
 *
 * A solution is a public class named Solution in:
 *   Topic/Subtopic/Solution.java
 *
 * Register solutions and test cases in the static block below.
 */
public final class main {
    private static final Map<String, Problem> PROBLEMS = new LinkedHashMap<>();

    static {
        // Example registration (remove the comment and add your real problems):
        // register(
        //     "two-sum",
        //     "Hashing.HashingAndPrefixSums.Solution",
        //     "twoSum",
        //     new TestCase(
        //         "example 1",
        //         () -> new Object[] {new int[] {2, 7, 11, 15}, 9},
        //         new int[] {0, 1}
        //     )
        // );
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java main [problem-slug]");
            System.exit(2);
        }

        if (args.length == 1) {
            String slug = args[0].trim().toLowerCase(Locale.ROOT);
            Problem problem = PROBLEMS.get(slug);
            if (problem == null) {
                printUnknownProblem(slug);
                System.exit(2);
            }
            System.exit(runProblem(slug, problem));
        }

        if (PROBLEMS.isEmpty()) {
            System.out.println("No problems registered yet. Add the first one in main.java.");
            return;
        }

        int failedProblems = 0;
        for (Map.Entry<String, Problem> entry : PROBLEMS.entrySet()) {
            failedProblems += runProblem(entry.getKey(), entry.getValue());
        }
        System.exit(failedProblems == 0 ? 0 : 1);
    }

    private static void register(
            String slug,
            String solutionClass,
            String methodName,
            TestCase... testCases
    ) {
        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);
        if (normalizedSlug.isEmpty()) {
            throw new IllegalArgumentException("Problem slug cannot be empty.");
        }
        if (testCases.length == 0) {
            throw new IllegalArgumentException("Add at least one test case for " + normalizedSlug + '.');
        }
        List<TestCase> cases = List.copyOf(java.util.Arrays.asList(testCases));
        if (PROBLEMS.put(normalizedSlug, new Problem(solutionClass, methodName, cases)) != null) {
            throw new IllegalArgumentException("Duplicate problem slug: " + normalizedSlug);
        }
    }

    private static int runProblem(String slug, Problem problem) {
        System.out.println("\n=== " + slug + " ===");

        Class<?> solutionClass;
        Object solution;
        try {
            solutionClass = Class.forName(problem.solutionClass());
            solution = solutionClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            System.out.println("  [ERROR] Cannot load " + problem.solutionClass());
            System.out.println("          Compile all .java files before running main.java.");
            System.out.println("          " + rootMessage(exception));
            return 1;
        }

        int passed = 0;
        for (TestCase testCase : problem.testCases()) {
            Object[] arguments;
            Method method;
            try {
                arguments = testCase.arguments().get();
                if (arguments == null) {
                    throw new IllegalArgumentException("Test input supplier returned null.");
                }
                method = findMethod(solutionClass, problem.methodName(), arguments.length);
                if (method == null) {
                    throw new NoSuchMethodException(
                            "No public method '" + problem.methodName() + "' with "
                                    + arguments.length + " parameter(s)."
                    );
                }
            } catch (ReflectiveOperationException | IllegalArgumentException exception) {
                System.out.println("  [ERROR] " + testCase.name() + ": " + exception.getMessage());
                continue;
            }

            try {
                Object returnedValue = method.invoke(solution, arguments);
                Object actual = method.getReturnType() == void.class ? arguments[0] : returnedValue;

                if (deepEquals(actual, testCase.expected())) {
                    System.out.println("  [PASS] " + testCase.name());
                    passed++;
                } else {
                    System.out.println("  [FAIL] " + testCase.name());
                    System.out.println("         expected: " + readable(testCase.expected()));
                    System.out.println("         actual:   " + readable(actual));
                }
            } catch (IllegalAccessException | IllegalArgumentException exception) {
                System.out.println("  [ERROR] " + testCase.name() + ": " + exception.getMessage());
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                System.out.println("  [ERROR] " + testCase.name() + ": " + rootMessage(cause));
            }
        }

        System.out.println(slug + ": " + passed + '/' + problem.testCases().size() + " passed");
        return passed == problem.testCases().size() ? 0 : 1;
    }

    private static Method findMethod(Class<?> solutionClass, String methodName, int parameterCount) {
        List<Method> matches = new ArrayList<>();
        for (Method method : solutionClass.getMethods()) {
            if (method.getName().equals(methodName)
                    && method.getParameterCount() == parameterCount) {
                matches.add(method);
            }
        }

        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                    "Multiple public methods named '" + methodName + "' match "
                            + parameterCount + " parameter(s); rename the overloaded method."
            );
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private static boolean deepEquals(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }

        Class<?> firstClass = first.getClass();
        Class<?> secondClass = second.getClass();

        if (firstClass.isArray() && secondClass.isArray()) {
            if (firstClass.getComponentType().isPrimitive()
                    != secondClass.getComponentType().isPrimitive()) {
                return false;
            }
            int length = Array.getLength(first);
            if (length != Array.getLength(second)) {
                return false;
            }
            for (int index = 0; index < length; index++) {
                if (!deepEquals(Array.get(first, index), Array.get(second, index))) {
                    return false;
                }
            }
            return true;
        }

        if (first instanceof List<?> firstList && second instanceof List<?> secondList) {
            if (firstList.size() != secondList.size()) {
                return false;
            }
            for (int index = 0; index < firstList.size(); index++) {
                if (!deepEquals(firstList.get(index), secondList.get(index))) {
                    return false;
                }
            }
            return true;
        }

        return first.equals(second);
    }

    private static String readable(Object value) {
        if (value == null) {
            return "null";
        }
        if (value.getClass().isArray()) {
            StringBuilder output = new StringBuilder("[");
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                if (index > 0) {
                    output.append(", ");
                }
                output.append(readable(Array.get(value, index)));
            }
            return output.append(']').toString();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(main::readable).toList().toString();
        }
        return String.valueOf(value);
    }

    private static String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    private static void printUnknownProblem(String slug) {
        System.err.println("Unknown problem: " + slug);
        System.err.println("Registered problems:");
        if (PROBLEMS.isEmpty()) {
            System.err.println("  (none)");
        } else {
            for (String registeredSlug : PROBLEMS.keySet()) {
                System.err.println("  - " + registeredSlug);
            }
        }
    }

    private record TestCase(
            String name,
            Supplier<Object[]> arguments,
            Object expected
    ) {
        private TestCase {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Test case name cannot be empty.");
            }
            if (arguments == null) {
                throw new IllegalArgumentException("Test case input cannot be null.");
            }
        }
    }

    private record Problem(
            String solutionClass,
            String methodName,
            List<TestCase> testCases
    ) {
        private Problem {
            if (solutionClass == null || solutionClass.isBlank()) {
                throw new IllegalArgumentException("Solution class cannot be empty.");
            }
            if (methodName == null || methodName.isBlank()) {
                throw new IllegalArgumentException("Method name cannot be empty.");
            }
        }
    }
}
