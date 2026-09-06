package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import java.util.List;
import java.util.UUID;

public record IamPrincipal(UUID userId, boolean mustChangePassword, List<String> authorities) {
}
