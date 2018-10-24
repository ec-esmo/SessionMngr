# Required environmental variables
- ASYNC_SIGNATURE= boolean, if true denotes RSA signing for JWTs, else HS256 signing is conducted
- KEYSTORE_PATH = path to the keystore holding the RSA certificate used for signing JWTs
- KEY_PASS= password for the certificate
- STORE_PASS=  password for the keystore containing the certificate
- CERT_ALIAS= alias of the certificate in the keystore
- SIGNING_SECRET= HS256 secret used for symmetric signing of jwts, e.g. QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=
- ISSUER= the name of the SessionManager MicroService that will be used as a claim in the JWT
- EXPIRES= how many minutes in the future the JWT will expire in