package net.forprogrammers.almosts3.test.dsl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.UUID;

public class TokenHelper {
    private static final RSAKey rsaJWK;
    private static final String jwtId = UUID.randomUUID().toString();

    static {
        try {
            rsaJWK = new RSAKeyGenerator(2048)
                    .keyID("sso")
                    .keyUse(KeyUse.SIGNATURE)
                    .generate();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateUserToken(String username, WireMockServer authServer) {
        try {
            JWSSigner signer = new RSASSASigner(rsaJWK);
            return generateToken(username, signer, authServer);
        } catch (JOSEException e) {
            return "";
        }
    }

    public String generateInvalidToken(String username, WireMockServer authServer) {
        try {
            RSAKey pubkey = new RSAKeyGenerator(2048)
                    .keyID("sso")
                    .keyUse(KeyUse.SIGNATURE)
                    .generate();
            JWSSigner signer = new RSASSASigner(pubkey);
            return generateToken(username, signer, authServer);
        } catch (JOSEException e) {
            return "";
        }
    }

    private String generateToken(String username, JWSSigner signer, WireMockServer authServer) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .jwtID(jwtId)
                .audience("clientid")
                .issuer("https://xakep.ru")
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .claim("account_id", username)
                .build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);
        signedJWT.sign(signer);

        RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();
        String pubkey = rsaPublicJWK.toJSONObject().toString();
        authServer.stubFor(WireMock.get(WireMock.urlEqualTo("/pubkey"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"keys\":[" + pubkey + "]}")
                )
        );
        return signedJWT.serialize() + "=";
    }

}
