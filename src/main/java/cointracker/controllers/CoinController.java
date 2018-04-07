package cointracker.controllers;

import cointracker.services.CoinBaseService;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.exception.CoinbaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
public class CoinController {
  @Autowired
  CoinBaseService coinBaseService;

  @RequestMapping("/transactions")
  public List<Transaction> transctions() {
    List<Transaction> transactions = Collections.emptyList();
    try {
      transactions = coinBaseService.transactions();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CoinbaseException e) {
      e.printStackTrace();
    } finally {
      return transactions;
    }
  }
}
