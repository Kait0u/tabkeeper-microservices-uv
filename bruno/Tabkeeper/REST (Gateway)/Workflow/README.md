# Tabkeeper gateway happy-path workflow

Run all requests in numbered order with the gateway available at localhost:8080 and
its user, group, and expense services running. Every run creates fresh users and a
fresh group, so no existing data is required.

The workflow verifies user creation, group creation, invitation acceptance, membership,
expense creation and histories, balance calculation, repayment allocation, repayment
histories, and the remaining balance after repayment.

Unlike the gRPC workflow, these HTTP scripts support Bruno Collection Runner and CLI.
The first request clears previous runtime variables, and later requests capture IDs
from creation responses.
