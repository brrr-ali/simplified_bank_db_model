package insert_lab3;

import java.sql.Connection;

public class PaymentsInsert extends BaseInsertClass{

    PaymentsInsert(Connection connection) {
        super(connection);
    }

    @Override
    void insert() {
        // пройтись по schedule и поставить те же числа только на день раньше

    }
}
