keytool -genkey -alias server -keyalg RSA -validity 365 -keystore microgramserver.ks -storetype pkcs12
keytool -exportcert -alias server -keystore microgramserver.ks -file microgramserver.cert
