package insert_lab3;

import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class ScoringClientsInsert extends BaseInsertClass {

    ScoringClientsInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() throws SQLException {
        String insertSQL = "INSERT INTO scoring_clients (client_id, loan_rate_id, is_approved) VALUES (?, ?, ?)";
        int maxClientId = getMaxIdFromTable("clients");
        int maxLoanRates = getMaxIdFromTable("loan_rates");
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (int i = 1; i < maxClientId; i++) {
                for (int j = 1; j < maxLoanRates; j += faker.random().nextInt(1, 100)) {
                    preparedStatement.setInt(1, faker.random().nextInt(1, i));
                    preparedStatement.setInt(2, faker.random().nextInt(1, j));
                    preparedStatement.setBoolean(3, faker.random().nextBoolean());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
            System.out.println("Скоринг клиентов добавлен");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
