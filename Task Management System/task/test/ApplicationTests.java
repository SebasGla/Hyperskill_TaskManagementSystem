import com.google.gson.Gson;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

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

        checkResponseStatus(response, expectedCode);

        return CheckResult.correct();
    }

    CheckResult testGetTasks(TestUser user, int expectedCode) {
        var response = get(tasksUrl).basicAuth(user.email(), user.password()).send();

        checkResponseStatus(response, expectedCode);

        return CheckResult.correct();
    }

    private void checkResponseStatus(HttpResponse response, int expectedCode) {
        var uri = response.getRequest().getUri();
        var method = response.getRequest().getMethod();
        var requestBody = response.getRequest().getContent();
        var statusCode = response.getStatusCode();
        var responseBody = response.getContent();
        if (statusCode != expectedCode) {
            throw new WrongAnswer("""
                    Request: %s %s
                    Request body: %s
                    Expected status code %d but received %d
                    Response body: %s
                    """.formatted(method, uri, requestBody, expectedCode, statusCode, responseBody)
            );
        }
    }

    private CheckResult reloadServer() {
        try {
            reloadSpring();
        } catch (Exception e) {
            throw new WrongAnswer("Failed to reload application");
        }
        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // test user registration
            () -> testCreateUser(TestUser.alice(), 200),
            () -> testCreateUser(TestUser.alice(), 409),
            () -> testCreateUser(TestUser.alice().withEmail("ALICE@email.com"), 409),
            () -> testCreateUser(TestUser.bob(), 200),
            () -> testCreateUser(TestUser.withBadEmail(" "), 400),
            () -> testCreateUser(TestUser.withBadEmail(null), 400),
            () -> testCreateUser(TestUser.withBadEmail("malformed@email."), 400),
            () -> testCreateUser(TestUser.withBadPassword(null), 400),
            () -> testCreateUser(TestUser.withBadPassword("      "), 400),
            () -> testCreateUser(TestUser.withBadPassword("12345"), 400),

            // test API access
            () -> testGetTasks(TestUser.alice(), 200),
            () -> testGetTasks(TestUser.bob(), 200),
            () -> testGetTasks(TestUser.alice().withEmail("alice@test.com"), 401),
            () -> testGetTasks(TestUser.alice().withEmail("ALICE@email.com"), 200),
            () -> testGetTasks(TestUser.alice().withPassword("Password"), 401),

            // test persistence
            this::reloadServer,
            () -> testCreateUser(TestUser.alice(), 409),
            () -> testGetTasks(TestUser.alice(), 200),
    };
}
