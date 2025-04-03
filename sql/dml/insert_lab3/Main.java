package insert_lab3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String dbUrl = "jdbc:postgresql://localhost:5432/bank2";
        String username = "postgres";
        String password = "6110";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            if (connection != null) {
                System.out.println("Успех");
            } else {
                System.out.println("Не удалось подключиться");
            }
            new ClientsInsert(connection).insert();
            new EmployeesInsert(connection).insert();
            new PaymentChannelsInsert(connection).insert();
            new ScoringClientsInsert(connection).insert();
            new LoanRatesInsert(connection).insert();
            new LoansInsert(connection).insert();
            new InterestAccrualInsert(connection).insert();
            new PaymentScheduleInsert(connection).insert();
            new PaymentsInsert(connection).insert();
            new CurrentBalanceInsert(connection).insert();
        } catch (SQLException e) {
            System.out.println("Ошибка при подключении к базе данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
