"""Offline tests for Azure Container Apps deployment safety gates."""

import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch


SCRIPT = Path(__file__).with_name("deploy-azure.py")
SPEC = importlib.util.spec_from_file_location("deploy_azure", SCRIPT)
deploy_azure = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(deploy_azure)


RESOURCE_GROUP = "rg-test"
APP_NAME = "api-test"
CONTAINER = "ecomind-api"
REGISTRY = "example.azurecr.io/ecomind-backend"
NEW_IMAGE = REGISTRY + "@sha256:" + ("a" * 64)
OLD_IMAGE = REGISTRY + "@sha256:" + ("b" * 64)
OTHER_IMAGE = REGISTRY + "@sha256:" + ("c" * 64)
NEW_SUFFIX = "gh-123-1"
NEW_REVISION = APP_NAME + "--" + NEW_SUFFIX
OLD_REVISION = APP_NAME + "--previous"
OTHER_REVISION = APP_NAME + "--other"
ROLLBACK_REVISION = APP_NAME + "--" + NEW_SUFFIX + "-rollback"


def app_state(ready_revision, latest_revision, image, provisioning="Succeeded"):
    return {
        "properties": {
            "latestReadyRevisionName": ready_revision,
            "latestRevisionName": latest_revision,
            "provisioningState": provisioning,
            "configuration": {
                "activeRevisionsMode": "Single",
                "ingress": {"fqdn": "api.example.test"},
            },
            "template": {"containers": [{"name": CONTAINER, "image": image}]},
        }
    }


def revision_state(image, active=True, health="Healthy", provisioning="Provisioned"):
    return {
        "properties": {
            "active": active,
            "healthState": health,
            "provisioningState": provisioning,
            "template": {"containers": [{"name": CONTAINER, "image": image}]},
        }
    }


PASSING_SMOKE = [{"passed": True}, {"passed": True}]
FAILING_SMOKE = [{"passed": True}, {"passed": False}]


class ReadyRevisionTests(unittest.TestCase):
    def test_only_exact_ready_revision_and_digest_match(self):
        ready = app_state(NEW_REVISION, NEW_REVISION, NEW_IMAGE)
        revision = revision_state(NEW_IMAGE)

        self.assertTrue(deploy_azure.ready_revision(
            ready, revision, NEW_REVISION, CONTAINER, NEW_IMAGE
        ))

        self.assertFalse(deploy_azure.ready_revision(
            app_state(OLD_REVISION, NEW_REVISION, NEW_IMAGE),
            revision,
            NEW_REVISION,
            CONTAINER,
            NEW_IMAGE,
        ), "an older ready revision must not satisfy the deployment gate")
        self.assertFalse(deploy_azure.ready_revision(
            ready,
            revision_state(OLD_IMAGE),
            NEW_REVISION,
            CONTAINER,
            NEW_IMAGE,
        ), "the container digest must match the requested image exactly")

    def test_unhealthy_or_unprovisioned_revision_is_not_ready(self):
        ready = app_state(NEW_REVISION, NEW_REVISION, NEW_IMAGE)
        self.assertFalse(deploy_azure.ready_revision(
            ready,
            revision_state(NEW_IMAGE, health="Unhealthy"),
            NEW_REVISION,
            CONTAINER,
            NEW_IMAGE,
        ))
        self.assertFalse(deploy_azure.ready_revision(
            ready,
            revision_state(NEW_IMAGE, provisioning="Provisioning"),
            NEW_REVISION,
            CONTAINER,
            NEW_IMAGE,
        ))


