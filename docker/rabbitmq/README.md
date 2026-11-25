# RabbitMQ definitions

`definitions.json` contains local account hashes and is intentionally ignored by Git.

To create it:

1. Copy `definitions.example.json` to `definitions.json`.
2. Generate a hash for your application password with RabbitMQ's `hash_password` command.
3. Replace `REPLACE_WITH_GENERATED_HASH` in the new file.
4. Keep `RABBITMQ_APP_PASSWORD` in `.env`, which is also ignored by Git.

RabbitMQ imports the definitions through `rabbitmq.conf`. An already initialized broker
requires an explicit definitions import; boot-time import does not overwrite existing data.
