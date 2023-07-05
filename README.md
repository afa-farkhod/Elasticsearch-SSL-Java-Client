# [Elasticsearch-SSL-Java-Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/_encrypted_communication.html)
Secure HTTPS Connection to Elasticsearch Cluster with Java High-Level REST Client

## [Encrypted communication](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/_encrypted_communication.html)

- Encrypted communication using TLS can also be configured through the `HttpClientConfigCallback`. The `org.apache.http.impl.nio.client.HttpAsyncClientBuilder` received as an argument exposes multiple methods to configure encrypted communication: `setSSLContext`, `setSSLSessionStrategy` and `setConnectionManager`, in order of precedence from the least important.
- When accessing an Elasticsearch cluster that is setup for TLS on the HTTP layer, the client needs to trust the certificate that Elasticsearch is using. The following is an example of setting up the client to trust the CA that has signed the certificate that Elasticsearch is using, when that CA certificate is available in a PKCS#12 keystore:

```
Path trustStorePath = Paths.get("/path/to/truststore.p12");
KeyStore truststore = KeyStore.getInstance("pkcs12");
try (InputStream is = Files.newInputStream(trustStorePath)) {
    truststore.load(is, keyStorePass.toCharArray());
}
SSLContextBuilder sslBuilder = SSLContexts.custom()
    .loadTrustMaterial(truststore, null);
final SSLContext sslContext = sslBuilder.build();
RestClientBuilder builder = RestClient.builder(
    new HttpHost("localhost", 9200, "https"))
    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(
                HttpAsyncClientBuilder httpClientBuilder) {
            return httpClientBuilder.setSSLContext(sslContext);
        }
    });
```
- The following is an example of setting up the client to trust the CA that has signed the certificate that Elasticsearch is using, when that CA certificate is available as a PEM encoded file.

```
Path caCertificatePath = Paths.get("/path/to/ca.crt");
CertificateFactory factory =
    CertificateFactory.getInstance("X.509");
Certificate trustedCa;
try (InputStream is = Files.newInputStream(caCertificatePath)) {
    trustedCa = factory.generateCertificate(is);
}
KeyStore trustStore = KeyStore.getInstance("pkcs12");
trustStore.load(null, null);
trustStore.setCertificateEntry("ca", trustedCa);
SSLContextBuilder sslContextBuilder = SSLContexts.custom()
    .loadTrustMaterial(trustStore, null);
final SSLContext sslContext = sslContextBuilder.build();
RestClient.builder(
    new HttpHost("localhost", 9200, "https"))
    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder httpClientBuilder) {
            return httpClientBuilder.setSSLContext(sslContext);
        }
    });
```
- When Elasticsearch is configured to require client TLS authentication, for example when a PKI realm is configured, the client needs to provide a client certificate during the TLS handshake in order to authenticate. The following is an example of setting up the client for TLS authentication with a certificate and a private key that are stored in a PKCS#12 keystore.

```
Path trustStorePath = Paths.get("/path/to/your/truststore.p12");
Path keyStorePath = Paths.get("/path/to/your/keystore.p12");
KeyStore trustStore = KeyStore.getInstance("pkcs12");
KeyStore keyStore = KeyStore.getInstance("pkcs12");
try (InputStream is = Files.newInputStream(trustStorePath)) {
    trustStore.load(is, trustStorePass.toCharArray());
}
try (InputStream is = Files.newInputStream(keyStorePath)) {
    keyStore.load(is, keyStorePass.toCharArray());
}
SSLContextBuilder sslBuilder = SSLContexts.custom()
    .loadTrustMaterial(trustStore, null)
    .loadKeyMaterial(keyStore, keyStorePass.toCharArray());
final SSLContext sslContext = sslBuilder.build();
RestClientBuilder builder = RestClient.builder(
    new HttpHost("localhost", 9200, "https"))
    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder httpClientBuilder) {
            return httpClientBuilder.setSSLContext(sslContext);
        }
    });
```
- If the client certificate and key are not available in a keystore but rather as PEM encoded files, you cannot use them directly to build an SSLContext. You must rely on external libraries to parse the PEM key into a PrivateKey instance. Alternatively, you can use external tools to build a keystore from your PEM files, as shown in the following example:

```
openssl pkcs12 -export -in client.crt -inkey private_key.pem \
        -name "client" -out client.p12
```

## [Implementation](https://github.com/af4092/Elasticsearch-SSL-Java-Client/tree/main/src/ElasticHighLevelRemoteHTTPS/src/main/java/org/example)

- `ElasticHighLevelRemoteHTTPS` folder includes the `ElasticsearchConnectionCheckHTTPS`, the code demonstrates how to establish a secure connection to an Elasticsearch cluster using HTTPS, authenticate with a username and password, and retrieve basic cluster information. Remote Elasticsearch credentials must be entered. The following is the demo test run:

<p align="center">
  <img src="https://github.com/af4092/Elasticsearch-JavaAPI-connection/assets/24220136/37383a13-a438-49a2-9800-68312bb7b2d8" alt="Image">
</p>

- While making connection with the remote Elasictsearch server with SSH protection `HTTPS`, there is a need for Elasticsearch `http_ca.crt` certificate file. But the server uses a self-signed certificate or a certificate signed by a Certificate Authority (CA) that is not recognized by the Java truststore. To resolve this issue, we need to add the Elasticsearch server's SSL certificate to the truststore used by Java application.
   ```
   keytool -import -alias http_ca -file C:Users\User\Downloads\http_ca.crt -keystore truststore.jks
   ```
  - The above command can be used for other clients as well. The keytool command is a generic tool that can be used to manage certificates and keystores. The -import option is not specific to Java clients. It can be used to import certificates into any keystore that supports the JKS format.
  - The truststore is a file that contains a list of trusted certificates. When a client connects to a server using SSL/TLS, the client will check the server's certificate against the truststore. If the certificate is found in the truststore, the client will trust the server and the connection will be established.
  - The truststore can be used by any client that supports SSL/TLS. This includes Java clients, as well as clients written in other languages.
