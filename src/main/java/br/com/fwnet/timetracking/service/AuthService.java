package br.com.fwnet.timetracking.service;

import br.com.fwnet.timetracking.dto.request.LoginRequest;
import br.com.fwnet.timetracking.dto.response.LoginResponse;
import br.com.fwnet.timetracking.entity.User;
import br.com.fwnet.timetracking.exception.UserNotFoundException;
import br.com.fwnet.timetracking.repository.UserRepository;
import br.com.fwnet.timetracking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                token,
                user.getFullName()
        );
    }
}