package insert_lab3;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PaymentChannelsInsert extends BaseInsertClass{
    Connection connection;

    PaymentChannelsInsert(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    void insert() {
        String insertSQL = "INSERT INTO payment_channels (type_of_channel, commission_percentage, commission_fixed_amount) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            Map<String, Double> m = new HashMap<>();
            m.put("Банковский счёт", 0.0);
            m.put("Карта", 0.5);
            m.put("Yandex.Money", 1.0);
            m.put("QIWI", 1.0);
            for (Map.Entry<String, Double> entry : m.entrySet()) {
                String channel = entry.getKey();
                Double commission = entry.getValue();
                preparedStatement.setString(1, channel);
                preparedStatement.setBigDecimal(2, BigDecimal.valueOf(commission));
                preparedStatement.setBigDecimal(3, BigDecimal.valueOf(0));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            System.out.println("Каналы платежей добавлены");
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
