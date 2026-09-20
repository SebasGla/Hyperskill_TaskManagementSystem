import com.google.gson.Gson;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.expect.json.builder.JsonArrayBuilder;
import org.hyperskill.hstest.testing.expect.json.builder.JsonObjectBuilder;

import java.util.List;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isArray;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isString;

public class ApplicationTests extends SpringTest {
    private static final String accountsUrl = "/api/accounts";
    private static final String tasksUrl = "/api/tasks";
    private final Gson gson = new Gson();

    public ApplicationTests() {
        super("../tms_db.mv.db");
    }

    CheckResult testCreateUser(TestUser user, int expectedCode) {
        var content = gson.toJson(user);
        var response = post(accountsUrl, content).send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        return CheckResult.correct();
    }

    CheckResult testCreateTask(TestTask task, TestUser author, int expectedCode) {
        var content = gson.toJson(task);
        var response = post(tasksUrl, content).basicAuth(author.email(), author.password()).send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("id", isString())
                            .value("title", task.getTitle())
                            .value("description", task.getDescription())
                            .value("status", "CREATED")
                            .value("author", author.email())
            );
            task.setAuthor(author.email());
            task.setStatus("CREATED");
        }

        return CheckResult.correct();
    }

    CheckResult testGetAllTasks(TestUser user, List<TestTask> expectedTasks, int expectedCode) {
        return testGetTasksByAuthor(user, null, expectedTasks, expectedCode);
    }

    CheckResult testGetTasksByAuthor(TestUser user, String author, List<TestTask> expectedTasks, int expectedCode) {
        var request = get(tasksUrl).basicAuth(user.email(), user.password());
        if (author != null) {
            request = request.addParam("author", author);
        }

        var response = request.send();

        System.out.println(getRequestDetails(response));

        var actualCode = response.getStatusCode();
        if (actualCode != expectedCode) {
            return CheckResult.wrong(
                    "Expected status code %d but received %d".formatted(expectedCode, actualCode)
            );
        }

        if (actualCode == 200) {
            JsonArrayBuilder arrayBuilder = isArray(expectedTasks.size());
            for (var task : expectedTasks) {
                JsonObjectBuilder objectBuilder = isObject()
                        .value("id", isString())
                        .value("title", task.getTitle())
                        .value("description", task.getDescription())
                        .value("status", task.getStatus())
                        .value("author", task.getAuthor());
                arrayBuilder = arrayBuilder.item(objectBuilder);
            }
            expect(response.getContent()).asJson().check(arrayBuilder);
        }

        return CheckResult.correct();
    }

    private String getRequestDetails(HttpResponse response) {
        var uri = response.getRequest().getUri();
        var method = response.getRequest().getMethod();
        var requestBody = response.getRequest().getContent();
        return "\nRequest: %s %s\nRequest body: %s\n".formatted(method, uri, requestBody);
    }

    private CheckResult reloadServer() {
        try {
            reloadSpring();
        } catch (Exception e) {
            throw new WrongAnswer("Failed to reload application");
        }
        return CheckResult.correct();
    }

    TestUser alice = TestUser.alice();
    TestUser bob = TestUser.bob();

    TestTask firstTask = TestTask.task1();
    TestTask secondTask = TestTask.task2();
    TestTask thirdTask = TestTask.task3();

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // register user
            () -> testCreateUser(alice, 200), // #1
            () -> testCreateUser(alice, 409),
            () -> testCreateUser(alice.withEmail("ALICE@email.com"), 409),
            () -> testCreateUser(bob, 200),
            () -> testCreateUser(TestUser.withBadEmail(" "), 400), // #5
            () -> testCreateUser(TestUser.withBadEmail(null), 400),
            () -> testCreateUser(TestUser.withBadEmail("malformed@email."), 400),
            () -> testCreateUser(TestUser.withBadPassword(null), 400),
            () -> testCreateUser(TestUser.withBadPassword("      "), 400),
            () -> testCreateUser(TestUser.withBadPassword("12345"), 400), // #10

            // create task
            () -> testCreateTask(firstTask, alice, 200),
            () -> testCreateTask(secondTask, alice, 200),
            () -> testCreateTask(thirdTask, bob, 200),
            () -> testCreateTask(thirdTask.withTitle(null), bob, 400),
            () -> testCreateTask(firstTask.withTitle(" "), bob, 400),  // #15
            () -> testCreateTask(firstTask.withDescription(null), bob, 400),
            () -> testCreateTask(firstTask.withDescription(" "), bob, 400),

            // get all tasks
            () -> testGetAllTasks(alice, List.of(thirdTask, secondTask, firstTask), 200),
            () -> testGetAllTasks(bob, List.of(thirdTask, secondTask, firstTask), 200),
            () -> testGetAllTasks(alice.withEmail("alice@test.com"), List.of(), 401), // #20
            () -> testGetAllTasks(alice.withEmail("ALICE@email.com"), List.of(thirdTask, secondTask, firstTask), 200),
            () -> testGetAllTasks(alice.withPassword("Password"), List.of(), 401),

            // get tasks by author
            () -> testGetTasksByAuthor(alice, alice.email(), List.of(secondTask, firstTask), 200),
            () -> testGetTasksByAuthor(bob, alice.email(), List.of(secondTask, firstTask), 200),
            () -> testGetTasksByAuthor(alice, bob.email(), List.of(thirdTask), 200), // #25
            () -> testGetTasksByAuthor(alice, "unknown", List.of(), 200),

            // test persistence
            this::reloadServer,
            () -> testCreateUser(alice, 409),
            () -> testGetAllTasks(alice, List.of(thirdTask, secondTask, firstTask), 200),
    };
}
