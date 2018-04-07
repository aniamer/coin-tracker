package cointracker;
import cointracker.config.AppConfig;
import cointracker.services.CoinBaseService;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudkms.v1.CloudKMS;
import com.google.api.services.cloudkms.v1.CloudKMSScopes;
import com.google.api.services.cloudkms.v1.model.DecryptRequest;
import com.google.api.services.cloudkms.v1.model.DecryptResponse;
import com.google.api.services.cloudkms.v1.model.ListCryptoKeysResponse;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ServiceConfiguration {
  Logger log = LoggerFactory.getLogger(ServiceConfiguration.class);

  @Autowired
  private AppConfig appConfig;

  @Bean
  public static CloudKMS authorizedClient() throws IOException {
    // Create the credential
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    // Authorize the client using Application Default Credentials
    // @see https://g.co/dv/identity/protocols/application-default-credentials
    String application_credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    GoogleCredential credential ;
    if(!Strings.isNullOrEmpty(application_credentials)){
      File credentialsFile = new File(application_credentials);
      InputStream credentialsStream = new FileInputStream(credentialsFile);
      credential = GoogleCredential.fromStream(credentialsStream);
    } else {
      credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
    }


    // Depending on the environment that provides the default credentials (e.g. Compute Engine, App
    // Engine), the credentials may require us to specify the scopes we need explicitly.
    // Check for this case, and inject the scope if required.
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(CloudKMSScopes.all());
    }
    return new CloudKMS.Builder(transport, jsonFactory, credential)
      .setApplicationName("CloudKMS snippets")
      .build();
  }

  @Bean
  public Coinbase coinBase() throws Exception {

    CloudKMS cloudKMS = authorizedClient();
    CloudKMS.Projects.Locations.KeyRings.CryptoKeys cryptoKeys = cloudKMS.projects().locations().keyRings().cryptoKeys();

//    log.info(cloudKMS.projects().locations().keyRings().cryptoKeys().list(keyRingPath).execute().toPrettyString());
    DecryptRequest apiKeyDecryptReq = new DecryptRequest();
    apiKeyDecryptReq.setCiphertext(appConfig.getApiKey());
    DecryptRequest apiSecretDecryptReq = new DecryptRequest();
    apiSecretDecryptReq.setCiphertext(appConfig.getApiSecret());

    String coinBaseApiCryptoKey = String.format("projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
      "coin-tracker-200219", "global","api_keys","coinbase_api_key");
    log.info(coinBaseApiCryptoKey);
    DecryptResponse coinbaseApiKeyResponse = cryptoKeys.decrypt(coinBaseApiCryptoKey, apiKeyDecryptReq).execute();

    String coinbaseApiSecretCryptoKey = String.format("projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
      "coin-tracker-200219", "global","api_keys","coinbase_api_secret");
    log.info(coinbaseApiSecretCryptoKey);
    DecryptResponse coinbaseApiSecretResponse = cryptoKeys.decrypt(coinbaseApiSecretCryptoKey, apiSecretDecryptReq).execute();

    Coinbase coinbase = new CoinbaseBuilder().withApiKey(coinbaseApiKeyResponse.getPlaintext(), coinbaseApiSecretResponse.getPlaintext())
      .build();
    return coinbase;
  }

  @Bean
  public CoinBaseService coinBaseService() {
    return new CoinBaseService();
  }

}
