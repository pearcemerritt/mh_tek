import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class TestAfterPayCodingChallenge {
    @Test
    public void throwsExceptionForNullTransactions() throws Exception {
        try {
            AfterPayCodingChallenge.identifyFraudulentCards(null, 0.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals("No transactions in input list", e.getMessage());
        }
    }

    @Test
    public void handlesNoTransactions() throws Exception {
        Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(new String[] {}, 0.0);
        assertEquals(new HashSet<String>(), actual);
    }

    @Test
    public void throwsExceptionWhenTransactionMalformed() throws Exception {
        try {
            String[] transactionStrings = new String[] { "no commas" };
            Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals(
                "Could not process transaction: \"no commas\". Must be three comma-separated values.",
                e.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenCardHashNotHex() throws Exception {
        try {
            String[] transactionStrings = new String[] { "abcdefghijklmnopqrstuvwxyz_, 2014-04-29T13:15:54, 10.00" };
            Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals(
                "Could not process transaction. Could not parse cardHash: \"abcdefghijklmnopqrstuvwxyz_\".Card hash must be a 27 digit hexidecimal integer.",
                e.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenCardHashTooShort() throws Exception {
        try {
            String[] transactionStrings = new String[] { "123, 2014-04-29T13:15:54, 10.00" };
            Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals(
                "Could not process transaction. Could not parse cardHash: \"123\".Card hash must be a 27 digit hexidecimal integer.",
                e.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenDateTimeMalformed() throws Exception {
        try {
            String[] transactionStrings = new String[] { "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29 at 13:15:54, 10.00" };
            Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals(
                "Could not process transaction. Could not parse timestamp: \"2014-04-29 at 13:15:54\".",
                e.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenAmountMalformed() throws Exception {
        try {
            String[] transactionStrings = new String[] { "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, ten dollars" };
            Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
            fail("expected an exception");
        } catch (TransactionException e) {
            assertEquals("Could not process transaction. Could not parse amount: \"ten dollars\".",
                e.getMessage());
        }
    }

    @Test
    public void handlesOneNonFraudulentCreditCard() throws Exception {
        String[] transactionStrings = new String[] {
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-30T09:10:47, 6.00",
            // sevearl transactions in a day, but well under $1000
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T19:22:17, 84.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:03:52, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:21:39, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T21:47:04, 6.29"
        };
        Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
        assertEquals(new HashSet<String>(), actual);
    }

    @Test
    public void handlesOneFraudulentCreditCard() throws Exception {
        String[] transactionStrings = new String[] {
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-30T09:10:47, 6.00",
            // sevearl transactions in a day totaling over $100
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T19:22:17, 84.92",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:03:52, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:21:39, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T21:47:04, 6.29"
        };
        Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 100.0);
        Set<String> expected = new HashSet<String>();
        expected.add("10d7ce2f43e35fa57d1bbf8b1e2");
        assertEquals(expected, actual);
    }

    @Test
    public void handlesMultipleFraudulentCreditCard() throws Exception {
        String[] transactionStrings = new String[] {
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00",
            "8ba97617e09f890bae837b7b2ad, 2014-04-29T14:11:26, 64.29",
            "668a9b73728a8f978fc801bb1a3, 2014-04-29T20:10:22, 3999.99",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-30T09:10:47, 6.37",
            "8ba97617e09f890bae837b7b2ad, 2014-04-30T09:10:47, 2.97",
            // over $100 spent by one card on cinco de may
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T19:22:17, 84.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:03:52, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T20:21:39, 6.29",
            "10d7ce2f43e35fa57d1bbf8b1e2, 2014-05-05T21:47:04, 6.29",
            // end cinco de mayo
            "8ba97617e09f890bae837b7b2ad, 2014-05-12T08:09:31, 85.32"
        };
        Set<String> actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 1000.0);
        Set<String> expected = new HashSet<String>();
        expected.add("668a9b73728a8f978fc801bb1a3");
        assertEquals(expected, actual);

        // Running again with a smaller threshold should flag more cards as fraudulent
        actual = AfterPayCodingChallenge.identifyFraudulentCards(transactionStrings, 100.0);
        expected.add("10d7ce2f43e35fa57d1bbf8b1e2");
        assertEquals(expected, actual);
    }
}