cp base-truststore.ks client-truststore.ks
echo "Use password: changeit"
keytool -importcert -file mediaserver.cert -alias mediastore -keystore client-truststore.ks
keytool -importcert -file microgramserver.cert -alias microgram -keystore client-truststore.ks
