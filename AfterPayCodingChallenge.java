import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class AfterPayCodingChallenge {
    /**
     * A credit card will be identified as fraudulent if the sum of amounts for
     * a unique hashed credit card number over a 24 hour sliding window period
     * exceeds the a threshold.
     * @param transactionStrings a chronological list of comma-separated values:
     *     a hashed cedit card,
     *     a transaction timestamp of format "year-month-dayThour:minute:second"
     *     a transaction amount of format 'dollars.cents'
     * @param threshold the threshold over which any total amount of
     *     spending in a 24-hour period for one card is considered fraudulent
     * @return any credit cards identified as fraudulent
     */
    public static Set<String> identifyFraudulentCards(
        String[] transactionStrings, double threshold) throws TransactionException
    {
        
        if (transactionStrings == null)
            throw new TransactionException("No transactions in input list");

        Set<String> fraudulentCards = new HashSet<String>();
        Map<String, TransactionWindow> cardWindows =
            new HashMap<String, TransactionWindow>();

        for (String transactionString : transactionStrings) {
            String[] transactionFields = transactionString.split(", ");
            if (transactionFields.length != 3)
                throw new TransactionException("Could not process transaction: \"" +  
                    transactionString + "\". Must be three comma-separated values.");

            String cardHash = transactionFields[0];
            if (!cardHash.matches("[0-9a-f]{27}")) 
                throw new TransactionException("Could not process transaction. " + 
                    "Could not parse cardHash: \"" + cardHash + "\"." +
                    "Card hash must be a 27 digit hexidecimal integer.");

            if (fraudulentCards.contains(cardHash))
                continue;

            Transaction transaction =
                parseFromFields(transactionFields[1], transactionFields[2]);

            if (!cardWindows.containsKey(cardHash)) 
                cardWindows.put(cardHash, new TransactionWindow());

            TransactionWindow transactionWindow = cardWindows.get(cardHash);
            transactionWindow.addTransaction(transaction);

            if (transactionWindow.getTotalAmount() > threshold) 
                fraudulentCards.add(cardHash);
        }

        return fraudulentCards;
    }

    private static Transaction parseFromFields(String dateTimeString, String amountString)
        throws TransactionException
    {
        Transaction transaction;
        try {
            LocalDateTime transactionDateTime = LocalDateTime.parse(dateTimeString);
            double transactionAmount = Double.valueOf(amountString);

            transaction = new Transaction(transactionDateTime, transactionAmount);
        }
        catch (DateTimeParseException e) {
            throw new TransactionException("Could not process transaction. " +
                "Could not parse timestamp: \"" + dateTimeString + "\".", e);
        }
        catch (NumberFormatException e) {
            throw new TransactionException("Could not process transaction. " +
                "Could not parse amount: \"" + amountString + "\".", e);
        }

        return transaction;
    }
}