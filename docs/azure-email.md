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
An initial poll retains the provider operation ID for later status diagnosis if waiting times out. Diagnostics log only validated operation IDs, safe status/error codes and exception class names; they omit message bodies, recipients and credentials. No timeout triggers an automatic resend.
HTTP operations have 5-second connect and 10-second response/read timeouts, with no
automatic retries. Polling waits up to 60 seconds; an in-flight HTTP request can extend
the total request duration. Do not describe this as a strict 60-second end-to-end bound.

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
Its explicit button opens the shared EcoMind custom URI handled by the Android and Flutter clients. GET only renders the handoff;
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
The final branded revision `ca-ecomind-api--0000002` is Healthy/Running and serves
100% of traffic. Source `930cdcda590afa2a8d4a12f156527b6f5680c625` passed full H2/PostgreSQL
verification and both image builds in [CI](https://github.com/upc-pre-202620-13980-greenminds/EcoMind_Backend/actions/runs/43257139726). The deployed Linux AMD64 image
runs as `10001:10001` and is pinned to `sha256:9cda70edabf84f4cd3ad7acb2b22a608f571bac0b40044a603635f9012dfa46b`.

The new English verification and Spanish recovery messages were received in Gmail.
**Both initially arrived in spam**, and Gmail blocked the inline image there. The owner’s
known test messages were manually marked as not spam for layout inspection; the actual
bundled 888-pixel logo then loaded and rendered at 222 pixels. This is not proof of
automatic inbox placement or a fix for sender reputation. A verified custom sending
domain and further deliverability checks remain advisable before broader use.

The received verification code returned 201. The recovery button opened the public HTTPS
helper in the browser and preserved the same token in the existing Android URI. Reset
confirmation returned 204, old-password sign-in 401, new-password sign-in 200 and token
reuse 422. Logout returned 204, followed by 401 after client token disposal. The public
helper returned 200 in Spanish, malformed/missing tokens returned 400, and POST to the
helper and unauthenticated account access returned 401. The helper is absent from the
business OpenAPI document. The first probe during revision transition timed out; the
repeat after Healthy/Running passed every check. Installed-app behavior on a physical
Android device remains unverified.

All four demonstration accounts still authenticated after rollout, with one family,
two communities and four quests read back from the API. The new deployment did not
replace the private database. Private videos and personal mailbox screenshots stay
outside the repositories and do not constitute participant App Validation evidence.

Physical-device handoff is not established by these API or browser checks.

## Sender identity and spam follow-up — November 12

Gmail's original-message view reported **SPF PASS, DKIM PASS and DMARC PASS** for the
actual message initially classified as spam. The transport used TLS 1.3. Forward/reverse
DNS matched the Microsoft relay. MIME contained text/plain and related text/html/PNG
parts with the expected logo CID. These checks establish healthy authentication and
formatting; they do not reveal Gmail's private classification criteria.

`email.bicep` now manages the existing default `donotreply` sender's display name as
`EcoMind`. ARM validation, change preview, deployment and readback succeeded. The same
default address and live application image remain in use. The subsequent English
recovery message showed **EcoMind** as the sender and arrived directly in **Inbox**, with
SPF/DKIM/DMARC still passing. It was not manually moved. This mailbox had previously
marked earlier messages as not spam, so the result is influenced by recipient history;
it cannot establish that the name change fixed filtering for other users.

The owner confirmed there is no custom domain. The report's example `greenminds.pe`
returned NXDOMAIN and is not a verified sending domain. A future owned domain can
provide a recognizable sender such as `donotreply@mail.<owned-domain>` with the exact
SPF/DKIM records returned by Azure and an aligned DMARC policy. Its API link can use a
sibling HTTPS name under the same brand. Verify DNS, certificates, both real message
types and initial placement in another consenting inbox before making broader claims.
Custom domains improve control of sender identity; they do not guarantee inbox placement.

No whitelist, bulk warm-up traffic, authentication-policy change or removal of the
requested logo was used as a workaround. No custom domain was purchased. The historical
spam results remain part of the evidence.

Sources: [Gmail sender guidelines](https://support.google.com/mail/answer/81126),
[Azure domain types](https://learn.microsoft.com/azure/communication-services/concepts/email/email-domain-and-sender-authentication),
and [managed-domain sender update](https://learn.microsoft.com/powershell/module/az.communication/update-azemailservicesenderusername).

## References

- [Microsoft retirement and onboarding notice](https://learn.microsoft.com/azure/communication-services/acs-retirement-and-breaking-changes-guide).
- [Managed-domain email quotas](https://learn.microsoft.com/azure/communication-services/concepts/service-limits#email).
- [Email pricing](https://azure.microsoft.com/pricing/details/communication-services/).
- [Java email SDK](https://learn.microsoft.com/java/api/com.azure.communication.email.emailclientbuilder?view=azure-java-stable).
