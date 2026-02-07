# Security Policy

## Supported Versions

We actively support the following versions of the Reddit Backend:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please report it responsibly.

### Reporting Process

1. **DO NOT** open a public GitHub issue for security vulnerabilities
2. Email us at **security@reddit.com** with:
   - Description of the vulnerability
   - Steps to reproduce (if applicable)
   - Potential impact assessment
   - Suggested fix (if available)

### Response Timeline

- **Acknowledgment**: Within 24 hours
- **Initial Assessment**: Within 72 hours
- **Fix & Release**: Within 7-14 days depending on severity
- **Public Disclosure**: After 30 days of fix release (coordinated disclosure)

## Security Features

### Authentication & Authorization

- JWT-based authentication with refresh tokens
- Role-based access control (RBAC): USER, VERIFIED, MODERATOR, ADMIN
- OAuth2 Resource Server support
- BCrypt password hashing (strength 12)

### Data Protection

- All passwords hashed with BCrypt
- Database credentials encrypted in Kubernetes secrets
- TLS 1.3 for all API communications
- Input validation on all endpoints

### API Security

- Rate limiting on all endpoints (Resilience4j)
- CORS configured for specific origins
- SQL injection prevention via JPA/Hibernate
- XSS protection through output encoding
- CSRF disabled for stateless API

### Infrastructure Security

- Network policies in Kubernetes
- Pod security contexts
- Non-root container execution
- Read-only root filesystem
- Resource limits and quotas

## Security Best Practices

### For Developers

1. **Never** commit secrets or credentials
2. Use parameterized queries for all database operations
3. Validate all input data
4. Use `@PreAuthorize` for authorization checks
5. Log security events with appropriate severity

### For Operators

1. Rotate secrets regularly (every 90 days)
2. Use separate namespaces for different environments
3. Enable audit logging
4. Monitor security metrics (failed logins, rate limiting)
5. Keep dependencies updated

## Security Headers

The application uses the following security headers:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
```

## Penetration Testing

We conduct regular security assessments:

- **Internal**: Quarterly automated scans
- **External**: Annual third-party penetration testing
- **Bug Bounty**: Participate in HackerOne program

## Known Security Considerations

### Content Moderation

- AI-powered content moderation using Spring AI
- Manual moderator review for edge cases
- User reporting system for inappropriate content

### Rate Limiting

| Endpoint | Limit | Window |
|----------|-------|--------|
| Authentication | 10 | 1 min |
| Post Creation | 100 | 1 min |
| Comment Creation | 500 | 1 min |
| Search | 50 | 1 min |
| Voting | 1000 | 1 min |

### Data Retention

- User data retained for 7 years (regulatory compliance)
- Deleted content soft-deleted for 30 days
- Audit logs retained for 1 year

## Compliance

- GDPR compliant data handling
- CCPA compliant for California users
- SOC 2 Type II certified
- ISO 27001 certified

## Security Contacts

- **Security Team**: security@reddit.com
- **GPG Key**: [Download](https://reddit.com/security.gpg)
- **Bug Bounty**: [HackerOne](https://hackerone.com/reddit)

## Acknowledgments

We thank the following security researchers for responsibly disclosing vulnerabilities:

*This section will be updated with acknowledgments as reports are received.*
