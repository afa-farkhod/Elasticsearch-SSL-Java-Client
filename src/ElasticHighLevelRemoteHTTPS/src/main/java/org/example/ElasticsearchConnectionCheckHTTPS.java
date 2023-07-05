package org.example;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class ElasticsearchConnectionCheckHTTPS {

    public static void main(String[] args) {
        String host = "IP-ADDRESS";
        int port = PORT;
        String username = "USERNAME";
        String password = "PASSWORD";
        String truststorePath = "\path\to\truststore.jks";
        String truststorePassword = "TRUSTSTORE-PASSWORD";

        try {
            KeyStore truststore = KeyStore.getInstance("JKS");
            truststore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, port, "https"))
                                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider)
                                    .setSSLContext(sslContext)
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)));

            MainResponse response = client.info(RequestOptions.DEFAULT);
            String clusterName = response.getClusterName().toString();
            String clusterVersion = response.getVersion().toString();
            System.out.println("Connected to Elasticsearch cluster: " + clusterName + " (Version: " + clusterVersion + ")");

            client.close();
        } catch (Exception e) {
            System.out.println("Failed to connect to Elasticsearch server. Exception: " + e.getMessage());
        }
    }
}
