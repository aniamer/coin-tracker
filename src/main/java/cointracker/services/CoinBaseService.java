package cointracker.services;

import com.coinbase.api.Coinbase;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.TransactionsResponse;
import com.coinbase.api.exception.CoinbaseException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class CoinBaseService {
  @Autowired
  private Coinbase coinbase;

  public List<Transaction> transactions() throws IOException, CoinbaseException {
    TransactionsResponse transactions = coinbase.getTransactions();
    return transactions.getTransactions();
  }
}
