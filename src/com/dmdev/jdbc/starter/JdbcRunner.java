package com.dmdev.jdbc.starter;

import com.dmdev.jdbc.starter.util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) {
        try {
            printAllAircrafts();
            System.out.println(getTicketsByFlightId(1));
            // saveImage();
            getImage();
        } finally {
            ConnectionManager.closePool();
        }
    }

    private static void getImage() {
        int id = 1;
        String sql = """
                SELECT img
                FROM aircraft
                WHERE id = ?;
                """;
        try (
                var connection = ConnectionManager.get();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                byte[] img = resultSet.getBytes("img");
                Files.write(Path.of("resources", "Boeing-777-new.png"), img);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveImage() {
        int id = 1;
        String sql = """
                UPDATE aircraft
                SET img = ?
                WHERE id = ?
                """;
        try (
                var connection = ConnectionManager.get();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setBytes(1, Files.readAllBytes(
                    Path.of("resources", "Boeing-777.png"))
            );
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Long> getTicketsByFlightId(int flightId) {
        List<Long> list = new ArrayList<>();
        String sql = """
                SELECT id
                FROM ticket
                WHERE flight_id = %d;
                """.formatted(flightId);
        String sql1 = """
                SELECT id
                FROM ticket
                WHERE flight_id = ?;
                """;
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(sql1)
        ) {
            preparedStatement.setInt(1, flightId);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getObject("id", Long.class));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static void printAllAircrafts() {
        try (
                var connection = ConnectionManager.get();
                var statement = connection.createStatement()
        ) {
            System.out.println("Transaction Isolation Level: " + connection.getTransactionIsolation());
            var resultSet = statement.executeQuery("SELECT * FROM aircraft ORDER BY 1;");

            while (resultSet.next()) {
                System.out.print(resultSet.getBigDecimal("id") + " ");
                System.out.println(resultSet.getString("model"));
            }
            System.out.println("----");

            // statement.executeUpdate("INSERT INTO aircraft (model) VALUES ('ABC')", Statement.RETURN_GENERATED_KEYS);
            // resultSet = statement.getGeneratedKeys();
            // if (resultSet.next()) {
            //     System.out.println(resultSet.getInt("id"));
            // }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
