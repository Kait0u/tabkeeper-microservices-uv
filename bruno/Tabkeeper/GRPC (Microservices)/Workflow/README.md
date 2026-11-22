# Tabkeeper happy-path workflow

Run the requests in numbered order in a Bruno version that supports gRPC scripting.
Each run creates fresh users and a fresh group, so no existing data is required.

The workflow verifies user creation, group creation, invitation acceptance, membership,
expense creation and histories, balance calculation, repayment allocation, repayment
histories, and the remaining balance after repayment.

Bruno currently supports these gRPC scripts only for interactive requests. Run each
request individually in sequence; Collection Runner and CLI execution are not yet
supported for gRPC scripting. The first request clears previous runtime variables.
