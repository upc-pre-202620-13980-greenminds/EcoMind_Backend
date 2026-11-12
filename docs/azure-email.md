# Azure email and demonstration data

## Scope and service limits

The Azure environment uses an Azure-managed email domain for IAM verification and
password recovery. Resend remains available through the same `EmailService` port.
Set `EMAIL_PROVIDER=azure` or `resend`; development uses the existing console adapter
unless the `email` profile is explicitly enabled. Automated tests never send email.

Microsoft has announced retirement of ACS Email on **September 30, 2028** and currently
permits onboarding new email resources, subject to change. This is suitable for the
current course validation environment, not a permanent provider commitment. The
managed domain is limited to **5 messages/minute and 10/hour**, shared by all users.
Plan demos within that quota. Do not request recovery messages for reserved demo
addresses; they cannot receive email. No production abuse protection or higher quota
is claimed by this deployment.

Reference pricing is USD 0.00025 per email plus USD 0.00012 per MB transferred, before
taxes. A provider success means the message is out for delivery, not that it reached an
inbox. Confirm receipt separately. A timeout is an **unknown outcome**: it does not
cancel provider processing, and retrying may produce a duplicate or an obsolete code.
HTTP operations have 5-second connect and 10-second response/read timeouts, with no
automatic retries. Polling waits up to 20 seconds; an in-flight HTTP request can extend
the total request duration. Do not describe this as a strict 20-second end-to-end bound.

## Reproducible setup

`infra/azure/email.bicep` creates the email service, Azure-managed domain, communication
resource and a Key Vault secret. ARM resolves the generated connection string directly
into `azure-email-connection-string`; it is never a template output or local credential.
Engagement tracking is disabled. There is no new service principal or runtime vault
permission. The connection string grants access to this dedicated communication
resource; it is not an email-only RBAC credential.

```bash
bash scripts/azure.sh deployment group what-if -g rg-ecomind-prod \
  --template-file infra/azure/email.bicep
bash scripts/azure.sh deployment group create -g rg-ecomind-prod \
  --template-file infra/azure/email.bicep
```

Verify the domain, SPF, DKIM, DKIM2 and DMARC states. The actual deployment on November
12 returned `Verified` for all five. Its sender is
`DoNotReply@4d885c80-0190-4ef1-b5b3-888f9aa6f8e6.azurecomm.net`.

Deploy a tested immutable application image using `infra/azure/application.bicep`, with
`emailProvider=azure`, `azureEmailFromEmail` set to that sender, and
`passwordRecoveryUrl` set to the public HTTPS `/authentication/reset-password` route. `emailProvider` is a
required root parameter so maintenance cannot silently revert to Resend. The nested
module resolves the mail secret through Key Vault into an ACA native secret reference.
No sender address or connection string is accepted as evidence of real delivery.

For key rotation, redeploy `email.bicep`, then the application template and restart or
revise running replicas. Secret updates alone do not refresh a running process. Keep
the previous working revision/image for rollback. Test a real inbox after rotation.

## Demonstration data

On November 12 the owner requested new test data instead of waiting for the old
Render/Aiven environment. **These are new synthetic records, not migrated users or
historical activity.** Content and profiles use `Demo` labels. Accounts use reserved
`@example.invalid` addresses and independently generated BCrypt passwords. Passwords
and hashes are not committed or printed in evidence.

The one-time `demo-data.bicep` wrapper accepts private additive SQL and resolves only
the `ecomind_app` database password. Its job uses PostgreSQL 17, `verify-full`, a
transaction, generated IDs and an advisory lock. The run inserted only missing demo
accounts/profiles and did not overwrite existing accounts. The successful job was
removed. Do not leave jobs with database credentials available after a run, including
failed runs. SQL hashes can appear in PostgreSQL error logs if a statement fails;
do not reuse real account credentials for these fixtures.

Subsequent sample data was created through authenticated API calls and checked with
GET requests. A private journal tracks successful writes to avoid duplicate replay.

| Demonstration content | Verified amount |
| --- | ---: |
| Accounts and profiles | 4 |
| Family group | 1, with a parent and two children |
| Accepted friendship | 1 |
| Communities | 2, local and thematic |
| Community goal and sample post | 1 each |
| Sample event | 1, explicitly marked as not a real event |
| Minigame | 1 |
| Published quests | 4 |
| Checkbox activity steps | 6 |

The event creator is registered automatically as an individual by the existing domain
service; the seed preserves that registration rather than creating a conflicting second
family registration. Learning materials and store catalogs are supplied by the existing
application startup seeds. No payments, push notifications or emails were sent while
creating these fixtures. Do not mistake sample completion for environmental measurement.

## Email presentation and recovery entry

Both providers use the same table-based HTML layout, inline styles and the official
Android/Flutter EcoMind PNG (`888 × 324` source pixels). The logo is bundled in the
application and attached inline with content ID `ecomind-logo`; email rendering does
not fetch an image from the API. English and Spanish messages retain plain-text
alternatives, expiry and account-security guidance. No third-party fonts or tracking
pixels are included. Gmail and other clients can still choose to hide images; the
image has an EcoMind alternative label and the message remains readable without it.

Gmail strips custom-scheme email links. Azure therefore uses the public HTTPS helper
`https://ca-ecomind-api.icybush-bd135296.westus2.azurecontainerapps.io/authentication/reset-password`.
Its explicit button opens the existing Android custom URI. GET only renders the handoff;
it never validates, consumes or resets the token. The page rejects malformed tokens,
uses no scripts or external assets, and sets no-store, no-referrer, noindex and a
restrictive CSP. Only this GET route is public. The existing reset API remains the
single-use confirmation operation. A device with EcoMind installed is required.
The token still appears in the HTTPS URL and can enter browser history or infrastructure
request logs; the headers do not erase those records. Treat the link as a short-lived
credential and do not record it in screenshots, videos or report evidence.

## Executed validation

The first Azure adapter revision (`632111c`) delivered a real English verification
message and a Spanish recovery message to the owner's Gmail inbox on November 12.
Verification returned 201, sign-in 200, recovery confirmation 204, the old password 401,
the new password 200 and reuse of the reset token 422. Logout returned 204; after
client-side token disposal the protected endpoint returned 401. The existing logout
contract is stateless and does not revoke already issued JWTs server-side.
The received sender includes the regional `.us2.azurecomm.net` suffix, while Azure
configuration uses the managed-domain sender shown above.

The subsequent branded template and HTTPS helper passed 19 focused tests. Fixture
previews generated by the actual Java content factory were visually reviewed at desktop
and 390-pixel mobile widths. A fixture preview is not mailbox delivery evidence.
The final branded revision's rollout and mailbox checks must be recorded separately.
Physical-device handoff is not established by these API or browser checks.

## References

- [Microsoft retirement and onboarding notice](https://learn.microsoft.com/azure/communication-services/acs-retirement-and-breaking-changes-guide).
- [Managed-domain email quotas](https://learn.microsoft.com/azure/communication-services/concepts/service-limits#email).
- [Email pricing](https://azure.microsoft.com/pricing/details/communication-services/).
- [Java email SDK](https://learn.microsoft.com/java/api/com.azure.communication.email.emailclientbuilder?view=azure-java-stable).
