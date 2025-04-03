package insert_lab3;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

import static java.lang.Double.parseDouble;

public class LoansInsert extends BaseInsertClass {
    public LoansInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() throws SQLException {
        String insertSQL = "INSERT INTO loans (loanRateApprovedForClients, amount, date_start, date_end, interest_start_date) VALUES (?, ?, ?, ?, ?)";
        int maxScoringClientId = getMaxIdFromTable("scoring_clients");

        String loanRatesSQL = "SELECT id, min_amount, max_amount, min_term, max_term FROM loan_rates";

        try (PreparedStatement loanRatesStatement = connection.prepareStatement(loanRatesSQL);
             ResultSet loanRatesResultSet = loanRatesStatement.executeQuery()) {

            while (loanRatesResultSet.next()) {
                int loanRateId = loanRatesResultSet.getInt("id");
                BigDecimal minAmount = BigDecimal.valueOf(parseDouble(loanRatesResultSet.getString("min_amount").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")));
                BigDecimal maxAmount = BigDecimal.valueOf(parseDouble(loanRatesResultSet.getString("max_amount").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")));

                int minTerm = loanRatesResultSet.getInt("min_term");
                int maxTerm = loanRatesResultSet.getInt("max_term");
                BigDecimal amount = minAmount.add(maxAmount.subtract(minAmount).multiply(BigDecimal.valueOf(faker.random().nextDouble())));
                int term = faker.random().nextInt(maxTerm - minTerm + 1) + minTerm;
                LocalDateTime dateStart = LocalDateTime.now().minusMonths(faker.random().nextInt(term));
                LocalDateTime dateEnd = dateStart.plusMonths(term);
                LocalDateTime interestStartDate = dateStart.plusMonths(faker.random().nextInt(3));

                String scoringClientsSQL = "SELECT id FROM scoring_clients WHERE loan_rate_id = ? ORDER BY RANDOM() LIMIT 1";
                try (PreparedStatement scoringClientsStatement = connection.prepareStatement(scoringClientsSQL)) {
                    scoringClientsStatement.setInt(1, loanRateId);
                    try (ResultSet scoringClientsResultSet = scoringClientsStatement.executeQuery()) {
                        if (scoringClientsResultSet.next()) {
                            int scoringClientId = scoringClientsResultSet.getInt("id");

                            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                                preparedStatement.setInt(1, scoringClientId);
                                preparedStatement.setObject(2, amount);
                                preparedStatement.setTimestamp(3, Timestamp.valueOf(dateStart));
                                preparedStatement.setTimestamp(4, Timestamp.valueOf(dateEnd));
                                preparedStatement.setTimestamp(5, Timestamp.valueOf(interestStartDate));

                                preparedStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
            System.out.println("Кредиты добавлены");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}