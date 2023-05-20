
NAME="users0-ourorg0"

rm *.jks

keytool -genkey -alias "$NAME" -keyalg RSA -validity 365 -keystore ./"$NAME".jks -storetype pkcs12 << 'EOF'
$NAME
$NAME
$NAME.$NAME
TP2
SD2223
LX
LX
PT
yes
$NAME
$NAME
EOF

echo
echo
echo "Exporting Certificates"
echo
echo

keytool -exportcert -alias "$NAME" -keystore "$NAME".jks -file "$NAME".cert << 'EOF'
$NAME
EOF

echo "Creating Client Truststore"
cp cacerts client-ts.jks
keytool -importcert -file "$NAME".cert -alias "$NAME" -keystore client-ts.jks << EOF
changeit
yes
EOF