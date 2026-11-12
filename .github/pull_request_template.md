## Change

Describe the resulting behavior and the related user stories.

## Validation

Include the tests executed and their results.

## Integration

- Target `develop` for features, `main` for releases or hotfixes.
- Use a Conventional Commit title.
- Keep credentials and local configuration out of the changes.

## Deployment

- Note any runtime configuration or database changes; keep migrations backward compatible.
- Describe the rollback plan when persisted data or external contracts change.
- A merge into `develop` deploys to the shared Azure API after all checks pass.
