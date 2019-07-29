import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * TransactionWindow
 * Represents a 24-hour window of time that contains a list of transactions
 * and exposes the sum of every transaction in the time window. When a new
 * more recent transaction is added, the time window will be adjusted such
 * that the end of the window is the same as the transaction's date and time.
 * When the window is adjusted, any existing transactions that are no longer
 * in the 24-hour window will be removed and the sum will no longer include
 * their transaction amounts. This class is designed with the intention that
 * transactions will be added chronologically from earliest to latest.
 * Using it in another way will have undefined results.
 */
public class TransactionWindow {
    private LocalDateTime endDateTime;
    private double totalAmount;
    private LinkedList<Transaction> transactions;
    public TransactionWindow() {
        totalAmount = 0.0;
        transactions = new LinkedList<Transaction>();
    }
    /**
     * The sum of the amount of all transactions in the window
     */
    public double getTotalAmount() {
        return totalAmount;
    }
    /**
     * Add the latest transaction to the window. The end of the window
     * will now be the date and time of the transaction and any transactions
     * outside the 24-hour window will now be discarded
     * @param transaction null will be ignored
     */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;
        
        endDateTime = transaction.getDateTime();
        totalAmount += transaction.getAmount();
        transactions.addLast(transaction);

        LocalDateTime startDateTime = endDateTime.minusDays(1);
        boolean someTransactionsOnDifferentDay = true;
        Transaction firstTransaction = transactions.getFirst();
        while (someTransactionsOnDifferentDay) {
            if (firstTransaction.getDateTime().isBefore(startDateTime)) {
                totalAmount -= firstTransaction.getAmount();
                transactions.removeFirst();
                firstTransaction = transactions.getFirst();
            }
            else {
                someTransactionsOnDifferentDay = false;
            }
        }
    }
}