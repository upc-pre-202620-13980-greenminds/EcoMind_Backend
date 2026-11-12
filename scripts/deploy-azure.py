#!/usr/bin/env python3
"""Update only the existing API image and verify its ready revision."""

import argparse
import json
import os
import re
import subprocess
import time
from pathlib import Path

from importlib.util import module_from_spec, spec_from_file_location

smoke_spec = spec_from_file_location("deployment_smoke", Path(__file__).with_name("verify-deployment.py"))
smoke = module_from_spec(smoke_spec)
smoke_spec.loader.exec_module(smoke)


def azure(*arguments):
    result = subprocess.run(
        ["az", *arguments, "--only-show-errors", "--output", "json"],
        capture_output=True, text=True, timeout=90, check=False,
    )
    if result.returncode:
        raise RuntimeError("Azure operation failed; inspect resource permissions and deployment state.")
    return json.loads(result.stdout) if result.stdout.strip() else None


def ready_revision(app, revision, expected_name, container_name, image):
    properties = revision.get("properties", {})
    containers = properties.get("template", {}).get("containers", [])
    return (
        app.get("properties", {}).get("latestReadyRevisionName") == expected_name
        and properties.get("active") is True
        and properties.get("healthState") == "Healthy"
        and properties.get("provisioningState") == "Provisioned"
        and any(container.get("name") == container_name and container.get("image") == image
                for container in containers)
    )


def rollout(resource_group, app_name, container_name, image, suffix, timeout=420):
    app_arguments = ["--resource-group", resource_group, "--name", app_name]
    before = azure("containerapp", "show", *app_arguments)
    if before["properties"]["configuration"]["activeRevisionsMode"] != "Single":
        raise RuntimeError("Expected Single revision mode before updating the API.")
    previous = before["properties"]["latestReadyRevisionName"]
    expected = app_name + "--" + suffix
    azure("containerapp", "update", *app_arguments, "--container-name", container_name,
          "--image", image, "--revision-suffix", suffix, "--no-wait")

    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        app = azure("containerapp", "show", *app_arguments)
        if app["properties"].get("latestReadyRevisionName") == expected:
            revision = azure("containerapp", "revision", "show", *app_arguments, "--revision", expected)
            if ready_revision(app, revision, expected, container_name, image):
                origin = "https://" + app["properties"]["configuration"]["ingress"]["fqdn"]
                checks = smoke.check(origin, 120)
                if not all(check["passed"] for check in checks):
                    raise RuntimeError("The new revision failed the public API smoke checks.")
                current = azure("containerapp", "show", *app_arguments)
                if current["properties"].get("latestReadyRevisionName") != expected:
                    raise RuntimeError("The ready revision changed during verification.")
                return {"revision": expected, "previous_revision": previous, "image": image,
                        "origin": origin, "checks": checks}
        if app["properties"].get("provisioningState") in ("Failed", "Canceled"):
            raise RuntimeError("Azure did not provision the requested revision.")
        time.sleep(5)
    raise RuntimeError("The requested image did not become the ready revision before the timeout.")


def deploy(resource_group, app_name, container_name, image, suffix, timeout=420):
    arguments = ["--resource-group", resource_group, "--name", app_name]
    before = azure("containerapp", "show", *arguments)
    properties = before["properties"]
    if properties.get("latestReadyRevisionName") != properties.get("latestRevisionName"):
        raise RuntimeError("Another revision is pending; leave it unchanged and retry after it finishes.")
    previous_image = next(container["image"] for container in properties["template"]["containers"]
                          if container["name"] == container_name)
    try:
        return rollout(resource_group, app_name, container_name, image, suffix, timeout)
    except (RuntimeError, subprocess.TimeoutExpired):
        current = azure("containerapp", "show", *arguments)
        containers = current["properties"]["template"]["containers"]
        current_image = next(container["image"] for container in containers if container["name"] == container_name)
        expected = app_name + "--" + suffix
        if current_image != image or current["properties"].get("latestRevisionName") != expected:
            raise RuntimeError("Deployment failed; another update is present, so no rollback was attempted.") from None
        try:
            restored = rollout(resource_group, app_name, container_name, previous_image, suffix + "-rollback", timeout)
        except (RuntimeError, subprocess.TimeoutExpired):
            raise RuntimeError("Deployment and rollback checks failed; inspect Azure before another deployment.") from None
        summary = os.environ.get("GITHUB_STEP_SUMMARY")
        if summary:
            with open(summary, "a", encoding="utf-8") as output:
                output.write(f"\nDeployment failed. Previous image restored and verified as `{restored['revision']}`.\n")
        raise RuntimeError("Deployment failed; the previous image was restored and verified.") from None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--image", required=True)
    parser.add_argument("--revision-suffix", required=True)
    args = parser.parse_args()
    registry = os.environ["AZURE_REGISTRY_SERVER"]
    if not re.fullmatch(re.escape(registry) + r"/ecomind-backend@sha256:[0-9a-f]{64}", args.image):
        parser.error("Expected this project's registry image pinned by SHA256 digest.")
    if not re.fullmatch(r"gh-[0-9]+-[0-9]+", args.revision_suffix):
        parser.error("Expected a GitHub run/attempt revision suffix.")
    result = deploy(os.environ["AZURE_RESOURCE_GROUP"], os.environ["AZURE_CONTAINER_APP"],
                    "ecomind-api", args.image, args.revision_suffix)
    print(json.dumps(result, indent=2))
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as output:
            output.write("\n### Azure deployment verified\n\n")
            output.write(f"- Revision: `{result['revision']}`\n- Image: `{result['image']}`\n")
            output.write(f"- Previous ready revision: `{result['previous_revision']}`\n")
            output.write(f"- Public API: {result['origin']}\n- OpenAPI 200; anonymous account access 401.\n")


if __name__ == "__main__":
    main()
