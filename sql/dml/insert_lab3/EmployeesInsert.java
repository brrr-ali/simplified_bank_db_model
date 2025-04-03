package insert_lab3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmployeesInsert extends BaseInsertClass{
    EmployeesInsert(Connection connection) {
        super(connection);
    }
    int count = 5000;
    @Override
    void insert() throws SQLException {
        int n = getMaxIdFromTable("employees");
        String insertSQL = "INSERT INTO employees (first_name, last_name, email, phone_number, manager_id) VALUES (?, ?, ?, ?, ?)";
        List<Integer> employeeIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (int i = 0; i < count; i++) {
                preparedStatement.setString(1, faker.name().firstName());
                preparedStatement.setString(2, faker.name().lastName());
                preparedStatement.setString(3, faker.internet().emailAddress());
                preparedStatement.setString(4, faker.phoneNumber().cellPhone());
                if (n == 0 && employeeIds.isEmpty()) {
                    preparedStatement.setObject(5, null);
                    employeeIds.add(1);
                } else {
                    preparedStatement.setInt(5, faker.random().nextInt(1, employeeIds.size() + n));
                    employeeIds.add(i);
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            System.out.println("Сотрудники добавлены");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

    }
}
