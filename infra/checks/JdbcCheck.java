import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

class JdbcCheck {
    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "check" : args[0];
        if (!java.util.Set.of("check", "write", "read").contains(mode))
            throw new IllegalArgumentException("Use check, write or read");
        String user = System.getenv("JDBC_USER");
        if (!"BOOKING".equals(user)) throw new IllegalStateException("Expected dedicated BOOKING schema");
        String password = Files.readString(Path.of(System.getenv("JDBC_PASSWORD_FILE"))).strip();
        DriverManager.setLoginTimeout(30);
        try (var connection = DriverManager.getConnection(System.getenv("JDBC_URL"), user, password);
             var stmt = connection.createStatement()) {
            stmt.setQueryTimeout(30);
            var meta = connection.getMetaData();
            if (!meta.getDatabaseProductName().contains("Oracle") || meta.getDatabaseMajorVersion() < 19)
                throw new IllegalStateException("Oracle 19+ required");
            System.out.println("JDBC: " + meta.getDriverVersion());
            System.out.println("Database: " + meta.getDatabaseProductVersion());
            try (var rows = stmt.executeQuery("SELECT USER, SYS_CONTEXT('USERENV','CON_NAME') FROM dual")) {
                if (!rows.next() || !"BOOKING".equals(rows.getString(1)) || !"FREEPDB1".equals(rows.getString(2)))
                    throw new IllegalStateException("Unexpected schema or PDB");
                System.out.println("Schema=BOOKING, PDB=FREEPDB1, JDBC connection OK");
            }
            // Infrastructure-only table: no reservation or business data.
            if ("write".equals(mode)) {
                try {
                    stmt.executeUpdate("CREATE TABLE ENVIRONMENT_PROBE (ID NUMBER PRIMARY KEY, TOKEN VARCHAR2(36) NOT NULL)");
                } catch (SQLException e) {
                    if (e.getErrorCode() != 955) throw e;
                }
                String token = UUID.randomUUID().toString();
                try (var ps = connection.prepareStatement(
                        "MERGE INTO ENVIRONMENT_PROBE p USING (SELECT 1 id, ? token FROM dual) s " +
                        "ON (p.id=s.id) WHEN MATCHED THEN UPDATE SET p.token=s.token " +
                        "WHEN NOT MATCHED THEN INSERT (id, token) VALUES (s.id,s.token)")) {
                    ps.setString(1, token);
                    ps.executeUpdate();
                }
                Path marker = Path.of(".local/persistence-token");
                Files.createDirectories(marker.getParent());
                Files.writeString(marker, token);
                System.out.println("Persistence marker committed.");
            } else if ("read".equals(mode)) {
                String expected = Files.readString(Path.of(".local/persistence-token"));
                try (var rows = stmt.executeQuery("SELECT TOKEN FROM ENVIRONMENT_PROBE WHERE ID=1")) {
                    if (!rows.next() || !expected.equals(rows.getString(1)))
                        throw new IllegalStateException("Persistence marker missing or changed");
                }
                System.out.println("Persistence after restart: OK");
            }
        }
    }
}