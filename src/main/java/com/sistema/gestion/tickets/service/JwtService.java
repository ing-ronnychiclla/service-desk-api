package com.sistema.gestion.tickets.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Esta es tu firma digital. En producción, esto JAMÁS va en el código,
    // se lee de variables de entorno (ej. AWS Secrets Manager o un .env).
    // Para que funcione, debe ser una clave en Base64 de al menos 256 bits (32 caracteres).
    private static final String SECRET_KEY = "Q2liZXJ0ZWMtU2VydmljZURlc2stQXBwLVNlY3JldC1LZXktUGFyYS1GaXJtYXItVG9rZW5zLTIwMjY=";

    // ==========================================
    // 1. GENERACIÓN DEL TOKEN
    // ==========================================
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims) // Datos extra que quieras enviar (ej. roles)
                .setSubject(userDetails.getUsername()) // El "dueño" del token (el email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Expira en 24 horas
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Algoritmo de firma
                .compact();
    }

    // ==========================================
    // 2. EXTRACCIÓN DE DATOS (LECTURA)
    // ==========================================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // Usa la misma llave secreta para abrirlo
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==========================================
    // 3. VALIDACIÓN DEL TOKEN
    // ==========================================
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Es válido si el usuario coincide y el token no ha caducado
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Método auxiliar para transformar el texto plano a una Key criptográfica
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
