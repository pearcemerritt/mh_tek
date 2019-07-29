import java.time.LocalDateTime;

public class Transaction {
    private LocalDateTime dateTime;
    private double amount;
    public Transaction(LocalDateTime dateTime, double amount) throws TransactionException {
        if (dateTime == null)
            throw new TransactionException("Cannot create a transaction with a null dateTime");

        this.dateTime = dateTime;
        this.amount = amount;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public double getAmount() {
        return amount;
    }
}