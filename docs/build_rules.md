# Build Rules

Always build after ANY code change:

```bash
./gradlew clean :app:assembleDebug --no-build-cache
```

Also use when needed:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Never skip:
- `clean`
- `--no-build-cache`

Reason:
Gradle cache can generate:
- stale Compose compiler outputs
- corrupted intermediates
- manifest issues
- stale compiler errors

Never leave the project in a non-compiling state.

Always fix:
- Compose preview errors
- unresolved imports
- navigation errors
- state/type mismatches