class DeploymentTests(unittest.TestCase):
    def setUp(self):
        self.previous = app_state(OLD_REVISION, OLD_REVISION, OLD_IMAGE)
        self.deployed = app_state(NEW_REVISION, NEW_REVISION, NEW_IMAGE)

    def run_deploy(self):
        return deploy_azure.deploy(
            RESOURCE_GROUP, APP_NAME, CONTAINER, NEW_IMAGE, NEW_SUFFIX, timeout=1
        )

    @patch.object(deploy_azure.smoke, "check", return_value=PASSING_SMOKE)
    @patch.object(deploy_azure, "azure")
    def test_rollout_requires_exact_new_ready_revision_and_public_smoke(
        self, azure_mock, smoke_mock
    ):
        azure_mock.side_effect = [
            self.previous,
            None,
            self.deployed,
            revision_state(NEW_IMAGE),
            self.deployed,
        ]

        result = deploy_azure.rollout(
            RESOURCE_GROUP, APP_NAME, CONTAINER, NEW_IMAGE, NEW_SUFFIX, timeout=1
        )

        self.assertEqual(result["revision"], NEW_REVISION)
        self.assertEqual(result["previous_revision"], OLD_REVISION)
        self.assertEqual(result["image"], NEW_IMAGE)
        self.assertEqual(result["origin"], "https://api.example.test")
        smoke_mock.assert_called_once_with("https://api.example.test", 120)

    @patch.object(deploy_azure.smoke, "check", return_value=FAILING_SMOKE)
    @patch.object(deploy_azure, "azure")
    def test_public_smoke_failure_fails_rollout(self, azure_mock, _smoke_mock):
        azure_mock.side_effect = [
            self.previous,
            None,
            self.deployed,
            revision_state(NEW_IMAGE),
        ]

        with self.assertRaisesRegex(RuntimeError, "failed the public API smoke checks"):
            deploy_azure.rollout(
                RESOURCE_GROUP, APP_NAME, CONTAINER, NEW_IMAGE, NEW_SUFFIX, timeout=1
            )

    @patch.object(deploy_azure, "rollout")
    @patch.object(deploy_azure, "azure")
    def test_success_returns_rollout_summary(self, azure_mock, rollout_mock):
        azure_mock.return_value = self.previous
        expected = {
            "revision": NEW_REVISION,
            "previous_revision": OLD_REVISION,
            "image": NEW_IMAGE,
        }
        rollout_mock.return_value = expected

        result = self.run_deploy()

        self.assertEqual(result, expected)
        rollout_mock.assert_called_once_with(
            RESOURCE_GROUP, APP_NAME, CONTAINER, NEW_IMAGE, NEW_SUFFIX, 1
        )

    @patch.object(deploy_azure, "rollout")
    @patch.object(deploy_azure, "azure")
    def test_public_failure_rolls_back_and_verifies_previous_image(self, azure_mock, rollout_mock):
        azure_mock.side_effect = [self.previous, self.deployed]
        rollout_mock.side_effect = [
            RuntimeError("The new revision failed the public API smoke checks."),
            {"revision": ROLLBACK_REVISION, "image": OLD_IMAGE},
        ]

        with self.assertRaisesRegex(RuntimeError, "previous image was restored and verified"):
            self.run_deploy()

        self.assertEqual(rollout_mock.call_count, 2)
        self.assertEqual(
            rollout_mock.call_args_list[1].args,
            (RESOURCE_GROUP, APP_NAME, CONTAINER, OLD_IMAGE, NEW_SUFFIX + "-rollback", 1),
        )

    @patch.object(deploy_azure, "rollout")
    @patch.object(deploy_azure, "azure")
    def test_concurrent_update_prevents_blind_rollback(self, azure_mock, rollout_mock):
        another_update = app_state(OTHER_REVISION, OTHER_REVISION, OTHER_IMAGE)
        azure_mock.side_effect = [self.previous, another_update]
        rollout_mock.side_effect = RuntimeError("smoke check failed")

        with self.assertRaisesRegex(RuntimeError, "another update is present, so no rollback"):
            self.run_deploy()

        rollout_mock.assert_called_once()

    @patch.object(deploy_azure, "rollout")
    @patch.object(deploy_azure, "azure")
    def test_failed_rollback_remains_failed_for_operator_review(self, azure_mock, rollout_mock):
        azure_mock.side_effect = [self.previous, self.deployed]
        rollout_mock.side_effect = [RuntimeError("deployment failed"), RuntimeError("rollback failed")]

        with self.assertRaisesRegex(RuntimeError, "Deployment and rollback checks failed"):
            self.run_deploy()

        self.assertEqual(rollout_mock.call_count, 2)

    @patch.object(deploy_azure, "rollout")
    @patch.object(deploy_azure, "azure")
    def test_pending_revision_aborts_before_rollout(self, azure_mock, rollout_mock):
        azure_mock.return_value = app_state(OLD_REVISION, NEW_REVISION, OLD_IMAGE)

        with self.assertRaisesRegex(RuntimeError, "Another revision is pending"):
            self.run_deploy()

        azure_mock.assert_called_once()
        rollout_mock.assert_not_called()


if __name__ == "__main__":
    unittest.main()
