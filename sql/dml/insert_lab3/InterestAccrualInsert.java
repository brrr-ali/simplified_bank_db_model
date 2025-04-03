/*package insert_lab3;

import jdk.internal.org.objectweb.asm.tree.InsnList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.parseDouble;

public class InterestAccrualInsert extends BaseInsertClass {
    InterestAccrualInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() throws SQLException {
        String insertSQL = "INSERT INTO InterestAccrual (loan_id, accrual_date, percentage_amount, penalties) VALUES (?, ?, ?, ?)";
        String selectInfoAboutLoan = "SELECT loan_id, planning_date, interest_payment, principal_payment FROM payment_schedule";
        try (PreparedStatement pstmt = connection.prepareStatement(selectInfoAboutLoan);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp planningDate = rs.getTimestamp("planning_date");
                BigDecimal amount = BigDecimal.valueOf(parseDouble(rs.getString("amount").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")));
                // todo: считай оставшуюся инфу
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

                }
                // todo: пройтись по датам и начиная с даты начисления процентов, каждый месяц до текущей даты, добавлять
                //  по (посмотри как считаются проценты - вроде годовая ставка / 12 * кол-во дней)
                // сделать запрос и получить инфу по те
                preparedStatement.setInt(1, i);
                preparedStatement.setTimestamp(2, Timestamp.valueOf(faker.date().
                        past(300, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
                // todo: как считаются проценты
                // preparedStatement.setObject(3, faker.commerce().price(100, 1000));
                preparedStatement.setObject(4, faker.commerce().price(0, 500));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            System.out.println("Начисления по процентам добавлены");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
} */
package insert_lab3;

import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class InterestAccrualInsert extends BaseInsertClass {
    private Faker faker = new Faker();

    InterestAccrualInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() throws SQLException {
        String insertSQL = "INSERT INTO InterestAccrual (loan_id, accrual_date, percentage_amount, penalties) VALUES (?, ?, ?, ?)";
        String selectInfoAboutLoan = "SELECT ps.loan_id, ps.planning_date, ps.interest_payment, sc.loan_rate_id " +
                                      "FROM payment_schedule ps " +
                                      "JOIN loans l ON ps.loan_id = l.id JOIN  scoring_clients sc ON sc.id = l.loanrateapprovedforclients";

        String selectLoanRate = "SELECT percentage_rate FROM loan_rates WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectInfoAboutLoan);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int loanId = rs.getInt("loan_id");
                Timestamp planningDate = rs.getTimestamp("planning_date");
                BigDecimal interestPayment = rs.getBigDecimal("interest_payment");
                int loanRateId = rs.getInt("loan_rate_id");
                BigDecimal annualRate;
                try (PreparedStatement loanRateStmt = connection.prepareStatement(selectLoanRate)) {
                    loanRateStmt.setInt(1, loanRateId);
                    try (ResultSet loanRateRs = loanRateStmt.executeQuery()) {
                        if (loanRateRs.next()) {
                            annualRate = loanRateRs.getBigDecimal("percentage_rate");
                        } else {
                            continue;
                        }
                    }
                }
                LocalDate startDate = planningDate.toLocalDateTime().toLocalDate();
                LocalDate currentDate = LocalDate.now();
                long daysBetween = java.time.Duration.between(startDate.atStartOfDay(), currentDate.atStartOfDay()).toDays();

                BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(365), BigDecimal.ROUND_HALF_UP);
                BigDecimal percentageAmount = dailyRate.multiply(BigDecimal.valueOf(daysBetween)).multiply(interestPayment);
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                    preparedStatement.setInt(1, loanId);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(currentDate.atStartOfDay()));
                    preparedStatement.setObject(3, percentageAmount);
                    preparedStatement.setObject(4, faker.commerce().price(0, 500));
                    preparedStatement.addBatch();
                }
            }
            // Выполняем пакетную вставку
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.executeBatch();
                System.out.println("Начисления по процентам добавлены");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}