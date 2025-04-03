package insert_lab3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class LoanRatesInsert extends BaseInsertClass{
    LoanRatesInsert(Connection connection) {
        super(connection);
    }
    int count = 100;

    @Override
    void insert() {
        String insertSQL = "INSERT INTO loan_rates (min_term, max_term, min_amount, max_amount, percentage_rate, purpose) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (int i = 0; i < count; i++) {
                preparedStatement.setInt(1, new Random().nextInt(12) + 1);
                preparedStatement.setInt(2, new Random().nextInt(12) + 12);
                preparedStatement.setInt(3, new Random().nextInt(5000) + 1000);
                preparedStatement.setInt(4, new Random().nextInt(1000000) + 50000);
                preparedStatement.setDouble(5, new Random().nextDouble(1, 20));
                preparedStatement.setString(6, faker.company().catchPhrase());
                preparedStatement.addBatch();

                if (i % 1000 == 0) {
                    preparedStatement.executeBatch();
                }
            }

            preparedStatement.executeBatch();
            System.out.println("Условия займа добавлены");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
