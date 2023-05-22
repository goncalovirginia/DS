#rm -f *.jks

keytool -genkey -alias feeds2-ourorg2 -ext SAN=dns:feeds2-ourorg2 -keyalg RSA -validity 365 -keystore ./feeds2-ourorg2.jks -storetype pkcs12 << EOF
feeds2-ourorg2
feeds2-ourorg2
feeds2-ourorg2.feeds2-ourorg2
TP2
SD2223
LX
LX
PT
yes
EOF

echo
echo
echo "Exporting Certificates"
echo
echo

keytool -exportcert -alias feeds2-ourorg2 -keystore feeds2-ourorg2.jks -file feeds2-ourorg2.cert << EOF
feeds2-ourorg2
EOF

echo "Creating Client Truststore"
#cp cacerts client-ts.jks
keytool -importcert -file feeds2-ourorg2.cert -alias feeds2-ourorg2 -keystore client-ts.jks << EOF
changeit
yes
EOF