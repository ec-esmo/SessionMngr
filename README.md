# Required environmental variables
- ASYNC_SIGNATURE= boolean, if true denotes RSA signing for JWTs, else HS256 signing is conducted
- KEYSTORE_PATH = path to the keystore holding the RSA certificate used for signing JWTs
- KEY_PASS= password for the certificate
- STORE_PASS=  password for the keystore containing the certificate
- JWT_CERT_ALIAS= alias of the certificate in the keystore used to sign JWT tokens
- HTTPSIG_CERT_ALIAS=alias of the certificate in the keystore used to sign HttpSignature messages
- SIGNING_SECRET= HS256 secret used for symmetric signing of jwts, e.g. QjG+wP1CbAH2z4PWlWIDkxP4oRlgK2vos5/jXFfeBw8=
- ISSUER= the name of the SessionManager MicroService that will be used as a claim in the JWT
- EXPIRES= how many minutes in the future the JWT will expire in, this also denotes when a jwt is ejected from the blacklist cache


# Dependencies

For better scalability (load-balancing etc.) the blacklist of invalid JWTs will be stored outside the the SessionManager in a shared cache that multiple instances of the SessionManager can connect to
At the current implementation it is stored in a Memcached instance (https://memcached.org/) deployed as a docker container. For more information about the memcached deployment see memchached.yml in 
src files. 

This instance can be shared by other micro-services if we want such features for them as well. 