# Setup Instructions for constellation-scalafix-rules

This document provides step-by-step instructions for setting up the repository on GitHub and configuring Sonatype Central publishing.

## 1. Initialize Git Repository

```bash
cd constellation-scalafix-rules
git init
git add .
git commit -m "Initial commit: Extract scalafix rules from tessellation"
```

## 2. Create GitHub Repository

1. Go to https://github.com/Constellation-Labs
2. Click "New repository"
3. Name it: `constellation-scalafix-rules`
4. Description: "Custom Scalafix rules for Constellation Network Scala projects"
5. Keep it public (or private if preferred)
6. Don't initialize with README (we already have one)
7. Click "Create repository"

## 3. Push to GitHub

```bash
git remote add origin git@github.com:Constellation-Labs/constellation-scalafix-rules.git
git branch -M main
git push -u origin main
```

## 4. Configure Sonatype Central Publishing

### Prerequisites

You need:
1. A Sonatype Central account (https://central.sonatype.com/)
2. GPG key for signing artifacts
3. Verified domain/group ownership for `io.constellationnetwork`

### Generate GPG Key (if you don't have one)

```bash
gpg --gen-key
# Follow prompts, use your email

# List keys
gpg --list-secret-keys --keyid-format LONG

# Export public key
gpg --armor --export YOUR_KEY_ID

# Export private key for GitHub Actions (base64 encoded)
gpg --armor --export-secret-keys YOUR_KEY_ID | base64 > pgp-secret.txt
```

### Upload GPG Public Key

Upload your public key to key servers:
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### Configure GitHub Secrets

Go to repository Settings > Secrets and variables > Actions > New repository secret

Add these secrets:

1. **SONATYPE_USERNAME**: Your Sonatype Central username
2. **SONATYPE_PASSWORD**: Your Sonatype Central password (or token)
3. **PGP_SECRET**: The base64-encoded private key (content of pgp-secret.txt)
4. **PGP_PASSPHRASE**: Your GPG key passphrase
5. **PGP_KEY_ID**: Your GPG key ID (short form, last 8 characters)

## 5. Test Local Build

Before publishing, test the build locally:

```bash
cd constellation-scalafix-rules

# Compile for both Scala versions
sbt +compile

# Test local publishing
sbt +publishLocal

# Verify artifacts
ls ~/.ivy2/local/io.constellationnetwork/constellation-scalafix-rules_2.13/
```

## 6. Publish First Release

### Option A: Automatic via Git Tag

```bash
# Create and push a tag
git tag v0.1.0
git push origin v0.1.0

# GitHub Actions will automatically:
# 1. Build for Scala 2.12 and 2.13
# 2. Sign artifacts
# 3. Publish to Sonatype Central
# 4. Create a GitHub Release
```

### Option B: Manual via sbt

```bash
# Set credentials
export SONATYPE_USERNAME="your-username"
export SONATYPE_PASSWORD="your-password"
export PGP_PASSPHRASE="your-gpg-passphrase"

# Publish
sbt "++2.12.19" clean publishSigned sonatypeBundleRelease
sbt "++2.13.16" clean publishSigned sonatypeBundleRelease
```

## 7. Verify Publication

After publishing, verify on:
- Sonatype Central: https://central.sonatype.com/
- Maven Central Search: https://search.maven.org/search?q=g:io.constellationnetwork

Artifacts typically sync to Maven Central within 30 minutes to a few hours.

## 8. Update Tessellation to Use Published Artifact

Once published, update tessellation/build.sbt:

```scala
// Remove the local dependency and auto-publish hook (lines 21, 68-83, 90-100)
// Add published dependency instead:
ThisBuild / scalafixDependencies += "io.constellationnetwork" %% "constellation-scalafix-rules" % "0.1.0"

// Remove the scalafixRules project aggregate from root project
```

Also remove/archive the `tessellation/scalafix-rules/` directory:
```bash
cd tessellation
git rm -r scalafix-rules/
git commit -m "Remove scalafix-rules module, now published separately"
```

## 9. Update .scalafix.conf

No changes needed! The rules remain the same in `.scalafix.conf`:

```hocon
rules = [
    OrganizeImports,
    NoSetSum,
    NoSetMap,
    NoMapConcat
]
```

## Troubleshooting

### GPG Signing Issues

If you get GPG errors during publishing:

```bash
# Check GPG agent is running
gpgconf --launch gpg-agent

# Test signing
echo "test" | gpg --clearsign
```

### Sonatype Authentication

Make sure your Sonatype credentials are correct:
- For new Sonatype Central (central.sonatype.com), use your portal credentials
- Old Sonatype OSS (oss.sonatype.org) uses different credentials

### GitHub Actions Failures

Check the Actions tab for detailed logs. Common issues:
- Incorrect secrets configuration
- GPG key not properly base64 encoded
- Wrong Sonatype credentials

## Future Releases

For subsequent releases:

1. Make your changes to the rules
2. Update version in git tag (e.g., v0.2.0)
3. Push the tag
4. GitHub Actions handles the rest
5. Update dependency version in tessellation

## Maintenance

### Adding New Rules

1. Create new rule class in `src/main/scala/io/constellationnetwork/scalafix/`
2. Add rule to `scalafix.v1.Rule` service file
3. Test locally with `sbt +compile`
4. Update README.md with rule documentation
5. Tag and release new version

### Versioning Strategy

Follow semantic versioning:
- **Patch (0.1.x)**: Bug fixes to existing rules
- **Minor (0.x.0)**: New rules added, backward compatible
- **Major (x.0.0)**: Breaking changes to rule behavior
