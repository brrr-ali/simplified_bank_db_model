package insert_lab3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PaymentScheduleInsert extends BaseInsertClass {

    PaymentScheduleInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() {
        String loanQuery = "SELECT id, amount, date_start, date_end FROM loans";
        String insertPaymentSQL = "INSERT INTO payment_schedule (loan_id, planning_date, interest_payment, principal_payment) VALUES (?, ?, ?, ?)";
        String rateQuery = "SELECT percentage_rate FROM loan_rates WHERE id = ?";
        try (
                PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
                PreparedStatement rateStmt = connection.prepareStatement(rateQuery);
                PreparedStatement insertPaymentStmt = connection.prepareStatement(insertPaymentSQL)
        ) {
            ResultSet loanResultSet = loanStmt.executeQuery();

            while (loanResultSet.next()) {
                int loanId = loanResultSet.getInt("id");

                BigDecimal amount = loanResultSet.getBigDecimal("amount");
                LocalDateTime dateStart = loanResultSet.getTimestamp("date_start").toLocalDateTime();
                LocalDateTime dateEnd = loanResultSet.getTimestamp("date_end").toLocalDateTime();

                int totalMonths = (int) ChronoUnit.MONTHS.between(dateStart, dateEnd);
                int loanRateId = loanResultSet.getInt("loan_rate_id");
                rateStmt.setInt(1, loanRateId);
                ResultSet rateResultSet = rateStmt.executeQuery();
                BigDecimal monthlyInterestRate = BigDecimal.ZERO;
                if (rateResultSet.next()) {
                    BigDecimal annualRate = rateResultSet.getBigDecimal("percentage_rate");
                    monthlyInterestRate = annualRate.divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP);
                }
                generatePaymentSchedule(insertPaymentStmt, loanId, dateStart, amount, totalMonths, monthlyInterestRate);
            }

            System.out.println("Графики платежей успешно добавлены.");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void generatePaymentSchedule(PreparedStatement insertPaymentStmt, int loanId, LocalDateTime dateStart, BigDecimal amount, int months, BigDecimal monthlyInterestRate) throws SQLException {
        LocalDateTime currentPaymentDate = dateStart;
        BigDecimal monthlyPrincipalPayment = amount.divide(BigDecimal.valueOf(months), RoundingMode.DOWN);
        BigDecimal remainingPrincipal = amount.subtract(monthlyPrincipalPayment.multiply(BigDecimal.valueOf(months)));
        // todo: достать ставку по кредиту и умножить на количество дней прошедших
        for (int i = 0; i < months; i++) {
            insertPaymentStmt.setInt(1, loanId);
            insertPaymentStmt.setTimestamp(2, java.sql.Timestamp.valueOf(currentPaymentDate));

            BigDecimal interestPayment = remainingPrincipal.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            insertPaymentStmt.setBigDecimal(3, (i == months - 1) ? monthlyPrincipalPayment.add(amount.remainder(BigDecimal.valueOf(months))) : monthlyPrincipalPayment);
            insertPaymentStmt.setBigDecimal(4, interestPayment);

            remainingPrincipal = remainingPrincipal.subtract(monthlyPrincipalPayment);
            if (i == months - 1)
                remainingPrincipal = remainingPrincipal.subtract(amount.remainder(BigDecimal.valueOf(months)));

            insertPaymentStmt.executeUpdate();
            currentPaymentDate = currentPaymentDate.plusMonths(1);
        }
    }
}