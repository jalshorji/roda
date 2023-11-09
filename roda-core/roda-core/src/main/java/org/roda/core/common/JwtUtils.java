/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthenticationDeniedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JwtUtils {

  private JwtUtils() {
    // empty constructor
  }

  public static String generateToken(String subject, Date expirationDate) {
    return generateToken(subject, expirationDate, new HashMap<>());
  }

  public static String generateToken(String subject, Date expirationDate, Map<String, Object> claims) {
    SecretKey secretKey = Keys.hmacShaKeyFor(RodaCoreFactory.getApiSecretKey().getBytes(StandardCharsets.UTF_8));
    return Jwts.builder().signWith(secretKey, Jwts.SIG.HS256).issuedAt(new Date(System.currentTimeMillis()))
      .subject(subject).expiration(expirationDate).claims(claims).compact();
  }

  public static String regenerateToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    String subject = getSubjectFromToken(token);
    Date expiredDate = getExpiredDateFromToken(token);

    return generateToken(subject, expiredDate, claims);
  }

  private static Claims getClaimsFromToken(String token) throws AuthenticationDeniedException {
    try {
      SecretKey secretKey = Keys.hmacShaKeyFor(RodaCoreFactory.getApiSecretKey().getBytes(StandardCharsets.UTF_8));
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (Exception e) {
      throw new AuthenticationDeniedException("invalid token");
    }
  }

  public static String getSubjectFromToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    return claims.getSubject();
  }

  private static Date getExpiredDateFromToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    return claims.getExpiration();
  }

  public static boolean isTokenExpired(String token) throws AuthenticationDeniedException {
    Date expiredDate = getExpiredDateFromToken(token);
    return expiredDate.before(new Date());
  }

  public static boolean validateToken(String token) {
    try {
      getSubjectFromToken(token);
      return true;
    } catch (AuthenticationDeniedException e) {
      return false;
    }
  }

}
