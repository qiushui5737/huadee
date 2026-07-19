import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RunSqlFile {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: RunSqlFile <jdbcUrl> <user> <password> <sqlFile>");
        }
        String sql = Files.readString(Path.of(args[3]), StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sql);
        try (Connection conn = DriverManager.getConnection(args[0], args[1], args[2]);
             Statement stmt = conn.createStatement()) {
            int count = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                stmt.execute(trimmed);
                count++;
            }
            System.out.println("Executed SQL statements: " + count);
        }
    }

    private static List<String> splitStatements(String sql) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';

            if (!inSingle && !inDouble && c == '-' && next == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            }

            if (c == ';' && !inSingle && !inDouble) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }
}
