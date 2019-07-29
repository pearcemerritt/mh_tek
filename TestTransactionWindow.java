import org.junit.Test;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class TestTransactionWindow {
    private static final double EQUALITY_ASSERTION_DELTA = 0.0000001;

    @Test
    public void initializesAmountToZero() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        assertEquals(0.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void ignoresNullTransaction() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        unit.addTransaction(null);
        assertEquals(0.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void addingTransactionUpdatesTotal() throws Exception {
        TransactionWindow unit = new TransactionWindow();

        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");
        unit.addTransaction(new Transaction(dateTime, 10.0));
        assertEquals(10.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void transactionsInSameDayAddToTotal() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, 10.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), 20.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(2), 5.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(3), 5.0));
        assertEquals(40.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void transactionsInEarlierDaysDropFromTotal() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        // All transactions in same day, thus contribute to total
        unit.addTransaction(new Transaction(dateTime, 10.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), 20.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(2), 30.0));
        assertEquals(60.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);

        // $10 transaction dropped from window and no longer contribute to total
        unit.addTransaction(new Transaction(dateTime.plusHours(25), 40.0));
        assertEquals(90.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);

        // $20 transaction dropped from window
        unit.addTransaction(new Transaction(dateTime.plusHours(26), 50.0));
        assertEquals(120.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);

        // $30 transaction dropped from window
        unit.addTransaction(new Transaction(dateTime.plusHours(27), 60.0));
        assertEquals(150.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);

        // all previous transactions dropped from window
        unit.addTransaction(new Transaction(dateTime.plusDays(10), 5.0));
        assertEquals(5.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void handlesZeroAmountTransactions() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, 0.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), 0.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(2), 0.0));
        assertEquals(0.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void handlesNegativeTransactionAmounts() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, -10.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), -20.0));
        assertEquals(-30.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void handlesNegativeAndPositiveTransactionAmounts() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, 10.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), -10.99));
        assertEquals(-0.99, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void handlesLargeTransactionAmountGap() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("2019-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, -1000000000.0));
        unit.addTransaction(new Transaction(dateTime.plusHours(1), 1000000001.0));
        assertEquals(1.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }

    @Test
    public void handlesLargeTransactionDateTimeGap() throws Exception {
        TransactionWindow unit = new TransactionWindow();
        LocalDateTime dateTime = LocalDateTime.parse("1980-01-01T00:00:00");

        unit.addTransaction(new Transaction(dateTime, 10.0));
        unit.addTransaction(new Transaction(dateTime.plusYears(1000).plusDays(200).plusHours(12), 20.0));
        assertEquals(20.0, unit.getTotalAmount(), EQUALITY_ASSERTION_DELTA);
    }
}