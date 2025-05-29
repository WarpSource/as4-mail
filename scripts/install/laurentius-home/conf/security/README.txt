keystore.p12

NOTE: Use the keytool from JDK 17 and above, where support was added for specifying a signer of the certificate using
the keytool -genkeypair. See: https://www.oracle.com/java/technologies/javase/17-relnote-issues.html

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias issuer-ca -keyalg ED25519 -sigalg ED25519 \
        -storepass passwd1234 -keypass passwd1234 \
        -ext bc:c,ca:true,pathlen:2 \
        -dname "CN=issuer-ca,OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU"  \
        -validity 3651

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias ed25519 -keyalg ED25519 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=ed25519, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias ed448 -keyalg ED448 \
         -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
         -storepass passwd1234 -keypass passwd1234 \
         -dname "CN=ed448, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
         -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias x25519 -keyalg X25519 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=x25519, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias x448 -keyalg X448 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=x448, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias secp256r1 -keyalg EC -groupname secp256r1 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=secp256r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias secp384r1 -keyalg EC -groupname secp384r1 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=secp384r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650

/opt/java/jdk-17.0.10/bin/keytool -genkeypair -keystore keystore.p12 -alias secp521r1 -keyalg EC -groupname secp521r1 \
        -sigalg ED25519 -signer issuer-ca  -signerkeypass passwd1234 \
        -storepass passwd1234 -keypass passwd1234 \
        -dname "CN=secp521r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
        -validity 3650


Following commands are used to generate self-signed certificates/keys for brainpool curves:
 brainpoolP256r1, brainpoolP384r1, brainpoolP512r1
 To generate certificate used JDK 11 - 15  and remove curve from jdk.disabled.namedCurves property
 in the java.security file. (For generating keystore.p12 the jdk-11.0.22 was used)
To generated for brainpoolP256r1, brainpoolP384r1, brainpoolP512r1 use the following command:

keytool -genkeypair -keystore keystore.p12 -alias brainpoolP256r1 -keyalg EC  -groupname brainpoolP256r1 \
    -storepass passwd1234 -keypass passwd1234  \
    -dname "CN=brainpoolP256r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
    -validity 3650

keytool -genkeypair -keystore keystore.p12 -alias brainpoolP384r1 -keyalg EC  -groupname brainpoolP384r1 \
    -storepass passwd1234 -keypass passwd1234  \
    -dname "CN=brainpoolP384r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
    -validity 3650

keytool -genkeypair -keystore keystore.p12 -alias brainpoolP512r1 -keyalg EC  -groupname brainpoolP512r1 \
    -storepass passwd1234 -keypass passwd1234  \
    -dname "CN=brainpoolP512r1, OU=eDeliveryAS4-2.0,OU=as4mail,O=warp-source,C=EU" \
    -validity 3650


# create truststore\

for cert in ed25519 ed448 x25519 x448 secp256r1 secp384r1 secp521r1 brainpoolP256r1 brainpoolP384r1 brainpoolP512r1; \
do \
    keytool -exportcert -keystore keystore.p12 -alias ${cert} -file ${cert}.crt -storepass passwd1234
    keytool -importcert -file ${cert}.crt -keystore root-ca.jks -storetype JKS -alias ${cert} -storepass passwd1234
done