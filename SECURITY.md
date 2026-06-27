# Security Policy

## Supported Versions

We actively maintain and patch security vulnerabilities for the current major release cycle and the immediate previous stable long-term support (LTS) branch. 

| Version | Supported          |
| ------- | ------------------ |
| 1.1.x   | :white_check_mark: |
| 1.0.x   | :x:                |
| 0.9.x   | :white_check_mark: |
| < 0.9   | :x:                |

## Reporting a Vulnerability

We take the security of on-device AI architectures, hardware metrics collection, and model delivery very seriously. If you discover a security vulnerability within the Memo Cache Android client library or its associated backend resolver routing, please report it immediately.

**Do not open a public GitHub issue for security vulnerabilities.**

### How to Report

Please report security bugs by emailing the maintainer directly at **[Insert Your Email Here]**.

To help us triage and patch the issue quickly, please include:
1. **Description:** A detailed summary of the vulnerability.
2. **Impact:** The potential risk (e.g., local model exposure, memory leak exhaustion, or man-in-the-middle vector tampering).
3. **PoC:** A minimal proof-of-concept project, snippet, or step-by-step instructions to reproduce the flaw.

### What to Expect

* **Initial Acknowledgement:** You can expect a response acknowledging your report within **48 hours**.
* **Status Updates:** We will provide tracking updates at least once every **5 business days** while actively diagnosing and patching the issue.
* **Resolution & Disclosure:** If accepted, a patch will be applied to the next minor/patch release. We practice responsible disclosure and will publish a security advisory alongside the release notes only after a stable, verified fix is pushed to production. 

Thank you for helping keep the on-device AI ecosystem safe!
