package com.ibrahim.studentserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.studentserver.beans.Role;
import com.ibrahim.studentserver.beans.User;
import com.ibrahim.studentserver.config.JwtService;
import com.ibrahim.studentserver.exceptions.GeneralKnownException;
import com.ibrahim.studentserver.repositories.UserRepository;
import com.ibrahim.studentserver.token.Token;
import com.ibrahim.studentserver.token.TokenRepository;
import com.ibrahim.studentserver.token.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository ;

    private final TokenRepository tokenRepository ;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService ;

    private final AuthenticationManager authenticationManager ;

    public AuthenticationResponse register(RegisterRequest request) throws GeneralKnownException {

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();
        if(userRepository.findByEmail(user.getEmail()).isEmpty()){
            var savedUser = userRepository.save(user) ;
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user) ;
            saveUserToken(savedUser, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        }else {
            throw  new GeneralKnownException("The userEmail already exist !") ;
        }

    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refresToken = jwtService.generateRefreshToken(user) ;
        revokeAlUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refresToken)
                .build();
    }

    public void revokeAlUserTokens(User user){
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId()) ;
        if(validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(t ->{
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens) ;
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token) ;
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken ;
        final String userEmail ;
        if(authHeader == null ||!authHeader.startsWith("Bearer ")){
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken) ;
        if(userEmail != null ){
            var  user = this.userRepository.findByEmail(userEmail).orElseThrow() ;
            if(jwtService.isTokenValid(refreshToken, user)) {
               var accessToken = jwtService.generateToken(user) ;
                revokeAlUserTokens(user);
                saveUserToken(user, accessToken);
               var authResponse = AuthenticationResponse.builder()
                       .accessToken(accessToken)
                       .refreshToken(refreshToken)
                       .build();
               new ObjectMapper().writeValue(response.getOutputStream(), authResponse) ;
            }
        }
    }
}
