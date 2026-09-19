public record TestUser(String email, String password) {
    public static TestUser alice() {
        return new TestUser("alice@email.com", "password");
    }

    public static TestUser bob() {
        return new TestUser("bob@example.com", "123456");
    }

    public static TestUser withBadEmail(String badEmail) {
        return new TestUser(badEmail, "password");
    }

    public static TestUser withBadPassword(String badPassword) {
        return new TestUser("test@test.com", badPassword);
    }

    public TestUser withEmail(String email) {
        return new TestUser(email, this.password);
    }

    public TestUser withPassword(String password) {
        return new TestUser(this.email, password);
    }
}
