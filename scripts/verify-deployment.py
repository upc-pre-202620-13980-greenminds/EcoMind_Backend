#!/usr/bin/env python3
"""Smoke-check OpenAPI exposure and authentication rejection without credentials."""

import argparse
import http.client
import json
import time
import urllib.error
import urllib.parse
import urllib.request


def check(base_url, timeout):
    results = []
    for route, expected in [("/v3/api-docs", 200), ("/api/v1/authentication/me", 401)]:
        started = time.monotonic()
        result = {"path": route, "expected_status": expected}
        try:
            request = urllib.request.Request(base_url + route, headers={"Accept": "application/json"})
            with urllib.request.urlopen(request, timeout=timeout) as response:
                result["status"] = response.status
                if route == "/v3/api-docs":
                    document = json.load(response)
                    if not isinstance(document, dict):
                        raise ValueError("Expected an OpenAPI object")
                    result["openapi"] = document.get("openapi")
                    paths = document.get("paths", {})
                    result["documented_paths"] = len(paths)
                    result["contract_valid"] = bool(
                        isinstance(result["openapi"], str) and isinstance(paths, dict)
                        and "/api/v1/authentication/sign-in" in paths
                    )
        except urllib.error.HTTPError as error:
            result["status"] = error.code
        except urllib.error.URLError as error:
            result["error"] = type(error).__name__
            result["cause"] = type(error.reason).__name__
        except (OSError, http.client.HTTPException, ValueError, TypeError) as error:
            result["error"] = type(error).__name__
        result["elapsed_seconds"] = round(time.monotonic() - started, 3)
        result["passed"] = result.get("status") == expected and (
            route != "/v3/api-docs" or result.get("contract_valid", False)
        )
        results.append(result)
    return results


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_url", help="Public HTTPS origin, without /api/v1")
    parser.add_argument("--timeout", type=float, default=120)
    args = parser.parse_args()
    parsed = urllib.parse.urlsplit(args.base_url)
    if (
        parsed.scheme != "https" or not parsed.hostname or parsed.username or parsed.password
        or parsed.query or parsed.fragment or parsed.path not in ("", "/")
    ):
        parser.error("Use an HTTPS origin without credentials, query parameters or a path.")
    if not 0 < args.timeout <= 300:
        parser.error("Timeout must be between 0 and 300 seconds.")
    results = check(args.base_url.rstrip("/"), args.timeout)
    print(json.dumps({"origin": args.base_url.rstrip("/"), "checks": results}, indent=2))
    return 0 if all(result["passed"] for result in results) else 1


if __name__ == "__main__":
    raise SystemExit(main())
