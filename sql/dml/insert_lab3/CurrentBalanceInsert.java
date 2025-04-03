package insert_lab3;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.Double.parseDouble;

public class CurrentBalanceInsert extends BaseInsertClass {
    CurrentBalanceInsert(Connection connection) {
        super(connection);
    }
    @Override
    void insert() throws SQLException {
        String loanQuery = "SELECT id, amount FROM loans";
        String interestAccrualQuery = "SELECT SUM(percentage_amount) AS total_interest, SUM(penalties) AS total_penalties FROM InterestAccrual WHERE loan_id = ?";
        String paymentsQuery = "SELECT SUM(amount) AS total_payments FROM payments WHERE loan_id = ?";
        try (PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
             ResultSet loanResultSet = loanStmt.executeQuery()) {

            while (loanResultSet.next()) {
                int loanId = loanResultSet.getInt("id");
                String amountString = loanResultSet.getString("amount"); // Save the amount string

                double loanAmount = 0.0;
                if (amountString != null) { // Check for null before processing
                    loanAmount = parseDouble(
                            amountString.replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")
                    );
                } else {
                    throw new SQLException("Amount cannot be null for loan_id: " + loanId);
                }

                double totalInterest = 0.0;
                double totalPenalties = 0.0;

                try (PreparedStatement interestStmt = connection.prepareStatement(interestAccrualQuery)) {
                    interestStmt.setInt(1, loanId);
                    try (ResultSet interestResultSet = interestStmt.executeQuery()) {
                        if (interestResultSet.next()) {
                            String totalInterestString = interestResultSet.getString("total_interest");
                            if (totalInterestString != null) {
                                totalInterest = parseDouble(
                                        totalInterestString.replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")
                                );
                            }

                            String totalPenaltiesString = interestResultSet.getString("total_penalties");
                            if (totalPenaltiesString != null) {
                                totalPenalties = parseDouble(
                                        totalPenaltiesString.replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")
                                );
                            }
                        }
                    }
                }

                double totalPayments = 0.0;
                try (PreparedStatement paymentsStmt = connection.prepareStatement(paymentsQuery)) {
                    paymentsStmt.setInt(1, loanId);
                    try (ResultSet paymentsResultSet = paymentsStmt.executeQuery()) {
                        if (paymentsResultSet.next()) {
                            String totalPaymentsString = paymentsResultSet.getString("total_payments"); // Fix reference to paymentsResultSet
                            if (totalPaymentsString != null) { // Check for null
                                totalPayments = parseDouble(
                                        totalPaymentsString.replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", "")
                                );
                            }
                        }
                    }
                }
                double principalBalance = loanAmount - totalPayments;
                double accruedInterest = totalInterest;
                double penaltyAmount = totalPenalties;
                double serviceFees = 0.0; // todo: select from paymentChannels * payment.amount

                String insertBalanceQuery = "INSERT INTO currentBalance (loan_id, principal_balance, accrued_interest, penalty_amount, service_fees) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (loan_id) DO UPDATE SET " +
                        "principal_balance = excluded.principal_balance, " +
                        "accrued_interest = excluded.accrued_interest, " +
                        "penalty_amount = excluded.penalty_amount, " +
                        "service_fees = excluded.service_fees";

                try (PreparedStatement insertStmt = connection.prepareStatement(insertBalanceQuery)) {
                    insertStmt.setInt(1, loanId);
                    insertStmt.setObject(2, BigDecimal.valueOf(principalBalance));
                    insertStmt.setBigDecimal(3, BigDecimal.valueOf(accruedInterest));
                    insertStmt.setBigDecimal(4, BigDecimal.valueOf(penaltyAmount));
                    insertStmt.setBigDecimal(5, BigDecimal.valueOf(serviceFees));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   /* @Override
    void insert() throws SQLException {
        String loanQuery = "SELECT id, amount FROM loans";
        String interestAccrualQuery = "SELECT SUM(percentage_amount) AS total_interest, SUM(penalties) AS total_penalties FROM InterestAccrual WHERE loan_id = ?";
        String paymentsQuery = "SELECT SUM(amount) AS total_payments FROM payments WHERE loan_id = ?";
        try (PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
             ResultSet loanResultSet = loanStmt.executeQuery()) {

            while (loanResultSet.next()) {
                int loanId = loanResultSet.getInt("id");
                double loanAmount = parseDouble(loanResultSet.getString("amount").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));


                double totalInterest = 0.0;
                double totalPenalties = 0.0;

                try (PreparedStatement interestStmt = connection.prepareStatement(interestAccrualQuery)) {
                    interestStmt.setInt(1, loanId);
                    try (ResultSet interestResultSet = interestStmt.executeQuery()) {
                        if (interestResultSet.next()) {
                            totalInterest = parseDouble(interestResultSet.getString("total_interest").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                            totalPenalties = parseDouble(interestResultSet.getString("total_penalties").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                        }
                    }
                }

                double totalPayments = 0.0;
                try (PreparedStatement paymentsStmt = connection.prepareStatement(paymentsQuery)) {
                    paymentsStmt.setInt(1, loanId);
                    try (ResultSet paymentsResultSet = paymentsStmt.executeQuery()) {
                        if (paymentsResultSet.next()) {
                            totalPayments = parseDouble(loanResultSet.getString("total_payments").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                        }
                    }
                }
                double principalBalance = loanAmount - totalPayments;
                double accruedInterest = totalInterest;
                double penaltyAmount = totalPenalties;
                double serviceFees = 0.0; // todo: select from paymentChannels * payment.amount

                String insertBalanceQuery = "INSERT INTO currentBalance (loan_id, principal_balance, accrued_interest, penalty_amount, service_fees) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (loan_id) DO UPDATE SET " +
                        "principal_balance = excluded.principal_balance, " +
                        "accrued_interest = excluded.accrued_interest, " +
                        "penalty_amount = excluded.penalty_amount, " +
                        "service_fees = excluded.service_fees";

                try (PreparedStatement insertStmt = connection.prepareStatement(insertBalanceQuery)) {
                    insertStmt.setInt(1, loanId);
                    insertStmt.setObject(2, BigDecimal.valueOf(principalBalance));
                    insertStmt.setBigDecimal(3, BigDecimal.valueOf(accruedInterest));
                    insertStmt.setBigDecimal(4, BigDecimal.valueOf(penaltyAmount));
                    insertStmt.setBigDecimal(5, BigDecimal.valueOf(serviceFees));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } *//*
    @Override
    void insert() throws SQLException {
        String loanQuery = "SELECT id, amount FROM loans";
        String interestAccrualQuery = "SELECT SUM(percentage_amount) AS total_interest, SUM(penalties) AS total_penalties FROM InterestAccrual WHERE loan_id = ?";
        String paymentsQuery = "SELECT SUM(amount) AS total_payments FROM payments WHERE loan_id = ?";

        try (PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
             ResultSet loanResultSet = loanStmt.executeQuery()) {

            while (loanResultSet.next()) {
                int loanId = loanResultSet.getInt("id");
                String amountStr = loanResultSet.getString("amount");
                double loanAmount = 0.0;
                if (amountStr != null) {
                    loanAmount = parseDouble(amountStr.replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                } else {
                    throw new SQLException("Amount cannot be null for loan_id: " + loanId);
                }

                double totalInterest = 0.0;
                double totalPenalties = 0.0;

                try (PreparedStatement interestStmt = connection.prepareStatement(interestAccrualQuery)) {
                    interestStmt.setInt(1, loanId);
                    try (ResultSet interestResultSet = interestStmt.executeQuery()) {
                        if (interestResultSet.next()) {
                            totalInterest = interestResultSet.getBigDecimal("total_interest") != null
                                    ? interestResultSet.getBigDecimal("total_interest").doubleValue()
                                    : 0.0;
                            totalPenalties = interestResultSet.getBigDecimal("total_penalties") != null
                                    ? interestResultSet.getBigDecimal("total_penalties").doubleValue()
                                    : 0.0;
                        }
                    }
                }

                double totalPayments = 0.0;
                try (PreparedStatement paymentsStmt = connection.prepareStatement(paymentsQuery)) {
                    paymentsStmt.setInt(1, loanId);
                    try (ResultSet paymentsResultSet = paymentsStmt.executeQuery()) {
                        if (paymentsResultSet.next()) {
                            totalPayments = paymentsResultSet.getBigDecimal("total_payments") != null
                                    ? paymentsResultSet.getBigDecimal("total_payments").doubleValue()
                                    : 0.0;
                        }
                    }
                }

                double principalBalance = loanAmount - totalPayments;
                double accruedInterest = totalInterest;
                double penaltyAmount = totalPenalties;
                double serviceFees = 0.0; // todo: select from paymentChannels * payment.amount

                String insertBalanceQuery = "INSERT INTO currentBalance (loan_id, principal_balance, accrued_interest, " +
                        "penalty_amount, service_fees) VALUES (?, ?, ?, ?, ?) ON CONFLICT (loan_id) " +
                        "DO UPDATE SET principal_balance = excluded.principal_balance, " +
                        "accrued_interest = excluded.accrued_interest, penalty_amount = excluded.penalty_amount, " +
                        "service_fees = excluded.service_fees";

                try (PreparedStatement insertStmt = connection.prepareStatement(insertBalanceQuery)) {
                    insertStmt.setInt(1, loanId);
                    insertStmt.setBigDecimal(2, BigDecimal.valueOf(principalBalance));
                    insertStmt.setBigDecimal(3, BigDecimal.valueOf(accruedInterest));
                    insertStmt.setBigDecimal(4, BigDecimal.valueOf(penaltyAmount));
                    insertStmt.setBigDecimal(5, BigDecimal.valueOf(serviceFees));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/ /*
    @Override
    void insert() throws SQLException {
        String loanQuery = "SELECT id, amount FROM loans";
        String interestAccrualQuery = "SELECT SUM(percentage_amount) AS total_interest, SUM(penalties) AS total_penalties FROM InterestAccrual WHERE loan_id = ?";
        String paymentsQuery = "SELECT SUM(amount) AS total_payments FROM payments WHERE loan_id = ?";

        try (PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
             ResultSet loanResultSet = loanStmt.executeQuery()) {

            while (loanResultSet.next()) {
                int loanId = loanResultSet.getInt("id");
                double loanAmount = parseDouble(loanResultSet.getString("amount").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));

                double totalInterest = 0.0;
                double totalPenalties = 0.0;


                try (PreparedStatement interestStmt = connection.prepareStatement(interestAccrualQuery)) {
                    interestStmt.setInt(1, loanId);
                    try (ResultSet interestResultSet = interestStmt.executeQuery()) {
                        if (interestResultSet.next()) {
                            totalInterest = parseDouble(interestResultSet.getString("total_interest").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                            totalPenalties = parseDouble(interestResultSet.getString("total_penalties").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                        }
                    }
                }

                double totalPayments = 0.0;
                try (PreparedStatement paymentsStmt = connection.prepareStatement(paymentsQuery)) {
                    paymentsStmt.setInt(1, loanId);
                    try (ResultSet paymentsResultSet = paymentsStmt.executeQuery()) {
                        if (paymentsResultSet.next()) {
                            totalPayments = parseDouble(loanResultSet.getString("total_payments").replace(" ", "").replace("?", "").replace(",", ".").replace("\u00A0", ""));
                        }
                    }
                }
                double principalBalance = loanAmount - totalPayments;
                double accruedInterest = totalInterest;
                double penaltyAmount = totalPenalties;
                double serviceFees = 0.0; // todo: select from paymentChannels * payment.amount

                String insertBalanceQuery = "INSERT INTO currentBalance (loan_id, principal_balance, accrued_interest, penalty_amount, service_fees) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (loan_id) DO UPDATE SET " +
                        "principal_balance = excluded.principal_balance, " +
                        "accrued_interest = excluded.accrued_interest, " +
                        "penalty_amount = excluded.penalty_amount, " +
                        "service_fees = excluded.service_fees";

                try (PreparedStatement insertStmt = connection.prepareStatement(insertBalanceQuery)) {
                    insertStmt.setInt(1, loanId);
                    insertStmt.setBigDecimal(2, BigDecimal.valueOf(principalBalance));
                    insertStmt.setBigDecimal(3, BigDecimal.valueOf(accruedInterest));
                    insertStmt.setBigDecimal(4, BigDecimal.valueOf(penaltyAmount));
                    insertStmt.setBigDecimal(5, BigDecimal.valueOf(serviceFees));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } */
}
